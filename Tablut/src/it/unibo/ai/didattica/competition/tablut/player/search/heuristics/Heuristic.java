package it.unibo.ai.didattica.competition.tablut.player.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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

        // Valutazione complessiva
        
        value += whitePawns * 150;          // Peso per le pedine bianche
        value -= blackPawns * 84.375;           // Peso per le pedine nere

        if(printEval){
            System.out.println("Evaluation Details:");
            System.out.printf("White Pawns: %d -> +%d%n", whitePawns, whitePawns * 150);
            System.out.printf("Black Pawns: %d -> -%d%n", blackPawns, blackPawns * 84);
            System.out.println("Total Evaluation Value: " + value);
        }

        return value;
    }
    
}