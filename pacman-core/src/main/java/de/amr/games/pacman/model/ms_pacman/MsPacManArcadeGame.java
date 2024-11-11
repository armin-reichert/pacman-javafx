/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.LevelData;
import de.amr.games.pacman.model.Portal;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Ms. Pac-Man Arcade game.
 *
 * <p>There are however some differences to the original.
 * <ul>
 *     <li>Attract mode is just a random hunting for at least 20 seconds.</li>
 *     <li>Timing of hunting phases unclear, just took all the information I had</li>
 *     <li>Bonus does not follow original "fruit paths" but randomly selects a portal to
 *     enter the maze, turns around the house and leaves the maze at a random portal on the other side</li>
 * </ul>
 * </p>
 *
 * @author Armin Reichert
 */
public class MsPacManArcadeGame extends GameModel {

    // These are the Pac-Man level data as given in the Pac-Man dossier.
    // I have no information that Ms. Pac-Man uses different data.
    private static final byte[][] LEVEL_DATA = {
        /* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
        /* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
        /* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
        /* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
        /* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
        /* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
        /* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
        /* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
        /* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
        /*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
        /*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
        /*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
        /*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
        /*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
        /*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
        /*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
        /*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
        /*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
    };

    /*
     * These numbers are from a conversation with @damselindis on Reddit.
     *
     * I am not sure if they are correct.
     *
     * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
     * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
     */
    private static final int[] HUNTING_TICKS_LEVEL_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    // Ms. Pac-Man game specific
    public static final String ANIM_MR_PACMAN_MUNCHING = "pacman_munching";

    private static final byte HOUSE_X = 10, HOUSE_Y = 15;

    // To assure that the demo level runs at least 20 seconds:
    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    private static final byte PELLET_VALUE = 10;
    private static final byte ENERGIZER_VALUE = 50;

    private static final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

    private final MapConfigurationManager mapConfigMgr = new MapConfigurationManager();
    private final Steering autopilot = new RuleBasedPacSteering(this);
    private final Steering demoLevelSteering = new RuleBasedPacSteering(this);

    private byte cruiseElroy; //TODO is this existing in Ms. Pac-Man at all?

    public MsPacManArcadeGame(File userDir) {
        super(userDir);

        initialLives = 3;
        simulateOverflowBug = true;

        scoreManager.setHighScoreFile(new File(userDir, "highscore-ms_pacman.xml"));
        scoreManager.setExtraLifeScores(10_000);

        huntingControl = new HuntingControl() {
            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex] : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingControl.setOnPhaseChange(() -> level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseASAP));
    }

    public Optional<LevelData> currentLevelData() {
        return level != null ? Optional.of(levelData(level.number)): Optional.empty();
    }

    @Override
    public void reset() {
        lives = initialLives;
        level = null;
        playing = false;
        cruiseElroy = 0;
        levelCounter().clear();
        scoreManager().loadHighScore();
        scoreManager.resetScore();
    }

    @Override
    public boolean canStartNewGame() {
        return GameController.it().coinControl().hasCredit();
    }

    @Override
    public boolean isOver() {
        return lives == 0;
    }

    @Override
    public long gameOverStateTicks() {
        return 150;
    }

    protected LevelData levelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    @Override
    public int intermissionNumberAfterLevel() {
        return switch (level.number) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        };
    }

    protected void createWorldAndPopulation(WorldMap map) {
        level.setWorld(new GameWorld(map));
        level.world().createArcadeHouse(HOUSE_X, HOUSE_Y);

        var pac = new Pac();
        pac.setName("Ms. Pac-Man");
        pac.setWorld(level.world());
        pac.reset();
        level.setPac(pac);

        var ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
        Stream.of(ghosts).forEach(ghost -> {
            ghost.setWorld(level.world());
            ghost.reset();
            ghost.setRevivalPosition(level.world().ghostPosition(ghost.id()));
        });
        ghosts[RED_GHOST].setRevivalPosition(level.world().ghostPosition(PINK_GHOST)); // middle house position
        level.setGhosts(ghosts);

        level.setBonusSymbol(0, computeBonusSymbol());
        level.setBonusSymbol(1, computeBonusSymbol());
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        level.pac().setBaseSpeed(1.25f);
        level.ghosts().forEach(ghost -> ghost.setBaseSpeed(1.25f));
    }

    @Override
    public void configureNormalLevel() {
        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounterEnabled = level.number < 8;
        level.setMapConfig(mapConfigMgr.getMapConfig(level.number));
        createWorldAndPopulation(level.mapConfig().worldMap());
        level.pac().setAutopilot(autopilot);
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void configureDemoLevel() {
        levelCounterEnabled = false;
        level.setMapConfig(mapConfigMgr.getMapConfig(level.number));
        createWorldAndPopulation(level.mapConfig().worldMap());
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
        demoLevelSteering.init();
        setDemoLevelBehavior();
    }

    @Override
    public void setDemoLevelBehavior() {
        level.pac().setAutopilot(demoLevelSteering);
        level.pac().setUsingAutopilot(true);
        level.pac().setImmune(false);
    }

    @Override
    public int numFlashes() {
        return level != null ? levelData(level.number).numFlashes() : 0;
    }

    @Override
    public float pacNormalSpeed() {
        if (level == null) {
            return 0;
        }
        byte percentage = levelData(level.number).pacSpeedPercentage();
        if (percentage == 0) {
            percentage = 100;
        }
        return percentage * 0.01f * level.pac().baseSpeed();
    }

    @Override
    public float pacPowerSpeed() {
        if (level == null) {
            return 0;
        }
        byte percentage = levelData(level.number).pacSpeedPoweredPercentage();
        if (percentage == 0) {
            percentage = 100;
        }
        return percentage * 0.01f * level.pac().baseSpeed();
    }

    @Override
    public long pacPowerTicks() {
        return level != null ? 60 * levelData(level.number).pacPowerSeconds() : 0;
    }

    @Override
    public long pacPowerFadingTicks() {
        // ghost flashing animation has frame length 14 so one full flash takes 28 ticks
        return numFlashes() * 28L;
    }

    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        if (level.world().isTunnel(ghost.tile()) && level.number <= 3) {
            return ghostTunnelSpeed(ghost);
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 1) {
            return levelData(level.number).elroy1SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 2) {
            return levelData(level.number).elroy2SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        return levelData(level.number).ghostSpeedPercentage() * 0.01f * ghost.baseSpeed();
    }

    @Override
    public float ghostSpeedInsideHouse(Ghost ghost) {
        return 0.5f;
    }

    @Override
    public float ghostSpeedReturningToHouse(Ghost ghost) {
        return 2;
    }

    @Override
    public float ghostFrightenedSpeed(Ghost ghost) {
        return level !=null
            ? levelData(level.number).ghostSpeedFrightenedPercentage() * 0.01f * ghost.baseSpeed()
            : 0;
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        return level !=null
            ? levelData(level.number).ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed()
            : 0;
    }

    @Override
    public boolean isPacManKillingIgnored() {
        float levelRunningSeconds = (System.currentTimeMillis() - level.startTime()) / 1000f;
        if (level.isDemoLevel() && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man killing ignored, demo level running for {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
    }

    @Override
    public void endGame() {
        GameController.it().coinControl().consumeCoin();
        scoreManager().updateHighScore(GameController.it().currentGameVariant());
        scoreManager.resetScore();
    }

    @Override
    protected void onPelletOrEnergizerEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        level.pac().setRestingTicks(energizer ? 3 : 1);
        if (uneatenFoodCount == levelData(level.number).elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (uneatenFoodCount == levelData(level.number).elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
        if (energizer) {
            processEatenEnergizer();
            scoreManager().scorePoints(this, ENERGIZER_VALUE);
            Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        } else {
            scoreManager.scorePoints(this, PELLET_VALUE);
        }
        gateKeeper.registerFoodEaten(level);
        if (isBonusReached()) {
            activateNextBonus();
            eventLog.bonusIndex = level.nextBonusIndex();
        }
    }

    @Override
    public void onPacKilled() {
        huntingControl.stop();
        Logger.info("Hunting timer stopped");
        level.powerTimer().stop();
        level.powerTimer().reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true);
        setCruiseElroyEnabled(false);
        level.pac().die();
    }

    protected void setCruiseElroyEnabled(boolean enabled) {
        if (enabled && cruiseElroy < 0 || !enabled && cruiseElroy > 0) {
            cruiseElroy = (byte) -cruiseElroy;
        }
    }

    @Override
    protected void onGhostReleased(Ghost ghost) {
        if (ghost.id() == ORANGE_GHOST && cruiseElroy < 0) {
            Logger.trace("Re-enable cruise elroy mode because {} exits house:", ghost.name());
            setCruiseElroyEnabled(true);
        }
    }

    @Override
    public boolean isBonusReached() {
        return level.world().eatenFoodCount() == 64 || level.world().eatenFoodCount() == 176;
    }

    /**
     * <p>Got this information from
     * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>:
     * </p>
     * <p style="font-style:italic">
     * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
     * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
     * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
     * spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
     * (TODO: what does never mean here? For the rest of the game?).
     * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
     * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
     * the following table:
     *
     * <table>
     * <tr align="left">
     *   <th>Cherry</th><th>Strawberry</th><th>Peach</th><th>Pretzel</th><th>Apple</th><th>Pear&nbsp;</th><th>Banana</th>
     * </tr>
     * <tr align="right">
     *     <td>5/32</td><td>5/32</td><td>5/32</td><td>5/32</td><td>4/32</td><td>4/32</td><td>4/32</td>
     * </tr>
     * </table>
     * </p>
     */
    @Override
    public byte computeBonusSymbol() {
        if (level.number <= 7) {
            return (byte) (level.number - 1);
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

    /**
     * Bonus symbol enters the world at a random portal, walks to the house entry, takes a tour around the
     * house and finally leaves the world through a random portal on the opposite side of the world.
     * <p>
     * Note: This is not the exact behavior from the original Arcade game.
     **/
    @Override
    public void activateNextBonus() {
        if (level.bonus().isPresent() && level.bonus().get().state() != Bonus.STATE_INACTIVE) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        level.advanceNextBonus();

        boolean leftToRight = RND.nextBoolean();
        Vector2i houseEntry = tileAt(level.world().houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, level.world().houseSize().y() + 1);
        List<Portal> portals = level.world().portals().toList();
        if (portals.isEmpty()) {
            return; // should not happen but...
        }
        Portal entryPortal = portals.get(RND.nextInt(portals.size()));
        Portal exitPortal  = portals.get(RND.nextInt(portals.size()));
        List<NavPoint> route = Stream.of(
            leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(NavPoint::np).toList();

        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        var movingBonus = new MovingBonus(level.world(), symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        movingBonus.setRoute(route, leftToRight);
        movingBonus.setBaseSpeed(1.25f);
        Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        level.setBonus(movingBonus);
        movingBonus.setEdible(TickTimer.INDEFINITE);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, movingBonus.entity().tile());
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    private void ghostHuntingBehaviour(Ghost ghost) {
        if (huntingControl.phaseIndex() == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(ghostAttackSpeed(ghost));
        } else {
            boolean chasing = huntingControl.phaseType() == HuntingControl.PhaseType.CHASING
                || ghost.id() == RED_GHOST && cruiseElroy > 0;
            Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
            ghost.followTarget(targetTile, ghostAttackSpeed(ghost));
        }
    }
}