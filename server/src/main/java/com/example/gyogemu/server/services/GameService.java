package com.example.gyogemu.server.services;


import com.example.gyogemu.common.GameConstants;
import com.example.gyogemu.common.dto.*;
import com.example.gyogemu.server.db.GameStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class GameService {

    @Autowired
    private GameStore gameStore;

    @Autowired
    private GameScanner gameScanner;

    public GameService() {

    }

    /**
     * This methods wipes all games, used for testing
     */
    public void wipe() {
        gameStore.wipe();
    }

    /**
     * This either creates a new game in a waiting state or joins the player to an existing game
     * @param playerName the name of the player to be added
     * @return the game details
     */
    public GameSnapshot joinGame(String playerName) {

        validatePlayerName(playerName);
        GameSnapshot snapshot = gameStore.getPreGame();
        if (snapshot == null) // no waiting games
        {
            // create new game with player 1 and set to pre game state
            GamePlayer player = GamePlayer.builder()
                    .playerId(UUID.randomUUID())
                    .playerName(playerName)
                    .build();
            GameBoard board = GameBoard.builder().slots(new GamePlayerType[GameConstants.MAX_ROWS][GameConstants.MAX_COLUMNS]).build();
            snapshot = GameSnapshot.builder()
                    .gameId(UUID.randomUUID())
                    .player1(player)
                    .state(GameState.PRE_GAME)
                    .board(board)
                    .build();
        } else {
            // add player 2 to waiting game and set to first turn state
            GamePlayer player = GamePlayer.builder()
                    .playerId(UUID.randomUUID())
                    .playerName(playerName)
                    .build();
            snapshot.setPlayer2(player);
            snapshot.setState(GameState.FIRST_PLAYERS_TURN);
        }
        gameStore.saveGame(snapshot);
        return snapshot;
    }

    /**
     * Retrieves the games matching the specified id
     * @param gameId the id of the game to be fetched
     * @return the game details
     */
    public GameSnapshot getGame(UUID gameId) {
        GameSnapshot snapshot = gameStore.getGame(gameId);
        if (snapshot == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game Not Found");
        }
        return snapshot;
    }

    /**
     * Abandons a game in progress
     * @param gameId the game to be quit
     * @return the game details
     */
    public GameSnapshot quitGame(UUID gameId) {
        GameSnapshot snapshot = gameStore.getGame(gameId);
        if (snapshot == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game Not Found");
        } else if (snapshot.getState() == GameState.GAME_OVER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game Already Over");
        }

        snapshot.setState(GameState.GAME_OVER);
        gameStore.saveGame(snapshot);
        return snapshot;
    }

    /**
     * Applies the turn to the game updating its state
     * @param turn the turn details
     * @return the game details
     */
    public GameSnapshot makeTurn(GameTurn turn) {
        GameSnapshot snapshot = gameStore.getGame(turn.getGameId());
        validateTurn(snapshot, turn);

        GamePlayerType slot = null;
        if (snapshot.getState() == GameState.FIRST_PLAYERS_TURN) {
            slot = GamePlayerType.PLAYER_ONE;
            snapshot.setState(GameState.SECOND_PLAYERS_TURN);

        } else if (snapshot.getState() == GameState.SECOND_PLAYERS_TURN) {
            slot = GamePlayerType.PLAYER_TWO;
            snapshot.setState(GameState.FIRST_PLAYERS_TURN);
        }

        snapshot = dropToken(snapshot, turn.getColumn(), slot);

        snapshot = updateWinningState(snapshot, turn);

        gameStore.saveGame(snapshot);
        return snapshot;
    }

    private GameSnapshot updateWinningState(GameSnapshot snapshot, GameTurn turn) {
        boolean winning = gameScanner.scan(snapshot);
        if (winning) {
            snapshot.setState(GameState.GAME_OVER);
            if (turn.getPlayerId().equals(snapshot.getPlayer1().getPlayerId())) {
                snapshot.setWinner(GamePlayerType.PLAYER_ONE);
            } else if (turn.getPlayerId().equals(snapshot.getPlayer2().getPlayerId())) {
                snapshot.setWinner(GamePlayerType.PLAYER_TWO);
            }
        } else if (gameScanner.isBoardFull(snapshot)) {
            snapshot.setState(GameState.GAME_OVER);
        }

        return snapshot;
    }

    private GameSnapshot dropToken(GameSnapshot snapshot, int column, GamePlayerType slot) {
        for (int row = 0; row < snapshot.getBoard().getSlots().length; row++) {
            if (snapshot.getBoard().getSlots()[row][column] == null) {
                snapshot.getBoard().getSlots()[row][column] = slot;
                return snapshot;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Column is full");
    }

    private void validatePlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No player Name specified");
        } else if (playerName.length() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player Name exceeds 10 chars");
        }
    }

    private void validateTurn(GameSnapshot snapshot, GameTurn turn) {
        if (snapshot == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game Not Found");
        } else if (snapshot.getState() == GameState.GAME_OVER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game Over");
        } else if (snapshot.getState() == GameState.PRE_GAME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game Not Started");
        } else if (!turn.getPlayerId().equals(snapshot.getPlayer1().getPlayerId()) && !turn.getPlayerId().equals(snapshot.getPlayer2().getPlayerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid player in this game");
        } else if (snapshot.getState() == GameState.FIRST_PLAYERS_TURN && !turn.getPlayerId().equals(snapshot.getPlayer1().getPlayerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It is Player one's turn");
        } else if (snapshot.getState() == GameState.SECOND_PLAYERS_TURN && !turn.getPlayerId().equals(snapshot.getPlayer2().getPlayerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It is Player two's turn");
        }
        if (turn.getColumn() < 0 || turn.getColumn() >= GameConstants.MAX_COLUMNS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Column");
        }
    }
}
