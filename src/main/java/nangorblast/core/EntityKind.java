package nangorblast.core;

/**
 * One constant per thing that can be drawn. Renderers map a kind to a glyph or a sprite
 * (see DESIGN.md), so the model never knows how anything looks. When a constant is added,
 * update DESIGN.md, the CLI mapping, and the GUI mapping in the same change.
 */
public enum EntityKind {
    INDESTRUCTIBLE_BLOCK,
    DESTRUCTIBLE_BLOCK,
    REINFORCED_BLOCK,
    PLAYER,
    NORMAL_ENEMY,
    BOSS,
    EXIT_LOCKED,
    EXIT_OPEN,
    LAVA_DORMANT,
    LAVA_WARNING,
    LAVA_ERUPTING,
    EXPLOSION,
    BOMB_NORMAL,
    BOMB_PUMPED,
    BOMB_REMOTE,
    POWER_UP_BOMB_UP,
    POWER_UP_FIRE_UP,
    POWER_UP_SPEED_UP,
    POWER_UP_REMOTE_DETONATOR,
    POWER_UP_HEART
}
