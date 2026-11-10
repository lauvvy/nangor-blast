package nangorblast.core;

/** Published when a player loses a life. */
public record PlayerDamaged(int playerIndex, int livesLeft) implements GameEvent {
}
