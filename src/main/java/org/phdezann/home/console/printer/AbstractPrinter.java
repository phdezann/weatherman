package org.phdezann.home.console.printer;

import org.apache.commons.lang3.StringUtils;

public class AbstractPrinter {

    protected String leftPad(String str, int width) {
        return leftPad(str, width, "");
    }

    protected String leftPad(String str, int width, String padStr) {
        String abbreviated = abbreviate(str, width);
        return StringUtils.leftPad(abbreviated, width, padStr);
    }

    protected String rightPad(String str, int width) {
        return rightPad(str, width, "");
    }

    protected String rightPad(String str, int width, String padStr) {
        var abbreviated = abbreviate(str, width);
        return StringUtils.rightPad(abbreviated, width, padStr);
    }

    protected String abbreviate(String str, int width) {
        return StringUtils.abbreviate(str, ".", width);
    }

    protected void checkLength(String line) {
        if (line.length() != 21) {
            throw new RuntimeException("Line must be 21 characters long");
        }
    }

    protected String toString(double value) {
        return StringUtils.substringBefore(Double.toString(value), ".");
    }
}
