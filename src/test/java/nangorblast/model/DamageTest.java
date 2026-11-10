package nangorblast.model;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DamageTest {

    @Test
    void activationIdsAreUnique() {
        assertNotEquals(Damage.newActivationId(), Damage.newActivationId());
    }

    @Test
    void refusesNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> new Damage(0, 1, DamageSource.HAZARD));
    }
}
