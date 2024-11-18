package it.unibo.ai.didattica.competition.tablut.player.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeuristicUtils {

    // Explicit constructor
    public HeuristicUtils() {
        super();
    }

    // Definiamo le coordinate delle citadelle (campi)
    private static final Set<String> camps = new HashSet<>();

    static {
        // Coordinate delle citadelle
        camps.add("a4");
        camps.add("a5");
        camps.add("a6");
        camps.add("b5");
        camps.add("i4");
        camps.add("i5");
        camps.add("i6");
        camps.add("h5");
        camps.add("d1");
        camps.add("e1");
        camps.add("f1");
        camps.add("e2");
        camps.add("d9");
        camps.add("e9");
        camps.add("f9");
        camps.add("e8");
    }

    public static boolean isCamp(String box) {
        return camps.contains(box);
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

        return isEdge && !isCamp(box);
    }

    public static List<int[]> getEscapePositions(State state) {
        List<int[]> escapes = new ArrayList<>();
        int size = state.getBoard().length;

        // Caselle sul bordo non occupate dalle citadelle
        for (int i = 0; i < size; i++) {
            if (!isCamp(state.getBox(0, i))) {
                escapes.add(new int[]{0, i});
            }
            if (!isCamp(state.getBox(size -1, i))) {
                escapes.add(new int[]{size -1, i});
            }
            if (!isCamp(state.getBox(i, 0))) {
                escapes.add(new int[]{i, 0});
            }
            if (!isCamp(state.getBox(i, size -1))) {
                escapes.add(new int[]{i, size -1});
            }
        }

        return escapes;
    }

    
}
