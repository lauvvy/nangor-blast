package nangorblast.model;

import java.util.HashSet;
import java.util.Set;
import nangorblast.core.BombType;
import nangorblast.core.Direction;
import nangorblast.core.EntityKind;
import nangorblast.core.InvalidActionException;
import nangorblast.core.Position;

/** Deeper, controlled by a player. Attributes from F01.1, limits from F01.1 and F06.2. */
public final class Player extends Entity implements Damageable {

    // Specification defaults. M-05 moves them into config/game.properties.
    public static final int START_LIVES = 3;
    public static final int MAX_LIVES = 5;
    public static final int START_BOMB_CAPACITY = 1;
    public static final int MAX_BOMB_CAPACITY = 3;
    public static final int START_BLAST_RANGE = 1;
    public static final int MAX_BLAST_RANGE = 5;
    public static final double SPEED_UP_STEP = 0.5; // of base speed, per Speed Up
    public static final double MAX_SPEED_BONUS = 1.0; // of base speed, in total
    public static final double INVULNERABLE_SECONDS = 1.5; // after losing a life (F01.2.4)

    private final int playerIndex;
    private final Position spawn;
    private final double baseSpeed;
    private Direction direction = Direction.DOWN;
    private int lives = START_LIVES;
    private int bombCapacity = START_BOMB_CAPACITY;
    private int activeBombs;
    private int blastRange = START_BLAST_RANGE;
    private double speed;
    private double invulnerableTime;
    private boolean remoteUnlocked;
    private BombType selectedBomb = BombType.NORMAL;
    private final Set<Long> hitBy = new HashSet<>();
    private boolean respawnPending;

    /**
     * @param playerIndex 0 for the first player, 1 for the second (bonus B04)
     * @param baseSpeed tiles per second before any Speed Up
     */
    public Player(Position spawn, int playerIndex, double baseSpeed) {
        super(spawn);
        if (baseSpeed <= 0) {
            throw new IllegalArgumentException("Base speed must be positive, got " + baseSpeed);
        }
        this.playerIndex = playerIndex;
        this.spawn = spawn;
        this.baseSpeed = baseSpeed;
        this.speed = baseSpeed;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.PLAYER;
    }

    /** Enemies cannot walk into the player; trying to counts as contact. */
    @Override
    public boolean blocksMovementOf(Entity mover) {
        return mover != this;
    }

    /**
     * Loses one life (F01.2.2). A hit also makes the player invulnerable at once, so two
     * activations in the same tick cost one life, and marks a respawn while lives remain;
     * the respawn itself needs the map and is done by {@code LifeSystem}.
     */
    @Override
    public boolean takeDamage(Damage damage) {
        if (lives == 0 || isInvulnerable() || !hitBy.add(damage.activationId())) {
            return false;
        }
        lives--;
        makeInvulnerable(INVULNERABLE_SECONDS);
        respawnPending = lives > 0;
        return true;
    }

    /** Where the player starts and respawns. */
    public Position spawn() {
        return spawn;
    }

    /** True after a lost life until {@link #respawnAt} runs. */
    public boolean isRespawnPending() {
        return respawnPending;
    }

    /** Places the player back on the map with a fresh 1.5 seconds of invulnerability. */
    public void respawnAt(Position tile) {
        moveTo(tile);
        respawnPending = false;
        invulnerableTime = INVULNERABLE_SECONDS;
    }

    public int playerIndex() {
        return playerIndex;
    }

    public Direction direction() {
        return direction;
    }

    public void face(Direction newDirection) {
        this.direction = newDirection;
    }

    public int lives() {
        return lives;
    }

    public boolean isAlive() {
        return lives > 0;
    }

    /** Heart power-up. */
    public void addLife() {
        lives = Math.min(MAX_LIVES, lives + 1);
    }

    public int bombCapacity() {
        return bombCapacity;
    }

    /** Bomb Up power-up. */
    public void increaseBombCapacity() {
        bombCapacity = Math.min(MAX_BOMB_CAPACITY, bombCapacity + 1);
    }

    public int activeBombs() {
        return activeBombs;
    }

    public boolean canPlaceBomb() {
        return activeBombs < bombCapacity;
    }

    public void bombPlaced() {
        if (!canPlaceBomb()) {
            throw new InvalidActionException("Bomb capacity is full");
        }
        activeBombs++;
    }

    /** Called when one of this player's bombs has finished exploding (F01.2.5). */
    public void bombFinished() {
        activeBombs = Math.max(0, activeBombs - 1);
    }

    public int blastRange() {
        return blastRange;
    }

    /** Fire Up power-up. */
    public void increaseBlastRange() {
        blastRange = Math.min(MAX_BLAST_RANGE, blastRange + 1);
    }

    /** Tiles per second. */
    public double speed() {
        return speed;
    }

    public double baseSpeed() {
        return baseSpeed;
    }

    /** Speed Up power-up: +50% of base speed, at most +100% in total. */
    public void increaseSpeed() {
        speed = Math.min(baseSpeed * (1 + MAX_SPEED_BONUS), speed + baseSpeed * SPEED_UP_STEP);
    }

    public boolean isInvulnerable() {
        return invulnerableTime > 0;
    }

    public double invulnerableTime() {
        return invulnerableTime;
    }

    public void makeInvulnerable(double seconds) {
        invulnerableTime = Math.max(invulnerableTime, seconds);
    }

    /** Counts invulnerability down. Called once per tick. */
    public void tickInvulnerability(double dt) {
        invulnerableTime = Math.max(0, invulnerableTime - dt);
    }

    public boolean isRemoteUnlocked() {
        return remoteUnlocked;
    }

    /** Remote Detonator power-up. */
    public void unlockRemote() {
        remoteUnlocked = true;
    }

    public BombType selectedBomb() {
        return selectedBomb;
    }

    /** F02.3: applies to the next bombs placed, not to bombs already on the map. */
    public void selectBomb(BombType type) {
        if (type == BombType.REMOTE && !remoteUnlocked) {
            throw new InvalidActionException("Remote Bomb needs the Remote Detonator");
        }
        this.selectedBomb = type;
    }
}
