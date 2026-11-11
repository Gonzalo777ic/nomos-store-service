package com.valeerp.treasury;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Línea del lote de pago masivo. Guarda ids locales de partner y documento por pagar (sin Feign).
 */
@Entity
@Table(name = "mass_vendor_payment_lines")
public class MassVendorPaymentLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private MassVendorPayment batch;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "payable_document_id")
    private Long payableDocumentId;

    @Column(nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(length = TreasuryPolicy.CONCEPT_MAX_LENGTH)
    private String concept;

    protected MassVendorPaymentLine() {
    }

    static MassVendorPaymentLine create(MassVendorPayment batch, Long partnerId, Long payableDocumentId,
                                        Money amount, String concept) {
        if (batch == null) {
            throw new DomainException("La línea exige el lote");
        }
        MassVendorPaymentLine line = new MassVendorPaymentLine();
        line.batch = batch;
        line.partnerId = partnerId;
        line.payableDocumentId = payableDocumentId;
        line.amount = amount.amount();
        line.currencyCode = amount.currency();
        line.concept = blankToNull(concept);
        return line;
    }

    public Long getId() {
        return id;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getPayableDocumentId() {
        return payableDocumentId;
    }

    public Money amount() {
        return Money.of(amount, currencyCode);
    }

    public String getConcept() {
        return concept;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > TreasuryPolicy.CONCEPT_MAX_LENGTH) {
            throw new DomainException("El concepto de línea excede la longitud permitida");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MassVendorPaymentLine line)) {
            return false;
        }
        return id != null && id.equals(line.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
