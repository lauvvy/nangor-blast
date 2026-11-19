package nangorblast.mapio;

import java.util.List;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Hostile;
import nangorblast.model.enemy.NormalEnemy;
import nangorblast.model.enemy.PatrolBehavior;

/** Test-only boss and lava fixtures until E-04/E-05 and M-03 supply production classes. */
final class MapFixtures {

    private MapFixtures() {
    }

    static EntityFactory factory(double playerSpeed) {
        return new EntityFactory(playerSpeed, MapFixtures::enemy,
                position -> new TestHostile(position, EntityKind.BOSS),
                position -> new TestEntity(position, EntityKind.LAVA_DORMANT));
    }

    static NormalEnemy enemy(Position position) {
        return new NormalEnemy(position, new PatrolBehavior(List.of(position)));
    }

    static class TestEntity extends Entity {
        private final EntityKind kind;

        TestEntity(Position position, EntityKind kind) {
            super(position);
            this.kind = kind;
        }

        @Override
        public EntityKind kind() {
            return kind;
        }
    }

    static final class TestHostile extends TestEntity implements Hostile {
        TestHostile(Position position, EntityKind kind) {
            super(position, kind);
        }

        @Override
        public boolean blocksMovementOf(Entity mover) {
            return mover != this;
        }
    }
}
