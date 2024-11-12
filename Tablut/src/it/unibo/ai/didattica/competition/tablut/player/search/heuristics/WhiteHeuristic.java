package it.unibo.ai.didattica.competition.tablut.player.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class WhiteHeuristic implements Heuristic {

    @Override
    public double evaluate(State state) {
        // Se il Bianco ha vinto
        if (state.getTurn().equalsTurn("WHITEWIN")) {
            return Double.POSITIVE_INFINITY;
        }

        // Se il Nero ha vinto
        if (state.getTurn().equalsTurn("BLACKWIN")) {
            return Double.NEGATIVE_INFINITY;
        }

        double value = 0.0;

        // Numero di pedine bianche
        int whitePawns = state.getNumberOf(Pawn.WHITE);

        // Numero di pedine nere
        int blackPawns = state.getNumberOf(Pawn.BLACK);

        // Distanza del re dalle uscite
        int kingDistance = evaluateKingDistance(state);

        // Valutazione complessiva
        value += whitePawns * 100;
        value -= blackPawns * 50;
        value += (8 - kingDistance) * 200;

        return value;
    }

    private int evaluateKingDistance(State state) {
        int kingRow = -1;
        int kingCol = -1;
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard()[i].length; j++) {
                if (state.getPawn(i, j).equalsPawn("K")) {
                    kingRow = i;
                    kingCol = j;
                    break;
                }
            }
        }
        int distanceTop = kingRow;
        int distanceBottom = 8 - kingRow;
        int distanceLeft = kingCol;
        int distanceRight = 8 - kingCol;

        return Math.min(Math.min(distanceTop, distanceBottom), Math.min(distanceLeft, distanceRight));
    }
}