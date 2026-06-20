# SQS設計書

## 1. 目的
Hoge社 AWS アカウントで利用する SQS キューの用途、設定値、権限制御方針を定義する。

## 2. 基本方針
- SQS はすべて Hoge社 AWS アカウントに配置する。
- Producer は OrderHub取込Batch、出荷依頼受付API、配送会社連携Worker とする。
- Consumer は対向システムごとの IAM 資格情報で接続する。
- DLQ は採用しない。失敗時は Consumer 側再取得と運用監視で対応する。
- Bar社向け出荷依頼は `bar-shipment-request-queue.fifo` を介して非同期化し、営業時間外はWorker側で再投入制御する。
- Fuga社向け出荷依頼は `fuga-shipment-request-queue.fifo` を介して非同期化し、配送条件判定後に即時送信対象としてWorkerへ引き渡す。

## 3. キュー一覧
| キュー名 | 種別 | Producer | Consumer | 用途 |
| --- | --- | --- | --- | --- |
| `bar-shipment-request-queue.fifo` | FIFO | OrderHub取込Batch / 出荷依頼受付API | 配送会社連携Worker | Bar向け出荷依頼送信待ち |
| `fuga-shipment-request-queue.fifo` | FIFO | OrderHub取込Batch / 出荷依頼受付API | 配送会社連携Worker | Fuga向け特殊配送依頼送信待ち |
| `billing-plan-queue` | Standard | 配送会社連携Worker | Baz Billing Hub | 請求予定連携 |
| `order-notice-queue.fifo` | FIFO | 配送会社連携Worker | Qux Mall Sync | 注文状態通知 |

## 4. 設定値
| 項目 | `bar-shipment-request-queue.fifo` | `fuga-shipment-request-queue.fifo` | `billing-plan-queue` | `order-notice-queue.fifo` |
| --- | --- | --- | --- | --- |
| MessageRetentionPeriod | 7日 | 4日 | 4日 | 4日 |
| VisibilityTimeout | 300秒 | 180秒 | 120秒 | 180秒 |
| ReceiveMessageWaitTimeSeconds | 20秒 | 20秒 | 20秒 | 20秒 |
| MaximumMessageSize | 256KB | 256KB | 256KB | 256KB |
| 暗号化 | SSE-KMS | SSE-KMS | SSE-KMS | SSE-KMS |
| KMS キー | `alias/hoge-orderhub-sqs` | `alias/hoge-orderhub-sqs` | `alias/hoge-orderhub-sqs` | `alias/hoge-orderhub-sqs` |
| MessageGroupId | `carrier_code + business_date` | `carrier_code + shipping_type` | - | `tenant_code + order_id` |
| DeduplicationId | `shipment_request_id + request_version` | `shipment_request_id + request_version` | - | `event_id` |

## 5. 性能設計
| 観点 | `bar-shipment-request-queue.fifo` | `fuga-shipment-request-queue.fifo` | `billing-plan-queue` | `order-notice-queue.fifo` |
| --- | --- | --- | --- | --- |
| 想定投入量 | 1日 60,000件 | 1日 8,000件 | 1日 50,000件 | 1日 80,000件 |
| ピーク投入 | 20 TPS | 5 TPS | 10 TPS | 15 TPS |
| Consumer 数 | Worker 2 タスク | Worker 2 タスク | Baz側 2 プロセス | Qux側 2 プロセス |
| 順序要件 | 同一配送会社・営業日単位で順序保持 | 同一配送区分単位で順序保持 | なし | 注文単位で順序保持 |

## 6. IAM 権限
| 主体 | 許可 |
| --- | --- |
| OrderHub取込Batch / 出荷依頼受付API タスクロール | `sqs:SendMessage`, `sqs:GetQueueUrl`, `sqs:GetQueueAttributes` |
| 配送会社連携Worker タスクロール | `sqs:SendMessage`, `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:ChangeMessageVisibility`, `sqs:GetQueueUrl`, `sqs:GetQueueAttributes` |
| Baz Billing Hub IAM ユーザ | `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:ChangeMessageVisibility`, `sqs:GetQueueAttributes` |
| Qux Mall Sync IAM ユーザ | `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:ChangeMessageVisibility`, `sqs:GetQueueAttributes` |

## 7. ネットワーク
- SQS 接続は Interface VPC Endpoint を経由する。
- ECS タスクからの通信は `sg-vpce-common` 宛 `TCP/443` のみを許可する。
- インターネット経由の SQS アクセスは許可しない。

## 8. 運用ポイント
- `bar-shipment-request-queue.fifo` は Bar社営業時間外に受信しても削除せず、次回送信予定時刻まで可視性タイムアウトを延長する。
- `fuga-shipment-request-queue.fifo` は 特殊配送案件を即時送信対象として扱い、営業時間待機は行わない。
- 金曜18:00以降に投入されたメッセージは、翌営業日の平日08:00以降に順次送信する。
- Standard キューの重複受信は Baz 側で吸収する。
- FIFO キューは `MessageGroupId` を固定し、同一注文の順序を保証する。
- 滞留増加時は Worker 送信量と Consumer 処理速度を切り分ける。
