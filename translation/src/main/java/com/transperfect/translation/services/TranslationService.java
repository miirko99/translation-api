package com.transperfect.translation.services;

import com.transperfect.translation.dao.TranslateRequest;
import com.transperfect.translation.exceptions.InvalidTranslationRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

@Slf4j
@Service
public class TranslationService {

    @Value("${translationApi}")
    private String translationApiUrl;
    HashSet<String> supportedLanguages = new HashSet<>();
    HashSet<String> supportedDomains = new HashSet<>();
    private final RestTemplate restTemplate;

    /**
     * Constructs a new TranslationService with the given RestTemplateBuilder.
     *
     * @param restTemplateBuilder the RestTemplateBuilder used to create RestTemplate instances
     */
    public TranslationService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Initializes the TranslationService by fetching supported languages and domains from the translation API.
     */
    @PostConstruct
    private void fetchData(){
        updateLanguagesAndDomains();
    }

    /**
     * Handles a translation request by validating it and performing the translation if request is valid.
     *
     * @param request the TranslateRequest containing the details of the translation
     * @return the translated text
     * @throws InvalidTranslationRequestException if the translation request is invalid
     */
    public String handleTranslateRequest(TranslateRequest request){
        validateTranslationRequest(request);
        return translate(request);
    }

    /**
     * Translates the content in the provided TranslateRequest using the translation API.
     *
     * @param request the TranslateRequest containing the details of the translation
     * @return the translated text
     * @throws InvalidTranslationRequestException if the translation request fails or is invalid
     */
    private String translate(TranslateRequest request) {
        try {
            String translateUrl = translationApiUrl + "/translate";
            ResponseEntity<String> translationResponse = restTemplate.postForEntity(translateUrl, request, String.class);
            return translationResponse.getBody();
        }
        catch (HttpClientErrorException.BadRequest e){
            log.error("Failed to translate {}, api responded with error message {}", request, e.getMessage());
            updateLanguagesAndDomains();
            throw new InvalidTranslationRequestException(e.getMessage());
        }
        catch (Exception e){
            log.error("Failed to translate request {}, error {}", request, e.getStackTrace());
            throw e;
        }
    }

    /**
     * Validates the translation request by checking if the source language, target language, domain,
     * and content are supported and within limits.
     *
     * @param request the TranslateRequest to validate
     * @throws InvalidTranslationRequestException if the translation request is invalid
     */
    public void validateTranslationRequest(TranslateRequest request){
        if (!supportedLanguages.contains(request.getSourceLang())){
            throw new InvalidTranslationRequestException(String.format("Unsupported source language: %s",request.getSourceLang()));
        }
        if (!supportedLanguages.contains(request.getTargetLang())){
            throw new InvalidTranslationRequestException(String.format("Unsupported target language: %s",request.getTargetLang()));
        }
        if (!supportedDomains.contains(request.getDomain())){
            throw new InvalidTranslationRequestException(String.format("Unsupported domain: %s", request.getDomain()));
        }
        if (request.getContent().split("\\s+").length>30){
            throw new InvalidTranslationRequestException("Content can't be longer than 30 words");
        }
    }

    /**
     * Scheduled task to update supported languages and domains from the translation API daily.
     */
    @Scheduled(cron = "0 0 0 * * *")
    void updateLanguagesAndDomains(){
        try {
            String suppLanguagesUrl = translationApiUrl + "/languages";
            ResponseEntity<String[]> languagesResponse = restTemplate.getForEntity(suppLanguagesUrl, String[].class);
            supportedLanguages = new HashSet<>(Arrays.asList(Objects.requireNonNull(languagesResponse.getBody())));
            log.info("supportedLanguages updated {}", supportedLanguages);

            String suppDomainsUrl = translationApiUrl + "/domains";
            ResponseEntity<String[]> domainsResponse = restTemplate.getForEntity(suppDomainsUrl, String[].class);
            supportedDomains = new HashSet<>(Arrays.asList(Objects.requireNonNull(domainsResponse.getBody())));
            log.info("supportedDomains updated {}", supportedDomains);

        } catch (Exception e) {
            log.error("Error updating languages and domains {}", e.getMessage());
        }
    }
}
