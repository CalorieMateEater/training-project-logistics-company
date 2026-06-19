package jp.co.hoge.customerregistry.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jp.co.hoge.customerregistry.CustomerRegistryApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = CustomerRegistryApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerStatusApiIT {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnSeededCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/customers/C00000000001/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C00000000001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.memberRank").value("GOLD"));
    }
}
