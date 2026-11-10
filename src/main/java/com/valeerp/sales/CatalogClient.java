package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CatalogClient {

    private final RestClient client;
    private final String token;

    public CatalogClient(@Value("${valeerp.catalog.base-url}") String baseUrl,
                         @Value("${valeerp.integration.token}") String token) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.token = token;
    }

    public CatalogPrice get(Long productId) {
        try {
            CatalogPrice price = client.get()
                    .uri("/api/integration/catalog/products/{id}", productId)
                    .header("X-ValeERP-Integration", token)
                    .retrieve()
                    .body(CatalogPrice.class);
            if (price == null || price.price() == null) {
                throw new DomainException("El catálogo no devolvió precio para el producto " + productId);
            }
            return price;
        } catch (DomainException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new DomainException("No se pudo consultar el catálogo: " + exception.getMessage());
        }
    }

    public record CatalogPrice(Long id, String name, BigDecimal price, String imageUrl, String category) {
    }
}
