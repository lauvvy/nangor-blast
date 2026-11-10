package nangorblast.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void mainRunsWithoutError() {
        assertDoesNotThrow(() -> Main.main(new String[0]));
    }
}
