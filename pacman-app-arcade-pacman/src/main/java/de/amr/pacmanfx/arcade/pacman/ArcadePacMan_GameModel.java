/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.Waypoint.wp;
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
public class ArcadePacMan_GameModel extends ArcadeCommon_GameModel {

    public static Pac createPac() {
        var pac = new Pac("Pac-Man");
        pac.reset();
        return pac;
    }

    public static Ghost createGhost(byte personality) {
        requireValidGhostPersonality(personality);
        return switch (personality) {
            case RED_GHOST_SHADOW -> new Ghost(RED_GHOST_SHADOW, "Blinky") {
                @Override
                public void hunt(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.gameLevel();
                    var arcadeGame = (ArcadeCommon_GameModel) gameContext.game();

                    boolean chase = arcadeGame.huntingTimer().phase() == HuntingPhase.CHASING || arcadeGame.cruiseElroy() > 0;
                    Vector2i targetTile = chase ? chasingTargetTile(gameContext) : level.ghostScatterTile(id().personality());
                    setSpeed(gameContext.game().actorSpeedControl().ghostAttackSpeed(gameContext, level, this));
                    tryMovingTowardsTargetTile(gameContext, targetTile);
                }
                @Override
                public Vector2i chasingTargetTile(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    // Blinky (red ghost) attacks Pac-Man directly
                    return gameContext.gameLevel().pac().tile();
                }
            };
            case PINK_GHOST_SPEEDY -> new Ghost(PINK_GHOST_SPEEDY, "Pinky") {
                @Override
                public void hunt(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.gameLevel();

                    boolean chase = gameContext.game().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile(gameContext) : level.ghostScatterTile(id().personality());
                    setSpeed(gameContext.game().actorSpeedControl().ghostAttackSpeed(gameContext, level, this));
                    tryMovingTowardsTargetTile(gameContext, targetTile);
                }
                @Override
                public Vector2i chasingTargetTile(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    // Pinky (pink ghost) ambushes Pac-Man
                    return gameContext.gameLevel().pac().tilesAheadWithOverflowBug(4);
                }
            };
            case CYAN_GHOST_BASHFUL -> new Ghost(CYAN_GHOST_BASHFUL, "Inky") {
                @Override
                public void hunt(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.gameLevel();

                    boolean chase = gameContext.game().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile(gameContext) : level.ghostScatterTile(id().personality());
                    setSpeed(gameContext.game().actorSpeedControl().ghostAttackSpeed(gameContext, level, this));
                    tryMovingTowardsTargetTile(gameContext, targetTile);
                }
                @Override
                public Vector2i chasingTargetTile(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    GameLevel level = gameContext.gameLevel();

                    // Inky (cyan ghost) attacks from opposite side as Blinky
                    return level.pac().tilesAheadWithOverflowBug(2).scaled(2).minus(level.ghost(RED_GHOST_SHADOW).tile());
                }
            };
            case ORANGE_GHOST_POKEY -> new Ghost(ORANGE_GHOST_POKEY, "Clyde") {
                @Override
                public void hunt(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.gameLevel();

                    boolean chase = gameContext.game().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile(gameContext) : level.ghostScatterTile(id().personality());
                    setSpeed(gameContext.game().actorSpeedControl().ghostAttackSpeed(gameContext, level, this));
                    tryMovingTowardsTargetTile(gameContext, targetTile);
                }
                @Override
                public Vector2i chasingTargetTile(GameContext gameContext) {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    GameLevel level = gameContext.gameLevel();
                    // Attacks directly or retreats towards scatter target if Pac is near
                    return tile().euclideanDist(level.pac().tile()) < 8
                        ? level.ghostScatterTile(id().personality())
                        : level.pac().tile();
                }
            };
            default -> throw new IllegalArgumentException("Illegal ghost personality " + personality);
        };
    }

    // Level data as given in the "Pac-Man dossier"
    protected static final byte[][] LEVEL_DATA = {
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

    protected static LevelData createLevelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    // Note: level numbering starts with 1
    protected static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = { -1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7 };

    // bonus points = multiplier * 100
    protected static final byte[] BONUS_VALUE_MULTIPLIERS = { 1, 3, 5, 7, 10, 20, 30, 50 };

    protected final MapSelector mapSelector;
    protected final HUDData hud = new ArcadePacMan_HUDData();
    protected final ScoreManager scoreManager;
    protected final HuntingTimer huntingTimer;
    protected final ActorSpeedControl actorSpeedControl;

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

        scoreManager = new DefaultScoreManager(this, highScoreFile);
        scoreManager.setExtraLifeScores(Set.of(EXTRA_LIFE_SCORE));

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
                gameLevel.ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::reverseAtNextOccasion);
            }
        });

        actorSpeedControl = new ArcadeCommon_ActorSpeedControl();

        gateKeeper = new GateKeeper(this);
        gateKeeper.setOnGhostReleased(prisoner -> {
            if (prisoner.id().personality() == ORANGE_GHOST_POKEY && !isCruiseElroyModeActive()) {
                Logger.debug("Re-enable 'Cruise Elroy' mode because {} got released:", prisoner.name());
                activateCruiseElroyMode(true);
            }
        });

        demoLevelSteering = new RouteBasedSteering(gameContext, List.of(
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

        mapSelector.loadAllMaps();
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public ScoreManager scoreManager() {
        return scoreManager;
    }

    @Override
    public HUDData hudData() {
        return hud;
    }

    @Override
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    @Override
    public ActorSpeedControl actorSpeedControl() {
        return actorSpeedControl;
    }

    @Override
    public OptionalInt optCutSceneNumber(int levelNumber) {
        return switch (levelNumber) {
            case 2 -> OptionalInt.of(1);
            case 5 -> OptionalInt.of(2);
            case 9, 13, 17 -> OptionalInt.of(3);
            default -> OptionalInt.empty();
        };
    }

    @Override
    public void createLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.getWorldMap(levelNumber);
        gameLevel = new GameLevel(levelNumber, worldMap, createLevelData(levelNumber));
        gameLevel.setGameOverStateTicks(90);

        Pac pacMan = createPac();
        pacMan.setAutopilotSteering(autopilot);
        gameLevel.setPac(pacMan);

        // Special tiles where attacking ghosts cannot move up
        List<Vector2i> oneWayDownTiles = worldMap.tiles()
            .filter(tile -> worldMap.content(LayerID.TERRAIN, tile) == TerrainTile.ONE_WAY_DOWN.$)
            .toList();
        gameLevel.setGhosts(
            createGhost(RED_GHOST_SHADOW),
            createGhost(PINK_GHOST_SPEEDY),
            createGhost(CYAN_GHOST_BASHFUL),
            createGhost(ORANGE_GHOST_POKEY));
        gameLevel.ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setSpecialTerrainTiles(oneWayDownTiles);
        });

        // Each level has a single bonus symbol appearing twice during the level. From level 13 on, the same symbol
        // (7 = "key") appears.
        byte symbol = BONUS_SYMBOLS_BY_LEVEL_NUMBER[Math.min(levelNumber, 13)];
        gameLevel.setBonusSymbol(0, symbol);
        gameLevel.setBonusSymbol(1, symbol);

        hud.theLevelCounter().setEnabled(true);
    }

    @Override
    protected boolean isPacManSafeInDemoLevel() {
        return false;
    }

    @Override
    protected boolean isBonusReached() {
        return gameLevel.eatenFoodCount() == 70 || gameLevel.eatenFoodCount() == 170;
    }

    @Override
    public void activateNextBonus() {
        gameLevel.selectNextBonus();
        byte symbol = gameLevel.bonusSymbol(gameLevel.currentBonusIndex());
        var bonus = new Bonus(symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100, null);
        Vector2i bonusTile = gameLevel.worldMap().getTerrainTileProperty(WorldMapProperty.POS_BONUS, new Vector2i(13, 20));
        bonus.setPosition(halfTileRightOf(bonusTile));
        bonus.setEdibleTicks(randomInt(9 * NUM_TICKS_PER_SEC, 10 * NUM_TICKS_PER_SEC));
        gameLevel.setBonus(bonus);
        eventManager().publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
    }

    @Override
    public GameEventManager eventManager() {
        return gameContext.eventManager();
    }
}