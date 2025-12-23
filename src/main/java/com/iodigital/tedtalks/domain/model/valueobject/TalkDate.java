package com.iodigital.tedtalks.domain.model.valueobject;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Locale;

public record TalkDate(int year, int month) implements Comparable<TalkDate> {

    private static final DateTimeFormatter PARSER =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    public TalkDate {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
    }

    public static TalkDate fromString(String dateStr) {
        try {
            YearMonth yearMonth = YearMonth.parse(dateStr, PARSER);
            return new TalkDate(yearMonth.getYear(), yearMonth.getMonthValue());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr +
                    ". Expected format like 'December 2021'", e);
        }
    }

    public java.time.LocalDate toLocalDate() {
        return java.time.LocalDate.of(year, month, 1);
    }

    @Override
    public int compareTo(TalkDate other) {
        return Comparator.comparingInt(TalkDate::year)
                .thenComparingInt(TalkDate::month)
                .compare(this, other);
    }

    @Override
    public String toString() {
        return String.format("%d-%02d", year, month);
    }
}