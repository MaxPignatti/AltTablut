package it.unibo.ai.didattica.competition.tablut.player.search;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.player.game.*;
import it.unibo.ai.didattica.competition.tablut.player.search.heuristics.*;
import it.unibo.ai.didattica.competition.tablut.util.*;
import java.util.*;


public class IterativeDeepeningAlphaBetaSearch {

    private final Heuristic heuristic;
    private long timeLimit;
    private long startTime;
    private int playerCoefficient;
    private int currentDepthLimit;

    // Move Ordering: memorizza la migliore mossa trovata per stato
    private Map<String, Action> bestMoves; // QUI DOVREMMO ORDINARE IN BASE ALL'ULTIMA EVALUATION

    public IterativeDeepeningAlphaBetaSearch(String player) {
        super();
        player = player.toUpperCase();
        if (player.equals("WHITE")) {
            
            playerCoefficient = -1;
        } else if (player.equals("BLACK")) {
            playerCoefficient = 1;
        }

        this.heuristic = new Heuristic();

        this.bestMoves = new HashMap<>();
    }

    public Action makeDecision(State state, long startTime, long timeLimit) {
        this.startTime = startTime;
        this.timeLimit = timeLimit;

        Action bestAction = null;

        this.currentDepthLimit = 1;

        List<Action> actions = getLegalActions(state);

        
        while (System.currentTimeMillis() - startTime < timeLimit) {

            try {
                bestAction = alphaBetaSearch(state, actions, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

                System.out.println("Profondità: " + this.currentDepthLimit + " - Azione: " + bestAction + " - Valutazione: " + heuristic.evaluate(applyAction(state, bestAction)));
                
                // Salva la migliore mossa per lo stato attuale
                bestMoves.put(state.toString(), bestAction);
            } catch (TimeOutException e) {
                System.out.println("Timeout alla profondità " + this.currentDepthLimit);
                break;
            }
            this.currentDepthLimit++;
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

    private Action alphaBetaSearch(State state, List<Action> actions, double alpha, double beta) throws TimeOutException {

        Action bestAction = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Action action : actions) {
            checkTime();

            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue; // Salta azioni non valide
            }

            double value = minValue(nextState, 1, alpha, beta);

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

        if (isTerminal(state) || depth > this.currentDepthLimit) {
            return playerCoefficient * heuristic.evaluate(state);
        }

        double value = Double.NEGATIVE_INFINITY;

        List<Action> actions = getLegalActions(state);

        for (Action action : actions) {
            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue; // Salta azioni non valide
            }
            value = Math.max(value, minValue(nextState, depth + 1, alpha, beta));
            // alpha = Math.max(alpha, value);
            // if (alpha >= beta) {
            //     break; // Potatura beta
            // }

            if (value >= beta) {
                return value;
            }
        }

        return value;
    }

    private double minValue(State state, int depth, double alpha, double beta) throws TimeOutException {
        checkTime();

        if (isTerminal(state) || depth > this.currentDepthLimit) {
            return playerCoefficient * heuristic.evaluate(state);
        }

        double value = Double.POSITIVE_INFINITY;

        List<Action> actions = getLegalActions(state);

        for (Action action : actions) {
            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue; // Salta azioni non valide
            }
            value = Math.min(value, maxValue(nextState, depth + 1, alpha, beta));
            // beta = Math.min(beta, value);
            // if (alpha >= beta) {
            //     break; // Potatura alpha
            // }

            if (value <= alpha) {
                return value;
            }
        }

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

}