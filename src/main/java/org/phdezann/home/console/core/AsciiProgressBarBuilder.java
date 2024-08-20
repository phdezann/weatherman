package org.phdezann.home.console.core;

import java.util.ArrayList;
import java.util.List;

public class AsciiProgressBarBuilder {

    public List<String> build(int maxValue, int value, int colCount, int rowCount) {
        var lines = new ArrayList<String>();
        var total = colCount * rowCount - 2 * rowCount;
        var scaledValue = value * total / maxValue;
        for (int c = 0; c < rowCount; c++) {
            var sb = new StringBuilder();
            sb.append("[");
            for (int r = 0; r < colCount; r++) {
                var i = c * colCount + r;
                if (i < scaledValue) {
                    sb.append("=");
                } else {
                    sb.append("·");
                }
            }
            sb.append("]");
            lines.add(sb.toString());
        }
        return lines;
    }
}
