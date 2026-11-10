package nangorblast.input;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CommandTest {

    @Test
    void refusesInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Command(-1, Action.PAUSE));
        assertThrows(NullPointerException.class, () -> new Command(0, null));
    }
}
