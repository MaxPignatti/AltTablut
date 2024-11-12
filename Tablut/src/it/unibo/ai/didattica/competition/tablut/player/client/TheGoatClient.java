package it.unibo.ai.didattica.competition.tablut.player.client;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.player.search.heuristics.*;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.player.search.IterativeDeepeningAlphaBetaSearch;
import java.io.IOException;

public class TheGoatClient extends TablutClient {

    private IterativeDeepeningAlphaBetaSearch searchAlgorithm;
    private Heuristic heuristic;
    private int timeout;

    public TheGoatClient(String player, String name, int timeout, String ipAddress) throws IOException, ClassNotFoundException {
        super(player, name, ipAddress);
        this.timeout = timeout;
        // Inizializzazione dell'euristica in base al giocatore
        // Inizializzazione dell'euristica in base al giocatore
        if (player.equalsIgnoreCase("white")) {
            this.heuristic = new WhiteHeuristic();
        } else {
            this.heuristic = new BlackHeuristic();
        }
        this.searchAlgorithm = new IterativeDeepeningAlphaBetaSearch(heuristic);
    }

    @Override
    public void run() {
        // Loop principale del client
        while (true) {
            System.out.println("dnetro while run");
            try {
                this.read();
                State state = this.getCurrentState();

                if (state.getTurn().equalsTurn(this.getPlayer().toString().toUpperCase())) {
                    // Gestione del tempo
                    long startTime = System.currentTimeMillis();
                    long timeLimit = this.timeout * 1000 - 2000; // Margine di 2 secondi
                    Action bestAction = searchAlgorithm.makeDecision(state, startTime, timeLimit);

                    // Invia la mossa al server
                    this.write(bestAction);
                    System.out.println("Mossa inviata: " + bestAction);
                } else {
                    // Attende il turno dell'avversario
                    System.out.println("In attesa del turno avversario...");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String role = "";
        String name = "TheGoatClient";
        int timeout = 60;
        String ipAddress = "localhost";

        if (args.length < 1) {
            // System.out.println("Usage: java TheGoatClient <role> [<timeout>] [<ip-address>]");
            // System.exit(-1);
            System.out.println("fanculo args");
            role = "white";
        } else {
            role = args[0];
            if (args.length >= 2) {
                timeout = Integer.parseInt(args[1]);
            }
            if (args.length >= 3) {
                ipAddress = args[2];
            }
        }

        try {
            System.out.println("dentro try");
            TheGoatClient client = new TheGoatClient(role, name, timeout, ipAddress);
            client.run();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}