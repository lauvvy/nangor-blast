package nangorblast.input;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nangorblast.core.ConfigLoadException;

/**
 * Maps key names to commands, read from {@code config/keybindings.properties}. Each line is
 * {@code p<player>.<ACTION> = key, key}. A key is one printable character (letters are
 * case-insensitive) or a named key such as {@code UP} or {@code ENTER}. Input sources turn
 * device keys into these names, so game code never sees a key.
 */
public final class KeyBindings {

    /** Keys that are not one printable character. Input sources produce exactly these names. */
    public static final List<String> NAMED_KEYS =
            List.of("UP", "DOWN", "LEFT", "RIGHT", "ENTER", "ESCAPE", "SPACE", "BACKSPACE", "TAB");

    private static final Pattern PROPERTY = Pattern.compile("p([1-9])\\.([A-Za-z_]+)");

    private final Map<String, Command> byKey;

    private KeyBindings(Map<String, Command> byKey) {
        this.byKey = Map.copyOf(byKey);
    }

    public static KeyBindings load(Path file) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new ConfigLoadException("Cannot read key bindings from " + file + ": " + e.getMessage(), e);
        }
        return parse(properties);
    }

    public static KeyBindings parse(Properties properties) {
        Map<String, Command> byKey = new HashMap<>();
        Map<Action, Boolean> firstPlayerBound = new HashMap<>();
        for (String name : new TreeSet<>(properties.stringPropertyNames())) {
            Matcher match = PROPERTY.matcher(name);
            if (!match.matches()) {
                throw new ConfigLoadException("Invalid key binding name '" + name
                        + "'. Expected p<player>.<ACTION>, for example p1.MOVE_UP");
            }
            int playerIndex = Integer.parseInt(match.group(1)) - 1;
            Action action;
            try {
                action = Action.valueOf(match.group(2).toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ConfigLoadException("Unknown action '" + match.group(2) + "' in " + name);
            }
            Command command = new Command(playerIndex, action);
            for (String raw : properties.getProperty(name).split(",")) {
                String key = normalize(raw.strip(), name);
                Command previous = byKey.putIfAbsent(key, command);
                if (previous != null) {
                    throw new ConfigLoadException("Key '" + key + "' is bound twice: to p"
                            + (previous.playerIndex() + 1) + "." + previous.action() + " and to " + name);
                }
            }
            if (playerIndex == 0) {
                firstPlayerBound.put(action, true);
            }
        }
        for (Action action : Action.values()) {
            if (!firstPlayerBound.containsKey(action)) {
                throw new ConfigLoadException("p1." + action + " has no key");
            }
        }
        return new KeyBindings(byKey);
    }

    private static String normalize(String raw, String property) {
        if (raw.length() == 1 && raw.charAt(0) > ' ' && raw.charAt(0) < 127) {
            return raw.toLowerCase(Locale.ROOT);
        }
        String named = raw.toUpperCase(Locale.ROOT);
        if (NAMED_KEYS.contains(named)) {
            return named;
        }
        throw new ConfigLoadException("Unknown key '" + raw + "' in " + property
                + ". Use one printable character or one of " + NAMED_KEYS);
    }

    /** The command bound to a key name, as produced by an input source. */
    public Optional<Command> commandFor(String key) {
        return Optional.ofNullable(byKey.get(key));
    }
}
