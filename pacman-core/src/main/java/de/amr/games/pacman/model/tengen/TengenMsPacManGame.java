/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.tengen;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.Portal;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Ms. Pac-Man Tengen game. Currently, this is just a copy of the Ms. Pac-Man Arcade game with the Tengen mazes.
 * <p>
 * TODO: use Tengen sprites all over the place
 * TODO: how do the ghosts in Tengen differ from the Arcade game? How can they handle non-Arcade maze structures?
 *
 * @author Armin Reichert
 */
public class TengenMsPacManGame extends GameModel {

    public enum MapCategory { ARCADE, MINI, BIG, STRANGE }
    public enum Difficulty { NORMAL, EASY, HARD, CRAZY }
    public enum PacBooster { OFF, TOGGLE_USING_KEY, ALWAYS_ON }

    // TODO what is the exact speed increase?
    private static final float BOOSTER_FACTOR = 1.5f;

    //TODO check how many maps are really used in Tengen game
    private static final int ARCADE_MAP_COUNT = 9;
    private static final int NON_ARCADE_MAP_COUNT = 37;
    private static final int MAP_COUNT = ARCADE_MAP_COUNT + NON_ARCADE_MAP_COUNT;

    private static final String MAPS_ROOT = "/de/amr/games/pacman/maps/tengen/";
    private static final String ARCADE_MAP_PATTERN  = MAPS_ROOT + "arcade/map%02d.world";
    private static final String NON_ARCADE_MAP_PATTERN  = MAPS_ROOT + "non_arcade/map%02d.world";

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    //TODO I have no idea about the hunting times in Tengen, use Ms. Pac-Man Arcade times for now
    private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    // Bonus symbols in Arcade, Mini and Big mazes
    public static final byte BONUS_CHERRY     = 0;
    public static final byte BONUS_STRAWBERRY = 1;
    public static final byte BONUS_ORANGE     = 2;
    public static final byte BONUS_PRETZEL    = 3;
    public static final byte BONUS_APPLE      = 4;
    public static final byte BONUS_PEAR       = 5;
    public static final byte BONUS_BANANA     = 6;
    // Additional bonus symbols in Strange mazes
    public static final byte BONUS_MILK       = 7;
    public static final byte BONUS_ICE_CREAM  = 8;
    public static final byte BONUS_GLASS_SLIPPER = 9;
    public static final byte BONUS_STAR       = 10;
    public static final byte BONUS_HAND       = 11;
    public static final byte BONUS_RING       = 12;
    public static final byte BONUS_FLOWER     = 13;

    // Bonus value = factor * 100
    private static final byte[] BONUS_VALUE_FACTORS = new byte[14];
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
        BONUS_VALUE_FACTORS[BONUS_GLASS_SLIPPER] = 60;
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
    private PacBooster pacBooster;
    private boolean boosterActive;
    private byte startingLevel; // 1-7
    private int mapNumber;

    public TengenMsPacManGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;
        highScoreFile = new File(userDir, "highscore-ms_pacman_tengen.xml");

        arcadeMaps = readMaps(ARCADE_MAP_PATTERN, ARCADE_MAP_COUNT);
        assignMapCategory(arcadeMaps, MapCategory.ARCADE);

        List<WorldMap> nonArcadeMaps = readMaps(NON_ARCADE_MAP_PATTERN, NON_ARCADE_MAP_COUNT);

        //TODO how are maps really categorized in Tengen?

        strangeMaps = nonArcadeMaps.stream().filter(this::isStrangeMap).toList();
        assignMapCategory(strangeMaps, MapCategory.STRANGE);

        miniMaps = nonArcadeMaps.stream().filter(worldMap -> worldMap.terrain().numRows() <= 30).toList();
        assignMapCategory(miniMaps, MapCategory.MINI);

        bigMaps = nonArcadeMaps.stream().filter(worldMap -> worldMap.terrain().numRows() > ARCADE_MAP_TILES_Y).toList();
        assignMapCategory(bigMaps, MapCategory.BIG);

        setMapCategory(MapCategory.ARCADE);
        setPacBooster(PacBooster.OFF);
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

    public void setPacBooster(PacBooster pacBooster) {
        this.pacBooster = pacBooster;
    }

    public PacBooster pacBooster() {
        return pacBooster;
    }

    public void setBoosterActive(boolean boosterActive) {
        this.boosterActive = boosterActive;
        float baseSpeed = PPS_AT_100_PERCENT * SEC_PER_TICK; // pixel/sec
        if (boosterActive) {
            baseSpeed *= BOOSTER_FACTOR;
        }
        pac.setBaseSpeed(baseSpeed);
        pac.selectAnimation(boosterActive ? Pac.ANIM_MUNCHING_BOOSTER : Pac.ANIM_MUNCHING);
        Logger.info("Ms. Pac-Man base speed is {0.00} px/s", baseSpeed);
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

    private void assignMapCategory(List<WorldMap> maps, MapCategory category) {
        maps.forEach(map -> {
            map.terrain().setProperty("map_category", category.name());
            Logger.info("Map #{} -> {}", map.terrain().getProperty("map_number"), category);
        });
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
        pac.selectAnimation(isBoosterActive() ? Pac.ANIM_MUNCHING_BOOSTER : Pac.ANIM_MUNCHING);
        pac.animations().ifPresent(Animations::resetSelected);
    }

    @Override
    protected Ghost[] createGhosts() {
        return new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = levelNumber;
        mapNumber = mapNumberByLevelNumber(levelNumber);
        var map = currentMapList.get(mapNumber - 1);
        setWorldAndCreatePopulation(createWorld(map));
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        initPacBooster();
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void buildDemoLevel() {
        levelNumber = 1;
        mapNumber = mapNumberByLevelNumber(levelNumber);
        setWorldAndCreatePopulation(createWorld(currentMapList.get(mapNumber - 1)));
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(true);
        initPacBooster();
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
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
        if (levelNumber <= maxBonus) {
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
        movingBonus.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);
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
        byte speed = huntingSpeedPct(ghost);
        if (huntingPhaseIndex == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(speed);
        } else {
            boolean chase = isChasingPhase(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
            ghost.followTarget(chase ? chasingTarget(ghost) : scatterTarget(ghost), speed);
        }
    }

    private void initPacBooster() {
        switch (pacBooster) {
            case ALWAYS_ON -> {
                pac.setBaseSpeed(BOOSTER_FACTOR * PPS_AT_100_PERCENT * SEC_PER_TICK);
                setBoosterActive(true);
            }
            case OFF -> {
                pac.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);
                setBoosterActive(false);
            }
            case TOGGLE_USING_KEY -> {
                pac.setBaseSpeed(BOOSTER_FACTOR * PPS_AT_100_PERCENT * SEC_PER_TICK);
                setBoosterActive(false);
            }
        }
    }
}