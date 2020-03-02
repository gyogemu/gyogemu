package com.example.gyogemu.server.db;

import com.example.gyogemu.common.dto.GameSnapshot;
import com.example.gyogemu.common.dto.GameState;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class GameStore {

    private HashMap<UUID, GameSnapshot> gameMap = new HashMap<>();

    public GameStore() {

    }

    public void wipe() {
        gameMap.clear();
    }

    public GameSnapshot getGame(UUID gameId) {
        return gameMap.get(gameId);
    }

    public GameSnapshot getPreGame() {
        for (Map.Entry<UUID, GameSnapshot> entry : gameMap.entrySet()) {
            if (entry.getValue().getState() == GameState.PRE_GAME) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void saveGame(GameSnapshot gameSnapshot) {
        gameMap.put(gameSnapshot.getGameId(), gameSnapshot);
    }
}
