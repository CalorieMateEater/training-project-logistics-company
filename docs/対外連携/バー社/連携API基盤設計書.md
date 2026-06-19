# 配送会社連携API基盤設計書 バー社

## 1. 対象IF

- IF-ID: `IF-HOGE-BAR-001`
- IF-ID: `IF-BAR-HOGE-002`

## 2. 通信構成

### 2.1 出荷指示登録API

```text
bar-shipment-request-queue.fifo
  → Hoge OrderHub Worker
  → Hoge社 Bar向け接続境界
  → VPN閉域連携
  → Bar社接続境界
  → Bar API Gateway
  → Bar Ingress LB
  → Bar Delivery API Cluster
```

### 2.2 配送結果返却API

```text
Bar Delivery Result Publisher
  → Bar API Gateway
  → Bar社接続境界
  → VPN閉域連携
  → Hoge社 Bar向け接続境界
  → Hoge Delivery Result Ingress
  → Hoge OrderHub 配送状態取込Worker
```

## 3. 基盤コンポーネント

| コンポーネント | 配置先 | 用途 |
| --- | --- | --- |
| Bar API Gateway | Bar社DMZ | 認証、レート制御、経路制御 |
| Bar Ingress LB | Bar社業務セグメント | APIクラスタへの振分 |
| Bar Delivery API Cluster | Bar社業務セグメント | 出荷指示受付 |
| Bar Idempotency Store | Bar社業務セグメント | `X-Idempotency-Key` 保持 |
| Bar Shipment DB | Bar社業務セグメント | 配送案件管理 |
| Bar Result Publisher | Bar社業務セグメント | Hoge社向け結果返却 |

## 4. 名前解決と接続先

| 用途 | FQDN | 備考 |
| --- | --- | --- |
| Hoge→Bar 出荷指示 | `shipment-api.bar.local` | VPN内専用名 |
| Bar→Hoge 配送結果返却 | `delivery-result.hoge.local` | VPN内専用名 |

## 5. TLS・認証

| 項目 | 内容 |
| --- | --- |
| TLSバージョン | TLS 1.2以上 |
| サーバ証明書 | Bar社内部CA発行 |
| Hoge→Bar 認証 | APIキー + 接続元IP制限 |
| Bar→Hoge 認証 | 共有APIキー + 接続元IP制限 |
| APIキー保管 | 各社の秘密情報管理基盤 |
| APIキー更新 | 四半期ごとのローテーション |

## 6. タイムアウト・再送・輻輳制御

### 6.1 Hoge→Bar

| 項目 | 値 |
| --- | --- |
| 接続タイムアウト | 1秒 |
| 応答タイムアウト | 5秒 |
| 再送回数 | 最大1回 |
| 再送条件 | 通信失敗、502、503、504 |
| 再送間隔 | 500ms固定 |
| 同時接続数上限 | 20 |
| 送信時間帯 | 平日08:00-18:00 JST |

### 6.2 Bar→Hoge

| 項目 | 値 |
| --- | --- |
| 接続タイムアウト | 1秒 |
| 応答タイムアウト | 5秒 |
| 再送回数 | 最大2回 |
| 再送条件 | 5xx、通信失敗 |
| 再送間隔 | 1秒、3秒 |

## 7. バー社サーバ側冪等性の基盤実装

### 7.1 保持方式

- `Bar Idempotency Store` に `partner_code`, `idempotency_key`, `request_hash`, `bar_shipment_id`, `response_payload`, `expires_at` を保存する
- 保存TTLは48時間とする
- `request_hash` は正規化JSONに対するSHA-256で計算する

### 7.2 判定順序

1. API Gatewayで必須ヘッダ存在確認
2. Delivery API Clusterで `X-Idempotency-Key` 検索
3. 一致なしなら Shipment DB 登録処理へ進む
4. 一致ありかつ `request_hash` 同一なら保存済みレスポンス返却
5. 一致ありかつ `request_hash` 相違なら `409 Conflict`

### 7.3 障害時の注意

- Shipment DB 登録前に Idempotency Store だけ成功した場合、整合性回復バッチで未完了レコードを検知する
- Shipment DB 登録成功後にレスポンス返却失敗した場合、クライアント再送で同一応答を返せるようレスポンス本文を保持する

## 8. 監視・ログ

| 観点 | 監視内容 |
| --- | --- |
| 疎通 | VPNトンネル状態、FQDN解決可否 |
| API | 2xx率、4xx率、5xx率、P95応答時間 |
| 冪等性 | 同一キー再送件数、キー衝突件数、`request_hash` 不一致件数 |
| 結果返却 | コールバック失敗件数、滞留件数 |
| 追跡 | `X-Trace-Id` による相互検索 |

## 9. 責任分界

| 領域 | Hoge社 | Bar社 |
| --- | --- | --- |
| リクエスト生成 | 実施 | - |
| 冪等性キー採番 | 実施 | - |
| 冪等性判定 | - | 実施 |
| API受付可用性 | 共同監視 | 主管理 |
| 配送結果イベント採番 | - | 実施 |
| 配送結果受信反映 | 実施 | - |

## 10. 補足

- バー社向け基盤仕様は、アプリ仕様と切り離して変更できるよう文書分離している
- Hoge社は営業時間外に受け付けた依頼をキューへ保持し、営業開始後に順次送信する
- 業務項目やエラーコードは [連携APIアプリ設計書](/C:/Users/yaku_/Documents/training-project-01/docs/対外連携/バー社/連携APIアプリ設計書.md) を参照する
