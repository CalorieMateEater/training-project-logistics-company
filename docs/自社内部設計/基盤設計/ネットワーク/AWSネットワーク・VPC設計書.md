# AWSネットワーク・VPC設計書

## 1. 目的
Hoge社 AWS 環境における VPC、サブネット、AZ 配置、ルーティング、対外接続、DNS、VPC Endpoint の設計方針を示す。

## 2. 基本情報
| 項目 | 内容 |
| --- | --- |
| リージョン | `ap-northeast-1` |
| 構成方針 | 単一リージョン、Multi-AZ |
| VPC 名 | `orderhub-prod-vpc` |
| VPC CIDR | `10.20.0.0/16` |
| 将来拡張予約 | `10.21.0.0/16` を将来拡張用に予約 |
| DNS | VPC DNS Hostnames / DNS Resolution を有効化 |

## 3. アドレス重複方針
| 接続先 | 想定 CIDR | 重複判定 |
| --- | --- | --- |
| Foo社 | `172.16.10.0/24` | 重複なし |
| Bar社 | `172.16.20.0/24` | 重複なし |
| Fuga社 | `172.16.30.0/24` | 重複なし |
| Hoge社 VPC | `10.20.0.0/16` | 基準 |

## 4. サブネット設計
| 区分 | サブネット名 | AZ | CIDR | 配置対象 |
| --- | --- | --- | --- | --- |
| Protected | `sn-protected-a` | 1a | `10.20.0.0/24` | HULFT受信、社外向け Private ALB |
| Protected | `sn-protected-c` | 1c | `10.20.1.0/24` | HULFT送信、社外向け Private ALB |
| Private App | `sn-app-a` | 1a | `10.20.10.0/24` | Batch、Worker |
| Private App | `sn-app-c` | 1c | `10.20.11.0/24` | Batch、Worker |
| Private API | `sn-api-a` | 1a | `10.20.12.0/24` | Internal API、Shipping API |
| Private API | `sn-api-c` | 1c | `10.20.13.0/24` | Internal API、Shipping API |
| DB | `sn-db-a` | 1a | `10.20.20.0/24` | RDS Primary |
| DB | `sn-db-c` | 1c | `10.20.21.0/24` | RDS Standby |
| Endpoint | `sn-endpoint-a` | 1a | `10.20.30.0/24` | Interface Endpoint |
| Endpoint | `sn-endpoint-c` | 1c | `10.20.31.0/24` | Interface Endpoint |

## 5. AZ 配置方針
- ECS 常駐サービスは 2AZ に分散配置する。
- ALB は 2AZ に配置する。
- RDS は Multi-AZ 構成とする。
- HULFT は受信系/送信系を役割分離し、障害時は起動テンプレートから再作成する。

## 6. ルーティング方針
| 観点 | 採用方針 |
| --- | --- |
| IGW | 未採用 |
| NAT Gateway | 未採用 |
| Transit Gateway | 未採用 |
| VPC Peering | 未採用 |
| Site-to-Site VPN | 採用 |
| Direct Connect | 未採用 |
| PrivateLink | 未採用 |
| Proxy | 社外 HTTP 送信はアプリ側の閉域宛送信とし、共通 Proxy は未採用 |

## 7. ルートテーブル
| ルートテーブル | 適用先 | 主なルート |
| --- | --- | --- |
| `rt-protected` | Protected サブネット | `10.20.0.0/16 -> local`、対向 CIDR -> VPN、S3 Prefix List -> Gateway Endpoint |
| `rt-app` | Private App サブネット | `10.20.0.0/16 -> local`、対向 CIDR -> VPN、S3 Prefix List -> Gateway Endpoint |
| `rt-api` | Private API サブネット | `10.20.0.0/16 -> local`、Foo/Bar/Fuga CIDR -> VPN、S3 Prefix List -> Gateway Endpoint |
| `rt-db` | DB サブネット | `10.20.0.0/16 -> local`、S3 Prefix List -> Gateway Endpoint |
| `rt-endpoint` | Endpoint サブネット | `10.20.0.0/16 -> local` |

## 8. 対外接続
| 接続名 | 接続方式 | 対象 |
| --- | --- | --- |
| `vpn-foo-b2b` | AWS Site-to-Site VPN | HULFT 受信/送信、状態照会 API |
| `vpn-bar-b2b` | AWS Site-to-Site VPN | 出荷指示 API、配送結果返却 API |
| `vpn-fuga-b2b` | AWS Site-to-Site VPN | 特殊配送依頼 API、配送結果通知 API |

### 8.1 Baz/Qux向けSQS接続
- `billing-plan-queue`と`order-notice-queue.fifo`はHoge社AWSアカウントに配置する。
- Hoge社ProducerはHoge社VPCのSQS Interface VPC Endpointからメッセージを投入する。
- Baz/Qux Consumerは各社VPCのSQS Interface VPC EndpointからAWS SQSサービスへ接続し、クロスアカウントIAM Roleとキューポリシーで認可する。
- Baz/QuxからHoge社VPCへの直接ルート、VPN、インバウンド通信は設けない。

## 9. 通信制御
| 観点 | 方針 |
| --- | --- |
| Security Group | 最小権限、SG 参照を優先 |
| NACL | サブネット境界で許可ポートのみ明示する |
| Network Firewall | 今回は未採用。閉域網 + SG + NACL で制御する |
| WAF | インターネット公開がないため未採用 |

## 10. DNS / 名前解決
| 観点 | 方針 |
| --- | --- |
| 内部 DNS | Route 53 Private Hosted Zone を利用 |
| 社外向け名前解決 | 対向先 FQDN は VPC 内カスタム DNS フォワーダで解決 |
| 名前規則 | `*.hoge.local` を内部向けに利用 |

## 11. VPC Endpoint
| 種別 | サービス | 用途 |
| --- | --- | --- |
| Gateway Endpoint | S3 | 着信、処理済み、返却、アーカイブ |
| Interface Endpoint | SQS | Worker 送信 |
| Interface Endpoint | CloudWatch Logs | ログ送信 |
| Interface Endpoint | Secrets Manager | 認証情報取得 |
| Interface Endpoint | KMS | 暗号化 |
| Interface Endpoint | ECR API / ECR DKR | ECS イメージ取得 |
| Interface Endpoint | SSM | EC2 保守 |
| Interface Endpoint | STS | タスクロール引受 |

- 本表のInterface EndpointはHoge社VPC内のProducerおよび社内Consumer向けである。
- Baz/Qux社は各社管理のSQS Interface VPC Endpointを利用するため、Hoge社Security Groupの許可対象には含めない。

## 12. NACL 設計方針
| サブネット | Inbound | Outbound |
| --- | --- | --- |
| Protected | `30000/TCP`, `443/TCP`, Ephemeral | 対向 CIDR、S3/Endpoint 向け `443/TCP` |
| Private App / API | `443/TCP`, `8443/TCP`, `5432/TCP`, Ephemeral | DB、Endpoint、VPN 宛必要ポートのみ |
| DB | `5432/TCP`, Ephemeral | App/API 宛 Ephemeral |

## 13. 補足
- インターネット接続を前提とした NAT、IGW、WAF は採用しない。
- 単一リージョン構成であるため、リージョン障害時はバックアップから再構築を行う。
