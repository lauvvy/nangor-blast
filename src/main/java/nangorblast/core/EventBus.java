package nangorblast.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronous publish and subscribe (Observer pattern). A listener subscribed to a type also
 * receives its subtypes. Used only on the game-loop thread, so it has no locking.
 */
public final class EventBus {

    private final List<Subscription<?>> subscriptions = new ArrayList<>();

    public <E extends GameEvent> void subscribe(Class<E> type, EventListener<? super E> listener) {
        subscriptions.add(new Subscription<>(type, listener));
    }

    public void publish(GameEvent event) {
        // Copy, so a listener may subscribe another listener while handling an event.
        for (Subscription<?> subscription : List.copyOf(subscriptions)) {
            subscription.deliver(event);
        }
    }

    private record Subscription<E extends GameEvent>(Class<E> type, EventListener<? super E> listener) {

        void deliver(GameEvent event) {
            if (type.isInstance(event)) {
                listener.onEvent(type.cast(event));
            }
        }
    }
}
