package jp.co.hoge.customerregistry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 顧客マスタを保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_customer_master")
public class CustomerMasterEntity {

    /** 顧客 ID。 */
    @Id
    @Column(name = "customer_id", nullable = false, length = 32)
    private String customerId;

    /** 顧客状態。 */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /** 会員ランク。 */
    @Column(name = "member_rank", nullable = false, length = 16)
    private String memberRank;

    /**
     * 顧客 ID を返却する。
     *
     * @return 顧客 ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * 顧客 ID を設定する。
     *
     * @param customerId 顧客 ID
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * 顧客状態を返却する。
     *
     * @return 顧客状態
     */
    public String getStatus() {
        return status;
    }

    /**
     * 顧客状態を設定する。
     *
     * @param status 顧客状態
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 会員ランクを返却する。
     *
     * @return 会員ランク
     */
    public String getMemberRank() {
        return memberRank;
    }

    /**
     * 会員ランクを設定する。
     *
     * @param memberRank 会員ランク
     */
    public void setMemberRank(String memberRank) {
        this.memberRank = memberRank;
    }
}
