package com.example.gyogemu.server;

import com.example.gyogemu.common.GameConstants;
import com.example.gyogemu.common.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.AssertJUnit.*;
import static org.testng.AssertJUnit.assertEquals;

public class TestUtils {

    private MockMvc mvc;

    public TestUtils(MockMvc mvc) {
        this.mvc = mvc;
    }

    public GameSnapshot performJoinGame(String playerName) throws Exception {
        return performJoinGame(playerName, null);
    }

    public GameSnapshot performJoinGame(String playerName, GameSnapshot previousSnapshot) throws Exception {
        GameSnapshot snapshot = joinGame(playerName);
        if (previousSnapshot == null) {
            validateFirstPlayerJoinedGame(snapshot, playerName);
        } else {
            validateSecondPlayerJoinedGame(previousSnapshot, snapshot, playerName);
        }
        retrieveAndValidateGame(snapshot);
        return snapshot;
    }

    public GameSnapshot performQuitGame(GameSnapshot previousSnapshot) throws Exception {
        GameSnapshot snapshot = quitGame(previousSnapshot.getGameId());
        validatePlayerQuitGame(snapshot);
        retrieveAndValidateGame(snapshot);
        return snapshot;
    }

    public GameSnapshot performVerticalWin(GameSnapshot previousSnapshot) throws Exception {
        GameSnapshot snapshot = previousSnapshot;
        for (int i = 0; i < 4; i++) {
            snapshot = performPlayer1Turn(snapshot, 1);
            snapshot = performPlayer2Turn(snapshot, 2);
        }

        return performPlayer1WinningTurn(snapshot, 1);
    }

    public GameSnapshot performHorizontalWin(GameSnapshot previousSnapshot) throws Exception {
        GameSnapshot snapshot = previousSnapshot;
        for (int i = 0; i < 4; i++) {
            snapshot = performPlayer1Turn(snapshot, i);
            snapshot = performPlayer2Turn(snapshot, i);
        }
        return performPlayer1WinningTurn(snapshot, 4);
    }

    public GameSnapshot performDiagonalRightWin(GameSnapshot previousSnapshot) throws Exception {
        GameSnapshot snapshot = previousSnapshot;

        snapshot = performPlayer1Turn(snapshot, 0);
        snapshot = performPlayer2Turn(snapshot, 1);
        snapshot = performPlayer1Turn(snapshot, 2);
        snapshot = performPlayer2Turn(snapshot, 3);
        snapshot = performPlayer1Turn(snapshot, 4);
        snapshot = performPlayer2Turn(snapshot, 5);

        snapshot = performPlayer1Turn(snapshot, 1);
        snapshot = performPlayer2Turn(snapshot, 2);
        snapshot = performPlayer1Turn(snapshot, 3);
        snapshot = performPlayer2Turn(snapshot, 4);

        snapshot = performPlayer1Turn(snapshot, 2);
        snapshot = performPlayer2Turn(snapshot, 3);
        snapshot = performPlayer1Turn(snapshot, 4);
        snapshot = performPlayer2Turn(snapshot, 5);

        snapshot = performPlayer1Turn(snapshot, 3);
        snapshot = performPlayer2Turn(snapshot, 4);

        return performPlayer1WinningTurn(snapshot, 4);
    }

    public GameSnapshot performDiagonalLeftWin(GameSnapshot previousSnapshot) throws Exception {
        GameSnapshot snapshot = previousSnapshot;

        snapshot = performPlayer1Turn(snapshot, 5);
        snapshot = performPlayer2Turn(snapshot, 4);
        snapshot = performPlayer1Turn(snapshot, 3);
        snapshot = performPlayer2Turn(snapshot, 2);
        snapshot = performPlayer1Turn(snapshot, 1);
        snapshot = performPlayer2Turn(snapshot, 0);

        snapshot = performPlayer1Turn(snapshot, 4);
        snapshot = performPlayer2Turn(snapshot, 3);
        snapshot = performPlayer1Turn(snapshot, 2);
        snapshot = performPlayer2Turn(snapshot, 1);

        snapshot = performPlayer1Turn(snapshot, 3);
        snapshot = performPlayer2Turn(snapshot, 2);
        snapshot = performPlayer1Turn(snapshot, 1);
        snapshot = performPlayer2Turn(snapshot, 0);

        snapshot = performPlayer1Turn(snapshot, 2);
        snapshot = performPlayer2Turn(snapshot, 1);

        return performPlayer1WinningTurn(snapshot, 1);
    }

    public GameSnapshot performPlayer1Turn(GameSnapshot previousSnapshot, int column) throws Exception {
        return performTurn(previousSnapshot, previousSnapshot.getPlayer1().getPlayerId(), column, false);
    }

    public GameSnapshot performPlayer2Turn(GameSnapshot previousSnapshot, int column) throws Exception {
        return performTurn(previousSnapshot, previousSnapshot.getPlayer2().getPlayerId(), column, false);
    }

    public GameSnapshot performPlayer1WinningTurn(GameSnapshot previousSnapshot, int column) throws Exception {
        return performTurn(previousSnapshot, previousSnapshot.getPlayer1().getPlayerId(), column, true);
    }

    public GameSnapshot performTurn(GameSnapshot previousSnapshot, UUID playerId, int column, boolean winning) throws Exception {
        GameTurn turn = makeTurn(previousSnapshot.getGameId(), playerId, column);
        GameSnapshot snapshot = takeTurn(turn);
        validateTurn(previousSnapshot, snapshot, turn, winning);
        retrieveAndValidateGame(snapshot);
        return snapshot;
    }

    public void retrieveAndValidateGame(GameSnapshot snapshot) throws Exception {
        GameSnapshot retrievedSnapshot = getGame(snapshot.getGameId());
        assertEquals(snapshot, retrievedSnapshot);
    }

    public void validateTurn(GameSnapshot previousSnapshot, GameSnapshot snapshot, GameTurn turn, boolean winning) {
        GamePlayerType slot = null;
        if (turn.getPlayerId().equals(snapshot.getPlayer1().getPlayerId())) {
            if (winning) {
                assertEquals(GameState.GAME_OVER, snapshot.getState());
                assertEquals(GamePlayerType.PLAYER_ONE, snapshot.getWinner());
            } else {
                assertEquals(GameState.SECOND_PLAYERS_TURN, snapshot.getState());
                assertNull(snapshot.getWinner());
            }
            slot = GamePlayerType.PLAYER_ONE;
        } else if (turn.getPlayerId().equals(snapshot.getPlayer2().getPlayerId())) {
            if (winning) {
                assertEquals(GameState.GAME_OVER, snapshot.getState());
                assertEquals(GamePlayerType.PLAYER_TWO, snapshot.getWinner());
            } else {
                assertEquals(GameState.FIRST_PLAYERS_TURN, snapshot.getState());
                assertNull(snapshot.getWinner());
            }
            slot = GamePlayerType.PLAYER_TWO;
        } else {
            fail("Invalid player id");
        }

        validateBoardAfterTurn(previousSnapshot.getBoard(), snapshot.getBoard(), turn.getColumn(), slot);
    }

    public void validatePlayerQuitGame(GameSnapshot quitSnapshot) {
        assertEquals(GameState.GAME_OVER, quitSnapshot.getState());
        assertNull(quitSnapshot.getWinner());
    }


    public void validateFirstPlayerJoinedGame(GameSnapshot firstPlayerJoinedSnapshot, String player1Name) {
        assertNotNull(firstPlayerJoinedSnapshot.getGameId());
        assertEquals(GameState.PRE_GAME, firstPlayerJoinedSnapshot.getState());
        assertNull(firstPlayerJoinedSnapshot.getWinner());
        validateEmptyBoard(firstPlayerJoinedSnapshot.getBoard());


        assertNotNull(firstPlayerJoinedSnapshot.getPlayer1());
        assertNotNull(firstPlayerJoinedSnapshot.getPlayer1().getPlayerId());
        assertEquals(firstPlayerJoinedSnapshot.getPlayer1().getPlayerName(), player1Name);

        assertNull(firstPlayerJoinedSnapshot.getPlayer2());
    }

    public void validateSecondPlayerJoinedGame(GameSnapshot firstPlayerJoinedSnapshot, GameSnapshot secondPlayerJoinedSnapshot, String player2Name) {
        assertNotNull(secondPlayerJoinedSnapshot.getGameId());
        assertEquals(GameState.FIRST_PLAYERS_TURN, secondPlayerJoinedSnapshot.getState());
        assertNull(secondPlayerJoinedSnapshot.getWinner());
        validateEmptyBoard(secondPlayerJoinedSnapshot.getBoard());

        assertEquals(secondPlayerJoinedSnapshot.getPlayer1(), firstPlayerJoinedSnapshot.getPlayer1());

        assertNotNull(secondPlayerJoinedSnapshot.getPlayer2());
        assertNotNull(secondPlayerJoinedSnapshot.getPlayer2().getPlayerId());
        assertEquals(secondPlayerJoinedSnapshot.getPlayer2().getPlayerName(), player2Name);
    }

    public void validateEmptyBoard(GameBoard board) {
        assertNotNull(board);
        assertNotNull(board.getSlots());
        assertEquals(GameConstants.MAX_ROWS, board.getSlots().length);
        assertEquals(GameConstants.MAX_COLUMNS, board.getSlots()[0].length);
        for (int i = 0; i < board.getSlots().length; i++) {
            for (int j = 0; j < board.getSlots()[i].length; j++) {
                assertNull(board.getSlots()[i][j]);
            }
        }
    }

    public void validateBoardAfterTurn(GameBoard previousBoard, GameBoard board, int turnColumn, GamePlayerType slotType) {
        assertNotNull(board);
        assertNotNull(board.getSlots());
        assertEquals(GameConstants.MAX_ROWS, board.getSlots().length);
        assertEquals(GameConstants.MAX_COLUMNS, board.getSlots()[0].length);

        // Determine turn row from column and previous board
        int turnRow = 0;
        for (int row = 0; row < previousBoard.getSlots().length; row++) {
            if (previousBoard.getSlots()[row][turnColumn] == null) {
                turnRow = row;
                break;
            }
        }

        // validate board is identical to previous state except for addition of new slot
        for (int row = 0; row < board.getSlots().length; row++) {
            for (int column = 0; column < board.getSlots()[row].length; column++) {
                if (row == turnRow && column == turnColumn) {
                    assertNotNull("Expected slot not filled after turn", board.getSlots()[row][column]);
                    assertEquals("Slot filled incorrectly after turn", slotType, board.getSlots()[row][column]);
                } else {
                    assertEquals(previousBoard.getSlots()[row][column], board.getSlots()[row][column]);
                }
            }
        }
    }

    public GameSnapshot takeTurn(GameTurn turn) throws Exception {
        MvcResult result = mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(turn)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        return deserialize(result);
    }

    public GameTurn makeTurn(UUID gameId, UUID playerId, int column) throws Exception {
        return GameTurn.builder()
                .gameId(gameId)
                .playerId(playerId)
                .column(column)
                .build();
    }

    public GameSnapshot joinGame(String playerName) throws Exception {
        MvcResult result = mvc.perform(post("/game/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(playerName)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        return deserialize(result);
    }

    public GameSnapshot getGame(UUID gameId) throws Exception {
        MvcResult result = mvc.perform(get("/game/" + gameId))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        return deserialize(result);
    }

    public GameSnapshot quitGame(UUID gameId) throws Exception {
        MvcResult result = mvc.perform(put("/game/" + gameId + "/quit"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        return deserialize(result);
    }

    public void wipe() throws Exception {
        mvc.perform(delete("/game/"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    public GameSnapshot deserialize(MvcResult result) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(result.getResponse().getContentAsString(), GameSnapshot.class);
    }

    public String serialize(GameTurn turn) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(turn);
    }

    public String serialize(String string) throws Exception {
        return string;
    }
}
