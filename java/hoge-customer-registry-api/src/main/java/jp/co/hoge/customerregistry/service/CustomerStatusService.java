package jp.co.hoge.customerregistry.service;

import jp.co.hoge.customerregistry.entity.CustomerMasterEntity;
import jp.co.hoge.customerregistry.repository.CustomerMasterRepository;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * 顧客状態照会処理を実行するサービス。
 *
 * @author Takuya Yamamoto
 */
@Service
@RequiredArgsConstructor
public class CustomerStatusService {
    /** 顧客マスタ参照先。 */
    private final CustomerMasterRepository customerMasterRepository;

    /**
     * 顧客 ID から顧客状態を取得する。
     *
     * @param customerId 顧客 ID
     * @return 顧客状態
     */
    public CustomerStatusResponse findStatus(String customerId) {
        CustomerMasterEntity customer = customerMasterRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));

        return new CustomerStatusResponse(customer.getCustomerId(), customer.getStatus(), customer.getMemberRank());
    }
}
