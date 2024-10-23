/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.MapColorScheme;
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

    // Ms. Pac-Man (Tengen) game specific animation IDs
    public static final String ANIM_PAC_MUNCHING_BOOSTER = "munching_booster";
    public static final String ANIM_PAC_HUSBAND_MUNCHING_BOOSTER = "husband_munching_booster";

    static final int ARCADE_MAP_COUNT = 4;
    static final int NON_ARCADE_MAP_COUNT = 37;
    static final String MAPS_ROOT = "/de/amr/games/pacman/maps/tengen/";
    static final String ARCADE_MAP_PATTERN      = MAPS_ROOT + "arcade%d.world";
    static final String NON_ARCADE_MAP_PATTERN  = MAPS_ROOT + "non_arcade/map%02d.world";

    private static float speedUnits(float units) {
        return units / 32f;
    }

    private static WorldMap coloredMap(List<WorldMap> maps, int mapNumber, MapColorScheme colorScheme) {
        WorldMap map = maps.get(mapNumber - 1);
        map.setColorScheme(colorScheme);
        return map;
    }

    private static boolean inRange(int n, int from, int to) {
        return from <= n && n <= to;
    }

    /* From https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PlayerAndGhostSpeeds.asm */

    private static float pacBoosterSpeedDelta() {
        return 0.5f;
    }

    private static float pacDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnits(switch (difficulty) {
            case EASY -> -4;
            case NORMAL -> 0;
            case HARD -> 12;
            case CRAZY -> 24;
        });
    }

    private static float ghostDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnits(switch (difficulty) {
            case EASY -> -8;
            case NORMAL -> 0;
            case HARD -> 16;
            case CRAZY -> 32;
        });
    }

    private static float ghostIDSpeedDelta(byte ghostID) {
        return speedUnits(switch (ghostID) {
            case RED_GHOST -> 3;
            case ORANGE_GHOST -> 2;
            case CYAN_GHOST -> 1;
            case PINK_GHOST -> 0;
            default -> throw GameException.illegalGhostID(ghostID);
        });
    }

    private static float pacBaseSpeedInLevel(int levelNumber) {
        int units = 0;
        if      (inRange(levelNumber, 1, 4))   { units = 0x20; }
        else if (inRange(levelNumber, 5, 12))  { units = 0x24; }
        else if (inRange(levelNumber, 13, 16)) { units = 0x28; }
        else if (inRange(levelNumber, 17, 20)) { units = 0x27; }
        else if (inRange(levelNumber, 21, 24)) { units = 0x26; }
        else if (inRange(levelNumber, 25, 28)) { units = 0x25; }
        else if (levelNumber >= 29)            { units = 0x24; }

        return speedUnits(units);
    }

    // TODO: do they all have the same base speed? Unclear from disassembly data.
    private static float ghostBaseSpeedInLevel(int levelNumber) {
        int units = 0x20; // default: 32
        if      (inRange(levelNumber, 1, 4))  { units = 0x18; }
        else if (inRange(levelNumber, 5, 12)) { units = 0x20 + (levelNumber - 5); } // 0x20-0x27
        else if (levelNumber >= 13)           { units = 0x28;}

        return speedUnits(units);
    }

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

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    //TODO: I have no idea about the timing in Tengen, use Ms. Pac-Man Arcade values for now
    private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    private final List<WorldMap> arcadeMaps;
    private final List<WorldMap> nonArcadeMaps;

    private final List<WorldMap> strangeLevels;

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private BoosterMode boosterMode;
    private boolean boosterActive;
    private byte startingLevel; // 1-7
    private int mapNumber;
    private boolean canStartGame;

    private LevelData currentLevelData;

    public TengenMsPacManGame(GameVariant gameVariant, File userDir) {
        super(gameVariant);
        this.userDir = userDir;
        initialLives = 3;
        scoreManager.setHighScoreFile(new File(userDir, "highscore-ms_pacman_tengen.xml"));
        scoreManager.setExtraLifeScore(10_000);

        arcadeMaps = readMaps(ARCADE_MAP_PATTERN, ARCADE_MAP_COUNT);
        nonArcadeMaps = readMaps(NON_ARCADE_MAP_PATTERN, NON_ARCADE_MAP_COUNT);

        // TODO refac
        strangeLevels = nonArcadeMaps.stream().filter(this::isStrangeMap).toList();
        for (WorldMap map : strangeLevels) {
            int mapNumber = Integer.parseInt(map.terrain().getProperty("map_number"));
            Logger.info("Strange map: {}", mapNumber);
        }

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

    // only for info panel in dashboard
    public Optional<LevelData> currentLevelData() {
        return Optional.ofNullable(currentLevelData);
    }

    @Override
    public boolean canStartNewGame() {
        return canStartGame;
    }

    public void setCanStartGame(boolean canStartGame) {
        this.canStartGame = canStartGame;
    }

    @Override
    protected void initScore(int levelNumber) {
        scoreManager.setScoreEnabled(levelNumber > 0);
        scoreManager.setHighScoreEnabled(levelNumber > 0 && !isDemoLevel());
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

    private WorldMap currentMap(int levelNumber) {
        return switch (mapCategory) {
            case ARCADE -> getArcadeMapInLevel(levelNumber);
            case STRANGE -> strangeLevels.get(levelNumber - 1); // TODO
            case MINI -> getMiniMapInLevel(levelNumber);
            case BIG -> getBigMapInLevel(levelNumber);
        };
    }

    private WorldMap getArcadeMapInLevel(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> coloredMap(arcadeMaps, 1, TengenMapColorSchemes.PINK_DARKRED);
            case 3,4,5       -> coloredMap(arcadeMaps, 2, TengenMapColorSchemes.LIGHTBLUE_WHITE_YELLOW);
            case 6,7,8,9     -> coloredMap(arcadeMaps, 3, TengenMapColorSchemes.ORANGE_WHITE);
            case 10,11,12,13 -> coloredMap(arcadeMaps, 4, TengenMapColorSchemes.BLUE_YELLOW);
            case 14,15,16,17 -> coloredMap(arcadeMaps, 3, TengenMapColorSchemes.PINK_YELLOW);
            case 18,19,20,21 -> coloredMap(arcadeMaps, 4, TengenMapColorSchemes.PINK_DARKRED);
            case 22,23,24,25 -> coloredMap(arcadeMaps, 3, TengenMapColorSchemes.ORANGE_WHITE);
            case 26,27,28,29 -> coloredMap(arcadeMaps, 4, TengenMapColorSchemes.VIOLET_WHITE);
            case 30,31,32    -> coloredMap(arcadeMaps, 3, TengenMapColorSchemes.BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private WorldMap getMiniMapInLevel(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> coloredMap(nonArcadeMaps, 34, TengenMapColorSchemes.PINK_DARKRED);
            case 2  -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.LIGHTBLUE_WHITE_YELLOW);
            case 3  -> coloredMap(nonArcadeMaps, 34, TengenMapColorSchemes.ORANGE_WHITE);
            case 4  -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.BLUE_YELLOW);
            case 5  -> coloredMap(nonArcadeMaps, 36, TengenMapColorSchemes.PINK_YELLOW);

            case 6  -> coloredMap(nonArcadeMaps, 34, TengenMapColorSchemes.PINK_DARKRED);
            case 7  -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.BROWN2_WHITE);
            case 8  -> coloredMap(nonArcadeMaps, 36, TengenMapColorSchemes.VIOLET_WHITE_YELLOW);
            case 9  -> coloredMap(nonArcadeMaps, 30, TengenMapColorSchemes.BLACK_WHITE_YELLOW);
            case 10 -> coloredMap(nonArcadeMaps, 34, TengenMapColorSchemes.BLACK_DARKBLUE);

            case 11 -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.VIOLET_PINK);
            case 12 -> coloredMap(nonArcadeMaps, 36, TengenMapColorSchemes.RED_WHITE);
            case 13 -> coloredMap(nonArcadeMaps, 30, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 14 -> coloredMap(nonArcadeMaps, 34, TengenMapColorSchemes.YELLOW_WHITE_GREEN);
            case 15 -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.GREEN_WHITE_YELLOW);

            case 16 -> coloredMap(nonArcadeMaps, 36, TengenMapColorSchemes.KHAKI_WHITE);
            case 17 -> coloredMap(nonArcadeMaps, 30, TengenMapColorSchemes.PINK_WHITE);
            case 18 -> coloredMap(nonArcadeMaps, 16, TengenMapColorSchemes.BLUE_WHITE_YELLOW);
            case 19 -> coloredMap(nonArcadeMaps, 16, TengenMapColorSchemes.BROWN_WHITE);
            case 20 -> coloredMap(nonArcadeMaps, 30, TengenMapColorSchemes.RED_PINK);

            case 21 -> coloredMap(nonArcadeMaps, 36, TengenMapColorSchemes.BLACK_WHITE_GREEN);
            case 22 -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 23 -> coloredMap(nonArcadeMaps, 34, TengenMapColorSchemes.GREEN_WHITE_VIOLET);
            case 24 -> coloredMap(nonArcadeMaps, 37, TengenMapColorSchemes.VIOLET_WHITE_GREEN);
            case 25 -> coloredMap(nonArcadeMaps, 34, TengenMapColorSchemes.GRAY_WHITE_YELLOW);

            case 26 -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.BLUE_WHITE);
            case 27 -> coloredMap(nonArcadeMaps, 36, TengenMapColorSchemes.VIOLET_WHITE);
            case 28 -> coloredMap(nonArcadeMaps, 30, TengenMapColorSchemes.BROWN2_WHITE);
            case 29 -> coloredMap(nonArcadeMaps, 28, TengenMapColorSchemes.BROWN2_WHITE);
            case 30 -> coloredMap(nonArcadeMaps, 35, TengenMapColorSchemes.BLUE_YELLOW);

            case 31 -> coloredMap(nonArcadeMaps, 36, TengenMapColorSchemes.BLACK_WHITE_GREEN);
            case 32 -> coloredMap(nonArcadeMaps, 37, TengenMapColorSchemes.RED_PINK);

            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private WorldMap getBigMapInLevel(int levelNumber) {
        return switch (levelNumber) {
            // 1-5
            case 1  -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.ROSE_RED);
            case 2  -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.LIGHTBLUE_WHITE_YELLOW);
            case 3  -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.ORANGE_WHITE);
            case 4  -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.BLUE_WHITE_YELLOW);
            case 5  -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.PINK_YELLOW);

            // 6-10
            case 6  -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.ROSE_RED);
            case 7  -> coloredMap(nonArcadeMaps, 22, TengenMapColorSchemes.ORANGE_WHITE);
            case 8  -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.VIOLET_WHITE_YELLOW);
            case 9  -> coloredMap(nonArcadeMaps, 17, TengenMapColorSchemes.BLACK_WHITE_YELLOW);
            case 10 -> coloredMap(nonArcadeMaps, 10, TengenMapColorSchemes.BLACK_DARKBLUE);

            // 11-15
            case 11 -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.PINK_ROSE);
            case 12 -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.PINK_WHITE);
            case 13 -> coloredMap(nonArcadeMaps, 22, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 14 -> coloredMap(nonArcadeMaps, 14, TengenMapColorSchemes.VIOLET_WHITE_GREEN);
            case 15 -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.GREEN_WHITE_YELLOW);

            // 16-20
            case 16 -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.KHAKI_WHITE);
            case 17 -> coloredMap(nonArcadeMaps, 10, TengenMapColorSchemes.PINK_WHITE);
            case 18 -> coloredMap(nonArcadeMaps, 17, TengenMapColorSchemes.BLUE_WHITE_YELLOW);
            case 19 -> coloredMap(nonArcadeMaps, 10, TengenMapColorSchemes.BROWN_WHITE);
            case 20 -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.PINK_ROSE);

            // 21-25
            case 21 -> coloredMap(nonArcadeMaps, 26, TengenMapColorSchemes.BLACK_WHITE_GREEN);
            case 22 -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 23 -> coloredMap(nonArcadeMaps, 22, TengenMapColorSchemes.GREEN_WHITE_VIOLET);
            case 24 -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.VIOLET_WHITE_GREEN);
            case 25 -> coloredMap(nonArcadeMaps, 14, TengenMapColorSchemes.GRAY_WHITE_YELLOW);

            // 26-30
            case 26 -> coloredMap(nonArcadeMaps, 25, TengenMapColorSchemes.VIOLET_WHITE);
            case 27 -> coloredMap(nonArcadeMaps, 14, TengenMapColorSchemes.PINK_WHITE);
            case 28 -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 29 -> coloredMap(nonArcadeMaps, 26, TengenMapColorSchemes.LIGHTBLUE_WHITE_YELLOW);
            case 30 -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.PINK_YELLOW);

            // 31-32
            case 31 -> coloredMap(nonArcadeMaps, 25, TengenMapColorSchemes.PINK_ROSE);
            case 32 -> coloredMap(nonArcadeMaps, 33, TengenMapColorSchemes.PINK_ROSE);

            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap getStrangeMapInLevel(int levelNumber) {
        return switch (levelNumber) {
            // 1-5
            case 1  -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.ROSE_RED);
            case 2  -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.LIGHTBLUE_WHITE_YELLOW);
            case 3  -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.ORANGE_WHITE);
            case 4  -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.BLUE_WHITE_YELLOW);
            case 5  -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.PINK_YELLOW);

            // 6-10
            case 6  -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.ROSE_RED);
            case 7  -> coloredMap(nonArcadeMaps, 22, TengenMapColorSchemes.ORANGE_WHITE);
            case 8  -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.VIOLET_WHITE_YELLOW);
            case 9  -> coloredMap(nonArcadeMaps, 17, TengenMapColorSchemes.BLACK_WHITE_YELLOW);
            case 10 -> coloredMap(nonArcadeMaps, 10, TengenMapColorSchemes.BLACK_DARKBLUE);

            // 11-15
            case 11 -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.PINK_ROSE);
            case 12 -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.PINK_WHITE);
            case 13 -> coloredMap(nonArcadeMaps, 22, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 14 -> coloredMap(nonArcadeMaps, 14, TengenMapColorSchemes.VIOLET_WHITE_GREEN);
            case 15 -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.GREEN_WHITE_YELLOW);

            // 16-20
            case 16 -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.KHAKI_WHITE);
            case 17 -> coloredMap(nonArcadeMaps, 10, TengenMapColorSchemes.PINK_WHITE);
            case 18 -> coloredMap(nonArcadeMaps, 17, TengenMapColorSchemes.BLUE_WHITE_YELLOW);
            case 19 -> coloredMap(nonArcadeMaps, 10, TengenMapColorSchemes.BROWN_WHITE);
            case 20 -> coloredMap(nonArcadeMaps, 19, TengenMapColorSchemes.PINK_ROSE);

            // 21-25
            case 21 -> coloredMap(nonArcadeMaps, 26, TengenMapColorSchemes.BLACK_WHITE_GREEN);
            case 22 -> coloredMap(nonArcadeMaps, 21, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 23 -> coloredMap(nonArcadeMaps, 22, TengenMapColorSchemes.GREEN_WHITE_VIOLET);
            case 24 -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.VIOLET_WHITE_GREEN);
            case 25 -> coloredMap(nonArcadeMaps, 14, TengenMapColorSchemes.GRAY_WHITE_YELLOW);

            // 26-30
            case 26 -> coloredMap(nonArcadeMaps, 25, TengenMapColorSchemes.VIOLET_WHITE);
            case 27 -> coloredMap(nonArcadeMaps, 14, TengenMapColorSchemes.PINK_WHITE);
            case 28 -> coloredMap(nonArcadeMaps, 23, TengenMapColorSchemes.GREEN_WHITE_WHITE);
            case 29 -> coloredMap(nonArcadeMaps, 26, TengenMapColorSchemes.LIGHTBLUE_WHITE_YELLOW);
            case 30 -> coloredMap(nonArcadeMaps, 20, TengenMapColorSchemes.PINK_YELLOW);

            // 31-32
            case 31 -> coloredMap(nonArcadeMaps, 25, TengenMapColorSchemes.PINK_ROSE);
            case 32 -> coloredMap(nonArcadeMaps, 33, TengenMapColorSchemes.PINK_ROSE);

            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
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
    public int pacPowerSeconds() {
        return currentLevelData.pacPowerSeconds();
    }

    @Override
    public float pacNormalSpeed() {
        return pac.baseSpeed();
    }

    @Override
    public float pacPowerSpeed() {
        return currentLevelData.pacSpeedPoweredPercentage() * 0.01f * pac.baseSpeed();
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
        return currentLevelData.ghostSpeedFrightenedPercentage() * 0.01f * ghost.baseSpeed();
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        return currentLevelData.ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
    }

    @Override
    protected boolean hasOverflowBug() {
        return false;
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
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        float pacBaseSpeed = pacBaseSpeedInLevel(levelNumber) + pacDifficultySpeedDelta(difficulty);
        pac.setBaseSpeed(pacBaseSpeed);
        if (boosterMode == BoosterMode.ALWAYS_ON) {
            activateBooster();
        }
        for (Ghost ghost : ghosts) {
            ghost.setBaseSpeed(ghostBaseSpeedInLevel(levelNumber)
                + ghostDifficultySpeedDelta(difficulty) + ghostIDSpeedDelta(ghost.id()));
        }
    }

    @Override
    protected void initActorAnimations() {
        pac.selectAnimation(isBoosterActive() ? ANIM_PAC_MUNCHING_BOOSTER : GameModel.ANIM_PAC_MUNCHING);
        pac.animations().ifPresent(Animations::resetCurrentAnimation);
        ghosts().forEach(ghost -> {
            ghost.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void activateBooster() {
        if (boosterActive) {
            Logger.warn("Pac booster is already active");
            return;
        }
        boosterActive = true;
        pac.setBaseSpeed(pacBaseSpeedInLevel(levelNumber) + pacDifficultySpeedDelta(difficulty) + pacBoosterSpeedDelta());
        pac.selectAnimation(ANIM_PAC_MUNCHING_BOOSTER);
        Logger.info("Ms. Pac-Man booster activated, base speed set to {0.00} px/s", pac.baseSpeed());
    }

    public void deactivateBooster() {
        if (!boosterActive) {
            Logger.warn("Pac booster is already inactive");
            return;
        }
        boosterActive = false;
        pac.setBaseSpeed(pacBaseSpeedInLevel(levelNumber) + pacDifficultySpeedDelta(difficulty));
        pac.selectAnimation(ANIM_PAC_MUNCHING);
        Logger.info("Ms. Pac-Man booster deactivated, base speed set to {0.00} px/s", pac.baseSpeed());
    }

    @Override
    public void buildLevel(int levelNumber) {
        this.levelNumber = levelNumber;
        mapNumber = levelNumber;
        WorldMap map = currentMap(levelNumber);
        createWorldAndPopulation(map);
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        deactivateBooster(); // gets activated in startLevel() if ALWAYS_ON
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));

        // TODO: change this. For now provide a level object such that all code that relies on existing level object still works
        currentLevelData = new LevelData(new byte[] {
            100, // Pac speed in % of base speed
            100, // Ghost speed in % of base speed
            40, // Ghost speed in tunnel...
            20, // Dots left for Elroy1
            105, // Elroy1 speed...
            10, // Dots left for Elroy2
            110, // Elroy2 speed
            110, // Pac (power mode) speed...
            50, // Frightened ghost speed...
            6, // pac power time (seconds)
            5, // Num flashes
            0 // cut scene after this level
        });
    }

    @Override
    public void buildDemoLevel() {
        levelNumber = 1;
        mapNumber = 1;
        WorldMap map = currentMap(1);
        createWorldAndPopulation(map);
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(true);
        deactivateBooster(); // gets activated in startLevel() if ALWAYS_ON
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));

        // TODO for now provide a Level object such that all code that relies on one works
        currentLevelData = new LevelData(new byte[]{
            100, // Pac speed in % of base speed
            100, // Ghost speed in % of base speed
            40, // Ghost speed in tunnel...
            20, // Dots left for Elroy1
            105, // Elroy1 speed...
            10, // Dots left for Elroy2
            110, // Elroy2 speed
            110, // Pac (power mode) speed...
            50, // Frightened ghost speed...
            6, // pac power time (seconds)
            5, // Num flashes
            0 // cut scene after this level
        });
    }

    @Override
    public int numFlashes() {
        return currentLevelData.numFlashes();
    }

    @Override
    public int intermissionNumberAfterLevel() {
        return currentLevelData.intermissionNumber();
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
        movingBonus.setBaseSpeed(1f); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        bonus = movingBonus;
        bonus.setEdible(TickTimer.INDEFINITE);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
    }

    @Override
    protected GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        Vector2i houseTopLeftTile = map.terrain().getTileProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, v2i(10, 15));
        world.createArcadeHouse(houseTopLeftTile.x(), houseTopLeftTile.y());
        return world;
    }

    @Override
    public void onGameEnded() {
        // nothing to do yet
    }

    @Override
    protected void onPelletOrEnergizerEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        //TODO does Ms. Pac-Man slow down after eating here too?
        //pac.setRestingTicks(energizer ? 3 : 1);
        if (energizer) {
            processEatenEnergizer();
            scoreManager.scorePoints(energizerValue());
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
        gateKeeper.resetCounterAndSetEnabled(true); // TODO how is that realized in Tengen?
        pac.die();
    }

    @Override
    protected void onGhostReleased(Ghost ghost) {
        // code that is executed when ghost is relased from prison
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    private void ghostHuntingBehaviour(Ghost ghost) {
        float speed = huntingSpeed(ghost);
        if (huntingControl.phaseIndex() == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(speed);
        } else {
            boolean chasing = huntingControl.phaseType() == HuntingControl.PhaseType.CHASING;
            Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
            ghost.followTarget(targetTile, speed);
        }
    }

    /* By @russianmansmwc on Discord:
       "By the way, there's an additional quirk regarding ghosts' speed.
       On normal difficulty ONLY and in levels 5 and above, the ghosts become slightly faster if there are few dots remain.
       if there are 31 or less dots, the speed is increased. the base increase value is 2, which is further increased by 1 for every 8 dots eaten.
       (i should note it's in subunits. it if was times 2, that would've been crazy)"
     */
    private float huntingSpeed(Ghost ghost) {
        float speed = ghost.baseSpeed();
        if (difficulty == Difficulty.NORMAL && levelNumber >= 5) {
            int dotsLeft = world.uneatenFoodCount();
            byte increase = 0; // units
            if (dotsLeft <= 7) {
                increase = 5;
            } else if (dotsLeft <= 15) {
                increase = 4;
            } else if (dotsLeft <= 23) {
                increase = 3;
            } else if (dotsLeft <= 31) {
                increase = 2;
            }
            if (increase > 0) {
                speed += speedUnits(increase);
                Logger.info("Ghost speed increased by {0} units to {0.00} px/tick for {}", increase, speed, ghost.name());
            }
        }
        return speed;
    }
}