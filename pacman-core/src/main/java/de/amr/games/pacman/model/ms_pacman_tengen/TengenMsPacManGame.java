/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Ms. Pac-Man Tengen game.
 *
 * @author Armin Reichert
 */
public class TengenMsPacManGame extends GameModel {

    // see https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PlayerAndGhostSpeeds.asm
    // "the way the coordinates are handled are 1px = 32 subunits, so if speed is 4, the entity will move 1 pixel in 8 frames."
    // Value 1 therefore corresponds to 1/32 pixels/tick

    static final byte BOOSTER_INCREMENT = 16;

    static float pacBaseSpeed(int levelNumber) {
        return 1/32f * switch (levelNumber) {
            case 1,2,3,4 -> 32;
            case 5,6,7,8 -> 36;
            case 9,10,11,12 -> 36;
            case 13,14,15,16 -> 40;
            case 17,18,19,20 -> 39;
            case 21,22,23,24 -> 38;
            case 25,26,27,28 -> 37;
            case 29,30,31,32 -> 36;
            // only accessible with cheats:
            case 33,34,35,36 -> 35;
            case 37,38,39,40 -> 34;
            case 41,42,43,44 -> 32;
            case 45,46,47,48 -> 31;
            case 49,50,51,52 -> 30;
            default -> 28;
        };
    };

    static float pacBaseSpeedDiff(Difficulty difficulty) {
        return 1/32f * switch (difficulty) { case EASY -> -4; case NORMAL -> 0; case HARD -> 12; case CRAZY -> 24; };
    }

    static float ghostBaseSpeed(int levelNumber) {
        return 1/32f * switch (levelNumber) {
            case 1,2,3,4 -> 24;
            case 5  -> 32;
            case 6  -> 33;
            case 7  -> 34;
            case 8  -> 35;
            case 9  -> 36;
            case 10 -> 37;
            case 11 -> 38;
            case 12 -> 39;
            default -> 40;
        };
    }

    static float ghostSpeedDiff(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> -8; case NORMAL -> 0; case HARD -> 16; case CRAZY -> 32;
        } / 32f;
    }

    // Ms. Pac-Man (Tengen) game specific animation IDs
    public static final String ANIM_PAC_MUNCHING_BOOSTER = "munching_booster";
    public static final String ANIM_PAC_HUSBAND_MUNCHING_BOOSTER = "husband_munching_booster";

    //TODO Clarify map order/colors in eacg category
    static final int ARCADE_MAP_COUNT = 9;
    static final int NON_ARCADE_MAP_COUNT = 37;
    static final int MAP_COUNT = ARCADE_MAP_COUNT + NON_ARCADE_MAP_COUNT;

    static final String MAPS_ROOT = "/de/amr/games/pacman/maps/tengen/";
    static final String ARCADE_MAP_PATTERN      = MAPS_ROOT + "arcade/map%02d.world";
    static final String NON_ARCADE_MAP_PATTERN  = MAPS_ROOT + "non_arcade/map%02d.world";

    /**
     * Got this sequence from YouTube video (https://www.youtube.com/watch?v=cD0oGudVpbw).
     */
    // TODO: need real data!
    private static List<WorldMap> miniMaps(List<WorldMap> nonArcadeMaps) {
        var miniMaps = new ArrayList<WorldMap>();

        // 1-5
        addMap(34, nonArcadeMaps, miniMaps, MapColoring.PINK_DARKRED);
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.LIGHTBLUE_WHITE_YELLOW);
        addMap(34, nonArcadeMaps, miniMaps, MapColoring.ORANGE_WHITE);
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.BLUE_YELLOW);
        addMap(36, nonArcadeMaps, miniMaps, MapColoring.PINK_YELLOW);

        // 6-10
        addMap(34, nonArcadeMaps, miniMaps, MapColoring.PINK_DARKRED);
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.BROWN2_WHITE);
        addMap(36, nonArcadeMaps, miniMaps, MapColoring.VIOLET_WHITE_YELLOW);
        addMap(30, nonArcadeMaps, miniMaps, MapColoring.BLACK_WHITE_YELLOW);
        addMap(34, nonArcadeMaps, miniMaps, MapColoring.BLACK_DARKBLUE);

        // 11-15
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.VIOLET_PINK);
        addMap(36, nonArcadeMaps, miniMaps, MapColoring.RED_WHITE);
        addMap(30, nonArcadeMaps, miniMaps, MapColoring.GREEN_WHITE_WHITE);
        addMap(34, nonArcadeMaps, miniMaps, MapColoring.YELLOW_WHITE_GREEN);
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.GREEN_WHITE_YELLOW);

        // 16-20
        addMap(36, nonArcadeMaps, miniMaps, MapColoring.KHAKI_WHITE);
        addMap(30, nonArcadeMaps, miniMaps, MapColoring.PINK_WHITE);
        addMap(16, nonArcadeMaps, miniMaps, MapColoring.BLUE_WHITE_YELLOW);
        addMap(16, nonArcadeMaps, miniMaps, MapColoring.BROWN_WHITE);
        addMap(30, nonArcadeMaps, miniMaps, MapColoring.RED_PINK);

        // 21-25
        addMap(36, nonArcadeMaps, miniMaps, MapColoring.BLACK_WHITE_GREEN);
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.GREEN_WHITE_WHITE);
        addMap(34, nonArcadeMaps, miniMaps, MapColoring.GREEN_WHITE_VIOLET);
        addMap(37, nonArcadeMaps, miniMaps, MapColoring.VIOLET_WHITE_GREEN);
        addMap(34, nonArcadeMaps, miniMaps, MapColoring.GRAY_WHITE_YELLOW);

        // 26-30
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.BLUE2_WHITE);
        addMap(36, nonArcadeMaps, miniMaps, MapColoring.VIOLET_WHITE);
        addMap(30, nonArcadeMaps, miniMaps, MapColoring.BROWN2_WHITE);
        addMap(28, nonArcadeMaps, miniMaps, MapColoring.BROWN2_WHITE);
        addMap(35, nonArcadeMaps, miniMaps, MapColoring.BLUE_YELLOW);

        // 31-32
        addMap(36, nonArcadeMaps, miniMaps, MapColoring.BLACK_WHITE_GREEN);
        addMap(37, nonArcadeMaps, miniMaps, MapColoring.RED_PINK);

        return miniMaps;
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private static List<WorldMap> bigMaps(List<WorldMap> nonArcadeMaps) {
        var bigMaps = new ArrayList<WorldMap>();

        // 1-5
        addMap(19, nonArcadeMaps, bigMaps, MapColoring.ROSE_RED);
        addMap(20, nonArcadeMaps, bigMaps, MapColoring.LIGHTBLUE_WHITE_YELLOW);
        addMap(21, nonArcadeMaps, bigMaps, MapColoring.ORANGE_WHITE);
        addMap(19, nonArcadeMaps, bigMaps, MapColoring.BLUE_WHITE_YELLOW);
        addMap(20, nonArcadeMaps, bigMaps, MapColoring.PINK_YELLOW);

        // 6-10
        addMap(21, nonArcadeMaps, bigMaps, MapColoring.ROSE_RED);
        addMap(22, nonArcadeMaps, bigMaps, MapColoring.ORANGE_WHITE);
        addMap(23, nonArcadeMaps, bigMaps, MapColoring.VIOLET_WHITE_YELLOW);
        addMap(17, nonArcadeMaps, bigMaps, MapColoring.BLACK_WHITE_YELLOW);
        addMap(10, nonArcadeMaps, bigMaps, MapColoring.BLACK_DARKBLUE);

        // 11-15
        addMap(23, nonArcadeMaps, bigMaps, MapColoring.PINK_ROSE);
        addMap(21, nonArcadeMaps, bigMaps, MapColoring.PINK_WHITE);
        addMap(22, nonArcadeMaps, bigMaps, MapColoring.GREEN_WHITE_WHITE);
        addMap(14, nonArcadeMaps, bigMaps, MapColoring.VIOLET_WHITE_GREEN);
        addMap(20, nonArcadeMaps, bigMaps, MapColoring.GREEN_WHITE_YELLOW);

        // 16-20
        addMap(19, nonArcadeMaps, bigMaps, MapColoring.KHAKI_WHITE);
        addMap(10, nonArcadeMaps, bigMaps, MapColoring.PINK_WHITE);
        addMap(17, nonArcadeMaps, bigMaps, MapColoring.BLUE_WHITE_YELLOW);
        addMap(10, nonArcadeMaps, bigMaps, MapColoring.BROWN_WHITE);
        addMap(19, nonArcadeMaps, bigMaps, MapColoring.PINK_ROSE);

        // 21-25
        addMap(26, nonArcadeMaps, bigMaps, MapColoring.BLACK_WHITE_GREEN);
        addMap(21, nonArcadeMaps, bigMaps, MapColoring.GREEN_WHITE_WHITE);
        addMap(22, nonArcadeMaps, bigMaps, MapColoring.GREEN_WHITE_VIOLET);
        addMap(23, nonArcadeMaps, bigMaps, MapColoring.VIOLET_WHITE_GREEN);
        addMap(14, nonArcadeMaps, bigMaps, MapColoring.GRAY_WHITE_YELLOW);

        // 26-30
        addMap(25, nonArcadeMaps, bigMaps, MapColoring.VIOLET_WHITE);
        addMap(14, nonArcadeMaps, bigMaps, MapColoring.PINK_WHITE);
        addMap(23, nonArcadeMaps, bigMaps, MapColoring.GREEN_WHITE_WHITE);
        addMap(26, nonArcadeMaps, bigMaps, MapColoring.LIGHTBLUE_WHITE_YELLOW);
        addMap(20, nonArcadeMaps, bigMaps, MapColoring.PINK_YELLOW);

        // 31-32
        addMap(25, nonArcadeMaps, bigMaps, MapColoring.PINK_ROSE);
        addMap(33, nonArcadeMaps, bigMaps, MapColoring.PINK_ROSE);

        return bigMaps;
    }

    private static void addMap(int mapNumber, List<WorldMap> sourceMaps, List<WorldMap> targetMaps, MapColoring mapColoring) {
        WorldMap map = new WorldMap(sourceMaps.get(mapNumber - 1));
        map.setColorScheme(mapColoring.colorScheme());
        targetMaps.add(map);
    }

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    //TODO: I have no idea about the timing in Tengen, use Ms. Pac-Man Arcade values for now
    private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    // Bonus symbols in Arcade, Mini and Big mazes
    public static final byte BONUS_CHERRY      = 0;
    public static final byte BONUS_STRAWBERRY  = 1;
    public static final byte BONUS_ORANGE      = 2;
    public static final byte BONUS_PRETZEL     = 3;
    public static final byte BONUS_APPLE       = 4;
    public static final byte BONUS_PEAR        = 5;
    public static final byte BONUS_BANANA      = 6;

    // Additional bonus symbols in Strange mazes
    public static final byte BONUS_MILK        = 7;
    public static final byte BONUS_ICE_CREAM   = 8;
    public static final byte BONUS_HIGH_HEELS  = 9;
    public static final byte BONUS_STAR        = 10;
    public static final byte BONUS_HAND        = 11;
    public static final byte BONUS_RING        = 12;
    public static final byte BONUS_FLOWER      = 13;

    // Bonus value = factor * 100
    static final byte[] BONUS_VALUE_FACTORS = new byte[14];
    static {
        BONUS_VALUE_FACTORS[BONUS_CHERRY]        = 1;
        BONUS_VALUE_FACTORS[BONUS_STRAWBERRY]    = 2;
        BONUS_VALUE_FACTORS[BONUS_ORANGE]        = 5;
        BONUS_VALUE_FACTORS[BONUS_PRETZEL]       = 7;
        BONUS_VALUE_FACTORS[BONUS_APPLE]         = 10;
        BONUS_VALUE_FACTORS[BONUS_PEAR]          = 20;
        BONUS_VALUE_FACTORS[BONUS_BANANA]        = 50; // !!
        BONUS_VALUE_FACTORS[BONUS_MILK]          = 30; // !!
        BONUS_VALUE_FACTORS[BONUS_ICE_CREAM]     = 40; // !!
        BONUS_VALUE_FACTORS[BONUS_HIGH_HEELS]    = 60;
        BONUS_VALUE_FACTORS[BONUS_STAR]          = 70;
        BONUS_VALUE_FACTORS[BONUS_HAND]          = 80;
        BONUS_VALUE_FACTORS[BONUS_RING]          = 90;
        BONUS_VALUE_FACTORS[BONUS_FLOWER]        = 100;
    }

    private static GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        Vector2i houseTopLeftTile = map.terrain().getTileProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, v2i(10, 15));
        world.createArcadeHouse(houseTopLeftTile.x(), houseTopLeftTile.y());
        return world;
    }

    private final List<WorldMap> arcadeMaps;
    private final List<WorldMap> strangeMaps;
    private final List<WorldMap> miniMaps;
    private final List<WorldMap> bigMaps;
    private List<WorldMap> currentMapList;

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private BoosterMode boosterMode;
    private boolean boosterActive;
    private byte startingLevel; // 1-7
    private int mapNumber;

    public TengenMsPacManGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;
        highScoreFile = new File(userDir, "highscore-ms_pacman_tengen.xml");

        arcadeMaps = readMaps(ARCADE_MAP_PATTERN, ARCADE_MAP_COUNT);
        List<WorldMap> nonArcadeMaps = readMaps(NON_ARCADE_MAP_PATTERN, NON_ARCADE_MAP_COUNT);
        strangeMaps = nonArcadeMaps.stream().filter(this::isStrangeMap).toList();
        miniMaps = miniMaps(nonArcadeMaps);
        bigMaps = bigMaps(nonArcadeMaps);

        setMapCategory(MapCategory.ARCADE);
        setPacBooster(BoosterMode.OFF);
        setDifficulty(Difficulty.NORMAL);
        setStartingLevel(1);
    }

    private boolean isStrangeMap(WorldMap worldMap) {
        int mapNumber = Integer.parseInt(worldMap.terrain().getProperty("map_number"));
        if (mapNumber == 16 || mapNumber == 28 || mapNumber == 30) {
            // 28 and 30 appear with different, random(?) color palette!?
            return true;
        }
        return worldMap.terrain().numRows() > 30; //TODO correct?
    }

    @Override
    public Optional<GameLevel> levelSettings(int levelNumber) {
        return Optional.empty();
    }

    public void setPacBooster(BoosterMode boosterMode) {
        this.boosterMode = boosterMode;
    }

    public BoosterMode pacBoosterMode() {
        return boosterMode;
    }

    public boolean isBoosterActive() {
        return boosterActive;
    }

    public MapCategory mapCategory() {
        return mapCategory;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = checkNotNull(mapCategory);
        currentMapList = switch (mapCategory) {
            case ARCADE -> arcadeMaps;
            case BIG -> bigMaps;
            case MINI -> miniMaps;
            case STRANGE -> strangeMaps;
        };
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public byte startingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        if (1 <= startingLevel && startingLevel <= 7) {
            this.startingLevel = (byte) startingLevel;
        } else {
            Logger.error("Starting level must be in interval 1-7");
        }
    }

    public int mapNumberByLevelNumber(int levelNumber) {
        int numMaps = currentMapList.size();
        return levelNumber <= numMaps ? levelNumber : Globals.randomInt(1, numMaps + 1);
    }

    private List<WorldMap> readMaps(String pattern, int maxNumber) {
        List<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= maxNumber; ++num) {
            String path = pattern.formatted(num);
            URL url = getClass().getResource(path);
            if (url != null) {
                WorldMap worldMap = new WorldMap(url);
                worldMap.terrain().setProperty("map_number", String.valueOf(num));
                maps.add(worldMap);
                Logger.info("World map #{} has been read from URL {}", num, url);
            }
        }
        return maps;
    }

    @Override
    public int currentMapNumber() {
        return mapNumber;
    }

    @Override
    public int mapCount() {
        return MAP_COUNT;
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    protected Pac createPac() {
        Pac msPacMan = new Pac();
        msPacMan.setName("Ms. Pac-Man");
        return msPacMan;
    }

    @Override
    protected void initPacAnimation() {
        pac.selectAnimation(isBoosterActive() ? ANIM_PAC_MUNCHING_BOOSTER : GameModel.ANIM_PAC_MUNCHING);
        pac.animations().ifPresent(Animations::resetCurrentAnimation);
    }

    @Override
    protected Ghost[] createGhosts() {
        return new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
    }

    @Override
    public void startNewGame() {
        reset();
        createLevel(startingLevel);
        if (startingLevel > 1) {
            levelCounter.clear();
            for (int number = 1; number <= startingLevel; ++number) {
                levelCounter.add((byte) (number - 1));
            }
        }
        setActorsBaseSpeed();
    }

    @Override
    protected void setActorsBaseSpeed() {
        pac.setBaseSpeed(pacBaseSpeed(startingLevel) + pacBaseSpeedDiff(difficulty));
        ghosts().forEach(ghost -> ghost.setBaseSpeed(ghostBaseSpeed(startingLevel) + ghostSpeedDiff(difficulty)));
        if (boosterMode == BoosterMode.ALWAYS_ON) {
            setPacBoosterActive(true);
        }
    }

    protected byte ghostSpeedPercentage(Ghost ghost) {
        if (world.isTunnel(ghost.tile())) {
            return 50; // TODO: check this
        }
        return 100;
    }

    public void setPacBoosterActive(boolean state) {
        if (boosterMode == BoosterMode.OFF) {
            Logger.error("Cannot activate booster when mode is {}", boosterMode);
            return;
        }
        if (boosterActive != state) {
            boosterActive = state;
            if (boosterActive) {
                pac.setBaseSpeed(pac.baseSpeed() + BOOSTER_INCREMENT / 32f);
                pac.selectAnimation(ANIM_PAC_MUNCHING_BOOSTER);
            } else {
                pac.setBaseSpeed(pac.baseSpeed() - BOOSTER_INCREMENT / 32f);
                pac.selectAnimation(ANIM_PAC_MUNCHING);
            }
            Logger.info("Ms. Pac-Man base speed set to {0.00} px/s", pac.baseSpeed());
        }
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = levelNumber;
        mapNumber = mapNumberByLevelNumber(levelNumber);
        var map = currentMapList.get(mapNumber - 1);
        setWorldAndCreatePopulation(createWorld(map));
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void buildDemoLevel() {
        levelNumber = 1;
        mapNumber = mapNumberByLevelNumber(levelNumber);
        setWorldAndCreatePopulation(createWorld(currentMapList.get(mapNumber - 1)));
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(true);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    protected boolean isScoreEnabledInDemoLevel() {
        return true;
    }

    /** In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
     * (also inside a level) whenever a bonus score is reached. At least that's what I was told.
     */
    @Override
    protected boolean isLevelCounterEnabled() {
        return levelNumber < 8 && !demoLevel;
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
    public boolean isBonusReached() {
        return world.eatenFoodCount() == 64 || world.eatenFoodCount() == 176;
    }

    @Override
    public byte computeBonusSymbol() {
        //TODO: I have no idea yet how Tengen does this
        byte maxBonus = mapCategory == MapCategory.STRANGE ? BONUS_FLOWER : BONUS_BANANA;
        if (levelNumber - 1 <= maxBonus) {
            return (byte) (levelNumber - 1);
        }
        return (byte) randomInt(0, maxBonus);
    }

    @Override
    public void activateNextBonus() {
        //TODO No idea how this behaves in Tengen
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
            return; // there should be no mazes without portal but who knows?
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
        movingBonus.setBaseSpeed(BASE_SPEED_IN_PX_PER_SEC * SEC_PER_TICK);
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
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
        byte speed = ghostSpeedPercentage(ghost);
        if (huntingPhaseIndex == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(speed);
        } else {
            boolean chase = isChasingPhase(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
            ghost.followTarget(chase ? chasingTarget(ghost) : scatterTarget(ghost), speed);
        }
    }
}