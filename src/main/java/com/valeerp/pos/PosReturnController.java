package com.valeerp.pos;

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

import java.util.List;

@RestController
@RequestMapping("/api/pos/returns")
public class PosReturnController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PosReturnService returnService;

    public PosReturnController(PosReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping
    public PageResponse<PosReturnResponse> list(
            @RequestParam(required = false) Long ticketId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(returnService.list(ticketId, pageable).map(PosReturnResponse::from));
    }

    @GetMapping("/by-ticket/{ticketId}")
    public List<PosReturnResponse> byTicket(@PathVariable Long ticketId) {
        return returnService.byTicket(ticketId).stream().map(PosReturnResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PosReturnResponse get(@PathVariable Long id) {
        return PosReturnResponse.from(returnService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PosReturnResponse create(@RequestBody PosReturnCreateRequest request) {
        return PosReturnResponse.from(returnService.createDraft(request));
    }

    @PostMapping("/{id}/confirm")
    public PosReturnResponse confirm(@PathVariable Long id) {
        return PosReturnResponse.from(returnService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    public PosReturnResponse cancel(@PathVariable Long id) {
        return PosReturnResponse.from(returnService.cancel(id));
    }
}
