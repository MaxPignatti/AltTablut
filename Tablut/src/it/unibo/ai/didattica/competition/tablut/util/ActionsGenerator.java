package it.unibo.ai.didattica.competition.tablut.util;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class ActionsGenerator {

    // Definiamo le coordinate dei campi
    private static final Set<String> camps = new HashSet<>();
    private static final Map<String, Integer> campToGroup = new HashMap<>();

    static {
        // Coordinate dei campi e associazione ai gruppi
        // Gruppo 1
        camps.add("a4");
        camps.add("a5");
        camps.add("a6");
        camps.add("b5");
        campToGroup.put("a4", 1);
        campToGroup.put("a5", 1);
        campToGroup.put("a6", 1);
        campToGroup.put("b5", 1);

        // Gruppo 2
        camps.add("i4");
        camps.add("i5");
        camps.add("i6");
        camps.add("h5");
        campToGroup.put("i4", 2);
        campToGroup.put("i5", 2);
        campToGroup.put("i6", 2);
        campToGroup.put("h5", 2);

        // Gruppo 3
        camps.add("d1");
        camps.add("e1");
        camps.add("f1");
        camps.add("e2");
        campToGroup.put("d1", 3);
        campToGroup.put("e1", 3);
        campToGroup.put("f1", 3);
        campToGroup.put("e2", 3);

        // Gruppo 4
        camps.add("d9");
        camps.add("e9");
        camps.add("f9");
        camps.add("e8");
        campToGroup.put("d9", 4);
        campToGroup.put("e9", 4);
        campToGroup.put("f9", 4);
        campToGroup.put("e8", 4);
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
        int rowStep = Integer.compare(rowTo, rowFrom);
        int colStep = Integer.compare(colTo, colFrom);

        String fromBox = state.getBox(rowFrom, colFrom);
        String turn = state.getTurn().toString(); // "W" o "B"

        // Determiniamo se la posizione di partenza è in un campo e otteniamo il gruppo di campi
        Integer startingCampGroup = campToGroup.get(fromBox);

        int currentRow = rowFrom;
        int currentCol = colFrom;

        while (currentRow != rowTo || currentCol != colTo) {
            currentRow += rowStep;
            currentCol += colStep;
            State.Pawn currentPawn = state.getPawn(currentRow, currentCol);
            if (!currentPawn.equalsPawn(State.Pawn.EMPTY.toString())) {
                return false;
            }

            String currentBox = state.getBox(currentRow, currentCol);

            // Controllo se la casella corrente è il trono
            if (currentBox.equals("e5")) {
                return false; // Nessuno può passare attraverso il trono
            }

            boolean isCurrentBoxCamp = camps.contains(currentBox);

            if (isCurrentBoxCamp) {
                // La casella corrente è un campo
                if (turn.equals("W")) {
                    // I bianchi non possono passare attraverso i campi
                    return false;
                } else if (turn.equals("B")) {
                    // Pedina nera
                    if (startingCampGroup != null) {
                        // La pedina nera ha iniziato in un campo
                        Integer currentCampGroup = campToGroup.get(currentBox);
                        if (!startingCampGroup.equals(currentCampGroup)) {
                            // Non può entrare in un gruppo di campi diverso
                            return false;
                        }
                        // Altrimenti, stesso gruppo di campi: movimento consentito
                    } else {
                        // La pedina nera ha iniziato fuori dai campi: non può entrare in alcun campo
                        return false;
                    }
                }
            } 
        }
        return true;
    }
}
