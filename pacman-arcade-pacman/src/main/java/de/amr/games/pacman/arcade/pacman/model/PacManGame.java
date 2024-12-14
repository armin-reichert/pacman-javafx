/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Tiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.LevelData;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.steering.RouteBasedSteering;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
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
public class PacManGame extends GameModel {

    public static final Vector2i ARCADE_MAP_SIZE_IN_TILES  = new Vector2i(28, 36);
    public static final Vector2f ARCADE_MAP_SIZE_IN_PIXELS = new Vector2f(224, 288);

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

    protected static int intermissionNumberAfterLevel(int number) {
        return switch (number) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        };
    }

    // Ghost house tile position in all Arcade mazes
    private static final byte HOUSE_X = 10, HOUSE_Y = 15;

    protected static final NavPoint[] PACMAN_DEMO_LEVEL_ROUTE = {
        np(12, 26), np(9, 26), np(12, 32), np(15, 32), np(24, 29), np(21, 23),
        np(18, 23), np(18, 20), np(18, 17), np(15, 14), np(12, 14), np(9, 17),
        np(6, 17), np(6, 11), np(6, 8), np(6, 4), np(1, 8), np(6, 8),
        np(9, 8), np(12, 8), np(6, 4), np(6, 8), np(6, 11), np(1, 8),
        np(6, 8), np(9, 8), np(12, 14), np(9, 17), np(6, 17), np(0, 17),
        np(21, 17), np(21, 23), np(21, 26), np(24, 29), /* avoid moving up: */ np(26, 29),
        np(15, 32), np(12, 32), np(3, 29), np(6, 23), np(9, 23), np(12, 26),
        np(15, 26), np(18, 23), np(21, 23), np(24, 29), /* avoid moving up: */ np(26, 29),
        np(15, 32), np(12, 32), np(3, 29), np(6, 23)
    };

    private static final byte PELLET_VALUE = 10;
    private static final byte ENERGIZER_VALUE = 50;

    // Note: First level number is 1
    protected static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {42, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
    // Bonus value = factor * 100
    protected static final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};
    protected static final Vector2f BONUS_POS = halfTileRightOf(13, 20);

    // Ticks of scatter and chasing phases, -1=INDEFINITE
    protected static final int[] HUNTING_TICKS_LEVEL_1 = {420, 1200, 420, 1200, 300,  1200, 300, -1};
    protected static final int[] HUNTING_TICKS_LEVEL_2_3_4 = {420, 1200, 420, 1200, 300, 61980,   1, -1};
    protected static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 300, 1200, 300, 62262,   1, -1};

    protected final WorldMap theWorldMap;
    protected final Steering autopilot = new RuleBasedPacSteering(this);
    protected Steering demoLevelSteering;
    protected byte cruiseElroy;

    public PacManGame(File userDir) {
        super(userDir);
        initialLives = 3;
        simulateOverflowBug = true;

        scoreManager.setHighScoreFile(new File(userDir, "highscore-pacman.xml"));
        scoreManager.setExtraLifeScores(10_000);

        String path = "/de/amr/games/pacman/maps/pacman.world";
        URL url = getClass().getResource(path);
        if (url == null) {
            throw new MissingResourceException("Map not found", getClass().getName(), path);
        }
        try {
            theWorldMap = new WorldMap(url);
            theWorldMap.setConfigValue("mapNumber", 1);
            theWorldMap.setConfigValue("colorMapIndex", 0);
        } catch (IOException x) {
            Logger.error("Could not create world map, url={}", url);
            throw new RuntimeException(x);
        }

        huntingControl = new HuntingControl() {
            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = switch (levelNumber) {
                    case 1 -> HUNTING_TICKS_LEVEL_1[phaseIndex];
                    case 2, 3, 4 -> HUNTING_TICKS_LEVEL_2_3_4[phaseIndex];
                    default -> HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
                };
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
        return GameController.it().coinControl().hasCredit();
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
        GameController.it().coinControl().consumeCoin();
        scoreManager().updateHighScore();
        publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
    }

    protected void populateLevel(WorldMap worldMap) {
        GameWorld world = new GameWorld(worldMap);
        world.createArcadeHouse(HOUSE_X, HOUSE_Y);

        var pac = new Pac();
        pac.setName("Pac-Man");
        pac.setWorld(world);
        pac.reset();

        var ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.clyde() };
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
        levelCounterEnabled = true;
        level.setNumFlashes(levelData(level.number).numFlashes());
        level.setIntermissionNumber(intermissionNumberAfterLevel(level.number));
        populateLevel(theWorldMap);
        level.pac().setAutopilot(autopilot);
        setCruiseElroy(0);
        List<Vector2i> oneWayDownTiles = theWorldMap.terrain().tiles()
            .filter(tile -> theWorldMap.terrain().get(tile) == Tiles.ONE_WAY_DOWN).toList();
        level.ghosts().forEach(ghost -> {
            ghost.setHuntingBehaviour(this::ghostHuntingBehaviour);
            ghost.setSpecialTerrainTiles(oneWayDownTiles);
        });
    }

    @Override
    public void configureDemoLevel() {
        configureNormalLevel();
        levelCounterEnabled = false;
        demoLevelSteering = new RouteBasedSteering(List.of(PACMAN_DEMO_LEVEL_ROUTE));
        setDemoLevelBehavior();
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
        if (level.world().isTunnel(ghost.tile())) {
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
        return level.world().eatenFoodCount() == 70 || level.world().eatenFoodCount() == 170;
    }

    // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
    @Override
    public byte computeBonusSymbol() {
        return level.number > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[level.number];
    }

    @Override
    public void activateNextBonus() {
        level.advanceNextBonus();
        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        StaticBonus staticBonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        staticBonus.setEdible(bonusEdibleTicks());
        staticBonus.entity().setPosition(BONUS_POS);
        level.setBonus(staticBonus);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, staticBonus.entity().tile());
    }

    protected int bonusEdibleTicks() {
        return randomInt(540, 600); // 9-10 seconds
    }

    protected void ghostHuntingBehaviour(Ghost ghost) {
        boolean chasing = huntingControl.phaseType() == HuntingControl.PhaseType.CHASING
            || ghost.id() == RED_GHOST && cruiseElroy > 0;
        Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
        ghost.followTarget(targetTile, ghostAttackSpeed(ghost));
    }
}