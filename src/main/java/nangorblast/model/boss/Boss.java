package nangorblast.model.boss;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nangorblast.core.Direction;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.Damage;
import nangorblast.model.Damageable;
import nangorblast.model.Entity;
import nangorblast.model.Hostile;
import nangorblast.model.Player;
import nangorblast.model.enemy.Enemy;

public abstract class Boss extends Entity implements Hostile, Damageable {
    public static final int MAX_HEALTH = 8;
    public static final double WARNING_SECONDS = 1.0;
    public static final double COOLDOWN_SECONDS = 5.0;
    public static final double VULNERABLE_SECONDS = 4.0;
    public static final int SCORE_VALUE = 500;
    private static final double EPSILON = 1e-9;

    private final List<AttackPattern> patterns;
    private final Set<Long> hitBy = new HashSet<>();
    private int health = MAX_HEALTH;
    private BossAttackState state = BossAttackState.COOLDOWN;
    private double timer = COOLDOWN_SECONDS;
    private double vulnerableLeft;
    private AttackPattern current;
    private AttackPattern last;
    private Set<Position> tiles = Set.of();
    private long activationId;
    private Direction facing = Direction.DOWN;

    protected Boss(Position position, List<AttackPattern> patterns) {
        super(position);
        this.patterns = patterns;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.BOSS;
    }

    @Override
    public boolean blocksMovementOf(Entity mover) {
        return mover instanceof Player || mover instanceof Enemy;
    }

    public int health() {
        return health;
    }

    public Direction facing() {
        return facing;
    }

    public void face(Direction direction) {
        this.facing = direction;
    }

    public void faceTowards(Position target) {
        int dCol = target.col() - position().col();
        int dRow = target.row() - position().row();
        if (dCol == 0 && dRow == 0) {
            return;
        }
        if (Math.abs(dCol) >= Math.abs(dRow)) {
            facing = dCol > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            facing = dRow > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    public boolean isDefeated() {
        return health <= 0;
    }

    public BossPhase phase() {
        return health * 2 <= MAX_HEALTH ? BossPhase.PHASE_TWO : BossPhase.PHASE_ONE;
    }

    public boolean isVulnerable() {
        return vulnerableLeft > EPSILON;
    }

    @Override
    public boolean takeDamage(Damage damage) {
        if (isDefeated() || !isVulnerable() || !hitBy.add(damage.activationId())) {
            return false;
        }
        health = Math.max(0, health - damage.amount());
        return true;
    }

    public BossAttackState state() {
        return state;
    }

    public List<AttackPattern> patterns() {
        return patterns;
    }

    public AttackPattern currentPattern() {
        return current;
    }

    public AttackPattern lastPattern() {
        return last;
    }

    public Set<Position> tiles() {
        return tiles;
    }

    public long activationId() {
        return activationId;
    }

    public void advance(double dt) {
        timer = Math.max(0, timer - dt);
        vulnerableLeft = Math.max(0, vulnerableLeft - dt);
    }

    public boolean isTimerDone() {
        return timer <= EPSILON;
    }

    public void startWarning(AttackPattern pattern, Set<Position> affected) {
        current = pattern;
        tiles = Set.copyOf(affected);
        state = BossAttackState.WARNING;
        timer = WARNING_SECONDS;
        vulnerableLeft = 0;
    }

    public void startActive(double seconds) {
        state = BossAttackState.ACTIVE;
        timer = seconds;
        activationId = Damage.newActivationId();
    }

    public void startCooldown() {
        state = BossAttackState.COOLDOWN;
        timer = COOLDOWN_SECONDS;
        vulnerableLeft = (current != null && current.grantsVulnerability()) ? VULNERABLE_SECONDS : 0;
        last = current;
        current = null;
        tiles = Set.of();
    }
}
