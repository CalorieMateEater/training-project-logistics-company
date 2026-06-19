package jp.co.hoge.customerregistry.config;

import jp.co.hoge.customerregistry.entity.CustomerMasterEntity;
import jp.co.hoge.customerregistry.repository.CustomerMasterRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 顧客マスタ初期データ投入設定。
 *
 * @author Takuya Yamamoto
 */
@Configuration
public class CustomerSeedData {
    /**
     * 顧客マスタ初期データ投入 Runner を生成する。
     *
     * @param repository 顧客マスタ参照先
     * @return 初期データ投入 Runner
     */
    @Bean
    ApplicationRunner seedCustomers(CustomerMasterRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            repository.save(customer("C00000000001", "ACTIVE", "GOLD"));
            repository.save(customer("C00000000002", "ACTIVE", "SILVER"));
            repository.save(customer("C00000000003", "SUSPENDED", "NORMAL"));
            repository.save(customer("C00000000004", "ACTIVE", "PLATINUM"));
        };
    }

    private CustomerMasterEntity customer(String customerId, String status, String memberRank) {
        CustomerMasterEntity entity = new CustomerMasterEntity();
        entity.setCustomerId(customerId);
        entity.setStatus(status);
        entity.setMemberRank(memberRank);
        return entity;
    }
}
