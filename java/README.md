# Hoge Java Projects

`java/` 配下には、Hoge社システムを機能単位で分割した Spring Boot / Gradle / Java 17 のプロジェクトを配置する。

## 構成

- `hoge-orderhub-common`
  - OrderHub 共通ドメイン、JPA エンティティ、Repository、共通 DTO
- `hoge-customer-registry-api`
  - 顧客確認 API
- `hoge-stock-keeper-api`
  - 在庫管理 API（在庫引当、引当解除、出荷確定、倉庫場所返却）
- `hoge-shipping-gateway-api`
  - 出荷依頼受付 API、出荷状態照会 API、Bar 配送結果受信 API、内部運用向け出荷取消 API
- `hoge-orderhub-batch`
  - Foo HULFT 注文ファイル取込、受付通知、日次アーカイブ
- `hoge-orderhub-worker`
  - Bar 送信待ち処理、Baz/Qux 通知、Foo 配送状態返却

## 補足

- Dockerfile は各プロジェクト配下に配置する。
- ローカル起動用のデータストアは H2 を利用し、OrderHub 系 3 アプリは同一ファイル DB を参照する想定とする。
- Stock Keeper は倉庫場所コード単位で在庫を管理し、保有在庫数、引当済在庫数、利用可能在庫数を保持する。
- 引当結果は OrderHub 側へ倉庫場所コード付きで返却し、Bar 社の `ACCEPTED` 受信時に出荷確定する。
- 未出荷の注文は Shipping Gateway の内部運用 API から取消でき、在庫引当解除まで一括で実行する。
- 本リポジトリには Gradle Wrapper を含めているため、ビルド時は `./gradlew` または `gradlew.bat` を利用できる。
