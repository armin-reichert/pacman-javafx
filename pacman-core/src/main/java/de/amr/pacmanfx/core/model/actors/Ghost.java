/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.world.WorldMovement;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.ghost.GhostStateMachine;
import de.amr.pacmanfx.core.model.world.House;

import java.util.Collection;
import java.util.Set;

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

    public Ghost(byte personality, String name) {
        this.name = requireNonNull(name);
        this.personality = Validations.requireValidGhostPersonality(personality);

        registerComponent(Movement.class, new Movement());
        registerComponent(WorldMovement.class, new WorldMovement());
        registerComponent(WorldMovementPolicy.class, new GhostWorldMovementPolicy());
        registerComponent(GhostStateComponent.class, new GhostStateComponent());
        //TODO call this in the actor factories of the different game variants
        if (personality == GameModel.RED_GHOST_SHADOW) {
            registerComponent(Elroy.class, new Elroy());
        }

        worldMovement().corneringSpeedDelta = -1.25f;
    }

    /**
     * @return this ghost's personality, see {@link GameModel#RED_GHOST_SHADOW},
     * {@link GameModel#PINK_GHOST_SPEEDY}, {@link GameModel#CYAN_GHOST_BASHFUL} and
     * {@link GameModel#ORANGE_GHOST_POKEY}.
     */
    public byte personality() {
        return personality;
    }

    public WorldMovement worldMovement() {
        return assertComponent(WorldMovement.class);
    }

    public GhostState state() {
        return assertComponent(GhostStateComponent.class).state();
    }

    public void setState(GhostState state) {
        requireNonNull(state);
        assertComponent(GhostStateComponent.class).setState(state);
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

    public House house() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
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
    public void update(GameContext gameContext) {
        final GhostStateMachine stateMachine = gameContext.systems().ghostStateMachine;
        stateMachine.update(gameContext, this);
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "personality=" + personality +
            ", state=" + state() +
            ", specialTerrainTiles=" + specialTerrainTiles +
            ", startPosition=" + startPosition +
            super.toString() +
            '}';
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
}