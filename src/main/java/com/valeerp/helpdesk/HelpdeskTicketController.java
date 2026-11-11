package com.valeerp.helpdesk;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/tickets")
public class HelpdeskTicketController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final HelpdeskTicketService ticketService;

    public HelpdeskTicketController(HelpdeskTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long assigneeUserId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        TicketStatus ticketStatus = HelpdeskTicketService.requireStatus(status);
        return PageResponse.from(ticketService
                .list(ticketStatus, partnerId, assigneeUserId, pageable)
                .map(ticketService::toResponse));
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable Long id) {
        return ticketService.toResponse(ticketService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@RequestBody TicketCreateRequest request) {
        return ticketService.toResponse(ticketService.create(request));
    }

    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable Long id, @RequestBody TicketUpdateRequest request) {
        return ticketService.toResponse(ticketService.update(id, request));
    }

    @PostMapping("/{id}/assign")
    public TicketResponse assign(@PathVariable Long id, @RequestBody TicketAssignRequest request) {
        return ticketService.toResponse(ticketService.assign(id, request));
    }

    @PostMapping("/{id}/start")
    public TicketResponse start(@PathVariable Long id) {
        return ticketService.toResponse(ticketService.startProgress(id));
    }

    @PostMapping("/{id}/wait")
    public TicketResponse waitForCustomer(@PathVariable Long id) {
        return ticketService.toResponse(ticketService.waitForCustomer(id));
    }

    @PostMapping("/{id}/resolve")
    public TicketResponse resolve(@PathVariable Long id) {
        return ticketService.toResponse(ticketService.resolve(id));
    }

    @PostMapping("/{id}/close")
    public TicketResponse close(@PathVariable Long id) {
        return ticketService.toResponse(ticketService.close(id));
    }

    @PostMapping("/{id}/cancel")
    public TicketResponse cancel(@PathVariable Long id) {
        return ticketService.toResponse(ticketService.cancel(id));
    }

    @PostMapping("/{id}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketAttachmentResponse attach(@PathVariable Long id, @RequestBody TicketAttachmentRequest request) {
        return TicketAttachmentResponse.from(ticketService.attach(id, request));
    }

    @PostMapping("/{id}/attachments/{attachmentId}/retire")
    public ResponseEntity<Void> retireAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        ticketService.retireAttachment(id, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statuses")
    public List<CatalogItem> statuses() {
        return Arrays.stream(TicketStatus.values())
                .map(status -> new CatalogItem(status.name(), status.name()))
                .toList();
    }

    @GetMapping("/priorities")
    public List<CatalogItem> priorities() {
        return Arrays.stream(TicketPriority.values())
                .map(priority -> new CatalogItem(priority.name(), priority.name()))
                .toList();
    }

    public record CatalogItem(String key, String label) {
    }
}
