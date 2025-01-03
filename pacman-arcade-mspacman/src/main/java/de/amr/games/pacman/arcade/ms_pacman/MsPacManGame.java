/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
public class MsPacManGame extends GameModel {

    private static final String MAP_PATTERN = "maps/mspacman_%d.world";

    private final List<WorldMap> maps = new ArrayList<>();

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

    /**
     * <p>In Ms. Pac-Man, there are 4 maps and 6 color schemes.
     * </p>
     * <ul>
     * <li>Levels 1-2: (1, 1): pink wall fill, white dots
     * <li>Levels 3-5: (2, 2)): light blue wall fill, yellow dots
     * <li>Levels 6-9: (3, 3): orange wall fill, red dots
     * <li>Levels 10-13: (4, 4): blue wall fill, white dots
     * </ul>
     * For level 14 and later, (map, color_scheme) alternates every 4th level between (3, 5) and (4, 6):
     * <ul>
     * <li>(3, 5): pink wall fill, cyan dots
     * <li>(4, 6): orange wall fill, white dots
     * </ul>
     * <p>
     */
    private static WorldMap configureWorldMap(List<WorldMap> maps, int levelNumber) {
        final int mapNumber = switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
        int colorMapIndex = levelNumber < 14 ? mapNumber - 1 : mapNumber + 2 - 1;
        WorldMap worldMap = new WorldMap(maps.get(mapNumber - 1));
        worldMap.setConfigValue("mapNumber", mapNumber);
        worldMap.setConfigValue("colorMapIndex", colorMapIndex);
        return worldMap;
    }

    private static int intermissionNumberAfterLevel(int number) {
        return switch (number) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        };
    }

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

    private static final byte HOUSE_X = 10, HOUSE_Y = 15;

    // To assure that the demo level runs at least 20 seconds:
    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    private static final byte PELLET_VALUE = 10;
    private static final byte ENERGIZER_VALUE = 50;

    private static final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

    private final Steering autopilot = new RuleBasedPacSteering(this);
    private final Steering demoLevelSteering = new RuleBasedPacSteering(this);

    private byte cruiseElroy; //TODO is this existing in Ms. Pac-Man at all?

    public MsPacManGame(File userDir) {
        for (int num = 1; num <= 4; ++num) {
            URL url = getClass().getResource(MAP_PATTERN.formatted(num));
            if (url != null) {
                try {
                    WorldMap worldMap = new WorldMap(url);
                    maps.add(worldMap);
                } catch (IOException x) {
                    Logger.error(x);
                    Logger.error("Could not create world map, url={}", url);
                }
            } else {
                Logger.error("Could not load world map, pattern={}, number={}", MAP_PATTERN, num);
            }
        }
        Logger.info("{} maps loaded ({})", maps.size(), GameVariant.MS_PACMAN);

        initialLives = 3;
        simulateOverflowBug = true;

        scoreManager.setHighScoreFile(new File(userDir, "highscore-ms_pacman.xml"));
        scoreManager.setExtraLifeScores(10_000);

        huntingControl = new HuntingTimer() {
            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex] : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingControl.setOnPhaseChange(() -> level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseASAP));
    }

    @Override
    public void resetEverything() {
        resetForStartingNewGame();
    }

    @Override
    public void resetForStartingNewGame() {
        playing = false;
        lives = initialLives;
        level = null;
        demoLevel = false;
        cruiseElroy = 0;
        levelCounter().clear();
        scoreManager().loadHighScore();
        scoreManager.resetScore();
        gateKeeper.reset();
        huntingControl.reset();
    }

    @Override
    public void endGame() {
        GameController.it().coinControl().consumeCoin();
        scoreManager().updateHighScore();
        publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public boolean canStartNewGame() {
        return GameController.it().coinControl().hasCredit();
    }

    @Override
    public boolean continueOnGameOver() {
        return false;
    }

    @Override
    public boolean isOver() {
        return lives == 0;
    }

    @Override
    public long gameOverStateTicks() {
        return 150;
    }

    private LevelData levelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    private void createWorldAndPopulation(WorldMap worldMap) {
        GameWorld world = new GameWorld(worldMap);
        world.createArcadeHouse(HOUSE_X, HOUSE_Y);

        var pac = new Pac();
        pac.setName("Ms. Pac-Man");
        pac.setWorld(world);
        pac.reset();

        var ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
        Stream.of(ghosts).forEach(ghost -> {
            ghost.setWorld(world);
            ghost.setRevivalPosition(world.ghostPosition(ghost.id()));
            ghost.reset();
        });
        ghosts[RED_GHOST].setRevivalPosition(world.ghostPosition(PINK_GHOST)); // middle house position

        level.setWorld(world);
        level.setPac(pac);
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
        level.setIntermissionNumber(intermissionNumberAfterLevel(level.number));
        level.setNumFlashes(levelData(level.number).numFlashes());
        WorldMap worldMap = configureWorldMap(maps, level.number);
        createWorldAndPopulation(worldMap);
        level.pac().setAutopilot(autopilot);
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void configureDemoLevel() {
        configureNormalLevel();
        levelCounterEnabled = false;
        setDemoLevelBehavior();
        demoLevelSteering.init();
    }

    @Override
    public void setDemoLevelBehavior() {
        level.pac().setAutopilot(demoLevelSteering);
        level.pac().setUsingAutopilot(true);
        level.pac().setImmune(false);
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
        //TODO find better solution.
        // Ghost flashing animation has frame length 14 so one full flash takes 28 ticks
        return level != null ? level.numFlashes() * 28L : 0;
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
        if (isDemoLevel() && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man killing ignored, demo level running for {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
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

    @Override
    public void killGhost(Ghost ghost) {
        eventLog.killedGhosts.add(ghost);
        int killedSoFar = level.victims().size();
        int points = KILLED_GHOST_VALUES[killedSoFar];
        level.addKilledGhost(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(this, points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        if (level.killedGhostCount() == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_IN_LEVEL;
            scoreManager.scorePoints(this, extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, level.number);
        }
    }

    private void setCruiseElroyEnabled(boolean enabled) {
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
     * (TODO: what does "never" mean here? For the rest of the game?).
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
     * Bonus symbol enters the world at some tunnel entry, walks to the house entry, takes a tour around the
     * house and finally leaves the world through a tunnel on the opposite side of the world.
     * <p>
     * According to <a href="https://strategywiki.org/wiki/Ms._Pac-Man/Walkthrough">this</a> Wiki,
     * some maps have a fixed entry tile for the bonus.
     * TODO: Not sure if that's correct.
     *
     * <p>
     * Note: This is not the exact behavior from the original Arcade game that uses fruit paths.
     **/
    @Override
    public void activateNextBonus() {
        if (level.bonus().isPresent() && level.bonus().get().state() != Bonus.STATE_INACTIVE) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        level.advanceNextBonus();

        List<Portal> portals = level.world().portals().toList();
        if (portals.isEmpty()) {
            return; // should not happen
        }

        Vector2i entryTile, exitTile;
        boolean crossMazeLeftToRight;

        TileMap terrainMap = level.world().map().terrain();
        if (terrainMap.hasProperty("pos_bonus")) {
            // use entry tile stored in terrain map
            entryTile = terrainMap.getTileProperty("pos_bonus", null);
            if (entryTile.x() == 0) {
                // start tile is at left maze border
                exitTile = portals.get(RND.nextInt(portals.size())).rightTunnelEnd().plus(1, 0);
                crossMazeLeftToRight = true;
            } else {
                // start tile is at right maze border
                exitTile = portals.get(RND.nextInt(portals.size())).leftTunnelEnd().minus(1, 0);
                crossMazeLeftToRight = false;
            }
        }
        else { // choose random portals for entry and exit
            crossMazeLeftToRight = RND.nextBoolean();
            if (crossMazeLeftToRight) {
                entryTile = portals.get(RND.nextInt(portals.size())).leftTunnelEnd();
                exitTile  = portals.get(RND.nextInt(portals.size())).rightTunnelEnd().plus(1, 0);
            } else {
                entryTile = portals.get(RND.nextInt(portals.size())).rightTunnelEnd();
                exitTile = portals.get(RND.nextInt(portals.size())).leftTunnelEnd().minus(1, 0);
            }
        }

        Vector2i houseEntry = tileAt(level.world().houseEntryPosition());
        Vector2i behindHouse = houseEntry.plus(0, level.world().houseSize().y() + 1);
        List<NavPoint> route = Stream.of(entryTile, houseEntry, behindHouse, houseEntry, exitTile).map(NavPoint::np).toList();

        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        var movingBonus = new MovingBonus(level.world(), symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        movingBonus.setEdible(TickTimer.INDEFINITE);
        movingBonus.setRoute(route, crossMazeLeftToRight);
        movingBonus.setBaseSpeed(1.25f);
        Logger.info("Moving bonus created, route: {} ({})", route, crossMazeLeftToRight ? "left to right" : "right to left");

        level.setBonus(movingBonus);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, movingBonus.actor().tile());
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
            boolean chasing = huntingControl.phaseType() == HuntingTimer.PhaseType.CHASING
                || ghost.id() == RED_GHOST && cruiseElroy > 0;
            Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
            ghost.followTarget(targetTile, ghostAttackSpeed(ghost));
        }
    }
}