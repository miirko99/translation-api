package com.transperfect.translation.services;

import com.transperfect.translation.dao.TranslateRequest;
import com.transperfect.translation.exceptions.InvalidTranslationRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.EndsWith;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.boot.web.client.RestTemplateBuilder;

class TranslationServiceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        translationService = new TranslationService(restTemplateBuilder);
    }

    @Test
    void handleTranslateRequest_ValidRequest_ReturnsTranslation() {
        translationService.supportedLanguages.add("eng");
        translationService.supportedLanguages.add("fra");
        translationService.supportedDomains.add("general");

        TranslateRequest request = new TranslateRequest("eng", "fra", "general", "Hello");

        ResponseEntity<String> translationResponse = ResponseEntity.ok("Bonjour");
        when(restTemplate.postForEntity(anyString(), any(TranslateRequest.class), eq(String.class)))
                .thenReturn(translationResponse);

        String translatedText = translationService.handleTranslateRequest(request);

        assertEquals("Bonjour", translatedText);
    }

    @Test
    void handleTranslateRequest_InvalidSourceLang_ThrowsException() {
        translationService.supportedLanguages.add("fra");
        translationService.supportedDomains.add("general");

        TranslateRequest request = new TranslateRequest("eng", "fra", "general", "Hello");

        assertThrows(InvalidTranslationRequestException.class,
                () -> translationService.handleTranslateRequest(request));
    }

    @Test
    void handleTranslateRequest_InvalidTargetLang_ThrowsException() {
        translationService.supportedLanguages.add("eng");
        translationService.supportedDomains.add("general");

        TranslateRequest request = new TranslateRequest("eng", "fra", "general", "Hello");

        assertThrows(InvalidTranslationRequestException.class,
                () -> translationService.handleTranslateRequest(request));
    }

    @Test
    void handleTranslateRequest_InvalidDomain_ThrowsException() {
        translationService.supportedLanguages.add("eng");
        translationService.supportedLanguages.add("fra");

        TranslateRequest request = new TranslateRequest("eng", "fra", "general", "Hello");

        assertThrows(InvalidTranslationRequestException.class,
                () -> translationService.handleTranslateRequest(request));
    }

    @Test
    void handleTranslateRequest_ContentTooLong_ThrowsException() {
        translationService.supportedLanguages.add("eng");
        translationService.supportedLanguages.add("fra");
        translationService.supportedDomains.add("general");


        String content="word ".repeat(31);
        TranslateRequest request = new TranslateRequest("eng", "fra", "general", content);

        assertThrows(InvalidTranslationRequestException.class,
                () -> translationService.handleTranslateRequest(request));
    }

    @Test
    void updateLanguagesAndDomains_ApiError_LogsError() {
        when(restTemplate.getForEntity(argThat(new EndsWith("/languages")), eq(String[].class)))
                .thenReturn(ResponseEntity.ok(new String[]{"eng","fra"}));
        when(restTemplate.getForEntity(argThat(new EndsWith("/domains")), eq(String[].class)))
                .thenReturn(ResponseEntity.ok(new String[]{"general"}));

        translationService.updateLanguagesAndDomains();
        verify(restTemplate, times(2)).getForEntity(anyString(), eq(String[].class));

    }

}
