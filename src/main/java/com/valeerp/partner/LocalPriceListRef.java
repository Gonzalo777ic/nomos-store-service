package com.valeerp.partner;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Proyección local de lista de precios (solo ids/metadatos). Sin Feign.
 */
@Entity
@Table(name = "partner_price_list_refs")
public class LocalPriceListRef extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Id publicado por Catálogo cuando exista; opcional en demos locales.
     */
    @Column(name = "catalog_price_list_id")
    private Long catalogPriceListId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    protected LocalPriceListRef() {
    }

    public static LocalPriceListRef create(Long catalogPriceListId, String code, String name) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de lista de precios es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de lista de precios es obligatorio");
        }
        LocalPriceListRef ref = new LocalPriceListRef();
        ref.catalogPriceListId = catalogPriceListId;
        ref.code = code.trim().toUpperCase();
        ref.name = name.trim();
        return ref;
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de lista de precios es obligatorio");
        }
        this.name = name.trim();
    }

    public void bindCatalogId(Long catalogPriceListId) {
        this.catalogPriceListId = catalogPriceListId;
    }

    public Long getId() {
        return id;
    }

    public Long getCatalogPriceListId() {
        return catalogPriceListId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LocalPriceListRef ref)) {
            return false;
        }
        return id != null && id.equals(ref.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
