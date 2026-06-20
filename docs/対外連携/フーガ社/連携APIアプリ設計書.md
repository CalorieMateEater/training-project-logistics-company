# 連携APIアプリ設計書 フーガ社

## 1. 対象IF

- `IF-HOGE-FUGA-001` 特殊配送依頼送信API
- `IF-FUGA-HOGE-002` 特殊配送結果通知API

## 2. 特殊配送依頼送信API

### 2.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | `IF-HOGE-FUGA-001` |
| API名 | 特殊配送依頼送信API |
| メソッド | `POST` |
| パス | `/api/v1/fuga-shipments` |
| 送信元 | Hoge OrderHub Worker |
| 送信先 | Fuga Delivery Center |

### 2.2 業務仕様

- Hoge社が在庫引当と倉庫場所確定を行った後、特殊配送条件に該当する案件のみ送信する
- フーガ社は依頼を受け付けた時点で配送受付番号を返却する
- 依頼受付は配送完了を意味せず、後続の配送結果通知で状態を更新する

### 2.3 リクエストヘッダ

| ヘッダ | 必須 | 内容 |
| --- | --- | --- |
| `X-Partner-Code` | 必須 | 固定値 `HOGE` |
| `X-Trace-Id` | 必須 | 分散トレース用ID |
| `X-Idempotency-Key` | 必須 | Hoge社生成の冪等キー |
| `X-Sent-At` | 必須 | 送信日時 |
| `Authorization` | 必須 | APIキーまたはBearerトークン |

### 2.4 リクエスト例

```json
{
  "order_id": "O20260620142015A1B2C3",
  "partner_order_id": "HO202606200001",
  "shipment_request_id": "SHP-20260620142015-A1B2C3",
  "order_source_code": "HOGE",
  "shipping_priority_class": "NORMAL",
  "source_warehouse_location_code": "WH-TOKYO-01",
  "temperature_zone": "COOL",
  "size_type": "LARGE",
  "requested_ship_date": "2026-06-20",
  "delivery_zip_code": "1000001",
  "delivery_address": "東京都千代田区1-1-1",
  "items": [
    {
      "item_code": "ITM0000001",
      "item_name": "冷蔵商品A",
      "quantity": 1
    }
  ]
}
```

### 2.5 バリデーション要点

| 項目 | 条件 | エラー |
| --- | --- | --- |
| `order_source_code` | `FOO`, `HOGE` のみ | 422 |
| `shipping_priority_class` | `NORMAL`, `PRIORITY` のみ | 422 |
| `source_warehouse_location_code` | Hoge社定義済み倉庫コードのみ | 422 |
| `temperature_zone` | `AMBIENT`, `COOL`, `FROZEN` のいずれか | 422 |
| `size_type` | `NORMAL`, `LARGE`, `REMOTE`, `SPECIAL` のいずれか | 422 |
| `items` | 1件以上 | 422 |

### 2.6 レスポンス例

```json
{
  "fuga_shipment_id": "FGS202606200001",
  "shipment_request_id": "SHP-20260620142015-A1B2C3",
  "acceptance_status": "ACCEPTED",
  "accepted_at": "2026-06-20T14:21:03",
  "duplicate": false
}
```

## 3. 特殊配送結果通知API

### 3.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | `IF-FUGA-HOGE-002` |
| API名 | 特殊配送結果通知API |
| メソッド | `POST` |
| パス | `/api/v1/delivery-results/fuga` |
| 送信元 | Fuga Delivery Center |
| 送信先 | Hoge OrderHub |

### 3.2 リクエスト例

```json
{
  "fuga_shipment_id": "FGS202606200001",
  "order_id": "O20260620142015A1B2C3",
  "partner_order_id": "HO202606200001",
  "status_seq": 3,
  "delivery_status": "IN_TRANSIT",
  "status_label": "配送中",
  "event_occurred_at": "2026-06-20T18:10:00",
  "temperature_zone": "COOL",
  "size_type": "LARGE",
  "reason_code": null,
  "reason_category": null
}
```

### 3.3 業務ルール

- `status_seq` が既処理値以下の場合は重複として破棄する
- `delivery_status` の遷移不正は 409 とする
- 異常終了時は `reason_code` または `reason_category` を設定する
- Hoge社は受信後、配送状態更新、Foo返却、Baz/Qux通知起票を行う

## 4. エラー仕様

| API | HTTP | 内容 |
| --- | --- | --- |
| 特殊配送依頼送信API | 400 | JSON形式不正 |
| 特殊配送依頼送信API | 401/403 | 認証または接続元エラー |
| 特殊配送依頼送信API | 409 | 冪等キー重複または受付済 |
| 特殊配送依頼送信API | 422 | 入力・業務条件エラー |
| 特殊配送結果通知API | 400 | 必須項目不足 |
| 特殊配送結果通知API | 409 | 状態遷移不正または古い `status_seq` |
| 特殊配送結果通知API | 503 | 一時障害 |

## 5. 補足

- フーガ社は注文元ではないため、Hoge社の直受注登録APIは利用しない
- 出荷状態照会APIは本資料の対象外とし、必要時はHoge社共通の照会仕様を参照する
