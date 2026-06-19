# ジョブ・Worker通信設計書

## 1. 目的
Batch、Worker、API サービスごとのインバウンド/アウトバウンド条件と、依存先を整理する。

## 2. 通信一覧
| コンポーネント | インバウンド | アウトバウンド |
| --- | --- | --- |
| FooOrderImportJob | なし | S3、Internal API、RDS、SQS、Secrets、Logs |
| 配送会社連携Worker | なし | Bar API、RDS、SQS、Secrets、Logs |
| 配送結果受付API / 配送状態取込Worker | Bar結果受付 ALB | RDS、Secrets、Logs |
| 配送結果返却Worker | なし | RDS、S3、Secrets、Logs |
| 日次アーカイブBatch | なし | RDS、S3、KMS、Logs |
| 出荷依頼受付 API | Shipping API ALB | Internal API、RDS、SQS、Secrets、Logs |
| 出荷状態照会 API | Shipping API ALB | RDS、Secrets、Logs |
| Customer Registry | Internal API ALB | Secrets、Logs |
| Stock Keeper | Internal API ALB | Secrets、Logs |

## 3. 詳細
### 3.1 FooOrderImportJob
- Inbound: なし
- Outbound: `s3://hoge-orderhub-landing-prod/foo/`, `internal-api.hoge.local:443`, `bar-shipment-request-queue.fifo`, `RDS:5432`
- SG: `sg-orderhub-batch`

### 3.2 配送会社連携Worker
- Inbound: なし
- Outbound: `bar-shipment-request-queue.fifo`, `shipment-api.bar.local:443`, `billing-plan-queue`, `order-notice-queue.fifo`, `RDS:5432`
- SG: `sg-bar-worker`

### 3.3 配送結果受付API / 配送状態取込Worker
- Inbound: `bar-result.hoge.local` 配下 ALB から `8443/TCP` を配送結果受付APIで受ける
- Outbound: `RDS:5432`
- SG: `sg-result-worker`

### 3.4 配送結果返却Worker
- Inbound: なし
- Outbound: `s3://hoge-orderhub-status-prod/foo/status/`, `RDS:5432`
- SG: `sg-notify-worker`

### 3.5 日次アーカイブBatch
- Inbound: なし
- Outbound: `s3://hoge-orderhub-archive-prod/`, `RDS:5432`
- SG: `sg-archive-batch`

### 3.6 出荷依頼受付 API / 出荷状態照会 API
- Inbound: `sg-external-api-alb` から `8443/TCP`
- Outbound: `internal-api.hoge.local:443`、`RDS:5432`、`bar-shipment-request-queue.fifo`
- SG: `sg-shipping-api`

## 4. IAM ロール方針
| ロール | 主な権限 |
| --- | --- |
| `role-orderhub-batch-prod` | S3 読書、SQS 送信、Secrets 読取 |
| `role-bar-worker-prod` | SQS 送受信、Secrets 読取 |
| `role-result-worker-prod` | Secrets 読取 |
| `role-notify-worker-prod` | S3 書込 |
| `role-archive-batch-prod` | S3 書込、KMS 利用 |
| `role-shipping-api-prod` | SQS 送信、Secrets 読取 |
