package com.example.bankDemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private String resultCode;
    @JsonProperty("resultDesc")
    private String resultMessage;

    public ApiResponse(String reesultCode, String resultMessage) {
        this.resultCode = reesultCode;
        this.resultMessage = resultMessage;
    }
}
