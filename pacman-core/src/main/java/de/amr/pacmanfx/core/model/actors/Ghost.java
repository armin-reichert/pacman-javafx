/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateMachine;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.world.WorldMovement;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.House;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static de.amr.basics.math.Direction.*;
import static de.amr.pacmanfx.core.Validations.stateIsOneOf;
import static java.util.Objects.requireNonNull;

/**
 * Common ghost base class. The specific ghosts differ in their hunting behavior and their look.
 */
public class Ghost extends Actor implements UpdatableEntity {

    private final byte personality;

    private Set<Vector2i> specialTerrainTiles = Set.of();
    private Vector2f startPosition;
    private House house;

    private Function<GameLevel, Vector2i> chasingTargetTileStrategy = _ -> null;

    /**
     * Default hunting behavior is to retreat towards the scatter tile in scatter phase
     * and to go towards current target tile in chasing phase.
     */
    private BiConsumer<GameContext, Float> huntingStrategy = (gameContext, speed) -> {
        requireNonNull(gameContext);
        requireNonNull(speed);

        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();
        
        worldMovementSystem.setSpeed(this, speed);

        final Vector2i targetTile = level.huntingRules().isChasing()
            ? chasingTargetTileStrategy.apply(level)
            : level.worldMap().terrainLayer().ghostScatterTile(personality());

        worldMovementSystem.tryMovingTowardsTargetTile(this, gameContext, targetTile);
    };

    public Ghost(byte personality, String name) {
        this.name = requireNonNull(name);
        this.personality = Validations.requireValidGhostPersonality(personality);

        registerComponent(Movement.class, new Movement());
        registerComponent(WorldMovement.class, new WorldMovement());
        registerComponent(WorldMovementPolicy.class, new GhostWorldMovementPolicy());
        registerComponent(GhostStateMachine.class, new GhostStateMachine());
        if (personality == GameModel.RED_GHOST_SHADOW) {
            registerComponent(Elroy.class, new Elroy());
        }

        worldMovement().corneringSpeedDelta = -1.25f;
    }

    public WorldMovement worldMovement() {
        return assertComponent(WorldMovement.class);
    }


    @Override
    public void update(GameContext gameContext) {
        assertComponent(GhostStateMachine.class).update(gameContext, this);
    }

    public GhostState state() {
        return assertComponent(GhostStateMachine.class).state();
    }

    public void setState(GhostState state) {
        requireNonNull(state);
        assertComponent(GhostStateMachine.class).setState(this, state);
    }

    /**
     * @param states ghost states to be checked
     * @return <code>true</code> if ghost ghost is in any of the given states.
     * If no alternatives are given, an exception is thrown.
     * <code>false</code>
     */
    public boolean inAnyOfStates(Collection<GhostState> states) {
        return stateIsOneOf(state(), states);
    }

    public void setHuntingStrategy(BiConsumer<GameContext, Float> huntingStrategy) {
        this.huntingStrategy = requireNonNull(huntingStrategy);
    }

    public Function<GameLevel, Vector2i> chasingTargetTileStrategy() {
        return chasingTargetTileStrategy;
    }

    public void setChasingTargetTileStrategy(Function<GameLevel, Vector2i> chasingTargetTileStrategy) {
        this.chasingTargetTileStrategy = requireNonNull(chasingTargetTileStrategy);
    }

    public House house() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    /**
     * @return this ghost's personality, see {@link GameModel#RED_GHOST_SHADOW},
     * {@link GameModel#PINK_GHOST_SPEEDY}, {@link GameModel#CYAN_GHOST_BASHFUL} and
     * {@link GameModel#ORANGE_GHOST_POKEY}.
     */
    public byte personality() {
        return personality;
    }

    public void setSpecialTerrainTiles(Set<Vector2i> tiles) {
        specialTerrainTiles = Set.copyOf(tiles);
    }

    public Set<Vector2i> specialTerrainTiles() {
        return specialTerrainTiles;
    }

    public void setStartPosition(Vector2f startPosition) {
        this.startPosition = startPosition;
    }

    public Vector2f startPosition() {
        return startPosition;
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "personality=" + personality +
            ", state=" + assertComponent(GhostStateMachine.class).state() +
            ", specialTerrainTiles=" + specialTerrainTiles +
            ", startPosition=" + startPosition +
            super.toString() +
            '}';
    }

    public void hunt(GameContext gameContext, float speed) {
        huntingStrategy.accept(gameContext, speed);
    }

    /**
     * Notifies this ghost about Pac-Man's assassination so he can react accordingly (send condolence message etc.)
     * @param ignored the game level where this happens
     */
    public void onPacKilled(GameLevel ignored) {
        if (hasComponent(Elroy.class)) {
            assertComponent(Elroy.class).setEnabled(false);
        }
    }

    /**
     * Lets the ghost roam through the current level's world.
     * <p>
     * <cite>
            Roam if you want to, roam around the world!<br>
            Roam if you want to, without wings without wheels!<br>
            Roam if you want to, roam around the world!<br>
            Roam if you want to, without anything but the love we feel!
     </cite>
     */
    public void roam(GameContext gameContext) {
        requireNonNull(gameContext);

        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();

        final Vector2i tile = WorldMovementSystem.computeTile(this);
        final boolean teleporting = level.worldMap().terrainLayer().isTileInPortalSpace(tile);

        final boolean stuck = !worldMovement().info.moved;
        if ((worldMovement().isNewTileEntered() || stuck) && !teleporting) {
            final Direction dir = computeRoamingDirection(gameContext, tile);
            worldMovementSystem.setWishDir(this, dir);
            Logger.debug("Ghost {} takes random wish direction {}", name, dir);
        }
        worldMovementSystem.tryMovingOrTeleporting(this, gameContext);
    }

    // try a random direction towards an accessible tile, do not turn back unless there is no other way
    private Direction computeRoamingDirection(GameContext gameContext, Vector2i currentTile) {
        final WorldMovementPolicy policy = assertComponent(WorldMovementPolicy.class);

        Direction dir = pseudoRandomDirection();
        int turns = 0;
        while (dir == worldMovement().moveDir().opposite()
            || !policy.canAccessTile(gameContext, this, currentTile.plus(dir.vector()))) {
            dir = dir.nextClockwise();
            if (++turns > 4) {
                return worldMovement().moveDir().opposite();  // avoid endless loop
            }
        }
        return dir;
    }

    private Direction pseudoRandomDirection() {
        final int rnd = RandomNumberSupport.randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }
}