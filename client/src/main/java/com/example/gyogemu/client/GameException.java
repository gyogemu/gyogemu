package com.example.gyogemu.client;

public class GameException extends Exception {

    private GameApiError error;

    public GameException(GameApiError error) {
        super(error.getMessage());
        this.error = error;
    }

    public GameApiError getError() {
        return error;
    }
}
