/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.*;
import static de.amr.pacmanfx.lib.Direction.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static java.util.Objects.requireNonNull;

/**
 * Common ghost base class. The specific ghosts differ in their hunting behavior and their look.
 */
public abstract class Ghost extends MovingActor implements Animated {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private final byte personality;
    private final String name;
    private ObjectProperty<GhostState> state;

    private Vector2f revivalPosition;
    private List<Vector2i> specialTerrainTiles = List.of();
    private ActorAnimationMap animationMap;

    /**
     * @param gameContext the game context for this ghost, may be null for example in cut scenes
     * @param personality ghost personality, allowed values are
     *          {@link de.amr.pacmanfx.Globals#RED_GHOST_SHADOW},
     *          {@link de.amr.pacmanfx.Globals#PINK_GHOST_SPEEDY},
     *          {@link de.amr.pacmanfx.Globals#CYAN_GHOST_BASHFUL} and
     *          {@link de.amr.pacmanfx.Globals#ORANGE_GHOST_POKEY}
     * @param name readable name, used for logging and debugging
     */
    protected Ghost(GameContext gameContext, byte personality, String name) {
        super(gameContext);
        this.personality = requireValidGhostPersonality(personality);
        this.name = requireNonNull(name);
        corneringSpeedUp = -1.25f;
    }

    public void setAnimations(ActorAnimationMap animationMap) {
        this.animationMap = requireNonNull(animationMap);
    }

    @Override
    public Optional<ActorAnimationMap> animationMap() {
        return Optional.ofNullable(animationMap);
    }

    @Override
    public String toString() {
        return "Ghost{" +
                "name='" + name + '\'' +
                ", state=" + (state != null ? state() : DEFAULT_STATE) +
                ", visible=" + isVisible() +
                ", position=" + position() +
                ", velocity=" + (velocity != null ? velocity() : DEFAULT_VELOCITY) +
                ", acceleration=" + (acceleration != null ? acceleration() : DEFAULT_ACCELERATION) +
                ", moveDir=" + (moveDir != null ? moveDir() : DEFAULT_MOVE_DIR) +
                ", wishDir=" + (wishDir != null ? wishDir() : DEFAULT_WISH_DIR) +
                ", targetTile=" + (targetTile != null ? targetTile() : DEFAULT_TARGET_TILE) +
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
     * the current game level.
     */
    public abstract void hunt();

    /**
     * Subclasses implement this method to define the target tile of the ghost when hunting Pac-Man through
     * the current game level.
     *
     * @return the current target tile when chasing Pac-Man
     */
    public abstract Vector2i chasingTargetTile();

    /**
     * Lets the ghost roam through the current level's world.
     * <p></p>
     * <cite>
     Roam if you want to, roam around the world!<br>
     Roam if you want to, without wings without wheels!<br>
     Roam if you want to, roam around the world!<br>
     Roam if you want to, without anything but the love we feel!
     </cite>
     */
    public void roam() {
        if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;

        GameLevel level = gameContext.theGameLevel();
        Vector2i currentTile = tile();
        if (!level.isTileInPortalSpace(currentTile) && (isNewTileEntered() || !moveInfo.moved)) {
            Direction dir = computeRoamingDirection(currentTile);
            setWishDir(dir);
        }
        findMyWayThroughThisCruelWorld();
    }

    // try a random direction towards an accessible tile, do not turn back unless there is no other way
    private Direction computeRoamingDirection(Vector2i currentTile) {
        Direction dir = pseudoRandomDirection();
        int turns = 0;
        while (dir == moveDir().opposite() || !canAccessTile(currentTile.plus(dir.vector()))) {
            dir = dir.nextClockwise();
            if (++turns > 4) {
                return moveDir().opposite();  // avoid endless loop
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
    public boolean canAccessTile(Vector2i tile) {
        requireNonNull(tile);

        if (gameContext == null || gameContext.optGameLevel().isEmpty()) return true;

        GameLevel level = gameContext.theGameLevel();

        // Portal tiles are the only tiles outside the world map that can be accessed
        if (!level.isTileInsideWorld(tile)) {
            return level.isTileInPortalSpace(tile);
        }
        // Hunting ghosts cannot enter some tiles in Pac-Man game from below
        // TODO: this is game-specific and does not belong here
        if (specialTerrainTiles.contains(tile)
                && state() == GhostState.HUNTING_PAC
                && level.worldMap().content(LayerID.TERRAIN, tile) == TerrainTile.ONE_WAY_DOWN.code()
                && tile.equals(tile().plus(UP.vector()))
        ) {
            Logger.debug("Hunting {} cannot move up to special tile {}", name, tile);
            return false;
        }
        if (level.house().isPresent() && level.house().get().isDoorAt(tile)) {
            return inAnyOfStates(GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);
        }
        return !level.isTileBlocked(tile);
    }

    @Override
    public boolean canReverse() {
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
        return state != null && isOneOf(state(), states);
    }

    /**
     * Changes the state of this ghost.
     *
     * @param newState the new state
     */
    public void setState(GhostState newState) {
        requireNonNull(newState);
        if (state() == newState) {
            Logger.warn("{} is already in state {}", name, newState);
        }
        stateProperty().set(newState);

        // "onEntry" action:
        switch (newState) {
            case LOCKED, HUNTING_PAC -> selectAnimation(ANIM_GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_HOME -> selectAnimation(ANIM_GHOST_EYES);
            case FRIGHTENED -> playAnimation(ANIM_GHOST_FRIGHTENED);
            case EATEN -> {}
        }
    }

    /**
     * Updates the state of this ghost in the current game context.
     */
    public void update() {
        if (gameContext == null) {
            return; // might happen for ghosts in cut scenes
        }
        gameContext.optGameLevel().ifPresent(level -> {
            switch (state()) {
                case LOCKED             -> updateStateLocked();
                case LEAVING_HOUSE      -> updateStateLeavingHouse();
                case HUNTING_PAC        -> updateStateHuntingPac();
                case FRIGHTENED         -> updateStateFrightened();
                case EATEN              -> updateStateEaten();
                case RETURNING_HOME     -> updateStateReturningToHouse();
                case ENTERING_HOUSE     -> updateStateEnteringHouse();
            }
        });
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
    private void updateStateLocked() {
        if (gameContext.optGameLevel().isPresent()) {
            GameLevel level = gameContext.theGameLevel();
            House house = level.house().orElse(null);
            if (house == null) {
                Logger.error("No ghost house in level? WTF!");
                return;
            }
            if (house.isVisitedBy(this)) {
                float minY = (house.minTile().y() + 1) * TS + HTS;
                float maxY = (house.maxTile().y() - 1) * TS - HTS;
                setSpeed(gameContext.theGame().actorSpeedControl().ghostSpeedInsideHouse(gameContext, level, this));
                move();
                float y = position().y();
                if (y <= minY) {
                    setMoveAndWishDir(DOWN);
                } else if (y >= maxY) {
                    setMoveAndWishDir(UP);
                }
                setPosition(position().x(), Math.clamp(y, minY, maxY));
            } else {
                setSpeed(0);
            }
            if (level.pac().powerTimer().isRunning() && !level.victims().contains(this)) {
                updateFrightenedAnimation();
            } else {
                selectAnimation(ANIM_GHOST_NORMAL);
            }
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
    private void updateStateLeavingHouse() {
        if (gameContext.optGameLevel().isPresent()) {
            GameLevel level = gameContext.theGameLevel();
            House house = level.house().orElse(null);
            if (house == null) {
                Logger.error("No ghost house in level? WTF!");
                return;
            }
            float speedInsideHouse = gameContext.theGame().actorSpeedControl().ghostSpeedInsideHouse(gameContext, level, this);
            Vector2f houseEntryPosition = house.entryPosition();
            Vector2f position = position();
            if (position.y() <= houseEntryPosition.y()) {
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
            float centerX = position.x() + HTS;
            float houseCenterX = house.center().x();
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
                updateFrightenedAnimation();
            } else {
                selectAnimation(ANIM_GHOST_NORMAL);
            }
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
    private void updateStateHuntingPac() {
        if (gameContext.optGameLevel().isPresent()) {
            // The specific hunting behaviour is defined by the game variant. For example, in Ms. Pac-Man,
            // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
            hunt();
        }
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
    private void updateStateFrightened() {
        if (gameContext.optGameLevel().isPresent()) {
            GameLevel level = gameContext.theGameLevel();
            House house = level.house().orElse(null);
            if (house == null) {
                Logger.error("No ghost house in level? WTF!");
                return;
            }
            float speed = level.isTunnel(tile())
                ? gameContext.theGame().actorSpeedControl().ghostTunnelSpeed(gameContext, level, this)
                : gameContext.theGame().actorSpeedControl().ghostFrightenedSpeed(gameContext, level, this);
            setSpeed(speed);
            roam();
            updateFrightenedAnimation();
        }
    }

    private void updateFrightenedAnimation() {
        if (gameContext.optGameLevel().isPresent()) {
            GameLevel level = gameContext.theGameLevel();
            House house = level.house().orElse(null);
            if (house == null) {
                Logger.error("No ghost house in level? WTF!");
                return;
            }
            if (level.pac().isPowerFadingStarting()) {
                playAnimation(ANIM_GHOST_FLASHING);
            } else if (!level.pac().isPowerFading()) {
                playAnimation(ANIM_GHOST_FRIGHTENED);
            }
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
        if (gameContext.optGameLevel().isPresent()) {
            GameLevel level = gameContext.theGameLevel();
            House house = level.house().orElse(null);
            if (house == null) {
                Logger.error("No ghost house in level? WTF!");
                return;
            }
            float speed = gameContext.theGame().actorSpeedControl().ghostSpeedReturningToHouse(gameContext, level, this);
            Vector2f houseEntry = house.entryPosition();
            if (position().roughlyEquals(houseEntry, speed, 0)) {
                setPosition(houseEntry);
                setMoveAndWishDir(DOWN);
                setState(GhostState.ENTERING_HOUSE);
            } else {
                setSpeed(speed);
                setTargetTile(house.leftDoorTile());
                navigateTowardsTarget();
                findMyWayThroughThisCruelWorld();
            }
        }
    }

    // --- ENTERING_HOUSE ---

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void updateStateEnteringHouse() {
        if (gameContext.optGameLevel().isPresent()) {
            GameLevel level = gameContext.theGameLevel();
            House house = level.house().orElse(null);
            if (house == null) {
                Logger.error("No ghost house in level? WTF!");
                return;
            }
            float speed = gameContext.theGame().actorSpeedControl().ghostSpeedReturningToHouse(gameContext, level, this);
            Vector2f position = position();
            if (position.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
                setPosition(revivalPosition);
                setMoveAndWishDir(UP);
                setState(GhostState.LOCKED);
                return;
            }
            if (position.y() < revivalPosition.y()) {
                setMoveAndWishDir(DOWN);
            } else if (position.x() > revivalPosition.x()) {
                setMoveAndWishDir(LEFT);
            } else if (position.x() < revivalPosition.x()) {
                setMoveAndWishDir(RIGHT);
            }
            setSpeed(speed);
            move();
        }
    }
}