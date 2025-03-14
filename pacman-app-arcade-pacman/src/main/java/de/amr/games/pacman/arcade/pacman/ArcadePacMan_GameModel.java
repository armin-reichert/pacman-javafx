/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.Waypoint;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.LevelData;
import de.amr.games.pacman.model.MapSelector;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.steering.RouteBasedSteering;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Classic Arcade Pac-Man.
 *
 * <p>There are however some differences to the original.
 *     <ul>
 *         <li>Only single player mode supported</li>
 *         <li>Attract mode (demo level) not identical to Arcade version because ghosts move randomly</li>
 *         <li>Pac-Man steering more comfortable because next direction can be selected before intersection is reached</li>
 *         <li>Cornering behavior is different</li>
 *         <li>Accuracy only about 90% (estimated) so patterns can not be used</li>
 *     </ul>
 * </p>
 *
 * @author Armin Reichert
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public class ArcadePacMan_GameModel extends GameModel {

    public static Ghost blinky() {
        var ghost = new Ghost(GameModel.RED_GHOST);
        ghost.setName("Blinky");
        return ghost;
    }

    public static Ghost pinky() {
        var ghost = new Ghost(GameModel.PINK_GHOST);
        ghost.setName("Pinky");
        return ghost;
    }

    public static Ghost inky() {
        var ghost = new Ghost(GameModel.CYAN_GHOST);
        ghost.setName("Inky");
        return ghost;
    }

    public static Ghost clyde() {
        var ghost = new Ghost(GameModel.ORANGE_GHOST);
        ghost.setName("Clyde");
        return ghost;
    }


    // Level settings as specified in the dossier
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

    protected static final Waypoint[] PACMAN_DEMO_LEVEL_ROUTE = {
        new Waypoint(12, 26), new Waypoint(9, 26), new Waypoint(12, 32), new Waypoint(15, 32), new Waypoint(24, 29), new Waypoint(21, 23),
        new Waypoint(18, 23), new Waypoint(18, 20), new Waypoint(18, 17), new Waypoint(15, 14), new Waypoint(12, 14), new Waypoint(9, 17),
        new Waypoint(6, 17), new Waypoint(6, 11), new Waypoint(6, 8), new Waypoint(6, 4), new Waypoint(1, 8), new Waypoint(6, 8),
        new Waypoint(9, 8), new Waypoint(12, 8), new Waypoint(6, 4), new Waypoint(6, 8), new Waypoint(6, 11), new Waypoint(1, 8),
        new Waypoint(6, 8), new Waypoint(9, 8), new Waypoint(12, 14), new Waypoint(9, 17), new Waypoint(6, 17), new Waypoint(0, 17),
        new Waypoint(21, 17), new Waypoint(21, 23), new Waypoint(21, 26), new Waypoint(24, 29), /* avoid moving up: */ new Waypoint(26, 29),
        new Waypoint(15, 32), new Waypoint(12, 32), new Waypoint(3, 29), new Waypoint(6, 23), new Waypoint(9, 23), new Waypoint(12, 26),
        new Waypoint(15, 26), new Waypoint(18, 23), new Waypoint(21, 23), new Waypoint(24, 29), /* avoid moving up: */ new Waypoint(26, 29),
        new Waypoint(15, 32), new Waypoint(12, 32), new Waypoint(3, 29), new Waypoint(6, 23)
    };

    private static final byte PELLET_VALUE = 10;
    private static final byte ENERGIZER_VALUE = 50;

    // Note: First level number is 1
    protected static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {42, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
    // Bonus value = factor * 100
    protected static final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

    protected final Steering autopilot = new RuleBasedPacSteering(this);
    protected Steering demoLevelSteering;
    protected byte cruiseElroy;

    public ArcadePacMan_GameModel() {
        this(new ArcadePacMan_MapSelector());
    }

    protected ArcadePacMan_GameModel(MapSelector mapSelector) {
        super(mapSelector, new ArcadePacMan_HuntingTimer());
        huntingTimer.setOnPhaseChange(() -> level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseASAP));
        lastLevelNumber = Integer.MAX_VALUE;
    }

    @Override
    public void init() {
        initialLives = 3;
        simulateOverflowBug = true;
        cutScenesEnabled = true;
        scoreManager.setHighScoreFile(new File(HOME_DIR, "highscore-pacman.xml"));
        scoreManager.setExtraLifeScores(10_000);
        mapSelector.loadAllMaps(this);
        demoLevelSteering = new RouteBasedSteering(List.of(PACMAN_DEMO_LEVEL_ROUTE));
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
        huntingTimer.reset();
    }

    protected LevelData levelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    public byte cruiseElroy() {
        return cruiseElroy;
    }

    protected void setCruiseElroy(int value) {
        if (!inClosedRange(value, 0, 2)) {
            throw new IllegalArgumentException("Allowed Cruise Elroy values are 0, 1, 2, but value is " + value);
        }
        cruiseElroy = (byte) value;
    }

    protected void setCruiseElroyEnabled(boolean enabled) {
        if (enabled && cruiseElroy < 0 || !enabled && cruiseElroy > 0) {
            cruiseElroy = (byte) -cruiseElroy;
        }
    }

    @Override
    public boolean canStartNewGame() {
        return GameController.THE_ONE.credit > 0;
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
        return 90;
    }

    @Override
    public void endGame() {
        if (GameController.THE_ONE.credit > 0) {
            GameController.THE_ONE.credit -= 1;
        }
        scoreManager().updateHighScore();
        publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
    }

    protected void populateLevel(GameLevel level) {
        WorldMap worldMap = level.map();

        if (!worldMap.hasProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MIN_TILE, formatTile(vec_2i(10, 15)));
        }
        if (!worldMap.hasProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MAX_TILE)) {
            Logger.warn("No house max tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, PROPERTY_POS_HOUSE_MAX_TILE, formatTile(vec_2i(17, 19)));
        }
        Vector2i minTile = worldMap.getTerrainTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
        Vector2i maxTile = worldMap.getTerrainTileProperty(PROPERTY_POS_HOUSE_MAX_TILE, null);
        level.createArcadeHouse(minTile.x(), minTile.y(), maxTile.x(), maxTile.y());

        var pac = new Pac();
        pac.setName("Pac-Man");
        pac.setGameLevel(level);
        pac.reset();

        var ghosts = new Ghost[] { blinky(), pinky(), inky(), clyde() };
        Stream.of(ghosts).forEach(ghost -> {
            ghost.setGameLevel(level);
            ghost.setRevivalPosition(level.ghostPosition(ghost.id()));
            ghost.reset();
        });
        ghosts[RED_GHOST].setRevivalPosition(level.ghostPosition(PINK_GHOST)); // middle house position

        level.setPac(pac);
        level.setGhosts(ghosts);
        level.setBonusSymbol(0, computeBonusSymbol(level.number));
        level.setBonusSymbol(1, computeBonusSymbol(level.number));
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        level.pac().setBaseSpeed(1.25f);
        level.ghosts().forEach(ghost -> ghost.setBaseSpeed(1.25f));
    }

    @Override
    public GameLevel makeNormalLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.selectWorldMap(levelNumber);

        GameLevel newLevel = new GameLevel(levelNumber, worldMap);
        newLevel.setNumFlashes(levelData(newLevel.number).numFlashes());
        newLevel.setCutSceneNumber(cutScenesEnabled ? cutSceneNumberAfterLevel(newLevel.number) : 0);

        levelCounterEnabled = true;

        populateLevel(newLevel);
        newLevel.pac().setAutopilot(autopilot);
        setCruiseElroy(0);
        newLevel.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));

        List<Vector2i> oneWayDownTiles = worldMap.tiles()
                .filter(tile -> worldMap.get(LayerID.TERRAIN, tile) == TerrainTiles.ONE_WAY_DOWN).toList();
        newLevel.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        return newLevel;
    }

    @Override
    public GameLevel makeDemoLevel() {
        GameLevel newLevel = makeNormalLevel(1);
        levelCounterEnabled = false;
        demoLevelSteering.init();
        assignDemoLevelBehavior(newLevel);
        return newLevel;
    }

    protected int cutSceneNumberAfterLevel(int number) {
        return switch (number) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        };
    }

    @Override
    public void assignDemoLevelBehavior(GameLevel demoLevel) {
        demoLevel.pac().setAutopilot(demoLevelSteering);
        demoLevel.pac().setUsingAutopilot(true);
        demoLevel.pac().setImmune(false);
    }

    @Override
    public float pacNormalSpeed() {
        if (level == null) {
            return 0;
        }
        byte percentage = levelData(level.number).pacSpeedPercentage();
        return percentage > 0 ? percentage * 0.01f * level.pac().baseSpeed() : level.pac().baseSpeed();
    }

    @Override
    public float pacPowerSpeed() {
        if (level == null) {
            return 0;
        }
        byte percentage = levelData(level.number).pacSpeedPoweredPercentage();
        return percentage > 0 ? percentage * 0.01f * level.pac().baseSpeed() : pacNormalSpeed();
    }

    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        LevelData levelData = levelData(level.number);
        if (level.isTunnel(ghost.tile())) {
            return levelData.ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 1) {
            return levelData.elroy1SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 2) {
            return levelData.elroy2SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        return levelData.ghostSpeedPercentage() * 0.01f * ghost.baseSpeed();
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
        if (level == null) {
            return 0;
        }
        float percentage = levelData(level.number).ghostSpeedFrightenedPercentage();
        return percentage > 0 ? percentage * 0.01f * ghost.baseSpeed() : ghost.baseSpeed();
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        return level != null
            ? levelData(level.number).ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed()
            : 0;
    }

    @Override
    public boolean isPacManKillingIgnored() {
        return false;
    }

    @Override
    protected void onPelletOrEnergizerEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        level.pac().setRestingTicks(energizer ? 3 : 1);
        if (uneatenFoodCount == levelData(level.number).elroy1DotsLeft()) {
            setCruiseElroy(1);
        } else if (uneatenFoodCount == levelData(level.number).elroy2DotsLeft()) {
            setCruiseElroy(2);
        }
        if (energizer) {
            processEatenEnergizer();
            scoreManager.scorePoints(this, ENERGIZER_VALUE);
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
    protected void onGhostReleased(Ghost ghost) {
        if (ghost.id() == ORANGE_GHOST && cruiseElroy < 0) {
            Logger.trace("Re-enable cruise elroy mode because {} exits house:", ghost.name());
            setCruiseElroyEnabled(true);
        }
    }

    @Override
    public void onPacKilled() {
        huntingTimer.stop();
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
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.addVictim(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(this, points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        if (level.victimsCount() == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_EATEN_IN_LEVEL;
            scoreManager.scorePoints(this, extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, level.number);
        }
    }

    @Override
    public long pacPowerTicks() {
        return level != null ? 60 * levelData(level.number).pacPowerSeconds() : 0;
    }

    @Override
    public long pacPowerFadingTicks() {
        // ghost flashing animation has frame length 14 so one full flash takes 28 ticks
        return level != null ? level.numFlashes() * 28L : 0;
    }

    @Override
    public boolean isBonusReached() {
        return level.eatenFoodCount() == 70 || level.eatenFoodCount() == 170;
    }

    // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
    @Override
    public byte computeBonusSymbol(int levelNumber) {
        return levelNumber > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber];
    }

    @Override
    public void activateNextBonus() {
        level.advanceNextBonus();
        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        StaticBonus staticBonus = new StaticBonus(symbol, ArcadePacMan_GameModel.BONUS_VALUE_FACTORS[symbol] * 100);
        level.setBonus(staticBonus);
        if (level.map().hasProperty(LayerID.TERRAIN, PROPERTY_POS_BONUS)) {
            Vector2i bonusTile = level.map().getTerrainTileProperty(PROPERTY_POS_BONUS, new Vector2i(13, 20));
            staticBonus.actor().setPosition(halfTileRightOf(bonusTile));
        } else {
            Logger.error("No bonus position found in map");
            staticBonus.actor().setPosition(halfTileRightOf(13, 20));
        }
        staticBonus.setEdible(bonusEdibleTicks());
        publishGameEvent(GameEventType.BONUS_ACTIVATED, staticBonus.actor().tile());
    }

    protected int bonusEdibleTicks() {
        return randomInt(540, 600); // 9-10 seconds
    }

    protected void ghostHuntingBehaviour(Ghost ghost) {
        boolean chasing = huntingTimer.phaseType() == HuntingTimer.PhaseType.CHASING
            || ghost.id() == RED_GHOST && cruiseElroy > 0;
        Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
        ghost.followTarget(targetTile, ghostAttackSpeed(ghost));
    }
}