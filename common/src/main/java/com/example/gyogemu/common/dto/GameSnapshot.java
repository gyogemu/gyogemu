package com.example.gyogemu.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GameSnapshot {
    private UUID gameId;
    private GamePlayer player1;
    private GamePlayer player2;
    private GameState state;
    private GamePlayerType winner;
    private GameBoard board;

    @JsonCreator
    public GameSnapshot(@JsonProperty("gameId") UUID gameId, @JsonProperty("player1") GamePlayer player1, @JsonProperty("player2") GamePlayer player2, @JsonProperty("state") GameState state, @JsonProperty("winner") GamePlayerType winner, @JsonProperty("board") GameBoard board) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.state = state;
        this.winner = winner;
        this.board = board;
    }
}
