/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.actors.CreatureMovement.followTarget;
import static de.amr.games.pacman.model.actors.CreatureMovement.roam;
import static de.amr.games.pacman.model.actors.GhostState.*;
import static de.amr.games.pacman.model.world.ArcadeWorld.*;

/**
 * Game models/variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariants implements GameModel {

    MS_PACMAN {
        final String PAC_NAME = "Ms. Pac-Man";
        final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Sue" };
        final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50}; // * 100
        final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");

        /**
         * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
         *
         * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
         * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
         */
        final int[][] HUNTING_DURATIONS = {
            {7 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 1-4
            {5 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 5+
        };

        @Override
        public String pacName() {
            return PAC_NAME;
        }

        @Override
        public String ghostName(byte id) {
            checkGhostID(id);
            return GHOST_NAMES[id];
        }

        @Override
        public boolean isBonusReached() {
            return level.world().eatenFoodCount() == 64 || level.world().eatenFoodCount() == 176;
        }

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
        @Override
        public byte nextBonusSymbol(int levelNumber) {
            checkLevelNumber(levelNumber);
            if (levelNumber <= 7) {
                return (byte) (levelNumber - 1);
            }
            int choice = randomInt(0, 320);
            if (choice <  50) return 0; // 5/32 probability
            if (choice < 100) return 1; // 5/32
            if (choice < 150) return 2; // 5/32
            if (choice < 200) return 3; // 5/32
            if (choice < 240) return 4; // 4/32
            if (choice < 280) return 5; // 4/32
            else              return 6; // 4/32
        }

        @Override
        public int bonusValue(byte symbol) {
            return BONUS_VALUE_FACTORS[symbol] * 100;
        }

        @Override
        public Optional<Bonus> createNextBonus(World world, Bonus existingBonus, int bonusIndex, byte symbol) {
            if (existingBonus != null && existingBonus.state() != Bonus.STATE_INACTIVE) {
                Logger.info("Previous bonus is still active, skip this one");
                return Optional.empty();
            }
            var bonus = createMovingBonus(world, symbol, RND.nextBoolean());
            bonus.setEdible(TickTimer.INDEFINITE);
            return Optional.of(bonus);
        }

        /**
         * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the
         * house and finally leaves the world through a random portal on the opposite side of the world.
         * <p>
         * TODO: This is not the exact behavior as in the original Arcade game.
         **/
        private Bonus createMovingBonus(World world, byte symbol, boolean leftToRight) {
            var houseEntry = tileAt(world.house().door().entryPosition());
            var houseEntryOpposite= houseEntry.plus(0, world.house().size().y() + 1);
            var entryPortal = world.portals().get(RND.nextInt(world.portals().size()));
            var exitPortal  = world.portals().get(RND.nextInt(world.portals().size()));

            var route = List.of(
                np(leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd()),
                np(houseEntry),
                np(houseEntryOpposite),
                np(houseEntry),
                np(leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0))
            );

            var movingBonus = new MovingBonus(symbol, bonusValue(symbol));
            movingBonus.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            // pass copy of list because route gets modified
            movingBonus.setRoute(new ArrayList<>(route), leftToRight);
            Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
            return movingBonus;
        }

        @Override
        public int[] huntingDurations(int levelNumber) {
            return HUNTING_DURATIONS[levelNumber <= 4 ? 0 : 1];
        }

        @Override
        public File highScoreFile() {
            return HIGH_SCORE_FILE;
        }

        @Override
        public void createAndStartLevel(int levelNumber, boolean demoLevel) {
            checkLevelNumber(levelNumber);

            if (demoLevel) {
                level = new GameLevel(1, true, LEVEL_DATA[0], createMsPacManWorld(1));
            } else {
                int rowIndex = Math.min(levelNumber - 1, LEVEL_DATA.length - 1);
                level = new GameLevel(levelNumber, false, LEVEL_DATA[rowIndex],
                    createMsPacManWorld(mapNumberMsPacMan(levelNumber)));
            }
            initGhostHouseAccessControl();

            level.pac().reset();
            level.pac().setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            level.pac().setPowerFadingTicks(PAC_POWER_FADING_TICKS); // not sure about duration
            level.pac().setAutopilot(new RuleBasedPacSteering(this));
            level.pac().setUseAutopilot(demoLevel);

            level.ghosts().forEach(ghost -> {
                ghost.reset();
                ghost.setHouse(level.world().house());
                ghost.setFrightenedBehavior(refugee ->
                    roam(refugee, level.world(), level.frightenedGhostRelSpeed(refugee), pseudoRandomDirection()));
                ghost.setHuntingBehavior(this::huntingBehaviour);
                ghost.setRevivalPosition(GHOST_REVIVAL_POSITIONS[ghost.id()]);
                ghost.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
                ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME / (float) FPS);
                ghost.setSpeedInsideHouse(PPS_GHOST_INHOUSE / (float) FPS);
            });

            huntingPhaseIndex = 0;
            huntingTimer.resetIndefinitely();
            bonusReachedIndex = -1;
            bonusSymbols = List.of(nextBonusSymbol(levelNumber), nextBonusSymbol(levelNumber));
            bonus = null;
            numGhostsKilledInLevel = 0;

            score.setLevelNumber(levelNumber);
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            if (levelNumber <= 7) {
                // In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
                // (also inside a level) whenever a bonus score is reached. At least that's what I was told.
                addSymbolToLevelCounter(bonusSymbol(0));
            }
            Logger.info("Level {} created ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts must have been created!
            letsGetReadyToRumble();
            if (demoLevel) {
                level.pac().show();
                level.ghosts().forEach(Ghost::show);
            } else {
                level.pac().hide();
                level.ghosts().forEach(Ghost::hide);
            }
            Logger.info("Level {} started ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }

        /**
         * <p>
         * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* hunting/scatter phase. Some say,
         * the original intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man
         * but because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who knows?
         * </p>
         */
        @Override
        public void huntingBehaviour(Ghost ghost, GameLevel level) {
            if (scatterPhase().isPresent() && scatterPhase().get() == 0
                && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
                roam(ghost, level.world(), level.huntingSpeedPercentage(ghost), pseudoRandomDirection());
            } else {
                PACMAN.huntingBehaviour(ghost, level);
            }
        }
    },

    /**
     * All about the Pac-Man Arcade game (1980)  can be found in:
     * <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>.
     */
    PACMAN {
        final String PAC_NAME = "Pac-Man";
        final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Clyde" };
        final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {7 /* default */, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
        final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50}; // * 100
        final File HIGH_SCORE_FILE = new File(System.getProperty("user.home"), "highscore-pacman.xml");
        final int[][] HUNTING_DURATIONS = { // Hunting duration (in ticks) of chase and scatter phases.
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS,   20 * FPS, 5 * FPS, -1}, // Level 1
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS,       1, -1}, // Levels 2-4
            {5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS,       1, -1}, // Levels 5+
        };

        @Override
        public String pacName() {
            return PAC_NAME;
        }

        @Override
        public String ghostName(byte id) {
            checkGhostID(id);
            return GHOST_NAMES[id];
        }

        @Override
        public boolean isBonusReached() {
            return level.world().eatenFoodCount() == 70 || level.world().eatenFoodCount() == 170;
        }

        // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
        @Override
        public byte nextBonusSymbol(int levelNumber) {
            return BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber < 13 ? levelNumber : 0];
        }

        @Override
        public Optional<Bonus> createNextBonus(World world, Bonus existingBonus, int bonusIndex, byte symbol) {
            var bonus = new StaticBonus(symbol, bonusValue(symbol));
            bonus.entity().setPosition(ArcadeWorld.BONUS_POSITION);
            bonus.setEdible(randomInt(9 * FPS, 10 * FPS));
            return Optional.of(bonus);
        }

        @Override
        public int bonusValue(byte symbol) {
            return BONUS_VALUE_FACTORS[symbol] * 100;
        }

        @Override
        public int[] huntingDurations(int levelNumber) {
            checkLevelNumber(levelNumber);
            return switch (levelNumber) {
                case 1       -> HUNTING_DURATIONS[0];
                case 2, 3, 4 -> HUNTING_DURATIONS[1];
                default      -> HUNTING_DURATIONS[2];
            };
        }

        @Override
        public File highScoreFile() {
            return HIGH_SCORE_FILE;
        }

        @Override
        public void createAndStartLevel(int levelNumber, boolean demoLevel) {
            checkLevelNumber(levelNumber);

            if (demoLevel) {
                level = new GameLevel(1, true, LEVEL_DATA[0], createPacManWorld());
            } else {
                int rowIndex = Math.min(levelNumber - 1, LEVEL_DATA.length - 1);
                level = new GameLevel(levelNumber, false, LEVEL_DATA[rowIndex], createPacManWorld());
            }
            addForbiddenMoves(level);
            initGhostHouseAccessControl();

            level.pac().reset();
            level.pac().setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
            level.pac().setPowerFadingTicks(PAC_POWER_FADING_TICKS); // not sure about duration
            if (demoLevel) {
                level.pac().setAutopilot(new RouteBasedSteering(List.of(ArcadeWorld.PACMAN_DEMO_LEVEL_ROUTE)));
                level.pac().setUseAutopilot(true);
            } else {
                level.pac().setAutopilot(new RuleBasedPacSteering(this));
                level.pac().setUseAutopilot(false);
            }

            level.ghosts().forEach(ghost -> {
                ghost.reset();
                ghost.setHouse(level.world().house());
                ghost.setFrightenedBehavior(refugee ->
                    roam(refugee, level.world(), level.frightenedGhostRelSpeed(refugee), pseudoRandomDirection()));
                ghost.setHuntingBehavior(this::huntingBehaviour);
                ghost.setRevivalPosition(GHOST_REVIVAL_POSITIONS[ghost.id()]);
                ghost.setBaseSpeed(PPS_AT_100_PERCENT / (float) FPS);
                ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME / (float) FPS);
                ghost.setSpeedInsideHouse(PPS_GHOST_INHOUSE / (float) FPS);
            });

            huntingPhaseIndex = 0;
            huntingTimer.resetIndefinitely();
            bonusReachedIndex = -1;
            bonusSymbols = List.of(nextBonusSymbol(levelNumber), nextBonusSymbol(levelNumber));
            bonus = null;
            numGhostsKilledInLevel = 0;

            score.setLevelNumber(levelNumber);
            if (levelNumber == 1) {
                levelCounter.clear();
            }
            addSymbolToLevelCounter(bonusSymbol(0));

            Logger.info("Level {} created ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_CREATED);

            // At this point, the animations of Pac-Man and the ghosts must have been created!
            letsGetReadyToRumble();
            if (demoLevel) {
                level.pac().show();
                level.ghosts().forEach(Ghost::show);
            } else {
                level.pac().hide();
                level.ghosts().forEach(Ghost::hide);
            }
            Logger.info("Level {} started ({})", levelNumber, this);
            publishGameEvent(GameEventType.LEVEL_STARTED);
        }

        private void addForbiddenMoves(GameLevel level) {
            var forbidden = new HashMap<Vector2i, List<Direction>>();
            var up = List.of(UP);
            ArcadeWorld.PACMAN_RED_ZONE.forEach(tile -> forbidden.put(tile, up));
            level.ghosts().forEach(ghost -> ghost.setForbiddenMoves(forbidden));
        }

        @Override
        public void huntingBehaviour(Ghost ghost, GameLevel level) {
            byte relSpeed = level.huntingSpeedPercentage(ghost);
            if (chasingPhase().isPresent() || ghost.id() == RED_GHOST && level.cruiseElroyState() > 0) {
                followTarget(ghost, level.world(), level.chasingTarget(ghost.id()), relSpeed);
            } else {
                followTarget(ghost, level.world(), ghostScatterTarget(ghost.id()), relSpeed);
            }
        }
    };

    // --- Common to all variants --------------------------------------------------------------------------------------

    final List<Byte> levelCounter = new LinkedList<>();
    final Score score = new Score();
    final Score highScore = new Score();

    GameLevel level;
    boolean playing;
    short initialLives = 3;
    short lives;

    final TickTimer huntingTimer = new TickTimer("HuntingTimer");
    byte huntingPhaseIndex;
    byte numGhostsKilledInLevel;
    byte bonusReachedIndex; // -1=no bonus, 0=first, 1=second
    List<Byte> bonusSymbols;
    Bonus bonus;

    // Ghost house access-control
    static final byte UNLIMITED = -1;
    byte[]  globalDotLimits;
    byte[]  privateDotLimits;
    int[]   dotCounters;
    int     pacStarvingLimit;
    int     globalDotCounter;
    boolean globalDotCounterEnabled;

    @Override
    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    /**
     * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
     * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
     * ghosts attack Pac-Man.
     *
     * @param index hunting phase index (0..7)
     */
    @Override
    public void startHuntingPhase(int index) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + index);
        }
        huntingPhaseIndex = (byte) index;
        var durations = huntingDurations(level.levelNumber);
        var ticks = durations[index] == -1 ? TickTimer.INDEFINITE : durations[index];
        huntingTimer.reset(ticks);
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            index, currentHuntingPhaseName(), huntingTimer.duration(),
            (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }

    @Override
    public byte huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    /**
     * @return (optional) index of current scattering phase <code>(0-3)</code>
     */
    @Override
    public Optional<Integer> scatterPhase() {
        return isEven(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    /**
     * @return (optional) index of current chasing phase <code>(0-3)</code>
     */
    @Override
    public Optional<Integer> chasingPhase() {
        return isOdd(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    @Override
    public String currentHuntingPhaseName() {
        return isEven(huntingPhaseIndex) ? "Scattering" : "Chasing";
    }

    SimulationStepEventLog eventLog() {
        return GameController.it().eventLog();
    }

    Direction pseudoRandomDirection() {
        float rnd = Globals.randomFloat(0, 100);
        if (rnd < 16.3) return UP;
        if (rnd < 16.3 + 25.2) return RIGHT;
        if (rnd < 16.3 + 25.2 + 28.5) return DOWN;
        return LEFT;
    }

    Vector2i ghostScatterTarget(byte ghostID) {
        return switch (ghostID) {
            case RED_GHOST -> SCATTER_TILE_NE;
            case PINK_GHOST -> SCATTER_TILE_NW;
            case CYAN_GHOST -> SCATTER_TILE_SE;
            case ORANGE_GHOST -> SCATTER_TILE_SW;
            default -> throw new IllegalGhostIDException(ghostID);
        };
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
    void initGhostHouseAccessControl() {
        globalDotLimits = new byte[] {UNLIMITED, 7, 17, UNLIMITED};
        privateDotLimits = new byte[] {0, 0, 0, 0};
        if (level.levelNumber == 1) {
            privateDotLimits[CYAN_GHOST] = 30;
            privateDotLimits[ORANGE_GHOST] = 60;
        } else if (level.levelNumber == 2) {
            privateDotLimits[ORANGE_GHOST] = 50;
        }
        dotCounters = new int[] {0, 0, 0, 0};
        globalDotCounter = 0;
        globalDotCounterEnabled = false;
        pacStarvingLimit = level.levelNumber < 5 ? 240 : 180; // 4 sec : 3 sec
    }

    void resetGlobalDotCounterAndSetEnabled(boolean enabled) {
        globalDotCounter = 0;
        globalDotCounterEnabled = enabled;
        Logger.trace("Global dot counter set to 0 and {}", enabled ? "enabled" : "disabled");
    }

    @Override
    public void updateDotCount() {
        if (globalDotCounterEnabled) {
            if (level.ghost(ORANGE_GHOST).inState(LOCKED) && globalDotCounter == 32) {
                Logger.trace("{} inside house when global counter reached 32", level.ghost(ORANGE_GHOST).name());
                resetGlobalDotCounterAndSetEnabled(false);
            } else {
                globalDotCounter++;
                Logger.trace("Global dot counter = {}", globalDotCounter);
            }
        } else {
            level.ghosts(LOCKED).filter(ghost -> ghost.insideHouse(level.world().house())).findFirst().ifPresent(ghost -> {
                dotCounters[ghost.id()]++;
                Logger.trace("{} dot counter = {}", ghost.name(), dotCounters[ghost.id()]);
            });
        }
    }

    @Override
    public Ghost unlockGhost() {
        // Important: Ghosts must be returned in order RED, PINK, CYAN, ORANGE
        Ghost prisoner = level.ghosts(LOCKED).findFirst().orElse(null);
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
        if (level.pac().starvingTicks() >= pacStarvingLimit) {
            level.pac().endStarving();
            eventLog().unlockedGhost = prisoner;
            eventLog().unlockGhostReason = String.format("%s reached starving limit (%d ticks)",
                level.pac().name(), pacStarvingLimit);
            return prisoner;
        }
        return null;
    }



    @Override
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void reset() {
        level = null;
        playing = false;
        lives = initialLives;
        score.reset();
        Logger.info("Game model ({}) reset", this);
    }

    @Override
    public void letsGetReadyToRumble() {
        level.pac().reset();
        level.pac().setPosition(ArcadeWorld.PAC_POSITION);
        level.pac().setMoveAndWishDir(Direction.LEFT);
        level.pac().selectAnimation(Pac.ANIM_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setPosition(GHOST_POSITIONS_ON_START[ghost.id()]);
            ghost.setMoveAndWishDir(GHOST_DIRECTIONS_ON_START[ghost.id()]);
            ghost.setState(LOCKED);
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
        level.blinking().setStartPhase(Pulse.ON); // Energizers are visible when ON
        level.blinking().reset();
    }

    @Override
    public void onPacDying() {
        huntingTimer().stop();
        Logger.info("Hunting timer stopped");
        resetGlobalDotCounterAndSetEnabled(true);
        level.enableCruiseElroyState(false);
        level.pac().die();
    }

    @Override
    public void onLevelCompleted() {
        level.blinking().setStartPhase(Pulse.OFF);
        level.blinking().reset();
        level.pac().freeze();
        level.ghosts().forEach(Ghost::hide);
        bonus().ifPresent(Bonus::setInactive);
        huntingTimer().stop();
        Logger.info("Hunting timer stopped");
        Logger.trace("Game level {} ({}) completed.", level.levelNumber, this);
    }

    @Override
    public void doLevelTestStep(TickTimer timer, int lastTestedLevel) {
        if (level.levelNumber <= lastTestedLevel) {
            if (timer.tick() > 2 * FPS) {
                level.blinking().tick();
                level.ghosts().forEach(ghost -> ghost.update(level));
                bonus().ifPresent(bonus -> bonus.update(level));
            }
            if (timer.atSecond(1.0)) {
                level.game().letsGetReadyToRumble();
                level.pac().show();
                level.ghosts().forEach(Ghost::show);
            } else if (timer.atSecond(2)) {
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().restart();
            } else if (timer.atSecond(2.5)) {
                onBonusReached(0);
            } else if (timer.atSecond(3.5)) {
                bonus().ifPresent(bonus -> bonus.setEaten(120));
                level.game().publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(4.5)) {
                bonus().ifPresent(Bonus::setInactive); // needed?
                onBonusReached(1);
            } else if (timer.atSecond(6.5)) {
                bonus().ifPresent(bonus -> bonus.setEaten(60));
                publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(8.5)) {
                level.pac().hide();
                level.ghosts().forEach(Ghost::hide);
                level.blinking().stop();
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().reset();
            } else if (timer.atSecond(9.5)) {
                GameController.it().state().setProperty("mazeFlashing", true);
                level.blinking().setStartPhase(Pulse.OFF);
                level.blinking().restart(2 * level.numFlashes());
            } else if (timer.atSecond(12.0)) {
                timer.restartIndefinitely();
                level.pac().freeze();
                level.ghosts().forEach(Ghost::hide);
                bonus().ifPresent(Bonus::setInactive);
                GameController.it().state().setProperty("mazeFlashing", false);
                level.blinking().reset();
                createAndStartLevel(level.levelNumber + 1, false);
            }
        } else {
            GameController.it().restart(GameState.BOOT);
        }
    }

    // Bonus Management

    @Override
    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public byte bonusSymbol(int index) {
        return bonusSymbols.get(index);
    }

    @Override
    public void onBonusReached(int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException("Bonus index must be 0 or 1");
        }
        bonus = createNextBonus(level.world(), bonus, index, bonusSymbol(index)).orElse(null);
        if (bonus != null) {
            publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
        }
    }


    private void scorePoints(int points) {
        if (!level.isDemoLevel()) {
            scorePoints(level.levelNumber, points);
        }
    }

    private void updateFood() {
        final Vector2i pacTile = level.pac().tile();
        if (level.world().hasFoodAt(pacTile)) {
            eventLog().foodFoundTile = pacTile;
            level.pac().endStarving();
            if (level.world().isEnergizerTile(pacTile)) {
                eventLog().energizerFound = true;
                level.pac().setRestingTicks(GameModel.RESTING_TICKS_ENERGIZER);
                level.pac().victims().clear();
                scorePoints(GameModel.POINTS_ENERGIZER);
                handleEnergizerEaten();
                Logger.info("Scored {} points for eating energizer", GameModel.POINTS_ENERGIZER);
            } else {
                level.pac().setRestingTicks(GameModel.RESTING_TICKS_PELLET);
                scorePoints(GameModel.POINTS_PELLET);
            }
            updateDotCount();
            level.world().eatFoodAt(pacTile);
            if (level.world().uneatenFoodCount() == level.elroy1DotsLeft()) {
                level.setCruiseElroyState(1);
            } else if (level.world().uneatenFoodCount() == level.elroy2DotsLeft()) {
                level.setCruiseElroyState(2);
            }
            if (isBonusReached()) {
                bonusReachedIndex += 1;
                eventLog().bonusIndex = bonusReachedIndex;
                onBonusReached(bonusReachedIndex);
            }
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            level.pac().starve();
        }
    }

    private void updatePac() {
        level.pac().update(level);
        if (level.pac().powerTimer().remaining() == GameModel.PAC_POWER_FADING_TICKS) {
            eventLog().pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (level.pac().powerTimer().hasExpired()) {
            level.pac().powerTimer().stop();
            level.pac().powerTimer().resetIndefinitely();
            huntingTimer().start();
            Logger.info("Hunting timer started");
            level.ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog().pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    private void handleEnergizerEaten() {
        if (level.pacPowerSeconds() > 0) {
            eventLog().pacGetsPower = true;
            huntingTimer().stop();
            Logger.info("Hunting timer stopped");
            level.pac().powerTimer().restartSeconds(level.pacPowerSeconds());
            // TODO do already frightened ghosts reverse too?
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            level.ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
            publishGameEvent(GameEventType.PAC_GETS_POWER);
        }
    }

    private void updateBonus() {
        if (bonus == null) {
            return;
        }
        if (bonus.state() == Bonus.STATE_EDIBLE && level.pac().sameTile(bonus.entity())) {
            bonus.setEaten(GameModel.BONUS_POINTS_SHOWN_TICKS);
            scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventLog().bonusEaten = true;
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
            bonus.update(level);
        }
    }

    private void updateHuntingTimer( ) {
        if (huntingTimer().hasExpired()) {
            level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
            startHuntingPhase(huntingPhaseIndex + 1);
        } else {
            huntingTimer().advance();
        }
    }

    private void updateGhosts() {
        Ghost unlockedGhost = unlockGhost();
        if (unlockedGhost != null) {
            if (unlockedGhost.insideHouse(level.world().house())) {
                unlockedGhost.setState(LEAVING_HOUSE);
            } else {
                unlockedGhost.setMoveAndWishDir(LEFT);
                unlockedGhost.setState(HUNTING_PAC);
            }
            if (unlockedGhost.id() == ORANGE_GHOST && level.cruiseElroyState() < 0) {
                level.enableCruiseElroyState(true);
                Logger.trace("Cruise elroy mode re-enabled because {} exits house", unlockedGhost.name());
            }
        }
        level.ghosts().forEach(ghost -> ghost.update(level));
    }

    @Override
    public GameState doHuntingStep() {
        updateFood();
        updateGhosts();
        updatePac();
        updateBonus();
        updateHuntingTimer();
        level.blinking().tick();

        // what next?
        if (level.world().uneatenFoodCount() == 0) {
            return GameState.LEVEL_COMPLETE;
        }
        var killers = level.ghosts(HUNTING_PAC).filter(level.pac()::sameTile).toList();
        if (!killers.isEmpty() && !GameController.it().isPacImmune()) {
            eventLog().pacDied = true;
            return GameState.PACMAN_DYING;
        }
        var prey = level.ghosts(FRIGHTENED).filter(level.pac()::sameTile).toList();
        if (!prey.isEmpty()) {
            killGhosts(prey);
            return GameState.GHOST_DYING;
        }
        return GameState.HUNTING;
    }


    @Override
    public void killGhosts(List<Ghost> prey) {
        if (!prey.isEmpty()) {
            prey.forEach(this::killGhost);
            if (numGhostsKilledInLevel == 16) {
                int points = GameModel.POINTS_ALL_GHOSTS_KILLED_IN_LEVEL;
                scorePoints(points);
                Logger.info("Scored {} points for killing all ghosts at level {}", points, level.levelNumber);
            }
        }
    }

    private void killGhost(Ghost ghost) {
        byte[] multiple = { 2, 4, 8, 16 };
        int killedSoFar = level.pac().victims().size();
        int points = 100 * multiple[killedSoFar];
        scorePoints(points);
        ghost.eaten(killedSoFar);
        level.pac().victims().add(ghost);
        eventLog().killedGhosts.add(ghost);
        numGhostsKilledInLevel += 1;
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    @Override
    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    @Override
    public short initialLives() {
        return initialLives;
    }

    @Override
    public void setInitialLives(int lives) {
        initialLives = (short) lives;
    }

    @Override
    public int lives() {
        return lives;
    }

    @Override
    public void addLives(int lives) {
        this.lives += (short) lives;
    }

    @Override
    public void loseLife() {
        if (lives == 0) {
            throw new IllegalArgumentException("No life left to loose :-(");
        }
        --lives;
    }

    @Override
    public List<Byte> levelCounter() {
        return levelCounter;
    }

    @Override
    public void addSymbolToLevelCounter(byte symbol) {
        levelCounter.add(symbol);
        if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
            levelCounter.removeFirst();
        }
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public void scorePoints(int levelNumber, int points) {
        int oldScore = score.points();
        int newScore = oldScore + points;
        score.setPoints(newScore);
        if (newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(levelNumber);
            highScore.setDate(LocalDate.now());
        }
        if (oldScore < EXTRA_LIFE_SCORE && newScore >= EXTRA_LIFE_SCORE) {
            addLives(1);
            publishGameEvent(GameEventType.EXTRA_LIFE_WON);
        }
    }

    @Override
    public Score highScore() {
        return highScore;
    }

    @Override
    public void updateHighScore() {
        var oldHighScore = new Score();
        oldHighScore.loadFromFile(highScoreFile());
        if (highScore.points() > oldHighScore.points()) {
            highScore.saveToFile(highScoreFile(), String.format("%s High Score", name()));
        }
    }

    // Game Event Support

    final List<GameEventListener> gameEventListeners = new ArrayList<>();

    @Override
    public void addGameEventListener(GameEventListener listener) {
        checkNotNull(listener);
        gameEventListeners.add(listener);
    }

    @Override
    public void publishGameEvent(GameEventType type) {
        publishGameEvent(new GameEvent(type, this));
    }

    @Override
    public void publishGameEvent(GameEventType type, Vector2i tile) {
        publishGameEvent(new GameEvent(type, this, tile));
    }

    @Override
    public void publishGameEvent(GameEvent event) {
        Logger.trace("Publish game event: {}", event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
    }
}