package com.example.gyogemu.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GameTurn {
    private UUID gameId;
    private UUID playerId;
    private int column;

    @JsonCreator
    public GameTurn(@JsonProperty("gameId") UUID gameId, @JsonProperty("playerId") UUID playerId, @JsonProperty("column") int column) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.column = column;
    }
}
