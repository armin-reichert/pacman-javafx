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
        gateKeeper.setOnGhostReleasedAction(ghost -> {
            if (ghost.id() == ORANGE_GHOST_ID && cruiseElroy < 0) {
                Logger.trace("Re-enable cruise elroy mode because {} exits house:", ghost.name());
                setCruiseElroyEnabled(true);
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

    protected void ghostHuntingBehaviour(Ghost ghost) {
        boolean chase = huntingTimer.phase() == HuntingPhase.CHASING || ghost.id() == RED_GHOST_ID && cruiseElroy > 0;
        Vector2i targetTile = chase
            ? chasingTargetTile(level, ghost.id())
            : level.ghostScatterTile(ghost.id());
        ghost.followTarget(targetTile, speedControl.ghostAttackSpeed(level, ghost));
    }

    @Override
    public void newLevel(int levelNumber, LevelData data) {
        WorldMap worldMap = mapSelector.selectWorldMap(requireValidLevelNumber(levelNumber));
        level = new GameLevel(this, levelNumber, data, worldMap);
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

        cruiseElroy = 0;

        var ghosts = List.of(
            new Ghost(RED_GHOST_ID, "Blinky"),
            new Ghost(PINK_GHOST_ID, "Pinky"),
            new Ghost(CYAN_GHOST_ID, "Inky"),
            new Ghost(ORANGE_GHOST_ID, "Clyde"));

        ghosts.forEach(ghost -> {
            ghost.setGameLevel(level);
            ghost.setRevivalPosition(level.ghostStartPosition(ghost.id()));
            ghost.setHuntingBehaviour(this::ghostHuntingBehaviour);
            ghost.reset();
        });
        ghosts.get(RED_GHOST_ID).setRevivalPosition(level.ghostStartPosition(PINK_GHOST_ID)); // middle house position

        level.setPac(pac);
        level.setGhosts(ghosts.toArray(Ghost[]::new));
        level.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        level.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        // Pac-Man specific: special tiles where attacking ghosts cannot move up
        List<Vector2i> oneWayDownTiles = worldMap.tiles()
            .filter(tile -> worldMap.get(LayerID.TERRAIN, tile) == TerrainTiles.ONE_WAY_DOWN).toList();
        ghosts.forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        levelCounter.setEnabled(true);
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
    @Override
    public byte computeBonusSymbol(int levelNumber) {
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