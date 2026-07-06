/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.HUDState;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Elroy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.model.world.WorldMap.tile;
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
public class ArcadePacMan_GameModel extends AbstractGameModel {

    protected static final int GAME_OVER_STATE_TICKS = 90;

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    public static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    protected final HUDState hudState = new HUDState();

    public ArcadePacMan_GameModel() {
        this(new ArcadePacMan_MapSelector());
    }

    /**
     * @param mapSelector e.g. selector that selects custom maps before standard maps
     */
    public ArcadePacMan_GameModel(WorldMapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        rules = new ArcadePacMan_GameRules();
        levelCounter = new ArcadePacMan_LevelCounter();
        automaticSteering = new RuleBasedPacSteering();
        createGateKeeper();
    }

    @Override
    public void init() {
        mapSelector().loadMapPrototypes();
        lives().setInitialCount(3);
        hudState().hideIt();
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        requireValidLevelNumber(levelNumber);

        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(POS_HOUSE_MIN_TILE, ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));

        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final LevelData levelData = ArcadePacMan_GameRules.levelData(levelNumber);
        final HuntingTimer huntingTimer = createHuntingTimer(rules);

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, huntingTimer, levelData.numFlashes());
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * levelData.numFlashes()); //TODO correct?

        createAndSetPacMan(level);
        createAndSetGhosts(level, house);

        level.setBonusSymbolCode(0, rules.selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, rules.selectBonusSymbolCode(level.number(), 1));

        levelCounter.setEnabled(true);

        return level;
    }

    @Override
    public HUDState hudState() {
        return hudState;
    }

    // helpers

    protected void createAndSetPacMan(GameLevel level) {
        final Pac pacMan = ArcadePacMan_ActorFactory.createPacMan();
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
            ArcadePacMan_ActorFactory.createGhost(RED_GHOST_SHADOW,   terrain, house, POS_GHOST_1_RED,    oneWayDownTiles),
            ArcadePacMan_ActorFactory.createGhost(PINK_GHOST_SPEEDY,  terrain, house, POS_GHOST_2_PINK,   oneWayDownTiles),
            ArcadePacMan_ActorFactory.createGhost(CYAN_GHOST_BASHFUL, terrain, house, POS_GHOST_3_CYAN,   oneWayDownTiles),
            ArcadePacMan_ActorFactory.createGhost(ORANGE_GHOST_POKEY, terrain, house, POS_GHOST_4_ORANGE, oneWayDownTiles)
        );
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
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> optGameLevel().ifPresent(level -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(Ghost::requestTurnBack);
            }
        }));
        return huntingTimer;
    }
}