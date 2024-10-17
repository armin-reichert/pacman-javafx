/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.pacman;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.steering.RouteBasedSteering;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;

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
public class PacManArcadeGame extends GameModel {

    public static final byte ARCADE_MAP_TILES_X = 28;
    public static final byte ARCADE_MAP_TILES_Y = 36;
    public static final int  ARCADE_MAP_SIZE_X = 224;
    public static final int  ARCADE_MAP_SIZE_Y = 288;

    // Level settings as specified in the dossier
    private static final GameLevel[] LEVELS = {
        /* 1*/ new GameLevel( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0),
        /* 2*/ new GameLevel( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1),
        /* 3*/ new GameLevel( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0),
        /* 4*/ new GameLevel( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0),
        /* 5*/ new GameLevel(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2),
        /* 6*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0),
        /* 7*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0),
        /* 8*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0),
        /* 9*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3),
        /*10*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0),
        /*11*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0),
        /*12*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0),
        /*13*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3),
        /*14*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0),
        /*15*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*16*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*17*/ new GameLevel(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3),
        /*18*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*19*/ new GameLevel(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0),
        /*20*/ new GameLevel(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0),
        /*21*/ new GameLevel( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0)
    };

    // Pac-Man game specific animation IDs
    public static final String ANIM_PAC_BIG          = "big_pacman";
    public static final String ANIM_BLINKY_DAMAGED   = "damaged";
    public static final String ANIM_BLINKY_STRETCHED = "stretched";
    public static final String ANIM_BLINKY_PATCHED   = "patched";
    public static final String ANIM_BLINKY_NAKED     = "naked";

    // Ghost house tile position in all Arcade mazes
    private static final byte HOUSE_X = 10, HOUSE_Y = 15;

    // The Pac-Man Arcade game map
    private static final WorldMap WORLD_MAP = new WorldMap(PacManArcadeGame.class.getResource("/de/amr/games/pacman/maps/pacman.world"));
    static {
        // just to be sure this property is set
        WORLD_MAP.terrain().setProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, formatTile(v2i(HOUSE_X, HOUSE_Y)));
    }

    private static final List<Vector2i> CANNOT_MOVE_UP_TILES = List.of(
        v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26)
    );

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

    // Ticks of scatter and chasing phases, -1 = forever
    protected static final int[] HUNTING_TICKS_1     = {420, 1200, 420, 1200, 300,  1200, 300, -1};
    protected static final int[] HUNTING_TICKS_2_3_4 = {420, 1200, 420, 1200, 300, 61980,   1, -1};
    protected static final int[] HUNTING_TICKS_5     = {300, 1200, 300, 1200, 300, 62262,   1, -1};

    // Note: First level number is 1
    protected static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {42, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
    // Bonus value = factor * 100
    protected static final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

    protected static final Vector2f BONUS_POS = halfTileRightOf(13, 20);


    private byte cruiseElroy;

    public PacManArcadeGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;
        highScoreFile = new File(userDir, "highscore-pacman.xml");
    }

    public byte cruiseElroy() {
        return cruiseElroy;
    }

    @Override
    public void reset() {
        super.reset();
        cruiseElroy = 0;
    }

    protected GameLevel levelData(int levelNumber) {
        return LEVELS[Math.min(levelNumber - 1, LEVELS.length - 1)];
    }

    @Override
    public int currentMapNumber() {
        return 1;
    }

    @Override
    public int intermissionNumberAfterLevel() {
        return levelNumber > 0 ? levelData(levelNumber).intermissionNumber() : 0;
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        checkHuntingPhaseIndex(phaseIndex);
        long ticks = switch (levelNumber) {
            case 1 -> HUNTING_TICKS_1[phaseIndex];
            case 2, 3, 4 -> HUNTING_TICKS_2_3_4[phaseIndex];
            default -> HUNTING_TICKS_5[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    protected Pac createPac() {
        Pac pacMan = new Pac();
        pacMan.setName("Pac-Man");
        return pacMan;
    }

    @Override
    protected Ghost[] createGhosts() {
        return new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.clyde() };
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        pac.setBaseSpeed(73.9f / 60f); // TODO should be 75 but then it doesn't run synchronously to the original game
        ghosts().forEach(ghost -> {
            ghost.setBaseSpeed(73.9f / 60f); // TODO see above
            ghost.setSpeedReturningHome(2.0f);
            ghost.setSpeedInsideHouse(0.5f);
        });
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = levelNumber;
        createWorldAndPopulation(WORLD_MAP);
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts().forEach(ghost -> {
            ghost.setHuntingBehaviour(this::ghostHuntingBehaviour);
            ghost.setCannotMoveUpTiles(CANNOT_MOVE_UP_TILES);
        });
    }

    @Override
    protected GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        world.createArcadeHouse(HOUSE_X, HOUSE_Y);
        return world;
    }

    @Override
    public Optional<GameLevel> currentLevelData() {
        return levelNumber > 0 ? Optional.of(levelData(levelNumber)): Optional.empty();
    }

    @Override
    public int numFlashes() {
        return levelNumber > 0 ? levelData(levelNumber).numFlashes() : 0;
    }

    @Override
    public float pacNormalSpeed() {
        return levelNumber > 0
            ? levelData(levelNumber).pacSpeedPoweredPercentage() * 0.01f * pac.baseSpeed()
            : 0;
    }

    @Override
    public float pacPowerSpeed() {
        return levelNumber > 0
            ? levelData(levelNumber).pacSpeedPoweredPercentage() * 0.01f * pac.baseSpeed()
            : 0;
    }

    @Override
    public float ghostFrightenedSpeed(Ghost ghost) {
        return levelNumber > 0
            ? levelData(levelNumber).ghostSpeedFrightenedPercentage() * 0.01f * ghost.baseSpeed()
            : 0;
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        return levelNumber > 0
            ? levelData(levelNumber).ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed()
            : 0;
    }

    @Override
    public void buildDemoLevel() {
        buildRegularLevel(1);
        pac.setAutopilot(new RouteBasedSteering(List.of(PACMAN_DEMO_LEVEL_ROUTE)));
        pac.setUseAutopilot(true);
    }

    @Override
    protected boolean isLevelCounterEnabled() {
        return !demoLevel;
    }

    @Override
    public boolean isPacManKillingIgnoredInDemoLevel() {
        return false;
    }

    @Override
    protected void onFoodEaten() {
        if (world.uneatenFoodCount() == levelData(levelNumber).elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (world.uneatenFoodCount() == levelData(levelNumber).elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
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
    public void onPacDying() {
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true);
        setCruiseElroyEnabled(false);
        pac.die();
    }

    @Override
    public int pacPowerSeconds() {
        return levelNumber > 0 ? levelData(levelNumber).pacPowerSeconds() : 0;
    }

    @Override
    public boolean isBonusReached() {
        return world.eatenFoodCount() == 70 || world.eatenFoodCount() == 170;
    }

    // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
    @Override
    public byte computeBonusSymbol() {
        return levelNumber > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber];
    }

    @Override
    public void activateNextBonus() {
        nextBonusIndex += 1;
        byte symbol = bonusSymbols[nextBonusIndex];
        bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        bonus.setEdible(bonusEdibleTicks());
        bonus.entity().setPosition(BONUS_POS);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
    }

    protected int bonusEdibleTicks() {
        return randomInt(540, 600); // 9-10 seconds
    }

    protected void ghostHuntingBehaviour(Ghost ghost) {
        boolean chasing = isChasingPhase(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
        Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
        float speed = huntingSpeed(ghost);
        ghost.followTarget(targetTile, speed);
    }

    private float huntingSpeed(Ghost ghost) {
        GameLevel level = levelData(levelNumber);
        if (world.isTunnel(ghost.tile())) {
            return level.ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 1) {
            return level.elroy1SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 2) {
            return level.elroy2SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        return level.ghostSpeedPercentage() * 0.01f * ghost.baseSpeed();
    }
}