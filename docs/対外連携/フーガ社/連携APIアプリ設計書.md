# 配送連携APIアプリ設計書 フーガ社

## 1. 対象IF

- IF-ID: `IF-FUGA-HOGE-003`
- IF-ID: `IF-FUGA-HOGE-004`

## 2. API一覧

| API | 利用者 | 用途 |
| --- | --- | --- |
| 出荷依頼受付API | フーガ社 | Hoge社への出荷依頼登録 |
| 出荷状態照会API | フーガ社 | 出荷・配送状態の参照 |

## 3. 出荷依頼受付API

### 3.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | IF-FUGA-HOGE-003 |
| API名 | 出荷依頼受付API |
| メソッド | POST |
| パス | `/api/v1/shipment-requests` |
| 送信元 | フーガ社 出荷依頼クライアント |
| 送信先 | Hoge Shipping Gateway |

### 3.2 業務用途

フーガ社業務システムで管理する出荷依頼を、Hoge社の配送管理対象として登録する。  
Hoge社は24時間365日で依頼を受け付けるが、受け付けた依頼は即時にバー社へ送信されるとは限らず、`配送条件判定`、顧客確認、在庫引当、送信待ちキュー登録、後続Worker処理の順に評価される。  
バー社への実送信は平日08:00-18:00の営業時間帯に実施する。

Hoge社は本APIで受け付けた注文を `order_source=FUGA` として登録し、Bar向け優先配送区分は `NORMAL` 固定とする。  
Foo社予約注文の優先度とは連動しない。
予約出荷を受け付けた場合、Hoge社は顧客確認・在庫引当までは即時実施し、Bar社向け送信は `shipping_release_at` 到来後に開始する。

### 3.3 リクエストヘッダ

| ヘッダ名 | 必須 | 説明 |
| --- | --- | --- |
| `X-Client-System-Id` | 必須 | 固定値 `FUGA-PORTAL` |
| `X-Request-Id` | 必須 | フーガ社側一意要求ID |
| `X-Trace-Id` | 必須 | 相互追跡用ID |
| `Authorization` | 必須 | APIキー認証 |

### 3.4 リクエスト例

```json
{
  "partner_request_id": "FG202606140001",
  "partner_order_id": "FGO202606140001",
  "customer_id": "C00000000001",
  "item_code": "ITM0000001",
  "quantity": 1,
  "shipment_preference": "STANDARD",
  "shipment_mode": "RESERVED",
  "delivery_constraint": {
    "temperature_zone": "AMBIENT",
    "time_slot": "EVENING"
  },
  "delivery_zip_code": "1000001",
  "delivery_address": "東京都千代田区千代田1-1",
  "requested_delivery_date": "2026-06-16",
  "shipping_release_at": "2026-06-16T08:00:00"
}
```

### 3.5 項目定義

| 項目名 | 物理名 | 型 | 必須 | 説明 |
| --- | --- | --- | --- | --- |
| 要求ID | partner_request_id | string | 必須 | フーガ社側要求一意キー |
| 注文ID | partner_order_id | string | 必須 | フーガ社側注文ID |
| 顧客ID | customer_id | string | 必須 | Hoge社顧客ID |
| 商品コード | item_code | string | 必須 | Hoge社商品コード |
| 数量 | quantity | number | 必須 | 1以上 |
| 出荷希望区分 | shipment_preference | string | 必須 | `STANDARD`, `DATE_SPECIFIED` |
| 出荷モード | shipment_mode | string | 任意 | `IMMEDIATE`, `RESERVED` |
| 配送制約 | delivery_constraint | object | 必須 | 温度帯・時間帯など |
| 配送先郵便番号 | delivery_zip_code | string | 必須 | 半角数字7桁 |
| 配送先住所 | delivery_address | string | 必須 | 200文字以内 |
| 配送希望日 | requested_delivery_date | string | 任意 | yyyy-MM-dd |
| 出荷解放日時 | shipping_release_at | string | 任意 | yyyy-MM-ddTHH:mm:ss。予約出荷時に指定 |

### 3.6 バリデーション

| 観点 | 条件 | エラー時の扱い |
| --- | --- | --- |
| 顧客ID | 顧客マスタに存在すること | 422 |
| 商品コード | 在庫システムで有効なこと | 422 |
| 数量 | 1以上999以下 | 422 |
| shipment_mode | 指定時は `IMMEDIATE`, `RESERVED` のいずれか | 422 |
| shipping_release_at | `shipment_mode=RESERVED` の場合は必須 | 422 |
| shipping_release_at | `shipment_mode=IMMEDIATE` の場合は指定不可 | 422 |
| 温度帯 | `AMBIENT`, `COOL` のいずれか | 422 |
| 時間帯 | `AM`, `PM`, `EVENING` のいずれか | 422 |
| 配送希望日 | 当日より前は不可 | 422 |

### 3.7 正常応答例

```json
{
  "order_id": "O202606140001",
  "partner_request_id": "FG202606140001",
  "registration_status": "ACCEPTED",
  "current_status": "WAITING_SHIPPING_RELEASE",
  "accepted_at": "2026-06-14T10:00:00"
}
```

## 4. 出荷状態照会API

### 4.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | IF-FUGA-HOGE-004 |
| API名 | 出荷状態照会API |
| メソッド | GET |
| パス | `/api/v1/shipment-status/{lookup_key}` |

### 4.2 照会キー

- 主キーは `partner_order_id`
- Fuga社は `partner_order_id` に加え `partner_request_id` でも照会可能とする
- 同一キーで複数配送イベントが存在する場合、最新状態を返却する

### 4.3 レスポンス例

```json
{
  "partner_request_id": "FG202606140001",
  "partner_order_id": "FGO202606140001",
  "order_id": "O202606140001",
  "current_status": "BAR_ACCEPTED",
  "delivery_company_code": "BAR",
  "latest_status_datetime": "2026-06-14T10:01:30",
  "allocation": {
    "allocation_status": "COMPLETED",
    "delivery_company_code": "BAR"
  },
  "latest_event": {
    "status_code": "PREPARING",
    "status_label": "配送準備中",
    "reason_category": "ADDRESS_CORRECTED",
    "display_status_name": "住所補正対応中"
  }
}
```

### 4.4 状態コード

| 状態コード | 説明 |
| --- | --- |
| `WAITING_BAR_REQUEST` | Bar営業時間待ち、または送信待ちキュー格納中 |
| `WAITING_SHIPPING_RELEASE` | 予約出荷の解放日時待ち |
| `BAR_REQUESTED` | Bar向け出荷依頼送信済 |
| `BAR_ACCEPTED` | Bar社受付済 |
| `PREPARING_FOR_SHIPMENT` | 配送準備中 |
| `IN_DELIVERY_FLOW` | 配送中 |
| `COMPLETED` | 配送完了 |
| `EXCEPTION` | 配送失敗、持戻り、住所不備などの例外状態 |
| `REDISPATCH_PENDING` | 再配達待ち |
| `CANCELLED` | キャンセル |

## 5. エラー応答設計

| API | HTTP | コード | 説明 |
| --- | --- | --- | --- |
| 出荷依頼受付API | 400 | `HSG-REQ-001` | JSON形式不正 |
| 出荷依頼受付API | 401 | `HSG-AUTH-001` | APIキー不正 |
| 出荷依頼受付API | 409 | `HSG-REQ-009` | `partner_request_id` 重複 |
| 出荷依頼受付API | 422 | `HSG-BIZ-004` | 業務バリデーション不正 |
| 出荷状態照会API | 404 | `HSG-STS-404` | 対象注文なし |

## 6. 補足

- フーガ社向けAPIは利用側仕様であり、配送会社向けAPIではない
- 出荷状態照会API自体は共有エンドポイントだが、本書ではフーガ社の利用条件のみを記載する
- 出荷依頼取消APIは要件候補として管理するが、本版では未採用とする
- 接続経路、APIキー運用、レート制御は [連携API基盤設計書](/C:/Users/yaku_/Documents/training-project-01/docs/対外連携/フーガ社/連携API基盤設計書.md) を参照する
