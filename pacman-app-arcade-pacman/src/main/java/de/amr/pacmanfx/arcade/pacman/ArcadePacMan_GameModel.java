/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.actors.*;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.Waypoint.wp;
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

    // Note: level numbering starts with 1
    protected static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = { -1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7 };

    // bonus points = multiplier * 100
    protected static final byte[] BONUS_VALUE_MULTIPLIERS = { 1, 3, 5, 7, 10, 20, 30, 50 };

    private static final List<Waypoint> PAC_MAN_DEMO_LEVEL_ROUTE = List.of(
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
    );

    protected final MapSelector mapSelector;
    protected final ArcadePacMan_LevelCounter levelCounter;
    protected final HUD hud = new BaseHUD();

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
        requireNonNull(mapSelector);
        requireNonNull(highScoreFile);

        this.mapSelector = mapSelector;

        levelCounter = new ArcadePacMan_LevelCounter();

        scoreManager.setHighScoreFile(highScoreFile);
        scoreManager.setExtraLifeScores(EXTRA_LIFE_SCORE);

        gateKeeper = new GateKeeper(this);
        gateKeeper.setOnGhostReleased((gameLevel, prisoner) -> {
            Blinky blinky = (Blinky) gameLevel.ghost(RED_GHOST_SHADOW);
            Clyde clyde = (Clyde) gameLevel.ghost(ORANGE_GHOST_POKEY);
            if (prisoner == clyde && blinky.cruiseElroyState() > 0 && !blinky.isCruiseElroyActive()) {
                Logger.debug("Re-enable 'Cruise Elroy' mode because {} got released:", prisoner.name());
                blinky.setCruiseElroyActive(true);
            }
        });

        demoLevelSteering = new RouteBasedSteering(PAC_MAN_DEMO_LEVEL_ROUTE);
        autopilot = new RuleBasedPacSteering(gameContext);

        mapSelector.loadAllMapPrototypes();
    }

    protected Arcade_LevelData levelData(int levelNumber) {
        int row = Math.min(levelNumber - 1, LEVEL_DATA.length - 1);
        return LEVEL_DATA[row];
    }

    @Override
    public Arcade_LevelData levelData(GameLevel gameLevel) {
        return levelData(gameLevel.number());
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public GameEventManager eventManager() {
        return gameContext.eventManager();
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
        final WorldMap worldMap = mapSelector.provideWorldMap(levelNumber);
        final ArcadeHouse house = new ArcadeHouse(ARCADE_MAP_HOUSE_MIN_TILE);
        worldMap.terrainLayer().setHouse(house);

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap, new ArcadePacMan_HuntingTimer());
        newGameLevel.setDemoLevel(demoLevel);
        newGameLevel.setGameOverStateTicks(90);

        final PacMan pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(pacMan);

        final Blinky blinky = ArcadePacMan_ActorFactory.createBlinky();
        blinky.setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_1_RED)));

        final Pinky pinky = ArcadePacMan_ActorFactory.createPinky();
        pinky.setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_2_PINK)));

        final Inky inky = ArcadePacMan_ActorFactory.createInky();
        inky.setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_3_CYAN)));

        final Clyde clyde = ArcadePacMan_ActorFactory.createClyde();
        clyde.setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, clyde);

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = worldMap.terrainLayer().tiles()
            .filter(tile -> worldMap.terrainLayer().get(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .toList();
        newGameLevel.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (7, "key") appears.
        byte symbol = BONUS_SYMBOLS_BY_LEVEL_NUMBER[Math.min(levelNumber, 13)];
        newGameLevel.setBonusSymbol(0, symbol);
        newGameLevel.setBonusSymbol(1, symbol);

        levelCounter.setEnabled(true);

        return newGameLevel;
    }

    @Override
    protected boolean isPacManSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public boolean isBonusReached(GameLevel gameLevel) {
        int eatenFoodCount = gameLevel.worldMap().foodLayer().eatenFoodCount();
        return eatenFoodCount == 70 || eatenFoodCount == 170;
    }

    @Override
    public void activateNextBonus(GameLevel gameLevel) {
        gameLevel.selectNextBonus();
        byte symbol = gameLevel.bonusSymbol(gameLevel.currentBonusIndex());
        var bonus = new Bonus(symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100);
        Vector2i bonusTile = gameLevel.worldMap().terrainLayer().getTileProperty(DefaultWorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
        bonus.setPosition(halfTileRightOf(bonusTile));
        bonus.setEdibleTicks(randomInt(9 * NUM_TICKS_PER_SEC, 10 * NUM_TICKS_PER_SEC));
        bonus.setEatenTicks(TickTimer.secToTicks(BONUS_EATEN_SECONDS));
        bonus.setEdible();
        gameLevel.setBonus(bonus);
        eventManager().publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
    }
}