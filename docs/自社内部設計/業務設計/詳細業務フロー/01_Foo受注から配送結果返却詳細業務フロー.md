# DFL-001 Foo受注から配送結果返却詳細業務フロー

## 1. 目的
Foo社注文の受付から配送状態返却までを、Bar社営業時間制約による送信待ちを含めて、Hoge社内の主要処理単位、内部コンポーネント、データストア CRUD とあわせて整理する。

## 2. 設計書ID
| 項目 | 内容 |
| --- | --- |
| 設計書ID | `DFL-001` |
| 業務領域 | Foo受注、Bar連携、配送結果返却 |
| 逆引き対象処理設計書 | `PDS-001`, `PDS-002`, `PDS-003`, `PDS-004`, `PDS-005`, `PDS-006` |

## 3. 登場アクター・内部コンポーネント
- Foo社担当者
- Foo社システム
- HULFT受信
- OrderHub取込Batch
- 顧客マスタ管理
- 在庫引当管理
- OrderHubデータストア
- 注文受付通知Worker
- Bar送信待ちキュー
- 配送会社連携Worker
- 配送結果受付API
- Barシステム
- 配送状態取込Worker
- 配送結果返却Worker
- HULFT送信

## 4. 詳細業務フロー図
```mermaid
sequenceDiagram
    box rgb(255, 244, 236) 他社
        actor FooUser as Foo社担当者
        participant FooSys as Foo社システム
    end
    box rgb(237, 246, 255) 自社
        participant HulftRx as HULFT受信
        participant ImportBatch as OrderHub取込Batch
        participant CustApi as 顧客マスタ管理
        participant StockApi as 在庫引当管理
        participant OrderHubDb as OrderHubデータストア
        participant AckWorker as 注文受付通知Worker
        participant BarQueue as Bar送信待ちキュー
        participant ShipWorker as 配送会社連携Worker
        participant ResultApi as 配送結果受付API
        participant ResultWorker as 配送状態取込Worker
        participant ResultNotify as 配送結果返却Worker
        participant HulftTx as HULFT送信
    end
    box rgb(255, 244, 236) 他社
        participant BarSys as Barシステム
    end

    FooUser->>FooSys: 注文登録
    FooSys->>HulftRx: 注文ファイル送信
    HulftRx->>ImportBatch: 着信ファイル引渡し
    ImportBatch->>CustApi: 顧客確認
    CustApi-->>ImportBatch: 顧客確認結果
    ImportBatch->>StockApi: 在庫引当
    StockApi-->>ImportBatch: 在庫引当結果
    ImportBatch->>OrderHubDb: 注文ヘッダ / 注文明細 C
    Note over ImportBatch,OrderHubDb: 注文元=FOO を登録し、予約注文かつ高優先度なら優先配送区分=PRIORITY を設定
    ImportBatch->>OrderHubDb: 顧客確認結果 / 在庫引当結果 C
    ImportBatch->>OrderHubDb: 注文状態 / 出荷依頼状態 U
    ImportBatch->>AckWorker: 注文受付通知要求
    AckWorker->>OrderHubDb: 注文情報 / 通知履歴 R
    AckWorker->>OrderHubDb: 通知履歴 U
    AckWorker->>HulftTx: 注文受付通知ファイル送信要求
    HulftTx-->>FooSys: 注文受付通知
    Note over AckWorker,BarQueue: 注文受付通知はBar送信成否と独立して返却する
    ImportBatch->>BarQueue: Bar送信要求投入
    alt Bar営業時間内
        ShipWorker->>BarQueue: 送信要求取得
        ShipWorker->>OrderHubDb: 注文情報 / 出荷依頼 R
        ShipWorker->>OrderHubDb: 冪等受付履歴 C
        ShipWorker->>BarSys: 出荷依頼\n注文元・優先配送区分付き
        BarSys-->>ShipWorker: 配送依頼受付応答
        ShipWorker->>OrderHubDb: 出荷依頼 / 注文状態 / 連携履歴 U
        ShipWorker->>OrderHubDb: 通知履歴 C
    else Bar営業時間外
        ShipWorker->>BarQueue: 送信要求取得
        ShipWorker->>OrderHubDb: 出荷依頼状態 / 次回送信予定時刻 U
        ShipWorker->>BarQueue: 可視性延長または再投入
    end
    loop 配送状態イベント
        BarSys-->>ResultApi: 配送受付済 / 配送準備中 / 配送中 / 配送完了
        ResultApi->>OrderHubDb: 連携履歴 C
        ResultApi-->>BarSys: 受付応答
        ResultApi->>ResultWorker: 状態反映要求
        ResultWorker->>OrderHubDb: 注文情報 / 最新配送状態 R
        ResultWorker->>OrderHubDb: 配送状態履歴 C
        ResultWorker->>OrderHubDb: 最新配送状態 / 注文状態 U
        ResultWorker->>OrderHubDb: 通知履歴 C
        ResultNotify->>OrderHubDb: 注文情報 / 最新配送状態 / 通知履歴 R
        ResultNotify->>OrderHubDb: 通知履歴 / 連携履歴 U
        ResultNotify->>HulftTx: 配送状態返却ファイル送信要求
        HulftTx-->>FooSys: 配送状態返却
    end
    FooSys-->>FooUser: 受付結果・配送状態表示
```

## 5. 処理単位と CRUD
| 処理単位 | 主体 | 主な DB CRUD | 補足 |
| --- | --- | --- | --- |
| 注文取込 | OrderHub取込Batch | 注文ヘッダ `C/U`、注文明細 `C`、顧客確認結果 `C`、在庫引当結果 `C`、出荷依頼 `C/U`、連携履歴 `C/U` | `order_source=FOO`、`partner_priority_level`、`shipping_priority_class` を登録 |
| 注文受付通知 | 注文受付通知Worker | 通知履歴 `R/U`、連携履歴 `C/U` | Foo社へ受付通知 |
| Bar送信待機管理 | OrderHub取込Batch / 配送会社連携Worker | 出荷依頼 `U`、連携履歴 `C/U` | `bar-shipment-request-queue.fifo` へ投入し、営業時間外は再投入する |
| 出荷依頼送信 | 配送会社連携Worker | 出荷依頼 `R/U`、冪等受付履歴 `C`、通知履歴 `C`、連携履歴 `C/U`、注文ヘッダ `U` | 平日08:00-18:00のみ送信し、優先配送区分で Bar電文を編集 |
| 配送結果受付 | 配送結果受付API | 連携履歴 `C/U` | Bar社通知を受け付け、状態反映要求を起票 |
| 配送結果反映 | 配送状態取込Worker | 配送状態履歴 `C`、配送状態最新 `R/U`、注文ヘッダ `U`、通知履歴 `C`、連携履歴 `C` | `status_seq` で順序制御し、配送準備中も保持する |
| 配送状態返却 | 配送結果返却Worker | 通知履歴 `R/U`、連携履歴 `U` | Foo社へ状態変化単位で返却 |

## 6. 関連処理設計書
- [PDS-001 Foo注文取込Batch処理設計書](../処理設計書/PDS-001_Foo注文取込Batch処理設計書.md)
- [PDS-002 注文受付通知Worker処理設計書](../処理設計書/PDS-002_注文受付通知Worker処理設計書.md)
- [PDS-003 配送会社連携Worker処理設計書](../処理設計書/PDS-003_配送会社連携Worker処理設計書.md)
- [PDS-004 配送結果受付API処理設計書](../処理設計書/PDS-004_配送結果受付API処理設計書.md)
- [PDS-005 配送状態取込Worker処理設計書](../処理設計書/PDS-005_配送状態取込Worker処理設計書.md)
- [PDS-006 配送結果返却Worker処理設計書](../処理設計書/PDS-006_配送結果返却Worker処理設計書.md)
