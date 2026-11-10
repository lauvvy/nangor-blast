package nangorblast.model;

/**
 * Marks an entity that hurts the player on contact: Normal Enemies and bosses. Lets
 * {@code PlayerSystem} report contact without knowing the enemy classes. Hostile entities
 * must also block the player in {@link Entity#blocksMovementOf(Entity)}, because a player
 * and an enemy never share a tile.
 */
public interface Hostile {
}
