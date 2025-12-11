/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.math.RandomNumberSupport;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.HuntingPhase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.*;
import static de.amr.pacmanfx.lib.Direction.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static java.util.Objects.requireNonNull;

/**
 * Common ghost base class. The specific ghosts differ in their hunting behavior and their look.
 */
public abstract class Ghost extends MovingActor {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private final byte personality;
    private ObjectProperty<GhostState> state;
    private List<Vector2i> specialTerrainTiles = List.of();
    private Vector2f startPosition;
    private House home;

    protected Ghost(byte personality, String name) {
        super(name);
        this.personality = Validations.requireValidGhostPersonality(personality);
        corneringSpeedUp = -1.25f;
    }

    /**
     * Notifies this ghost about Pac-Man's assassination so he can react accordingly (send condolence message etc.)
     * @param level the game level where this happens
     */
    public abstract void onPacKilled(GameLevel level);

    public void setHome(House home) {
        this.home = home;
    }

    public House home() {
        return home;
    }

    /**
     * @return this ghost's personality, see {@link de.amr.pacmanfx.Globals#RED_GHOST_SHADOW},
     * {@link de.amr.pacmanfx.Globals#PINK_GHOST_SPEEDY}, {@link de.amr.pacmanfx.Globals#CYAN_GHOST_BASHFUL} and
     * {@link de.amr.pacmanfx.Globals#ORANGE_GHOST_POKEY}.
     */
    public final byte personality() {
        return personality;
    }

    public void setSpecialTerrainTiles(List<Vector2i> tiles) {
        specialTerrainTiles = new ArrayList<>(tiles);
    }

    public List<Vector2i> specialTerrainTiles() {
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
                "name=" + name() +
                ", state=" + (state != null ? state() : DEFAULT_STATE) +
                ", visible=" + isVisible() +
                ", position=" + position() +
                ", velocity=" + velocity() +
                ", acceleration=" + acceleration() +
                ", moveDir=" + moveDir() +
                ", wishDir=" + wishDir() +
                ", targetTile=" + targetTile() +
                ", newTileEntered=" + newTileEntered +
                ", turnBackRequested=" + turnBackRequested +
                ", canTeleport=" + canTeleport +
                ", corneringSpeedUp" + corneringSpeedUp +
                '}';
    }

    /**
     * Default hunting behavior is to retreat towards the scatter tile in scatter phase
     * and to go towards current target tile in chasing phase.
     *
     * @param level the game level this ghost lives in
     * @param speed the speed in pixel                  
     */
    public void hunt(GameLevel level, float speed) {
        requireNonNull(level);
        requireNonNegative(speed);
        setSpeed(speed);
        final Vector2i targetTile = level.huntingTimer().phase() == HuntingPhase.CHASING
            ? chasingTargetTile(level)
            : level.worldMap().terrainLayer().ghostScatterTile(personality());
        tryMovingTowardsTargetTile(level, targetTile);
    }

    /**
     * Subclasses implement this method to define the target tile of the ghost when hunting Pac-Man through
     * the current game level.
     *
     * @param level the game level this ghost lives in
     * @return the current target tile when chasing Pac-Man
     */
    public abstract Vector2i chasingTargetTile(GameLevel level);

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
    public void roam(GameLevel level) {
        requireNonNull(level);

        final Vector2i currentTile = tile();
        if (!level.worldMap().terrainLayer().isTileInPortalSpace(currentTile)
            && (isNewTileEntered() || !moveInfo.moved)) {
            Direction dir = computeRoamingDirection(level, currentTile);
            setWishDir(dir);
        }
        moveThroughThisCruelWorld(level);
    }

    // try a random direction towards an accessible tile, do not turn back unless there is no other way
    private Direction computeRoamingDirection(GameLevel level, Vector2i currentTile) {
        Direction dir = pseudoRandomDirection();
        int turns = 0;
        while (dir == moveDir().opposite() || !canAccessTile(level, currentTile.plus(dir.vector()))) {
            dir = dir.nextClockwise();
            if (++turns > 4) {
                return moveDir().opposite();  // avoid endless loop
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

    @Override
    public boolean canAccessTile(GameLevel level, Vector2i tile) {
        final TerrainLayer terrainLayer = level.worldMap().terrainLayer();
        // Portal tiles are the only tiles outside the world map that can be accessed
        if (terrainLayer.outOfBounds(tile)) {
            return terrainLayer.isTileInPortalSpace(tile);
        }
        // Hunting ghosts cannot enter some tiles in Pac-Man game from below
        // TODO: this is game-specific and does not belong here
        if (specialTerrainTiles.contains(tile)
                && state() == GhostState.HUNTING_PAC
                && terrainLayer.content(tile) == TerrainTile.ONE_WAY_DOWN.$
                && tile.equals(tile().plus(UP.vector()))
        ) {
            Logger.debug("Hunting {} cannot move up to special tile {}", name(), tile);
            return false;
        }
        if (home != null && home.isDoorAt(tile)) {
            return inAnyOfStates(GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);
        }
        return !terrainLayer.isTileBlocked(tile);
    }

    @Override
    public boolean canTurnBack() {
        return newTileEntered && inAnyOfStates(GhostState.HUNTING_PAC, GhostState.FRIGHTENED);
    }

    // Here begins the state machine part

    public final ObjectProperty<GhostState> stateProperty() {
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
    public boolean inAnyOfStates(GhostState... states) {
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
            case LOCKED, HUNTING_PAC -> selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_HOME -> selectAnimation(CommonAnimationID.ANIM_GHOST_EYES);
            case FRIGHTENED -> playAnimation(CommonAnimationID.ANIM_GHOST_FRIGHTENED);
            case EATEN -> {}
        }
    }

    /**
     * Updates the state of this ghost in the current game context.
     */
    @Override
    public void tick(Game game) {
        game.optGameLevel().ifPresent(level -> {
            final float speed = game.ghostSpeed(level, this);
            switch (state()) {
                case LOCKED         -> updateStateLocked(level, speed);
                case LEAVING_HOUSE  -> updateStateLeavingHouse(level, speed);
                case HUNTING_PAC    -> updateStateHuntingPac(level, speed);
                case FRIGHTENED     -> updateStateFrightened(level, speed);
                case EATEN          -> updateStateEaten();
                case RETURNING_HOME -> updateStateReturningToHouse(level, speed);
                case ENTERING_HOUSE -> updateStateEnteringHouse(level, speed);
            }});
    }

    // --- LOCKED ---

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    private void updateStateLocked(GameLevel level, float speed) {
        if (home.isVisitedBy(this)) {
            final float minY = (home.minTile().y() + 1) * TS + HTS;
            final float maxY = (home.maxTile().y() - 1) * TS - HTS;
            final float y = position().y();
            if (y <= minY) {
                setMoveDir(DOWN);
                setWishDir(DOWN);
            } else if (y >= maxY) {
                setMoveDir(UP);
                setWishDir(UP);
            }
            setY(Math.clamp(y, minY, maxY));
            setSpeed(speed);
            move();
        } else {
            setSpeed(0);
        }
        if (isInDanger(level)) {
            playFrightenedAnimation(level, level.pac());
        } else {
            selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
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
    private void updateStateLeavingHouse(GameLevel level, float speed) {
        final Vector2f position = position();
        final Vector2f houseEntryPosition = home.entryPosition();
        if (position.y() <= houseEntryPosition.y()) {
            // outside at house entry
            setY(houseEntryPosition.y());
            setMoveDir(LEFT);
            setWishDir(LEFT);
            newTileEntered = false; // don't change direction until new tile is entered by moving
            setState(isInDanger(level) ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
        }
        else {
            // still inside house
            final float centerX = position.x() + HTS;
            final float houseCenterX = home.center().x();
            if (differsAtMost(0.5f * speed, centerX, houseCenterX)) {
                // align horizontally and raise
                setX(houseCenterX - HTS);
                setMoveDir(UP);
                setWishDir(UP);
            } else {
                // move sidewards until center axis is reached
                setMoveDir(centerX < houseCenterX ? RIGHT : LEFT);
                setWishDir(centerX < houseCenterX ? RIGHT : LEFT);
            }
            setSpeed(speed);
            move();
            if (isInDanger(level)) {
                playFrightenedAnimation(level, level.pac());
            } else {
                selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
            }
        }
    }

    private boolean isInDanger(GameLevel level) {
        return level.pac().powerTimer().isRunning() && !level.energizerVictims().contains(this);
    }

    // --- HUNTING_PAC ---

    /**
     * In each game level there are 8 alternating (scattering vs. chasing) hunting phases of different duration. The first
     * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
     * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The last hunting phase
     * is an "infinite" chasing phase.
     * <p>
     * @param level the game level this ghost lives in
     */
    private void updateStateHuntingPac(GameLevel level, float speed) {
        // The specific hunting behaviour is defined by the game variant. For example, in Ms. Pac-Man,
        // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
        hunt(level, speed);
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
    private void updateStateFrightened(GameLevel level, float speed) {
        setSpeed(speed);
        roam(level);
        playFrightenedAnimation(level, level.pac());
    }

    private void playFrightenedAnimation(GameLevel level, Pac pac) {
        if (pac.isPowerFadingStarting(level)) {
            playAnimation(CommonAnimationID.ANIM_GHOST_FLASHING);
        } else if (!pac.isPowerFading(level)) {
            playAnimation(CommonAnimationID.ANIM_GHOST_FRIGHTENED);
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
    private void updateStateReturningToHouse(GameLevel level, float speed) {
        final Vector2f houseEntry = home.entryPosition();
        if (position().roughlyEquals(houseEntry, speed, 0)) {
            setPosition(houseEntry);
            setMoveDir(DOWN);
            setWishDir(DOWN);
            setState(GhostState.ENTERING_HOUSE);
        } else {
            setSpeed(speed);
            setTargetTile(home.leftDoorTile());
            navigateTowardsTarget(level);
            moveThroughThisCruelWorld(level);
        }
    }

    // --- ENTERING_HOUSE ---

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void updateStateEnteringHouse(GameLevel level, float speed) {
        final Vector2f position = position();
        final Vector2f revivalPosition = halfTileRightOf(home.ghostRevivalTile(personality()));
        if (position.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            setPosition(revivalPosition);
            setMoveDir(UP);
            setWishDir(UP);
            setState(GhostState.LOCKED);
            return;
        }
        if (position.y() < revivalPosition.y()) {
            setMoveDir(DOWN);
            setWishDir(DOWN);
        } else if (position.x() > revivalPosition.x()) {
            setMoveDir(LEFT);
            setWishDir(LEFT);
        } else if (position.x() < revivalPosition.x()) {
            setMoveDir(RIGHT);
            setWishDir(RIGHT);
        }
        setSpeed(speed);
        move();
    }
}