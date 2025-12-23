package com.iodigital.tedtalks.application.port;

import com.iodigital.tedtalks.domain.service.ImportResult;

import java.io.InputStream;

public interface CsvImporter {

    ImportResult importFromCsv(String importId, InputStream csvStream);
}
