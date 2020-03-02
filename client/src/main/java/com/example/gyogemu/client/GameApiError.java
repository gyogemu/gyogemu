package com.example.gyogemu.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
class GameApiError {

    private String timestamp;
    private String status;
    private String error;
    private String message;
    private String trace;
    private String path;

    @JsonCreator
    public GameApiError(@JsonProperty("timestamp") String timestamp, @JsonProperty("status") String status, @JsonProperty("error") String error, @JsonProperty("message") String message, @JsonProperty("trace") String trace, @JsonProperty("path") String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.trace = trace;
        this.path = path;
    }
}
