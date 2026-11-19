package nangorblast.mapio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.map.GameMap;

/** Reads map text, validates it, and delegates entity placement to EntityFactory. */
public final class MapLoader {

    private final MapValidator validator;
    private final EntityFactory factory;
    private final double timeLimit;

    public MapLoader(EntityFactory factory, double timeLimit) {
        this(new MapValidator(), factory, timeLimit);
    }

    public MapLoader(MapValidator validator, EntityFactory factory, double timeLimit) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.factory = Objects.requireNonNull(factory, "factory");
        if (!Double.isFinite(timeLimit) || timeLimit <= 0) {
            throw new IllegalArgumentException("timeLimit must be finite and positive, got " + timeLimit);
        }
        this.timeLimit = timeLimit;
    }

    public GameWorld load(Path path) {
        return load(path, new Random());
    }

    /** The file's name without its extension becomes the world's map name. */
    public GameWorld load(Path path, Random random) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(random, "random");
        List<String> rows = readRows(path);
        String filename = path.getFileName().toString();
        int extension = filename.lastIndexOf('.');
        String name = extension > 0 ? filename.substring(0, extension) : filename;
        try {
            return parse(name, rows, random);
        } catch (InvalidMapException ex) {
            throw new InvalidMapException("Invalid map file '" + path + "': " + ex.getMessage(), ex);
        }
    }

    /** UTF-8 decoding is separate from validation and world construction. */
    public List<String> readRows(Path path) {
        Objects.requireNonNull(path, "path");
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new InvalidMapException("Cannot read map '" + path + "': " + ex.getMessage(), ex);
        }
    }

    public GameWorld parse(String name, List<String> rows) {
        return parse(name, rows, new Random());
    }

    /**
     * Checks the entire text before invoking any entity constructors. This is structural
     * parsing only; fixed dimensions, gameplay counts, and reachability belong to M-02.
     * Coordinates in error messages are one-based; {@link Position} is zero-based.
     */
    public GameWorld parse(String name, List<String> rows, Random random) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(random, "random");
        List<String> copy;
        try {
            List<String> normalized = new ArrayList<>(MapValidator.copyRows(rows));
            String first = normalized.get(0);
            if (first.startsWith("\uFEFF")) {
                normalized.set(0, first.substring(1));
            }
            copy = List.copyOf(normalized);
            validator.validate(copy);
        } catch (InvalidMapException ex) {
            throw new InvalidMapException("Map '" + name + "': " + ex.getMessage(), ex);
        }
        Position spawn = playerSpawn(copy);
        Player player = factory.createPlayer(spawn);
        GameMap map = new GameMap(name, copy.get(0).length(), copy.size());
        GameWorld world = new GameWorld(map, player, timeLimit, random);
        factory.populate(world, copy);
        return world;
    }

    private static Position playerSpawn(List<String> rows) {
        for (int row = 0; row < rows.size(); row++) {
            int col = rows.get(row).indexOf('P');
            if (col >= 0) {
                return new Position(col, row);
            }
        }
        throw new InvalidMapException("Map must contain exactly one P player spawn");
    }
}
