package nangorblast.model;

/** Where damage comes from, for logging and for rules that treat sources differently. */
public enum DamageSource {
    EXPLOSION,
    BOSS_ATTACK,
    ENEMY_CONTACT,
    HAZARD
}
