package it.unibo.ai.didattica.competition.tablut.player.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import java.util.List;

public class BlackHeuristic implements Heuristic {

    @Override
    public double evaluate(State state) {
        // Se il Nero ha vinto
        if (state.getTurn().equalsTurn("BLACKWIN")) {
            return Double.POSITIVE_INFINITY;
        }

        // Se il Bianco ha vinto
        if (state.getTurn().equalsTurn("WHITEWIN")) {
            return Double.NEGATIVE_INFINITY;
        }

        double value = 0.0;

        // Numero di pedine nere e bianche
        int blackPawns = state.getNumberOf(Pawn.BLACK);
        int whitePawns = state.getNumberOf(Pawn.WHITE);

        // Posizione del re
        int[] kingPosition = findKing(state);
        int kingRow = kingPosition[0];
        int kingCol = kingPosition[1];

        // Distanza minima del re dalle uscite
        int kingDistance = minKingDistanceToEscape(state, kingRow, kingCol);

        // Numero di mosse possibili per il re
        int kingMobility = getKingMobility(state, kingRow, kingCol);

        // Minacce al re
        int threatsToKing = numberOfThreatsToKing(state, kingRow, kingCol);

        // Controllo delle uscite
        int escapesBlocked = numberOfEscapesBlocked(state);

        // Valutazione complessiva
        value += blackPawns * 100;            // Peso per le pedine nere
        value -= whitePawns * 50;             // Peso per le pedine bianche
        value += (8 - kingDistance) * 100;    // Più lontano dall'uscita, meglio è
        value -= kingMobility * 50;           // Meno mobilità per il re, meglio è
        value += threatsToKing * 500;         // Bonus per minacce al re
        value += escapesBlocked * 200;        // Bonus per uscite bloccate

        return value;
    }

    private int[] findKing(State state) {
        int[] position = new int[2];
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard()[i].length; j++) {
                if (state.getPawn(i, j).equalsPawn("K")) {
                    position[0] = i;
                    position[1] = j;
                    return position;
                }
            }
        }
        return null; // Il re non è stato trovato (non dovrebbe accadere)
    }

    private int minKingDistanceToEscape(State state, int kingRow, int kingCol) {
        int minDistance = Integer.MAX_VALUE;
        int size = state.getBoard().length;

        // Le posizioni delle uscite sono le caselle sul bordo non occupate dalle citadelle
        List<int[]> escapePositions = HeuristicUtils.getEscapePositions(state);

        for (int[] escape : escapePositions) {
            int distance = Math.abs(kingRow - escape[0]) + Math.abs(kingCol - escape[1]);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        return minDistance;
    }

    private int getKingMobility(State state, int kingRow, int kingCol) {
        int mobility = 0;
        int size = state.getBoard().length;

        // Movimento verso l'alto
        for (int i = kingRow - 1; i >= 0; i--) {
            if (isCellFree(state, i, kingCol)) {
                mobility++;
            } else {
                break;
            }
        }

        // Movimento verso il basso
        for (int i = kingRow + 1; i < size; i++) {
            if (isCellFree(state, i, kingCol)) {
                mobility++;
            } else {
                break;
            }
        }

        // Movimento verso sinistra
        for (int j = kingCol - 1; j >= 0; j--) {
            if (isCellFree(state, kingRow, j)) {
                mobility++;
            } else {
                break;
            }
        }

        // Movimento verso destra
        for (int j = kingCol + 1; j < size; j++) {
            if (isCellFree(state, kingRow, j)) {
                mobility++;
            } else {
                break;
            }
        }

        return mobility;
    }

    private boolean isCellFree(State state, int row, int col) {
        State.Pawn pawn = state.getPawn(row, col);
        String box = state.getBox(row, col);

        return pawn.equalsPawn(State.Pawn.EMPTY.toString()) && !HeuristicUtils.isCitadel(box) && !HeuristicUtils.isThrone(row, col);
    }

    private int numberOfThreatsToKing(State state, int kingRow, int kingCol) {
        int threats = 0;
        int size = state.getBoard().length;

        // Controlla le caselle adiacenti al re
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] dir : directions) {
            int row = kingRow + dir[0];
            int col = kingCol + dir[1];
            if (row >= 0 && row < size && col >= 0 && col < size) {
                State.Pawn pawn = state.getPawn(row, col);
                if (pawn.equalsPawn(State.Pawn.BLACK.toString())) {
                    threats++;
                }
            }
        }

        return threats;
    }

    private int numberOfEscapesBlocked(State state) {
        int escapesBlocked = 0;

        // Ottieni le posizioni delle uscite
        List<int[]> escapePositions = HeuristicUtils.getEscapePositions(state);

        for (int[] escape : escapePositions) {
            State.Pawn pawn = state.getPawn(escape[0], escape[1]);
            if (pawn.equalsPawn(State.Pawn.BLACK.toString())) {
                escapesBlocked++;
            }
        }

        return escapesBlocked;
    }
}
