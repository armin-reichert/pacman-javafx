/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.BonusActivatedEvent;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.basics.math.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.core.Globals.*;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
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
public class ArcadePacMan_GameModel extends Arcade_GameModel {

    public static Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    protected static final List<Vector2i> DEMO_LEVEL_ROUTE = List.of(
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

    protected static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    /**
     * @param coinMechanism the coin mechanism
     */
    public ArcadePacMan_GameModel(GameFlow flow, CoinMechanism coinMechanism) {
        this(flow, coinMechanism, new ArcadePacMan_MapSelector());
    }

    /**
     * @param coinMechanism the coin mechanism
     * @param mapSelector e.g. selector that selects custom maps before standard maps
     */
    public ArcadePacMan_GameModel(GameFlow flow, CoinMechanism coinMechanism, WorldMapSelector mapSelector) {
        super(flow);
        this.coinMechanism = requireNonNull(coinMechanism);
        hud.setCoinMechanism(coinMechanism);
        this.mapSelector = requireNonNull(mapSelector);
        levelCounter = new ArcadePacMan_LevelCounter();
        demoLevelSteering = new RouteBasedSteering(DEMO_LEVEL_ROUTE);
        automaticSteering = new RuleBasedPacSteering();
        createGateKeeper();
    }

    @Override
    public GameLevel createLevel(GameContext gameContext, int levelNumber, boolean demoLevel) {
        requireValidLevelNumber(levelNumber);

        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(POS_HOUSE_MIN_TILE, ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));

        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final LevelData levelData = ArcadePacMan_GameRules.levelData(levelNumber);
        final HuntingTimer huntingTimer = createHuntingTimer(gameContext.gameRules());

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, huntingTimer, levelData.numFlashes());
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * levelData.numFlashes()); //TODO correct?

        createAndSetPacMan(level);
        createAndSetGhosts(level, house);

        level.setBonusSymbolCode(0, gameContext.gameRules().selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, gameContext.gameRules().selectBonusSymbolCode(level.number(), 1));

        levelCounter.setEnabled(true);

        return level;
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public void activateNextBonus(GameContext gameContext, GameLevel level) {
        level.selectNextBonus();
        final int bonusSymbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final Bonus bonus = new Bonus(bonusSymbolCode, gameContext.gameRules().pointsForBonus(bonusSymbolCode));
        final Vector2i bonusTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, DEFAULT_BONUS_TILE);
        bonus.setPosition(halfTileRightOf(bonusTile));
        bonus.showEdibleForSeconds(randomFloat(9, 10));
        level.setBonus(bonus);
        flow.publishGameEvent(new BonusActivatedEvent(flow.context(), bonus));
    }

    // helpers

    protected void createAndSetPacMan(GameLevel level) {
        final Pac pacMan = createPacMan();
        pacMan.setAutomaticSteering(automaticSteering);
        level.setPac(pacMan);
    }

    protected void createAndSetGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        // Special tiles where attacking ghosts cannot move up
        final Set<Vector2i> oneWayDownTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .collect(Collectors.toUnmodifiableSet());

        level.setGhosts(
            createGhost(RED_GHOST_SHADOW,   terrain, house, POS_GHOST_1_RED,    oneWayDownTiles),
            createGhost(PINK_GHOST_SPEEDY,  terrain, house, POS_GHOST_2_PINK,   oneWayDownTiles),
            createGhost(CYAN_GHOST_BASHFUL, terrain, house, POS_GHOST_3_CYAN,   oneWayDownTiles),
            createGhost(ORANGE_GHOST_POKEY, terrain, house, POS_GHOST_4_ORANGE, oneWayDownTiles)
        );
    }

    protected Ghost createGhost(byte personality, TerrainLayer terrain, House house, String startTileProperty,
                                Set<Vector2i> specialTiles) {
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> GhostFactory.createRedGhostShadow("Blinky");
            case PINK_GHOST_SPEEDY  -> GhostFactory.createPinkGhostAmbusher("Pinky");
            case CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Clyde");
            default -> throw new IllegalArgumentException("Unknown personality: " + personality);
        };
        ghost.setHome(house);
        ghost.setSpecialTerrainTiles(specialTiles);
        setGhostStartPosition(ghost, terrain.getTileProperty(startTileProperty));
        return ghost;
    }

    protected void createGateKeeper() {
        gateKeeper = new GateKeeper();
        gateKeeper.setOnGhostReleased((level, prisoner) -> {
            if (prisoner.personality() == ORANGE_GHOST_POKEY) {
                final Ghost redGhost = level.ghost(RED_GHOST_SHADOW);
                if (redGhost.elroy().boost() != Elroy.Boost.NONE && !redGhost.elroy().enabled()) {
                    redGhost.elroy().setEnabled(true);
                    Logger.debug("Re-enabled {}'s Cruise Elroy mode because {} is released:", redGhost.name(), prisoner.name());
                }
            }
        });
    }

    protected HuntingTimer createHuntingTimer(GameRules gameRules) {
        final var huntingTimer = new HuntingTimer("Arcade Pac-Man Hunting Timer", gameRules.numHuntingPhases());
        // On each phase start (except the initial phase), the ghosts reverse their move direction
        huntingTimer.phaseIndexProperty().addListener((_, _, newPhaseIndex) -> {
            optGameLevel().ifPresent(level -> {
                if (newPhaseIndex.intValue() > 0) {
                    level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                        .forEach(Ghost::requestTurnBack);
                }
            });
            huntingTimer.logPhase();
        });
        return huntingTimer;
    }
}