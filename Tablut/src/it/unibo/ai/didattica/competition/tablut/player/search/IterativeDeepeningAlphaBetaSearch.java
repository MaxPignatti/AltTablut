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

    private Map<String, Action> bestMoves;

    private Map<Long, TranspositionTableEntry> transpositionTable;

    private static class TranspositionTableEntry {
        public double value;
        public int depth;

        public TranspositionTableEntry(double value, int depth) {
            this.value = value;
            this.depth = depth;
        }
    }

    private long[] zobristTable;

    public IterativeDeepeningAlphaBetaSearch(String player) {
        super();
        player = player.toUpperCase();
        if (player.equals("WHITE")) {
            playerCoefficient = 1;
        } else if (player.equals("BLACK")) {
            playerCoefficient = -1;
        }

        this.heuristic = new Heuristic();
        this.bestMoves = new HashMap<>();
        this.transpositionTable = new HashMap<>();
    }

    private void initializeZobristTable(int boardSize) {
        Random rand = new Random(0);
        int numPieces = 4;
        zobristTable = new long[boardSize * boardSize * numPieces];
        for (int i = 0; i < zobristTable.length; i++) {
            zobristTable[i] = rand.nextLong();
        }
    }
    

    private long computeZobristHash(State state) {
        long h = 0;
        int size = state.getBoard().length;
        int numPieces = 4;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                State.Pawn pawn = state.getPawn(i, j);
                int piece = 0;
                if (pawn.equalsPawn(State.Pawn.WHITE.toString())) {
                    piece = 1;
                } else if (pawn.equalsPawn(State.Pawn.BLACK.toString())) {
                    piece = 2;
                } else if (pawn.equalsPawn(State.Pawn.KING.toString())) {
                    piece = 3;
                }
                h ^= zobristTable[(i * size + j) * numPieces + piece];
            }
        }
        return h;
    }
    

    public Action makeDecision(State state, long startTime, long timeLimit) {
        this.startTime = startTime;
        this.timeLimit = timeLimit;

        Action bestAction = null;

        this.currentDepthLimit = 1;

        List<Action> actions = getLegalActions(state);

        initializeZobristTable(state.getBoard().length);

        while (System.currentTimeMillis() - startTime < timeLimit) {

            try {

                bestAction = alphaBetaSearch(state, actions, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                
                System.out.println("Profondità: " + this.currentDepthLimit + " - Azione: " + bestAction + " - Valutazione: " + heuristic.evaluate(applyAction(state, bestAction), false));
                heuristic.evaluate(applyAction(state, bestAction), true); // QUESTO È FATTO SOLO PER STAMPARE I VALORI DELL'EVAL

                bestMoves.put(state.toString(), bestAction);

            } catch (TimeOutException e) {
                System.out.println("Timeout alla profondità " + this.currentDepthLimit);
                break;
            } catch (Exception e) {
                e.printStackTrace();
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

        actions = addBestMove(state, actions);

        for (Action action : actions) {
            checkTime();

            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue; // Salta azioni non valide
            }

            double value = minValue(nextState, 1, alpha, beta);

            if (value > bestValue) { 
                bestValue = value;
                bestAction = action;
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
    
        long zobristHash = computeZobristHash(state);
        
        TranspositionTableEntry entry = transpositionTable.get(zobristHash);

        if (entry != null && entry.depth >= depth) {
            System.out.println("Transposition Table hit: " + entry.value + "con Hash: " +  zobristHash);
            return entry.value;
        }
    
        if (isTerminal(state) || depth >= this.currentDepthLimit) {
            double eval = playerCoefficient * heuristic.evaluate(state, false);
            storeInTranspositionTable(zobristHash, eval, depth);
            return eval;
        }
    
        double value = Double.NEGATIVE_INFINITY;
        List<Action> actions = getLegalActions(state);
    
        for (Action action : actions) {
            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue;
            }
    
            value = Math.max(value, minValue(nextState, depth + 1, alpha, beta));
            alpha = Math.max(alpha, value);
            if (alpha >= beta) {
                break; // Beta cut-off
            }
        }
        
        storeInTranspositionTable(zobristHash, value, depth);
    
        return value;
    }
    
    private double minValue(State state, int depth, double alpha, double beta) throws TimeOutException {
        checkTime();
    
        long zobristHash = computeZobristHash(state);
        
        TranspositionTableEntry entry = transpositionTable.get(zobristHash);
        
        if (entry != null && entry.depth >= depth) {
            System.out.println("Transposition Table hit: " + entry + "con Hash: " +  zobristHash);
            return entry.value;
        }   
    
        if (isTerminal(state) || depth >= this.currentDepthLimit) {
            double eval = playerCoefficient * heuristic.evaluate(state, false);
            storeInTranspositionTable(zobristHash, eval, depth);
            return eval;
        }
    
        double value = Double.POSITIVE_INFINITY;
        List<Action> actions = getLegalActions(state);
    
        for (Action action : actions) {
            State nextState = applyAction(state, action);
            if (nextState == null) {
                continue;
            }
    
            value = Math.min(value, maxValue(nextState, depth + 1, alpha, beta));
            beta = Math.min(beta, value);
            if (alpha >= beta) {
                break;
            }
        }
    
        storeInTranspositionTable(zobristHash, value, depth);
    
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
        // OurGameAshtonTablut rules = new OurGameAshtonTablut("white", "black");
        try {
            State newState = OurGameAshtonTablut.checkMove(state.clone(), action);
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

    private List<Action> addBestMove(State state, List<Action> actions) {
        Action bestMove = bestMoves.get(state.toString());
        if (bestMove != null && actions.contains(bestMove)) {
            actions.remove(bestMove);
            actions.add(0, bestMove);
        }
        return actions;
    }
    
    private void storeInTranspositionTable(long zobristHash, double value, int depth) {
        TranspositionTableEntry entry = transpositionTable.get(zobristHash);
        if (entry == null || entry.depth < depth) {
            transpositionTable.put(zobristHash, new TranspositionTableEntry(value, depth));
        }
    }    

}