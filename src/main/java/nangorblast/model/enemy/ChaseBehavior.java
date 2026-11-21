package nangorblast.model.enemy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nangorblast.core.Direction;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;

public final class ChaseBehavior implements MovementBehavior {
    public static final int DETECTION_RANGE = 3;
    public static final double STUCK_LIMIT = 1.0;

    private final PatrolBehavior patrol;
    private ChaseState state = ChaseState.PATROL;
    private List<Position> path = new ArrayList<>();
    private Position lastPlayerTile = null;
    private double stuckTimer = 0;
    private int pathComputations = 0;

    public ChaseBehavior(PatrolBehavior patrol) {
        this.patrol = patrol;
    }

    public ChaseState state() {
        return state;
    }

    public int pathComputations() {
        return pathComputations;
    }

    @Override 
    public void update(Enemy enemy, GameWorld world, double dt) {
        while(!path.isEmpty() && path.get(0).equals(enemy.position())) {
            path.remove(0);
        }

        Position player = world.player().position();
        int distance = enemy.position().manhattanDistance(player);
        switch (state) {
            case PATROL -> whilePatrol(enemy, world, player, distance);
            case CHASE -> whileChasing(enemy, world, player, distance, dt);
        }
    }

    private void whilePatrol(Enemy enemy, GameWorld world, Position player, int distance) {
        if (distance > DETECTION_RANGE) {
            return;
        }

        recompute(enemy, world, player);
        if (!path.isEmpty()) {
            state = ChaseState.CHASE;
            stuckTimer = 0;
        }
    }

    private void whileChasing(Enemy enemy, GameWorld world, Position player, int distance, double dt) {
        if (distance > DETECTION_RANGE) {
            leaveChase();
            return;
        }
        if (!player.equals(lastPlayerTile) || nextStepBlocked(enemy, world, player)) {
            recompute(enemy, world, player);
        }
        if (path.isEmpty() || nextStepBlocked(enemy, world, player)) {
            stuckTimer += dt;
        } else {
            stuckTimer = 0;
        }
        if (stuckTimer >= STUCK_LIMIT) {
            leaveChase();
        }
    }

    private void leaveChase() {
        state = ChaseState.PATROL;
        path = new ArrayList<>();
        stuckTimer = 0;
    }

    private boolean nextStepBlocked(Enemy enemy, GameWorld world, Position player) {
        return !path.isEmpty() 
            && !path.get(0).equals(player) 
            && world.isBlockedFor(enemy, path.get(0));
    }

    private void recompute(Enemy enemy, GameWorld world, Position player) {
        path = findPath(enemy, world, player);
        lastPlayerTile = player;
        pathComputations++;
    }

    private List<Position> findPath(Enemy enemy, GameWorld world, Position goal) {
        Position start = enemy.position();
        Deque<Position> queue = new ArrayDeque<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        queue.add(start);
        cameFrom.put(start, start);
        while(!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(goal)) {
                List<Position> result = new ArrayList<>();
                Position step = goal;
                while (!step.equals(start)) {
                    result.add(step);
                    step = cameFrom.get(step);
                }
                Collections.reverse(result);
                return result;
            }
            for (Direction d : Direction.values()) {
                Position next = current.translate(d);
                if (cameFrom.containsKey(next)) {
                    continue;
                }
                if (world.isBlockedFor(enemy, next) && !next.equals(goal)) {
                    continue;
                }
                cameFrom.put(next, current);
                queue.add(next);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Position nextTile(Enemy enemy) {
        if (state == ChaseState.PATROL) {
            return patrol.nextTile(enemy);
        }
        return path.isEmpty() ? enemy.position() : path.get(0);
    }
    
    @Override 
    public void onBlocked() {
        if (state == ChaseState.PATROL) {
            patrol.onBlocked();
        }
    }
}
