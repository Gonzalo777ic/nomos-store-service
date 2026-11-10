package com.valeerp.order;

import com.nomos.store.service.service.SaleService;
import com.valeerp.customer.Customer;
import com.valeerp.customer.CustomerService;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.sales.CatalogClient;
import com.valeerp.sales.RegisterSale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository orderRepository;
    private final CustomerService customerService;
    private final CatalogClient catalogClient;
    private final SaleService saleService;
    private final String currency;
    private final Long storeSellerId;
    private final String defaultTaxName;

    public CustomerOrderService(CustomerOrderRepository orderRepository,
                                CustomerService customerService,
                                CatalogClient catalogClient,
                                SaleService saleService,
                                @Value("${valeerp.currency}") String currency,
                                @Value("${valeerp.orders.store-seller-id}") Long storeSellerId,
                                @Value("${valeerp.orders.default-tax-name}") String defaultTaxName) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.catalogClient = catalogClient;
        this.saleService = saleService;
        this.currency = currency;
        this.storeSellerId = storeSellerId;
        this.defaultTaxName = defaultTaxName;
    }

    public List<CustomerOrder> findForCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public CustomerOrder place(String auth0Id, String email, String name, String address,
                             List<RequestedLine> lines, Long taxRateId) {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("El pedido no tiene productos");
        }
        Customer customer = customerService.remember(auth0Id, email, name, address);
        CustomerOrder order = CustomerOrder.open(customer.getId(), currency);
        for (RequestedLine requested : lines) {
            CatalogClient.CatalogPrice price = catalogClient.get(requested.productId());
            order.addLine(price.id(), price.name(), requested.quantity(), Money.of(price.price(), currency));
        }
        order.confirm();
        CustomerOrder saved = orderRepository.save(order);
        saleService.createSale(new RegisterSale(
                customer.getId(),
                LocalDateTime.now(),
                null,
                "TICKET",
                "CONTADO",
                0,
                storeSellerId,
                1,
                lines.stream().map(line -> new RegisterSale.Line(line.productId(), line.quantity(), taxRateId, null)).toList()
        ));
        return saved;
    }

    public String defaultTaxName() {
        return defaultTaxName;
    }

    public record RequestedLine(Long productId, int quantity) {
    }
}
