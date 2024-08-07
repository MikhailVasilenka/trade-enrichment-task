package com.verygoodbank.tes.web.controller;

import com.verygoodbank.tes.service.TradeEnrichmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TradeEnrichmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TradeEnrichmentService tradeEnrichmentService;

    @InjectMocks
    private TradeEnrichmentController tradeEnrichmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tradeEnrichmentController).build();
    }

    @Test
    void givenValidFile_whenEnrichTradeData_thenReturnsEnrichedTrades() throws Exception {
        // given
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv",
                "text/csv", "sample,data".getBytes());

        doAnswer(invocation -> {
            OutputStream outputStream = (OutputStream) invocation.getArguments()[1];
            outputStream.write("date,product_id,product_name,currency,price\n".getBytes());
            outputStream.write("20240101,1,Treasury Bills Domestic,EUR,10.0".getBytes());
            return null;
        }).when(tradeEnrichmentService).enrichTradeData(any(), any());

        // when & then
        mockMvc.perform(multipart("/api/v1/enrich")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=enriched_trades.csv"))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("date,product_id,product_name,currency,price\n20240101,1,Treasury Bills Domestic,EUR,10.0"));

        verify(tradeEnrichmentService, times(1)).enrichTradeData(any(), any());
    }

    @Test
    void givenEmptyFile_whenEnrichTradeData_thenReturnsBadRequest() throws Exception {
        // given
        final MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        // when & then
        mockMvc.perform(multipart("/api/v1/enrich")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(tradeEnrichmentService, never()).enrichTradeData(any(), any());
    }

    @Test
    void givenNonCsvFile_whenEnrichTradeData_thenReturnsBadRequest() throws Exception {
        // given
        final MockMultipartFile nonCsvFile = new MockMultipartFile("file", "data.txt", "text/plain", "sample data".getBytes());

        // when & then
        mockMvc.perform(multipart("/api/v1/enrich")
                        .file(nonCsvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(tradeEnrichmentService, never()).enrichTradeData(any(), any());
    }
}
