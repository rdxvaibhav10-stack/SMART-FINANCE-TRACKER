package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtil {
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");
    public static String currentMonth() { return YM.format(LocalDate.now()); }
    public static String toMonth(LocalDate d) { return YM.format(d); }
}
