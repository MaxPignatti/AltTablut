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

    private static List<Action> generateMovesForPawn(State state, int row, int col) throws IOException{
        List<Action> moves = new ArrayList<>();
        String from = state.getBox(row, col);

        // Movimento verso l'alto
        for (int i = row - 1; i >= 0; i--) {
            if (isValidMove(state, row, col, i, col)) {
                String to = state.getBox(i, col);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movimento verso il basso
        for (int i = row + 1; i < state.getBoard().length; i++) {
            if (isValidMove(state, row, col, i, col)) {
                String to = state.getBox(i, col);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movimento verso sinistra
        for (int j = col - 1; j >= 0; j--) {
            if (isValidMove(state, row, col, row, j)) {
                String to = state.getBox(row, j);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movimento verso destra
        for (int j = col + 1; j < state.getBoard().length; j++) {
            if (isValidMove(state, row, col, row, j)) {
                String to = state.getBox(row, j);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        return moves;
    }

    private static boolean isValidMove(State state, int rowFrom, int colFrom, int rowTo, int colTo) {
        State.Pawn toPawn = state.getPawn(rowTo, colTo);

        // La casella deve essere vuota
        if (!toPawn.equalsPawn("O")) {
            return false;
        }

        // Controlla se si sta attraversando altre pedine
        // (Questo controllo è già implicito nel ciclo di generateMovesForPawn)

        // Altre regole specifiche possono essere aggiunte qui

        return true;
    }
}