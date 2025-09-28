/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import org.tinylog.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.Waypoint.wp;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

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
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public class ArcadePacMan_GameModel extends Arcade_GameModel {

    public static final WorldMapColorScheme MAP_COLOR_SCHEME = new WorldMapColorScheme(
        "#000000", "#2121ff", "#ffb7ff", "#febdb4"
    );

    public static class PacMan extends Pac {

        public PacMan() {
            super("Pac-Man");
            reset();
        }
    }

    public static class Blinky extends Ghost {

        public Blinky() {
            reset();
        }

        @Override
        public String name() {
            return "Blinky";
        }

        @Override
        public byte personality() {
            return RED_GHOST_SHADOW;
        }

        @Override
        public void hunt(GameLevel gameLevel, HuntingTimer huntingTimer) {
            // Blinky overrides hunt method to take Cruise Elroy mode into account
            var arcadeGame = (Arcade_GameModel) gameLevel.game();
            boolean chase = huntingTimer.phase() == HuntingPhase.CHASING || arcadeGame.cruiseElroy() > 0;
            Vector2i targetTile = chase
                ? chasingTargetTile(gameLevel)
                : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
            setSpeed(gameLevel.game().ghostAttackSpeed(gameLevel, this));
            tryMovingTowardsTargetTile(gameLevel, targetTile);
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            // Blinky (red ghost) attacks Pac-Man directly
            return gameLevel.pac().tile();
        }
    }

    public static class Pinky extends Ghost {

        public Pinky() {
            reset();
        }

        @Override
        public String name() {
            return "Pinky";
        }

        @Override
        public byte personality() {
            return PINK_GHOST_SPEEDY;
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            // Pinky (pink ghost) ambushes Pac-Man
            return gameLevel.pac().tilesAheadWithOverflowBug(4);
        }
    }

    public static class Inky extends Ghost {

        public Inky() {
            reset();
        }

        @Override
        public String name() {
            return "Inky";
        }

        @Override
        public byte personality() {
            return CYAN_GHOST_BASHFUL;
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            // Inky (cyan ghost) attacks from opposite side as Blinky
            return gameLevel.pac().tilesAheadWithOverflowBug(2).scaled(2).minus(gameLevel.ghost(RED_GHOST_SHADOW).tile());
        }
    }

    public static class Clyde extends Ghost {

        public Clyde() {
            reset();
        }

        @Override
        public String name() {
            return "Clyde";
        }

        @Override
        public byte personality() {
            return ORANGE_GHOST_POKEY;
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            // Attacks directly or retreats towards scatter target if Pac is near
            return tile().euclideanDist(gameLevel.pac().tile()) < 8
                ? gameLevel.worldMap().terrainLayer().ghostScatterTile(personality())
                : gameLevel.pac().tile();
        }
    }

    // Level data as given in the "Pac-Man dossier"
    protected static final byte[][] RAW_LEVEL_DATA = {
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

    protected static final ArcadeLevelData[] LEVEL_DATA = Arrays.stream(RAW_LEVEL_DATA)
        .map(ArcadeLevelData::new)
        .toArray(ArcadeLevelData[]::new);

    // Note: level numbering starts with 1
    protected static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = { -1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7 };

    // bonus points = multiplier * 100
    protected static final byte[] BONUS_VALUE_MULTIPLIERS = { 1, 3, 5, 7, 10, 20, 30, 50 };

    protected static final Map<Integer, Integer> CUT_SCENE_AFTER_LEVEL = Map.of(
         2, 1, // after level #2, play cut scene #1
         5, 2,
         9, 3,
        13, 3,
        17, 3
    );

    protected final MapSelector mapSelector;
    protected final HUD hud = new DefaultHUD();
    protected final HuntingTimer huntingTimer;

    public ArcadePacMan_GameModel(GameContext gameContext, File highScoreFile) {
        this(gameContext, new ArcadePacMan_MapSelector(), highScoreFile);
    }

    /**
     * @param gameContext the game context
     * @param mapSelector e.g. selector that selects custom maps before standard maps
     * @param highScoreFile file where high score is stored
     */
    public ArcadePacMan_GameModel(GameContext gameContext, MapSelector mapSelector, File highScoreFile) {
        super(gameContext);

        this.mapSelector = requireNonNull(mapSelector);

        scoreManager.setHighScoreFile(highScoreFile);
        scoreManager.setExtraLifeScores(EXTRA_LIFE_SCORE);

        huntingTimer = new HuntingTimer("ArcadePacMan-HuntingTimer", 8) {
            // Ticks of scatter and chasing phases, -1 = INFINITE
            static final int[] TICKS_LEVEL_1     = {420, 1200, 420, 1200, 300,  1200, 300, -1};
            static final int[] TICKS_LEVEL_2_3_4 = {420, 1200, 420, 1200, 300, 61980,   1, -1};
            static final int[] TICKS_LEVEL_5_ON  = {300, 1200, 300, 1200, 300, 62262,   1, -1};

            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = switch (levelNumber) {
                    case 1 -> TICKS_LEVEL_1[phaseIndex];
                    case 2, 3, 4 -> TICKS_LEVEL_2_3_4[phaseIndex];
                    default -> TICKS_LEVEL_5_ON[phaseIndex];
                };
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingTimer.phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) {
                gameLevel().ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::requestTurnBack);
            }
        });

        gateKeeper = new GateKeeper(this);
        gateKeeper.setOnGhostReleased(prisoner -> {
            if (prisoner.personality() == ORANGE_GHOST_POKEY && !isCruiseElroyModeActive()) {
                Logger.debug("Re-enable 'Cruise Elroy' mode because {} got released:", prisoner.name());
                activateCruiseElroyMode(true);
            }
        });

        demoLevelSteering = new RouteBasedSteering(List.of(
            wp(9, 26), wp(9, 29), wp(12,29), wp(12, 32), wp(26,32),
            wp(26,29), wp(24,29), wp(24,26), wp(26,26), wp(26,23),
            wp(21,23), wp(18,23), wp(18,14), wp(9,14), wp(9,17),
            wp(6,17), wp(6,4), wp(1,4), wp(1,8), wp(12,8),
            wp(12,4), wp(6,4), wp(6,11), wp(1,11), wp(1,8),
            wp(9,8), wp(9,11), wp(12,11), wp(12,14), wp(9,14),
            wp(9,17), wp(0,17), /*warp tunnel*/ wp(21,17), wp(21,29),
            wp(26,29), wp(26,32), wp(1,32), wp(1,29), wp(3,29),
            wp(3,26), wp(1,26), wp(1,23), wp(12,23), wp(12,26),
            wp(15,26), wp(15,23), wp(26,23), wp(26,26), wp(24,26),
            wp(24,29), wp(26,29), wp(26,32), wp(1,32),
            wp(1,29), wp(3,29), wp(3,26), wp(1,26), wp(1,23),
            wp(6,23) /* eaten at 3,23 in original game */
        ));
        autopilot = new RuleBasedPacSteering(gameContext);

        mapSelector.loadAllMapPrototypes();
    }

    protected ArcadeLevelData levelData(int levelNumber) {
        int row = Math.min(levelNumber - 1, LEVEL_DATA.length - 1);
        return LEVEL_DATA[row];
    }

    @Override
    protected ArcadeLevelData levelData(GameLevel gameLevel) {
        return levelData(gameLevel.number());
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public GameEventManager eventManager() {
        return gameContext.eventManager();
    }

    @Override
    public ScoreManager scoreManager() {
        return scoreManager;
    }

    @Override
    public HUD hud() {
        return hud;
    }

    @Override
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    @Override
    public Optional<Integer> optCutSceneNumber(int levelNumber) {
        Integer cutSceneNumber = CUT_SCENE_AFTER_LEVEL.get(levelNumber);
        return Optional.ofNullable(cutSceneNumber);
    }

    @Override
    public void createLevel(int levelNumber) {
        final WorldMap worldMap = mapSelector.getWorldMapCopy(levelNumber);
        final ArcadeHouse house = new ArcadeHouse(ARCADE_MAP_HOUSE_MIN_TILE);
        worldMap.terrainLayer().setHouse(house);

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap);
        newGameLevel.setGameOverStateTicks(90);

        final Pac pacMan = new PacMan();
        pacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(pacMan);

        final Blinky blinky = new Blinky();
        final Pinky pinky = new Pinky();
        final Inky inky = new Inky();
        final Clyde clyde = new Clyde();

        blinky.setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_1_RED)));
        pinky .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_2_PINK)));
        inky  .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_3_CYAN)));
        clyde .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, clyde);

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = worldMap.terrainLayer().tiles()
            .filter(tile -> worldMap.terrainLayer().get(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .toList();
        newGameLevel.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (7, "key") appears.
        byte symbol = BONUS_SYMBOLS_BY_LEVEL_NUMBER[Math.min(levelNumber, 13)];
        newGameLevel.setBonusSymbol(0, symbol);
        newGameLevel.setBonusSymbol(1, symbol);

        setGameLevel(newGameLevel);
        setLevelCounterEnabled(true);
    }

    @Override
    protected boolean isPacManSafeInDemoLevel() {
        return false;
    }

    @Override
    protected boolean isBonusReached() {
        FoodLayer foodLayer = gameLevel().worldMap().foodLayer();
        return foodLayer.eatenFoodCount() == 70 || foodLayer.eatenFoodCount() == 170;
    }

    @Override
    public void activateNextBonus() {
        gameLevel().selectNextBonus();
        byte symbol = gameLevel().bonusSymbol(gameLevel().currentBonusIndex());
        var bonus = new Bonus(symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100, null);
        Vector2i bonusTile = gameLevel().worldMap().terrainLayer().getTileProperty(DefaultWorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
        bonus.setPosition(halfTileRightOf(bonusTile));
        bonus.setEdible(randomInt(9 * NUM_TICKS_PER_SEC, 10 * NUM_TICKS_PER_SEC));
        gameLevel().setBonus(bonus);
        eventManager().publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
    }
}