package com.valeerp.receivable;

import com.valeerp.store.service.model.AccountsReceivable;
import com.valeerp.store.service.model.Installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountsReceivableResponse(
        Long id,
        Long saleId,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        String currencyCode,
        String status,
        List<InstallmentResponse> installments
) {

    public record InstallmentResponse(
            Long id,
            Integer number,
            LocalDate dueDate,
            BigDecimal expectedAmount,
            BigDecimal paidAmount,
            BigDecimal pendingAmount,
            String status
    ) {
        static InstallmentResponse from(Installment installment) {
            return new InstallmentResponse(
                    installment.getId(),
                    installment.getNumber(),
                    installment.getDueDate(),
                    installment.getExpectedAmount(),
                    installment.getPaidAmount(),
                    installment.getPendingAmount(),
                    installment.getStatus().name()
            );
        }
    }

    public static AccountsReceivableResponse from(AccountsReceivable ar) {
        return new AccountsReceivableResponse(
                ar.getId(),
                ar.getSale() == null ? null : ar.getSale().getId(),
                ar.getTotalAmount(),
                ar.getPaidAmount(),
                ar.getBalance(),
                ar.getCurrencyCode(),
                ar.getStatus().name(),
                ar.getInstallments().stream().map(InstallmentResponse::from).toList()
        );
    }
}
