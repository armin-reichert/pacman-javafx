/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.actors.*;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vec2Byte;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;
import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.Vec2Byte.vec2Byte;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomFloat;
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

    // Note: level numbering starts with 1, first entry is not used
    protected static final byte[] BONUS_SYMBOL_CODES_BY_LEVEL_NUMBER = { -1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7 };

    // bonus points = multiplier * 100
    protected static final byte[] BONUS_VALUE_MULTIPLIERS = { 1, 3, 5, 7, 10, 20, 30, 50 };

    private static final List<Vec2Byte> PAC_MAN_DEMO_LEVEL_ROUTE = List.of(
        vec2Byte(9, 26), vec2Byte(9, 29), vec2Byte(12,29), vec2Byte(12, 32), vec2Byte(26,32),
        vec2Byte(26,29), vec2Byte(24,29), vec2Byte(24,26), vec2Byte(26,26),  vec2Byte(26,23),
        vec2Byte(21,23), vec2Byte(18,23), vec2Byte(18,14), vec2Byte(9,14),   vec2Byte(9,17),
        vec2Byte(6,17),  vec2Byte(6,4),   vec2Byte(1,4),   vec2Byte(1,8),    vec2Byte(12,8),
        vec2Byte(12,4),  vec2Byte(6,4),   vec2Byte(6,11),  vec2Byte(1,11),   vec2Byte(1,8),
        vec2Byte(9,8),   vec2Byte(9,11),  vec2Byte(12,11), vec2Byte(12,14),  vec2Byte(9,14),
        vec2Byte(9,17),  vec2Byte(0,17), /*warp tunnel*/   vec2Byte(21,17),  vec2Byte(21,29),
        vec2Byte(26,29), vec2Byte(26,32), vec2Byte(1,32),  vec2Byte(1,29),   vec2Byte(3,29),
        vec2Byte(3,26),  vec2Byte(1,26),  vec2Byte(1,23),  vec2Byte(12,23),  vec2Byte(12,26),
        vec2Byte(15,26), vec2Byte(15,23), vec2Byte(26,23), vec2Byte(26,26),  vec2Byte(24,26),
        vec2Byte(24,29), vec2Byte(26,29), vec2Byte(26,32), vec2Byte(1,32),
        vec2Byte(1,29),  vec2Byte(3,29),  vec2Byte(3,26),  vec2Byte(1,26),   vec2Byte(1,23),
        vec2Byte(6,23) /* eaten at 3,23 in original game */
    );

    protected static final int FIRST_BONUS_PELLETS_EATEN = 70;
    protected static final int SECOND_BONUS_PELLETS_EATEN = 170;

    protected static final int GAME_OVER_STATE_TICKS = 90;
    protected static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    protected final MapSelector mapSelector;
    protected final ArcadePacMan_LevelCounter levelCounter;
    protected final BaseHUD hud = new BaseHUD();

    public ArcadePacMan_GameModel(CoinMechanism coinMechanism, File highScoreFile) {
        this(coinMechanism, new ArcadePacMan_MapSelector(), highScoreFile);
        hud.numCoinsProperty().bind(coinMechanism.numCoinsProperty());
    }

    /**
     * @param coinMechanism the coin mechanism
     * @param mapSelector e.g. selector that selects custom maps before standard maps
     * @param highScoreFile file where high score is stored
     */
    public ArcadePacMan_GameModel(CoinMechanism coinMechanism, MapSelector mapSelector, File highScoreFile) {
        super(coinMechanism);

        this.mapSelector = requireNonNull(mapSelector);
        mapSelector.loadAllMapPrototypes();

        levelCounter = new ArcadePacMan_LevelCounter();

        setHighScoreFile(requireNonNull(highScoreFile));
        setExtraLifeScores(EXTRA_LIFE_SCORE);

        gateKeeper = new GateKeeper(this);
        gateKeeper.setOnGhostReleased((gameLevel, prisoner) -> {
            Blinky blinky = (Blinky) gameLevel.ghost(RED_GHOST_SHADOW);
            Ghost clyde = gameLevel.ghost(ORANGE_GHOST_POKEY);
            if (prisoner == clyde && blinky.cruiseElroyValue() > 0 && !blinky.isCruiseElroyActive()) {
                Logger.debug("Re-enable Blinky 'Cruise Elroy' mode because {} got released:", prisoner.name());
                blinky.setCruiseElroyActive(true);
            }
        });

        demoLevelSteering = new RouteBasedSteering(PAC_MAN_DEMO_LEVEL_ROUTE);
        automaticSteering = new RuleBasedPacSteering();

        hud.numCoinsProperty().bind(coinMechanism.numCoinsProperty());
    }

    @Override
    public LevelData levelData(int levelNumber) {
        final int row = Math.min(levelNumber - 1, LEVEL_DATA.length - 1);
        return LEVEL_DATA[row];
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public ArcadePacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public HUD hud() {
        return hud;
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final WorldMap worldMap = mapSelector.selectWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final ArcadeHouse house = new ArcadeHouse(ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.setHouse(house);

        final int numFlashes = levelData(levelNumber).numFlashes();
        final GameLevel level = new GameLevel(this, levelNumber, worldMap, createHuntingTimer(), numFlashes);
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData(levelNumber).secPacPower());
        level.setPacPowerFadingSeconds(0.5f * numFlashes); //TODO correct?

        final PacMan pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAutomaticSteering(automaticSteering);
        level.setPac(pacMan);

        final Blinky blinky = ArcadePacMan_ActorFactory.createBlinky();
        blinky.setHome(house);
        setGhostStartPosition(blinky, terrain.getTileProperty(POS_GHOST_1_RED));

        final Pinky pinky = ArcadePacMan_ActorFactory.createPinky();
        pinky.setHome(house);
        setGhostStartPosition(pinky, terrain.getTileProperty(POS_GHOST_2_PINK));

        final Inky inky = ArcadePacMan_ActorFactory.createInky();
        inky.setHome(house);
        setGhostStartPosition(inky, terrain.getTileProperty(POS_GHOST_3_CYAN));

        final Clyde clyde = ArcadePacMan_ActorFactory.createClyde();
        clyde.setHome(house);
        setGhostStartPosition(clyde, terrain.getTileProperty(POS_GHOST_4_ORANGE));

        level.setGhosts(blinky, pinky, inky, clyde);

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .toList();
        level.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (7, "key") appears.
        byte symbol = BONUS_SYMBOL_CODES_BY_LEVEL_NUMBER[Math.min(levelNumber, 13)];
        level.setBonusSymbol(0, symbol);
        level.setBonusSymbol(1, symbol);

        levelCounter.setEnabled(true);

        return level;
    }

    private AbstractHuntingTimer createHuntingTimer() {
        final var huntingTimer = new ArcadePacMan_HuntingTimer();
        huntingTimer.phaseIndexProperty().addListener((py, ov, newPhaseIndex) -> {
            optGameLevel().ifPresent(level -> {
                if (newPhaseIndex.intValue() > 0) {
                    level.ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                        .forEach(Ghost::requestTurnBack);
                }
            });
            huntingTimer.logPhase();
        });
        return huntingTimer;
    }

    @Override
    protected boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public boolean isBonusReached() {
        final int eatenFoodCount = level().worldMap().foodLayer().eatenFoodCount();
        return eatenFoodCount == FIRST_BONUS_PELLETS_EATEN || eatenFoodCount == SECOND_BONUS_PELLETS_EATEN;
    }

    @Override
    public void activateNextBonus() {
        final GameLevel level = level();
        level.selectNextBonus();
        final byte symbol = level.bonusSymbol(level.currentBonusIndex());
        final var bonus = new Bonus(symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100);
        final Vector2i bonusTile = level.worldMap().terrainLayer()
            .getTileProperty(DefaultWorldMapPropertyName.POS_BONUS, DEFAULT_BONUS_TILE);
        bonus.setPosition(halfTileRightOf(bonusTile));
        bonus.setEdibleSeconds(randomFloat(9, 10));
        level.setBonus(bonus);
        publishGameEvent(GameEvent.Type.BONUS_ACTIVATED, bonusTile);
    }
}