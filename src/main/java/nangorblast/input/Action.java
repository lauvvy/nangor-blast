package nangorblast.input;

/**
 * Every action a player can take. Keys are bound to actions in
 * {@code config/keybindings.properties}; game code reads actions, never keys.
 * Menus reuse the move actions for navigation.
 *
 * <p>{@link #UNBOUND} is never bound: input sources send it for a key with no binding, so a
 * menu can report invalid input.
 */
public enum Action {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    PLACE_BOMB,
    SELECT_NORMAL,
    SELECT_PUMPED,
    SELECT_REMOTE,
    DETONATE,
    PAUSE,
    CONFIRM,
    BACK,
    CHOICE_1,
    CHOICE_2,
    CHOICE_3,
    UNBOUND
}
