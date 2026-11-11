package com.valeerp.pos;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pos/tickets")
public class PosTicketController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PosTicketService ticketService;

    public PosTicketController(PosTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public PageResponse<PosTicketResponse> list(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long posConfigId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        PosTicketStatus ticketStatus = parseStatus(status);
        return PageResponse.from(ticketService.list(companyId, posConfigId, ticketStatus, pageable)
                .map(PosTicketResponse::from));
    }

    @GetMapping("/{id}")
    public PosTicketResponse get(@PathVariable Long id) {
        PosTicket ticket = ticketService.require(id);
        return PosTicketResponse.from(ticket, ticketService.reversalsOf(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PosTicketResponse open(@RequestBody PosTicketOpenRequest request) {
        return PosTicketResponse.from(ticketService.open(request));
    }

    @PostMapping("/{id}/lines")
    public PosTicketResponse addLine(@PathVariable Long id, @RequestBody PosTicketAddLineRequest request) {
        return PosTicketResponse.from(ticketService.addLine(id, request));
    }

    @PostMapping("/{id}/payments")
    public PosTicketResponse addPayment(@PathVariable Long id, @RequestBody PosTicketAddPaymentRequest request) {
        return PosTicketResponse.from(ticketService.addPayment(id, request));
    }

    @PostMapping("/{id}/post")
    public PosTicketResponse post(@PathVariable Long id) {
        return PosTicketResponse.from(ticketService.post(id));
    }

    @PostMapping("/{id}/void")
    public PosTicketResponse voidTicket(@PathVariable Long id, @RequestBody PosTicketVoidRequest request) {
        PosTicket ticket = ticketService.voidTicket(id, request);
        return PosTicketResponse.from(ticket, ticketService.reversalsOf(id));
    }

    private static PosTicketStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PosTicketStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de ticket inválido: " + status);
        }
    }
}
