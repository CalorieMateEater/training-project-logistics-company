# IAM権限設計書

## 1. 目的
Hoge社受注連携基盤で利用する人、アプリケーション、CI/CD、外部接続主体の IAM 権限境界を整理する。

## 2. 主体一覧
| 主体 | 種別 | 用途 |
| --- | --- | --- |
| 開発者ロール | 人 | 非本番参照、デプロイ申請 |
| 運用者ロール | 人 | 本番参照、障害対応 |
| 緊急管理ロール | 人 | Break Glass 用 |
| CI/CD ロール | システム | デプロイ実行 |
| FooOrderImportJob ロール | アプリ | S3 読取、RDS 接続、SQS 送信 |
| 配送会社連携Worker ロール | アプリ | Bar API 呼出、SQS 送受信 |
| 配送状態取込Worker ロール | アプリ | RDS 更新、Baz/Qux向けSQS送信 |
| 配送結果返却Worker ロール | アプリ | S3 書込 |
| 日次アーカイブBatch ロール | アプリ | S3 書込、KMS 利用 |
| Shipping API ロール | アプリ | RDS 参照、Secrets 取得、SQS 送信 |
| Customer Registry ロール | アプリ | 顧客マスタ参照、Secrets 取得 |
| Stock Keeper ロール | アプリ | 在庫参照更新、Secrets 取得 |
| Baz 接続用クロスアカウントRole | 外部 | SQS 受信 |
| Qux 接続用クロスアカウントRole | 外部 | SQS 受信 |

## 3. 権限方針
- 人の権限は IAM Identity Center 経由で払い出す。
- 本番更新権限は CI/CD ロールへ集約し、人が直接更新しない。
- 外部接続主体には最小限のキュー受信権限のみ付与する。
- Baz/Quxには長期アクセスキーを発行せず、各社AWSアカウントの専用IAM Roleをキューポリシーで許可する。
- Secrets 参照はコンポーネント単位に分離する。

## 4. アプリロール権限
| ロール | 主な許可 |
| --- | --- |
| `role-orderhub-batch-prod` | 対象S3プレフィックスの `s3:GetObject`, `s3:PutObject`、配送会社送信待ちキューの `sqs:SendMessage`、`secretsmanager:GetSecretValue`、必要なKMS利用 |
| `role-bar-worker-prod` | 配送会社送信待ちキューの `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:ChangeMessageVisibility`、Baz/Quxキューの `sqs:SendMessage`、`secretsmanager:GetSecretValue`、必要なKMS利用 |
| `role-result-worker-prod` | Baz/Quxキューの `sqs:SendMessage`、`secretsmanager:GetSecretValue`、必要なKMS利用 |
| `role-notify-worker-prod` | `s3:PutObject`, `secretsmanager:GetSecretValue` |
| `role-archive-batch-prod` | `s3:PutObject`, `kms:Encrypt`, `kms:Decrypt` |
| `role-shipping-api-prod` | `sqs:SendMessage`, `secretsmanager:GetSecretValue` |
| `role-customer-registry-prod` | `secretsmanager:GetSecretValue` |
| `role-stock-keeper-prod` | `secretsmanager:GetSecretValue` |

## 5. 人のロール権限
| ロール | 主な許可 |
| --- | --- |
| 開発者ロール | 非本番 CloudWatch 参照、ECS 参照、S3 参照 |
| 運用者ロール | 本番参照、ログ確認、再実行起動 |
| 緊急管理ロール | 限定時間で管理者相当。利用時は監査必須 |

## 6. 外部主体
| 主体 | 許可 |
| --- | --- |
| Baz接続用クロスアカウントRole | `billing-plan-queue` の受信・削除・可視性変更・属性参照、および当該キュー暗号化キーの `kms:Decrypt` |
| Qux接続用クロスアカウントRole | `order-notice-queue.fifo` の受信・削除・可視性変更・属性参照、および当該キュー暗号化キーの `kms:Decrypt` |

- キューポリシーはBaz/Qux各社の専用Role ARNだけをPrincipalとして許可する。
- `aws:PrincipalArn`、対象キューARN、必要に応じて `aws:SourceVpce` 条件で利用主体と経路を限定する。

## 7. 監査方針
- IAM 変更は CloudTrail で全件記録する。
- Break Glass ロール使用時はインシデント票起票を必須とする。
- アクセスキーの長期未使用は月次棚卸しで無効化する。
