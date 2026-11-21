package nangorblast.model.enemy;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;

public interface MovementBehavior {
    default void update(Enemy enemy, GameWorld world, double dt){}
    
    Position nextTile(Enemy enemy);

    void onBlocked();
}
