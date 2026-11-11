package com.valeerp.crm;

import com.valeerp.kernel.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CrmActivityTest {

    @Test
    void completesAndCancelsWithoutPhysicalDelete() {
        Instant now = Instant.parse("2026-11-10T15:00:00Z");
        CrmActivity call = CrmActivity.schedule(
                42L, ActivityType.CALL, "Llamar al contacto", null, now, now
        );

        call.complete(now.plusSeconds(60), "Contestó");
        assertEquals(ActivityStatus.COMPLETED, call.getStatus());

        CrmActivity meeting = CrmActivity.schedule(
                42L, ActivityType.MEETING, "Demo", null, now, now
        );
        meeting.cancel(now.plusSeconds(30), "Cliente reagenda");
        assertEquals(ActivityStatus.CANCELLED, meeting.getStatus());
        assertEquals("Cliente reagenda", meeting.getCancelReason());
    }

    @Test
    void rejectsMutationsWhenClosed() {
        Instant now = Instant.parse("2026-11-10T15:00:00Z");
        CrmActivity email = CrmActivity.schedule(
                7L, ActivityType.EMAIL, "Enviar propuesta", null, now, now
        );
        email.complete(now, null);

        assertThrows(DomainException.class, () -> email.cancel(now, "tarde"));
    }
}
