package nangorblast.core;

/** Receives events of one type from the {@link EventBus}. */
@FunctionalInterface
public interface EventListener<E extends GameEvent> {

    void onEvent(E event);
}
