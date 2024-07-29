package com.verygoodbank.tes.service.impl;

import com.verygoodbank.tes.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeEnrichmentServiceImplTest {

    @InjectMocks
    private TradeEnrichmentServiceImpl tradeEnrichmentService;

    @Mock
    private ProductService productService;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newSingleThreadExecutor();
        ReflectionTestUtils.setField(tradeEnrichmentService, "executorService", executorService);
        ReflectionTestUtils.setField(tradeEnrichmentService, "batchSize", 10);
    }

    @Test
    void givenValidFile_whenEnrichTradeData_thenReturnEnrichedTrades() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeData.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));
        when(productService.getProductName("1")).thenReturn("Treasury Bills Domestic");
        when(productService.getProductName("2")).thenReturn("Corporate Bonds Domestic");
        when(productService.getProductName("3")).thenReturn("REPO Domestic");
        when(productService.getProductName("11")).thenReturn("Missing Product Name");


        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("date,product_id,product_name,currency,price", result.get(0));
        assertEquals("20240101,1,Treasury Bills Domestic,EUR,10.0", result.get(1));
        assertEquals("20240101,2,Corporate Bonds Domestic,EUR,20.1", result.get(2));
        assertEquals("20240101,3,REPO Domestic,EUR,30.34", result.get(3));
        assertEquals("20240101,11,Missing Product Name,EUR,35.34", result.get(4));
    }

    @Test
    void givenInvalidDateFormat_whenEnrichTradeData_thenSkipInvalidTrade() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeDataInvalidDate.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeDataInvalidDate.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));
        when(productService.getProductName(any())).thenReturn("Corporate Bonds Domestic");


        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("date,product_id,product_name,currency,price", result.get(0));
        assertEquals("20240101,2,Corporate Bonds Domestic,EUR,20.1", result.get(1));
    }

    @Test
    void givenInvalidTradeDataFormat_whenEnrichTradeData_thenSkipInvalidTrade() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeDataInvalidFormat.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeDataInvalidFormat.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));
        when(productService.getProductName(any())).thenReturn("Corporate Bonds Domestic");


        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("date,product_id,product_name,currency,price", result.get(0));
        assertEquals("20240101,2,Corporate Bonds Domestic,EUR,20.1", result.get(1));
    }

    @Test
    void givenMissingProductMapping_whenEnrichTradeData_thenUseDefaultProductName() throws Exception {
        // given
        final Path tradeDataPath = Path.of("src/test/resources/tradeData.csv");
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv",
                "text/csv", Files.readAllBytes(tradeDataPath));

        when(productService.getProductName(any())).thenReturn("Missing Product Name");


        // when
        List<String> result = tradeEnrichmentService.enrichTradeData(file);

        // then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("date,product_id,product_name,currency,price", result.get(0));
        assertEquals("20240101,1,Missing Product Name,EUR,10.0", result.get(1));
        assertEquals("20240101,2,Missing Product Name,EUR,20.1", result.get(2));
        assertEquals("20240101,3,Missing Product Name,EUR,30.34", result.get(3));
        assertEquals("20240101,11,Missing Product Name,EUR,35.34", result.get(4));
    }
}
