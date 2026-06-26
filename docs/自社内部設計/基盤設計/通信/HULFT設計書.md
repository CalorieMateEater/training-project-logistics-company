# HULFT設計書

## 1. 目的
Foo社とのファイル連携に利用する HULFT 受信・送信設定と、S3 正本化の方式を定義する。

## 2. 基本方針
- Foo社とのファイル授受は HULFT を利用する。
- Foo社からの出荷依頼ファイルは、注文確定都度に送信される前提とする。
- 業務データの正本は S3 とし、HULFT サーバのローカル領域は一時退避用途に限定する。
- HULFT 障害時は起動テンプレートから代替 EC2 を再作成し、RTO 60 分以内で復旧する。

## 3. サーバ一覧
| 論理名 | ホスト名 | 用途 |
| --- | --- | --- |
| HULFT受信サーバ | `hoge-hulft-rx-01` | 注文ファイル受信 |
| HULFT送信サーバ | `ec2-hulft-tx-01` | 注文受付通知、配送状態ファイル送信 |

## 4. 接続設定
| 項目 | 受信 | 送信 |
| --- | --- | --- |
| HULFT ホスト ID | `HOGE_RX01` | `HOGE_TX01` |
| 対向ホスト ID | `FOO_HULFT01` | `FOO_HULFT01` |
| 通信ポート | `30000/TCP` | `30000/TCP` |
| 文字コード | UTF-8 | UTF-8 |
| 改行コード | LF | LF |
| 再送 | 3回、60秒間隔 | 3回、60秒間隔 |

## 5. ファイル ID
| ファイル ID | 方向 | 用途 | S3 格納先 |
| --- | --- | --- | --- |
| `FOO_ORDER_IN` | Foo -> Hoge | 注文ファイル受信 | `s3://hoge-orderhub-landing-prod/foo/` |
| `FOO_ACK_OUT` | Hoge -> Foo | 注文受付通知 | `s3://hoge-orderhub-status-prod/foo/ack/` |
| `FOO_STATUS_OUT` | Hoge -> Foo | 配送状態返却 | `s3://hoge-orderhub-status-prod/foo/status/` |

## 6. 一時ディレクトリ
| サーバ | ディレクトリ | 用途 |
| --- | --- | --- |
| HULFT受信サーバ | `/hulft/receive/foo/incoming` | 着信直後 |
| HULFT受信サーバ | `/hulft/receive/foo/work` | S3 転送待ち |
| HULFT送信サーバ | `/hulft/send/foo/pickup` | 送信待ち |
| HULFT送信サーバ | `/hulft/send/foo/sent` | 送信済み一時退避 |

## 7. 処理フロー
1. Foo社が `FOO_ORDER_IN` を送信する。
2. HULFT受信サーバが着信後、S3 `landing` バケットへアップロードする。
3. S3 配置完了イベントを契機に FooOrderImportJob を即時起動する。
4. FooOrderImportJob が S3 から取得して取り込む。
5. 注文受付通知Worker が注文受付通知ファイルを S3 `ack` プレフィックスへ出力する。
6. 配送結果返却Worker が配送状態返却ファイルを S3 `status` プレフィックスへ出力する。
7. HULFT送信サーバが対象ファイルを取得し、`FOO_ACK_OUT` または `FOO_STATUS_OUT` として送信する。

## 8. 可用性設計
- HULFT サーバは単一インスタンス運用とするが、AMI と起動テンプレートを整備する。
- 障害時は Session Manager 経由調査または再作成を行う。
- 業務データ正本は S3 であるため、EC2 ローカル障害で業務データ喪失は発生させない。
