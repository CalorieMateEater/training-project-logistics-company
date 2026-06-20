# Foo受注から配送結果返却業務フロー

## 1. 目的
Foo社からの注文受付後、Hoge社が24時間365日で依頼を受け付け、自社在庫の倉庫場所と配送条件をもとに Bar社またはFuga社へ委託しながら、段階的に配送状態を返却する一連の業務を整理する。

## 2. 登場アクター
- Foo社担当者
- Foo社システム
- Hoge社システム
- 配送会社システム

## 3. 業務フロー図
```mermaid
sequenceDiagram
    box rgb(255, 244, 236) 他社
        actor FooUser as Foo社担当者
        participant FooSys as Foo社システム
    end
    box rgb(237, 246, 255) 自社
        participant HogeSys as Hoge社システム
    end
    box rgb(255, 244, 236) 他社
        participant CarrierSys as 配送会社システム
    end

    FooUser->>FooSys: 注文登録
    FooSys->>HogeSys: 注文ファイル送信
    HogeSys-->>FooSys: 注文受付通知
    HogeSys->>HogeSys: 顧客確認 / 自社在庫引当・倉庫場所確定 / 配送条件判定
    alt Fuga委託条件に合致
        HogeSys->>CarrierSys: Fuga向け出荷依頼送信
        CarrierSys-->>HogeSys: 配送依頼受付応答
    else shipping_release_at 指定あり かつ 未到来
        Note over HogeSys: 出荷解放待ちとして保持
    else Bar営業時間内 平日08:00-18:00
        HogeSys->>CarrierSys: Bar向け出荷依頼送信
        CarrierSys-->>HogeSys: 配送依頼受付応答
    else Bar営業時間外
        Note over HogeSys: キュー保持し、次の営業時間帯に送信
        HogeSys->>CarrierSys: 翌営業時間帯にBar向け出荷依頼送信
        CarrierSys-->>HogeSys: 配送依頼受付応答
    end
    loop 配送状態の進行
        CarrierSys-->>HogeSys: 配送受付済 / 配送準備中 / 配送中 / 配送完了
        HogeSys->>HogeSys: 配送受付済で在庫出荷確定
        HogeSys-->>FooSys: 配送状態返却
    end
    FooSys-->>FooUser: 受付結果・配送状態表示
```

## 4. 業務の流れ
1. Foo社担当者が Foo社システムへ注文を登録する。
2. Foo社システムが注文ファイルを Hoge社システムへ送信する。
3. Hoge社システムが注文を受け付け、配送会社への送信成否とは切り離して Foo社システムへ注文受付通知を返す。
4. Hoge社システムが顧客確認、自社保有在庫の在庫引当と倉庫場所確定、配送条件判定を行い、注文元を FOO として登録したうえで、予約注文かつ高優先度の場合は優先配送区分を設定する。
5. 冷蔵便、大型商品、遠方配送などの特殊配送条件に合致する場合は、Hoge社システムが Fuga社へ特殊配送依頼を送信する。
6. 特殊配送条件に合致しない場合は Bar社向け標準配送案件として扱い、`shipping_release_at` が未来日時なら出荷解放待ちとし、そうでなければ Bar送信待ちキューへ登録する。
7. Bar社営業時間内であれば、Hoge社システムがキューから依頼を取り出して Barシステムへ出荷依頼する。
8. Bar社営業時間外であれば、Hoge社システムは依頼を保持し、次の営業時間帯に Barシステムへ送信する。
9. BarシステムまたはFugaシステムが配送依頼受付応答を返し、Hoge社システムは配送依頼受付済として状態を更新する。
10. 配送会社システムは配送準備、配送中、配送完了などの状態を時間差で通知する。
11. Hoge社システムは最初の `配送受付済` を受信した時点で引当済在庫を出荷確定へ更新する。
12. Hoge社システムは配送状態を反映し、その都度 Foo社システムへ配送状態を返却する。

## 5. 関連資料
- [../../自社内部設計/業務設計/詳細業務フロー/01_Foo受注から配送結果返却詳細業務フロー.md](../../自社内部設計/業務設計/詳細業務フロー/01_Foo受注から配送結果返却詳細業務フロー.md)
