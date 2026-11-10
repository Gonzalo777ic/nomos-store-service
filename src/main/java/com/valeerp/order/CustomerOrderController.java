package com.valeerp.order;

import com.nomos.store.service.model.TaxRate;
import com.nomos.store.service.repository.TaxRateRepository;
import com.valeerp.customer.CustomerService;
import com.valeerp.kernel.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final CustomerOrderService orderService;
    private final CustomerService customerService;
    private final TaxRateRepository taxRateRepository;
    private final String defaultTaxName;

    public CustomerOrderController(CustomerOrderService orderService,
                                   CustomerService customerService,
                                   TaxRateRepository taxRateRepository,
                                   @Value("${valeerp.orders.default-tax-name}") String defaultTaxName) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.taxRateRepository = taxRateRepository;
        this.defaultTaxName = defaultTaxName;
    }

    @GetMapping
    public List<OrderView> mine(@AuthenticationPrincipal Jwt jwt) {
        return customerService.findByAuth(jwt.getSubject()).stream()
                .flatMap(customer -> orderService.findForCustomer(customer.getId()).stream())
                .map(OrderView::from)
                .toList();
    }

    @PostMapping
    public OrderView place(@AuthenticationPrincipal Jwt jwt, @RequestBody PlaceOrder request) {
        Long taxRateId = taxRateRepository.findByName(defaultTaxName).stream().findFirst()
                .map(TaxRate::getId)
                .orElseThrow(() -> new DomainException("Configura la tasa " + defaultTaxName));
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getSubject();
        }
        String name = request.fullName();
        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("name");
        }
        CustomerOrder order = orderService.place(jwt.getSubject(), email, name, request.address(), request.items(), taxRateId);
        return OrderView.from(order);
    }

    public record PlaceOrder(List<CustomerOrderService.RequestedLine> items, String fullName, String address) {
    }

    public record OrderView(Long id, BigDecimal total, Instant createdAt, String status) {
        static OrderView from(CustomerOrder order) {
            return new OrderView(order.getId(), order.getTotal(), order.getCreatedAt(), order.getStatus().name());
        }
    }
}
