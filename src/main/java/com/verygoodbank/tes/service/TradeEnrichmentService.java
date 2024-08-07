package com.verygoodbank.tes.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

public interface TradeEnrichmentService {

    void enrichTradeData(MultipartFile file, OutputStream outputStream);
}
