package com.verygoodbank.tes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TradeEnrichmentServiceImplTest {

    @InjectMocks
    private TradeEnrichmentServiceImpl tradeEnrichmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tradeEnrichmentService, "batchSize", 10);
        ReflectionTestUtils.setField(tradeEnrichmentService, "productFilePath", "src/test/resources/productData.csv");

        tradeEnrichmentService.init();
    }

    @Test
    void givenValidFile_whenEnrichTradeData_thenReturnEnrichedTrades() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeData.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));

        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("20240101,1,Treasury Bills Domestic,EUR,10.0", result.get(0));
        assertEquals("20240101,2,Corporate Bonds Domestic,EUR,20.1", result.get(1));
        assertEquals("20240101,3,REPO Domestic,EUR,30.34", result.get(2));
        assertEquals("20240101,11,Missing Product Name,EUR,35.34", result.get(3));
    }

    @Test
    void givenInvalidDateFormat_whenEnrichTradeData_thenSkipInvalidTrade() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeDataInvalidDate.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeDataInvalidDate.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));

        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("20240101,2,Corporate Bonds Domestic,EUR,20.1", result.get(0));
    }

    @Test
    void givenInvalidTradeDataFormat_whenEnrichTradeData_thenSkipInvalidTrade() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeDataInvalidFormat.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeDataInvalidFormat.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));

        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("20240101,2,Corporate Bonds Domestic,EUR,20.1", result.get(0));
    }

    @Test
    void givenMissingProductMapping_whenEnrichTradeData_thenUseDefaultProductName() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeData.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));

        // Clear product map to simulate missing products
        ReflectionTestUtils.setField(tradeEnrichmentService, "productMap", new ConcurrentHashMap<>());

        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("20240101,1,Missing Product Name,EUR,10.0", result.get(0));
        assertEquals("20240101,2,Missing Product Name,EUR,20.1", result.get(1));
        assertEquals("20240101,3,Missing Product Name,EUR,30.34", result.get(2));
        assertEquals("20240101,11,Missing Product Name,EUR,35.34", result.get(3));
    }
}
