/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.gameplay.CommonGamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.model.rules.HuntingTimer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainTile;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.core.steering.RouteGuidedSteering;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.basics.math.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tile;
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
    public void onSessionStart(GameContext game) {
        super.onSessionStart(game);

        final GameSession session = game.session();

        final HUDState hudState = session.hud();
        hudState.creditProperty().bind(game.coinMechanism().numCoinsProperty());
        hudState.hide();

        configureGateKeeper(session.gateKeeper());
    }

    // Level building and level start

    @Override
    public GameLevel createLevel(GameContext gameContext, int levelNumber, boolean demoLevel) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        final WorldNavigationSystem navigator = gameContext.systems().worldNavigator();

        final GameSession session = gameContext.session();
        final GameModel model = gameContext.model();
        final WorldMap worldMap = model.mapSelector().supplyWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(
            WorldMapPropertyName.POS_HOUSE_MIN_TILE, ArcadePacMan_GameModel.ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(WorldMapPropertyName.POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));

        final LevelData levelData = ArcadePacMan_GameRules.levelData(levelNumber);
        final HuntingTimer huntingTimer = new HuntingTimer("Arcade Pac-Man Hunting Timer", model.rules().numHuntingPhases());

        final GameLevel level = new GameLevel(model, levelNumber, worldMap, huntingTimer, levelData.numFlashes());

        session.setLevel(level);
        session.setAttractMode(demoLevel);

        final House house = HouseFactory.createArcadeHouse(houseMinTile);
        level.entities().add(house);

        // On each phase start (except the initial phase), the ghosts reverse their move direction
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(navigator::requestTurnBack);
            }
        });

        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * levelData.numFlashes()); //TODO correct?

        createAndSetPacMan(gameContext.systems(), level);
        createAndSetGhosts(level);

        level.setBonusSymbolCode(0, model.rules().selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, model.rules().selectBonusSymbolCode(level.number(), 1));

        final LivesCounter livesCounter = new LivesCounter();
        level.entities().add(livesCounter);
        // Value is set later

        level.entities().add(new MessageView());

        return level;
    }

    protected void createAndSetPacMan(GameSystems sys, GameLevel level) {
        final var factory = ArcadePacMan_ActorFactory.instance();
        final Pac pacMan = factory.createPacMan();

        pacMan.autoSteering().setSteering(new RuleGuidedPacSteering(
            sys.worldNavigator(), sys.pacWorldMovementPolicy()
        ));

        level.setPac(pacMan);
    }

    protected void createAndSetGhosts(GameLevel level) {
        final var factory = ArcadePacMan_ActorFactory.instance();

        final Ghost redGhost    = factory.createRedGhost();
        final Ghost pinkGhost   = factory.createPinkGhost();
        final Ghost cyanGhost   = factory.createCyanGhost();
        final Ghost orangeGhost = factory.createOrangeGhost();

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = level.entities().theOne(House.class);

        // Special tiles where attacking ghosts cannot move up
        final Set<Vector2i> oneWayTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .collect(Collectors.toUnmodifiableSet());

        redGhost.worldPlacement()   .init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED,    oneWayTiles);
        pinkGhost.worldPlacement()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK,   oneWayTiles);
        cyanGhost.worldPlacement()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN,   oneWayTiles);
        orangeGhost.worldPlacement().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE, oneWayTiles);

        level.setGhosts(redGhost, pinkGhost, cyanGhost, orangeGhost);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameSession session = gameContext.session();

        final GameLevel level = createLevel(gameContext, 1, true);

        final Pac pac = level.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        // Overwrite autosteering for demo level by fixed route steering
        pac.autoSteering().setSteering(new RouteGuidedSteering(
            gameContext.systems().worldNavigator(),
            gameContext.systems().pacWorldMovementPolicy(),
            DEMO_LEVEL_ROUTE
        ));

        session.setLevel(level);
        session.setAttractMode(true);
        session.gateKeeper().setLevelNumber(1);

        ScoreSystem.setLevelNumber(session.score(), 1);

        return level;
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public void startLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(game);
        showLevelMessage(game, level, GameLevelMessageType.READY);
        session.score().data().setEnabled(true);

        LevelCounterSystem.update(session.levelCounter(), level.number(), level.bonusSymbolCode(0));

        game.cheats().update(game);
    }

    // Playing level

    @Override
    public void onEatPellet(GameContext gameContext, GameLevel level, Vector2i tile) {
        super.onEatPellet(gameContext, level, tile);
        checkCruiseElroyActivation(level);
    }

    @Override
    public void onEatEnergizer(GameContext gameContext, GameLevel level, Vector2i tile) {
        super.onEatEnergizer(gameContext, level, tile);
        checkCruiseElroyActivation(level);
    }

    @Override
    public void activateNextBonus(GameContext gameContext, GameLevel level) {
        requireNonNull(gameContext);
        requireNonNull(level);

        final GameSystems sys = gameContext.systems();
        final GameModel model = gameContext.model();
        final GameEventManager eventManager = gameContext.eventManager();

        level.selectNextBonus();

        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final int value = model.rules().scoringRules().pointsForBonus(symbolCode);
        final float edibleSec = randomFloat(9, 10);
        final Vector2i tile = level.worldMap().terrainLayer().getTilePropertyOrDefault(
            WorldMapPropertyName.POS_BONUS, ArcadePacMan_GameModel.DEFAULT_BONUS_TILE);

        final Bonus bonus = Bonus.createStaticBonus(symbolCode, value);
        bonus.pos().set(WorldMap.halfTileRightOf(tile));
        sys.bonusState().showEdibleForSeconds(bonus, edibleSec);

        level.setBonus(bonus);
        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }

    protected void configureGateKeeper(ArcadeHouseGateKeeper gateKeeper) {
        gateKeeper.setGhostReleasedCallback((level, prisoner) -> {
            if (prisoner.personality() == GhostPersonality.ORANGE_GHOST_POKEY) {
                final Ghost redGhost = level.ghost(GhostPersonality.RED_GHOST_SHADOW);
                final ElroyComp elroy = redGhost.requireComp(ElroyComp.class);
                if (elroy.boost() != ElroyComp.Boost.NONE && !elroy.enabled()) {
                    elroy.setEnabled(true);
                    Logger.debug("Re-enabled {}'s Cruise Elroy mode because {} is released:", redGhost.name(), prisoner.name());
                }
            }
        });
    }


    private void checkCruiseElroyActivation(GameLevel level) {
        final Ghost redGhost = level.ghost(GhostPersonality.RED_GHOST_SHADOW);
        if (!redGhost.hasComp(ElroyComp.class)) {
            return;
        }
        final ElroyComp elroy = redGhost.requireComp(ElroyComp.class);
        final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
        final int remainingFoodCount = level.worldMap().foodLayer().remainingFoodCount();
        if (remainingFoodCount == data.numDotsLeftElroy1()) {
            elroy.setBoost(ElroyComp.Boost.MEDIUM);
        } else if (remainingFoodCount == data.numDotsLeftElroy2()) {
            elroy.setBoost(ElroyComp.Boost.LARGE);
        }
    }
}
