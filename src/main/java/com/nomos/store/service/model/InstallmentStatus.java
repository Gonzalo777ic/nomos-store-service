package com.nomos.store.service.model;

public enum InstallmentStatus {
    PENDING,  // A la espera de pago
    PARTIAL,  // Pago incompleto
    PAID,     // Pagado
    OVERDUE   // Vencido (Marcar esto requiere un proceso batch/job diario)
}