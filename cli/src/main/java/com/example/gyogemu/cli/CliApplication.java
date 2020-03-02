package com.example.gyogemu.cli;


import com.example.gyogemu.client.GameClient;
import com.example.gyogemu.client.GameException;
import com.example.gyogemu.common.dto.GamePlayerType;
import com.example.gyogemu.common.dto.GameSnapshot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@SpringBootApplication
public class CliApplication implements CommandLineRunner {

    private GameClient client;

    public static void main(String[] args) {
        SpringApplication.run(CliApplication.class, args);
    }

    @Override
    public void run(String... args) throws GameException {
        String uriBasePath = "http://localhost:8080";
        if (args.length > 0) {
            uriBasePath = args[0].trim();
        }

        this.client = new GameClient(uriBasePath);

        startGame();
        waitForGameStart();
        checkEndState();

        while (true) {
            waitForOtherPlayer();
            printSnapshot();
            checkEndState();

            actOnInput(getInput("Enter 1-9, or Q to quit"));
            printSnapshot();
            checkEndState();
        }
    }

    private void checkEndState() {
        if (client.isGameOver()) {
            if (client.isWinner()) {
                System.out.println("You're The Winner!");
            } else if (client.isLoser()) {
                System.out.println("You're The Loser!");
            } else if (client.isQuit()) {
                System.out.println(client.getOtherPlayer().getPlayerName() + " quit the game!");
            }
            System.out.println("GAME OVER");
            System.exit(0);
        }
    }

    private void waitForGameStart() {
        if (client.isPreGame() && !client.isGameOver()) {
            System.out.println("Waiting for another player to join...");
            while (client.isPreGame() && !client.isGameOver()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
                try {
                    client.getGame();
                    if (!client.isPreGame()) {
                        System.out.println("Game Has Started");
                    }
                } catch (GameException ge) {
                    System.out.println("Error: " + ge.getMessage());
                    System.exit(0);
                }
            }
        }
    }

    private void waitForOtherPlayer() {
        if (!client.isLocalPlayersTurn()) {
            System.out.println("Waiting for " + client.getOtherPlayer().getPlayerName() + " to take their turn...");
            while (!client.isLocalPlayersTurn() && !client.isGameOver()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
                try {
                    GameSnapshot snapshot = client.getGame();
                    if (client.isLocalPlayersTurn()) {
                        System.out.println("Its your turn now " + client.getPlayer().getPlayerName());
                    }
                } catch (GameException ge) {
                    System.out.println("Error: " + ge.getMessage());
                    System.exit(0);
                }
            }
        }
    }

    private void printSnapshot() {
        GameSnapshot snapshot = client.getLastSnapshot();
        StringBuffer buffy = new StringBuffer();
        for (int column = 0; column < snapshot.getBoard().getSlots()[0].length; column++) {
            buffy.append("-" + column + "-");
        }
        buffy.append("\n");
        for (int row = snapshot.getBoard().getSlots().length - 1; row >= 0; row--) {
            for (int column = 0; column < snapshot.getBoard().getSlots()[0].length; column++) {
                buffy.append("[");
                if (snapshot.getBoard().getSlots()[row][column] == GamePlayerType.PLAYER_ONE) {
                    buffy.append("X");
                } else if (snapshot.getBoard().getSlots()[row][column] == GamePlayerType.PLAYER_TWO) {
                    buffy.append("O");
                } else {
                    buffy.append(" ");
                }
                buffy.append("]");
            }
            buffy.append("\n");
        }
        buffy.append("X=" + snapshot.getPlayer1().getPlayerName() + " - O=" + snapshot.getPlayer2().getPlayerName());
        System.out.println(buffy.toString());
    }

    private void startGame() throws GameException {
        System.out.println("    ________  ______  ______________  _____  __ ");
        System.out.println("   / ____/\\ \\/ / __ \\/ ____/ ____/  |/  / / / / ");
        System.out.println("  / / __   \\  / / / / / __/ __/ / /|_/ / / / /  ");
        System.out.println(" / /_/ /   / / /_/ / /_/ / /___/ /  / / /_/ /   ");
        System.out.println(" \\____/   /_/\\____/\\____/_____/_/  /_/\\____/    ");
        while (true) {
            try {
                String name = getInput("Enter Your Name:");
                System.out.println("Joining Game ...");
                this.client.joinGame(name);
                return;
            } catch (GameException ge) {
                System.out.println(ge.getMessage());
            }
        }
    }

    private String getInput(String msg) {
        System.out.println(msg);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        try {
            input = reader.readLine();
        } catch (IOException ioe) {
            System.exit(1);
        }
        return input;
    }

    private void actOnInput(String input) throws GameException {
        if (input.equalsIgnoreCase("Q")) {
            System.out.println("Quitting...");
            this.client.quit();
            System.exit(0);
        } else if (input.matches("[0-9]")) {
            System.out.println("Taking Turn ...");
            try {
                this.client.takeTurn(Integer.parseInt(input));
            } catch (GameException ge) {
                System.out.println("Error:" + ge.getMessage());
            }
        } else {
            System.out.println("Unknown Command");
        }
    }

}
