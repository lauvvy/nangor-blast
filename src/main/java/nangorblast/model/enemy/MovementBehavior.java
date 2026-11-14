package nangorblast.model.enemy;
import nangorblast.core.Position;

public interface MovementBehavior {
    Position nextTile(Enemy enemy);

    void onBlocked();
}
