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
import de.amr.pacmanfx.core.model.component.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.MovementSystem;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static de.amr.basics.math.Direction.*;
import static de.amr.pacmanfx.core.Validations.differsAtMost;
import static de.amr.pacmanfx.core.Validations.stateIsOneOf;
import static java.util.Objects.requireNonNull;

/**
 * Common ghost base class. The specific ghosts differ in their hunting behavior and their look.
 */
public class Ghost extends Actor implements UpdatableEntity {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private final byte personality;
    private ObjectProperty<GhostState> state;
    private Set<Vector2i> specialTerrainTiles = Set.of();
    private Vector2f startPosition;
    private House home;

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
        registerComponent(Movement.class, new Movement());
        registerComponent(WorldMovement.class, new WorldMovement());
        registerComponent(WorldMovementPolicy.class, new GhostWorldMovementPolicy());
        if (personality == GameModel.RED_GHOST_SHADOW) {
            registerComponent(Elroy.class, new Elroy());
        }

        this.name = requireNonNull(name);
        this.personality = Validations.requireValidGhostPersonality(personality);

        worldMovement().corneringSpeedDelta = -1.25f;
    }

    public WorldMovement worldMovement() {
        return assertComponent(WorldMovement.class);
    }

    @Override
    public void update(GameContext gameContext) {
        //TODO simplify!
        final ActorSpeedRules speedRules = gameContext.model().rules().actorSpeedRules();
        final float speed = speedRules.ghostSpeed(gameContext, this);
        switch (state()) {
            case LOCKED         -> updateStateLocked(gameContext, speed);
            case LEAVING_HOUSE  -> updateStateLeavingHouse(gameContext, speed);
            case HUNTING_PAC    -> updateStateHuntingPac(gameContext, speed);
            case FRIGHTENED     -> updateStateFrightened(gameContext, speed);
            case EATEN          -> updateStateEaten();
            case RETURNING_HOME -> updateStateReturningToHouse(gameContext, speed);
            case ENTERING_HOUSE -> updateStateEnteringHouse(gameContext, speed);
        }
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

    public House home() {
        return home;
    }

    public void setHome(House home) {
        this.home = home;
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
            ", state=" + state() +
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


    // Here begins the state machine part

    public ObjectProperty<GhostState> stateProperty() {
        if (state == null) {
            state = new SimpleObjectProperty<>(DEFAULT_STATE);
        }
        return state;
    }

    /**
     * The current state of this ghost.
     */
    public GhostState state() {
        return state != null ? stateProperty().get() : DEFAULT_STATE;
    }

    /**
     * @param states ghost states to be checked
     * @return <code>true</code> if this ghost is in any of the given states.
     * If no alternatives are given, an exception is thrown.
     * <code>false</code>
     */
    public boolean inAnyOfStates(Collection<GhostState> states) {
        return state != null && stateIsOneOf(state(), states);
    }

    /**
     * Changes the state of this ghost.
     *
     * @param newState the new state
     */
    public void setState(GhostState newState) {
        requireNonNull(newState);
        if (state() == newState) {
            Logger.debug("{} is already in state {}", name(), newState);
        }
        stateProperty().set(newState);

        // "onEntry" action:
        switch (newState) {
            case LOCKED, HUNTING_PAC -> animations.select(CommonAnimationID.GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_HOME -> animations.select(CommonAnimationID.GHOST_EYES);
            case FRIGHTENED -> {
                animations.select(CommonAnimationID.GHOST_FRIGHTENED);
                animations.playSelected();
            }
            case EATEN -> {}
        }
    }

    // --- LOCKED ---

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    private void updateStateLocked(GameContext gameContext, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        if (home.isVisitedBy(this)) {
            final float minY = (home.minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (home.maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position().y <= minY) {
                worldMovementSystem.setMoveDir(this, DOWN);
                worldMovementSystem.setWishDir(this, DOWN);
            } else if (position().y >= maxY) {
                worldMovementSystem.setMoveDir(this, UP);
                worldMovementSystem.setWishDir(this, UP);
            }
            position().setY(Math.clamp(position().y, minY, maxY));
            worldMovementSystem.setSpeed(this, speed);
            movementSystem.moveAccelerated(this);
        } else {
            worldMovementSystem.setSpeed(this, 0);
        }
        if (isInDanger(gameContext)) {
            final GameLevel level = gameContext.assertLevel();
            playFrightenedAnimation(level, level.entities().pac());
        } else {
            animations.select(CommonAnimationID.GHOST_NORMAL);
        }
    }

    // --- LEAVING_HOUSE ---

    /**
     * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
     * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
     * and then raises up until he has passed the door on top of the house.
     * <p>
     * The ghost speed is slower than outside, but I do not know the exact value.
     */
    private void updateStateLeavingHouse(GameContext gameContext, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();

        final Vector2f houseEntryPosition = home.entryPosition();
        if (position().y <= houseEntryPosition.y()) {
            // outside at house entry
            position().setY(houseEntryPosition.y());
            worldMovementSystem.setMoveDir(this, LEFT);
            worldMovementSystem.setWishDir(this, LEFT);
            worldMovement().setNewTileEntered(false); // don't change direction until new tile is entered by moving
            setState(isInDanger(gameContext) ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
        }
        else {
            // still inside house
            final float centerX = position().x + WorldMap.HTS;
            final float houseCenterX = home.center().x();
            if (differsAtMost(0.5f * speed, centerX, houseCenterX)) {
                // align horizontally and raise
                position().setX(houseCenterX - WorldMap.HTS);
                worldMovementSystem.setMoveDir(this, UP);
                worldMovementSystem.setWishDir(this, UP);
            } else {
                // move sidewards until center axis is reached
                worldMovementSystem.setMoveDir(this, centerX < houseCenterX ? RIGHT : LEFT);
                worldMovementSystem.setWishDir(this, centerX < houseCenterX ? RIGHT : LEFT);
            }
            worldMovementSystem.setSpeed(this, speed);
            movementSystem.moveAccelerated(this);

            if (isInDanger(gameContext)) {
                playFrightenedAnimation(level, level.entities().pac());
            } else {
                animations.select(CommonAnimationID.GHOST_NORMAL);
            }
        }
    }

    private boolean isInDanger(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        return level.entities().pac().powerTimer().isRunning() && !level.isInGhostKilledChain(this);
    }

    // --- HUNTING_PAC ---

    /**
     * In each game level there are 8 alternating (scattering vs. chasing) hunting phases of different duration. The first
     * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
     * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The last hunting phase
     * is an "infinite" chasing phase.
     * <p>
     */
    private void updateStateHuntingPac(GameContext gameContext, float speed) {
        // The specific hunting behavior is defined by the game variant. For example, in Ms. Pac-Man,
        // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
        hunt(gameContext, speed);
    }

    // --- FRIGHTENED ---

    /**
     * <p>
     * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
     * his power. Speed is about half of the normal speed. Reversing the move direction is not allowed in this state either.
     * </p><p>
     * Frightened ghosts choose a "random" direction when they enter a new tile. If the chosen direction
     * can be taken, it is stored and taken as soon as possible. Otherwise, the remaining directions are checked in
     * clockwise order.
     * </p>
     *
     * @see <a href="https://www.youtube.com/watch?v=eFP0_rkjwlY">YouTube: How Frightened Ghosts Decide Where to Go</a>
     */
    private void updateStateFrightened(GameContext gameContext, float speed) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();

        worldMovementSystem.setSpeed(this, speed);
        roam(gameContext);
        playFrightenedAnimation(level, level.entities().pac());
    }

    private void playFrightenedAnimation(GameLevel level, Pac pac) {
        if (pac.isPowerFadingStarting(level)) {
            animations.select(CommonAnimationID.GHOST_FLASHING);
            animations.playSelected();
        } else if (!pac.isPowerFading(level)) {
            animations.select(CommonAnimationID.GHOST_FRIGHTENED);
            animations.playSelected();
        }
    }

    // --- EATEN ---

    /**
     * After a ghost is eaten by Pac-Man, he is displayed for a short time as the number of points earned for eating him.
     * The value doubles for each ghost eaten using the power of the same energizer.
     */
    private void updateStateEaten() {
    }

    // --- RETURNING_TO_HOUSE ---

    /**
     * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns
     * to the ghost house to be revived. Hallelujah!
     */
    private void updateStateReturningToHouse(GameContext gameContext, float speed) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final Vector2f houseEntry = home.entryPosition();
        //TODO
        final Vector2f positionVec =  position().asVector2f();
        if (positionVec.roughlyEquals(houseEntry, speed, 0)) {
            position().set(houseEntry.x(), houseEntry.y());
            worldMovementSystem.setMoveDir(this, DOWN);
            worldMovementSystem.setWishDir(this, DOWN);
            setState(GhostState.ENTERING_HOUSE);
        } else {
            worldMovement().setTargetTile(home.leftDoorTile());
            worldMovementSystem.setSpeed(this, speed);
            worldMovementSystem.navigateTowardsTarget(this, gameContext);
            worldMovementSystem.tryMovingOrTeleporting(this, gameContext);
        }
    }

    // --- ENTERING_HOUSE ---

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void updateStateEnteringHouse(GameContext gameContext, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final Vector2f revivalPosition = WorldMap.halfTileRightOf(home.ghostRevivalTile(personality()));
        final Vector2f positionVec = position().asVector2f();
        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position().set(revivalPosition.x(), revivalPosition.y());
            worldMovementSystem.setMoveDir(this, UP);
            worldMovementSystem.setWishDir(this, UP);
            setState(GhostState.LOCKED);
            return;
        }
        if (position().y < revivalPosition.y()) {
            worldMovementSystem.setMoveDir(this, DOWN);
            worldMovementSystem.setWishDir(this, DOWN);
        }
        else if (position().x > revivalPosition.x()) {
            worldMovementSystem.setMoveDir(this, LEFT);
            worldMovementSystem.setWishDir(this, LEFT);
        }
        else if (position().x < revivalPosition.x()) {
            worldMovementSystem.setMoveDir(this, RIGHT);
            worldMovementSystem.setWishDir(this, RIGHT);
        }

        worldMovementSystem.setSpeed(this, speed);
        movementSystem.moveAccelerated(this);
    }
}