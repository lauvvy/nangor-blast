package nangorblast.core;

/** A short text for the message line, such as a refused action. */
public record HudMessage(String text) implements GameEvent {
}
