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

        // Minacce al re
        int threatsToKing = numberOfThreatsToKing(state, kingRow, kingCol);

        // Controllo delle uscite
        int escapesBlocked = numberOfEscapesBlocked(state);

        // Numero di vie di fuga aperte per il re
        int openEscapes = numberOfOpenEscapes(state, kingRow, kingCol);

        // Valutazione complessiva
        value += whitePawns * 120;          // Peso per le pedine bianche
        value -= blackPawns * 70;           // Peso per le pedine nere
        value += openEscapes * openEscapes * 700;         // Più vie di fuga aperte, meglio è per il bianco
        value -= threatsToKing * 700;       // Penalità per minacce al re
        value -= escapesBlocked * 150;      // Più uscite bloccate, peggio è per il bianco

        if(printEval){
            System.out.println("Evaluation Details:");
            System.out.printf("White Pawns: %d -> +%d%n", whitePawns, whitePawns * 120);
            System.out.printf("Black Pawns: %d -> -%d%n", blackPawns, blackPawns * 70);
            System.out.printf("Open Escapes: %d -> +%d%n", openEscapes, openEscapes * openEscapes * 700);
            System.out.printf("Threats to King: %d -> -%d%n", threatsToKing, threatsToKing * 700);
            System.out.printf("Escapes Blocked: %d -> -%d%n", escapesBlocked, escapesBlocked * 150);
            System.out.println("Total Evaluation Value: " + value);
        }

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

        System.out.println("King not found!");
        
        return null; // Il re non è stato trovato (non dovrebbe accadere)
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
            if (!pawn.equalsPawn(State.Pawn.EMPTY.toString()) || HeuristicUtils.isCamp(box)) {
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
                if (pawn.equalsPawn(State.Pawn.BLACK.toString()) || HeuristicUtils.isCamp(state.getBox(row, col)) ) {
                    threats++;
                }
            }
        }
        
        if (kingRow == 4 && kingCol == 4) {
            threats -= 2;
        } else if ((kingRow == 4 && kingCol == 5) || (kingRow == 5 && kingCol == 4) || (kingRow == 3 && kingCol == 4) || (kingRow == 4 && kingCol == 3)) {
            threats--;
        }
        if (threats < 0) {
            return 0;
        }
        return threats;
    }

    private int numberOfEscapesBlocked(State state) {
        int escapesBlocked = 0;

        for (int[] escape : HeuristicUtils.escapes) {
            State.Pawn pawn = state.getPawn(escape[0], escape[1]);
            if (pawn.equalsPawn(State.Pawn.BLACK.toString())) {
                escapesBlocked++;
            }
        }

        return escapesBlocked;
    }
}