# Hoge Java Projects

`java/` 配下には、Hoge社システムを機能単位で分割した Spring Boot / Gradle / Java 17 のプロジェクトを配置する。

## 構成

- `hoge-orderhub-common`
  - OrderHub 共通ドメイン、JPA エンティティ、Repository、共通 DTO
- `hoge-customer-registry-api`
  - 顧客確認 API
- `hoge-stock-keeper-api`
  - 在庫引当 API
- `hoge-shipping-gateway-api`
  - 出荷依頼受付 API、出荷状態照会 API、Bar 配送結果受信 API
- `hoge-orderhub-batch`
  - Foo HULFT 注文ファイル取込、受付通知、日次アーカイブ
- `hoge-orderhub-worker`
  - Bar 送信待ち処理、Baz/Qux 通知、Foo 配送状態返却

## 補足

- Dockerfile は各プロジェクト配下に配置する。
- ローカル起動用のデータストアは H2 を利用し、OrderHub 系 3 アプリは同一ファイル DB を参照する想定とする。
- 本リポジトリには Gradle Wrapper を含めていないため、ビルド時は Gradle 実行環境を別途用意する。
