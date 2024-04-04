/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameController.publishGameEvent;
import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.actors.CreatureMovement.followTarget;
import static de.amr.games.pacman.model.actors.CreatureMovement.roam;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * @author Armin Reichert
 */
public class GameLevel {

    private final int levelNumber;
    private final boolean demoLevel;
    private final GameLevelData data;
    private final GameModel game;
    private final TickTimer huntingTimer = new TickTimer("HuntingTimer");
    private final World world;
    private final Pac pac;
    private final Ghost[] ghosts;
    private final GhostHouseControl houseControl;
    private final byte[] bonusSymbols = new byte[2];
    private Bonus bonus;
    private byte huntingPhaseIndex;
    private byte totalNumGhostsKilled;
    private byte cruiseElroyState;
    private SimulationStepEventLog eventLog;
    private byte bonusReachedIndex; // -1=no bonus, 0=first, 1=second

    public GameLevel(int levelNumber, GameLevelData levelData, GameModel game, World world, boolean demoLevel) {
        checkLevelNumber(levelNumber);
        checkNotNull(levelData);
        checkGameNotNull(game);
        checkNotNull(world);

        this.game = game;
        this.world = world;
        this.levelNumber = levelNumber;
        this.data = levelData;
        this.demoLevel = demoLevel;

        houseControl = new GhostHouseControl(levelNumber);
        eventLog = new SimulationStepEventLog();
        bonusReachedIndex = -1;

        pac = new Pac(game.variant() == GameVariant.MS_PACMAN ? "Ms. Pac-Man" : "Pac-Man");
        pac.reset();
        pac.setBaseSpeed(SPEED_AT_100_PERCENT);
        pac.setPowerFadingTicks(PAC_POWER_FADING_TICKS); // not sure about duration

        ghosts = new Ghost[] {
            new Ghost(RED_GHOST, "Blinky"),
            new Ghost(PINK_GHOST, "Pinky"),
            new Ghost(CYAN_GHOST, "Inky"),
            new Ghost(ORANGE_GHOST, game.variant() == GameVariant.MS_PACMAN ? "Sue" : "Clyde")
        };

        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setHouse(world.house());
            ghost.setFrightenedBehavior(this::frightenedGhostBehavior);
            ghost.setRevivalPosition(ghostRevivalPosition(ghost.id()));
            ghost.setBaseSpeed(SPEED_AT_100_PERCENT);
            ghost.setSpeedReturningToHouse(SPEED_GHOST_RETURNING_TO_HOUSE);
            ghost.setSpeedInsideHouse(SPEED_GHOST_INSIDE_HOUSE);
        });

        switch (game.variant()) {
            case MS_PACMAN -> ghosts().forEach(ghost -> ghost.setHuntingBehavior(this::huntingBehaviorMsPacManGame));
            case PACMAN -> {
                var forbiddenMovesAtTile = new HashMap<Vector2i, List<Direction>>();
                var up = List.of(UP);
                ArcadeWorld.PACMAN_RED_ZONE.forEach(tile -> forbiddenMovesAtTile.put(tile, up));
                ghosts().forEach(ghost -> {
                    ghost.setForbiddenMoves(forbiddenMovesAtTile);
                    ghost.setHuntingBehavior(this::huntingBehaviorPacManGame);
                });
            }
        }

        bonusSymbols[0] = nextBonusSymbol();
        bonusSymbols[1] = nextBonusSymbol();

        Logger.trace("Game level {} ({}) created.", levelNumber, game.variant());
    }

    public Direction initialGhostDirection(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case RED_GHOST -> LEFT;
            case PINK_GHOST -> DOWN;
            case CYAN_GHOST, ORANGE_GHOST -> UP;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2f initialGhostPosition(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case RED_GHOST    -> world.house().door().entryPosition();
            case PINK_GHOST   -> ArcadeWorld.HOUSE_MIDDLE_SEAT;
            case CYAN_GHOST   -> ArcadeWorld.HOUSE_LEFT_SEAT;
            case ORANGE_GHOST -> ArcadeWorld.HOUSE_RIGHT_SEAT;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2f ghostRevivalPosition(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case RED_GHOST, PINK_GHOST -> ArcadeWorld.HOUSE_MIDDLE_SEAT;
            case CYAN_GHOST            -> ArcadeWorld.HOUSE_LEFT_SEAT;
            case ORANGE_GHOST          -> ArcadeWorld.HOUSE_RIGHT_SEAT;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2i ghostScatterTarget(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case RED_GHOST    -> ArcadeWorld.SCATTER_TARGET_RIGHT_UPPER_CORNER;
            case PINK_GHOST   -> ArcadeWorld.SCATTER_TARGET_LEFT_UPPER_CORNER;
            case CYAN_GHOST   -> ArcadeWorld.SCATTER_TARGET_RIGHT_LOWER_CORNER;
            case ORANGE_GHOST -> ArcadeWorld.SCATTER_TARGET_LEFT_LOWER_CORNER;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public GameLevelData data() {
        return data;
    }

    public GameModel game() {
        return game;
    }

    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }

    /**
     * @return level number, starting with 1.
     */
    public int number() {
        return levelNumber;
    }

    public World world() {
        return world;
    }

    public Pac pac() {
        return pac;
    }

    /**
     * @param id ghost ID, one of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST},
     *           {@value GameModel#CYAN_GHOST}, {@link GameModel#ORANGE_GHOST}
     * @return the ghost with the given ID
     */
    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts[id];
    }

    /**
     * @param states states specifying which ghosts are returned
     * @return all ghosts which are in any of the given states or all ghosts, if no states are specified
     */
    public Stream<Ghost> ghosts(GhostState... states) {
        if (states.length > 0) {
            return Stream.of(ghosts).filter(ghost -> ghost.is(states));
        }
        // when no states are given, return *all* ghosts (ghost.is() would return *no* ghosts!)
        return Stream.of(ghosts);
    }

    /**
     * @return information about what happened during the current (hunting) frame
     */
    public SimulationStepEventLog eventLog() {
        return eventLog;
    }

    /**
     * @return Blinky's "cruise elroy" state. Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    public byte cruiseElroyState() {
        return cruiseElroyState;
    }

    /**
     * @param cruiseElroyState Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    public void setCruiseElroyState(int cruiseElroyState) {
        if (cruiseElroyState < -2 || cruiseElroyState > 2) {
            throw new IllegalArgumentException(
                "Cruise Elroy state must be one of -2, -1, 0, 1, 2, but is " + cruiseElroyState);
        }
        this.cruiseElroyState = (byte) cruiseElroyState;
        Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
    }

    public void enableCruiseElroyState(boolean enabled) {
        if (enabled && cruiseElroyState < 0 || !enabled && cruiseElroyState > 0) {
            cruiseElroyState = (byte) (-cruiseElroyState);
            Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
        }
    }

    public int totalNumGhostsKilled() {
        return totalNumGhostsKilled;
    }

    /**
     * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
     * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
     * ghosts attack Pac-Man.
     *
     * @param index hunting phase index (0..7)
     */
    public void startHuntingPhase(int index) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + index);
        }
        huntingPhaseIndex = (byte) index;
        var durations = game.huntingDurations(levelNumber);
        var ticks = durations[index] == -1 ? TickTimer.INDEFINITE : durations[index];
        huntingTimer.reset(ticks);
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            index, currentHuntingPhaseName(), huntingTimer.duration(),
            (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }

    public void stopHuntingPhase() {
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
    }

    /**
     * @return number of current phase <code>(0-7)
     */
    public int huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    public void letPacDie() {
        stopHuntingPhase();
        houseControl.resetGlobalCounterAndSetEnabled(true);
        enableCruiseElroyState(false);
        pac.die();
    }

    /**
     * <p>
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* hunting/scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man
     * but because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who knows?
     * </p>
     */
    private void huntingBehaviorMsPacManGame(Ghost ghost) {
        if (scatterPhase().isPresent() && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            roam(ghost, world, huntingSpeedPercentage(ghost), pseudoRandomDirection());
        } else {
            huntingBehaviorPacManGame(ghost);
        }
    }

    private void huntingBehaviorPacManGame(Ghost ghost) {
        byte relSpeed = huntingSpeedPercentage(ghost);
        if (chasingPhase().isPresent() || ghost.id() == RED_GHOST && cruiseElroyState > 0) {
            followTarget(ghost, world, chasingTarget(ghost.id()), relSpeed);
        } else {
            followTarget(ghost, world, ghostScatterTarget(ghost.id()), relSpeed);
        }
    }

    private Vector2i chasingTarget(byte ghostID) {
        return switch (ghostID) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> tilesAheadWithOverflowBug(pac, 4);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> tilesAheadWithOverflowBug(pac,2).scaled(2).minus(ghosts[RED_GHOST].tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghosts[ORANGE_GHOST].tile().euclideanDistance(pac.tile()) < 8
                ? ghostScatterTarget(ORANGE_GHOST)
                : pac.tile();
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the creature.
     * In case the creature looks UP, additional {@code numTiles} tiles are added towards LEFT.
     * This simulates an overflow bug in the original Arcade games.
     */
    private static Vector2i tilesAheadWithOverflowBug(Creature creature, int numTiles) {
        Vector2i ahead = creature.tile().plus(creature.moveDir().vector().scaled(numTiles));
        return creature.moveDir() == Direction.UP ? ahead.minus(numTiles, 0) : ahead;
    }

    /**
     * <p>
     * Frightened ghosts choose a "random" direction when they enter a new tile. If the chosen direction
     * can be taken, it is stored and taken as soon as possible. Otherwise, the remaining directions are checked in
     * clockwise order.
     * </p>
     *
     * @see <a href="https://www.youtube.com/watch?v=eFP0_rkjwlY">YouTube: How Frightened Ghosts Decide Where to Go</a>
     */
    private void frightenedGhostBehavior(Ghost ghost) {
        byte relSpeed = world().isTunnel(ghost.tile())
            ? data.ghostSpeedTunnelPercentage() : data.ghostSpeedFrightenedPercentage();
        roam(ghost, world, relSpeed, pseudoRandomDirection());
    }

    private static Direction pseudoRandomDirection() {
        float rnd = Globals.randomFloat(0, 100);
        if (rnd < 16.3) return UP;
        if (rnd < 16.3 + 25.2) return RIGHT;
        if (rnd < 16.3 + 25.2 + 28.5) return DOWN;
        return LEFT;
    }

    /**
     * @return (optional) index of current scattering phase <code>(0-3)</code>
     */
    public Optional<Integer> scatterPhase() {
        return isEven(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    /**
     * @return (optional) index of current chasing phase <code>(0-3)</code>
     */
    public Optional<Integer> chasingPhase() {
        return isOdd(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    public String currentHuntingPhaseName() {
        return isEven(huntingPhaseIndex) ? "Scattering" : "Chasing";
    }

    /**
     * Pac-Man and the ghosts are placed at their initial positions and locked. The bonus, Pac-Man power timer and
     * energizer pulse are reset too.
     *
     * @param guysVisible if the guys are visible
     */
    public void letsGetReadyToRumble(boolean guysVisible) {
        pac.reset();
        pac.setPosition(ArcadeWorld.PAC_POSITION);
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.setVisible(guysVisible);
        pac.resetAnimation();
        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setPosition(initialGhostPosition(ghost.id()));
            ghost.setMoveAndWishDir(initialGhostDirection(ghost.id()));
            ghost.setVisible(guysVisible);
            ghost.setState(LOCKED);
            ghost.resetAnimation();
        });
        world.mazeFlashing().reset();
        world.energizerBlinking().reset();
    }

    /**
     * @param ghost a ghost
     * @return relative speed of ghost in percent of the base speed
     */
    public byte huntingSpeedPercentage(Ghost ghost) {
        if (world.isTunnel(ghost.tile())) {
            return data.ghostSpeedTunnelPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 1) {
            return data.elroy1SpeedPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 2) {
            return data.elroy2SpeedPercentage();
        }
        return data.ghostSpeedPercentage();
    }

    /* --- Here comes the main logic of the game. --- */

    private void scorePoints(int points) {
        if (demoLevel) {
            return;
        }
        int oldScore = game.score().points();
        int newScore = oldScore + points;
        game.score().setPoints(newScore);
        if (newScore > game.highScore().points()) {
            game.highScore().setPoints(newScore);
            game.highScore().setLevelNumber(levelNumber);
            game.highScore().setDate(LocalDate.now());
        }
        if (oldScore < EXTRA_LIFE_SCORE && newScore >= EXTRA_LIFE_SCORE) {
            game.addLives((short) 1);
            publishGameEvent(GameEventType.EXTRA_LIFE_WON);
        }
    }

    private void updateFood() {
        final Vector2i pacTile = pac.tile();
        if (world.hasFoodAt(pacTile)) {
            eventLog.foodFoundTile = pacTile;
            pac.endStarving();
            if (world.isEnergizerTile(pacTile)) {
                eventLog.energizerFound = true;
                pac.setRestingTicks(GameModel.RESTING_TICKS_ENERGIZER);
                pac.victims().clear();
                scorePoints(GameModel.POINTS_ENERGIZER);
                Logger.info("Scored {} points for eating energizer", GameModel.POINTS_ENERGIZER);
            } else {
                pac.setRestingTicks(GameModel.RESTING_TICKS_NORMAL_PELLET);
                scorePoints(GameModel.POINTS_NORMAL_PELLET);
            }
            houseControl.updateDotCount(this);
            world.removeFood(pacTile);
            if (world.uneatenFoodCount() == data.elroy1DotsLeft()) {
                setCruiseElroyState(1);
            } else if (world.uneatenFoodCount() == data.elroy2DotsLeft()) {
                setCruiseElroyState(2);
            }
            if (isBonusReached()) {
                bonusReachedIndex += 1;
                eventLog.bonusIndex = bonusReachedIndex;
                onBonusReached(bonusReachedIndex);
            }
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            pac.starve();
        }
    }

    private void updatePacPower() {
        if (eventLog.energizerFound && data.pacPowerSeconds() > 0) {
            stopHuntingPhase();
            pac.powerTimer().restartSeconds(data.pacPowerSeconds());
            ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
            eventLog.pacGetsPower = true;
            publishGameEvent(GameEventType.PAC_GETS_POWER);
        } else if (pac.powerTimer().remaining() == GameModel.PAC_POWER_FADING_TICKS) {
            eventLog.pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (pac.powerTimer().hasExpired()) {
            pac.powerTimer().stop();
            pac.powerTimer().resetIndefinitely();
            huntingTimer.start();
            Logger.info("Hunting timer started");
            ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog.pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    private void updateBonus() {
        if (bonus != null) {
            boolean eaten = checkPacEatsBonus(bonus);
            if (eaten) {
                eventLog.bonusEaten = true;
                publishGameEvent(GameEventType.BONUS_EATEN);
            }
            bonus.update(this);
        }
    }

    private void updateHuntingTimer( ) {
        if (huntingTimer.hasExpired()) {
            ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
            startHuntingPhase(huntingPhaseIndex + 1);
        } else {
            huntingTimer.advance();
        }
    }

    private void updateGhosts() {
        houseControl.unlockGhost(this).ifPresent(unlocked -> {
            var ghost = unlocked.ghost();
            Logger.info("{} unlocked: {}", ghost.name(), unlocked.reason());
            if (ghost.insideHouse(world.house())) {
                ghost.setState(LEAVING_HOUSE);
            } else {
                ghost.setMoveAndWishDir(LEFT);
                ghost.setState(HUNTING_PAC);
            }
            if (ghost.id() == ORANGE_GHOST && cruiseElroyState < 0) {
                enableCruiseElroyState(true);
                Logger.trace("Cruise elroy mode re-enabled because {} exits house", ghost.name());
            }
            eventLog.unlockedGhost = ghost;
        });
        ghosts().forEach(ghost -> ghost.update(pac, world));
    }

    public GameState doHuntingStep() {
        eventLog = new SimulationStepEventLog();
        pac.update(this);
        updateGhosts();
        updateFood();
        updatePacPower();
        updateBonus();
        updateHuntingTimer();
        // what next?
        if (world.uneatenFoodCount() == 0) {
            return GameState.LEVEL_COMPLETE;
        }
        var killers = ghosts(HUNTING_PAC).filter(pac::sameTile).toList();
        if (!killers.isEmpty() && !GameController.it().isPacImmune()) {
            eventLog.pacDied = true;
            return GameState.PACMAN_DYING;
        }
        var prey = ghosts(FRIGHTENED).filter(pac::sameTile).toList();
        if (!prey.isEmpty()) {
            killGhosts(prey);
            return GameState.GHOST_DYING;
        }
        return GameState.HUNTING;
    }

    public void doLevelTestStep(TickTimer timer, int lastTestedLevel) {
        if (number() <= lastTestedLevel) {
            if (timer.atSecond(0.5)) {
                letsGetReadyToRumble(true);
            } else if (timer.atSecond(1.5)) {
                onBonusReached(0);
            } else if (timer.atSecond(3.5)) {
                bonus().ifPresent(bonus -> bonus.setEaten(120));
                publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(4.5)) {
                bonus.setInactive();//TODO
                onBonusReached(1);
            } else if (timer.atSecond(6.5)) {
                bonus().ifPresent(bonus -> bonus.setEaten(60));
                publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(8.5)) {
                pac.hide();
                ghosts().forEach(Ghost::hide);
                var flashing = world().mazeFlashing();
                flashing.restart(2 * data.numFlashes());
            } else if (timer.atSecond(12.0)) {
                pac.freeze();
                ghosts().forEach(Ghost::hide);
                bonus().ifPresent(Bonus::setInactive);
                world().mazeFlashing().reset();
                GameController.it().createAndStartLevel(levelNumber + 1);
                timer.restartIndefinitely();
            }
            world().energizerBlinking().tick();
            world().mazeFlashing().tick();
            ghosts().forEach(ghost -> ghost.update(pac, world));
            bonus().ifPresent(bonus -> bonus.update(this));
        } else {
            GameController.it().restart(GameState.BOOT);
        }
    }

    /**
     * Called by cheat action only.
     */
    public void killAllHuntingAndFrightenedGhosts() {
        pac.victims().clear();
        killGhosts(ghosts(FRIGHTENED, HUNTING_PAC).toList());
    }

    public void killGhosts(List<Ghost> prey) {
        if (!prey.isEmpty()) {
            prey.forEach(this::killGhost);
            if (totalNumGhostsKilled == 16) {
                int points = GameModel.POINTS_ALL_GHOSTS_KILLED_IN_LEVEL;
                scorePoints(points);
                Logger.info("Scored {} points for killing all ghosts at level {}", points, levelNumber);
            }
        }
    }

    private void killGhost(Ghost ghost) {
        int killedSoFar = pac.victims().size();
        int points = game.pointsForKillingGhost(killedSoFar);
        scorePoints(points);
        ghost.eaten(killedSoFar);
        pac.victims().add(ghost);
        eventLog.killedGhosts.add(ghost);
        totalNumGhostsKilled += 1;
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    // Bonus Management

    /**
     * <p>Got this information from
     * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>:
     * </p>
     * <p><em>
     * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
     * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
     * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
     * spawn.</em></p>
     *
     * <p><b>Dying while a fruit is on screen causes it to immediately disappear and never return.
     * (TODO: what does never mean here? For the rest of the game?)</b></p>
     *
     * <p><em>
     * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
     * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
     * the following table:
     * </em></p>
     *
     * <table>
     * <tr>
     *   <th>Cherry</th>
     *   <th>Strawberry</th>
     *   <th>Peach</th>
     *   <th>Pretzel</th>
     *   <th>Apple</th>
     *   <th>Pear</th>
     *   <th>Banana</th>
     * </tr>
     * <tr align="right">
     *   <td>5/32</td>
     *   <td>5/32</td>
     *   <td>5/32</td>
     *   <td>5/32</td>
     *   <td>4/32</td>
     *   <td>4/32</td>
     *   <td>4/32</td>
     * </tr>
     * </table>
     */
    private byte nextBonusSymbol() {
        return switch (game.variant()) {
            case MS_PACMAN -> switch (levelNumber) {
                case 1 -> GameModel.MS_PACMAN_CHERRIES;
                case 2 -> GameModel.MS_PACMAN_STRAWBERRY;
                case 3 -> GameModel.MS_PACMAN_ORANGE;
                case 4 -> GameModel.MS_PACMAN_PRETZEL;
                case 5 -> GameModel.MS_PACMAN_APPLE;
                case 6 -> GameModel.MS_PACMAN_PEAR;
                case 7 -> GameModel.MS_PACMAN_BANANA;
                default -> {
                    int random = randomInt(0, 320);
                    if (random <  50) yield GameModel.MS_PACMAN_CHERRIES;
                    if (random < 100) yield GameModel.MS_PACMAN_STRAWBERRY;
                    if (random < 150) yield GameModel.MS_PACMAN_ORANGE;
                    if (random < 200) yield GameModel.MS_PACMAN_PRETZEL;
                    if (random < 240) yield GameModel.MS_PACMAN_APPLE;
                    if (random < 280) yield GameModel.MS_PACMAN_PEAR;
                    else              yield GameModel.MS_PACMAN_BANANA;
                }
            };
            // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
            case PACMAN -> switch (levelNumber) {
                case 1 ->      GameModel.PACMAN_CHERRIES;
                case 2 ->      GameModel.PACMAN_STRAWBERRY;
                case 3, 4 ->   GameModel.PACMAN_PEACH;
                case 5, 6 ->   GameModel.PACMAN_APPLE;
                case 7, 8 ->   GameModel.PACMAN_GRAPES;
                case 9, 10 ->  GameModel.PACMAN_GALAXIAN;
                case 11, 12 -> GameModel.PACMAN_BELL;
                default ->     GameModel.PACMAN_KEY;
            };
        };
    }

    public boolean isBonusReached() {
        return switch (game.variant()) {
            case MS_PACMAN -> world().eatenFoodCount() == 64 || world().eatenFoodCount() == 176;
            case    PACMAN -> world().eatenFoodCount() == 70 || world().eatenFoodCount() == 170;
        };
    }

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public byte bonusSymbol(int index) {
        return bonusSymbols[index];
    }

    private boolean checkPacEatsBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE && pac.sameTile(bonus.entity())) {
            bonus.setEaten(GameModel.BONUS_POINTS_SHOWN_TICKS);
            scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            return true;
        }
        return false;
    }

    /**
     * Called on bonus achievement (public access for unit tests and level test).
     *
     * @param bonusIndex bonus index (0 or 1).
     */
    public void onBonusReached(int bonusIndex) {
        if (bonusIndex < 0 || bonusIndex > 1) {
            throw new IllegalArgumentException("Bonus index must be 0 or 1 but is " + bonusIndex);
        }
        switch (game.variant()) {
            case MS_PACMAN -> {
                if (bonusIndex == 1 && bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
                    Logger.info("First bonus still active, skip second one");
                    return;
                }
                byte symbol = bonusSymbols[bonusIndex];
                int points = GameModel.BONUS_VALUES_MS_PACMAN[symbol] * 100;
                bonus = createMovingBonus(symbol, points, RND.nextBoolean());
                bonus.setEdible(TickTimer.INDEFINITE);
                publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
            }
            case PACMAN -> {
                byte symbol = bonusSymbols[bonusIndex];
                int points = GameModel.BONUS_VALUES_PACMAN[symbol] * 100;
                bonus = new StaticBonus(symbol, points);
                bonus.entity().setPosition(ArcadeWorld.BONUS_POSITION);
                bonus.setEdible(randomInt(9 * FPS, 10 * FPS));
                publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
            }
        }
    }

    /**
     * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the house and
     * finally leaves the world through a random portal on the opposite side of the world.
     * <p>
     * TODO: This is not the exact behavior as in the original Arcade game.
     **/
    private Bonus createMovingBonus(byte symbol, int points, boolean leftToRight) {
        var house       = world.house();
        var houseEntry  = tileAt(house.door().entryPosition());
        var houseEntryOpposite = houseEntry.plus(0, house.size().y() + 1);

        var portals     = world.portals();
        var entryPortal = portals.get(RND.nextInt(portals.size()));
        var exitPortal  = portals.get(RND.nextInt(portals.size()));

        var route = List.of(
            np(leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd()),
            np(houseEntry),
            np(houseEntryOpposite),
            np(houseEntry),
            np(leftToRight
                ? exitPortal.rightTunnelEnd().plus(1, 0)
                : exitPortal.leftTunnelEnd().minus(1, 0))
        );

        var movingBonus = new MovingBonus(symbol, points);
        movingBonus.setBaseSpeed(SPEED_AT_100_PERCENT);
        // pass copy of list because route gets modified
        movingBonus.setRoute(new ArrayList<>(route), leftToRight);
        Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        return movingBonus;
    }
}