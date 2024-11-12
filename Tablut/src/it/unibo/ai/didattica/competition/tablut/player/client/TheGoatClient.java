package it.unibo.ai.didattica.competition.tablut.player.client;

import it.unibo.ai.didattica.competition.tablut.client.TablutRandomClient;
import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.player.search.heuristics.*;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.player.search.IterativeDeepeningAlphaBetaSearch;
import java.io.IOException;
import java.net.UnknownHostException;

public class TheGoatClient extends TablutClient {

    private final IterativeDeepeningAlphaBetaSearch searchAlgorithm;
    private final int timeout;

    public TheGoatClient(String player, String name, int timeout, String ipAddress) throws IOException, ClassNotFoundException {
        super(player, name, ipAddress);
        this.timeout = timeout;

        Heuristic heuristic;

        if (player.equalsIgnoreCase("white")) {
            heuristic = new WhiteHeuristic();
        } else {
            heuristic = new BlackHeuristic();
        }
        this.searchAlgorithm = new IterativeDeepeningAlphaBetaSearch(heuristic);
    }

    @Override
    public void run() {

        try {
            this.declareName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("You are player " + this.getPlayer().toString() + "!");

        while (true) {
            System.out.println("iteration");
            try {

                System.out.println("leggo");
                try {
                    this.read();
                } catch (ClassNotFoundException | IOException e1) {

                    e1.printStackTrace();
                    System.exit(1);
                }
                System.out.println("non leggo più");

                State state = this.getCurrentState();

                System.out.println("Stato: " + state);
                System.out.println("Stato turno: " + state.getTurn());
                System.out.println("Get player: " + this.getPlayer().toString().toUpperCase());

                if (state.getTurn().equalsTurn(this.getPlayer().toString().toUpperCase())) {

                    System.out.println("Cerco mossa...");

                    long startTime = System.currentTimeMillis();
                    long timeLimit = this.timeout * 1000L - 2000;
                    Action bestAction = searchAlgorithm.makeDecision(state, startTime, timeLimit);

                    System.out.println("Trovato mossa...");

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
        String role = "white";
        String name = "AltTablut";
        int timeout = 10;
        String ipAddress = "localhost";

        if (args.length < 1) {
             System.out.println("Usage: java TheGoatClient <role> [<timeout>] [<ip-address>]");
//             System.exit(-1);
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