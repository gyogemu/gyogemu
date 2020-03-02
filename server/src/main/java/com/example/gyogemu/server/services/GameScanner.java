package com.example.gyogemu.server.services;

import com.example.gyogemu.common.dto.GamePlayerType;
import com.example.gyogemu.common.dto.GameSnapshot;
import org.springframework.stereotype.Component;

@Component
public class GameScanner {

    /**
     * Determines if the board has all slots filled
     * @param snapshot the game to scan
     * @return true if full, false otherwise
     */
    public boolean isBoardFull(GameSnapshot snapshot) {
        for (int row = 0; row < snapshot.getBoard().getSlots().length; row++) {
            for (int column = 0; column < snapshot.getBoard().getSlots()[0].length; column++) {
                if (snapshot.getBoard().getSlots()[row][column] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Scans the game board for a victory condition
     * @param snapshot the game to scan
     * @return true if game is in a winning state
     */
    public boolean scan(GameSnapshot snapshot) {
        for (int row = 0; row < snapshot.getBoard().getSlots().length; row++) {
            for (int column = 0; column < snapshot.getBoard().getSlots()[0].length; column++) {
                if (snapshot.getBoard().getSlots()[row][column] != null) {
                    if (scan(snapshot, row, column)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean scan(GameSnapshot snapshot, int targetRow, int targetColumn) {
        return verticalScan(snapshot, targetRow, targetColumn) ||
                horizontalScan(snapshot, targetRow, targetColumn) ||
                diagonalLeftScan(snapshot, targetRow, targetColumn) ||
                diagonalRightScan(snapshot, targetRow, targetColumn);
    }

    private boolean verticalScan(GameSnapshot snapshot, int targetRow, int targetColumn) {
        return scan(snapshot, targetRow, targetColumn, 0, 1);
    }

    private boolean horizontalScan(GameSnapshot snapshot, int targetRow, int targetColumn) {
        return scan(snapshot, targetRow, targetColumn, 1, 0);
    }

    private boolean diagonalLeftScan(GameSnapshot snapshot, int targetRow, int targetColumn) {
        return scan(snapshot, targetRow, targetColumn, 1, -1);
    }

    private boolean diagonalRightScan(GameSnapshot snapshot, int targetRow, int targetColumn) {
        return scan(snapshot, targetRow, targetColumn, 1, 1);
    }

    private boolean scan(GameSnapshot snapshot, int targetRow, int targetColumn, int rowIncrement, int columnIncrement) {
        GamePlayerType targetSlot = snapshot.getBoard().getSlots()[targetRow][targetColumn];
        int hits = 0;
        int row = targetRow;
        int column = targetColumn;
        while (hits < 5 && !(row >= snapshot.getBoard().getSlots().length || row < 0 || column >= snapshot.getBoard().getSlots()[0].length || column < 0)) {
            if (!(snapshot.getBoard().getSlots()[row][column] == targetSlot)) {
                break;
            }
            hits++;
            row += rowIncrement;
            column += columnIncrement;
        }
        return hits == 5;
    }
}
