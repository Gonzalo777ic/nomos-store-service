package com.valeerp.partner;

/**
 * Roles comerciales del Partner. Independiente del User de identidad.
 */
public enum PartnerType {
    CUSTOMER("Cliente"),
    SUPPLIER("Proveedor"),
    EMPLOYEE("Empleado"),
    CONTACT("Contacto");

    private final String label;

    PartnerType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
