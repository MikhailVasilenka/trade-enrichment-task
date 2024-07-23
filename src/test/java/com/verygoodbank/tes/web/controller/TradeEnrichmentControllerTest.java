package com.verygoodbank.tes.web.controller;

import com.verygoodbank.tes.service.TradeEnrichmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TradeEnrichmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TradeEnrichmentService tradeEnrichmentService;

    @InjectMocks
    private TradeEnrichmentController tradeEnrichmentController;

    @Test
    void givenValidFile_whenEnrichTradeData_thenReturnsEnrichedTrades() throws Exception {
        // given
        mockMvc = MockMvcBuilders.standaloneSetup(tradeEnrichmentController).build();
        final MockMultipartFile file = new MockMultipartFile("file", "tradeData.csv", "text/csv", "sample,data".getBytes());

        when(tradeEnrichmentService.enrichTradeData(any(MultipartFile.class)))
                .thenReturn(List.of("20160101,1,Treasury Bills Domestic,EUR,10.0"));

        // when & then
        mockMvc.perform(multipart("/api/v1/enrich")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("20160101,1,Treasury Bills Domestic,EUR,10.0")));
    }

    @Test
    void givenEmptyFile_whenEnrichTradeData_thenReturnsBadRequest() throws Exception {
        // given
        mockMvc = MockMvcBuilders.standaloneSetup(tradeEnrichmentController).build();
        final MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        // when & then
        mockMvc.perform(multipart("/api/v1/enrich")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Please upload a file")));
    }
}
