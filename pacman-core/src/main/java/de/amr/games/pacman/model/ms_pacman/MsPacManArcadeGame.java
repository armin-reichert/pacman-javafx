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
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        /* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0},
        /* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1},
        /* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0},
        /* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0},
        /* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2},
        /* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0},
        /* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
        /* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
        /* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3},
        /*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0},
        /*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0},
        /*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0},
        /*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3},
        /*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0},
        /*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3},
        /*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
        /*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
        /*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
    };

    // Ms. Pac-Man game specific
    public static final String ANIM_MR_PACMAN_MUNCHING = "pacman_munching";

    private static final byte HOUSE_X = 10, HOUSE_Y = 15;

    // I use a randomly running demo level, to assure it runs at least 20 seconds:
    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    private static final String MAP_PATTERN = "/de/amr/games/pacman/maps/mspacman/mspacman_%d.world";

    public static final List<Map<String, String>> MAP_COLOR_SCHEMES = List.of(
        Map.of("fill", "FFB7AE", "stroke", "FF0000", "door", "FCB5FF", "pellet", "DEDEFF"),
        Map.of("fill", "47B7FF", "stroke", "DEDEFF", "door", "FCB5FF", "pellet", "FFFF00"),
        Map.of("fill", "DE9751", "stroke", "DEDEFF", "door", "FCB5FF", "pellet", "FF0000"),
        Map.of("fill", "2121FF", "stroke", "FFB751", "door", "FCB5FF", "pellet", "DEDEFF"),
        Map.of("fill", "FFB7FF", "stroke", "FFFF00", "door", "FCB5FF", "pellet", "00FFFF"),
        Map.of("fill", "FFB7AE", "stroke", "FF0000", "door", "FCB5FF", "pellet", "DEDEFF")
    );

    private static final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

    private final List<WorldMap> maps = new ArrayList<>();
    private byte cruiseElroy; //TODO is this existing in Ms. Pac-Man at all?

    public MsPacManArcadeGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;

        scoreManager.setHighScoreFile(new File(userDir, "highscore-ms_pacman.xml"));
        scoreManager.setExtraLifeScore(10_000);

        for (int number = 1; number <= 4; ++number) {
            maps.add(new WorldMap(getClass().getResource(MAP_PATTERN.formatted(number))));
        }
        Logger.info("{} maps loaded ({})", maps.size(), variant());

        huntingControl = new HuntingControl("HuntingControl-" + getClass().getSimpleName()) {
            /*
             * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
             *
             * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
             * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
             */
            private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
            private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingControl.setOnPhaseChange(() -> ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible));
    }

    public Optional<LevelData> currentLevelData() {
        return currentLevelNumber > 0 ? Optional.of(levelData(currentLevelNumber)): Optional.empty();
    }

    @Override
    public void reset() {
        super.reset();
        cruiseElroy = 0;
    }

    @Override
    protected void initScore(int levelNumber) {
        scoreManager.setScoreEnabled(levelNumber > 0 && !isDemoLevel());
        scoreManager.setHighScoreEnabled(levelNumber > 0 && !isDemoLevel());
    }

    @Override
    public boolean canStartNewGame() {
        return GameController.it().coinControl().hasCredit();
    }

    @Override
    public long gameOverStateTicks() {
        return 150;
    }

    protected LevelData levelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

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
    private void selectMap(int levelNumber) {
        final int mapNumber = switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
        final int colorSchemeNumber = levelNumber < 14 ? mapNumber : mapNumber + 2;

        currentMapNumber = mapNumber;
        currentMap = maps.get(mapNumber - 1);
        currentMapColorScheme = MAP_COLOR_SCHEMES.get(colorSchemeNumber - 1);
    }

    @Override
    public int intermissionNumberAfterLevel() {
        return currentLevelNumber > 0 ? levelData(currentLevelNumber).intermissionNumber() : 0;
    }

    @Override
    protected Pac createPac() {
        Pac msPacMan = new Pac();
        msPacMan.setName("Ms. Pac-Man");
        return msPacMan;
    }

    @Override
    protected Ghost[] createGhosts() {
        return new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        pac.setBaseSpeed(1.25f);
        ghosts().forEach(ghost -> ghost.setBaseSpeed(1.25f));
    }

    @Override
    public void buildLevel(int levelNumber) {
        this.currentLevelNumber = levelNumber;
        selectMap(currentLevelNumber);
        createWorldAndPopulation(currentMap);
        pac.setName("Ms. Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void buildDemoLevel() {
        currentLevelNumber = 1;
        selectMap(currentLevelNumber);
        createWorldAndPopulation(currentMap);
        pac.setName("Ms. Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(true);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    protected GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        world.createArcadeHouse(HOUSE_X, HOUSE_Y);
        //TODO: store in map files
        map.terrain().setProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, TileMap.formatTile(world.houseTopLeftTile()));
        return world;
    }

    @Override
    public int numFlashes() {
        return currentLevelNumber > 0 ? levelData(currentLevelNumber).numFlashes() : 0;
    }

    @Override
    public float pacNormalSpeed() {
        if (currentLevelNumber == 0) {
            return 0;
        }
        byte percentage = levelData(currentLevelNumber).pacSpeedPercentage();
        if (percentage == 0) {
            percentage = 100;
        }
        return percentage * 0.01f * pac.baseSpeed();
    }

    @Override
    public float pacPowerSpeed() {
        if (currentLevelNumber == 0) {
            return 0;
        }
        byte percentage = levelData(currentLevelNumber).pacSpeedPoweredPercentage();
        if (percentage == 0) {
            percentage = 100;
        }
        return percentage * 0.01f * pac.baseSpeed();
    }

    @Override
    public long pacPowerTicks() {
        return currentLevelNumber > 0 ? 60 * levelData(currentLevelNumber).pacPowerSeconds() : 0;
    }

    @Override
    public long pacPowerFadingTicks() {
        // ghost flashing animation has frame length 14 so one full flash takes 28 ticks
        return numFlashes() * 28L;
    }

    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        if (world.isTunnel(ghost.tile()) && currentLevelNumber <= 3) {
            return ghostTunnelSpeed(ghost);
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 1) {
            return levelData(currentLevelNumber).elroy1SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 2) {
            return levelData(currentLevelNumber).elroy2SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        return levelData(currentLevelNumber).ghostSpeedPercentage() * 0.01f * ghost.baseSpeed();
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
        return currentLevelNumber > 0
            ? levelData(currentLevelNumber).ghostSpeedFrightenedPercentage() * 0.01f * ghost.baseSpeed()
            : 0;
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        return currentLevelNumber > 0
            ? levelData(currentLevelNumber).ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed()
            : 0;
    }

    /** In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
     * (also inside a level) whenever a bonus score is reached. At least that's what I was told.
     */
    @Override
    protected boolean isLevelCounterEnabled() {
        return currentLevelNumber < 8 && !demoLevel;
    }

    @Override
    public boolean isPacManKillingIgnoredInDemoLevel() {
        float levelRunningSeconds = (System.currentTimeMillis() - levelStartTime) / 1000f;
        if (demoLevel && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man killing ignored, demo level running for {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
    }

    @Override
    public void onGameEnded() {
        GameController.it().coinControl().consumeCoin();
    }

    @Override
    protected void onPelletOrEnergizerEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        pac.setRestingTicks(energizer ? 3 : 1);
        if (uneatenFoodCount == levelData(currentLevelNumber).elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (uneatenFoodCount == levelData(currentLevelNumber).elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
        if (energizer) {
            processEatenEnergizer();
            scoreManager().scorePoints(energizerValue());
            Logger.info("Scored {} points for eating energizer", energizerValue());
        } else {
            scoreManager.scorePoints(pelletValue());
        }
        gateKeeper.registerFoodEaten();
        if (isBonusReached()) {
            activateNextBonus();
            eventLog.bonusIndex = nextBonusIndex;
        }
    }

    @Override
    public void onPacDying() {
        huntingControl.stop();
        Logger.info("Hunting timer stopped");
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true);
        setCruiseElroyEnabled(false);
        pac.die();
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
        return world.eatenFoodCount() == 64 || world.eatenFoodCount() == 176;
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
        if (currentLevelNumber <= 7) {
            return (byte) (currentLevelNumber - 1);
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
        if (bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        nextBonusIndex += 1;

        boolean leftToRight = RND.nextBoolean();
        Vector2i houseEntry = tileAt(world.houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, world.houseSize().y() + 1);
        List<Portal> portals = world.portals().toList();
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

        byte symbol = bonusSymbols[nextBonusIndex];
        var movingBonus = new MovingBonus(world, symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        movingBonus.setRoute(route, leftToRight);
        movingBonus.setBaseSpeed(1.25f);
        Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        bonus = movingBonus;
        bonus.setEdible(TickTimer.INDEFINITE);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
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