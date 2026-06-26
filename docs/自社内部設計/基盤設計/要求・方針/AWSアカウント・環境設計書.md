# AWSアカウント・環境設計書

## 1. 目的
Hoge社 AWS 利用におけるアカウント境界、環境分離、命名規則、タグ、権限制御方針を定義する。

## 2. 基本方針
- リージョンは全環境 `ap-northeast-1` 固定とする。
- マルチリージョン DR は本設計対象外とする。
- 本番と非本番は AWS アカウントで分離する。
- セキュリティ監査と共通運用は別アカウントで分離する。

## 3. Organizations / OU 設計
| OU | 用途 |
| --- | --- |
| `本番` | 本番ワークロード |
| `非本番` | 開発、検証、ステージング |
| `共通` | 共通 CI/CD、共通 ECR、共通監視連携 |
| `セキュリティ` | 監査ログ、Security Hub、GuardDuty 集約 |

## 4. アカウント設計
| アカウント名 | 用途 |
| --- | --- |
| `hoge-prod-orderhub` | 本番受注連携基盤 |
| `hoge-nonprod-orderhub` | 開発・検証・総合試験 |
| `hoge-shared-platform` | 共通 ECR、CI/CD、証明書配布補助 |
| `hoge-security-audit` | CloudTrail 集約、Config、Security Hub、GuardDuty |

## 5. 環境分離
| 環境 | 分離方式 | 備考 |
| --- | --- | --- |
| 開発 | 非本番アカウント内の `dev` VPC | 開発者向け |
| 検証 | 非本番アカウント内の `qa` VPC | IF 接続試験用 |
| 本番 | 本番アカウント内の `prod` VPC | 対外接続を保持 |

## 6. 命名規則
| 種別 | 形式 | 例 |
| --- | --- | --- |
| VPC | `{system}-{env}-vpc` | `orderhub-prod-vpc` |
| サブネット | `{env}-{tier}-{az}` | `prod-app-a` |
| Security Group | `sg-{system}-{role}` | `sg-orderhub-batch` |
| IAM Role | `role-{system}-{component}-{env}` | `role-orderhub-batch-prod` |
| S3 Bucket | `{company}-{system}-{purpose}-{env}` | `hoge-orderhub-landing-prod` |
| ECS Cluster | `ecs-{system}-{env}` | `ecs-orderhub-prod` |

## 7. タグ設計
| タグ | 必須 | 例 |
| --- | --- | --- |
| `System` | 必須 | `OrderHub` |
| `Env` | 必須 | `prod` |
| `Owner` | 必須 | `logistics-platform-team` |
| `CostCenter` | 必須 | `CC-4100` |
| `Project` | 必須 | `HogeLogistics` |
| `DataClass` | 必須 | `Internal` |
| `ManagedBy` | 必須 | `Terraform` |

## 8. 権限管理
| 観点 | 方針 |
| --- | --- |
| 人の認証 | IAM Identity Center を利用する |
| MFA | 管理者、運用者、承認者は必須 |
| Root ユーザー | 日常利用禁止、物理金庫保管手順で管理 |
| アプリ権限 | IAM Role を利用し、長期アクセスキーを禁止 |
| SCP | 本番 OU は `ec2:TerminateInstances`、`kms:ScheduleKeyDeletion` などを制限 |
| Break Glass | 緊急管理ロールを別管理し、使用時は監査記録を必須化 |

## 9. 補足
- 共通 ECR は `hoge-shared-platform` に配置し、イメージ参照のみを許可する。
- 監査ログは `hoge-security-audit` へ集約し、ワークロードアカウント側で削除できないようにする。

## 10. コスト管理
| 項目 | 設定 |
| --- | --- |
| 月額予算 | 1,000,000円 |
| 70%到達 | WarningとしてPM、基盤リーダー、FinOps担当へ通知 |
| 90%到達 | CriticalとしてPM、基盤責任者へ通知し、増加要因を当日確認 |
| 100%到達 | Criticalとしてプロジェクト責任者へ通知し、追加利用の承認要否を判断 |
| 予算管理 | AWS Budgetsを本番、非本番、共通基盤の各アカウントへ設定 |
| 利用分析 | Cost Explorerと必須コスト配賦タグでシステム、環境、所有者別に集計 |

- 予算超過を理由に受注・配送処理を自動停止しない。
- 月次でECS稼働時間、RDS、S3ライフサイクル、CloudWatch Logs保持量、データ転送量を棚卸しする。
