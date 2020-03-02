package com.example.gyogemu.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GamePlayer {
    private UUID playerId;
    private String playerName;

    @JsonCreator
    public GamePlayer(@JsonProperty("playerId") UUID playerId, @JsonProperty("playerName") String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
}

