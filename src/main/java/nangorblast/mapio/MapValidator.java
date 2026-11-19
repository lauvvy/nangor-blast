package nangorblast.mapio;

import java.util.ArrayList;
import java.util.List;
import nangorblast.core.InvalidMapException;

/**
 * M-01's structural validation, shared by MapLoader and direct EntityFactory.populate.
 * M-02 extends this with the fixed size, all gameplay counts, and path checks.
 */
public final class MapValidator {

    public void validate(List<String> rows) {
        List<String> copy = copyRows(rows);
        int width = copy.get(0).length();
        if (width == 0) {
            throw new InvalidMapException("Map must have a nonempty first row");
        }
        int players = 0;
        for (int row = 0; row < copy.size(); row++) {
            String line = copy.get(row);
            if (line.length() != width) {
                throw new InvalidMapException("Map row " + (row + 1) + " has width " + line.length()
                        + "; expected " + width + " (rectangular map required)");
            }
            for (int col = 0; col < width; col++) {
                char symbol = line.charAt(col);
                String context = "Map row " + (row + 1) + ", column " + (col + 1);
                if (!EntityFactory.supports(symbol)) {
                    throw new InvalidMapException(context + ": unknown character " + EntityFactory.describe(symbol));
                }
                if (symbol == 'P' && ++players > 1) {
                    throw new InvalidMapException(context + ": expected exactly one P player spawn");
                }
            }
        }
        if (players == 0) {
            throw new InvalidMapException("Map must contain exactly one P player spawn; found none");
        }
    }

    static List<String> copyRows(List<String> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new InvalidMapException("Map must contain at least one row");
        }
        List<String> copy = new ArrayList<>(rows.size());
        for (int row = 0; row < rows.size(); row++) {
            String line = rows.get(row);
            if (line == null) {
                throw new InvalidMapException("Map row " + (row + 1) + " is null");
            }
            copy.add(line);
        }
        return List.copyOf(copy);
    }
}
