package nangorblast.model.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nangorblast.TestWorlds;
import nangorblast.core.Direction;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;
import org.junit.jupiter.api.Test;

class HadesAttackTest {
    private static final Position HADES_AT = new Position(5, 5);

    private static GameWorld worldWithPlayerAt(int playerCol, int playerRow) {
        String[] rows = new String[11];
        for (int row = 0; row < 11; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < 11; col++) {
                line.append(col == playerCol && row == playerRow ? 'P' : '.');
            }
            rows[row] = line.toString();
        }
        return TestWorlds.fromAscii(rows);
    }

    private static Hades hadesIn(GameWorld world) {
        Hades hades = new Hades(HADES_AT);
        world.add(hades);
        return hades;
    }

    private static Set<Position> rect(int col1, int col2, int row1, int row2) {
        Set<Position> tiles = new HashSet<>();
        for (int col = col1; col <= col2; col++) {
            for (int row = row1; row <= row2; row++) {
                tiles.add(new Position(col, row));
            }
        }
        return tiles;
    }

    @Test
    void heavyPunchHitsThreeTilesAheadInEveryDirection() {
        Map<Direction, Set<Position>> expected = Map.of(
            Direction.UP, rect(5, 5, 2, 4),
            Direction.DOWN, rect(5, 5, 6, 8),
            Direction.LEFT, rect(2, 4, 5, 5),
            Direction.RIGHT, rect(6, 8, 5, 5));
        GameWorld world = worldWithPlayerAt(0, 0);
        Hades hades = hadesIn(world);
        AttackPattern punch = new HeavyPunch();

        for (Map.Entry<Direction, Set<Position>> entry : expected.entrySet()) {
            hades.face(entry.getKey());
            assertEquals(entry.getValue(), punch.tiles(hades, world, BossPhase.PHASE_ONE), entry.getKey().name());
        }
        assertEquals(1.0, punch.duration(BossPhase.PHASE_ONE), 1e-9);
    }

    @Test 
    void spinAttackHitsTheEightTilesAroundHades() {
        Set<Position> around = rect(4, 6, 4, 6);
        around.remove(HADES_AT);
        GameWorld world = worldWithPlayerAt(0, 0);
        Hades hades = hadesIn(world);
        AttackPattern spin = new SpinAttack();

        for (Direction direction : Direction.values()) {
            hades.face(direction);
            assertEquals(around, spin.tiles(hades, world, BossPhase.PHASE_ONE), direction.name());
        }
        assertEquals(8, around.size());
        assertEquals(1.0, spin.duration(BossPhase.PHASE_ONE), 1e-9);
    }

    @Test 
    void groundSlamHitsThreeByThreeInFrontInEveryDirection() {
        Map<Direction, Set<Position>> expected = Map.of(
            Direction.UP, rect(4, 6, 2, 4),
            Direction.DOWN, rect(4, 6, 6, 8),
            Direction.LEFT, rect(2, 4, 4, 6),
            Direction.RIGHT, rect(6, 8, 4, 6));
        GameWorld world = worldWithPlayerAt(0, 0);
        Hades hades = hadesIn(world);
        AttackPattern slam = new GroundSlam();

        for (Map.Entry<Direction, Set<Position>> entry : expected.entrySet()) {
            hades.face(entry.getKey());
            assertEquals(entry.getValue(), slam.tiles(hades, world, BossPhase.PHASE_ONE), entry.getKey().name());
        }
        assertEquals(1.5, slam.duration(BossPhase.PHASE_ONE), 1e-9);
    }

    @Test 
    void beamHitsTheWholeRowOrColumnThroughThePlayer() {
        GameWorld world = worldWithPlayerAt(2, 7);
        Hades hades = hadesIn(world);
        AttackPattern beam = new BeamAttack();

        hades.face(Direction.LEFT);
        assertEquals(rect(0, 10, 7, 7), beam.tiles(hades, world, BossPhase.PHASE_ONE));
        hades.face(Direction.RIGHT);
        assertEquals(rect(0, 10, 7, 7), beam.tiles(hades, world, BossPhase.PHASE_ONE));
        hades.face(Direction.UP);
        assertEquals(rect(2, 2, 0, 10), beam.tiles(hades, world, BossPhase.PHASE_ONE));
        hades.face(Direction.DOWN);
        assertEquals(rect(2, 2, 0, 10), beam.tiles(hades, world, BossPhase.PHASE_ONE));
        assertEquals(1.0, beam.duration(BossPhase.PHASE_ONE), 1e-9);
    }

    @Test
    void hadesIsVulnerableOnlyAfterHeavyPunchAndSpinAttack() {
        assertTrue(new HeavyPunch().grantsVulnerability());
        assertTrue(new SpinAttack().grantsVulnerability());
        assertFalse(new BeamAttack().grantsVulnerability());
        assertFalse(new GroundSlam().grantsVulnerability());
    }

    @Test 
    void hadesTurnsTowardsThePlayer() {
        GameWorld world = worldWithPlayerAt(5, 0);
        Hades hades = hadesIn(world);
        hades.faceTowards(world.player().position());
        assertEquals(Direction.UP, hades.facing());

        world.player().moveTo(new Position(10, 5));
        hades.faceTowards(world.player().position());
        assertEquals(Direction.RIGHT, hades.facing());
    }
}
