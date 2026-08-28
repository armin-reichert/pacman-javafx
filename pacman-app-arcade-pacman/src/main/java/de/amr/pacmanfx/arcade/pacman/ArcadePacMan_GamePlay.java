/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterBehavior;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.CommonGamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.core.rules.HuntingTimer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainTile;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.core.steering.RouteGuidedSteering;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.basics.math.RandomNumbers.randomFloat;
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
    public void startSession(GameContext game) {
        requireNonNull(game);
        final GameSession session = game.session();

        final int lives = game.variant().initialLifeCount();
        session.setNumLives(lives);
        session.hud().livesCounter().data().setMaxLivesShown(5);

        session.setCutScenesEnabled(true);
        session.setLevel(null);
        session.setGameRunning(false);
        initScores(session);
        configureLevelCounter(game, session.hud().levelCounter());

        game.variant().gameFlow().restartGameState(game, CommonGameStateID.BOOT);
    }

    // Level building and level start

    @Override
    public void configureLevelCounter(GameContext game, LevelCounter levelCounter) {
        final LevelCounterSystem system = game.variant().systems().levelCounterSystem();
        system.setCounterBehavior(levelCounter, LevelCounterBehavior.SHIFT_WHEN_FULL);
        system.setCounterCapacity(levelCounter, 7);
        system.clear(levelCounter);
        system.enableCounter(levelCounter, true);
    }

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameLevelEntitySet entities = new GameLevelEntitySet();

        final GameSession session = game.session();
        final WorldNavigationSystem navigator = game.variant().systems().worldNavigator();
        final WorldMap worldMap = game.variant().worldMapManager().supplyWorldMap(levelNumber);

        addEntities(entities, game, worldMap);

        final HuntingTimer huntingTimer = new HuntingTimer("Arcade Pac-Man Hunting Timer", game.variant().rules().numHuntingPhases());

        final GameLevel level = new GameLevel(levelNumber, worldMap, entities, huntingTimer);

        level.gateKeeper().setGhostReleasedCallback(this::onGhostReleasedFromHouse);

        session.setLevel(level);
        session.setGameOverStateTicks(GAME_OVER_STATE_TICKS);

        final GameRules rules = game.variant().rules();
        level.setBonusSymbolCodes(rules.bonusSymbols(levelNumber));

        // On each phase start (except the initial phase), the ghosts reverse their move direction
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                level.entities().ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(navigator::requestTurnBack);
            }
        });

        return level;
    }

    private void addEntities(GameLevelEntitySet entities, GameContext game, WorldMap worldMap) {
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(
            WorldMapPropertyName.POS_HOUSE_MIN_TILE, ArcadePacMan_GameVariantUIConfig.ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(WorldMapPropertyName.POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));

        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        final House house = HouseFactory.createArcadeHouse(houseMinTile);

        final Pac pacMan        = actorFactory.createPacMan();
        final Ghost redGhost    = actorFactory.createRedGhost();
        final Ghost pinkGhost   = actorFactory.createPinkGhost();
        final Ghost cyanGhost   = actorFactory.createCyanGhost();
        final Ghost orangeGhost = actorFactory.createOrangeGhost();

        entities.add(house);

        entities.add(pacMan);

        entities.add(redGhost);
        entities.add(pinkGhost);
        entities.add(cyanGhost);
        entities.add(orangeGhost);

        // Configure entities

        final GameSystems systems = game.variant().systems();
        pacMan.autoSteering().setSteering(new RuleGuidedPacSteering(
            systems.worldNavigator(), systems.pacWorldMovementPolicy()
        ));

        // Special tiles where attacking ghosts cannot move up
        final Set<Vector2i> oneWayTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .collect(Collectors.toUnmodifiableSet());

        redGhost.worldInfo()   .init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED,    oneWayTiles);
        pinkGhost.worldInfo()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK,   oneWayTiles);
        cyanGhost.worldInfo()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN,   oneWayTiles);
        orangeGhost.worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE, oneWayTiles);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();

        final GameLevel level = createLevel(game, 1);

        final Pac pac = level.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        // Overwrite autosteering for demo level by fixed route steering
        pac.autoSteering().setSteering(new RouteGuidedSteering<>(
            game.variant().systems().worldNavigator(),
            game.variant().systems().pacWorldMovementPolicy(),
            DEMO_LEVEL_ROUTE
        ));

        session.setLevel(level);
        session.setAttractMode(true);

        ScoreSystem.setLevelNumber(session.hud().gameScore(), 1);

        return level;
    }

    @Override
    public void startLevel(GameContext game, GameLevel level) {
        requireNonNull(game);

        final GameSession session = game.session();

        prepareLevelForPlaying(game, level);
        session.setLevelStartTimeMillis(System.currentTimeMillis());
        session.cheats().update(game);

        session.hud().gameScore().data().setEnabled(true);

        final LevelCounterSystem levelCounterSystem = game.variant().systems().levelCounterSystem();
        final LevelCounter levelCounter = session.hud().levelCounter();
        levelCounterSystem.updateCounter(levelCounter, level.number(), level.bonusSymbolCode(0));

        showMessage(game, MessageType.READY);

        // Note: This event is very important because it triggers the creation of the actor animations!
        game.eventManager().publishGameEvent(new LevelStartedEvent(level.number()));
    }

    // Playing level

    @Override
    public boolean canStart(GameContext game) {
        return !game.coinMechanism().isEmpty();
    }

    @Override
    public void activateNextBonus(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameSystems systems = game.variant().systems();
        final GameEventManager eventManager = game.eventManager();

        level.selectNextBonus();

        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final int value = game.variant().rules().scoringRules().pointsForBonus(symbolCode);
        final float edibleSec = randomFloat(9, 10);
        final Vector2i tile = level.worldMap().terrainLayer().getTilePropertyOrDefault(
            WorldMapPropertyName.POS_BONUS, ArcadePacMan_GameVariantUIConfig.DEFAULT_BONUS_TILE);

        final Bonus bonus = Bonus.createStaticBonus(symbolCode, value);
        level.entities().optBonus().ifPresent(oldBonus -> level.entities().remove(oldBonus));
        level.entities().add(bonus);

        bonus.pos().set(WorldMap.halfTileRightOf(tile));
        systems.bonusState().showEdibleForSeconds(bonus, edibleSec);

        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }

    private void onGhostReleasedFromHouse(GameLevel level, Ghost prisoner) {
        final Ghost redGhost = level.entities().ghost(GhostPersonality.RED_GHOST_SHADOW);
        // Disabled elroy mode of Blinky is re-enabled when Clyde is released from house
        redGhost.optComp(ElroyComp.class).ifPresent(elroy -> {
            if (prisoner.personality() == GhostPersonality.ORANGE_GHOST_POKEY) {
                if (elroy.boost() != ElroyComp.Boost.NONE && !elroy.enabled()) {
                    elroy.setEnabled(true);
                    Logger.debug("Re-enabled {}'s Cruise Elroy mode because {} is released:", redGhost.name(), prisoner.name());
                }
            }
        });
    }
}
