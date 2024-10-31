/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.controller.HuntingControl;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.actors.GhostState.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NamedMapColorScheme.*;

/**
 * Ms. Pac-Man Tengen game.
 *
 * @author Armin Reichert
 */
public class TengenMsPacManGame extends GameModel {

    private static final List<NamedMapColorScheme> COLOR_SCHEMES_IN_LEVEL_ORDER = List.of(
        MSC_36_15_20_PINK_RED_WHITE,       // Level 1
        MSC_21_20_28_BLUE_WHITE_YELLOW,
        MSC_16_20_15_ORANGE_WHITE_RED,
        MSC_01_38_20_BLUE_YELLOW_WHITE,
        MSC_35_28_20_PINK_YELLOW_WHITE,    // Level 5
        MSC_36_15_20_PINK_RED_WHITE,
        MSC_17_20_20_BROWN_WHITE_WHITE,
        MSC_13_20_28_VIOLET_WHITE_YELLOW,
        MSC_0F_20_28_BLACK_WHITE_YELLOW,
        MSC_0F_01_20_BLACK_BLUE_WHITE,     // Level 10
        MSC_14_25_20_VIOLET_ROSE_WHITE,
        MSC_15_20_20_RED_WHITE_WHITE,
        MSC_1B_20_20_GREEN_WHITE_WHITE,
        MSC_28_20_2A_YELLOW_WHITE_GREEN,
        MSC_1A_20_28_GREEN_WHITE_YELLOW,   // Level 15
        MSC_18_20_20_KHAKI_WHITE_WHITE,
        MSC_25_20_20_ROSE_WHITE_WHITE,
        MSC_12_20_28_BLUE_WHITE_YELLOW,
        MSC_07_20_20_BROWN_WHITE_WHITE,
        MSC_15_25_20_RED_ROSE_WHITE,       // Level 20
        MSC_0F_20_1C_BLACK_WHITE_GREEN,
        MSC_19_20_20_GREEN_WHITE_WHITE,
        MSC_0C_20_14_GREEN_WHITE_VIOLET,
        MSC_23_20_2B_VIOLET_WHITE_GREEN,
        MSC_10_20_28_GRAY_WHITE_YELLOW,    // Level 25
        MSC_03_20_20_VIOLET_WHITE_WHITE,
        MSC_04_20_20_VIOLET_WHITE_WHITE   // TODO clarify when randomization starts
        // Levels 28-32 use randomly selected schemes
    );

    private static Map<String, String> randomMapColorScheme() {
        return COLOR_SCHEMES_IN_LEVEL_ORDER.get(Globals.randomInt(0, COLOR_SCHEMES_IN_LEVEL_ORDER.size())).get();
    }

    static final String MAPS_ROOT = "/de/amr/games/pacman/maps/tengen/";

    static final String ARCADE_MAP_PATTERN = MAPS_ROOT + "arcade%d.world";
    static final int ARCADE_MAP_COUNT = 4;

    static final String MINI_MAP_PATTERN = MAPS_ROOT + "mini%d.world";
    static final int MINI_MAP_COUNT = 6;

    static final String STRANGE_OR_BIG_MAP_PATTERN = MAPS_ROOT + "strange_or_big/map%02d.world";
    static final int STRANGE_OR_BIG_MAP_COUNT = 33;

    // Ms. Pac-Man (Tengen) game specific animation IDs
    public static final String ANIM_PAC_MUNCHING_BOOSTER = "munching_booster";
    public static final String ANIM_PAC_HUSBAND_MUNCHING_BOOSTER = "husband_munching_booster";

    private static float speedUnits(float units) {
        return units / 32f;
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

    private List<WorldMap> arcadeMaps;
    private List<WorldMap> miniMaps;
    private List<WorldMap> strangeOrBigMaps; //TODO separate?

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private BoosterMode boosterMode;
    private boolean boosterActive;
    private byte startingLevel; // 1-7
    private boolean canStartGame;

    private LevelData currentLevelData;

    public TengenMsPacManGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;
        scoreManager.setHighScoreFile(new File(userDir, "highscore-ms_pacman_tengen.xml"));
        scoreManager.setExtraLifeScore(10_000);

        huntingControl = new HuntingControl("HuntingControl-" + getClass().getSimpleName()) {
            //TODO: I have no idea about the timing in Tengen, use Ms. Pac-Man Arcade values for now
            private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
            private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingControl.setOnPhaseChange(() -> ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible));

        setMapCategory(MapCategory.ARCADE);
        setPacBooster(BoosterMode.OFF);
        setDifficulty(Difficulty.NORMAL);
        setStartingLevel(1);
    }

    @Override
    public void reset() {
        super.reset();
        setMapCategory(MapCategory.ARCADE);
        setPacBooster(BoosterMode.OFF);
        setDifficulty(Difficulty.NORMAL);
        setStartingLevel(1);
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
    public long gameOverStateTicks() {
        return 600; // TODO how much really?
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
        this.startingLevel = (byte) startingLevel;
    }

    private void selectMapForLevel(int levelNumber) {
        switch (mapCategory) {
            case ARCADE  -> selectArcadeMapForLevel(levelNumber);
            case STRANGE -> selectStrangeMapForLevel(levelNumber);
            case MINI    -> selectMiniMapForLevel(levelNumber);
            case BIG     -> selectBigMapForLevel(levelNumber);
        }
    }

    private void selectArcadeMapForLevel(int levelNumber) {
        List<WorldMap> maps = getArcadeMaps();
        switch (levelNumber) {
            case 1,2         -> selectMap(maps, 1, MSC_36_15_20_PINK_RED_WHITE.get());
            case 3,4,5       -> selectMap(maps, 2, MSC_21_20_28_BLUE_WHITE_YELLOW.get());
            case 6,7,8,9     -> selectMap(maps, 3, MSC_16_20_15_ORANGE_WHITE_RED.get());
            case 10,11,12,13 -> selectMap(maps, 4, MSC_01_38_20_BLUE_YELLOW_WHITE.get());
            case 14,15,16,17 -> selectMap(maps, 3, MSC_35_28_20_PINK_YELLOW_WHITE.get());
            case 18,19,20,21 -> selectMap(maps, 4, MSC_36_15_20_PINK_RED_WHITE.get());
            case 22,23,24,25 -> selectMap(maps, 3, MSC_17_20_20_BROWN_WHITE_WHITE.get());
            //TODO also random color schemes from level 28 on?
            case 26,27       -> selectMap(maps, 4, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 28,29       -> selectMap(maps, 4, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 30,31,32    -> selectMap(maps, 3, MSC_0F_20_28_BLACK_WHITE_YELLOW.get());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        }
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private void selectMiniMapForLevel(int levelNumber) {
        List<WorldMap> maps = getMiniMaps();
        switch (levelNumber) {
            case 1  -> selectMap(maps, 1, MSC_36_15_20_PINK_RED_WHITE.get());
            case 2  -> selectMap(maps, 2, MSC_21_20_28_BLUE_WHITE_YELLOW.get());
            case 3  -> selectMap(maps, 1, MSC_16_20_15_ORANGE_WHITE_RED.get());
            case 4  -> selectMap(maps, 2, MSC_01_38_20_BLUE_YELLOW_WHITE.get());
            case 5  -> selectMap(maps, 3, MSC_35_28_20_PINK_YELLOW_WHITE.get());
            case 6  -> selectMap(maps, 1, MSC_36_15_20_PINK_RED_WHITE.get());
            case 7  -> selectMap(maps, 2, MSC_17_20_20_BROWN_WHITE_WHITE.get());
            case 8  -> selectMap(maps, 3, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 9  -> selectMap(maps, 4, MSC_0F_20_28_BLACK_WHITE_YELLOW.get());
            case 10 -> selectMap(maps, 1, MSC_0F_01_20_BLACK_BLUE_WHITE.get());
            case 11 -> selectMap(maps, 2, MSC_14_25_20_VIOLET_ROSE_WHITE.get());
            case 12 -> selectMap(maps, 3, MSC_15_20_20_RED_WHITE_WHITE.get());
            case 13 -> selectMap(maps, 4, MSC_1B_20_20_GREEN_WHITE_WHITE.get());
            case 14 -> selectMap(maps, 1, MSC_28_20_2A_YELLOW_WHITE_GREEN.get());
            case 15 -> selectMap(maps, 2, MSC_1A_20_28_GREEN_WHITE_YELLOW.get());
            case 16 -> selectMap(maps, 3, MSC_18_20_20_KHAKI_WHITE_WHITE.get());
            case 17 -> selectMap(maps, 4, MSC_25_20_20_ROSE_WHITE_WHITE.get());
            case 18 -> selectMap(maps, 5, MSC_12_20_28_BLUE_WHITE_YELLOW.get());
            case 19 -> selectMap(maps, 5, MSC_07_20_20_BROWN_WHITE_WHITE.get());
            case 20 -> selectMap(maps, 4, MSC_15_25_20_RED_ROSE_WHITE.get());
            case 21 -> selectMap(maps, 3, MSC_0F_20_1C_BLACK_WHITE_GREEN.get());
            case 22 -> selectMap(maps, 2, MSC_19_20_20_GREEN_WHITE_WHITE.get());
            case 23 -> selectMap(maps, 1, MSC_0C_20_14_GREEN_WHITE_VIOLET.get());
            case 24 -> selectMap(maps, 6, MSC_23_20_2B_VIOLET_WHITE_GREEN.get());
            case 25 -> selectMap(maps, 1, MSC_10_20_28_GRAY_WHITE_YELLOW.get());
            case 26 -> selectMap(maps, 2, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 27 -> selectMap(maps, 3, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 28 -> selectMap(maps, 4, randomMapColorScheme());
            case 29 -> selectMap(maps, 5, randomMapColorScheme());
            case 30 -> selectMap(maps, 2, randomMapColorScheme());
            case 31 -> selectMap(maps, 3, randomMapColorScheme());
            case 32 -> selectMap(maps, 6, randomMapColorScheme());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        }
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private void selectBigMapForLevel(int levelNumber) {
        List<WorldMap> maps = getStrangeOrBigMaps();
        switch (levelNumber) {
            case 1  -> selectMap(maps, 19, MSC_36_15_20_PINK_RED_WHITE.get());
            case 2  -> selectMap(maps, 20, MSC_21_20_28_BLUE_WHITE_YELLOW.get());
            case 3  -> selectMap(maps, 21, MSC_16_20_15_ORANGE_WHITE_RED.get());
            case 4  -> selectMap(maps, 19, MSC_01_38_20_BLUE_YELLOW_WHITE.get());
            case 5  -> selectMap(maps, 20, MSC_35_28_20_PINK_YELLOW_WHITE.get());
            case 6  -> selectMap(maps, 21, MSC_36_15_20_PINK_RED_WHITE.get());
            case 7  -> selectMap(maps, 22, MSC_17_20_20_BROWN_WHITE_WHITE.get());
            case 8  -> selectMap(maps, 23, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 9  -> selectMap(maps, 17, MSC_0F_20_28_BLACK_WHITE_YELLOW.get());
            case 10 -> selectMap(maps, 10, MSC_0F_01_20_BLACK_BLUE_WHITE.get());
            case 11 -> selectMap(maps, 23, MSC_15_25_20_RED_ROSE_WHITE.get());
            case 12 -> selectMap(maps, 21, MSC_25_20_20_ROSE_WHITE_WHITE.get());
            case 13 -> selectMap(maps, 22, MSC_1B_20_20_GREEN_WHITE_WHITE.get());
            case 14 -> selectMap(maps, 14, MSC_28_20_2A_YELLOW_WHITE_GREEN.get());
            case 15 -> selectMap(maps, 20, MSC_1A_20_28_GREEN_WHITE_YELLOW.get());
            case 16 -> selectMap(maps, 19, MSC_18_20_20_KHAKI_WHITE_WHITE.get());
            case 17 -> selectMap(maps, 10, MSC_25_20_20_ROSE_WHITE_WHITE.get());
            case 18 -> selectMap(maps, 17, MSC_12_20_28_BLUE_WHITE_YELLOW.get());
            case 19 -> selectMap(maps, 10, MSC_07_20_20_BROWN_WHITE_WHITE.get());
            case 20 -> selectMap(maps, 19, MSC_15_25_20_RED_ROSE_WHITE.get());
            case 21 -> selectMap(maps, 26, MSC_0F_20_1C_BLACK_WHITE_GREEN.get());
            case 22 -> selectMap(maps, 21, MSC_19_20_20_GREEN_WHITE_WHITE.get());
            case 23 -> selectMap(maps, 22, MSC_0C_20_14_GREEN_WHITE_VIOLET.get());
            case 24 -> selectMap(maps, 23, MSC_23_20_2B_VIOLET_WHITE_GREEN.get());
            case 25 -> selectMap(maps, 14, MSC_10_20_28_GRAY_WHITE_YELLOW.get());
            case 26 -> selectMap(maps, 25, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 27 -> selectMap(maps, 14, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 28 -> selectMap(maps, 23, randomMapColorScheme());
            case 29 -> selectMap(maps, 26, randomMapColorScheme());
            case 30 -> selectMap(maps, 20, randomMapColorScheme());
            case 31 -> selectMap(maps, 25, randomMapColorScheme());
            case 32 -> selectMap(maps, 33, randomMapColorScheme());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        }
    }

    private void selectStrangeMapForLevel(int levelNumber) {
        List<WorldMap> maps = getStrangeOrBigMaps();
        int mapNumber = levelNumber; // TODO: check this
        WorldMap worldMap = maps.get(mapNumber - 1);
        selectMap(maps, mapNumber, createColorSchemeFromMap(worldMap));
    }

    private void selectMap(List<WorldMap> maps, int mapNumber, Map<String, String> mapColorScheme) {
        currentMapNumber = mapNumber;
        currentMap = new WorldMap(maps.get(mapNumber - 1));
        currentMapColorScheme = mapColorScheme;
    }

    private Map<String, String> createColorSchemeFromMap(WorldMap worldMap) {
        Map<String, String> defaultScheme = MSC_36_15_20_PINK_RED_WHITE.get();
        String fill   = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_WALL_FILL, defaultScheme.get("fill"));
        String stroke = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_WALL_STROKE, defaultScheme.get("stroke"));
        String door   = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_DOOR, defaultScheme.get("door"));
        String pellet = worldMap.food().getPropertyOrDefault(PROPERTY_COLOR_FOOD, defaultScheme.get("pellet"));
        return Map.of(
            "fill",   colorToHexFormat(fill).orElse(defaultScheme.get("fill")),
            "stroke", colorToHexFormat(stroke).orElse(defaultScheme.get("stroke")),
            "door",   colorToHexFormat(door).orElse(defaultScheme.get("door")),
            "pellet", colorToHexFormat(pellet).orElse(defaultScheme.get("pellet"))
        );
    }

    private List<WorldMap> getArcadeMaps() {
        if (arcadeMaps == null) {
            Logger.info("Loading ARCADE maps");
            arcadeMaps = createMaps(ARCADE_MAP_PATTERN, ARCADE_MAP_COUNT);
            Logger.info("Maps loaded.");
        }
        return arcadeMaps;
    }

    private List<WorldMap> getMiniMaps() {
        if (miniMaps == null) {
            Logger.info("Loading MINI maps");
            miniMaps = createMaps(MINI_MAP_PATTERN, MINI_MAP_COUNT);
            Logger.info("Maps loaded.");
        }
        return miniMaps;
    }

    private List<WorldMap> getStrangeOrBigMaps() {
        if (strangeOrBigMaps == null) {
            Logger.info("Loading STRANGE and BIG maps");
            strangeOrBigMaps = createMaps(STRANGE_OR_BIG_MAP_PATTERN, STRANGE_OR_BIG_MAP_COUNT);
            Logger.info("Maps loaded.");
        }
        return strangeOrBigMaps;
    }

    private List<WorldMap> createMaps(String pattern, int maxNumber) {
        List<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= maxNumber; ++num) {
            String path = pattern.formatted(num);
            URL url = getClass().getResource(path);
            if (url != null) {
                WorldMap worldMap = new WorldMap(url);
                maps.add(worldMap);
                Logger.info("World map #{} has been read from URL {}", num, url);
            } else {
                Logger.error("World map #{} could not be read from URL {}", num, url);
            }
        }
        return maps;
    }

    @Override
    protected Pac createPac() {
        Pac msPacMan = new Pac();
        msPacMan.setName("Ms. Pac-Man");
        return msPacMan;
    }

    @Override
    public long pacPowerTicks() {
        if (!inRange(currentLevelNumber, 1, 32)) {
            Logger.error("Level number ({}) is out of range", currentLevelNumber);
            return 0;
        }
        double seconds = switch (currentLevelNumber) {
            case  1 -> 6;
            case  2 -> 5;
            case  3 -> 4;
            case  4 -> 3;
            case  5 -> 2;
            case  6 -> 5;
            case  7 -> 2;
            case  8 -> 1.75;
            case  9 -> 1.5;
            case 10 -> 4;
            case 11 -> 2;
            case 12 -> 1.75;
            case 13 -> 1.5;
            case 14 -> 2;
            case 15 -> 1.75;
            case 16 -> 1.5;
            case 17 -> 0;
            case 18 -> 1.5;
            default -> 2;
        };
        return (long) (seconds * 60);
    }

    @Override
    public long pacPowerFadingTicks() {
        return numFlashes() * 28L; // TODO check in emulator
    }

    @Override
    public float pacNormalSpeed() {
        return pac.baseSpeed();
    }

    @Override
    public float pacPowerSpeed() {
        float percentage = currentLevelData.pacSpeedPoweredPercentage();
        return percentage > 0 ? percentage * 0.01f * pac.baseSpeed() : pac.baseSpeed();
    }

    /*
    @RussianManSMWC on Discord:
    By the way, there's an additional quirk regarding ghosts' speed.
    On normal difficulty ONLY and in levels 5 and above, the ghosts become slightly faster if there are few dots remain.
    if there are 31 or less dots, the speed is increased. the base increase value is 2, which is further increased by 1 for every 8 dots eaten.
    (i should note it's in subunits. it if was times 2, that would've been crazy)
    */
    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        float speed = ghost.baseSpeed();
        if (difficulty == Difficulty.NORMAL && currentLevelNumber >= 5) {
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
        float percentage = currentLevelData.ghostSpeedFrightenedPercentage();
        return percentage > 0 ? percentage * 0.01f * ghost.baseSpeed() : ghost.baseSpeed();
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
            for (int number = 1; number <= Math.min(startingLevel, LEVEL_COUNTER_MAX_SIZE); ++number) {
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
        pac.setBaseSpeed(pacBaseSpeedInLevel(currentLevelNumber) + pacDifficultySpeedDelta(difficulty) + pacBoosterSpeedDelta());
        pac.selectAnimation(ANIM_PAC_MUNCHING_BOOSTER);
        Logger.info("Ms. Pac-Man booster activated, base speed set to {0.00} px/s", pac.baseSpeed());
    }

    public void deactivateBooster() {
        if (!boosterActive) {
            Logger.warn("Pac booster is already inactive");
            return;
        }
        boosterActive = false;
        pac.setBaseSpeed(pacBaseSpeedInLevel(currentLevelNumber) + pacDifficultySpeedDelta(difficulty));
        pac.selectAnimation(ANIM_PAC_MUNCHING);
        Logger.info("Ms. Pac-Man booster deactivated, base speed set to {0.00} px/s", pac.baseSpeed());
    }

    @Override
    public void buildLevel(int levelNumber) {
        if (!inRange(levelNumber, 1, 32)) {
            throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        }
        currentLevelNumber = levelNumber;
        selectMapForLevel(levelNumber);
        createWorldAndPopulation(currentMap);

        Logger.info("World created. Map number: {}, URL: {}", currentMapNumber, currentMap.url());

        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUsingAutopilot(false);
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
        currentLevelNumber = 1;
        selectMapForLevel(1);
        createWorldAndPopulation(currentMap);
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUsingAutopilot(true);
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
    public void letsGetReadyToRumble() {
        super.letsGetReadyToRumble();
        // ghosts inside house start at floor of house
        float posY = world.houseFloorY() - HTS;
        ghosts().filter(Ghost::insideHouse).forEach(ghost -> ghost.setPosY(posY));
    }

    @Override
    public int numFlashes() {
        //TODO need to find out what Tengen really does
        return 5;
    }

    @Override
    public int intermissionNumberAfterLevel() {
        return switch (currentLevelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3; // TODO not sure what happens in later levels
            default -> 0;
        };
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
    public boolean isBonusReached() {
        return world.eatenFoodCount() == 64 || world.eatenFoodCount() == 176;
    }

    @Override
    public byte computeBonusSymbol() {
        //TODO: I have no idea yet how Tengen does this
        byte maxBonus = mapCategory == MapCategory.STRANGE ? BONUS_FLOWER : BONUS_BANANA;
        if (currentLevelNumber - 1 <= maxBonus) {
            return (byte) (currentLevelNumber - 1);
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
        // code that is executed when ghost is released from jailhouse
    }

    // TODO clarify what exactly Tengen Ms. Pac-Man does
    private void ghostHuntingBehaviour(Ghost ghost) {
        float speed = ghostAttackSpeed(ghost);
        if (huntingControl.phaseIndex() == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(speed);
        } else {
            boolean chasing = huntingControl.phaseType() == HuntingControl.PhaseType.CHASING;
            Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
            ghost.followTarget(targetTile, speed);
        }
    }
}