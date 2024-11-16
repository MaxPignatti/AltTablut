package it.unibo.ai.didattica.competition.tablut.player.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeuristicUtils {

    // Definiamo le coordinate delle citadelle (campi)
    private static final Set<String> citadels = new HashSet<>();

    static {
        // Coordinate delle citadelle
        citadels.add("a4");
        citadels.add("a5");
        citadels.add("a6");
        citadels.add("b5");
        citadels.add("i4");
        citadels.add("i5");
        citadels.add("i6");
        citadels.add("h5");
        citadels.add("d1");
        citadels.add("e1");
        citadels.add("f1");
        citadels.add("e2");
        citadels.add("d9");
        citadels.add("e9");
        citadels.add("f9");
        citadels.add("e8");
    }

    public static boolean isCitadel(String box) {
        return citadels.contains(box);
    }

    public static boolean isThrone(int row, int col) {
        // Il trono si trova al centro della scacchiera
        int size = 9; // Dimensione della scacchiera
        return row == size / 2 && col == size / 2;
    }

    public static boolean isEscape(String box) {
        // Le caselle di fuga sono le caselle sul bordo non occupate dalle citadelle
        int row = box.charAt(1) - '1';
        int col = box.charAt(0) - 'a';
        int size = 9;

        boolean isEdge = row == 0 || row == size - 1 || col == 0 || col == size -1;

        return isEdge && !isCitadel(box);
    }

    public static List<int[]> getEscapePositions(State state) {
        List<int[]> escapes = new ArrayList<>();
        int size = state.getBoard().length;

        // Caselle sul bordo non occupate dalle citadelle
        for (int i = 0; i < size; i++) {
            if (!isCitadel(state.getBox(0, i))) {
                escapes.add(new int[]{0, i});
            }
            if (!isCitadel(state.getBox(size -1, i))) {
                escapes.add(new int[]{size -1, i});
            }
            if (!isCitadel(state.getBox(i, 0))) {
                escapes.add(new int[]{i, 0});
            }
            if (!isCitadel(state.getBox(i, size -1))) {
                escapes.add(new int[]{i, size -1});
            }
        }

        return escapes;
    }
}
