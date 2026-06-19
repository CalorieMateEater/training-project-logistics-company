# 04. API設計書 Hoge Customer Registry

## 1. 対象IF

- IF-ID: `IF-HOGE-REG-001`
- IF名: 顧客確認API

## 2. 基本情報

| 項目 | 内容 |
| --- | --- |
| API名 | 顧客確認API |
| 送信元 | Hoge OrderHub Batch / Hoge Shipping Gateway |
| 送信先 | Hoge Customer Registry |
| 方式 | REST API / HTTPS |
| メソッド | GET |
| パス | `/api/v1/customers/{customer_id}/status` |
| タイムアウト | 3秒 |
| リトライ | 最大2回 |
| 認証方式 | mTLS + APIキー |
| 接続経路 | OrderHub Batch / 出荷依頼受付API → Internal API Gateway / 社内API入口 → Customer Registry |

## 3. 用途

`FooOrderImportJob` および `出荷依頼受付API` にて、入力検証通過後の依頼に対して顧客存在確認および会員状態確認を行う。

## 4. リクエスト

### 4.1 パスパラメータ

| 項目名 | 物理名 | 型 | 必須 | 桁数 | 説明 |
| --- | --- | --- | --- | ---: | --- |
| 顧客ID | customer_id | string | 必須 | 12 | 顧客を一意に識別するID |

## 5. レスポンス

### 5.1 レスポンス例

```json
{
  "customer_id": "C00000000001",
  "status": "ACTIVE",
  "member_rank": "GOLD"
}
```

### 5.2 項目定義

| 項目名 | 物理名 | 型 | 必須 | 説明 |
| --- | --- | --- | --- | --- |
| 顧客ID | customer_id | string | 必須 | 顧客ID |
| 会員状態 | status | string | 必須 | ACTIVE, SUSPENDED, WITHDRAWN |
| 会員ランク | member_rank | string | 任意 | NORMAL, SILVER, GOLD, PLATINUM |

## 6. バリデーション

| 項目 | 条件 |
| --- | --- |
| customer_id | 半角英数字12桁 |
| status | ACTIVE, SUSPENDED, WITHDRAWN のいずれか |
| member_rank | NORMAL, SILVER, GOLD, PLATINUM のいずれか |

## 7. 補足

- 本APIは正常系後続処理の一部である
- Foo社ファイル取込では、入力ファイルがバリデーションエラーで停止した場合、本APIは未到達となる
- Fuga社API受付では、リクエストの基本検証通過後に本APIを呼び出す
- API実行ログは CloudWatch Logs に送られ、異常時はDatadog監視対象となる
