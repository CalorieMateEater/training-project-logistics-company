# DFL-001 Foo受注から配送結果返却詳細業務フロー

## 1. 目的
Foo社注文の受付から配送状態返却までを、配送条件に応じた Bar社/Fuga社 への委託分岐と、Bar社営業時間制約による送信待ちを含めて、Hoge社内の主要処理単位、内部コンポーネント、データストア CRUD とあわせて整理する。あわせて、在庫引当が Hoge社保有在庫に対する社内処理であり、Bar社/Fuga社は配送委託先として実配送を担うことを明確にする。

## 2. 設計書ID
| 項目 | 内容 |
| --- | --- |
| 設計書ID | `DFL-001` |
| 業務領域 | Foo受注、配送会社連携、配送結果返却 |
| 逆引き対象処理設計書 | `PDS-001`, `PDS-002`, `PDS-003`, `PDS-004`, `PDS-005`, `PDS-006` |

## 3. 登場アクター・内部コンポーネント
- Foo社担当者
- Foo社システム
- HULFT受信
- OrderHub取込Batch
- 顧客マスタ管理
- 在庫管理
- OrderHubデータストア
- 注文受付通知Worker
- 配送会社送信待ちキュー
- 配送会社連携Worker
- 配送結果受付API
- Barシステム
- Fugaシステム
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
        participant StockApi as 在庫管理
        participant OrderHubDb as OrderHubデータストア
        participant AckWorker as 注文受付通知Worker
        participant CarrierQueue as 配送会社送信待ちキュー
        participant ShipWorker as 配送会社連携Worker
        participant ResultApi as 配送結果受付API
        participant ResultWorker as 配送状態取込Worker
        participant ResultNotify as 配送結果返却Worker
        participant HulftTx as HULFT送信
    end
    box rgb(255, 244, 236) 他社
        participant BarSys as Barシステム
        participant FugaSys as Fugaシステム
    end

    FooUser->>FooSys: 注文登録
    FooSys->>HulftRx: 注文ファイル送信
    HulftRx->>ImportBatch: 着信ファイル引渡し
    ImportBatch->>CustApi: 顧客確認
    CustApi-->>ImportBatch: 顧客確認結果
    ImportBatch->>StockApi: Hoge社保有在庫の引当 / 倉庫場所確定
    StockApi-->>ImportBatch: 在庫引当結果
    ImportBatch->>OrderHubDb: 注文ヘッダ / 注文明細 C
    Note over ImportBatch,OrderHubDb: 注文元=FOO を登録し、予約注文かつ高優先度なら優先配送区分=PRIORITY を設定する\n在庫引当結果は Hoge社保有在庫の引当結果として保持し、倉庫場所コードも保存する
    ImportBatch->>OrderHubDb: 顧客確認結果 / 在庫引当結果 C
    ImportBatch->>OrderHubDb: 注文状態 / 出荷依頼状態 U
    ImportBatch->>AckWorker: 注文受付通知要求
    AckWorker->>OrderHubDb: 注文情報 / 通知履歴 R
    AckWorker->>OrderHubDb: 通知履歴 U
    AckWorker->>HulftTx: 注文受付通知ファイル送信要求
    HulftTx-->>FooSys: 注文受付通知
    Note over AckWorker,CarrierQueue: 注文受付通知は配送会社送信成否と独立して返却する
    ImportBatch->>CarrierQueue: 配送会社送信要求投入
    alt 標準配送かつBar営業時間内
        ShipWorker->>CarrierQueue: 送信要求取得
        ShipWorker->>OrderHubDb: 注文情報 / 出荷依頼 R
        ShipWorker->>OrderHubDb: 冪等受付履歴 C
        ShipWorker->>BarSys: 出荷依頼\n注文元・優先配送区分付き
        BarSys-->>ShipWorker: 配送依頼受付応答
        ShipWorker->>OrderHubDb: 出荷依頼 / 注文状態 / 連携履歴 U
        ShipWorker->>OrderHubDb: 通知履歴 C
    else 標準配送かつBar営業時間外
        ShipWorker->>CarrierQueue: 送信要求取得
        ShipWorker->>OrderHubDb: 出荷依頼状態 / 次回送信予定時刻 U
        ShipWorker->>CarrierQueue: 可視性延長または再投入
    else 特殊配送でFuga委託
        ShipWorker->>CarrierQueue: 送信要求取得
        ShipWorker->>OrderHubDb: 注文情報 / 出荷依頼 R
        ShipWorker->>OrderHubDb: 冪等受付履歴 C
        ShipWorker->>FugaSys: 特殊配送依頼\n温度帯・サイズ区分付き
        FugaSys-->>ShipWorker: 配送依頼受付応答
        ShipWorker->>OrderHubDb: 出荷依頼 / 注文状態 / 連携履歴 U
        ShipWorker->>OrderHubDb: 通知履歴 C
    end
    loop 配送状態イベント
        BarSys-->>ResultApi: 配送受付済 / 配送準備中 / 配送中 / 配送完了
        FugaSys-->>ResultApi: 特殊配送の状態変化
        ResultApi->>OrderHubDb: 連携履歴 C
        ResultApi-->>BarSys: 受付応答
        ResultApi-->>FugaSys: 受付応答
        ResultApi->>ResultWorker: 状態反映要求
        ResultWorker->>OrderHubDb: 注文情報 / 最新配送状態 R
        ResultWorker->>StockApi: 配送受付済時に在庫出荷確定
        StockApi-->>ResultWorker: 出荷確定結果
        ResultWorker->>OrderHubDb: 配送状態履歴 C
        ResultWorker->>OrderHubDb: 最新配送状態 / 注文状態 / 在庫引当結果 U
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
| 注文取込 | OrderHub取込Batch | 注文ヘッダ `C/U`、注文明細 `C`、顧客確認結果 `C`、在庫引当結果 `C`、出荷依頼 `C/U`、連携履歴 `C/U` | `order_source=FOO`、`partner_priority_level`、`shipping_priority_class` を登録し、Hoge社保有在庫の引当結果と倉庫場所コードを保持 |
| 注文受付通知 | 注文受付通知Worker | 通知履歴 `R/U`、連携履歴 `C/U` | Foo社へ受付通知 |
| 配送会社送信待機管理 | OrderHub取込Batch / 配送会社連携Worker | 出荷依頼 `U`、連携履歴 `C/U` | `bar-shipment-request-queue.fifo` または `fuga-shipment-request-queue.fifo` へ投入し、Bar向けは営業時間外に再投入する |
| 出荷依頼送信 | 配送会社連携Worker | 出荷依頼 `R/U`、冪等受付履歴 `C`、通知履歴 `C`、連携履歴 `C/U`、注文ヘッダ `U` | 配送条件に応じて Bar または Fuga 電文を編集して送信する |
| 配送結果受付 | 配送結果受付API | 連携履歴 `C/U` | Bar社またはFuga社の通知を受け付け、状態反映要求を起票 |
| 配送結果反映 | 配送状態取込Worker | 配送状態履歴 `C`、配送状態最新 `R/U`、注文ヘッダ `U`、在庫引当結果 `U`、通知履歴 `C`、連携履歴 `C` | `status_seq` で順序制御し、初回 `配送受付済` 受信時に在庫出荷確定を反映する |
| 配送状態返却 | 配送結果返却Worker | 通知履歴 `R/U`、連携履歴 `U` | Foo社へ状態変化単位で返却 |

## 6. 関連処理設計書
- [PDS-001 Foo注文取込Batch処理設計書](../処理設計書/PDS-001_Foo注文取込Batch処理設計書.md)
- [PDS-002 注文受付通知Worker処理設計書](../処理設計書/PDS-002_注文受付通知Worker処理設計書.md)
- [PDS-003 配送会社連携Worker処理設計書](../処理設計書/PDS-003_配送会社連携Worker処理設計書.md)
- [PDS-004 配送結果受付API処理設計書](../処理設計書/PDS-004_配送結果受付API処理設計書.md)
- [PDS-005 配送状態取込Worker処理設計書](../処理設計書/PDS-005_配送状態取込Worker処理設計書.md)
- [PDS-006 配送結果返却Worker処理設計書](../処理設計書/PDS-006_配送結果返却Worker処理設計書.md)
