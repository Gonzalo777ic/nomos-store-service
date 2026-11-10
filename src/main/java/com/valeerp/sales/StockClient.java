package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class StockClient {

    private final RestClient client;
    private final String token;

    public StockClient(@Value("${valeerp.inventory.base-url}") String baseUrl,
                       @Value("${valeerp.integration.token}") String token) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.token = token;
    }

    public void issue(Long referenceId, String reason, List<StockLine> lines) {
        post("/api/integration/stock/issue", new StockCommand(referenceId, "SALES", reason, "SALIDA_VENTA", lines));
    }

    public void restock(Long referenceId, String reason, List<StockLine> lines) {
        post("/api/integration/stock/issue", new StockCommand(referenceId, "RETURNS", reason, "AJUSTE_DEVOLUCION", lines));
    }

    public void reverse(Long referenceId, String referenceService, String reason) {
        post("/api/integration/stock/reverse", new StockCommand(referenceId, referenceService, reason, null, List.of()));
    }

    private void post(String path, StockCommand command) {
        try {
            client.post()
                    .uri(path)
                    .header("X-ValeERP-Integration", token)
                    .body(command)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new DomainException("Inventario rechazó el movimiento: " + exception.getMessage());
        }
    }

    public record StockLine(Long productId, int quantity) {
    }

    public record StockCommand(Long referenceId, String referenceService, String reason, String type, List<StockLine> lines) {
    }
}
