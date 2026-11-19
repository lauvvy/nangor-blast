package nangorblast.mapio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;

/** Reads a rectangular UTF-8 map and builds a fresh world, without starting a game loop. */
public final class MapLoader {

    private final EntityFactory factory;
    private final double timeLimit;

    public MapLoader(EntityFactory factory, double timeLimit) {
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
        List<String> rows;
        try {
            rows = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new InvalidMapException("Cannot read map '" + path + "': " + ex.getMessage(), ex);
        }
        String filename = path.getFileName().toString();
        int extension = filename.lastIndexOf('.');
        String name = extension > 0 ? filename.substring(0, extension) : filename;
        try {
            return parse(name, rows, random);
        } catch (InvalidMapException ex) {
            throw new InvalidMapException("Invalid map file '" + path + "': " + ex.getMessage(), ex);
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
        Layout layout = readLayout(name, rows);
        Player player = factory.createPlayer(layout.spawn());
        GameWorld world = new GameWorld(name, layout.rows().get(0).length(), layout.rows().size(),
                player, timeLimit, random);
        Set<Long> ids = new HashSet<>();
        ids.add(player.id());
        for (int row = 0; row < world.height(); row++) {
            for (int col = 0; col < world.width(); col++) {
                char symbol = layout.rows().get(row).charAt(col);
                if (symbol == '.' || symbol == 'P') {
                    continue; // GameWorld already contains the player; floor is implicit.
                }
                String context = context(name, row, col);
                Entity entity;
                try {
                    entity = factory.create(symbol, new Position(col, row)).orElseThrow();
                } catch (InvalidMapException ex) {
                    throw new InvalidMapException(context + ": " + ex.getMessage(), ex);
                }
                if (!ids.add(entity.id())) {
                    throw new InvalidMapException(context + ": factory reused an entity; return a fresh entity per tile");
                }
                world.add(entity);
            }
        }
        return world;
    }

    private static Layout readLayout(String name, List<String> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new InvalidMapException("Map '" + name + "' must contain at least one row");
        }
        List<String> copy = new ArrayList<>(rows.size());
        Position spawn = null;
        int width = -1;
        for (int row = 0; row < rows.size(); row++) {
            String line = rows.get(row);
            if (line == null) {
                throw new InvalidMapException("Map '" + name + "', row " + (row + 1) + " is null");
            }
            if (row == 0 && line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            if (row == 0) {
                width = line.length();
                if (width == 0) {
                    throw new InvalidMapException("Map '" + name + "' must have a nonempty first row");
                }
            }
            if (line.length() != width) {
                throw new InvalidMapException("Map '" + name + "', row " + (row + 1)
                        + " has width " + line.length() + "; expected " + width + " (rectangular map required)");
            }
            for (int col = 0; col < width; col++) {
                char symbol = line.charAt(col);
                if (!EntityFactory.supports(symbol)) {
                    throw new InvalidMapException(context(name, row, col)
                            + ": unknown character " + EntityFactory.describe(symbol));
                }
                if (symbol == 'P') {
                    if (spawn != null) {
                        throw new InvalidMapException(context(name, row, col) + ": expected exactly one P player spawn");
                    }
                    spawn = new Position(col, row);
                }
            }
            copy.add(line);
        }
        if (spawn == null) {
            throw new InvalidMapException("Map '" + name + "' must contain exactly one P player spawn; found none");
        }
        return new Layout(List.copyOf(copy), spawn);
    }

    private static String context(String name, int row, int col) {
        return "Map '" + name + "', row " + (row + 1) + ", column " + (col + 1);
    }

    private record Layout(List<String> rows, Position spawn) {
    }
}
