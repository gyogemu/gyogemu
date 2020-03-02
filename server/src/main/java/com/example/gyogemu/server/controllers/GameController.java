package com.example.gyogemu.server.controllers;

import com.example.gyogemu.common.dto.GameSnapshot;
import com.example.gyogemu.common.dto.GameTurn;
import com.example.gyogemu.server.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;


    @PostMapping("/game")
    public GameSnapshot joinGame(@RequestBody String playerName, HttpServletRequest request) {

        return gameService.joinGame(playerName);
    }

    @GetMapping("/game/{gameId}")
    public GameSnapshot getGame(@PathVariable UUID gameId, HttpServletRequest request) {

        return gameService.getGame(gameId);
    }

    @PutMapping("/game/{gameId}/quit")
    public GameSnapshot quitGame(@PathVariable UUID gameId, HttpServletRequest request) {
        return gameService.quitGame(gameId);
    }

    @PostMapping("/game/turn")
    public GameSnapshot makeTurn(@RequestBody GameTurn turn, HttpServletRequest request) {

        return gameService.makeTurn(turn);
    }

    @DeleteMapping("/game")
    public void wipe() {

        gameService.wipe();
    }


}
