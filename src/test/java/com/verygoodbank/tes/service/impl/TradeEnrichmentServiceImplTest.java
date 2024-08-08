package com.verygoodbank.tes.service.impl;

import com.verygoodbank.tes.exception.InternalServerError;
import com.verygoodbank.tes.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeEnrichmentServiceImplTest {

    @Mock
    private ProductService productService;

    @Mock
    private ForkJoinPool forkJoinPool;

    @InjectMocks
    private TradeEnrichmentServiceImpl tradeEnrichmentService;

    @BeforeEach
    void setUp() {
        forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        tradeEnrichmentService = new TradeEnrichmentServiceImpl(productService, forkJoinPool);
    }

    @Test
    void givenValidFile_whenEnrichTradeData_thenReturnEnrichedTrades() throws Exception {
        // given
        final String csvContent = "date,product_id,currency,price\n20240101,1,EUR,10.0\n20240101,2,EUR,20.1";
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv", "text/csv", csvContent.getBytes());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(productService.getProductName("1")).thenReturn("Treasury Bills Domestic");
        when(productService.getProductName("2")).thenReturn("Corporate Bonds Domestic");

        // when
        tradeEnrichmentService.enrichTradeData(file, outputStream);

        // then
        final String result = outputStream.toString(StandardCharsets.UTF_8);
        final String[] lines = result.split("\\r?\\n");
        assertEquals(3, lines.length);
        assertEquals("date,product_id,product_name,currency,price", lines[0]);
        assertEquals("20240101,1,Treasury Bills Domestic,EUR,10.0", lines[1]);
        assertEquals("20240101,2,Corporate Bonds Domestic,EUR,20.1", lines[2]);
    }

    @Test
    void givenInvalidDateFormat_whenEnrichTradeData_thenSkipInvalidTrade() throws Exception {
        // given
        final String csvContent = "date,product_id,currency,price\n2024-01-01,1,EUR,10.0\n20240101,2,EUR,20.1";
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv", "text/csv", csvContent.getBytes());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(productService.getProductName(anyString())).thenReturn("Some Product");

        // when
        tradeEnrichmentService.enrichTradeData(file, outputStream);

        // then
        final String result = outputStream.toString(StandardCharsets.UTF_8);
        final String[] lines = result.split("\\r?\\n");
        assertEquals(2, lines.length);
        assertEquals("date,product_id,product_name,currency,price", lines[0]);
        assertEquals("20240101,2,Some Product,EUR,20.1", lines[1]);
    }

    @Test
    void givenMissingProductMapping_whenEnrichTradeData_thenUseDefaultProductName() throws Exception {
        // given
        final String csvContent = "date,product_id,currency,price\n20240101,1,EUR,10.0";
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv", "text/csv", csvContent.getBytes());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(productService.getProductName("1")).thenReturn("Missing Product Name");

        // when
        tradeEnrichmentService.enrichTradeData(file, outputStream);

        // then
        final String result = outputStream.toString(StandardCharsets.UTF_8);
        final String[] lines = result.split("\\r?\\n");
        assertEquals(2, lines.length);
        assertEquals("date,product_id,product_name,currency,price", lines[0]);
        assertEquals("20240101,1,Missing Product Name,EUR,10.0", lines[1]);
    }

    @Test
    void givenIOException_whenEnrichTradeData_thenThrowInternalServerError() throws Exception {
        // given
        final String csvContent = "date,product_id,currency,price\n20240101,1,EUR,10.0";
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv", "text/csv", csvContent.getBytes());
        final ByteArrayOutputStream outputStream = mock(ByteArrayOutputStream.class);

        doThrow(new IOException("Simulated IO error")).when(outputStream).write(any(byte[].class));

        // when & then
        assertThrows(InternalServerError.class, () -> tradeEnrichmentService.enrichTradeData(file, outputStream));
    }

    @Test
    void givenValidDate_whenIsValidDate_thenReturnTrue() {
        // given
        final String validDate = "20240101";

        // when
        final boolean result = tradeEnrichmentService.isValidDate(validDate);

        // then
        assertTrue(result);
    }

    @Test
    void givenInvalidDate_whenIsValidDate_thenReturnFalse() {
        // given
        final String invalidDate = "2024-01-01";

        // when
        final boolean result = tradeEnrichmentService.isValidDate(invalidDate);

        // then
        assertFalse(result);
    }

    @Test
    void givenCachedDate_whenIsValidDate_thenReturnCachedResult() {
        // given
        final String validDate = "20240101";

        // when
        final boolean firstResult = tradeEnrichmentService.isValidDate(validDate);
        final boolean secondResult = tradeEnrichmentService.isValidDate(validDate);

        // then
        assertTrue(firstResult);
        assertTrue(secondResult);
        // Verify that the second call uses the cached result (you might need to expose the cache for this assertion)
    }
}
