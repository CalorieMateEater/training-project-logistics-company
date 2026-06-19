package jp.co.hoge.stockkeeper.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jp.co.hoge.stockkeeper.StockKeeperApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = StockKeeperApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StockReservationApiIT {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReserveStockByApi() throws Exception {
        mockMvc.perform(post("/api/v1/stocks/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "O202606170001",
                                  "items": [
                                    { "itemCode": "ITM0000001", "quantity": 2 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.results[0].reservedQuantity").value(2));
    }
}
