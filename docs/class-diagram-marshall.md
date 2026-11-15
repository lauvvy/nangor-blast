# Class Diagram — Marshall's packages

Input for `S-05`. Covers `model/bomb`, `model/explosion`, `model/block`, `model/powerup`, and `engine/bomb`. Classes marked `<<planned>>` do not exist yet; their task is in the note. Types owned by other members (`Entity`, `Player`, `GameWorld`, `GameSystem`, `CommandHandler`, `Damageable`, `EventBus`) are shown only where Marshall's classes use them.

GitHub renders the diagram below. To export an image, paste it into <https://mermaid.live>.

```mermaid
classDiagram
    direction TB

    class Entity {
        <<abstract>>
        +id() long
        +position() Position
        +isActive() boolean
        +deactivate() void
        +kind()* EntityKind
        +blocksMovementOf(Entity mover) boolean
    }
    class Player
    class Damageable {
        <<interface>>
        +takeDamage(Damage damage) boolean
    }
    class GameSystem {
        <<interface>>
        +update(GameWorld world, double dt) void
    }
    class CommandHandler {
        <<interface>>
        +handle(GameWorld world, Command command) void
    }

    %% model/bomb
    class Bomb {
        <<abstract>>
        -Player owner
        -int blastRange
        -double fuseTime
        +type()* BombType
        +owner() Player
        +blastRange() int
        +fuseTime() double
        +damage() int
        +tickFuse(double dt) void
        +trigger() void
        +isDue() boolean
        +blocksMovementOf(Entity mover) boolean
    }
    class NormalBomb {
        +FUSE_SECONDS = 3.0
    }
    class PumpedBomb {
        <<planned B-04>>
        +FUSE_SECONDS = 4.5
        +MAX_RANGE = 7
    }
    class RemoteBomb {
        <<planned B-05>>
        +tickFuse(double dt) void
    }
    class BombFactory {
        +create(BombType type, Position position, Player owner)$ Bomb
    }

    %% model/explosion
    class Explosion {
        +DURATION_SECONDS = 0.5
        -Position center
        -int blastRange
        -Player owner
        -BombType bombType
        -int damage
        -long activationId
        -double duration
        +isCenter() boolean
        +tick(double dt) void
        +isFinished() boolean
        +hasHurt(Entity target) boolean
        +markHurt(Entity target) void
    }

    %% model/block
    class Block {
        <<abstract>>
        +blocksMovementOf(Entity mover) boolean
        +isDestroyedBy(BombType type)* boolean
    }
    class IndestructibleBlock
    class DestructibleBlock
    class ReinforcedBlock

    %% model/powerup
    class PowerUp {
        <<planned B-07>>
        -PowerUpType type
        +applyTo(Player player) void
    }
    class PowerUpType {
        <<enumeration>>
        BOMB_UP
        FIRE_UP
        SPEED_UP
        REMOTE_DETONATOR
        HEART
    }

    %% engine/bomb
    class BombSystem {
        +handle(GameWorld world, Command command) void
        +update(GameWorld world, double dt) void
    }
    class ExplosionSystem {
        +update(GameWorld world, double dt) void
    }
    class PowerUpSystem {
        <<planned B-07>>
    }
    class ScoreSystem {
        <<planned B-08>>
    }

    Entity <|-- Bomb
    Entity <|-- Explosion
    Entity <|-- Block
    Entity <|-- PowerUp
    Entity <|-- Player
    Bomb <|-- NormalBomb
    Bomb <|-- PumpedBomb
    Bomb <|-- RemoteBomb
    Block <|-- IndestructibleBlock
    Block <|-- DestructibleBlock
    Block <|-- ReinforcedBlock
    Damageable <|.. Player

    Bomb --> Player : owner
    BombFactory ..> Bomb : creates
    PowerUp --> PowerUpType

    CommandHandler <|.. BombSystem
    GameSystem <|.. BombSystem
    GameSystem <|.. ExplosionSystem
    GameSystem <|.. PowerUpSystem
    BombSystem ..> BombFactory : places
    ExplosionSystem ..> Bomb : detonates
    ExplosionSystem ..> Explosion : creates
    ExplosionSystem ..> Block : isDestroyedBy
    ExplosionSystem ..> Damageable : takeDamage
    PowerUpSystem ..> PowerUp : collects
```

## Patterns and principles shown

| Item | Where |
| --- | --- |
| Inheritance | `Bomb` with three bomb types, `Block` with three block types. |
| Abstract class | `Bomb`, `Block`. |
| Polymorphism | `Bomb.type()`, `Block.isDestroyedBy()`, `Damageable.takeDamage()`. |
| Factory Method | `BombFactory` creates a bomb from `BombType`. |
| Observer | `ScoreSystem` subscribes to `EnemyDefeated` and `GameEnded` on the `EventBus`. |
| Open/Closed | A new bomb or block type is a new subclass; `BombSystem` and `ExplosionSystem` do not change. |
