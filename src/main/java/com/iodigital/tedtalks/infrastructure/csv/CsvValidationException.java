package com.iodigital.tedtalks.infrastructure.csv;

import lombok.Getter;

@Getter
public class CsvValidationException extends RuntimeException {
    private final long recordNumber;
    private final String field;
    private final String value;

    public CsvValidationException(long recordNumber, String field,
                                  String value, String message) {
        super(String.format("Record %d, field '%s'='%s': %s",
                recordNumber, field, value, message));
        this.recordNumber = recordNumber;
        this.field = field;
        this.value = value;
    }

}

