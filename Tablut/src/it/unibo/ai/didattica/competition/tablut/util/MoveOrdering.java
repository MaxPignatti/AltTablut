package it.unibo.ai.didattica.competition.tablut.util;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class MoveOrdering {

    public static List<Action> orderMoves(State state, List<Action> actions) {
        // Implementazione dell'ordinamento delle mosse
        // Ad esempio, ordina le mosse che catturano per prime

        Collections.sort(actions, new Comparator<Action>() {
            @Override
            public int compare(Action a1, Action a2) {
                // Valuta le azioni in base a una funzione di priorità
                // Questo è un esempio semplificato
                return 0;
            }
        });

        return actions;
    }
}