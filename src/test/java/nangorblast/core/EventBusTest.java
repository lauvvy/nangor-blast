package nangorblast.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventBusTest {

    @Test
    void deliversOnlyMatchingTypes() {
        EventBus bus = new EventBus();
        List<EnemyDefeated> defeated = new ArrayList<>();
        bus.subscribe(EnemyDefeated.class, defeated::add);

        bus.publish(new EnemyDefeated(new Position(1, 1), 100, false));
        bus.publish(new HudMessage("ignored"));

        assertEquals(1, defeated.size());
        assertEquals(100, defeated.get(0).scoreValue());
    }

    @Test
    void listenerOfSupertypeReceivesEveryEvent() {
        EventBus bus = new EventBus();
        List<GameEvent> all = new ArrayList<>();
        bus.subscribe(GameEvent.class, all::add);

        bus.publish(new HudMessage("a"));
        bus.publish(new PlayerDamaged(0, 2));

        assertEquals(2, all.size());
    }

    @Test
    void listenerMaySubscribeWhileHandling() {
        EventBus bus = new EventBus();
        List<HudMessage> late = new ArrayList<>();
        bus.subscribe(HudMessage.class, e -> bus.subscribe(HudMessage.class, late::add));

        bus.publish(new HudMessage("first"));
        bus.publish(new HudMessage("second"));

        assertEquals(List.of(new HudMessage("second")), late);
    }
}
