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

    public static int[][] escapes = {
        {1, 0},
        {2, 0},
        {6, 0},
        {7, 0},
        {1, 8},
        {2, 8},
        {6, 8},
        {7, 8},
        {0, 1},
        {0, 2},
        {0, 6},
        {0, 7},
        {8, 1},
        {8, 2},
        {8, 6},
        {8, 7}
    };

    public static boolean isCamp(String box) {
        return camps.contains(box);
    }

    public static boolean isEscape(String box) {
        // Le caselle di fuga sono le caselle sul bordo non occupate dalle citadelle
        int row = box.charAt(1) - '1';
        int col = box.charAt(0) - 'a';
        int size = 9;

        boolean isEdge = row == 0 || row == size - 1 || col == 0 || col == size -1;

        return isEdge && !isCamp(box);
    }
    
}
