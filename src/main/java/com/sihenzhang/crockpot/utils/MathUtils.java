package com.sihenzhang.crockpot.utils;

public final class MathUtils {
    public static boolean fuzzyEquals(final double a, final double b) {
        return Math.abs(a - b) * 1000000000000.0D <= Math.min(Math.abs(a), Math.abs(b));
    }

    public static boolean fuzzyEquals(final float a, final float b) {
        return Math.abs(a - b) * 100000.0F <= Math.min(Math.abs(a), Math.abs(b));
    }

    public static boolean fuzzyIsZero(final double d) {
        return Math.abs(d) <= 0.000000000001D;
    }

    public static boolean fuzzyIsZero(final float f) {
        return Math.abs(f) <= 0.00001F;
    }
}
