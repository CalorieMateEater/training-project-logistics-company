# 配送会社連携APIアプリ設計書 バー社

## 1. 対象IF

- IF-ID: `IF-HOGE-BAR-001`
- IF名: 出荷指示登録API
- IF-ID: `IF-BAR-HOGE-002`
- IF名: 配送結果返却API

## 2. アプリケーション設計方針

- Hoge社からバー社へ渡す出荷指示は、配送案件の新規登録要求として扱う
- バー社は `X-Idempotency-Key` を用いて同一要求の重複登録を防止する
- バー社からHoge社への配送結果返却は、配送案件単位ではなく配送イベント単位で通知する
- 配送イベントは `status_seq` により順序を管理する
- 住所不備や持戻りなどの業務例外は、単なる文言ではなく `reason_code` で返却する
- 業務例外の外部表示制御用に `reason_category` を返却し、住所補正時は補正有無と補正レベルを通知する
- 複数個口配送は検討済みだが、本版では単一配送案件前提とする

## 3. 出荷指示登録API

### 3.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | IF-HOGE-BAR-001 |
| API名 | 出荷指示登録API |
| 送信元 | Hoge OrderHub Worker |
| 送信先 | Bar Delivery Center |
| 方式 | REST API / HTTPS |
| メソッド | POST |
| パス | `/api/v2/shipments` |

### 3.2 業務用途

`配送条件判定` によりバー社配送対象となった出荷依頼を、バー社の配送案件として受け付ける。  
受け付け完了時点では配送確定ではなく、バー社内部では以下の後続処理が行われる前提とする。

1. 受付内容の業務バリデーション
2. 配送サービスレベル判定
3. 配車・配送店割当
4. 配送案件採番

バー社の出荷依頼受付時間は平日08:00-18:00とし、Hoge社は原則としてこの営業時間帯にのみ本APIを呼び出す。

### 3.3 リクエストヘッダ

| ヘッダ名 | 必須 | 説明 |
| --- | --- | --- |
| `X-Partner-Code` | 必須 | 固定値 `HOGE` |
| `X-Trace-Id` | 必須 | 分散トレース用ID |
| `X-Idempotency-Key` | 必須 | Hoge社が配送依頼単位で生成する一意キー |
| `X-Sent-At` | 必須 | 送信日時。ISO-8601形式 |
| `Authorization` | 必須 | APIキー認証用 |

### 3.4 リクエスト例

```json
{
  "order_id": "O20260620142015A1B2C3",
  "partner_order_id": "FO202606080001",
  "shipment_request_id": "SHP-20260620142015-A1B2C3",
  "order_source_code": "FOO",
  "shipping_priority_class": "PRIORITY",
  "partner_priority_level": 8,
  "delivery_type": "NORMAL",
  "service_level": "NEXT_DAY",
  "temperature_zone": "AMBIENT",
  "package_count": 2,
  "cash_on_delivery_amount": 0,
  "requested_ship_date": "2026-06-08",
  "requested_delivery_date": "2026-06-09",
  "delivery_zip_code": "1000001",
  "delivery_address": "東京都千代田区千代田1-1",
  "delivery_name": "山田 太郎",
  "delivery_phone": "0312345678",
  "special_instruction": "置き配不可",
  "items": [
    {
      "item_code": "ITM0000001",
      "item_name": "標準商品A",
      "quantity": 1,
      "unit_weight_gram": 850
    },
    {
      "item_code": "ITM0000002",
      "item_name": "標準商品B",
      "quantity": 1,
      "unit_weight_gram": 1200
    }
  ]
}
```

### 3.5 項目定義

| 項目名 | 物理名 | 型 | 必須 | 説明 |
| --- | --- | --- | --- | --- |
| 注文ID | order_id | string | 必須 | Hoge社内注文ID |
| 提携先注文ID | partner_order_id | string | 必須 | Foo社側注文ID |
| 出荷依頼ID | shipment_request_id | string | 必須 | Hoge社側の配送依頼単位ID |
| 注文元コード | order_source_code | string | 必須 | `FOO`, `HOGE` |
| 優先配送区分 | shipping_priority_class | string | 必須 | `NORMAL`, `PRIORITY` |
| 提携先優先度 | partner_priority_level | number | 任意 | Foo社優先度。非優先時は `0` |
| 配送種別 | delivery_type | string | 必須 | `NORMAL`, `EXPRESS` |
| サービスレベル | service_level | string | 必須 | `SAME_DAY`, `NEXT_DAY`, `DATE_SPECIFIED` |
| 温度帯 | temperature_zone | string | 必須 | `AMBIENT`, `COOL` |
| 荷物個数 | package_count | number | 必須 | 1以上 |
| 代引金額 | cash_on_delivery_amount | number | 必須 | 0以上 |
| 出荷希望日 | requested_ship_date | string | 必須 | yyyy-MM-dd |
| 配送希望日 | requested_delivery_date | string | 任意 | yyyy-MM-dd |
| 配送先郵便番号 | delivery_zip_code | string | 必須 | 半角数字7桁 |
| 配送先住所 | delivery_address | string | 必須 | 200文字以内 |
| 配送先氏名 | delivery_name | string | 必須 | 60文字以内 |
| 配送先電話番号 | delivery_phone | string | 必須 | 半角数字10〜11桁 |
| 特記事項 | special_instruction | string | 任意 | 200文字以内 |
| 商品明細 | items | array | 必須 | 1件以上 |

### 3.6 業務バリデーション

| 観点 | 条件 | エラー時の扱い |
| --- | --- | --- |
| 注文元コード | `FOO`, `HOGE` のいずれか | 422 |
| 優先配送区分 | `NORMAL`, `PRIORITY` のいずれか | 422 |
| 優先配送区分と注文元 | `order_source_code=HOGE` の場合 `shipping_priority_class=PRIORITY` は不可 | 422 |
| 提携先優先度 | `shipping_priority_class=PRIORITY` の場合 `partner_priority_level` は 5〜9 必須 | 422 |
| 配送種別 | `NORMAL`, `EXPRESS` のいずれか | 422 |
| サービスレベル組合せ | `EXPRESS` のとき `SAME_DAY` または `NEXT_DAY` のみ | 422 |
| 温度帯組合せ | `EXPRESS` かつ `COOL` は不可 | 422 |
| 代引金額 | `cash_on_delivery_amount > 0` の場合 `delivery_type = NORMAL` のみ | 422 |
| 荷物個数 | `package_count` は `items` の数量合計以下 | 422 |
| 配送希望日 | 出荷希望日より前日は不可 | 422 |
| 特記事項 | 禁止語句を含む場合は受付不可 | 422 |

### 3.7 バー社サーバ側冪等性

#### 3.7.1 判定ルール

- バー社は `X-Partner-Code + X-Idempotency-Key` を冪等性キーとして保存する
- 冪等性キーの保持期間は48時間とする
- 同一冪等性キーかつ同一リクエストボディの場合、バー社は新規登録を行わず前回結果を返す
- 同一冪等性キーかつ異なるリクエストボディの場合、`409 Conflict` を返す
- 異なる冪等性キーでも、同一 `shipment_request_id` が既に受付済みでキャンセルされていない場合は `409 Conflict` を返す

#### 3.7.2 戻り値の考え方

| ケース | HTTP | 説明 |
| --- | --- | --- |
| 新規受付 | 201 | 配送案件を新規登録 |
| 同一再送 | 200 | 既存受付結果を再返却 |
| 同一キー内容相違 | 409 | クライアント実装不整合 |
| 同一配送依頼の二重送信 | 409 | 業務重複 |
| 営業時間外受付 | 503 | 受付時間外。Hoge社は再送待機へ戻す |

### 3.8 優先配送の扱い

- Hoge社は提携先から受け取った注文元と優先度を保持する。
- Hoge社自身は配送資材引当や配送順制御の詳細ロジックを持たず、Bar社向けには `shipping_priority_class` のみを編集して送信する。
- Foo注文で `shipping_priority_class=PRIORITY` の場合、Bar社は通常配送案件より優先して処理する可能性がある。

### 3.9 レスポンス例

#### 3.8.1 新規受付

```json
{
  "bar_shipment_id": "BARS202606080001",
  "shipment_request_id": "SHP-20260620142015-A1B2C3",
  "acceptance_status": "ACCEPTED",
  "accepted_at": "2026-06-08T02:00:03",
  "duplicate": false
}
```

#### 3.8.2 同一再送

```json
{
  "bar_shipment_id": "BARS202606080001",
  "shipment_request_id": "SHP-20260620142015-A1B2C3",
  "acceptance_status": "ACCEPTED",
  "accepted_at": "2026-06-08T02:00:03",
  "duplicate": true
}
```

## 4. 配送結果返却API

### 4.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | IF-BAR-HOGE-002 |
| API名 | 配送結果返却API |
| 送信元 | Bar Delivery Center |
| 送信先 | Hoge OrderHub 配送状態取込Worker |
| 方式 | REST API / HTTPS |
| メソッド | POST |
| パス | `/api/v1/delivery-results/bar` |

### 4.2 業務用途

バー社で進行した配送イベントを、Hoge社の配送状態管理へ反映する。  
1案件に対して複数回通知されることを前提とし、単一の完了通知APIではなくイベント通知APIとして扱う。

### 4.3 リクエスト例

```json
{
  "bar_shipment_id": "BARS202606080001",
  "order_id": "O20260620142015A1B2C3",
  "partner_order_id": "FO202606080001",
  "status_seq": 2,
  "delivery_status": "PREPARING",
  "status_label": "配送準備中",
  "event_occurred_at": "2026-06-08T10:30:00",
  "location_code": "TKY-CHY",
  "reason_code": null,
  "reason_category": "ADDRESS_CORRECTED",
  "address_corrected": true,
  "address_correction_level": "MINOR",
  "driver_comment": null
}
```

### 4.4 項目定義

| 項目名 | 物理名 | 型 | 必須 | 説明 |
| --- | --- | --- | --- | --- |
| バー社配送案件ID | bar_shipment_id | string | 必須 | バー社採番ID |
| 注文ID | order_id | string | 必須 | Hoge社内注文ID |
| 提携先注文ID | partner_order_id | string | 必須 | Foo社側注文ID |
| ステータス通番 | status_seq | number | 必須 | 同一案件内で単調増加 |
| 配送ステータス | delivery_status | string | 必須 | 状態コード |
| ステータス表示名 | status_label | string | 必須 | 業務表示用ラベル |
| 発生日時 | event_occurred_at | string | 必須 | yyyy-MM-ddTHH:mm:ss |
| 拠点コード | location_code | string | 任意 | 配送拠点 |
| 理由コード | reason_code | string | 任意 | 例外理由 |
| 理由分類コード | reason_category | string | 任意 | 外部表示用の理由分類 |
| 住所補正有無 | address_corrected | boolean | 任意 | 軽微な住所補正をBar社内で行った場合に `true` |
| 住所補正レベル | address_correction_level | string | 任意 | `MINOR`, `MAJOR` |
| ドライバコメント | driver_comment | string | 任意 | 200文字以内 |

### 4.5 ステータス遷移制約

| 現在状態 | 遷移可能状態 |
| --- | --- |
| `ACCEPTED` | `PREPARING`, `CANCELLED` |
| `PREPARING` | `IN_TRANSIT`, `CANCELLED` |
| `IN_TRANSIT` | `DELIVERED`, `DELIVERY_FAILED`, `RETURNED_TO_BASE`, `ADDRESS_ERROR` |
| `DELIVERY_FAILED` | `IN_TRANSIT`, `CANCELLED`, `RETURNED_TO_BASE` |

### 4.6 Hoge社受信時の期待挙動

- 同一 `bar_shipment_id` で `status_seq` が重複した場合は再送として扱う
- より小さい `status_seq` が後着した場合は旧イベントとして無視する
- `reason_code` が存在する場合、Hoge社では配送状態だけでなく調査理由として保持する
- `reason_category` は Foo社・Qux社向け表示制御および Hoge社内運用確認用に保持する
- `address_corrected=true` の場合、Hoge社は表示用状態名を `住所補正対応中` へ正規化する

## 5. エラー応答設計

| API | HTTP | コード | 説明 |
| --- | --- | --- | --- |
| 出荷指示登録API | 400 | `BAR-REQ-001` | JSON形式不正 |
| 出荷指示登録API | 409 | `BAR-IDEMP-001` | 冪等性キー重複かつ内容差異 |
| 出荷指示登録API | 409 | `BAR-BIZ-009` | `shipment_request_id` 重複 |
| 出荷指示登録API | 422 | `BAR-BIZ-004` | 業務バリデーション不正 |
| 出荷指示登録API | 503 | `BAR-WINDOW-001` | 営業時間外受付 |
| 配送結果返却API | 400 | `BAR-RES-001` | 必須項目不足 |
| 配送結果返却API | 409 | `BAR-RES-009` | 許可されない状態遷移 |

## 6. 補足

- バー社側の冪等性は、アプリケーションサーバで判定し永続化する
- Hoge社の再送は、タイムアウトと5xxに限って行う
- 4xx応答は業務エラーとして扱い、機械的な再送対象にしない
