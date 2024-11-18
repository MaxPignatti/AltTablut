package it.unibo.ai.didattica.competition.tablut.player.search;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.player.game.*;
import it.unibo.ai.didattica.competition.tablut.player.search.heuristics.*;
import it.unibo.ai.didattica.competition.tablut.util.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IterativeDeepeningAlphaBetaSearch {

    private final Heuristic heuristic;
    private long timeLimit;
    private long startTime;
    private int playerCoefficient;

    // Tabella di trasposizione per memorizzare gli stati già valutati
    private Map<String, Double> transpositionTable;

    // Move Ordering: memorizza la migliore mossa trovata per stato
    private Map<String, Action> bestMoves; // QUI DOVREMMO ORDINARE IN BASE ALL'ULTIMA EVALUATION

    public IterativeDeepeningAlphaBetaSearch(String player) {
        super();
        
        if (player.equals("WHITE")) {
            playerCoefficient = 1;
        } else if (player.equals("BLACK")) {
            playerCoefficient = -1;
        }

        this.heuristic = new Heuristic();

        this.transpositionTable = new ConcurrentHashMap<>();
        this.bestMoves = new HashMap<>();
    }

    public Action makeDecision(State state, long startTime, long timeLimit) {
        this.startTime = startTime;
        this.timeLimit = timeLimit;

        Action bestAction = null;

        int depth = 1;

        List<Action> actions = getLegalActions(state);

        while (System.currentTimeMillis() - startTime < timeLimit) {
            
            try {
                bestAction = alphaBetaSearch(state, actions, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                
                // Salva la migliore mossa per lo stato attuale
                bestMoves.put(state.toString(), bestAction);
            } catch (TimeOutException e) {
                System.out.println("Timeout alla profondità " + depth);
                break;
            }
            depth++;
        }

        // QUI IN TEORIA NON DOVREBBE ENTRARCI
        if (bestAction == null) {
            
            System.out.println("WARNING:    bestAction == null ");
            
            if (!actions.isEmpty()) {
                bestAction = actions.get(0);
                System.out.println("Nessuna azione trovata, selezionata azione predefinita: " + bestAction);
            } else {
                // Nessuna azione legale disponibile
                System.out.println("Nessuna azione legale disponibile");
                return null;
            }
        }

        return bestAction;
    }

    private Action alphaBetaSearch(State state, List<Action> actions, int depth, double alpha, double beta) throws TimeOutException {

        // Move Ordering: ordina le mosse utilizzando la migliore mossa precedente
        actions = orderMoves(state, actions);

        Action bestAction = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Action action : actions) {
            checkTime();

            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue; // Salta azioni non valide
            }

            double value = minValue(nextState, depth - 1, alpha, beta);

            if (value > bestValue) { // GIUSTO METTERE > OPPURE SERVE < ???
                bestValue = value;
                bestAction = action;

                // Aggiorna la migliore mossa per il Move Ordering
                bestMoves.put(state.toString(), bestAction);
            }

            alpha = Math.max(alpha, bestValue);
            if (alpha >= beta) {
                break; // Potatura beta
            }
        }

        return bestAction;
    }

    private double maxValue(State state, int depth, double alpha, double beta) throws TimeOutException {
        checkTime();

        if (isTerminal(state) || depth == 0) {
            return playerCoefficient * heuristic.evaluate(state);
        }

        String stateKey = state.toString();

        // Controlla la tabella di trasposizione
        if (transpositionTable.containsKey(stateKey)) {
            return transpositionTable.get(stateKey);
        }

        double value = Double.NEGATIVE_INFINITY;

        List<Action> actions = getLegalActions(state);

        // Move Ordering: ordina le mosse utilizzando la migliore mossa precedente
        actions = orderMoves(state, actions);

        for (Action action : actions) {
            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue; // Salta azioni non valide
            }
            value = Math.max(value, minValue(nextState, depth - 1, alpha, beta));
            alpha = Math.max(alpha, value);
            if (alpha >= beta) {
                break; // Potatura beta
            }
        }

        transpositionTable.put(stateKey, value); // Memorizza il valore valutato
        return value;
    }

    private double minValue(State state, int depth, double alpha, double beta) throws TimeOutException {
        checkTime();

        if (isTerminal(state) || depth == 0) {
            return playerCoefficient * heuristic.evaluate(state);
        }

        String stateKey = state.toString();

        // Controlla la tabella di trasposizione
        if (transpositionTable.containsKey(stateKey)) {
            return transpositionTable.get(stateKey);
        }

        double value = Double.POSITIVE_INFINITY;

        List<Action> actions = getLegalActions(state);

        // Move Ordering: ordina le mosse utilizzando la migliore mossa precedente
        actions = orderMoves(state, actions);

        for (Action action : actions) {
            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue; // Salta azioni non valide
            }
            value = Math.min(value, maxValue(nextState, depth - 1, alpha, beta));
            beta = Math.min(beta, value);
            if (alpha >= beta) {
                break; // Potatura alpha
            }
        }

        transpositionTable.put(stateKey, value); // Memorizza il valore valutato
        return value;
    }

    private void checkTime() throws TimeOutException {
        if (System.currentTimeMillis() - startTime >= timeLimit) {
            throw new TimeOutException("Tempo limite superato");
        }
    }

    private List<Action> getLegalActions(State state) {
        return ActionsGenerator.getLegalActions(state);
    }

    private State applyAction(State state, Action action) {
        OurGameAshtonTablut rules = new OurGameAshtonTablut("white", "black"); // TODO: make everything static
        try {
            State newState = rules.checkMove(state.clone(), action);
            return newState;
        } catch (Exception e) {
            // Mossa non valida, restituisce null per indicare che l'azione non può essere applicata
            return null;
        }
    }

    private boolean isTerminal(State state) {
        return state.getTurn().equalsTurn(State.Turn.WHITEWIN.toString()) ||
                state.getTurn().equalsTurn(State.Turn.BLACKWIN.toString()) ||
                state.getTurn().equalsTurn(State.Turn.DRAW.toString());
    }

    // SPOSTA IN CIMA SOLO LA MOSSA MIGLIORE
    private List<Action> orderMoves(State state, List<Action> actions) {
        Action bestMove = bestMoves.get(state.toString());
        if (bestMove != null && actions.contains(bestMove)) {
            actions.remove(bestMove);
            actions.add(0, bestMove);
        }
        return actions;
    }
}