# 連携API基盤設計書 フーガ社

## 1. 対象IF

- `IF-HOGE-FUGA-001` 特殊配送依頼送信API
- `IF-FUGA-HOGE-002` 特殊配送結果通知API

## 2. 通信経路

```text
Hoge OrderHub Worker
  -> Hoge社 Fuga向け接続境界
  -> Site-to-Site VPN
  -> Fuga社接続境界
  -> Fuga Carrier Gateway

Fuga Delivery Center
  -> Fuga Carrier Gateway
  -> Site-to-Site VPN
  -> Hoge社 Fuga向け接続境界
  -> 配送状態取込Worker
```

## 3. 接続先

| 方向 | FQDN | 用途 |
| --- | --- | --- |
| Hoge社 → フーガ社 | `fuga-shipment.fuga.local` | 特殊配送依頼送信API |
| フーガ社 → Hoge社 | `fuga-result.hoge.local` | 特殊配送結果通知API |

## 4. 認証・接続制御

| 項目 | 内容 |
| --- | --- |
| 通信方式 | VPN閉域網上の HTTPS |
| 認証 | mTLS + APIキー |
| 接続元制御 | 双方の許可CIDRのみ |
| 冪等性 | Hoge社送信時は `X-Idempotency-Key` を必須 |
| トレース | `X-Trace-Id` を双方で引き継ぐ |

## 5. タイムアウト・リトライ

| IF | タイムアウト | リトライ | 備考 |
| --- | ---: | ---: | --- |
| `IF-HOGE-FUGA-001` | 10秒 | 1回 | 失敗時は送信待ちキューへ再投入 |
| `IF-FUGA-HOGE-002` | 15秒 | フーガ社側最大2回 | Hoge社は `status_seq` で重複排除 |

## 6. レート制御

| IF | 上限 |
| --- | --- |
| `IF-HOGE-FUGA-001` | 1分あたり120件 |
| `IF-FUGA-HOGE-002` | 1分あたり300件 |

## 7. 監視

| 観点 | 内容 |
| --- | --- |
| 疎通監視 | VPNトンネル状態、TLSハンドシェイク |
| API監視 | 4xx/5xx、レスポンスタイム |
| 業務監視 | Fuga送信待ち件数、結果通知未着件数 |
| ログ追跡 | `X-Trace-Id`、`shipment_request_id`、`fuga_shipment_id` |

## 8. 補足

- フーガ社は注文元ではなく配送委託先である
- Hoge社の直受注登録APIや状態照会APIの利用者としては扱わない
- 状態照会系の共有APIは本資料の対象外とする
