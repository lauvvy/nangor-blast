package nangorblast.model.enemy;

import java.util.List;
import nangorblast.core.Position;

public final class PatrolBehavior implements MovementBehavior {
    private final List<Position> path;
    private int step = 1;

    public PatrolBehavior(List<Position> path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path must not be empty");
        }
        this.path = List.copyOf(path);
    }

    @Override
    public Position nextTile(Enemy enemy) {
        int index = path.indexOf(enemy.position());
        if (index < 0) {
            return enemy.position();
        }
        int candidate = index + step;
        if (candidate < 0 || candidate >= path.size()) {
            step = -step;
            candidate = index + step;
        }
        if (candidate < 0 || candidate >= path.size()) {
            return enemy.position();
        }
        return path.get(candidate);
    }

    @Override
    public void onBlocked() {
        step = -step;
    }
}
