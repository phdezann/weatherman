package org.phdezann.home.console.printer;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.phdezann.home.console.core.InfluxDBRepository.DateValue;

public class ConsumptionPrinter extends AbstractPrinter {

    public String printHistorical(List<DateValue> dateValues) {
        var lines = new ArrayList<String>();
        lines.add(toLineLayoutV1("historique", ""));
        lines.addAll(dateValues //
                .stream() //
                .sorted(Comparator.comparing(DateValue::getDay).reversed()) //
                .map(dateValue -> toLineLayoutV1(format(dateValue.getDay()),
                        format(dateValue.getValue(), false) + " kWh")) //
                .toList());
        return String.join("\n", lines);
    }

    public String printRealtime(Optional<DateValue> current, List<DateValue> dateValues) {
        if (current.isEmpty()) {
            return "N/A";
        }
        var reference = current.get().getValue();
        dateValues = convertToDiff(dateValues, reference);
        var lines = new ArrayList<String>();
        lines.add(toLineLayoutV1("temps réel", format(reference, false) + " kWh"));
        lines.addAll(dateValues //
                .stream() //
                .sorted(Comparator.comparing(DateValue::getDay).reversed()) //
                .map(dateValue -> toLineLayoutV1(format(dateValue.getDay()),
                        format(dateValue.getValue(), true) + " kWh")) //
                .toList());
        return String.join("\n", lines);
    }

    public List<DateValue> convertToDiff(List<DateValue> dateValues, BigDecimal reference) {
        return dateValues //
                .stream() //
                .map(dateValue -> new DateValue(dateValue.getDay(), reference.subtract(dateValue.getValue()))) //
                .toList();
    }

    private String format(LocalDate date) {
        var formatter = DateTimeFormatter.ofPattern("dd/MM eee", Locale.FRENCH);
        return date.format(formatter);
    }

    private String format(BigDecimal bigDecimal, boolean addPlusSign) {
        DecimalFormat df = new DecimalFormat("0.000");
        var str = df.format(bigDecimal);
        if (addPlusSign && bigDecimal.signum() == 1) {
            str = "+" + str;
        }
        return str;
    }

    private String toLineLayoutV1(String col1, String col2) {
        return rightPad(col1, 10) + " " + leftPad(col2, 10);
    }

}
