package com.transperfect.translation.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TranslateRequest {

    @JsonProperty("source_language")
    String sourceLang;

    @JsonProperty("target_language")
    String targetLang;

    String domain;

    String content;
}
