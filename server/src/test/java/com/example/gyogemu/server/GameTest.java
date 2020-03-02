package com.example.gyogemu.server;

import com.example.gyogemu.common.GameConstants;
import com.example.gyogemu.common.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(ServerApplication.class)
public class GameTest {

    @Autowired
    MockMvc mvc;

    TestUtils utils;

    @BeforeEach
    void setup() throws Exception {
        this.utils = new TestUtils(mvc);
        utils.wipe();
    }

    @Test
    void contextLoads() {
    }

    // happy path tests

    @Test
    public void player1JoiningGameSucceeds() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");
    }

    @Test
    public void player2JoiningGameSucceeds() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);
    }

    @Test
    public void player1TakesFirstTurnSucceeds() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 makes Turn
        snapshot = utils.performPlayer1Turn(snapshot, 1);
    }

    @Test
    public void player2TakesFirstTurnSucceeds() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 makes Turn
        snapshot = utils.performPlayer1Turn(snapshot, 1);

        // Player 2 makes Turn
        snapshot = utils.performPlayer2Turn(snapshot, 1);
    }

    @Test
    public void player1WinsVertically() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 vertical win
        utils.performVerticalWin(snapshot);
    }

    @Test
    public void player1WinsHorizontally() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 vertical win
        utils.performHorizontalWin(snapshot);
    }

    @Test
    public void player1WinsDiagonallyRight() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 vertical win
        utils.performDiagonalRightWin(snapshot);
    }

    @Test
    public void player1WinsDiagonallyLeft() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 vertical win
        utils.performDiagonalLeftWin(snapshot);
    }

    @Test
    public void playerQuittingGameSucceeds() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 1 quits game
        utils.performQuitGame(snapshot);
    }

    // negative tests

    @Test
    public void joiningGameWithNoNameFails() throws Exception {

        mvc.perform(post("/game/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize("")))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void joiningGameWithJustWhitespaceFails() throws Exception {

        mvc.perform(post("/game/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize("    ")))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("No player Name specified"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void joiningGameWithNameLongerThanTenCharsFails() throws Exception {

        mvc.perform(post("/game/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize("0123456789X")))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Player Name exceeds 10 chars"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void player1GoingBeforeGameStartsFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // Player 1 makes Turn
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), snapshot.getPlayer1().getPlayerId(), 0);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Game Not Started"))
                .andDo(print())
                .andReturn();
    }


    @Test
    public void player2GoingOutOfTurnFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 2 makes Turn
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), snapshot.getPlayer2().getPlayerId(), 0);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("It is Player one's turn"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void player1GoingOutOfTurnFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 makes Turn
        snapshot = utils.performPlayer1Turn(snapshot, 1);

        // Player 1 makes Turn
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), snapshot.getPlayer1().getPlayerId(), 0);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("It is Player two's turn"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void unknownPlayerMakingTurnFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Unknown Player makes Turn
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), UUID.randomUUID(), 0);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Not a valid player in this game"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void makingTurnOnUnknownGameFails() throws Exception {

        // Unknown Player makes Turn
        GameTurn turn = utils.makeTurn(UUID.randomUUID(), UUID.randomUUID(), 0);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Game Not Found"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void makingTurnOnFinishedGameFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 vertical win
        utils.performVerticalWin(snapshot);

        // Player 2 makes Turn
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), snapshot.getPlayer2().getPlayerId(), 0);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Game Over"))
                .andDo(print())
                .andReturn();

    }

    @Test
    public void gettingGameWithInvalidGameIdFails() throws Exception {

        //get unknown game
        mvc.perform(get("/game/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Game Not Found"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void playerMakesTurnWithNegativeColumnFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 makes invalid turn
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), snapshot.getPlayer1().getPlayerId(), -1);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Invalid Column"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void playerMakesTurnWithTooLargeColumnFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 makes invalid turn
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), snapshot.getPlayer1().getPlayerId(), GameConstants.MAX_COLUMNS + 1);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Invalid Column"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void playerMakesTurnOnFullColumnFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // make enough turns to fill column
        for (int i = 0; i < GameConstants.MAX_ROWS; i += 2) {
            snapshot = utils.performPlayer1Turn(snapshot, 0);
            snapshot = utils.performPlayer2Turn(snapshot, 0);
        }

        // Player 1 makes turn to full column
        GameTurn turn = utils.makeTurn(snapshot.getGameId(), snapshot.getPlayer1().getPlayerId(), 0);
        mvc.perform(post("/game/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(utils.serialize(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Column is full"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void playerQuitsFinishedGameFails() throws Exception {

        // player 1 joins game
        GameSnapshot snapshot = utils.performJoinGame("bob");

        // player 2 joins game
        snapshot = utils.performJoinGame("bill", snapshot);

        // Player 1 vertical win
        snapshot = utils.performVerticalWin(snapshot);

        // player 1 quits game
        mvc.perform(put("/game/" + snapshot.getGameId() + "/quit"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Game Already Over"))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void playerQuitsUnknownGameFails() throws Exception {

        // player 1 quits game
        mvc.perform(put("/game/" + UUID.randomUUID() + "/quit"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Game Not Found"))
                .andDo(print())
                .andReturn();
    }
}