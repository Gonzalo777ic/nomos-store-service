package com.valeerp.treasury;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/treasury/cash-movements")
public class TreasuryCashMovementController {

    private final TreasuryCashMovementService cashMovementService;

    public TreasuryCashMovementController(TreasuryCashMovementService cashMovementService) {
        this.cashMovementService = cashMovementService;
    }

    @GetMapping
    public List<CashMovementResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long sessionId) {
        if (sessionId != null) {
            return cashMovementService.listBySession(sessionId).stream()
                    .map(CashMovementResponse::from)
                    .toList();
        }
        return cashMovementService.listByDate(startDate, endDate).stream()
                .map(CashMovementResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CashMovementResponse get(@PathVariable Long id) {
        return CashMovementResponse.from(cashMovementService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashMovementResponse create(@RequestBody CashMovementCreateRequest request) {
        return CashMovementResponse.from(cashMovementService.createManual(request));
    }

    @PostMapping("/{id}/void")
    public CashMovementResponse voidMovement(@PathVariable Long id, @RequestBody CashMovementVoidRequest request) {
        return CashMovementResponse.from(cashMovementService.voidMovement(id, request.reason()));
    }
}
