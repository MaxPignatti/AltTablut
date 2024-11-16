package it.unibo.ai.didattica.competition.tablut.util;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActionsGenerator {

    // Definiamo le coordinate dei campi
    private static final Set<String> camps = new HashSet<>();

    static {
        // Coordinate dei campi
        camps.add("a4");
        camps.add("a5");
        camps.add("a6");
        camps.add("b5");
        camps.add("i4");
        camps.add("i5");
        camps.add("i6");
        camps.add("h5");
        camps.add("d1");
        camps.add("e1");
        camps.add("f1");
        camps.add("e2");
        camps.add("d9");
        camps.add("e9");
        camps.add("f9");
        camps.add("e8");
    }

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

        // Movimento verso l'alto
        for (int i = row - 1; i >= 0; i--) {
            if (isValidMove(state, row, col, i, col, pawn)) {
                String to = state.getBox(i, col);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movimento verso il basso
        for (int i = row + 1; i < state.getBoard().length; i++) {
            if (isValidMove(state, row, col, i, col, pawn)) {
                String to = state.getBox(i, col);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movimento verso sinistra
        for (int j = col - 1; j >= 0; j--) {
            if (isValidMove(state, row, col, row, j, pawn)) {
                String to = state.getBox(row, j);
                moves.add(new Action(from, to, state.getTurn()));
            } else {
                break;
            }
        }

        // Movimento verso destra
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
        // Controllo che il percorso sia libero
        if (!isPathClear(state, rowFrom, colFrom, rowTo, colTo)) {
            return false;
        }

        State.Pawn toPawn = state.getPawn(rowTo, colTo);

        // La destinazione deve essere vuota
        if (!toPawn.equalsPawn(State.Pawn.EMPTY.toString())) {
            return false;
        }

        String fromBox = state.getBox(rowFrom, colFrom);
        String toBox = state.getBox(rowTo, colTo);

        // Definiamo le caselle speciali
        boolean isThrone = isThrone(rowTo, colTo);
        boolean isCitadel = camps.contains(toBox);

        // Il trono è una barriera, nessuno può entrarci o attraversarlo (nemmeno il re una volta uscito)
        if (isThrone) {
            return false;
        }

        // I bianchi non possono entrare nei campi
        if (state.getTurn().equalsTurn("W") && isCitadel) {
            return false;
        }

        // I neri, una volta usciti dei campi, non possono rientrarci
        if (state.getTurn().equalsTurn("B")) {
            boolean fromCitadel = camps.contains(fromBox);
            if (!fromCitadel && isCitadel) {
                return false;
            }
        }

        // Nessuno può passare attraverso il trono o campi
        if (!isPathPassable(state, rowFrom, colFrom, rowTo, colTo, pawn)) {
            return false;
        }

        // Non è necessario un controllo speciale per le caselle di fuga (bordi), poiché qualsiasi pezzo può muoversi su di esse
        // L'unica restrizione è data dei campi, che sono già gestiti

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

    private static boolean isPathPassable(State state, int rowFrom, int colFrom, int rowTo, int colTo, State.Pawn pawn) {
        int rowStep = Integer.compare(rowTo, rowFrom);
        int colStep = Integer.compare(colTo, colFrom);

        int currentRow = rowFrom + rowStep;
        int currentCol = colFrom + colStep;

        while (currentRow != rowTo || currentCol != colTo) {
            String currentBox = state.getBox(currentRow, currentCol);

            // Controllo se la casella è il trono
            if (isThrone(currentRow, currentCol)) {
                return false;
            }

            // Controllo se la casella è un campo
            if (camps.contains(currentBox)) {
                // I bianchi non possono passare attraverso i campi
                if (state.getTurn().equalsTurn("W")) {
                    return false;
                }
                // I neri non possono rientrare nei campi una volta usciti
                if (state.getTurn().equalsTurn("B")) {
                    String fromBox = state.getBox(rowFrom, colFrom);
                    boolean fromCitadel = camps.contains(fromBox);
                    if (!fromCitadel) {
                        return false;
                    }
                }
            }

            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    private static boolean isThrone(int row, int col) {
        // Il trono si trova al centro della scacchiera
        int size = 9; // Dimensione della scacchiera
        return row == size / 2 && col == size / 2;
    }
}
