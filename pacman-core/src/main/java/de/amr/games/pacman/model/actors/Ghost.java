/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.checkGhostID;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * There are 4 ghosts with different "personalities".
 *
 * @author Armin Reichert
 */
public class Ghost extends Creature {

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
    private String name;
    private GhostState state;
    private Vector2f revivalPosition;
    private float speedReturningToHouse;
    private float speedInsideHouse;
    private Animations animations;
    private Consumer<Ghost> huntingBehaviour = game -> {};
    private List<Vector2i> cannotMoveUpTiles = List.of();

    /**
     * @param id  The ghost ID. One of
     * {@link GameModel#RED_GHOST},
     * {@link GameModel#PINK_GHOST},
     * {@link GameModel#CYAN_GHOST},
     * {@link GameModel#ORANGE_GHOST}.
     * @param name readable name like "Blinky"
     * @param world the world where this ghost lives (optional)
     */
    public Ghost(byte id, String name, GameWorld world) {
        super(world);
        this.id = checkGhostID(id);
        this.name = checkNotNull(name);
    }

    public void setHuntingBehaviour(Consumer<Ghost> huntingBehaviour) {
        this.huntingBehaviour = checkNotNull(huntingBehaviour);
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "name='" + name + '\'' +
            ", state=" + state +
            ". tile=" + tile() +
            ", newTileEntered=" + newTileEntered +
            ", gotReverseCommand=" + gotReverseCommand +
            ", posX=" + posX +
            ", posY=" + posY +
            '}';
    }

    public byte id() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void setAnimations(Animations animations) {
        this.animations = animations;
    }

    public Optional<Animations> animations() {
        return Optional.ofNullable(animations);
    }

    public boolean insideHouse() {
        return world.isPartOfHouse(tile());
    }

    public void setRevivalPosition(Vector2f position) {
        checkNotNull(position);
        revivalPosition = position;
    }

    public void setCannotMoveUpTiles(List<Vector2i> tiles) {
        cannotMoveUpTiles = new ArrayList<>(tiles);
    }

    public List<Vector2i> cannotMoveUpTiles() {
        return cannotMoveUpTiles;
    }

    public void setSpeedReturningHome(float pixelsPerTick) {
        speedReturningToHouse = pixelsPerTick;
    }

    public void setSpeedInsideHouse(float pixelsPerTick) {
        speedInsideHouse = pixelsPerTick;
    }

    /**
     * Ig frightened, the ghost roams through the maze with frightened speed, except in tunnels,
     * where he moves even slower.
     *
     * @param level game level
     */
    private void roamFrightened(GameLevel level) {
        roam(world.isTunnel(tile()) ? level.ghostSpeedTunnelPct() : level.ghostSpeedFrightenedPct());
    }

    /**
     * Lets the ghost randomly roam through the world.
     *
     * @param speedPct the relative speed (in percentage of base speed)
     */
    public void roam(byte speedPct) {
        Vector2i currentTile = tile();
        if (!world.isPortalAt(currentTile) && (isNewTileEntered() || !moveInfo.moved)) {
            Direction dir = pseudoRandomDirection();
            while (dir == moveDir().opposite()
                || !canAccessTile(currentTile.plus(dir.vector()))) {
                dir = dir.nextClockwise();
            }
            setWishDir(dir);
        }
        setSpeedPct(speedPct);
        tryMoving();
    }

    private Direction pseudoRandomDirection() {
        int rnd = Globals.randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }

    @Override
    public boolean canAccessTile(Vector2i tile) {
        checkTileNotNull(tile);
        checkNotNull(world);

        // hunting ghosts cannot move up at certain tiles in Pac-Man game
        if (state == HUNTING_PAC) {
            var currentTile = tile();
            if (cannotMoveUpTiles.contains(currentTile)) {
                if (currentTile.plus(UP.vector()).equals(tile)) {
                    Logger.trace("Hunting {} cannot move up at {}", name, currentTile);
                    return false;
                }
            }
        }
        if (world.isDoorAt(tile)) {
            return inState(ENTERING_HOUSE, LEAVING_HOUSE);
        }
        if (world.isInsideWorld(tile)) {
            return !world.isBlockedTile(tile);
        }
        return world.isPortalAt(tile);
    }

    @Override
    public boolean canReverse() {
        return newTileEntered && inState(HUNTING_PAC, FRIGHTENED);
    }

    public void selectAnimation(String name) {
        selectAnimation(name, 0);
    }

    public void selectAnimation(String name, int index) {
        if (animations != null) {
            animations.select(name, index);
        } else {
            Logger.warn("Trying to select animation '{}' (index: {}) before animations have been created!", name, index);
        }
    }

    public void startAnimation() {
        if (animations != null) {
            animations.startSelected();
        } else {
            Logger.warn("Trying to start animation before animations have been created!");
        }
    }

    public void stopAnimation() {
        if (animations != null) {
            animations.stopSelected();
        } else {
            Logger.warn("Trying to stop animation before animations have been created!");
        }
    }

    public void resetAnimation() {
        if (animations != null) {
            animations.resetSelected();
        } else {
            Logger.warn("Trying to reset animation before animations have been created!");
        }
    }

    // Here begins the state machine part

    /**
     * The current state of this ghost.
     */
    public GhostState state() {
        return state;
    }

    /**
     * @param stateAlternatives ghost states to be checked
     * @return <code>true</code> if this ghost is in any of the given states.
     * If no alternatives are given, an exception is thrown.
     * <code>false</code>
     */
    public boolean inState(GhostState... stateAlternatives) {
        if (stateAlternatives.length == 0) {
            throw new IllegalArgumentException("No states to check for ghost");
        }
        return oneOf(state, stateAlternatives);
    }

    /**
     * Changes the state of this ghost.
     *
     * @param state the new state
     */
    public void setState(GhostState state) {
        checkNotNull(state);
        if (this.state == state) {
            Logger.trace("{} is already in state {}", name, state);
        }
        this.state = state;
        // onEntry action:
        switch (state) {
            case LOCKED, HUNTING_PAC -> selectAnimation(ANIM_GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_HOME -> selectAnimation(ANIM_GHOST_EYES);
            case FRIGHTENED -> selectAnimation(ANIM_GHOST_FRIGHTENED);
            case EATEN -> {}
        }
    }

    /**
     * Executes a single simulation step for this ghost in the current game level.
     *
     * @param game game variant
     */
    public void update(GameModel game) {
        checkNotNull(game);
        switch (state) {
            case LOCKED             -> updateStateLocked(game);
            case LEAVING_HOUSE      -> updateStateLeavingHouse(game);
            case HUNTING_PAC        -> updateStateHuntingPac(game);
            case FRIGHTENED         -> updateStateFrightened(game);
            case EATEN              -> updateStateEaten();
            case RETURNING_HOME     -> updateStateReturningToHouse();
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
    private void updateStateLocked(GameModel game) {
        if (insideHouse()) {
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
        if (game.powerTimer().isRunning() && !game.victims().contains(this)) {
            updateFrightenedAnimation(game);
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
    private void updateStateLeavingHouse(GameModel game) {
        Vector2f houseEntryPosition = world.houseEntryPosition();
        if (posY() <= houseEntryPosition.y()) {
            // has raised and is outside house
            setPosition(houseEntryPosition);
            setMoveAndWishDir(LEFT);
            newTileEntered = false; // force moving left until new tile is entered
            if (game.powerTimer().isRunning() && !game.victims().contains(this)) {
                setState(FRIGHTENED);
            } else {
                setState(HUNTING_PAC);
            }
            return;
        }
        // move inside house
        float centerX = center().x();
        float houseCenterX = world.houseCenter().x();
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
        if (game.powerTimer().isRunning() && !game.victims().contains(this)) {
            updateFrightenedAnimation(game);
        } else {
            selectAnimation(ANIM_GHOST_NORMAL);
        }
    }

    // --- HUNTING_PAC ---

    /**
     * In each game level there are 8 alternating (scattering vs. chasing) hunting phases of different duration. The first
     * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
     * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The last hunting phase
     * is an "infinite" chasing phase.
     * <p>
     */
    private void updateStateHuntingPac(GameModel game) {
        // The specific hunting behaviour is defined by the game variant. For example, in Ms. Pac-Man,
        // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
        huntingBehaviour.accept(this);
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
    private void updateStateFrightened(GameModel game) {
        game.level().ifPresent(level -> {
            roamFrightened(level);
            updateFrightenedAnimation(game);
        });
    }

    private void updateFrightenedAnimation(GameModel game) {
        if (game.isPowerFadingStarting()) {
            selectAnimation(ANIM_GHOST_FLASHING);
        } else if (!game.isPowerFading()) {
            selectAnimation(ANIM_GHOST_FRIGHTENED);
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
    private void updateStateReturningToHouse() {
        Vector2f houseEntry = world.houseEntryPosition();
        if (position().almostEquals(houseEntry, 0.5f * speedReturningToHouse, 0)) {
            setPosition(houseEntry);
            setMoveAndWishDir(DOWN);
            setState(ENTERING_HOUSE);
        } else {
            setSpeed(speedReturningToHouse);
            setTargetTile(world.houseLeftDoorTile());
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
        float speed = speedReturningToHouse; // correct?
        if (position().almostEquals(revivalPosition, speed / 2, speed / 2)) {
            setPosition(revivalPosition);
            setMoveAndWishDir(UP);
            setState(LOCKED);
            return;
        }
        if (posY < revivalPosition.y()) {
            setMoveAndWishDir(DOWN);
        } else if (posX > revivalPosition.x()) {
            setMoveAndWishDir(LEFT);
        } else if (posX < revivalPosition.x()) {
            setMoveAndWishDir(RIGHT);
        }
        setSpeed(speed);
        move();
    }
}