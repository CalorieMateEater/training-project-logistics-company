# Hoge OrderHub Project

Hoge社の受注連携・配送連携システムを題材にした、設計資料群と Java 実装を管理するリポジトリです。

本リポジトリには、要件定義、対外連携設計、自社内部設計、基盤設計、運用設計、PJ管理資料と、それに対応する Spring Boot / Gradle / Java 17 の実装が含まれています。

## リポジトリ構成

```text
.
├─ docs/
│  ├─ 要件定義/
│  ├─ 共通資料/
│  ├─ 対外連携/
│  ├─ 自社内部設計/
│  ├─ 運用調査/
│  └─ PJ管理/
└─ java/
   ├─ hoge-orderhub-common/
   ├─ hoge-orderhub-batch/
   ├─ hoge-orderhub-worker/
   ├─ hoge-shipping-gateway-api/
   ├─ hoge-customer-registry-api/
   └─ hoge-stock-keeper-api/
```

## ドキュメント構成

### 要件定義
- `docs/要件定義/要件定義概要.md`
- `docs/要件定義/システム概要.md`
- `docs/要件定義/アクター定義書.md`
- `docs/要件定義/業務機能一覧.md`
- `docs/要件定義/業務ルール定義書.md`
- `docs/要件定義/業務フロー/`

### 共通資料
- `docs/共通資料/システム間連携図.md`
- `docs/共通資料/外部インターフェース一覧.md`

### 対外連携
- `docs/対外連携/フー社/連携ファイル設計書.md`
- `docs/対外連携/バー社/連携API設計書.md`
- `docs/対外連携/バー社/連携APIアプリ設計書.md`
- `docs/対外連携/バー社/連携API基盤設計書.md`
- `docs/対外連携/フーガ社/連携API設計書.md`
- `docs/対外連携/フーガ社/連携APIアプリ設計書.md`
- `docs/対外連携/フーガ社/連携API基盤設計書.md`
- `docs/対外連携/バズ社/連携メッセージ設計書.md`
- `docs/対外連携/クックス社/連携メッセージ設計書.md`

### 自社内部設計
- `docs/自社内部設計/業務設計/業務設計概要.md`
- `docs/自社内部設計/業務設計/ビジネスルール定義書.md`
- `docs/自社内部設計/業務設計/データモデル設計書.md`
- `docs/自社内部設計/業務設計/テーブル一覧.md`
- `docs/自社内部設計/業務設計/処理機能・インターフェース一覧.md`
- `docs/自社内部設計/業務設計/処理設計書/`
- `docs/自社内部設計/業務設計/処理機能設計書/`
- `docs/自社内部設計/業務設計/詳細業務フロー/`
- `docs/自社内部設計/基盤設計/基盤設計概要.md`

### 運用調査
- `docs/運用調査/運用設計概要.md`
- `docs/運用調査/監視設計書.md`
- `docs/運用調査/処理手順書.md`
- `docs/運用調査/障害対応手順書.md`
- `docs/運用調査/障害調査整理資料.md`
- `docs/運用調査/リリース設計書.md`

### PJ管理
- `docs/PJ管理/PJ管理概要.md`
- `docs/PJ管理/プロジェクトメンバー管理/`
- `docs/PJ管理/対外会議議事録/`

## Java 実装構成

`java/` 配下は機能単位で分割したマルチプロジェクト構成です。

- `hoge-orderhub-common`
  - 共通 Entity、Repository、DTO、ユーティリティ
- `hoge-orderhub-batch`
  - Foo社 HULFT 注文取込、注文受付通知連携、日次アーカイブ
- `hoge-orderhub-worker`
  - Bar社向け出荷依頼、配送結果返却、SQS ベースの非同期処理
- `hoge-shipping-gateway-api`
  - 出荷依頼受付 API、出荷状態照会 API、Bar社配送結果受付 API
- `hoge-customer-registry-api`
  - 顧客マスタ確認 API
- `hoge-stock-keeper-api`
  - 在庫引当 API

詳細は `java/README.md` を参照してください。

## ビルド方法

ルートではなく `java/` 配下で Gradle を実行します。

```powershell
cd java
.\gradlew.bat build
```

## 前提技術

- Java 17
- Spring Boot 3.3.x
- Gradle 8.x
- Spring Data JPA
- H2 Database
- MapStruct
- Lombok

## このリポジトリで扱う主な業務・連携

- Foo社から HULFT で注文ファイルを受信する
- Hoge社で注文受付、顧客確認、在庫引当、配送会社選定を行う
- Bar社へ配送依頼を連携し、配送結果を取り込む
- Foo社、Fuga社から出荷状態照会を受け付ける
- バズ社、クックス社へ非同期メッセージ連携を行う
- 日次で完了済みデータをアーカイブする

## 補足

- PDF は一部資料の派生物であり、Markdown 側を正本として扱います。
- AWS マネージドサービスやミドルウェアの採用バージョンは `docs/自社内部設計/基盤設計/構成管理/製品・ミドルウェアバージョン管理台帳.md` を参照してください。
- VS Code の共有設定は `.vscode/settings.json` を利用します。
