package jp.co.hoge.customerregistry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import jp.co.hoge.customerregistry.entity.CustomerMasterEntity;
import jp.co.hoge.customerregistry.repository.CustomerMasterRepository;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CustomerStatusServiceTest {
    @Mock
    private CustomerMasterRepository customerMasterRepository;

    @InjectMocks
    private CustomerStatusService customerStatusService;

    @Test
    void shouldReturnCustomerStatus() {
        CustomerMasterEntity entity = new CustomerMasterEntity();
        entity.setCustomerId("C00000000001");
        entity.setStatus("ACTIVE");
        entity.setMemberRank("GOLD");
        when(customerMasterRepository.findById("C00000000001")).thenReturn(Optional.of(entity));

        CustomerStatusResponse response = customerStatusService.findStatus("C00000000001");

        assertThat(response.customerId()).isEqualTo("C00000000001");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldThrowWhenCustomerMissing() {
        when(customerMasterRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerStatusService.findStatus("UNKNOWN"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
