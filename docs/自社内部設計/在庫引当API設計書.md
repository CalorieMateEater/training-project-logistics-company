# 05. API設計書 Hoge Stock Keeper

## 1. 対象IF

| IF-ID | IF名 | パス | 用途 |
| --- | --- | --- | --- |
| `IF-HOGE-STK-001` | 在庫引当API | `POST /api/v1/stocks/reservations` | 倉庫場所を確定し、引当済在庫を登録する |
| `IF-HOGE-STK-002` | 在庫引当解除API | `POST /api/v1/stocks/reservations/{reservation_id}/releases` | 未出荷取消や障害復旧時に引当済在庫を解放する |
| `IF-HOGE-STK-003` | 在庫出荷確定API | `POST /api/v1/stocks/reservations/{reservation_id}/ship-confirms` | Bar社の配送受付済受信時に引当済在庫を出荷確定へ更新する |
| `IF-HOGE-STK-004` | 倉庫在庫照会API | `GET /api/v1/stocks/inventories` | 倉庫担当者が担当倉庫場所の在庫を照会する |
| `IF-HOGE-STK-005` | 入庫登録API | `POST /api/v1/stocks/receipts` | 倉庫担当者が実入庫を在庫へ反映する |

## 2. 基本情報

| 項目 | 内容 |
| --- | --- |
| API名 | Hoge Stock Keeper 在庫管理API |
| 送信元 | Hoge OrderHub Batch / Hoge Shipping Gateway / Hoge OrderHub Worker / Hoge倉庫担当者端末 |
| 送信先 | Hoge Stock Keeper |
| 方式 | REST API / HTTPS |
| タイムアウト | 在庫引当系は5秒、倉庫在庫照会は5秒、入庫登録は10秒 |
| リトライ | OrderHub系は最大1回、倉庫在庫照会は最大1回、入庫登録は自動再試行なし |
| 認証方式 | OrderHub系は OAuth2 Client Credentials、倉庫担当者端末は OIDC Access Token |
| 接続経路 | OrderHub Batch / 出荷依頼受付API / 配送状態取込Worker / 倉庫担当者端末 → Internal API Gateway / 社内API入口 → Stock Keeper |

## 3. 用途

Hoge社保有在庫を倉庫場所単位で管理する同期API群である。  
Foo社ファイル取込とHoge直受注登録では在庫引当APIを利用し、未出荷取消や障害復旧時には在庫引当解除API、Bar社の配送受付済受信時には在庫出荷確定APIを利用する。  
倉庫担当者端末は、担当倉庫場所に対する在庫照会APIと入庫登録APIを利用する。

## 4. 在庫引当API

### 4.1 リクエスト例

```json
{
  "orderId": "O20260620142015A1B2C3",
  "items": [
    {
      "itemCode": "ITM0000001",
      "quantity": 2
    }
  ]
}
```

### 4.2 項目定義

| 項目名 | 物理名 | 型 | 必須 | 桁数 | 説明 |
| --- | --- | --- | --- | ---: | --- |
| 注文ID | orderId | string | 必須 | 21 | Hoge OrderHubで採番した注文ID |
| 商品リスト | items | array | 必須 | - | 引当対象の商品一覧 |
| 商品コード | itemCode | string | 必須 | 10 | 商品コード |
| 数量 | quantity | number | 必須 | 1〜999 | 引当数量 |

### 4.3 レスポンス例

```json
{
  "reservationId": "RSV-20260620142015-D4E5F6",
  "status": "RESERVED",
  "results": [
    {
      "itemCode": "ITM0000001",
      "requestedQuantity": 2,
      "reservedQuantity": 2,
      "warehouseLocationCode": "WH-TYO-01",
      "reservationStatus": "RESERVED",
      "onHandQuantity": 60,
      "reservedTotalQuantity": 2,
      "availableQuantity": 58
    }
  ]
}
```

### 4.4 レスポンス項目

| 項目名 | 物理名 | 型 | 説明 |
| --- | --- | --- | --- |
| 引当ID | reservationId | string | 引当単位を識別するID |
| 引当状態 | status | string | 引当全体の状態 |
| 商品別結果 | results | array | 商品ごとの引当結果 |
| 商品コード | itemCode | string | 商品コード |
| 要求数量 | requestedQuantity | number | 要求された数量 |
| 引当数量 | reservedQuantity | number | 実際に引当した数量 |
| 倉庫場所コード | warehouseLocationCode | string | 引当先の倉庫場所コード |
| 商品別状態 | reservationStatus | string | `RESERVED`, `SHORTAGE` など |
| 保有在庫数 | onHandQuantity | number | 処理時点の保有在庫数 |
| 引当済在庫数 | reservedTotalQuantity | number | 処理後の引当済在庫数 |
| 利用可能在庫数 | availableQuantity | number | 処理後の利用可能在庫数 |

## 5. 在庫引当解除API

### 5.1 リクエスト例

```json
{
  "reservationId": "RSV-20260620142015-D4E5F6"
}
```

### 5.2 補足

- `reservation_id` 単位で解除する
- 在庫出荷確定済みの引当は解除不可とする
- 解除時は倉庫場所コードをもとに引当済在庫数のみを減算する

## 6. 在庫出荷確定API

### 6.1 リクエスト例

```json
{
  "reservationId": "RSV-20260620142015-D4E5F6"
}
```

### 6.2 補足

- `reservation_id` 単位で出荷確定する
- 初版では Bar社からの `配送受付済` イベント受信を契機に呼び出す
- 出荷確定時は保有在庫数と引当済在庫数を同時に減算する

## 7. 倉庫在庫照会API

### 7.1 リクエスト例

```http
GET /api/v1/stocks/inventories?warehouse_location_code=WH-TYO-01&item_code=ITM0000001
Authorization: Bearer eyJ...
```

### 7.2 レスポンス例

```json
{
  "warehouseLocationCode": "WH-TYO-01",
  "items": [
    {
      "itemCode": "ITM0000001",
      "onHandQuantity": 60,
      "reservedQuantity": 2,
      "availableQuantity": 58,
      "lastReceivedAt": "2026-06-21T09:15:00+09:00"
    }
  ]
}
```

### 7.3 補足

- `warehouse_location_code` は必須、`item_code` は任意とする
- 担当外倉庫場所は `403 Forbidden` とする
- 対象在庫が存在しない場合は 200 応答で `items=[]` を返す

## 8. 入庫登録API

### 8.1 リクエスト例

```json
{
  "warehouseLocationCode": "WH-TYO-01",
  "itemCode": "ITM0000001",
  "receivedQuantity": 20,
  "receiptReferenceNo": "RCV-20260621091501-000123"
}
```

### 8.2 レスポンス例

```json
{
  "receiptReferenceNo": "RCV-20260621091501-000123",
  "warehouseLocationCode": "WH-TYO-01",
  "itemCode": "ITM0000001",
  "receivedQuantity": 20,
  "status": "REGISTERED",
  "onHandQuantity": 80,
  "reservedQuantity": 2,
  "availableQuantity": 78,
  "lastReceivedAt": "2026-06-21T09:15:01+09:00"
}
```

### 8.3 補足

- `receipt_reference_no` は倉庫場所単位で一意とする
- 同一 `receipt_reference_no` の同一内容再送は 200 応答で初回結果を返す
- 同一 `receipt_reference_no` の異内容再送は `409 Conflict` とする
- 在庫残高が未作成の場合は 0 在庫を起点に新規作成する
- 入庫では保有在庫数と利用可能在庫数のみを増加させ、引当済在庫数は変更しない

## 9. バリデーション

| 項目 | 条件 |
| --- | --- |
| orderId | 半角英数字21桁 |
| itemCode | 半角英数字10桁 |
| quantity | 1以上999以下 |
| warehouseLocationCode | `WH-` で始まる半角英数字20桁以内 |
| receivedQuantity | 1以上9999以下 |
| receiptReferenceNo | 半角英数字記号40桁以内 |

## 10. 補足

- `item_code` は入力ファイルおよびAPIリクエストの双方で半角英数字10桁を前提とする
- Foo社ファイル取込では、商品コード形式が不正なレコードは本API到達前に `Hoge OrderHub` 側でバリデーションエラーとなる
- Hoge直受注登録では、基本形式を通過した後に本APIで在庫引当可否と倉庫場所を判定する
- Stock Keeper では `商品コード + 倉庫場所コード` 単位で排他制御を行う
- 倉庫場所コードは将来の配送会社選定や倉庫別配送制約判定で利用する
- 認証トークン取得に必要な資格情報は Secrets Manager / KMS で管理する
