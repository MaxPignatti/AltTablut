package it.unibo.ai.didattica.competition.tablut.player.search;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.player.search.heuristics.*;
import it.unibo.ai.didattica.competition.tablut.util.*;
import java.util.List;

public class IterativeDeepeningAlphaBetaSearch {

    private final Heuristic heuristic;
    private long timeLimit;
    private long startTime;

    public IterativeDeepeningAlphaBetaSearch(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    public Action makeDecision(State state, long startTime, long timeLimit) {
        this.startTime = startTime;
        this.timeLimit = timeLimit;

        Action bestAction = null;
        int depth = 1;

        while (System.currentTimeMillis() - startTime < timeLimit) {
            System.out.println("Depth: " + depth);
            try {
                bestAction = alphaBetaSearch(state, depth);
                System.out.println("Best action: " + bestAction);
                depth++;
            } catch (TimeOutException e) {
                System.out.println("Tempo scaduto alla profondità " + depth);
                break;
            }
        }

        return bestAction;
    }

    private Action alphaBetaSearch(State state, int depth) throws TimeOutException {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        Action bestAction = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        List<Action> actions = getLegalActions(state);
        System.out.println("Legal actions : " + actions);

        System.out.println(actions.size());
        actions = MoveOrdering.orderMoves(state, actions);
        System.out.println(actions.size());


        for (Action action : actions) {
            checkTime();
            State nextState = applyAction(state, action);
            double value = minValue(nextState, depth - 1, alpha, beta);
            if (value > bestValue) {
                bestValue = value;
                bestAction = action;
            }
            alpha = Math.max(alpha, bestValue);
        }

        return bestAction;
    }

    private double maxValue(State state, int depth, double alpha, double beta) throws TimeOutException {
        checkTime();
        if (isTerminal(state) || depth == 0) {
            return heuristic.evaluate(state);
        }
        double value = Double.NEGATIVE_INFINITY;
        List<Action> actions = getLegalActions(state);
        actions = MoveOrdering.orderMoves(state, actions);

        for (Action action : actions) {
            State nextState = applyAction(state, action);
            value = Math.max(value, minValue(nextState, depth - 1, alpha, beta));
            if (value >= beta) {
                return value;
            }
            alpha = Math.max(alpha, value);
        }
        return value;
    }

    private double minValue(State state, int depth, double alpha, double beta) throws TimeOutException {
        checkTime();
        if (isTerminal(state) || depth == 0) {
            return heuristic.evaluate(state);
        }
        double value = Double.POSITIVE_INFINITY;
        List<Action> actions = getLegalActions(state);
        actions = MoveOrdering.orderMoves(state, actions);

        for (Action action : actions) {
            State nextState = applyAction(state, action);
            value = Math.min(value, maxValue(nextState, depth - 1, alpha, beta));
            if (value <= alpha) {
                return value;
            }
            beta = Math.min(beta, value);
        }
        return value;
    }

    private void checkTime() throws TimeOutException {
        if (System.currentTimeMillis() - startTime >= timeLimit) {
            throw new TimeOutException("Tempo scaduto");
        }
    }

    private List<Action> getLegalActions(State state) {
        // Implementazione della generazione delle mosse legali
        return ActionsGenerator.getLegalActions(state);
    }

    private State applyAction(State state, Action action) {
        // Applicazione dell'azione allo stato corrente
        State newState = state.clone();
        GameAshtonTablut rules = new GameAshtonTablut(99, 0, "logs", "white", "black");
        try {
            newState = rules.checkMove(newState, action);
        } catch (Exception e) {
            // Gestione delle eccezioni, ad esempio mossa illegale
        }
        return newState;
    }

    private boolean isTerminal(State state) {
        // Verifica se lo stato è terminale
        return state.getTurn().equalsTurn("WHITEWIN") ||
               state.getTurn().equalsTurn("BLACKWIN") ||
               state.getTurn().equalsTurn("DRAW");
    }
}