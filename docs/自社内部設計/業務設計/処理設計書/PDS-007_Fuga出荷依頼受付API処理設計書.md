# PDS-007 Fuga出荷依頼受付API処理設計書

## 1. 基本情報
| 項目 | 内容 |
| --- | --- |
| 処理設計書ID | `PDS-007` |
| 関連詳細業務フローID | `DFL-002` |
| 処理名 | Fuga出荷依頼受付API |
| 開始契機 | `POST /api/v1/shipment-requests` |
| 終了条件 | 注文起票、Bar送信待ちメッセージ投入後にAPI応答を返却すること |

## 2. フロー図
```mermaid
flowchart TD
    A[APIリクエスト受信] --> B[認証/接続元確認]
    B --> C[入力検証]
    C --> D{重複partner_request_idか}
    D -- Yes --> E[409応答]
    D -- No --> F[顧客確認API呼出]
    F --> G[在庫引当API呼出]
    G --> H[注文元/優先配送区分導出]
    H --> I[注文/出荷依頼登録]
    I --> J[Bar送信待ちメッセージ投入]
    J --> K[受付応答返却]
```

## 3. 処理手順
| 手順 | 内容 |
| --- | --- |
| 1 | 認証情報、接続元IP、必須ヘッダを確認する |
| 2 | JSON形式、必須項目、`shipment_mode`、`shipping_release_at` の整合を検証する |
| 3 | `partner_request_id` の重複を確認し、重複時は `409` を返却する |
| 4 | 顧客確認API、在庫引当APIを同期呼出する |
| 5 | `order_source=FUGA`、`shipping_priority_class=NORMAL` を導出する |
| 6 | 注文ヘッダ、注文明細、出荷依頼、連携履歴を登録する |
| 7 | `bar-shipment-request-queue.fifo` に送信待ちメッセージを投入する |
| 8 | `order_id`、`registration_status`、`current_status` を含む応答を返却する |

## 4. 応答方針
- 予約出荷かつ `shipping_release_at` 未到来なら `WAITING_SHIPPING_RELEASE` を返却する。
- 即時出荷でもBar営業時間外なら内部的には送信待ちとなるが、API応答は受付完了を返す。
