package nangorblast.core;

/** Published once when a Normal Enemy or a boss is defeated. ScoreSystem adds {@code scoreValue}. */
public record EnemyDefeated(Position position, int scoreValue, boolean boss) implements GameEvent {
}
