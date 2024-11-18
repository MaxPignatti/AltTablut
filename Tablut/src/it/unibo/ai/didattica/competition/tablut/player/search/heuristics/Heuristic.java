package it.unibo.ai.didattica.competition.tablut.player.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import java.util.List;

public class Heuristic {

    public Heuristic() {
        super();
    }

    public double evaluate(State state, boolean printEval) {
        // Se il Bianco ha vinto
        if (state.getTurn().equalsTurn("WW")) {
            return Double.POSITIVE_INFINITY;
        }

        // Se il Nero ha vinto
        if (state.getTurn().equalsTurn("BW")) {
            return Double.NEGATIVE_INFINITY;
        }

        double value = 0.0;

        // Numero di pedine bianche e nere
        int whitePawns = state.getNumberOf(Pawn.WHITE);
        int blackPawns = state.getNumberOf(Pawn.BLACK);

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

        // Controllo del centro
        int centerControl = countWhiteInCenter(state);

        // Numero di vie di fuga aperte per il re
        int openEscapes = numberOfOpenEscapes(state, kingRow, kingCol);

        int blackNearKing = blackPawnsNearKing(state, kingRow, kingCol);

        // Valutazione complessiva
        value += whitePawns * 100;          // Peso per le pedine bianche
        value -= blackPawns * 60;           // Peso per le pedine nere
        value -= kingDistance * 50;         // Più vicino all'uscita, meglio è per il bianco
        value += openEscapes * 300;         // Più vie di fuga aperte, meglio è per il bianco
        value += kingMobility * 50;         // Più mobilità per il re, meglio è per il bianco
        value += centerControl * 30;        // Controllo del centro
        value -= threatsToKing * 500;       // Penalità per minacce al re
        value -= escapesBlocked * 200;      // Più uscite bloccate, peggio è per il bianco
        value -= blackNearKing * 200;       // Penalize based on the number of black pawns near the king

        if(printEval){
            System.out.println("Evaluation Details:");
            System.out.printf("White Pawns: %d -> +%d%n", whitePawns, whitePawns * 100);
            System.out.printf("Black Pawns: %d -> -%d%n", blackPawns, blackPawns * 60);
            System.out.printf("King Distance: %d -> -%d%n", kingDistance, kingDistance * 50);
            System.out.printf("Open Escapes: %d -> +%d%n", openEscapes, openEscapes * 300);
            System.out.printf("King Mobility: %d -> +%d%n", kingMobility, kingMobility * 50);
            System.out.printf("Center Control: %d -> +%d%n", centerControl, centerControl * 30);
            System.out.printf("Threats to King: %d -> -%d%n", threatsToKing, threatsToKing * 500);
            System.out.printf("Escapes Blocked: %d -> -%d%n", escapesBlocked, escapesBlocked * 200);
            System.out.printf("Black Near King: %d -> -%d%n", blackNearKing, blackNearKing * 200);
            System.out.println("Total Evaluation Value: " + value);
        }

        return value;
    }

    private int blackPawnsNearKing(State state, int kingRow, int kingCol) {
        int count = 0;
        // Check for black pawns within a certain radius of the king
        for (int i = kingRow - 2; i <= kingRow + 2; i++) {
            for (int j = kingCol - 2; j <= kingCol + 2; j++) {
                if (isValidPosition(state, i, j)) {
                    State.Pawn pawn = state.getPawn(i, j);
                    if (pawn.equalsPawn(State.Pawn.BLACK.toString())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    private boolean isValidPosition(State state, int row, int col) {
        int size = state.getBoard().length;
        return row >= 0 && row < size && col >= 0 && col < size;
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

        System.out.println("King not found!");
        
        return null; // Il re non è stato trovato (non dovrebbe accadere)
    }

    private int minKingDistanceToEscape(State state, int kingRow, int kingCol) {
        int minDistance = Integer.MAX_VALUE;

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

    private int numberOfOpenEscapes(State state, int kingRow, int kingCol) {
        int openEscapes = 0;

        // Controlla in ogni direzione se il re ha una via libera fino a un'uscita
        if (isEscapePathClear(state, kingRow, kingCol, -1, 0)) { // Su
            openEscapes++;
        }
        if (isEscapePathClear(state, kingRow, kingCol, 1, 0)) { // Giù
            openEscapes++;
        }
        if (isEscapePathClear(state, kingRow, kingCol, 0, -1)) { // Sinistra
            openEscapes++;
        }
        if (isEscapePathClear(state, kingRow, kingCol, 0, 1)) { // Destra
            openEscapes++;
        }

        return openEscapes;
    }

    private boolean isEscapePathClear(State state, int row, int col, int rowDir, int colDir) {
        int size = state.getBoard().length;
        int currentRow = row + rowDir;
        int currentCol = col + colDir;

        while (currentRow >= 0 && currentRow < size && currentCol >= 0 && currentCol < size) {
            State.Pawn pawn = state.getPawn(currentRow, currentCol);
            String box = state.getBox(currentRow, currentCol);

            // Se incontriamo un pezzo o una casella proibita, il percorso non è libero
            if (!pawn.equalsPawn(State.Pawn.EMPTY.toString()) || HeuristicUtils.isCamp(box) || HeuristicUtils.isThrone(currentRow, currentCol)) {
                return false;
            }

            // Se siamo su una casella di fuga, il percorso è libero
            if (HeuristicUtils.isEscape(box)) {
                return true;
            }

            currentRow += rowDir;
            currentCol += colDir;
        }

        return false;
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

        return pawn.equalsPawn(State.Pawn.EMPTY.toString()) && !HeuristicUtils.isCamp(box) && !HeuristicUtils.isThrone(row, col);
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

    private int countWhiteInCenter(State state) {
        int centerControl = 0;
        int size = state.getBoard().length;
        int center = size / 2;

        // Consideriamo le caselle intorno al trono
        int[][] positions = {
            {center-1, center},
            {center+1, center},
            {center, center-1},
            {center, center+1}
        };

        for (int[] pos : positions) {
            State.Pawn pawn = state.getPawn(pos[0], pos[1]);
            if (pawn.equalsPawn(State.Pawn.WHITE.toString())) {
                centerControl++;
            }
        }

        return centerControl;
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