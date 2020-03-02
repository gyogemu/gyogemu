package com.example.gyogemu.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameBoard {
    private GamePlayerType[][] slots;

    @JsonCreator
    public GameBoard(@JsonProperty("slots") GamePlayerType[][] slots) {
        this.slots = slots;
    }
}
