package com.verygoodbank.tes.web.controller;


import com.verygoodbank.tes.dao.Trade;
import com.verygoodbank.tes.service.TradeEnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Slf4j
public class TradeEnrichmentController {

    private final TradeEnrichmentService tradeEnrichmentService;

    @PostMapping("/enrich")
    public ResponseEntity<List<String>> enrichTradeData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonList("Please upload a file"));
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(Collections.singletonList("Invalid file format. Please upload a CSV file."));
        }

        log.debug("enrichTradeData POST: file {} received for processing. File size: {}", file.getName(), file.getSize());
        return ResponseEntity.ok(tradeEnrichmentService.enrichTradeData(file));
    }
}


