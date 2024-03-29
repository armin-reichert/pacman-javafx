/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.House;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * There are 4 ghosts with different "personalities".
 *
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimationDirector {

    public static final String ANIM_GHOST_NORMAL     = "normal";
    public static final String ANIM_GHOST_FRIGHTENED = "frightened";
    public static final String ANIM_GHOST_EYES       = "eyes";
    public static final String ANIM_GHOST_FLASHING   = "flashing";
    public static final String ANIM_GHOST_NUMBER     = "number";
    public static final String ANIM_BLINKY_DAMAGED   = "damaged";
    public static final String ANIM_BLINKY_STRETCHED = "stretched";
    public static final String ANIM_BLINKY_PATCHED   = "patched";
    public static final String ANIM_BLINKY_NAKED     = "naked";

    private final byte id;
    private GhostState state;
    private Consumer<Ghost> huntingBehavior;
    private Consumer<Ghost> frightenedBehavior;
    private House house;
    private Vector2f revivalPosition;
    private float speedReturningToHouse;
    private float speedInsideHouse;
    private Animations animations;
    private Map<Vector2i, List<Direction>> forbiddenMoves = Collections.emptyMap();

    /**
     * @param id  The ghost ID. One of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST}, {@link GameModel#CYAN_GHOST},
     * {@link GameModel#ORANGE_GHOST}.
     * @param name the ghost's readable name e.g. "Pinky"
     */
    public Ghost(byte id, String name) {
        super(name);
        checkGhostID(id);
        this.id = id;
        reset();
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "id=" + id +
            ", state=" + state +
            '}';
    }

    public byte id() {
        return id;
    }

    public void setAnimations(Animations animations) {
        this.animations = animations;
    }

    @Override
    public Optional<Animations> animations() {
        return Optional.ofNullable(animations);
    }

    public void setHouse(House house) {
        checkNotNull(house);
        this.house = house;
    }

    public boolean insideHouse(House house) {
        return house.contains(tile());
    }

    public void setRevivalPosition(Vector2f position) {
        checkNotNull(position);
        revivalPosition = position;
    }

    public void setSpeedReturningToHouse(float pixelsPerTick) {
        speedReturningToHouse = pixelsPerTick;
    }

    public void setSpeedInsideHouse(float pixelsPerTick) {
        speedInsideHouse = pixelsPerTick;
    }

    public void setForbiddenMoves(Map<Vector2i, List<Direction>> moves) {
        forbiddenMoves = moves;
    }

    public void setHuntingBehavior(Consumer<Ghost> function) {
        checkNotNull(function);
        huntingBehavior = function;
    }

    /**
     * @param function function specifying the behavior when frightened
     */
    public void setFrightenedBehavior(Consumer<Ghost> function) {
        checkNotNull(function);
        frightenedBehavior = function;
    }

    @Override
    public boolean canAccessTile(Vector2i tile) {
        checkTileNotNull(tile);

        // hunting ghosts cannot move up at certain tiles in Pac-Man game
        if (state == HUNTING_PAC) {
            var currentTile = tile();
            if (forbiddenMoves.containsKey(currentTile)) {
                for (Direction dir : forbiddenMoves.get(currentTile)) {
                    if (currentTile.plus(dir.vector()).equals(tile)) {
                        Logger.trace("Hunting {} cannot move {} at {}", name(), dir, currentTile);
                        return false;
                    }
                }
            }
        }
        if (house.door().occupies(tile)) {
            return is(ENTERING_HOUSE, LEAVING_HOUSE);
        }
        if (world.insideBounds(tile)) {
            return !world.isWall(tile);
        }
        return world.belongsToPortal(tile);
    }

    @Override
    public boolean canReverse() {
        return newTileEntered && is(HUNTING_PAC, FRIGHTENED);
    }

    // Here begins the state machine part

    /**
     * The current state of this ghost.
     */
    public GhostState state() {
        return state;
    }

    /**
     * @param alternatives ghost states to be checked
     * @return <code>true</code> if this ghost is in any of the given states. If no alternatives are given, returns
     * <code>false</code>
     */
    public boolean is(GhostState... alternatives) {
        if (state == null) {
            throw new IllegalStateException("Ghost state is not defined");
        }
        return oneOf(state, alternatives);
    }

    /**
     * Changes the state of this ghost.
     *
     * @param state the new state
     */
    public void setState(GhostState state) {
        checkNotNull(state);
        if (this.state == state) {
            Logger.trace("{} is already in state {}", name(), state);
        }
        this.state = state;
        switch (state) {
            case LOCKED, HUNTING_PAC -> selectAnimation(ANIM_GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_TO_HOUSE -> selectAnimation(ANIM_GHOST_EYES);
            case FRIGHTENED -> selectAnimation(ANIM_GHOST_FRIGHTENED);
            default -> {}
        }
    }

    /**
     * Executes a single simulation step for this ghost in the current game level.
     *
     * @param pac Pac-Man or Ms. Pac-Man
     */
    public void update(Pac pac) {
        checkNotNull(pac);
        switch (state) {
            case LOCKED             -> updateStateLocked(pac);
            case LEAVING_HOUSE      -> updateStateLeavingHouse(pac);
            case HUNTING_PAC        -> updateStateHuntingPac();
            case FRIGHTENED         -> updateStateFrightened(pac);
            case EATEN              -> updateStateEaten(pac);
            case RETURNING_TO_HOUSE -> updateStateReturningToHouse();
            case ENTERING_HOUSE     -> updateStateEnteringHouse();
        }
    }

    public void eaten(int index) {
        setState(EATEN);
        selectAnimation(ANIM_GHOST_NUMBER, index);
    }

    // --- LOCKED ---

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    private void updateStateLocked(Pac pac) {
        if (insideHouse(house)) {
            float minY = revivalPosition.y() - 4, maxY = revivalPosition.y() + 4;
            setSpeed(speedInsideHouse);
            move();
            if (posY <= minY) {
                setMoveAndWishDir(DOWN);
            } else if (posY >= maxY) {
                setMoveAndWishDir(UP);
            }
            posY = clamp(posY, minY, maxY);
        } else {
            setSpeed(0);
        }
        if (pac.powerTimer().isRunning() && !pac.victims().contains(this)) {
            updateFrightenedAnimation(pac);
        } else {
            selectAnimation(ANIM_GHOST_NORMAL);
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
    private void updateStateLeavingHouse(Pac pac) {
        Vector2f houseEntryPosition = house.door().entryPosition();
        if (posY() <= houseEntryPosition.y()) {
            // has raised and is outside house
            setPosition(houseEntryPosition);
            setMoveAndWishDir(LEFT);
            newTileEntered = false; // force moving left until new tile is entered
            if (pac.powerTimer().isRunning() && !pac.victims().contains(this)) {
                setState(FRIGHTENED);
            } else {
                setState(HUNTING_PAC);
            }
            return;
        }
        // move inside house
        float centerX = center().x();
        float houseCenterX = house.center().x();
        if (differsAtMost(0.5f * speedInsideHouse, centerX, houseCenterX)) {
            // align horizontally and raise
            setPosX(houseCenterX - HTS);
            setMoveAndWishDir(UP);
        } else {
            // move sidewards until center axis is reached
            setMoveAndWishDir(centerX < houseCenterX ? RIGHT : LEFT);
        }
        setSpeed(speedInsideHouse);
        move();
        if (pac.powerTimer().isRunning() && !pac.victims().contains(this)) {
            updateFrightenedAnimation(pac);
        } else {
            selectAnimation(ANIM_GHOST_NORMAL);
        }
    }

    // --- HUNTING_PAC ---

    /**
     * In each game level there are 4 alternating (scattering vs. chasing) hunting phases of different duration. The first
     * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
     * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The 4th hunting phase
     * is an "infinite" chasing phase.
     * <p>
     */
    private void updateStateHuntingPac() {
        huntingBehavior.accept(this);
    }

    // --- FRIGHTENED ---

    /**
     * When frightened, a ghost moves randomly through the world, at each new tile he randomly decides where to move next.
     * Reversing the move direction is not allowed in this state either.
     * <p>
     * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
     * his power. Speed is about half of the normal speed.
     */
    private void updateStateFrightened(Pac pac) {
        frightenedBehavior.accept(this);
        updateFrightenedAnimation(pac);
    }

    private void updateFrightenedAnimation(Pac pac) {
        if (pac.isPowerFadingStarting()) {
            selectAnimation(ANIM_GHOST_FLASHING);
        } else if (!pac.isPowerFading()) {
            selectAnimation(ANIM_GHOST_FRIGHTENED);
        }
    }

    // --- EATEN ---

    /**
     * After a ghost is eaten by Pac-Man, he is displayed for a short time as the number of points earned for eating him.
     * The value doubles for each ghost eaten using the power of the same energizer.
     */
    private void updateStateEaten(Pac pac) {
    }

    // --- RETURNING_TO_HOUSE ---

    /**
     * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns
     * to the ghost house to be revived. Hallelujah!
     */
    private void updateStateReturningToHouse() {
        Vector2f houseEntry = house.door().entryPosition();
        if (position().almostEquals(houseEntry, 0.5f * speedReturningToHouse, 0)) {
            setPosition(houseEntry);
            setMoveAndWishDir(DOWN);
            setState(ENTERING_HOUSE);
        } else {
            setSpeed(speedReturningToHouse);
            setTargetTile(house.door().leftWing());
            navigateTowardsTarget();
            tryMoving();
        }
    }

    // --- ENTERING_HOUSE ---

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void updateStateEnteringHouse() {
        Vector2f houseCenter = house.center();
        if (posY >= houseCenter.y()) {
            // reached ground
            setPosY(houseCenter.y());
            if (revivalPosition.x() < posX) {
                setMoveAndWishDir(LEFT);
            } else if (revivalPosition.x() > posX) {
                setMoveAndWishDir(RIGHT);
            }
        }
        setSpeed(speedReturningToHouse);
        move();
        if (posY >= revivalPosition.y() && differsAtMost(0.5 * speedReturningToHouse, posX, revivalPosition.x())) {
            setPosition(revivalPosition);
            setMoveAndWishDir(UP);
            setState(LOCKED);
        }
    }
}