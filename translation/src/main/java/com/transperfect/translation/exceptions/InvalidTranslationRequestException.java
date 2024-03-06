package com.transperfect.translation.exceptions;

public class InvalidTranslationRequestException extends RuntimeException{
    public InvalidTranslationRequestException(String message) {
        super(message);
    }
}
