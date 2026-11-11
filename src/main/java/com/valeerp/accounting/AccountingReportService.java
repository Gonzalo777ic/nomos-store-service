package com.valeerp.accounting;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes contables por consultas locales (#79). Sin Feign.
 */
@Service
public class AccountingReportService {

    private final JournalEntryRepository entryRepository;

    public AccountingReportService(JournalEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public TrialBalanceResponse trialBalance(Long companyId, LocalDate fromDate, LocalDate toDate) {
        requireRange(companyId, fromDate, toDate);
        List<JournalEntry> entries = entryRepository.findPostedBetween(companyId, fromDate, toDate);
        Map<String, AccountAccumulator> byAccount = accumulate(entries);
        String currency = resolveCurrency(entries);

        List<TrialBalanceLineResponse> lines = new ArrayList<>();
        Money totalDebit = Money.zero(currency);
        Money totalCredit = Money.zero(currency);
        for (AccountAccumulator acc : sorted(byAccount)) {
            Money debit = acc.debit;
            Money credit = acc.credit;
            Money net = debit.minus(credit);
            Money balanceDebit = net.isPositive() || net.isZero() ? net : Money.zero(currency);
            Money balanceCredit = net.isNegative() ? Money.of(net.amount().abs(), currency) : Money.zero(currency);
            lines.add(new TrialBalanceLineResponse(
                    acc.code,
                    acc.name,
                    acc.accountClass.name(),
                    debit.amount(),
                    credit.amount(),
                    balanceDebit.amount(),
                    balanceCredit.amount()
            ));
            totalDebit = totalDebit.plus(debit);
            totalCredit = totalCredit.plus(credit);
        }
        return new TrialBalanceResponse(
                companyId, fromDate, toDate, currency,
                totalDebit.amount(), totalCredit.amount(), lines
        );
    }

    @Transactional(readOnly = true)
    public GeneralLedgerResponse generalLedger(Long companyId, String accountCode,
                                               LocalDate fromDate, LocalDate toDate) {
        requireRange(companyId, fromDate, toDate);
        if (accountCode == null || accountCode.isBlank()) {
            throw new DomainException("El código de cuenta es obligatorio para el mayor");
        }
        String code = accountCode.trim();
        List<JournalEntry> prior = entryRepository.findPostedBetween(
                companyId, LocalDate.of(1900, 1, 1), fromDate.minusDays(1));
        List<JournalEntry> period = entryRepository.findPostedBetween(companyId, fromDate, toDate);

        AccountAccumulator openingAcc = accumulateAccount(prior, code);
        String currency = openingAcc != null
                ? openingAcc.debit.currency()
                : resolveCurrency(period);
        Money opening = openingAcc == null
                ? Money.zero(currency)
                : signedBalance(openingAcc);

        List<GeneralLedgerMovementResponse> movements = new ArrayList<>();
        Money running = opening;
        String accountName = openingAcc == null ? null : openingAcc.name;
        AccountClass accountClass = openingAcc == null ? null : openingAcc.accountClass;

        for (JournalEntry entry : period) {
            for (JournalEntryLine line : entry.getLines()) {
                if (!code.equals(line.getAccountCode())) {
                    continue;
                }
                if (accountName == null) {
                    accountName = line.getAccountName();
                    accountClass = line.getAccountClass();
                    currency = line.getCurrencyCode();
                    if (opening.isZero()) {
                        opening = Money.zero(currency);
                        running = opening;
                    }
                }
                running = running.plus(line.debit()).minus(line.credit());
                movements.add(new GeneralLedgerMovementResponse(
                        entry.getId(),
                        entry.getEntryDate(),
                        entry.getConcept(),
                        entry.getReferenceDocument(),
                        line.debit().amount(),
                        line.credit().amount(),
                        running.amount()
                ));
            }
        }
        if (accountName == null) {
            throw new DomainException("No hay movimientos para la cuenta " + code + " en el rango");
        }
        return new GeneralLedgerResponse(
                companyId,
                code,
                accountName,
                accountClass.name(),
                fromDate,
                toDate,
                currency,
                opening.amount(),
                running.amount(),
                movements
        );
    }

    @Transactional(readOnly = true)
    public BalanceSheetResponse balanceSheet(Long companyId, LocalDate asOf) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        if (asOf == null) {
            throw new DomainException("La fecha de corte es obligatoria");
        }
        List<JournalEntry> entries = entryRepository.findPostedUpTo(companyId, asOf);
        Map<String, AccountAccumulator> byAccount = accumulate(entries);
        String currency = resolveCurrency(entries);

        Map<AccountClass, List<BalanceSheetSectionResponse.BalanceSheetAccountResponse>> grouped =
                new EnumMap<>(AccountClass.class);
        Map<AccountClass, Money> totals = new EnumMap<>(AccountClass.class);
        for (AccountClass type : AccountClass.values()) {
            grouped.put(type, new ArrayList<>());
            totals.put(type, Money.zero(currency));
        }

        for (AccountAccumulator acc : sorted(byAccount)) {
            Money signed = reportBalance(acc);
            if (signed.isZero()) {
                continue;
            }
            grouped.get(acc.accountClass).add(new BalanceSheetSectionResponse.BalanceSheetAccountResponse(
                    acc.code, acc.name, signed.amount()));
            totals.put(acc.accountClass, totals.get(acc.accountClass).plus(signed));
        }

        Money assets = totals.get(AccountClass.ASSET);
        Money liabilities = totals.get(AccountClass.LIABILITY);
        Money equity = totals.get(AccountClass.EQUITY);
        Money income = totals.get(AccountClass.INCOME);
        Money expense = totals.get(AccountClass.EXPENSE);
        Money netIncome = income.minus(expense);
        Money equityWithResult = equity.plus(netIncome);
        Money rightSide = liabilities.plus(equityWithResult);
        boolean balanced = assets.equals(rightSide);

        List<BalanceSheetSectionResponse> sections = List.of(
                section("ASSETS", assets, grouped.get(AccountClass.ASSET)),
                section("LIABILITIES", liabilities, grouped.get(AccountClass.LIABILITY)),
                section("EQUITY", equity, grouped.get(AccountClass.EQUITY)),
                new BalanceSheetSectionResponse(
                        "NET_INCOME",
                        netIncome.amount(),
                        List.of(new BalanceSheetSectionResponse.BalanceSheetAccountResponse(
                                "RESULT", "Resultado del ejercicio", netIncome.amount())))
        );

        return new BalanceSheetResponse(
                companyId,
                asOf,
                currency,
                assets.amount(),
                liabilities.amount(),
                equityWithResult.amount(),
                netIncome.amount(),
                balanced,
                sections
        );
    }

    private static BalanceSheetSectionResponse section(
            String name,
            Money total,
            List<BalanceSheetSectionResponse.BalanceSheetAccountResponse> accounts) {
        return new BalanceSheetSectionResponse(name, total.amount(), accounts);
    }

    private static Map<String, AccountAccumulator> accumulate(List<JournalEntry> entries) {
        Map<String, AccountAccumulator> byAccount = new LinkedHashMap<>();
        for (JournalEntry entry : entries) {
            for (JournalEntryLine line : entry.getLines()) {
                AccountAccumulator acc = byAccount.computeIfAbsent(
                        line.getAccountCode(),
                        code -> new AccountAccumulator(code, line.getAccountName(), line.getAccountClass(),
                                line.getCurrencyCode()));
                acc.debit = acc.debit.plus(line.debit());
                acc.credit = acc.credit.plus(line.credit());
            }
        }
        return byAccount;
    }

    private static AccountAccumulator accumulateAccount(List<JournalEntry> entries, String code) {
        AccountAccumulator acc = null;
        for (JournalEntry entry : entries) {
            for (JournalEntryLine line : entry.getLines()) {
                if (!code.equals(line.getAccountCode())) {
                    continue;
                }
                if (acc == null) {
                    acc = new AccountAccumulator(code, line.getAccountName(), line.getAccountClass(),
                            line.getCurrencyCode());
                }
                acc.debit = acc.debit.plus(line.debit());
                acc.credit = acc.credit.plus(line.credit());
            }
        }
        return acc;
    }

    private static Money signedBalance(AccountAccumulator acc) {
        return acc.debit.minus(acc.credit);
    }

    /**
     * Presentación de balance: activo/gasto en debe neto; pasivo/patrimonio/ingreso en haber neto.
     */
    private static Money reportBalance(AccountAccumulator acc) {
        Money signed = signedBalance(acc);
        return switch (acc.accountClass) {
            case ASSET, EXPENSE -> signed;
            case LIABILITY, EQUITY, INCOME -> Money.of(signed.amount().negate(), signed.currency());
            case OFF_BALANCE -> signed;
        };
    }

    private static List<AccountAccumulator> sorted(Map<String, AccountAccumulator> byAccount) {
        return byAccount.values().stream()
                .sorted(Comparator.comparing(a -> a.code))
                .toList();
    }

    private static String resolveCurrency(List<JournalEntry> entries) {
        return entries.stream()
                .map(JournalEntry::getCurrencyCode)
                .findFirst()
                .orElse(Currency.PEN.code());
    }

    private static void requireRange(Long companyId, LocalDate fromDate, LocalDate toDate) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        if (fromDate == null || toDate == null) {
            throw new DomainException("El rango de fechas es obligatorio");
        }
        if (toDate.isBefore(fromDate)) {
            throw new DomainException("La fecha fin no puede ser anterior al inicio");
        }
    }

    private static final class AccountAccumulator {
        private final String code;
        private final String name;
        private final AccountClass accountClass;
        private Money debit;
        private Money credit;

        private AccountAccumulator(String code, String name, AccountClass accountClass, String currency) {
            this.code = code;
            this.name = name;
            this.accountClass = accountClass;
            this.debit = Money.zero(currency);
            this.credit = Money.zero(currency);
        }
    }
}
