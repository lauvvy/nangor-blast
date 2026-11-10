package nangorblast.engine;

import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.view.WorldSnapshot;
import nangorblast.view.WorldSnapshot.Hud;
import nangorblast.view.WorldSnapshot.Sprite;

/** Copies a {@link GameWorld} into an immutable {@link WorldSnapshot} at the end of a tick. */
public final class SnapshotBuilder {

    private SnapshotBuilder() {
    }

    public static WorldSnapshot build(GameWorld world) {
        var sprites = world.entities().stream()
                .map(e -> new Sprite(e.id(), e.kind(), e.position()))
                .toList();
        Player player = world.player();
        var hud = new Hud(player.lives(), world.score(), (int) Math.ceil(world.timeLeft()), player.selectedBomb(),
                player.isInvulnerable());
        return new WorldSnapshot(world.width(), world.height(), sprites, world.warningTiles(), hud, world.state());
    }
}
