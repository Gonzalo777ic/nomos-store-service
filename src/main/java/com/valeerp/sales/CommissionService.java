package com.valeerp.sales;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommissionService {

    private final CommissionRuleRepository ruleRepository;
    private final SalesCommissionRepository commissionRepository;
    private final TimeProvider timeProvider;

    public CommissionService(CommissionRuleRepository ruleRepository,
                             SalesCommissionRepository commissionRepository,
                             TimeProvider timeProvider) {
        this.ruleRepository = ruleRepository;
        this.commissionRepository = commissionRepository;
        this.timeProvider = timeProvider;
    }

    public List<CommissionRule> listRules() {
        return ruleRepository.findAll();
    }

    public CommissionRule requireRule(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Regla de comisión no encontrada"));
    }

    @Transactional
    public CommissionRule createRule(CommissionRuleRequest request) {
        ruleRepository.findByCode(request.code() == null ? "" : request.code().trim().toUpperCase())
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe una regla con código " + existing.getCode());
                });
        return ruleRepository.save(CommissionRule.create(
                request.code(), request.name(), request.percentRate(), request.description()));
    }

    @Transactional
    public CommissionRule updateRule(Long id, CommissionRuleRequest request) {
        CommissionRule rule = requireRule(id);
        rule.rename(request.code(), request.name(), request.percentRate(), request.description());
        return ruleRepository.save(rule);
    }

    @Transactional
    public void retireRule(Long id) {
        CommissionRule rule = requireRule(id);
        rule.retire(Actors.currentActor(), timeProvider.now());
        ruleRepository.save(rule);
    }

    @Transactional
    public SalesCommission accrueForSale(Long saleId, Long sellerId, Long ruleId, Money base) {
        CommissionRule rule = requireRule(ruleId);
        SalesCommission commission = SalesCommission.accrue(saleId, null, sellerId, rule, base, timeProvider.now());
        return commissionRepository.save(commission);
    }

    @Transactional
    public SalesCommission accrueForSaleOrder(Long saleOrderId, Long sellerId, Long ruleId, Money base) {
        CommissionRule rule = requireRule(ruleId);
        SalesCommission commission = SalesCommission.accrue(null, saleOrderId, sellerId, rule, base, timeProvider.now());
        return commissionRepository.save(commission);
    }

    @Transactional
    public SalesCommission cancel(Long id) {
        SalesCommission commission = commissionRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Comisión no encontrada"));
        commission.cancel(timeProvider.now());
        return commissionRepository.save(commission);
    }

    public List<SalesCommission> listBySeller(Long sellerId) {
        return commissionRepository.findBySellerId(sellerId);
    }

    public List<SalesCommission> listBySale(Long saleId) {
        return commissionRepository.findBySaleId(saleId);
    }

    public List<SalesCommission> listBySaleOrder(Long saleOrderId) {
        return commissionRepository.findBySaleOrderId(saleOrderId);
    }
}
