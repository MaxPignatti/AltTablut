package it.unibo.ai.didattica.competition.tablut.util;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActionsGenerator {

    public static List<Action> getLegalActions(State state) {
        List<Action> actions = new ArrayList<>();

        State.Pawn[][] board = state.getBoard();
        State.Turn player = state.getTurn();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                State.Pawn pawn = board[i][j];
                if ((player.equalsTurn("W") && (pawn.equalsPawn("W") || pawn.equalsPawn("K"))) ||
                        (player.equalsTurn("B") && pawn.equalsPawn("B"))) {
                    try {
                        actions.addAll(generateMovesForPawn(state, i, j));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return actions;
    }

    private static List<Action> generateMovesForPawn(State state, int row, int col) throws IOException {
        List<Action> moves = new ArrayList<>();
        String from = state.getBox(row, col);
        State.Pawn pawn = state.getPawn(row, col);

        // Movement upwards
        for (int i = row - 1; i >= 0; i--) {
            if (isValidMove(state, row, col, i, col, pawn)) {
                String to = state.getBox(i, col);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movement downwards
        for (int i = row + 1; i < state.getBoard().length; i++) {
            if (isValidMove(state, row, col, i, col, pawn)) {
                String to = state.getBox(i, col);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movement left
        for (int j = col - 1; j >= 0; j--) {
            if (isValidMove(state, row, col, row, j, pawn)) {
                String to = state.getBox(row, j);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movement right
        for (int j = col + 1; j < state.getBoard().length; j++) {
            if (isValidMove(state, row, col, row, j, pawn)) {
                String to = state.getBox(row, j);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        return moves;
    }

    private static boolean isValidMove(State state, int rowFrom, int colFrom, int rowTo, int colTo, State.Pawn pawn) {
        // Check if the path is clear
        if (!isPathClear(state, rowFrom, colFrom, rowTo, colTo)) {
            return false;
        }

        State.Pawn toPawn = state.getPawn(rowTo, colTo);

        // The destination must be empty
        if (!toPawn.equalsPawn(State.Pawn.EMPTY.toString())) {
            return false;
        }

        // Define escape squares
        int boardSize = state.getBoard().length;
        boolean isEscape = (rowTo == 0 || rowTo == boardSize - 1) && (colTo == 0 || colTo == boardSize - 1);

        // Only the king can move onto escape squares
        if (isEscape && !pawn.equalsPawn("K")) {
            return false;
        }

        // All pieces can move onto the throne square
        // No need to restrict movement onto the throne

        return true;
    }

    private static boolean isPathClear(State state, int rowFrom, int colFrom, int rowTo, int colTo) {
        int rowStep = Integer.compare(rowTo, rowFrom);
        int colStep = Integer.compare(colTo, colFrom);

        int currentRow = rowFrom + rowStep;
        int currentCol = colFrom + colStep;

        while (currentRow != rowTo || currentCol != colTo) {
            State.Pawn currentPawn = state.getPawn(currentRow, currentCol);
            if (!currentPawn.equalsPawn(State.Pawn.EMPTY.toString())) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }
}
