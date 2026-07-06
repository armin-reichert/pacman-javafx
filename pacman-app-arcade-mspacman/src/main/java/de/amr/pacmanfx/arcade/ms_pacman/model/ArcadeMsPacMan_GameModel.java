/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
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

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.model.world.WorldMap.tile;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man Arcade game.
 *
 * <p>There are slight differences to the original Arcade game.
 * <ul>
 *     <li>Attract mode is just a random hunting for at least 20 seconds.</li>
 *     <li>Timing of hunting phases unclear, just took all the information I had</li>
 *     <li>Bonus does not follow original "fruit paths" but randomly selects a portal to
 *     enter the maze, turns around the house and leaves the maze at a random portal on the other side</li>
 * </ul>
 * </p>
 */
public class ArcadeMsPacMan_GameModel extends AbstractGameModel {

    protected static final int GAME_OVER_STATE_TICKS = 150;

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    protected final HUDState hudState = new HUDState();

    protected final GateKeeper gateKeeper = new GateKeeper();

    protected WorldMapSelector mapSelector;

    protected ArcadeMsPacMan_GameRules rules;

    public ArcadeMsPacMan_GameModel() {
        this(new ArcadeMsPacMan_MapSelector());
    }

    public ArcadeMsPacMan_GameModel(WorldMapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        rules = new ArcadeMsPacMan_GameRules();
        levelCounter = new ArcadeMsPacMan_LevelCounter();
        automaticSteering = new RuleBasedPacSteering();
        configureGateKeeper();
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
        terrain.propertyMap().put(POS_HOUSE_MIN_TILE, houseMinTile.toString());

        terrain.setHouse(new ArcadeHouse(houseMinTile));

        final HuntingTimer huntingTimer = createHuntingTimer(rules);
        final int numFlashes = ArcadePacMan_GameRules.levelData(levelNumber).numFlashes();

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, huntingTimer, numFlashes);
        level.setDemoLevel(demoLevel);

        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);

        final LevelData levelData = ArcadePacMan_GameRules.levelData(levelNumber);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * numFlashes); //TODO correct?

        createAndSetMsPacMan(level);
        createAndSetGhosts(level, terrain.house());

        level.setBonusSymbolCode(0, rules.selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, rules.selectBonusSymbolCode(level.number(), 1));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounter.setEnabled(levelNumber < 8);

        return level;
    }

    @Override
    public GateKeeper gateKeeper() {
        return gateKeeper;
    }

    @Override
    public HUDState hudState() {
        return hudState;
    }

    @Override
    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public void setMapSelector(WorldMapSelector mapSelector) {
        this.mapSelector = mapSelector;
    }

    @Override
    public GameRules rules() {
        return rules;
    }

    // Helpers

    private void createAndSetMsPacMan(GameLevel level) {
        final Pac msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutomaticSteering(automaticSteering);
        level.setPac(msPacMan);
    }

    private void createAndSetGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        level.setGhosts(
            ArcadeMsPacMan_ActorFactory.createGhost(RED_GHOST_SHADOW, terrain, house, POS_GHOST_1_RED),
            ArcadeMsPacMan_ActorFactory.createGhost(PINK_GHOST_SPEEDY, terrain, house, POS_GHOST_2_PINK),
            ArcadeMsPacMan_ActorFactory.createGhost(CYAN_GHOST_BASHFUL, terrain, house, POS_GHOST_3_CYAN),
            ArcadeMsPacMan_ActorFactory.createGhost(ORANGE_GHOST_POKEY, terrain, house, POS_GHOST_4_ORANGE)
        );
    }

    private void configureGateKeeper() {
        gateKeeper.setOnGhostReleased((level, releasedPrisoner) -> {
            if (releasedPrisoner.personality() == ORANGE_GHOST_POKEY) {
                final Ghost blinky = level.ghost(RED_GHOST_SHADOW);
                if (blinky.elroy().boost() != Elroy.Boost.NONE && !blinky.elroy().enabled()) {
                    blinky.elroy().setEnabled(true);
                    Logger.debug("Re-enabled {}'s Elroy state ({}). Reason; ({} got released):",
                        blinky.name(), blinky.elroy(), releasedPrisoner.name());
                }
            }
        });
    }

    private HuntingTimer createHuntingTimer(GameRules gameRules) {
        final var huntingTimer = new HuntingTimer("Arcade Ms. Pac-Man Hunting Timer", gameRules.numHuntingPhases());
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> optGameLevel().ifPresent(level -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(Ghost::requestTurnBack);
            }
        }));
        return huntingTimer;
    }
}