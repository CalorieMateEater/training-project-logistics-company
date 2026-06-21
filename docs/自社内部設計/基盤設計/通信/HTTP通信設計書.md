# HTTP通信設計書

## 1. 目的
本書は、Hoge社システムで利用するHTTP/HTTPS通信の接続先、認証方式、タイムアウト、リトライ方針を整理する。

## 2. 通信一覧

| 通信ID | 送信元 | 送信先 | 用途 |
| --- | --- | --- | --- |
| `HTTP-INT-001` | FooOrderImportJob | Internal API Gateway | 顧客確認 |
| `HTTP-INT-002` | FooOrderImportJob | Internal API Gateway | 在庫引当 |
| `HTTP-INT-003` | 出荷依頼受付API | Internal API Gateway | 顧客確認 |
| `HTTP-INT-004` | 出荷依頼受付API | Internal API Gateway | 在庫引当 |
| `HTTP-INT-005` | 配送会社連携Worker | Internal API Gateway | 在庫引当解除 |
| `HTTP-INT-006` | 配送状態取込Worker | Internal API Gateway | 在庫出荷確定 |
| `HTTP-INT-007` | Hoge倉庫担当者端末 | Internal API Gateway | 倉庫在庫照会 |
| `HTTP-INT-008` | Hoge倉庫担当者端末 | Internal API Gateway | 入庫登録 |
| `HTTP-EXT-001` | 配送会社連携Worker | Bar API Gateway | Bar向け出荷依頼送信 |
| `HTTP-EXT-002` | Bar API Gateway | 配送状態取込Worker | Bar配送結果通知 |
| `HTTP-EXT-003` | Foo状態照会クライアント | 出荷状態照会API | 状態照会 |
| `HTTP-EXT-004` | Hoge直受注登録ポータル | 出荷依頼受付API | 直受注登録 |
| `HTTP-EXT-005` | Fuga Carrier Gateway | 配送結果受付API | Fuga配送結果通知 |
| `HTTP-EXT-006` | 配送会社連携Worker | Fuga Carrier Gateway | Fuga向け特殊配送依頼送信 |

## 3. エンドポイント

| エンドポイント | FQDN | 用途 |
| --- | --- | --- |
| Internal API Gateway | `internal-api.hoge.local` | 社内API入口 |
| 出荷依頼受付API | `ship-request.hoge.local` | Hoge直受注登録受付 |
| 出荷状態照会API | `ship-status.hoge.local` | Foo社状態照会受付 / Hoge社業務確認 |
| 配送結果受付API | `carrier-result.hoge.local` | Bar/Fuga配送結果通知受付 |
| Bar API Gateway | `bar-ship.bar.local` | Bar向け出荷依頼送信 |
| Fuga Carrier Gateway | `fuga-shipment.fuga.local` | Fuga向け特殊配送依頼送信 |

## 4. 認証方式

| 通信ID | 認証方式 |
| --- | --- |
| `HTTP-INT-001` | mTLS + APIキー |
| `HTTP-INT-002` | OAuth2 Client Credentials |
| `HTTP-INT-003` | mTLS + APIキー |
| `HTTP-INT-004` | OAuth2 Client Credentials |
| `HTTP-INT-005` | OAuth2 Client Credentials |
| `HTTP-INT-006` | OAuth2 Client Credentials |
| `HTTP-INT-007` | OIDC Access Token + mTLS |
| `HTTP-INT-008` | OIDC Access Token + mTLS |
| `HTTP-EXT-001` | mTLS + APIキー + `X-Idempotency-Key` |
| `HTTP-EXT-002` | 送信元証明書 + 送信元IP制御 |
| `HTTP-EXT-003` | mTLS + APIキー |
| `HTTP-EXT-004` | mTLS + APIキー |
| `HTTP-EXT-005` | 送信元証明書 + 送信元IP制御 |
| `HTTP-EXT-006` | mTLS + APIキー + `X-Idempotency-Key` |

## 5. タイムアウト・リトライ

| 通信ID | 接続タイムアウト | 読込タイムアウト | リトライ | 備考 |
| --- | ---: | ---: | ---: | --- |
| `HTTP-INT-001` | 1秒 | 3秒 | 2回 | 顧客確認 |
| `HTTP-INT-002` | 1秒 | 5秒 | 1回 | 在庫引当 |
| `HTTP-INT-003` | 1秒 | 3秒 | 2回 | 顧客確認 |
| `HTTP-INT-004` | 1秒 | 5秒 | 1回 | 在庫引当 |
| `HTTP-INT-005` | 1秒 | 5秒 | 1回 | 在庫引当解除 |
| `HTTP-INT-006` | 1秒 | 5秒 | 1回 | 在庫出荷確定 |
| `HTTP-INT-007` | 2秒 | 5秒 | 1回 | 倉庫在庫照会 |
| `HTTP-INT-008` | 2秒 | 10秒 | 0回 | 入庫登録 |
| `HTTP-EXT-001` | 2秒 | 10秒 | 2回 | Bar営業時間内のみ送信 |
| `HTTP-EXT-002` | 2秒 | 15秒 | Bar側最大2回 | Bar配送結果通知 |
| `HTTP-EXT-003` | 2秒 | 5秒 | 1回 | 状態照会 |
| `HTTP-EXT-004` | 2秒 | 10秒 | 1回 | 直受注登録 |
| `HTTP-EXT-005` | 2秒 | 15秒 | Fuga側最大2回 | 配送結果通知 |
| `HTTP-EXT-006` | 2秒 | 10秒 | 1回 | 特殊配送依頼送信 |

## 6. 共通ヘッダ

| ヘッダ | 用途 |
| --- | --- |
| `X-Request-Id` | リクエスト識別 |
| `X-Correlation-Id` | 業務処理相関 |
| `X-Caller-System` | 呼出元識別 |
| `Authorization` | Bearer トークン |
| `X-API-Key` | 対向認証 |
| `X-Idempotency-Key` | 冪等制御 |
| `X-Trace-Id` | 分散トレース |

## 7. 補足

- Foo社からの照会は `出荷状態照会API` のみを利用する
- Hoge社直受注は `出荷依頼受付API` を利用し、注文元は `HOGE` として登録される
- Fuga社は注文元ではなく配送会社であるため、Hoge社へは配送結果通知のみを送信する
- Fuga向けの配送依頼は Hoge社の配送会社連携Worker が送信する
- 倉庫担当者端末からの在庫照会 / 入庫登録は、倉庫ネットワークから `Internal API Gateway` へ閉域接続する
- 入庫登録APIの再試行は自動リトライせず、同一 `receipt_reference_no` を利用した利用者再送で冪等性を担保する
