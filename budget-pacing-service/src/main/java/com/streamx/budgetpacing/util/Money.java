package com.streamx.budgetpacing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private static final long MICROS_PER_UNIT = 1_000_000L;

    private Money() {
    }

    public static long toMicros(BigDecimal dollars) {
        return dollars.multiply(BigDecimal.valueOf(MICROS_PER_UNIT))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
    }

    public static BigDecimal toDollars(long micros) {
        return BigDecimal.valueOf(micros, 0)
                .divide(BigDecimal.valueOf(MICROS_PER_UNIT), 6, RoundingMode.UNNECESSARY);
    }
}
