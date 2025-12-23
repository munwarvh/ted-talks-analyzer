package com.iodigital.tedtalks.application.port;

import com.iodigital.tedtalks.domain.service.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface CsvImporter {

    ImportResult importFromCsv(String importId, InputStream csvStream);

    ImportResult importFromCsv(String importId, MultipartFile file);
}
