package com.transperfect.translation.controllers;

import com.transperfect.translation.dao.TranslateRequest;
import com.transperfect.translation.exceptions.InvalidTranslationRequestException;
import com.transperfect.translation.services.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class responsible for handling translation requests and exceptions.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class TranslationController {
    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * Endpoint for handling translation requests. Translates the content in the provided TranslateRequest
     * and returns the translated text.
     *
     * @param request the TranslateRequest containing the details of the translation
     * @return a ResponseEntity with the translated text and a status code of 200 if the translation is successful
     */
    @PostMapping("/validated-translate")
    public ResponseEntity<String> translate(@RequestBody TranslateRequest request){
        String translate = translationService.handleTranslateRequest(request);
        return ResponseEntity.ok(translate);
    }

    /**
     * Exception handler for handling InvalidTranslationRequestException. Returns a ResponseEntity with
     * a status code of 400 (Bad Request) and the error message from the exception.
     *
     * @param e the InvalidTranslationRequestException to handle
     * @return a ResponseEntity with a status code of 400 and the error message from the exception
     */
    @ExceptionHandler(InvalidTranslationRequestException.class)
    public ResponseEntity<String> handleException(InvalidTranslationRequestException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

}
