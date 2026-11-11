package com.valeerp.sales;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogPromotionService {

    private final CatalogPromotionRepository promotionRepository;
    private final TimeProvider timeProvider;

    public CatalogPromotionService(CatalogPromotionRepository promotionRepository, TimeProvider timeProvider) {
        this.promotionRepository = promotionRepository;
        this.timeProvider = timeProvider;
    }

    public Page<CatalogPromotion> list(Pageable pageable) {
        return promotionRepository.findAll(pageable);
    }

    public CatalogPromotion require(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Promoción no encontrada"));
    }

    public CatalogPromotion requireActive(Long id) {
        CatalogPromotion promotion = require(id);
        if (!promotion.isActiveAt(timeProvider.now())) {
            throw new DomainException("La promoción no está vigente");
        }
        return promotion;
    }

    @Transactional
    public CatalogPromotion create(CatalogPromotionCreateRequest request) {
        PromotionKind kind = PromotionKind.require(request.kind());
        String currency = resolveCurrency(request.currency());
        ensureCodeAvailable(request.code(), null);
        CatalogPromotion promotion = CatalogPromotion.define(
                request.code(),
                request.name(),
                kind,
                request.discountValue(),
                currency,
                request.startsAt(),
                request.endsAt(),
                timeProvider.now()
        );
        return promotionRepository.save(promotion);
    }

    @Transactional
    public CatalogPromotion update(Long id, CatalogPromotionUpdateRequest request) {
        CatalogPromotion promotion = require(id);
        ensureCodeAvailable(request.code(), id);
        promotion.rename(request.code(), request.name());
        promotion.changeRule(
                PromotionKind.require(request.kind()),
                request.discountValue(),
                resolveCurrency(request.currency())
        );
        promotion.changeWindow(request.startsAt(), request.endsAt());
        return promotionRepository.save(promotion);
    }

    @Transactional
    public void retire(Long id) {
        CatalogPromotion promotion = require(id);
        promotion.archive(Actors.currentActor(), timeProvider.now());
        promotionRepository.save(promotion);
    }

    private void ensureCodeAvailable(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de promoción es obligatorio");
        }
        promotionRepository.findByCode(code.trim().toUpperCase())
                .filter(existing -> excludeId == null || !excludeId.equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe una promoción con código " + code.trim());
                });
    }

    private static String resolveCurrency(String raw) {
        if (raw == null || raw.isBlank()) {
            return SalesPolicy.DEFAULT_CURRENCY;
        }
        return Currency.of(raw).code();
    }
}
