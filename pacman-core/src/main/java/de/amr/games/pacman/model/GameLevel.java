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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.actors.CreatureMovement.roam;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * @author Armin Reichert
 */
public class GameLevel {

    public final int levelNumber; // Level number; starts at 1.
    public final boolean demoLevel;
    public final byte pacSpeedPercentage; // Relative Pac-Man speed (percentage of base speed).
    public final byte ghostSpeedPercentage; // Relative ghost speed when hunting or scattering.
    public final byte ghostSpeedTunnelPercentage; // Relative ghost speed inside tunnel.
    public final byte elroy1DotsLeft;//  Number of pellets left when Blinky becomes "Cruise Elroy" grade 1.
    public final byte elroy1SpeedPercentage; // Relative speed of Blinky being "Cruise Elroy" grade 1.
    public final byte elroy2DotsLeft; // Number of pellets left when Blinky becomes "Cruise Elroy" grade 2.
    public final byte elroy2SpeedPercentage; //Relative speed of Blinky being "Cruise Elroy" grade 2.
    public final byte pacSpeedPoweredPercentage; // Relative speed of Pac-Man in power mode.
    public final byte ghostSpeedFrightenedPercentage; // Relative speed of frightened ghost.
    public final byte pacPowerSeconds; // Number of seconds Pac-Man gets power.
    public final byte numFlashes; // Number of maze flashes at end of this level.
    public final byte intermissionNumber; // Number (1,2,3) of intermission scene played after this level (0=no intermission).

    private final TickTimer huntingTimer = new TickTimer("HuntingTimer");
    private final World world;
    private final Pulse energizerBlinking;
    private final Pulse mazeFlashing;
    private final Pac pac;
    private final Ghost[] ghosts;
    private final List<Byte> bonusSymbols;
    private Bonus bonus;
    private byte huntingPhaseIndex;
    private byte totalNumGhostsKilled;
    private byte cruiseElroyState;
    private byte bonusReachedIndex; // -1=no bonus, 0=first, 1=second

    // Ghost house access-control
    private static final byte UNLIMITED = -1;
    private byte[]  globalDotLimits;
    private byte[]  privateDotLimits;
    private int[]   dotCounters;
    private int     pacStarvingLimit;
    private int     globalDotCounter;
    private boolean globalDotCounterEnabled;

    public GameLevel(int levelNumber, boolean demoLevel, byte[] data, World world) {
        checkLevelNumber(levelNumber);
        checkNotNull(data);
        checkNotNull(world);

        this.levelNumber = levelNumber;
        this.demoLevel   = demoLevel;
        this.world = world;

        this.pacSpeedPercentage             = data[0];
        this.ghostSpeedPercentage           = data[1];
        this.ghostSpeedTunnelPercentage     = data[2];
        this.elroy1DotsLeft                 = data[3];
        this.elroy1SpeedPercentage          = data[4];
        this.elroy2DotsLeft                 = data[5];
        this.elroy2SpeedPercentage          = data[6];
        this.pacSpeedPoweredPercentage      = data[7];
        this.ghostSpeedFrightenedPercentage = data[8];
        this.pacPowerSeconds                = data[9];
        this.numFlashes                     = data[10];
        this.intermissionNumber             = data[11];

        this.energizerBlinking = new Pulse(10, true);
        this.mazeFlashing      = new Pulse(10, false);

        bonusReachedIndex = -1;
        initGhostHouseAccessControl();

        pac = new Pac(game().pacName());
        pac.reset();
        pac.setBaseSpeed(GameModel.PPS_AT_100_PERCENT / (float) GameModel.FPS);
        pac.setPowerFadingTicks(GameModel.PAC_POWER_FADING_TICKS); // not sure about duration

        ghosts = new Ghost[] {
            new Ghost(RED_GHOST,    game().ghostName(RED_GHOST)),
            new Ghost(PINK_GHOST,   game().ghostName(PINK_GHOST)),
            new Ghost(CYAN_GHOST,   game().ghostName(CYAN_GHOST)),
            new Ghost(ORANGE_GHOST, game().ghostName(ORANGE_GHOST))
        };
        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setHouse(world.house());
            ghost.setFrightenedBehavior(g -> roam(g, world, frightenedGhostRelSpeed(g), pseudoRandomDirection()));
            ghost.setHuntingBehavior(game()::huntingBehaviour);
            ghost.setRevivalPosition(GHOST_REVIVAL_POSITIONS[ghost.id()]);
            ghost.setBaseSpeed(GameModel.PPS_AT_100_PERCENT / (float) GameModel.FPS);
            ghost.setSpeedReturningHome(GameModel.PPS_GHOST_RETURNING_HOME / (float) GameModel.FPS);
            ghost.setSpeedInsideHouse(GameModel.PPS_GHOST_INHOUSE / (float) GameModel.FPS);
        });

        bonusSymbols = List.of(game().nextBonusSymbol(this.levelNumber), game().nextBonusSymbol(this.levelNumber));
        Logger.trace("Game level {} created.", this.levelNumber);
    }

    public GameModel game() {
        return GameController.it().game();
    }

    public SimulationStepEventLog eventLog() {
        return GameController.it().eventLog();
    }

    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }

    public World world() {
        return world;
    }

    public Pulse energizerBlinking() {
        return energizerBlinking;
    }

    public Pulse mazeFlashing() {
        return mazeFlashing;
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
            return Stream.of(ghosts).filter(ghost -> ghost.inState(states));
        }
        // when no states are given, return *all* ghosts (ghost.is() would return *no* ghosts!)
        return Stream.of(ghosts);
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
        var durations = game().huntingDurations(levelNumber);
        var ticks = durations[index] == -1 ? TickTimer.INDEFINITE : durations[index];
        huntingTimer.reset(ticks);
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            index, currentHuntingPhaseName(), huntingTimer.duration(),
            (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }

    /**
     * @return number of current phase <code>(0-7)
     */
    public int huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    public void letPacDie() {
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        resetGlobalDotCounterAndSetEnabled(true);
        enableCruiseElroyState(false);
        pac.die();
    }

    public Vector2i chasingTarget(byte ghostID) {
        return switch (ghostID) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> pac.tilesAheadWithOverflowBug(4);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> pac.tilesAheadWithOverflowBug(2).scaled(2).minus(ghosts[RED_GHOST].tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghosts[ORANGE_GHOST].tile().euclideanDistance(pac.tile()) < 8
                ? ArcadeWorld.SCATTER_TILE_SW
                : pac.tile();
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    private byte frightenedGhostRelSpeed(Ghost ghost) {
        return world.isTunnel(ghost.tile()) ? ghostSpeedTunnelPercentage : ghostSpeedFrightenedPercentage;
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
     * @param visible if the guys are visible
     */
    public void letsGetReadyToRumble(boolean visible) {
        pac.reset();
        pac.setPosition(ArcadeWorld.PAC_POSITION);
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.setVisible(visible);
        pac.resetAnimation();
        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setPosition(GHOST_POSITIONS_ON_START[ghost.id()]);
            ghost.setMoveAndWishDir(GHOST_DIRECTIONS_ON_START[ghost.id()]);
            ghost.setVisible(visible);
            ghost.setState(LOCKED);
            ghost.resetAnimation();
        });
        mazeFlashing.reset();
        energizerBlinking.reset();
    }

    /**
     * @param ghost a ghost
     * @return relative speed of ghost in percent of the base speed
     */
    public byte huntingSpeedPercentage(Ghost ghost) {
        if (world.isTunnel(ghost.tile())) {
            return ghostSpeedTunnelPercentage;
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 1) {
            return elroy1SpeedPercentage;
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 2) {
            return elroy2SpeedPercentage;
        }
        return ghostSpeedPercentage;
    }

    /* --- Here comes the main logic of the game. --- */

    private void scorePoints(int points) {
        if (!isDemoLevel()) {
            game().scorePoints(levelNumber, points);
        }
    }

    private void updateFood() {
        final Vector2i pacTile = pac.tile();
        if (world.hasFoodAt(pacTile)) {
            eventLog().foodFoundTile = pacTile;
            pac.endStarving();
            if (world.isEnergizerTile(pacTile)) {
                eventLog().energizerFound = true;
                pac.setRestingTicks(GameModel.RESTING_TICKS_ENERGIZER);
                pac.victims().clear();
                scorePoints(GameModel.POINTS_ENERGIZER);
                handleEnergizerEaten();
                Logger.info("Scored {} points for eating energizer", GameModel.POINTS_ENERGIZER);
            } else {
                pac.setRestingTicks(GameModel.RESTING_TICKS_PELLET);
                scorePoints(GameModel.POINTS_PELLET);
            }
            updateDotCount();
            world.eatFoodAt(pacTile);
            if (world.uneatenFoodCount() == elroy1DotsLeft) {
                setCruiseElroyState(1);
            } else if (world.uneatenFoodCount() == elroy2DotsLeft) {
                setCruiseElroyState(2);
            }
            if (game().isBonusReached(this)) {
                bonusReachedIndex += 1;
                eventLog().bonusIndex = bonusReachedIndex;
                onBonusReached(bonusReachedIndex);
            }
            game().publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            pac.starve();
        }
    }

    private void updatePac() {
        pac.update(this);
        if (pac.powerTimer().remaining() == GameModel.PAC_POWER_FADING_TICKS) {
            eventLog().pacStartsLosingPower = true;
            game().publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (pac.powerTimer().hasExpired()) {
            pac.powerTimer().stop();
            pac.powerTimer().resetIndefinitely();
            huntingTimer.start();
            Logger.info("Hunting timer started");
            ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog().pacLostPower = true;
            game().publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    private void handleEnergizerEaten() {
        if (pacPowerSeconds > 0) {
            eventLog().pacGetsPower = true;
            huntingTimer.stop();
            Logger.info("Hunting timer stopped");
            pac.powerTimer().restartSeconds(pacPowerSeconds);
            // TODO do already frightened ghosts reverse too?
            ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
            game().publishGameEvent(GameEventType.PAC_GETS_POWER);
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
            eventLog().bonusEaten = true;
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
        Ghost unlockedGhost = unlockGhost();
        if (unlockedGhost != null) {
            if (unlockedGhost.insideHouse(world.house())) {
                unlockedGhost.setState(LEAVING_HOUSE);
            } else {
                unlockedGhost.setMoveAndWishDir(LEFT);
                unlockedGhost.setState(HUNTING_PAC);
            }
            if (unlockedGhost.id() == ORANGE_GHOST && cruiseElroyState < 0) {
                enableCruiseElroyState(true);
                Logger.trace("Cruise elroy mode re-enabled because {} exits house", unlockedGhost.name());
            }
        }
        ghosts().forEach(ghost -> ghost.update(this));
    }

    public GameState doHuntingStep() {
        updateFood();
        updateGhosts();
        updatePac();
        updateBonus();
        updateHuntingTimer();
        energizerBlinking.tick();

        // what next?
        if (world.uneatenFoodCount() == 0) {
            return GameState.LEVEL_COMPLETE;
        }
        var killers = ghosts(HUNTING_PAC).filter(pac::sameTile).toList();
        if (!killers.isEmpty() && !GameController.it().isPacImmune()) {
            eventLog().pacDied = true;
            return GameState.PACMAN_DYING;
        }
        var prey = ghosts(FRIGHTENED).filter(pac::sameTile).toList();
        if (!prey.isEmpty()) {
            killGhosts(prey);
            return GameState.GHOST_DYING;
        }
        return GameState.HUNTING;
    }

    public void onCompleted() {
        mazeFlashing.reset();
        pac.freeze();
        ghosts().forEach(Ghost::hide);
        bonus().ifPresent(Bonus::setInactive);
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        Logger.trace("Game level {} ({}) completed.", levelNumber, game());
    }

    public void doLevelTestStep(TickTimer timer, int lastTestedLevel) {
        if (levelNumber <= lastTestedLevel) {
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
                mazeFlashing().restart(2 * numFlashes);
            } else if (timer.atSecond(12.0)) {
                timer.restartIndefinitely();
                pac.freeze();
                ghosts().forEach(Ghost::hide);
                bonus().ifPresent(Bonus::setInactive);
                mazeFlashing().reset();
                game().createAndStartLevel(levelNumber + 1);
            }
            energizerBlinking().tick();
            mazeFlashing().tick();
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
                Logger.info("Scored {} points for killing all ghosts at level {}", points, levelNumber);
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
        eventLog().killedGhosts.add(ghost);
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

    /**
     * From the Pac-Man dossier:
     * <p>
     * Commonly referred to as the ghost house or monster pen, this cordoned-off area in the center of the maze is the
     * domain of the four ghosts and off-limits to Pac-Man.
     * </p>
     * <p>
     * Whenever a level is completed or a life is lost, the ghosts are returned to their starting positions in and around
     * the ghost house before play continues—Blinky is always located just above and outside, while the other three are
     * placed inside: Inky on the left, Pinky in the middle, and Clyde on the right.
     * The pink door on top is used by the ghosts to enter or exit the house. Once a ghost leaves, however, it cannot
     * reenter unless it is first captured by Pac-Man—then the disembodied eyes can return home to be revived.
     * Since Blinky is already on the outside after a level is completed or a life is lost, the only time he can get
     * inside the ghost house is after Pac-Man captures him, and he immediately turns around to leave once revived.
     * That's about all there is to know about Blinky's behavior in terms of the ghost house, but determining when the
     * other three ghosts leave home is an involved process based on several variables and conditions.
     * The rest of this section will deal with them exclusively. Accordingly, any mention of “the ghosts” below refers t
     * o Pinky, Inky, and Clyde, but not Blinky.
     * </p>
     * <p>
     * The first control used to evaluate when the ghosts leave home is a personal counter each ghost retains for
     * tracking the number of dots Pac-Man eats. Each ghost's “dot counter” is reset to zero when a level begins and can
     * only be active when inside the ghost house, but only one ghost's counter can be active at any given time regardless
     * of how many ghosts are inside. The order of preference for choosing which ghost's counter to activate is:
     * Pinky, then Inky, and then Clyde. For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its
     * dot counter increased by one. Each ghost also has a “dot limit” associated with his counter, per level.
     * If the preferred ghost reaches or exceeds his dot limit, it immediately exits the house and its dot counter is
     * deactivated (but not reset). The most-preferred ghost still waiting inside the house (if any) activates its timer
     * at this point and begins counting dots.
     * </p>
     * <p>
     * Pinky's dot limit is always set to zero, causing him to leave home immediately when every level begins.
     * For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60. This results in Pinky exiting
     * immediately which, in turn, activates Inky's dot counter. His counter must then reach or exceed 30 dots before
     * he can leave the house. Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and
     * starts counting dots. When his counter reaches or exceeds 60, he may exit. On the second level, Inky's dot limit
     * is changed from 30 to zero, while Clyde's is changed from 60 to 50. Inky will exit the house as soon as the level
     * begins from now on. Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game
     * and will leave the ghost house immediately at the start of every level.
     * </p>
     * <p>
     * Whenever a life is lost, the system disables (but does not reset) the ghosts' individual dot counters and uses
     * a global dot counter instead. This counter is enabled and reset to zero after a life is lost, counting the number
     * of dots eaten from that point forward. The three ghosts inside the house must wait for this special counter to
     * tell them when to leave. Pinky is released when the counter value is equal to 7 and Inky is released when it
     * equals 17. The only way to deactivate the counter is for Clyde to be inside the house when the counter equals 32;
     * otherwise, it will keep counting dots even after the ghost house is empty. If Clyde is present at the
     * appropriate time, the global counter is reset to zero and deactivated, and the ghosts' personal dot limits are
     * re-enabled and used as before for determining when to leave the house (including Clyde who is still in the house
     * at this time).
     * </p>
     * <p>
     * If dot counters were the only control, Pac-Man could simply stop eating dots early on and keep the ghosts
     * trapped inside the house forever. Consequently, a separate timer control was implemented to handle this case by
     * tracking the amount of time elapsed since Pac-Man has last eaten a dot. This timer is always running but gets
     * reset to zero each time a dot is eaten. Anytime Pac-Man avoids eating dots long enough for the timer to reach
     * its limit, the most-preferred ghost waiting in the ghost house (if any) is forced to leave immediately, and
     * the timer is reset to zero. The same order of preference described above is used by this control as well.
     * The game begins with an initial timer limit of four seconds, but lowers to it to three seconds starting with
     * level five.
     * </p>
     * <p>
     * The more astute reader may have already noticed there is subtle flaw in this system resulting in a way to
     * keep Pinky, Inky, and Clyde inside the ghost house for a very long time after eating them.
     * The trick involves having to sacrifice a life in order to reset and enable the global dot counter,
     * and then making sure Clyde exits the house before that counter is equal to 32.
     * This is accomplished by avoiding eating dots and waiting for the timer limit to force Clyde out.
     * Once Clyde is moving for the exit, start eating dots again until at least 32 dots have been consumed since
     * the life was lost. Now head for an energizer and gobble up some ghosts. Blinky will leave the house immediately
     * as usual, but the other three ghosts will remain “stuck” inside as long as Pac-Man continues eating dots with
     * sufficient frequency as not to trigger the control timer. Why does this happen?
     * The key lies in how the global dot counter works—it cannot be deactivated if Clyde is outside the house when
     * the counter has a value of 32. By letting the timer force Clyde out before 32 dots are eaten, the global dot
     * counter will keep counting dots instead of deactivating when it reaches 32. Now when the ghosts are eaten by
     * Pac-Man and return home, they will still be using the global dot counter to determine when to leave.
     * As previously described, however, this counter's logic only checks for three values: 7, 17, and 32, and
     * once those numbers are exceeded, the counter has no way to release the ghosts associated with them.
     * The only control left to release the ghosts is the timer which can be easily avoided by eating a dot every
     * so often to reset it.
     * </p>
     * </pre>
     */
    private void initGhostHouseAccessControl() {
        globalDotLimits = new byte[] {UNLIMITED, 7, 17, UNLIMITED};
        privateDotLimits = new byte[] {0, 0, 0, 0};
        if (levelNumber == 1) {
            privateDotLimits[CYAN_GHOST] = 30;
            privateDotLimits[ORANGE_GHOST] = 60;
        } else if (levelNumber == 2) {
            privateDotLimits[ORANGE_GHOST] = 50;
        }
        dotCounters = new int[] {0, 0, 0, 0};
        globalDotCounter = 0;
        globalDotCounterEnabled = false;
        pacStarvingLimit = levelNumber < 5 ? 240 : 180; // 4 sec : 3 sec
    }

    private void resetGlobalDotCounterAndSetEnabled(boolean enabled) {
        globalDotCounter = 0;
        globalDotCounterEnabled = enabled;
        Logger.trace("Global dot counter set to 0 and {}", enabled ? "enabled" : "disabled");
    }

    private void updateDotCount() {
        if (globalDotCounterEnabled) {
            if (ghost(ORANGE_GHOST).inState(LOCKED) && globalDotCounter == 32) {
                Logger.trace("{} inside house when global counter reached 32", ghost(ORANGE_GHOST).name());
                resetGlobalDotCounterAndSetEnabled(false);
            } else {
                globalDotCounter++;
                Logger.trace("Global dot counter = {}", globalDotCounter);
            }
        } else {
            ghosts(LOCKED).filter(ghost -> ghost.insideHouse(world.house())).findFirst().ifPresent(ghost -> {
                    dotCounters[ghost.id()]++;
                    Logger.trace("{} dot counter = {}", ghost.name(), dotCounters[ghost.id()]);
                });
        }
    }

    private Ghost unlockGhost() {
        // Important: Ghosts must be returned in order RED, PINK, CYAN, ORANGE
        Ghost prisoner = ghosts(LOCKED).findFirst().orElse(null);
        if (prisoner == null) {
            return null;
        }
        byte id = prisoner.id();
        if (id == RED_GHOST) {
            eventLog().unlockedGhost = prisoner;
            eventLog().unlockGhostReason = "Red ghost is unlocked immediately";
            return prisoner;
        }
        // check private dot counter first (if enabled)
        if (!globalDotCounterEnabled && dotCounters[id] >= privateDotLimits[id]) {
            eventLog().unlockedGhost = prisoner;
            eventLog().unlockGhostReason = String.format("Private dot counter at limit (%d)", privateDotLimits[id]);
            return prisoner;
        }
        // check global dot counter
        if (globalDotLimits[id] != UNLIMITED && globalDotCounter >= globalDotLimits[id]) {
            eventLog().unlockedGhost = prisoner;
            eventLog().unlockGhostReason = String.format("Global dot limit (%d) reached", globalDotLimits[id]);
            return prisoner;
        }
        // check Pac-Man starving time
        if (pac.starvingTicks() >= pacStarvingLimit) {
            pac.endStarving();
            eventLog().unlockedGhost = prisoner;
            eventLog().unlockGhostReason = String.format("%s reached starving limit (%d ticks)", pac.name(), pacStarvingLimit);
            return prisoner;
        }
        return null;
    }
}