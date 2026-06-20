# MTD-004 Hoge直受注登録メソッド設計書

## 1. 基本情報
| 項目 | 内容 |
| --- | --- |
| メソッド設計書ID | `MTD-004` |
| 対応処理機能ID | `PGD-004` |
| 対象論理機能 | Hoge直受注登録 |
| 関連処理設計書ID | `PDS-007` |

## 2. 対象メソッド
| メソッド | 種別 | 説明 |
| --- | --- | --- |
| `register(ShipmentRegistrationRequest request, String clientSystemId, String requestId, String traceId)` | `public` | Hoge社業務部門の直受注を受け付ける。 |

## 3. `register(...)`
### 3.1 シグネチャ
```java
public ShipmentRegistrationAcceptedResponse register(
        ShipmentRegistrationRequest request,
        String clientSystemId,
        String requestId,
        String traceId
)
```

### 3.2 処理概要
1. 呼出元識別子を検証し、直受注登録ポータル以外を拒否する。
2. リクエストの必須項目、数量、配送条件を検証する。
3. 顧客状態と在庫引当結果を確認する。
4. 注文元 `HOGE` の出荷依頼として注文・明細・出荷依頼待ちを登録する。
5. 配送会社がBarで営業時間外の場合は `WAITING_BUSINESS_HOURS` で待機させる。
6. 受付番号を返却し、IF履歴を記録する。

### 3.3 フロー図
```mermaid
flowchart TD
    A[API受付] --> B[呼出元検証]
    B --> C[入力検証]
    C --> D[顧客状態照会]
    D --> E[在庫引当]
    E --> F[注文・出荷依頼登録]
    F --> G[営業時間判定]
    G --> H[受付応答返却]
    H --> I[IF履歴記録]
```

