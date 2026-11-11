package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.util.ArrayList;
import java.util.List;

/**
 * Totales de venta con invariante Money: el subtotal del cliente no participa.
 * Descuento global prorrateado; impuesto incluido por tasa de catálogo fiscal por línea.
 */
public final class SaleTotals {

    private final List<PricedLine> lines;
    private final Money gross;
    private final Money lineAndPromoDiscount;
    private final Money globalDiscount;
    private final Money net;
    private final Money tax;
    private final Money base;

    private SaleTotals(
            List<PricedLine> lines,
            Money gross,
            Money lineAndPromoDiscount,
            Money globalDiscount,
            Money net,
            Money tax,
            Money base
    ) {
        this.lines = List.copyOf(lines);
        this.gross = gross;
        this.lineAndPromoDiscount = lineAndPromoDiscount;
        this.globalDiscount = globalDiscount;
        this.net = net;
        this.tax = tax;
        this.base = base;
    }

    public static SaleTotals compute(List<PricedLine> drafts, Discount globalDiscount) {
        if (drafts == null || drafts.isEmpty()) {
            throw new DomainException("La venta exige al menos una línea valuada");
        }
        Discount global = globalDiscount == null ? Discount.none() : globalDiscount;
        String currency = drafts.get(0).net().currency();

        Money preGlobalNet = Money.zero(currency);
        Money gross = Money.zero(currency);
        Money lineDisc = Money.zero(currency);
        List<Money> weights = new ArrayList<>(drafts.size());
        for (PricedLine draft : drafts) {
            gross = gross.plus(draft.gross());
            lineDisc = lineDisc.plus(draft.promotionDiscount()).plus(draft.lineDiscount());
            preGlobalNet = preGlobalNet.plus(draft.net());
            weights.add(draft.net());
        }

        Money globalAmount = global.applyTo(preGlobalNet);
        if (globalAmount.greaterThan(preGlobalNet)) {
            globalAmount = preGlobalNet;
        }
        List<Money> shares = MoneyAllocator.allocate(globalAmount, weights);

        List<PricedLine> priced = new ArrayList<>(drafts.size());
        Money net = Money.zero(currency);
        Money tax = Money.zero(currency);
        Money base = Money.zero(currency);
        Money appliedGlobal = Money.zero(currency);
        for (int i = 0; i < drafts.size(); i++) {
            PricedLine line = drafts.get(i).withGlobalShare(shares.get(i));
            priced.add(line);
            net = net.plus(line.net());
            tax = tax.plus(line.tax());
            base = base.plus(line.base());
            appliedGlobal = appliedGlobal.plus(line.globalDiscountShare());
        }

        assertInvariant(gross, lineDisc, appliedGlobal, net, tax, base);
        return new SaleTotals(priced, gross, lineDisc, appliedGlobal, net, tax, base);
    }

    private static void assertInvariant(
            Money gross,
            Money lineDisc,
            Money globalDisc,
            Money net,
            Money tax,
            Money base
    ) {
        Money expectedNet = gross.minus(lineDisc).minus(globalDisc);
        if (expectedNet.compareTo(net) != 0) {
            throw new DomainException("Invariante de totales: neto != bruto - descuentos");
        }
        Money expectedNetFromTax = base.plus(tax);
        if (expectedNetFromTax.compareTo(net) != 0) {
            throw new DomainException("Invariante de totales: neto != base + impuesto");
        }
    }

    public List<PricedLine> lines() {
        return lines;
    }

    public Money gross() {
        return gross;
    }

    public Money lineAndPromoDiscount() {
        return lineAndPromoDiscount;
    }

    public Money globalDiscount() {
        return globalDiscount;
    }

    public Money totalDiscount() {
        return lineAndPromoDiscount.plus(globalDiscount);
    }

    public Money net() {
        return net;
    }

    public Money tax() {
        return tax;
    }

    public Money base() {
        return base;
    }
}
