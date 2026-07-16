/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.HuntingTimer;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.world.*;
import de.amr.pacmanfx.core.simulation.CommonGamePlay;
import de.amr.pacmanfx.core.steering.RouteBasedSteering;
import de.amr.pacmanfx.core.steering.RuleBasedPacSteering;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.basics.math.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory.createGhost;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.core.model.world.WorldMap.tile;
import static java.util.Objects.requireNonNull;

/**
 * Classic Arcade Pac-Man game.
 *
 * <p>There are still some differences to the original.
 *     <ul>
 *         <li>Only single player mode supported</li>
 *         <li>Attract mode (demo level) differs from original (frightened ghosts move "really" randomly)</li>
 *         <li>Pac-Man steering: Next move direction can be pre-selected before an intersection is reached</li>
 *         <li>Cornering not implemented as in original game, just some slowdown for ghosts going around corners</li>
 *     </ul>
 * </p>
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public class ArcadePacMan_GamePlay extends CommonGamePlay {

    public static final List<Vector2i> DEMO_LEVEL_ROUTE = List.of(
        tile( 9,26), tile( 9,29), tile(12,29), tile(12,32), tile(26,32),
        tile(26,29), tile(24,29), tile(24,26), tile(26,26), tile(26,23),
        tile(21,23), tile(18,23), tile(18,14), tile( 9,14), tile( 9,17),
        tile( 6,17), tile( 6 ,4), tile( 1, 4), tile( 1, 8), tile(12, 8),
        tile(12, 4), tile( 6, 4), tile( 6,11), tile( 1,11), tile( 1, 8),
        tile( 9, 8), tile( 9,11), tile(12,11), tile(12,14), tile( 9,14),
        tile( 9,17), tile( 0,17), /*tunnel*/   tile(21,17), tile(21,29),
        tile(26,29), tile(26,32), tile( 1,32), tile( 1,29), tile( 3,29),
        tile( 3,26), tile( 1,26), tile( 1,23), tile(12,23), tile(12,26),
        tile(15,26), tile(15,23), tile(26,23), tile(26,26), tile(24,26),
        tile(24,29), tile(26,29), tile(26,32), tile( 1,32),
        tile( 1,29), tile( 3,29), tile( 3,26), tile( 1,26), tile( 1,23),
        tile( 6,23)
    );

    protected static final int GAME_OVER_STATE_TICKS = 90;

    public ArcadePacMan_GamePlay() {}

    // Game start

    @Override
    public void init(GameContext gameContext) {
        requireNonNull(gameContext);
        gameContext.model().init();
        resetForNewGame(gameContext);
    }

    // Level building and level start

    @Override
    public GameLevel createLevel(GameModel model, int levelNumber, boolean demoLevel) {
        requireValidLevelNumber(levelNumber);

        final WorldMap worldMap = model.mapSelector().supplyWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(
            WorldMapPropertyName.POS_HOUSE_MIN_TILE, ArcadePacMan_GameModel.ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(WorldMapPropertyName.POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));

        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final LevelData levelData = ArcadePacMan_GameRules.levelData(levelNumber);
        final HuntingTimer huntingTimer = new HuntingTimer("Arcade Pac-Man Hunting Timer", model.rules().numHuntingPhases());

        final GameLevel level = new GameLevel(model, levelNumber, worldMap, huntingTimer, levelData.numFlashes());

        // On each phase start (except the initial phase), the ghosts reverse their move direction
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(Ghost::requestTurnBack);
            }
        });
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * levelData.numFlashes()); //TODO correct?

        createAndSetPacMan(level);
        createAndSetGhosts(level, house);

        level.setBonusSymbolCode(0, model.rules().selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, model.rules().selectBonusSymbolCode(level.number(), 1));

        model.levelCounter().setEnabled(true);

        return level;
    }

    protected void createAndSetPacMan(GameLevel level) {
        final Pac pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAutomaticSteering(new RuleBasedPacSteering());
        level.setPac(pacMan);
    }

    protected void createAndSetGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        // Special tiles where attacking ghosts cannot move up
        final Set<Vector2i> oneWayTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .collect(Collectors.toUnmodifiableSet());

        level.setGhosts(
            createGhost(GameModel.RED_GHOST_SHADOW,   terrain, house, WorldMapPropertyName.POS_GHOST_1_RED,    oneWayTiles),
            createGhost(GameModel.PINK_GHOST_SPEEDY,  terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK,   oneWayTiles),
            createGhost(GameModel.CYAN_GHOST_BASHFUL, terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN,   oneWayTiles),
            createGhost(GameModel.ORANGE_GHOST_POKEY, terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE, oneWayTiles)
        );
    }

    @Override
    public GameLevel buildDemoLevel(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameModel model = gameContext.model();
        final GameLevel demoLevel = createLevel(model, 1, true);

        final Pac pac = demoLevel.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        final var demoLevelSteering = new RouteBasedSteering(DEMO_LEVEL_ROUTE);
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        model.gateKeeper().setLevelNumber(1);
        model.levelCounter().setEnabled(true);
        model.score().setLevelNumber(1);

        return demoLevel;
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public void startLevel(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(gameContext);
        showLevelMessage(level, GameLevelMessageType.READY);
        model.levelCounter().update(level.number(), level.bonusSymbolCode(0));
        model.score().setEnabled(true);

        //TODO
        //context.cheats().update(level);
    }

    // Playing level

    @Override
    public void onEatPellet(GameContext gameContext, Vector2i tile) {
        super.onEatPellet(gameContext, tile);
        checkRedGhostCruiseElroyActivation(gameContext.assertLevel());
    }

    @Override
    public void onEatEnergizer(GameContext gameContext, Vector2i tile) {
        super.onEatEnergizer(gameContext, tile);
        checkRedGhostCruiseElroyActivation(gameContext.assertLevel());
    }

    @Override
    public void activateNextBonus(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        level.selectNextBonus();
        final int bonusSymbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final Bonus bonus = new Bonus(bonusSymbolCode, model.rules().pointsForBonus(bonusSymbolCode));
        final Vector2i bonusTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, ArcadePacMan_GameModel.DEFAULT_BONUS_TILE);
        bonus.setPosition(WorldMap.halfTileRightOf(bonusTile));
        bonus.showEdibleForSeconds(randomFloat(9, 10));
        level.setBonus(bonus);

        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }

    protected void checkRedGhostCruiseElroyActivation(GameLevel level) {
        final Ghost redGhost = level.ghost(GameModel.RED_GHOST_SHADOW);
        if (redGhost != null) {
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
            final int uneatenFoodCount = level.worldMap().foodLayer().remainingFoodCount();
            if (uneatenFoodCount == data.numDotsLeftElroy1()) {
                redGhost.elroy().setBoost(Elroy.Boost.MEDIUM);
            } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
                redGhost.elroy().setBoost(Elroy.Boost.LARGE);
            }
        } else {
            throw new IllegalStateException("Red ghost not existing in this level");
        }
    }
}
