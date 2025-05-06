/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.steering.RouteBasedSteering;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.Waypoint.wp;
import static de.amr.games.pacman.model.actors.GhostState.*;
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
 * @author Armin Reichert
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public class ArcadePacMan_GameModel extends ArcadeAny_GameModel {

    // Level settings as specified in the "Pac-Man dossier"
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

    protected LevelData createLevelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    // Note: level numbering start with 1
    private static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = { 69, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6 };

    // bonus points = multiplier * 100
    private static final byte[] BONUS_VALUE_MULTIPLIERS = { 1, 3, 5, 7, 10, 20, 30, 50 };

    public ArcadePacMan_GameModel() {
        this(new ArcadePacMan_MapSelector());
    }

    /**
     * @param mapSelector map selector e.g. selector that selects custom maps before standard maps
     */
    protected ArcadePacMan_GameModel(MapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        highScoreFile = new File(HOME_DIR, "highscore-pacman.xml");
        extraLifeScores = List.of(EXTRA_LIFE_SCORE);
        levelCounter = new ArcadePacMan_LevelCounter();

        huntingTimer = new HuntingTimer(8) {
            // Ticks of scatter and chasing phases, -1=INDEFINITE
            static final int[] HUNTING_TICKS_LEVEL_1 = {420, 1200, 420, 1200, 300,  1200, 300, -1};
            static final int[] HUNTING_TICKS_LEVEL_2_3_4 = {420, 1200, 420, 1200, 300, 61980,   1, -1};
            static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 300, 1200, 300, 62262,   1, -1};

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
        huntingTimer.phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAtNextOccasion);
        });

        gateKeeper = new GateKeeper();
        gateKeeper.setOnGhostReleasedAction(prisoner -> {
            if (prisoner.id() == ORANGE_GHOST_ID && level.ghost(RED_GHOST_ID).cruiseElroy() < 0) {
                Logger.trace("Re-enable Blinky Cruise Elroy mode because {} exits house:", prisoner.name());
                level.ghost(RED_GHOST_ID).enableCruiseElroyMode(true);
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
        autopilot = new RuleBasedPacSteering(this);
    }

    @Override
    public void createLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.selectWorldMap(requireValidLevelNumber(levelNumber));
        level = new GameLevel(this, levelNumber, worldMap);
        level.setData(createLevelData(levelNumber));
        level.setHuntingTimer(huntingTimer);
        level.setCutSceneNumber(switch (levelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        });
        level.setGameOverStateTicks(90);
        level.addArcadeHouse();

        var pac = new Pac();
        pac.setName("Pac-Man");
        pac.setGameLevel(level);
        pac.reset();
        pac.setAutopilot(autopilot);
        level.setPac(pac);

        // Special tiles where attacking ghosts cannot move up
        List<Vector2i> oneWayDownTiles = worldMap.tiles()
            .filter(tile -> worldMap.get(LayerID.TERRAIN, tile) == TerrainTiles.ONE_WAY_DOWN)
            .toList();
        level.setGhosts(createRedGhost(), createPinkGhost(), createCyanGhost(), createOrangeGhost());
        level.ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setRevivalPosition(ghost.id() == RED_GHOST_ID
                ? level.ghostStartPosition(PINK_GHOST_ID)
                : level.ghostStartPosition(ghost.id()));
            ghost.setGameLevel(level);
            ghost.setSpecialTerrainTiles(oneWayDownTiles);
        });

        level.setSpeedControl(new ArcadeActorSpeedControl());
        // Must be called after creation of the actors!
        level.speedControl().applyToActorsInLevel(level);

        level.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        level.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        levelCounter.setEnabled(true);
    }

    protected static Ghost createRedGhost() {
        return new Ghost(RED_GHOST_ID, "Blinky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING || cruiseElroy() > 0;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Blinky (red ghost) attacks Pac-Man directly
                return level.pac().tile();
            }
        };
    }

    /** @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>. */
    protected static Ghost createPinkGhost() {
        return new Ghost(PINK_GHOST_ID, "Pinky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Pinky (pink ghost) ambushes Pac-Man
                return level.pac().tilesAhead(4, true);
            }
        };
    }

    /** @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>. */
    protected static Ghost createCyanGhost() {
        return new Ghost(CYAN_GHOST_ID, "Inky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Inky (cyan ghost) attacks from opposite side as Blinky
                return level.pac().tilesAhead(2, true).scaled(2).minus(level.ghost(RED_GHOST_ID).tile());
            }
        };
    }

    protected static Ghost createOrangeGhost() {
        return new Ghost(ORANGE_GHOST_ID, "Clyde") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Attacks directly or retreats towards scatter target if Pac is near
                return tile().euclideanDist(level.pac().tile()) < 8 ? level.ghostScatterTile(id()) : level.pac().tile();
            }
        };
    }

    @Override
    public boolean isPacManSafeInDemoLevel() {
        return false;
    }

    @Override
    public long pacPowerFadingTicks(GameLevel level) {
        // ghost flashing animation has frame length 14 so one full flash takes 28 ticks
        return level != null ? level.data().numFlashes() * 28L : 0;
    }

    @Override
    public boolean isBonusReached() {
        return level.eatenFoodCount() == 70 || level.eatenFoodCount() == 170;
    }

    // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
    private byte computeBonusSymbol(int levelNumber) {
        return levelNumber > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber];
    }

    @Override
    public void activateNextBonus() {
        level.selectNextBonus();
        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        var bonus = new StaticBonus(symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100);
        if (level.worldMap().hasProperty(LayerID.TERRAIN, WorldMapProperty.POS_BONUS)) {
            Vector2i bonusTile = level.worldMap().getTerrainTileProperty(WorldMapProperty.POS_BONUS, new Vector2i(13, 20));
            bonus.actor().setPosition(halfTileRightOf(bonusTile));
        } else {
            Logger.error("No bonus position found in map");
            bonus.actor().setPosition(halfTileRightOf(13, 20));
        }
        bonus.setEdibleTicks(randomInt(9 * TICKS_PER_SECOND, 10 * TICKS_PER_SECOND));
        level.setBonus(bonus);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.BONUS_ACTIVATED, bonus.actor().tile());
    }
}