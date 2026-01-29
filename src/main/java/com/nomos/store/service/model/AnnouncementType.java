package com.nomos.store.service.model;

public enum AnnouncementType {
    BANNER("Banner Informativo"),
    POPUP("Ventana Emergente"),
    SYSTEM("Aviso de Sistema");

    private final String displayValue;

    AnnouncementType(String displayValue) {
        this.displayValue = displayValue;
    }
}