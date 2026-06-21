# 連携API設計書 フーガ社

## 1. 目的
本書は、Hoge社とフーガ社の配送委託連携で利用するAPIの概要を整理する。
フーガ社は注文元ではなく、特殊配送を担当する配送委託先である。

## 2. 対象IF

- IF-ID: `IF-HOGE-FUGA-001`
- IF名: 特殊配送依頼送信API
- 方向: Hoge社 → フーガ社

- IF-ID: `IF-FUGA-HOGE-002`
- IF名: 特殊配送結果通知API
- 方向: フーガ社 → Hoge社

## 3. 連携概要

| IF-ID | 方向 | 方式 | 概要 |
| --- | --- | --- | --- |
| `IF-HOGE-FUGA-001` | Hoge社 → フーガ社 | REST API / HTTPS | 冷蔵便、大型商品、遠方配送などの特殊配送案件をフーガ社へ依頼する |
| `IF-FUGA-HOGE-002` | フーガ社 → Hoge社 | REST API / HTTPS | フーガ社で処理中の配送案件について、受付済・配送中・配送完了・異常終了などの結果を通知する |

## 4. 業務前提

- Hoge社は商品在庫を保有し、在庫引当と倉庫場所確定を自社で行う
- フーガ社は配送事業者として、特殊配送条件に該当する案件のみを担当する
- Hoge社は注文元を `FOO` または `HOGE` として管理する
- フーガ社連携では、注文元コード、配送優先度、倉庫場所、温度帯、サイズ区分を配送依頼電文に含める
- フーガ社からの配送結果は、Hoge社が配送状態管理、Foo社返却、Baz/Qux通知に利用する

## 5. 特殊配送依頼送信API

### 5.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | `IF-HOGE-FUGA-001` |
| メソッド | `POST` |
| パス | `/api/v1/fuga-shipments` |
| 呼出元 | Hoge OrderHub Worker |
| 呼出先 | Fuga Delivery Center |
| 用途 | 特殊配送依頼登録 |

### 5.2 主な送信項目

- `order_id`
- `partner_order_id`
- `shipment_request_id`
- `order_source_code`
- `shipping_priority_class`
- `source_warehouse_location_code`
- `temperature_zone`
- `size_type`
- `requested_ship_date`
- `delivery_zip_code`
- `delivery_address`
- `items`

## 6. 特殊配送結果通知API

### 6.1 概要

| 項目 | 内容 |
| --- | --- |
| IF-ID | `IF-FUGA-HOGE-002` |
| メソッド | `POST` |
| パス | `/api/v1/delivery-results/fuga` |
| 呼出元 | Fuga Delivery Center |
| 呼出先 | Hoge OrderHub |
| 用途 | 特殊配送イベント通知 |

### 6.2 主な通知項目

- `fuga_shipment_id`
- `order_id`
- `partner_order_id`
- `status_seq`
- `delivery_status`
- `status_label`
- `event_occurred_at`
- `temperature_zone`
- `size_type`
- `reason_code`
- `reason_category`

## 7. 関連資料

- [連携APIアプリ設計書](./連携APIアプリ設計書.md)
- [連携API基盤設計書](./連携API基盤設計書.md)
- [外部インターフェース一覧](../../外部インターフェース一覧.md)
