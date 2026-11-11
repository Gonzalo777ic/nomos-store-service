package com.valeerp.partner;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos literales de demo local (fixtures). No van en controladores.
 */
final class PartnerFixtureData {

    record CategorySeed(String code, String name, String description) {
    }

    record TermSeed(String code, String name, int daysDue, String description) {
    }

    record PriceListSeed(Long catalogId, String code, String name) {
    }

    record AddressSeed(AddressKind kind, String line, String city, String region,
                       String postalCode, String country, boolean primary) {
    }

    record ContactSeed(String fullName, String email, String phone, String roleLabel) {
    }

    record BankSeed(String bankCode, String bankName, String accountNumber, String cci,
                    String currency, String holderName, BankAccountPurpose purpose, boolean primary) {
    }

    record PartnerSeed(
            PartnerType type,
            String name,
            String tradeName,
            DocumentType documentType,
            String documentBase,
            String email,
            String phone,
            String categoryCode,
            String priceListCode,
            String paymentTermCode,
            BigDecimal creditLimit,
            List<String> tags,
            List<AddressSeed> addresses,
            List<ContactSeed> contacts,
            List<BankSeed> banks
    ) {
    }

    static final List<CategorySeed> CATEGORIES = List.of(
            new CategorySeed("VIP", "Clientes VIP", "Alto volumen y prioridad comercial"),
            new CategorySeed("MAYORISTA", "Mayoristas", "Descuentos por volumen"),
            new CategorySeed("MINORISTA", "Minoristas", "Canal retail / mostrador"),
            new CategorySeed("PROVEEDOR-LOCAL", "Proveedores locales", "Abastecimiento nacional")
    );

    static final List<TermSeed> TERMS = List.of(
            new TermSeed("CONTADO", "Contado", 0, "Pago al confirmar"),
            new TermSeed("NET15", "Crédito 15 días", 15, "Vence a 15 días"),
            new TermSeed("NET30", "Crédito 30 días", 30, "Vence a 30 días"),
            new TermSeed("NET45", "Crédito 45 días", 45, "Vence a 45 días")
    );

    static final List<PriceListSeed> PRICE_LISTS = List.of(
            new PriceListSeed(101L, "ESTANDAR", "Lista estándar"),
            new PriceListSeed(102L, "MAYORISTA", "Lista mayorista")
    );

    static List<PartnerSeed> partners() {
        return List.of(
                // Personas naturales (cliente)
                new PartnerSeed(
                        PartnerType.CUSTOMER, "Ana María Quispe", null, DocumentType.DNI, "45678901",
                        "ana.quispe@demo.local", "999111222", "MINORISTA", "ESTANDAR", "CONTADO",
                        null, List.of("persona", "retail"),
                        List.of(new AddressSeed(AddressKind.BILLING, "Av. Arequipa 1200", "Lima", "Lima", "15036", "PE", true)),
                        List.of(), List.of()
                ),
                new PartnerSeed(
                        PartnerType.CUSTOMER, "Carlos Mendoza Ríos", null, DocumentType.DNI, "40123456",
                        "carlos.mendoza@demo.local", "988777666", "VIP", "ESTANDAR", "NET15",
                        new BigDecimal("2500.00"), List.of("persona", "vip"),
                        List.of(new AddressSeed(AddressKind.SHIPPING, "Calle Los Pinos 45", "Arequipa", "Arequipa", "04001", "PE", true)),
                        List.of(), List.of()
                ),
                // Empresa cliente
                new PartnerSeed(
                        PartnerType.CUSTOMER, "Comercial Andina SAC", "Andina Market", DocumentType.RUC, "2010006660",
                        "ventas@andina-demo.local", "014445566", "MAYORISTA", "MAYORISTA", "NET30",
                        new BigDecimal("50000.00"), List.of("empresa", "mayorista"),
                        List.of(
                                new AddressSeed(AddressKind.BILLING, "Jr. Lampa 500", "Lima", "Lima", "15001", "PE", true),
                                new AddressSeed(AddressKind.SHIPPING, "Almacén Callao Mz A", "Callao", "Callao", "07001", "PE", false)
                        ),
                        List.of(new ContactSeed("Lucía Torres", "lucia@andina-demo.local", "999000111", "Compras")),
                        List.of(new BankSeed("002", "BCP", "19145678901234", "00219100145678901234",
                                PartnerCommercePolicy.DEFAULT_CURRENCY, "Comercial Andina SAC",
                                BankAccountPurpose.COLLECTION, true))
                ),
                // Empresa proveedor
                new PartnerSeed(
                        PartnerType.SUPPLIER, "Insumos del Norte EIRL", "Insumos Norte", DocumentType.RUC, "2060123456",
                        "proveedor@norte-demo.local", "074221100", "PROVEEDOR-LOCAL", "ESTANDAR", "NET45",
                        new BigDecimal("80000.00"), List.of("empresa", "proveedor"),
                        List.of(new AddressSeed(AddressKind.OTHER, "Av. España 890", "Trujillo", "La Libertad", "13001", "PE", true)),
                        List.of(new ContactSeed("Pedro Vargas", "pedro@norte-demo.local", "944112233", "Cuentas por cobrar")),
                        List.of(
                                new BankSeed("011", "BBVA", "001101230012345678", "01100110123001234567",
                                        PartnerCommercePolicy.DEFAULT_CURRENCY, "Insumos del Norte EIRL",
                                        BankAccountPurpose.PAYMENT, true),
                                new BankSeed("003", "Interbank", "2003001234567", "00320030012345678901",
                                        PartnerCommercePolicy.DEFAULT_CURRENCY, "Insumos del Norte EIRL",
                                        BankAccountPurpose.BOTH, false)
                        )
                ),
                // Contacto persona (mixto: contacto ligado a empresa vía tags)
                new PartnerSeed(
                        PartnerType.CONTACT, "María Elena Soto", null, DocumentType.DNI, "47890123",
                        "maria.soto@demo.local", "955443322", null, null, null,
                        null, List.of("persona", "contacto-andina"),
                        List.of(), List.of(), List.of()
                ),
                // Empleado comercial (ficha partner ≠ User auth)
                new PartnerSeed(
                        PartnerType.EMPLOYEE, "José Luis Paredes", null, DocumentType.DNI, "43456789",
                        "jose.paredes@valeerp-demo.local", "977665544", null, null, "CONTADO",
                        null, List.of("persona", "empleado"),
                        List.of(new AddressSeed(AddressKind.OTHER, "Jr. Cusco 210", "Lima", "Lima", "15001", "PE", true)),
                        List.of(), List.of()
                ),
                // Mixto: empresa pequeña con RUC y lista estándar
                new PartnerSeed(
                        PartnerType.CUSTOMER, "Panadería El Trigal SAC", "El Trigal", DocumentType.RUC, "2055566677",
                        "hola@trigal-demo.local", "015556677", "MINORISTA", "ESTANDAR", "NET15",
                        new BigDecimal("8000.00"), List.of("empresa", "mixto-retail"),
                        List.of(new AddressSeed(AddressKind.BILLING, "Av. Brasil 1500", "Lima", "Lima", "15072", "PE", true)),
                        List.of(new ContactSeed("Rosa Trigal", "rosa@trigal-demo.local", "912345678", "Gerencia")),
                        List.of(new BankSeed("009", "Scotiabank", "000123456789", "00900012345678901234",
                                PartnerCommercePolicy.DEFAULT_CURRENCY, "Panadería El Trigal SAC",
                                BankAccountPurpose.COLLECTION, true))
                ),
                // Mixto: proveedor persona natural con RUC 10
                new PartnerSeed(
                        PartnerType.SUPPLIER, "Luis Alberto Chávez", "Servicios Chávez", DocumentType.RUC, "1045678901",
                        "luis.chavez@demo.local", "989898989", "PROVEEDOR-LOCAL", "ESTANDAR", "CONTADO",
                        new BigDecimal("3000.00"), List.of("persona", "proveedor-mixto"),
                        List.of(new AddressSeed(AddressKind.OTHER, "Psje. Las Flores 12", "Cusco", "Cusco", "08000", "PE", true)),
                        List.of(),
                        List.of(new BankSeed("002", "BCP", "19199887766554", "00219100199887766554",
                                PartnerCommercePolicy.DEFAULT_CURRENCY, "Luis Alberto Chávez",
                                BankAccountPurpose.PAYMENT, true))
                )
        );
    }

    private PartnerFixtureData() {
    }

    static String completeRuc(String firstTenDigits) {
        int check = RucChecksum.expectedCheckDigit(firstTenDigits);
        return firstTenDigits + check;
    }
}
