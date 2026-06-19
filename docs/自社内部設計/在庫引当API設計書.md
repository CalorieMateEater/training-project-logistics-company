# 05. API設計書 Hoge Stock Keeper

## 1. 対象IF

- IF-ID: `IF-HOGE-STK-001`
- IF名: 在庫引当API

## 2. 基本情報

| 項目 | 内容 |
| --- | --- |
| API名 | 在庫引当API |
| 送信元 | Hoge OrderHub Batch / Hoge Shipping Gateway |
| 送信先 | Hoge Stock Keeper |
| 方式 | REST API / HTTPS |
| メソッド | POST |
| パス | `/api/v1/stocks/reservations` |
| タイムアウト | 5秒 |
| リトライ | 最大1回 |
| 認証方式 | OAuth2 Client Credentials |
| 接続経路 | OrderHub Batch / 出荷依頼受付API → Internal API Gateway / 社内API入口 → Stock Keeper |

## 3. 用途

顧客確認完了後、注文商品の在庫を引き当てるために呼び出す同期APIである。Foo社ファイル取込とFuga社API受付の双方で利用する。

## 4. リクエスト

### 4.1 リクエスト例

```json
{
  "order_id": "O202606080001",
  "items": [
    {
      "item_code": "ITM0000001",
      "quantity": 2
    }
  ]
}
```

### 4.2 項目定義

| 項目名 | 物理名 | 型 | 必須 | 桁数 | 説明 |
| --- | --- | --- | --- | ---: | --- |
| 注文ID | order_id | string | 必須 | 14 | Hoge OrderHubで採番した注文ID |
| 商品リスト | items | array | 必須 | - | 引当対象の商品一覧 |
| 商品コード | item_code | string | 必須 | 10 | 商品コード |
| 数量 | quantity | number | 必須 | 1〜999 | 引当数量 |

## 5. レスポンス

### 5.1 レスポンス例

```json
{
  "reservation_id": "R202606080001",
  "status": "RESERVED"
}
```

## 6. バリデーション

| 項目 | 条件 |
| --- | --- |
| order_id | 半角英数字14桁 |
| item_code | 半角英数字10桁 |
| quantity | 1以上999以下 |

## 7. 補足

- `item_code` は入力ファイルおよびAPIリクエストの双方で半角英数字10桁を前提とする
- Foo社ファイル取込では、異常値 `ITEM-000012345` は本API到達前に `Hoge OrderHub` 側でバリデーションエラーとなる
- Fuga社API受付では、基本形式を通過した後に本APIで在庫引当可否を判定する
- 認証トークン取得に必要な資格情報は Secrets Manager / KMS で管理する
