/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTileSet;
import de.amr.pacmanfx.model.GameLevel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.*;
import static de.amr.pacmanfx.lib.Direction.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.lib.tilemap.TerrainTileSet.TileID.ONE_WAY_DOWN;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static java.util.Objects.requireNonNull;

/**
 * Common ghost base class. The specific ghosts differ in their hunting behavior and their look.
 */
public abstract class Ghost extends MovingActor implements AnimatedActor {

    private final byte personality;
    private final String name;
    private GhostState state;
    private Vector2f revivalPosition;
    private List<Vector2i> specialTerrainTiles = List.of();
    private ActorAnimationMap animationMap;

    /**
     * @param personality ghost personality, allowed values are
     *          {@link de.amr.pacmanfx.Globals#RED_GHOST_SHADOW},
     *          {@link de.amr.pacmanfx.Globals#PINK_GHOST_SPEEDY},
     *          {@link de.amr.pacmanfx.Globals#CYAN_GHOST_BASHFUL} and
     *          {@link de.amr.pacmanfx.Globals#ORANGE_GHOST_POKEY}
     * @param name readable name, used for logging and debugging
     */
    protected Ghost(byte personality, String name) {
        this.personality = requireValidGhostPersonality(personality);
        this.name = requireNonNull(name);
        corneringSpeedUp = -1.25f;
    }

    public void setAnimations(ActorAnimationMap animationMap) {
        this.animationMap = requireNonNull(animationMap);
    }

    @Override
    public Optional<ActorAnimationMap> animations() {
        return Optional.ofNullable(animationMap);
    }

    @Override
    public String toString() {
        return "Ghost{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", visible=" + visible +
                ", x=" + x +
                ", y=" + y +
                ", vx=" + vx +
                ", vy=" + vy +
                ", ax=" + ax +
                ", ay=" + ay +
                ", moveDir=" + moveDir +
                ", wishDir=" + wishDir +
                ", targetTile=" + targetTile +
                ", newTileEntered=" + newTileEntered +
                ", gotReverseCommand=" + gotReverseCommand +
                ", canTeleport=" + canTeleport +
                ", corneringSpeedUp" + corneringSpeedUp +
            '}';
    }

    public byte personality() {
        return personality;
    }

    public String name() {
        return name;
    }

    public void setRevivalPosition(Vector2f position) {
        requireNonNull(position);
        revivalPosition = position;
    }

    public void setSpecialTerrainTiles(List<Vector2i> tiles) {
        specialTerrainTiles = new ArrayList<>(tiles);
    }

    public List<Vector2i> specialTerrainTiles() {
        return specialTerrainTiles;
    }

    /**
     * Subclasses implement this method to define the behavior of the ghost when hunting Pac-Man through
     * the given game level.
     *
     * @param level the game level
     */
    public abstract void hunt(GameLevel level);

    /**
     * Subclasses implement this method to define the target tile of the ghost when hunting Pac-Man through
     * the given game level.
     *
     * @param level the game level
     * @return the current target tile when chasing Pac-Man
     */
    public abstract Vector2i chasingTargetTile(GameLevel level);

    /**
     * Lets the ghost randomly roam through the world.
     *
     * @param level the game level
     */
    public void roam(GameLevel level) {
        Vector2i currentTile = tile();
        if (!level.isPortalAt(currentTile) && (isNewTileEntered() || !moveInfo.moved)) {
            Direction dir = computeRoamingDirection(level, currentTile);
            setWishDir(dir);
        }
        tryMoving(level);
    }

    // try a random direction towards an accessible tile, do not turn back unless there is no other way
    private Direction computeRoamingDirection(GameLevel level, Vector2i currentTile) {
        Direction dir = pseudoRandomDirection();
        int turns = 0;
        while (dir == moveDir.opposite() || !canAccessTile(level, currentTile.plus(dir.vector()))) {
            dir = dir.nextClockwise();
            if (++turns > 4) {
                return moveDir.opposite();  // avoid endless loop
            }
        }
        return dir;
    }

    private Direction pseudoRandomDirection() {
        int rnd = randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }

    @Override
    public boolean canAccessTile(GameLevel level, Vector2i tile) {
        requireNonNull(tile);

        // hunting ghosts cannot move up at certain tiles in Pac-Man game
        if (state == GhostState.HUNTING_PAC) {
            var currentTile = tile();
            if (specialTerrainTiles.contains(tile)
                    && level.worldMap().content(LayerID.TERRAIN, tile) == TerrainTileSet.valueOf(ONE_WAY_DOWN)
                    && currentTile.equals(tile.plus(DOWN.vector()))) {
                Logger.debug("Hunting {} cannot move up to special tile {}", name, tile);
                return false;
            }
        }
        if (level.isDoorAt(tile)) {
            return inAnyOfStates(GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);
        }
        if (level.isInsideWorld(tile)) {
            return !level.isBlockedTile(tile);
        }
        return level.isPortalAt(tile);
    }

    @Override
    public boolean canReverse() {
        return newTileEntered && inAnyOfStates(GhostState.HUNTING_PAC, GhostState.FRIGHTENED);
    }

    // Here begins the state machine part

    /**
     * The current state of this ghost.
     */
    public GhostState state() {
        return state;
    }

    /**
     * @param states ghost states to be checked
     * @return <code>true</code> if this ghost is in any of the given states.
     * If no alternatives are given, an exception is thrown.
     * <code>false</code>
     */
    public boolean inAnyOfStates(GhostState... states) {
        return state != null && isOneOf(state, states);
    }

    /**
     * Changes the state of this ghost.
     *
     * @param state the new state
     */
    public void setState(GhostState state) {
        requireNonNull(state);
        if (this.state == state) {
            Logger.warn("{} is already in state {}", name, state);
        }
        this.state = state;
        // onEntry action:
        switch (state) {
            case LOCKED, HUNTING_PAC -> selectAnimation(ANIM_GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_HOME -> selectAnimation(ANIM_GHOST_EYES);
            case FRIGHTENED -> playAnimation(ANIM_GHOST_FRIGHTENED);
            case EATEN -> {}
        }
    }

    /**
     * Executes a single simulation step for this ghost in the current game level.
     */
    public void update(GameLevel level) {
        requireNonNull(level);
        switch (state) {
            case LOCKED             -> updateStateLocked(level);
            case LEAVING_HOUSE      -> updateStateLeavingHouse(level);
            case HUNTING_PAC        -> updateStateHuntingPac(level);
            case FRIGHTENED         -> updateStateFrightened(level);
            case EATEN              -> updateStateEaten();
            case RETURNING_HOME     -> updateStateReturningToHouse(level);
            case ENTERING_HOUSE     -> updateStateEnteringHouse(level);
        }
    }

    public void eaten(int index) {
        setState(GhostState.EATEN);
        selectAnimation(ANIM_GHOST_NUMBER, index);
    }

    // --- LOCKED ---

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    private void updateStateLocked(GameLevel level) {
        if (level.isInsideHouse(this)) {
            float minY = (level.houseMinTile().y() + 1) * TS + HTS;
            float maxY = (level.houseMaxTile().y() - 1) * TS - HTS;
            setSpeed(level.speedControl().ghostSpeedInsideHouse(level, this));
            move();
            if (y <= minY) {
                setMoveAndWishDir(DOWN);
            } else if (y >= maxY) {
                setMoveAndWishDir(UP);
            }
            y = Math.clamp(y, minY, maxY);
        } else {
            setSpeed(0);
        }
        if (level.pac().powerTimer().isRunning() && !level.victims().contains(this)) {
            updateFrightenedAnimation(level);
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
    private void updateStateLeavingHouse(GameLevel level) {
        float speedInsideHouse = level.speedControl().ghostSpeedInsideHouse(level, this);
        Vector2f houseEntryPosition = level.houseEntryPosition();
        if (y <= houseEntryPosition.y()) {
            // has raised and is outside house
            setPosition(houseEntryPosition);
            setMoveAndWishDir(LEFT);
            newTileEntered = false; // force moving left until new tile is entered
            if (level.pac().powerTimer().isRunning() && !level.victims().contains(this)) {
                setState(GhostState.FRIGHTENED);
            } else {
                setState(GhostState.HUNTING_PAC);
            }
            return;
        }
        // move inside house
        float centerX = x + HTS;
        float houseCenterX = level.houseCenter().x();
        if (differsAtMost(0.5f * speedInsideHouse, centerX, houseCenterX)) {
            // align horizontally and raise
            setX(houseCenterX - HTS);
            setMoveAndWishDir(UP);
        } else {
            // move sidewards until center axis is reached
            setMoveAndWishDir(centerX < houseCenterX ? RIGHT : LEFT);
        }
        setSpeed(speedInsideHouse);
        move();
        if (level.pac().powerTimer().isRunning() && !level.victims().contains(this)) {
            updateFrightenedAnimation(level);
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
    private void updateStateHuntingPac(GameLevel level) {
        // The specific hunting behaviour is defined by the game variant. For example, in Ms. Pac-Man,
        // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
        hunt(level);
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
    private void updateStateFrightened(GameLevel level) {
        float speed = level.isTunnel(tile())
            ? level.speedControl().ghostTunnelSpeed(level, this)
            : level.speedControl().ghostFrightenedSpeed(level, this);
        setSpeed(speed);
        roam(level);
        updateFrightenedAnimation(level);
    }

    private void updateFrightenedAnimation(GameLevel level) {
        if (level.pac().isPowerFadingStarting(level)) {
            playAnimation(ANIM_GHOST_FLASHING);
        } else if (!level.pac().isPowerFading(level)) {
            playAnimation(ANIM_GHOST_FRIGHTENED);
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
    private void updateStateReturningToHouse(GameLevel level) {
        float speed = level.speedControl().ghostSpeedReturningToHouse(level, this);
        Vector2f houseEntry = level.houseEntryPosition();
        if (position().roughlyEquals(houseEntry, speed, 0)) {
            setPosition(houseEntry);
            setMoveAndWishDir(DOWN);
            setState(GhostState.ENTERING_HOUSE);
        } else {
            setSpeed(speed);
            setTargetTile(level.houseLeftDoorTile());
            navigateTowardsTarget(level);
            tryMoving(level);
        }
    }

    // --- ENTERING_HOUSE ---

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void updateStateEnteringHouse(GameLevel level) {
        float speed = level.speedControl().ghostSpeedReturningToHouse(level, this);
        if (position().roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            setPosition(revivalPosition);
            setMoveAndWishDir(UP);
            setState(GhostState.LOCKED);
            return;
        }
        if (y < revivalPosition.y()) {
            setMoveAndWishDir(DOWN);
        } else if (x > revivalPosition.x()) {
            setMoveAndWishDir(LEFT);
        } else if (x < revivalPosition.x()) {
            setMoveAndWishDir(RIGHT);
        }
        setSpeed(speed);
        move();
    }
}