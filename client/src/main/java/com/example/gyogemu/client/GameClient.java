package com.example.gyogemu.client;

import com.example.gyogemu.common.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class GameClient {

    private String uriBasePath;
    private GameSnapshot lastSnapshot;
    private UUID gameId;
    private GamePlayer player;

    private GamePlayer otherPlayer;

    public GameClient(String uriBasePath) {
        this.uriBasePath = uriBasePath;
    }

    public GameSnapshot getLastSnapshot() {
        return lastSnapshot;
    }

    public UUID getGameId() {
        return gameId;
    }

    public GamePlayer getPlayer() {
        return player;
    }

    public GamePlayer getOtherPlayer() {
        return otherPlayer;
    }

    public boolean isGameOver() {
        return lastSnapshot.getState() == GameState.GAME_OVER;
    }

    public boolean isPreGame() {
        return lastSnapshot.getState() == GameState.PRE_GAME;
    }

    public boolean isQuit() {
        return lastSnapshot != null && lastSnapshot.getState() == GameState.GAME_OVER && lastSnapshot.getWinner() == null;
    }

    public boolean isWinner() {
        if (lastSnapshot != null && lastSnapshot.getState() == GameState.GAME_OVER) {
            if (player.getPlayerId().equals(lastSnapshot.getPlayer1().getPlayerId()) && lastSnapshot.getWinner() == GamePlayerType.PLAYER_ONE) {
                return true;
            }
            if (player.getPlayerId().equals(lastSnapshot.getPlayer2().getPlayerId()) && lastSnapshot.getWinner() == GamePlayerType.PLAYER_TWO) {
                return true;
            }
        }
        return false;
    }

    public boolean isLoser() {
        if (lastSnapshot != null && lastSnapshot.getState() == GameState.GAME_OVER) {
            if (otherPlayer.getPlayerId().equals(lastSnapshot.getPlayer1().getPlayerId()) && lastSnapshot.getWinner() == GamePlayerType.PLAYER_ONE) {
                return true;
            }
            if (otherPlayer.getPlayerId().equals(lastSnapshot.getPlayer2().getPlayerId()) && lastSnapshot.getWinner() == GamePlayerType.PLAYER_TWO) {
                return true;
            }
        }
        return false;
    }

    public boolean isLocalPlayersTurn() {
        if (lastSnapshot != null) {
            if (lastSnapshot.getState() == GameState.FIRST_PLAYERS_TURN && player.getPlayerId().equals(lastSnapshot.getPlayer1().getPlayerId())) {
                return true;
            } else if (lastSnapshot.getState() == GameState.SECOND_PLAYERS_TURN && player.getPlayerId().equals(lastSnapshot.getPlayer2().getPlayerId())) {
                return true;
            }
        }
        return false;
    }

    public GameSnapshot getGame() throws GameException {
        lastSnapshot = send("GET", "/game/" + this.gameId, "");
        if (this.otherPlayer == null) {
            this.otherPlayer = lastSnapshot.getPlayer2();
        }
        return lastSnapshot;
    }

    public GameSnapshot joinGame(String playerName) throws GameException {
        lastSnapshot = send("POST", "/game", playerName);
        this.gameId = lastSnapshot.getGameId();
        if (lastSnapshot.getPlayer2() == null) {
            this.player = lastSnapshot.getPlayer1();
        } else {
            this.player = lastSnapshot.getPlayer2();
            this.otherPlayer = lastSnapshot.getPlayer1();
        }
        return lastSnapshot;
    }

    public GameSnapshot takeTurn(int column) throws GameException {
        GameTurn turn = GameTurn.builder().column(column).playerId(player.getPlayerId()).gameId(gameId).build();
        lastSnapshot = send("POST", "/game/turn", serialize(turn));
        return lastSnapshot;
    }

    public GameSnapshot quit() throws GameException {
        lastSnapshot = send("PUT", "/game/" + this.gameId + "/quit", "");
        return lastSnapshot;
    }

    private GameSnapshot send(String method, String path, String content) throws GameException {
        try {

            URL url = new URL(uriBasePath + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod(method);
            conn.setRequestProperty("Accept", "application/json");
            if (!method.equals("GET")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStream os = conn.getOutputStream();
                os.write(content.getBytes());
                os.flush();
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                StringBuffer buffy = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
                String output;
                while ((output = br.readLine()) != null) {
                    buffy.append(output);
                }
                conn.disconnect();
                GameApiError error = deserializeError(buffy.toString());
                throw new GameException(error);
            } else {
                StringBuffer buffy = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String output;
                while ((output = br.readLine()) != null) {
                    buffy.append(output);
                }
                conn.disconnect();
                return deserialize(buffy.toString());
            }

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
        return null;
    }


    public GameApiError deserializeError(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, GameApiError.class);
        } catch (Exception e) {
            return null;
        }
    }

    public GameSnapshot deserialize(String snapshot) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(snapshot, GameSnapshot.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String serialize(GameTurn turn) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(turn);
        } catch (Exception e) {
            return null;
        }
    }
}
