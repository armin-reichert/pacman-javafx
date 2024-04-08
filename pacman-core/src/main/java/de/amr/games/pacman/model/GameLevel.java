/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.CreatureMovement.roam;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * @author Armin Reichert
 */
public class GameLevel {

    public record Data(
        int number, // Level number, starts at 1.
        boolean demoLevel,
        byte pacSpeedPercentage, // Relative Pac-Man speed (percentage of base speed).
        byte ghostSpeedPercentage, // Relative ghost speed when hunting or scattering.
        byte ghostSpeedTunnelPercentage, // Relative ghost speed inside tunnel.
        byte elroy1DotsLeft,//  Number of pellets left when Blinky becomes "Cruise Elroy" grade 1.
        byte elroy1SpeedPercentage, // Relative speed of Blinky being "Cruise Elroy" grade 1.
        byte elroy2DotsLeft, // Number of pellets left when Blinky becomes "Cruise Elroy" grade 2.
        byte elroy2SpeedPercentage, //Relative speed of Blinky being "Cruise Elroy" grade 2.
        byte pacSpeedPoweredPercentage, // Relative speed of Pac-Man in power mode.
        byte ghostSpeedFrightenedPercentage, // Relative speed of frightened ghost.
        byte pacPowerSeconds, // Number of seconds Pac-Man gets power.
        byte numFlashes, // Number of maze flashes at end of this level.
        byte intermissionNumber) // Number (1,2,3) of intermission scene played after this level (0=no intermission).
    {
        public Data(int number, boolean demoLevel, byte[] data) {
            this(number, demoLevel, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8],
                data[9], data[10], data[11]);
        }
    }

    private final Data data;
    private final TickTimer huntingTimer = new TickTimer("HuntingTimer");
    private final World world;
    private final Pac pac;
    private final Ghost[] ghosts;
    private final GhostHouseControl houseControl;
    private final List<Byte> bonusSymbols;
    private Bonus bonus;
    private byte huntingPhaseIndex;
    private byte totalNumGhostsKilled;
    private byte cruiseElroyState;
    private SimulationStepEventLog eventLog;
    private byte bonusReachedIndex; // -1=no bonus, 0=first, 1=second

    public GameLevel(Data levelData, World world) {
        checkNotNull(levelData);
        checkNotNull(world);

        this.world = world;
        this.data = levelData;

        houseControl = new GhostHouseControl(levelData.number());
        eventLog = new SimulationStepEventLog();
        bonusReachedIndex = -1;

        pac = new Pac(game().pacName());
        pac.reset();
        pac.setBaseSpeed(GameModel.PPS_AT_100_PERCENT / (float) GameModel.FPS);
        pac.setPowerFadingTicks(GameModel.PAC_POWER_FADING_TICKS); // not sure about duration

        ghosts = Stream.of(GameModel.RED_GHOST, GameModel.PINK_GHOST, GameModel.CYAN_GHOST, GameModel.ORANGE_GHOST)
            .map(id -> new Ghost(id, game().ghostName(id))).toArray(Ghost[]::new);

        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setHouse(world.house());
            ghost.setFrightenedBehavior(g -> roam(g, world, frightenedGhostRelSpeed(g), pseudoRandomDirection()));
            ghost.setHuntingBehavior(game()::huntingBehaviour);
            ghost.setRevivalPosition(ghostRevivalPosition(ghost.id()));
            ghost.setBaseSpeed(GameModel.PPS_AT_100_PERCENT / (float) GameModel.FPS);
            ghost.setSpeedReturningHome(GameModel.PPS_GHOST_RETURNING_HOME / (float) GameModel.FPS);
            ghost.setSpeedInsideHouse(GameModel.PPS_GHOST_INHOUSE / (float) GameModel.FPS);
        });

        //TODO avoid switch over game variant
        switch (game()) {
            case GameVariants.MS_PACMAN -> {}
            case GameVariants.PACMAN -> {
                var forbiddenMovesAtTile = new HashMap<Vector2i, List<Direction>>();
                var up = List.of(UP);
                ArcadeWorld.PACMAN_RED_ZONE.forEach(tile -> forbiddenMovesAtTile.put(tile, up));
                ghosts().forEach(ghost -> ghost.setForbiddenMoves(forbiddenMovesAtTile));
            }
            default -> throw new IllegalGameVariantException(game());
        }

        bonusSymbols = List.of(game().nextBonusSymbol(number()), game().nextBonusSymbol(number()));
        Logger.trace("Game level {} created.", levelData.number());
    }

    public Direction initialGhostDirection(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case GameModel.RED_GHOST -> LEFT;
            case GameModel.PINK_GHOST -> DOWN;
            case GameModel.CYAN_GHOST, GameModel.ORANGE_GHOST -> UP;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2f initialGhostPosition(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case GameModel.RED_GHOST    -> world.house().door().entryPosition();
            case GameModel.PINK_GHOST   -> ArcadeWorld.HOUSE_MIDDLE_SEAT;
            case GameModel.CYAN_GHOST   -> ArcadeWorld.HOUSE_LEFT_SEAT;
            case GameModel.ORANGE_GHOST -> ArcadeWorld.HOUSE_RIGHT_SEAT;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2f ghostRevivalPosition(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case GameModel.RED_GHOST, GameModel.PINK_GHOST -> ArcadeWorld.HOUSE_MIDDLE_SEAT;
            case GameModel.CYAN_GHOST            -> ArcadeWorld.HOUSE_LEFT_SEAT;
            case GameModel.ORANGE_GHOST          -> ArcadeWorld.HOUSE_RIGHT_SEAT;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2i ghostScatterTarget(byte ghostID) {
        checkGhostID(ghostID);
        return switch (ghostID) {
            case GameModel.RED_GHOST    -> ArcadeWorld.SCATTER_TARGET_RIGHT_UPPER_CORNER;
            case GameModel.PINK_GHOST   -> ArcadeWorld.SCATTER_TARGET_LEFT_UPPER_CORNER;
            case GameModel.CYAN_GHOST   -> ArcadeWorld.SCATTER_TARGET_RIGHT_LOWER_CORNER;
            case GameModel.ORANGE_GHOST -> ArcadeWorld.SCATTER_TARGET_LEFT_LOWER_CORNER;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Data data() {
        return data;
    }

    public GameModel game() {
        return GameController.it().game();
    }

    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    public boolean isDemoLevel() {
        return data.demoLevel();
    }

    /**
     * @return level number, starting with 1.
     */
    public int number() {
        return data().number();
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
        var durations = game().huntingDurations(number());
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

    public Vector2i chasingTarget(byte ghostID) {
        return switch (ghostID) {
            // Blinky: attacks Pac-Man directly
            case GameModel.RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case GameModel.PINK_GHOST -> pac.tilesAheadWithOverflowBug(4);
            // Inky: attacks from opposite side as Blinky
            case GameModel.CYAN_GHOST -> pac.tilesAheadWithOverflowBug(2).scaled(2).minus(ghosts[GameModel.RED_GHOST].tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case GameModel.ORANGE_GHOST -> ghosts[GameModel.ORANGE_GHOST].tile().euclideanDistance(pac.tile()) < 8
                ? ghostScatterTarget(GameModel.ORANGE_GHOST)
                : pac.tile();
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    private byte frightenedGhostRelSpeed(Ghost ghost) {
        return world.isTunnel(ghost.tile()) ? data.ghostSpeedTunnelPercentage() : data.ghostSpeedFrightenedPercentage();
    }

    public Direction pseudoRandomDirection() {
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
        if (ghost.id() == GameModel.RED_GHOST && cruiseElroyState == 1) {
            return data.elroy1SpeedPercentage();
        }
        if (ghost.id() == GameModel.RED_GHOST && cruiseElroyState == 2) {
            return data.elroy2SpeedPercentage();
        }
        return data.ghostSpeedPercentage();
    }

    /* --- Here comes the main logic of the game. --- */

    private void scorePoints(int points) {
        if (!isDemoLevel()) {
            game().scorePoints(number(), points);
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
            if (game().isBonusReached(this)) {
                bonusReachedIndex += 1;
                eventLog.bonusIndex = bonusReachedIndex;
                onBonusReached(bonusReachedIndex);
            }
            game().publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
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
            game().publishGameEvent(GameEventType.PAC_GETS_POWER);
        } else if (pac.powerTimer().remaining() == GameModel.PAC_POWER_FADING_TICKS) {
            eventLog.pacStartsLosingPower = true;
            game().publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (pac.powerTimer().hasExpired()) {
            pac.powerTimer().stop();
            pac.powerTimer().resetIndefinitely();
            huntingTimer.start();
            Logger.info("Hunting timer started");
            ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog.pacLostPower = true;
            game().publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    private void updateBonus() {
        if (bonus == null) {
            return;
        }
        if (bonus.state() == Bonus.STATE_EDIBLE && pac.sameTile(bonus.entity())) {
            bonus.setEaten(GameModel.BONUS_POINTS_SHOWN_TICKS);
            scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventLog.bonusEaten = true;
            game().publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
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
            if (ghost.id() == GameModel.ORANGE_GHOST && cruiseElroyState < 0) {
                enableCruiseElroyState(true);
                Logger.trace("Cruise elroy mode re-enabled because {} exits house", ghost.name());
            }
            eventLog.unlockedGhost = ghost;
        });
        ghosts().forEach(ghost -> ghost.update(this));
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
                game().publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(4.5)) {
                bonus().ifPresent(Bonus::setInactive); // needed?
                onBonusReached(1);
            } else if (timer.atSecond(6.5)) {
                bonus().ifPresent(bonus -> bonus.setEaten(60));
                game().publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(8.5)) {
                pac.hide();
                ghosts().forEach(Ghost::hide);
                world.mazeFlashing().restart(2 * data.numFlashes());
            } else if (timer.atSecond(12.0)) {
                timer.restartIndefinitely();
                pac.freeze();
                ghosts().forEach(Ghost::hide);
                bonus().ifPresent(Bonus::setInactive);
                world.mazeFlashing().reset();
                game().createAndStartLevel(number() + 1);
            }
            world.energizerBlinking().tick();
            world.mazeFlashing().tick();
            ghosts().forEach(ghost -> ghost.update(this));
            bonus().ifPresent(bonus -> bonus.update(this));
        } else {
            GameController.it().restart(GameState.BOOT);
        }
    }

    public void killGhosts(List<Ghost> prey) {
        if (!prey.isEmpty()) {
            prey.forEach(this::killGhost);
            if (totalNumGhostsKilled == 16) {
                int points = GameModel.POINTS_ALL_GHOSTS_KILLED_IN_LEVEL;
                scorePoints(points);
                Logger.info("Scored {} points for killing all ghosts at level {}", points, number());
            }
        }
    }

    private void killGhost(Ghost ghost) {
        byte[] multiple = { 2, 4, 8, 16 };
        int killedSoFar = pac.victims().size();
        int points = 100 * multiple[killedSoFar];
        scorePoints(points);
        ghost.eaten(killedSoFar);
        pac.victims().add(ghost);
        eventLog.killedGhosts.add(ghost);
        totalNumGhostsKilled += 1;
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    // Bonus Management

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public byte bonusSymbol(int index) {
        return bonusSymbols.get(index);
    }

    /**
     * Called on bonus achievement (public access for unit tests and level test).
     *
     * @param index bonus index (0 or 1).
     */
    public void onBonusReached(int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException("Bonus index must be 0 or 1");
        }
        bonus = game().createNextBonus(world, bonus, index, bonusSymbol(index)).orElse(null);
        if (bonus != null) {
            game().publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
        }
    }
}