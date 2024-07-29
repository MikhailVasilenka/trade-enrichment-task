package com.verygoodbank.tes.service.impl;

import com.verygoodbank.tes.exception.InternalServerError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;

import static com.verygoodbank.tes.exception.ResponseErrorCode.ERROR_LOADING_PRODUCT_DATA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    private final String productFilePath = "src/test/resources/productData.csv";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(productService, "productFilePath", productFilePath);
        productService.init();
    }

    @Test
    void givenValidProductId_whenGetProductName_thenReturnProductName() {
        // given
        final String productId = "1";
        final String expectedProductName = "Treasury Bills Domestic";

        // when
        String actualProductName = productService.getProductName(productId);

        // then
        assertEquals(expectedProductName, actualProductName);
    }

    @Test
    void givenInvalidProductId_whenGetProductName_thenReturnMissingProductName() {
        // given
        final String productId = "999";
        final String expectedProductName = "Missing Product Name";

        // when
        String actualProductName = productService.getProductName(productId);

        // then
        assertEquals(expectedProductName, actualProductName);
    }

    @Test
    void givenCsvFile_whenLoadProductData_thenProductMapIsPopulated() {
        // given
        final String productId = "2";
        final String expectedProductName = "Corporate Bonds Domestic";

        // when
        productService.init();
        String actualProductName = productService.getProductName(productId);

        // then
        assertEquals(expectedProductName, actualProductName);
    }

    @Test
    void givenIoException_whenLoadProductData_thenLogErrorAndThrowException() {
        // given
        final String errorMessage = "Mocked IO exception";
        final String productFilePath = "test/path/products.csv";
        ReflectionTestUtils.setField(productService, "productFilePath", productFilePath);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newBufferedReader(any()))
                    .thenThrow(new IOException(errorMessage));

            // when
            Exception exception = assertThrows(InternalServerError.class, () -> {
                ReflectionTestUtils.invokeMethod(productService, "loadProductData");
            });

            // then
            assertEquals(ERROR_LOADING_PRODUCT_DATA, exception.getMessage());
        }
    }
}
