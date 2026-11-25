package nangorblast.engine.enemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nangorblast.TestWorlds;
import nangorblast.core.Position;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.GameWorld;
import nangorblast.model.boss.AttackPattern;
import nangorblast.model.boss.Boss;
import nangorblast.model.boss.BossAttackState;
import nangorblast.model.boss.BossPhase;
import org.junit.jupiter.api.Test;

class BossSystemTest {
    private static final double DT = 0.05;

    private record FakeAttack(String name, double length, boolean designated) implements AttackPattern {
        @Override 
        public boolean isUsable(Boss boss, GameWorld world, BossPhase phase) {
            return true;
        }

        @Override
        public Set<Position> tiles(Boss boss, GameWorld world, BossPhase phase) {
            return Set.of(new Position(3, 0));
        }

        @Override
        public double duration(BossPhase phase) {
            return length;
        }

        @Override
        public boolean grantsVulnerability() {
            return designated;
        }
    }

    private static final class TestBoss extends Boss {
        TestBoss(Position at, List<AttackPattern> patterns) {
            super(at, patterns);
        }
    }

    private static TestBoss addBoss(GameWorld world, AttackPattern... patterns) {
        TestBoss boss = new TestBoss(new Position(5, 0), List.of(patterns));
        world.add(boss);
        return boss;
    }

    private static void run(BossSystem system, GameWorld world, int ticks) {
        for (int i = 0; i < ticks; i++) {
            system.update(world, DT);
        }
    }

    private static double secondsUntil(BossSystem system, GameWorld world, Boss boss, BossAttackState target) {
        int ticks = 0;
        while (boss.state() != target && ticks < 10_000) {
            system.update(world, DT);
            ticks++;
        }
        return ticks * DT;
    }

    @Test
    void followsWarningActiveAndCooldownTimings() {
        GameWorld world = TestWorlds.fromAscii("P.......");
        TestBoss boss = addBoss(world, new FakeAttack("fake", 2.0, true));
        BossSystem system = new BossSystem();

        assertEquals(5.0, secondsUntil(system, world, boss, BossAttackState.WARNING), 0.06);
        assertEquals(1.0, secondsUntil(system, world, boss, BossAttackState.ACTIVE), 0.06);
        assertEquals(2.0, secondsUntil(system, world, boss, BossAttackState.COOLDOWN), 0.06);
        assertEquals(5.0, secondsUntil(system, world, boss, BossAttackState.WARNING), 0.06);
    }

    @Test 
    void isVulnerableOnlyForFourSecondsAfterADesignatedAttack() {
        GameWorld world = TestWorlds.fromAscii("P.......");
        TestBoss boss = addBoss(world, new FakeAttack("designated", 1.0, true));
        BossSystem system = new BossSystem();

        reachCooldownAfterAnAttack(system, world, boss);
        assertTrue(boss.isVulnerable());
        run(system, world, 78);
        assertTrue(boss.isVulnerable());
        run(system, world, 4);
        assertFalse(boss.isVulnerable());
    }

    @Test
    void isNotVulnerableAfterAnOrdinaryAttack() {
        GameWorld world = TestWorlds.fromAscii("P.......");
        TestBoss boss = addBoss(world, new FakeAttack("ordinary", 1.0, false));
        BossSystem system = new BossSystem();

        reachCooldownAfterAnAttack(system, world, boss);
        assertFalse(boss.isVulnerable());
        assertFalse(boss.takeDamage(new Damage(1, Damage.newActivationId(), DamageSource.EXPLOSION)));
        assertEquals(Boss.MAX_HEALTH, boss.health());
    }

    @Test
    void damageComesFromTheBombAndPhaseTwoStartsAtHalfHealth() {
        GameWorld world = TestWorlds.fromAscii("P.......");
        TestBoss boss = addBoss(world, new FakeAttack("designated", 1.0, true));
        BossSystem system = new BossSystem();
        reachCooldownAfterAnAttack(system, world, boss);
        
        Damage normal = new Damage(1, Damage.newActivationId(), DamageSource.EXPLOSION);
        assertTrue(boss.takeDamage(normal));
        assertEquals(7, boss.health());
        assertFalse(boss.takeDamage(normal));
        assertEquals(7, boss.health());

        assertTrue(boss.takeDamage(new Damage(2, Damage.newActivationId(), DamageSource.EXPLOSION)));
        assertEquals(5, boss.health());
        assertEquals(BossPhase.PHASE_ONE, boss.phase());

        assertTrue(boss.takeDamage(new Damage(1, Damage.newActivationId(), DamageSource.EXPLOSION)));
        assertEquals(4, boss.health());
        assertEquals(BossPhase.PHASE_TWO, boss.phase());
    }

    @Test
    void neverRepeatAnAttackWhenAnotherIsUsable() {
        GameWorld world = TestWorlds.fromAscii("P.......");
        TestBoss boss = addBoss(world, new FakeAttack("a", 0.5, false), new FakeAttack("b", 0.5, false));
        BossSystem system = new BossSystem();

        List<AttackPattern> chosen = new ArrayList<>();
        BossAttackState before = boss.state();
        for (int i = 0; i < 4000; i++) {
            system.update(world, DT);
            if (boss.state() == BossAttackState.WARNING && before != BossAttackState.WARNING) {
                chosen.add(boss.currentPattern());
            }
            before = boss.state();
        }

        assertTrue(chosen.size() >= 10);
        for (int i = 1; i < chosen.size(); i++) {
            assertNotEquals(chosen.get(i - 1), chosen.get(i));
        }
    }

    private static void reachCooldownAfterAnAttack(BossSystem system, GameWorld world, Boss boss) {
        secondsUntil(system, world, boss, BossAttackState.WARNING);
        secondsUntil(system, world, boss, BossAttackState.ACTIVE);
        secondsUntil(system, world, boss, BossAttackState.COOLDOWN);
    }
}
