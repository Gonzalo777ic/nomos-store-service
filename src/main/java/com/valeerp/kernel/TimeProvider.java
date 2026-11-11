package com.valeerp.kernel;

import java.time.Instant;

@FunctionalInterface
public interface TimeProvider {

    Instant now();
}
