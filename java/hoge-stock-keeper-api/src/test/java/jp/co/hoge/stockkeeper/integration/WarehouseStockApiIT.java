package jp.co.hoge.stockkeeper.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class WarehouseStockApiIT {
  @Autowired private MockMvc mockMvc;

  @Test
  void shouldSearchWarehouseStocksByAssignedScope() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/stocks/inventories")
                .header("Authorization", "Bearer employee:EMP-WH-TYO-001")
                .param("warehouse_location_code", "WH-TYO-01"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.warehouseLocationCode").value("WH-TYO-01"))
        .andExpect(jsonPath("$.stocks[0].warehouseLocationCode").value("WH-TYO-01"));
  }

  @Test
  void shouldRejectWarehouseStocksByUnassignedScope() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/stocks/inventories")
                .header("Authorization", "Bearer employee:EMP-WH-TYO-001")
                .param("warehouse_location_code", "WH-OSA-01"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldRegisterStockReceiptAndReturnUpdatedBalance() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/stocks/receipts")
                .header("Authorization", "Bearer employee:EMP-WH-TYO-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "warehouseLocationCode": "WH-TYO-01",
                      "itemCode": "ITM0000001",
                      "receivedQuantity": 5,
                      "receiptReferenceNo": "RCPT-202606270001"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.warehouseLocationCode").value("WH-TYO-01"))
        .andExpect(jsonPath("$.itemCode").value("ITM0000001"))
        .andExpect(jsonPath("$.receivedQuantity").value(5))
        .andExpect(jsonPath("$.onHandQuantity").value(65))
        .andExpect(jsonPath("$.availableQuantity").exists());
  }

  @Test
  void shouldTreatSameReceiptRequestAsIdempotentSuccess() throws Exception {
    String requestBody =
        """
        {
          "warehouseLocationCode": "WH-TYO-01",
          "itemCode": "ITM0000002",
          "receivedQuantity": 3,
          "receiptReferenceNo": "RCPT-202606270002"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/stocks/receipts")
                .header("Authorization", "Bearer employee:EMP-WH-TYO-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/stocks/receipts")
                .header("Authorization", "Bearer employee:EMP-WH-TYO-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.receiptReferenceNo").value("RCPT-202606270002"))
        .andExpect(jsonPath("$.receivedQuantity").value(3));
  }
}
