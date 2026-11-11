package com.valeerp.accounting;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/accounting/reports")
public class AccountingReportController {

    private final AccountingReportService reportService;

    public AccountingReportController(AccountingReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/trial-balance")
    public TrialBalanceResponse trialBalance(
            @RequestParam Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return reportService.trialBalance(companyId, fromDate, toDate);
    }

    @GetMapping("/general-ledger")
    public GeneralLedgerResponse generalLedger(
            @RequestParam Long companyId,
            @RequestParam String accountCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return reportService.generalLedger(companyId, accountCode, fromDate, toDate);
    }

    @GetMapping("/balance-sheet")
    public BalanceSheetResponse balanceSheet(
            @RequestParam Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return reportService.balanceSheet(companyId, asOf);
    }
}
