# HTTP通信設計書

## 1. 目的
社内 HTTP 通信および社外閉域 HTTP 通信のエンドポイント、認証、タイムアウト、再送、冪等性方針を整理する。

## 2. 通信一覧
| 通信 ID | 呼出元 | 呼出先 | 用途 |
| --- | --- | --- | --- |
| `HTTP-INT-001` | FooOrderImportJob | Internal API Gateway | 顧客確認 |
| `HTTP-INT-002` | FooOrderImportJob | Internal API Gateway | 在庫引当 |
| `HTTP-INT-003` | 出荷依頼受付 API | Internal API Gateway | 顧客確認 |
| `HTTP-INT-004` | 出荷依頼受付 API | Internal API Gateway | 在庫引当 |
| `HTTP-EXT-001` | 配送会社連携Worker | Bar API Gateway | 出荷指示送信 |
| `HTTP-EXT-002` | Bar API Gateway | 配送状態取込Worker | 配送結果通知 |
| `HTTP-EXT-003` | Foo社 状態照会クライアント | 出荷状態照会 API | 状態照会 |
| `HTTP-EXT-004` | Fuga社 API クライアント | 出荷依頼受付 API | 出荷依頼 |
| `HTTP-EXT-005` | Fuga社 API クライアント | 出荷状態照会 API | 状態照会 |

## 3. エンドポイント
| 論理名 | FQDN | 用途 |
| --- | --- | --- |
| Internal API Gateway | `internal-api.hoge.local` | 社内 API 入口 |
| 出荷依頼受付 API | `ship-request.hoge.local` | Fuga社出荷依頼受付 |
| 出荷状態照会 API | `ship-status.hoge.local` | Foo/Fuga 状態照会受付 |
| 配送結果受付 API | `bar-result.hoge.local` | Bar 配送結果受付 |

## 4. 認証方式
| 通信 ID | 認証方式 |
| --- | --- |
| `HTTP-INT-001` | mTLS + API キー |
| `HTTP-INT-002` | OAuth2 Client Credentials |
| `HTTP-INT-003` | mTLS + API キー |
| `HTTP-INT-004` | OAuth2 Client Credentials |
| `HTTP-EXT-001` | mTLS + API キー + `X-Idempotency-Key` |
| `HTTP-EXT-002` | 送信元証明書 + 送信元 IP 制限 |
| `HTTP-EXT-003` | mTLS + API キー |
| `HTTP-EXT-004` | mTLS + API キー |
| `HTTP-EXT-005` | mTLS + API キー |

## 5. タイムアウト / リトライ
| 通信 ID | 接続 | 読込 | リトライ | 備考 |
| --- | ---: | ---: | ---: | --- |
| `HTTP-INT-001` | 1秒 | 3秒 | 2回 | 顧客確認 |
| `HTTP-INT-002` | 1秒 | 5秒 | 1回 | 在庫引当 |
| `HTTP-INT-003` | 1秒 | 3秒 | 2回 | 顧客確認 |
| `HTTP-INT-004` | 1秒 | 5秒 | 1回 | 在庫引当 |
| `HTTP-EXT-001` | 2秒 | 10秒 | 2回 | `bar-shipment-request-queue.fifo` から非同期起動、平日08:00-18:00のみ送信 |
| `HTTP-EXT-002` | 2秒 | 15秒 | Bar側最大2回再送 | 結果通知 |
| `HTTP-EXT-003` | 2秒 | 5秒 | 1回 | 参照系 |
| `HTTP-EXT-004` | 2秒 | 10秒 | 1回 | 登録系 |
| `HTTP-EXT-005` | 2秒 | 5秒 | 1回 | 参照系 |

## 6. ヘッダ
| ヘッダ | 用途 |
| --- | --- |
| `X-Request-Id` | リクエスト単位追跡 |
| `X-Correlation-Id` | 注文単位相関 |
| `X-Caller-System` | 呼出元識別 |
| `X-API-Key` | 契約単位認証 |
| `X-Idempotency-Key` | Bar 宛冪等制御 |

## 7. 経路方針
- FooOrderImportJob と 出荷依頼受付 API は、社内参照系IFとして Internal API Gateway を呼び出す。
- Foo社、Fuga社からの着信は閉域 VPN 配下の Private ALB で受ける。
- Bar社向け送信は `bar-shipment-request-queue.fifo` を配送会社連携Worker が購読し、Bar営業時間内に限って閉域先 FQDN を呼び出す。
- Bar社からの結果通知は配送結果受付 API を経由して配送状態取込Worker へ到達する。

## 8. 冪等性と順序性
- Bar への出荷指示は `X-Idempotency-Key` を固定し、タイムアウト再送時の重複登録を防止する。
- Bar からの配送結果は `status_seq` 昇順で処理し、過去シーケンスの通知は反映しない。
