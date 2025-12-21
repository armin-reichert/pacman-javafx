/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.actors.*;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.math.Vector2b;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;
import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.lib.math.Vector2b.vec2Byte;
import static de.amr.pacmanfx.model.world.DefaultWorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

/**
 * Classic Arcade Pac-Man.
 *
 * <p>There are however some differences to the original.
 *     <ul>
 *         <li>Only single player mode supported</li>
 *         <li>Attract mode (demo level) not identical to Arcade version because frightened ghosts move randomly</li>
 *         <li>Pac-Man steering more suitable for keyboard because next direction can be selected before intersection is reached</li>
 *         <li>Cornering not implemented as in original game, just some slowdown for ghosts going around corners</li>
 *     </ul>
 * </p>
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public class ArcadePacMan_GameModel extends Arcade_GameModel implements LevelCounter {

    public static final int MAX_LEVEL_COUNTER_SYMBOLS = 7;

    private static final List<Vector2b> PAC_MAN_DEMO_LEVEL_ROUTE = List.of(
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

    protected static final int GAME_OVER_STATE_TICKS = 90;
    protected static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    protected final MapSelector mapSelector;

    public ArcadePacMan_GameModel(CoinMechanism coinMechanism, File highScoreFile) {
        this(coinMechanism, new ArcadePacMan_MapSelector(), highScoreFile);
    }

    /**
     * @param coinMechanism the coin mechanism
     * @param mapSelector e.g. selector that selects custom maps before standard maps
     * @param highScoreFile file where high score is stored
     */
    public ArcadePacMan_GameModel(CoinMechanism coinMechanism, MapSelector mapSelector, File highScoreFile) {
        super(coinMechanism, highScoreFile);

        this.mapSelector = requireNonNull(mapSelector);

        bonus1PelletsEaten = 70;
        bonus2PelletsEaten = 170;

        this.gateKeeper = new GateKeeper();
        this.gateKeeper.setOnGhostReleased((gameLevel, prisoner) -> {
            final var blinky = (Blinky) gameLevel.ghost(RED_GHOST_SHADOW);
            if (prisoner.personality() == ORANGE_GHOST_POKEY
                && blinky.elroyMode() != Blinky.ElroyMode.NONE && !blinky.isCruiseElroyEnabled()) {
                Logger.debug("Re-enable Blinky 'Cruise Elroy' mode because {} got released:", prisoner.name());
                blinky.setCruiseElroyEnabled(true);
            }
        });

        this.demoLevelSteering = new RouteBasedSteering(PAC_MAN_DEMO_LEVEL_ROUTE);
        this.automaticSteering = new RuleBasedPacSteering();

        mapSelector.loadAllMapPrototypes();
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
    public HUD hud() {
        return hud;
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final LevelData levelData = levelData(levelNumber);

        final WorldMap worldMap = mapSelector.selectWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(POS_HOUSE_MIN_TILE, ARCADE_MAP_HOUSE_MIN_TILE);
        // Just in case, property is not set in terrain layer:
        terrain.propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));

        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final AbstractHuntingTimer huntingTimer = createHuntingTimer();
        final int numFlashes = levelData.numFlashes();

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, huntingTimer, numFlashes);
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
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

        // Each level has a single bonus symbol appearing twice during the level. From level 13 on, the same symbol
        // (code=7, "key") appears. Klingt komisch? Is aber so!
        final byte symbol = bonusSymbol(Math.min(levelNumber, 13));
        level.setBonusSymbol(0, symbol);
        level.setBonusSymbol(1, symbol);

        setLevelCounterEnabled(true);

        return level;
    }

    protected AbstractHuntingTimer createHuntingTimer() {
        final var huntingTimer = new ArcadePacMan_HuntingTimer();
        huntingTimer.phaseIndexProperty().addListener((_, _, newPhaseIndex) -> {
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
    public boolean isBonusReached(GameLevel level) {
        final int eaten = level.worldMap().foodLayer().eatenFoodCount();
        return eaten == bonus1PelletsEaten || eaten == bonus2PelletsEaten;
    }

    @Override
    public void activateNextBonus(GameLevel level) {
        level.selectNextBonus();
        final byte symbol = level.bonusSymbol(level.currentBonusIndex());
        final var bonus = new Bonus(symbol, bonusValue(symbol));
        final Vector2i bonusTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(DefaultWorldMapPropertyName.POS_BONUS, DEFAULT_BONUS_TILE);
        bonus.setPosition(halfTileRightOf(bonusTile));
        bonus.setEdibleSeconds(randomFloat(9, 10));
        level.setBonus(bonus);
        publishGameEvent(GameEvent.Type.BONUS_ACTIVATED, bonusTile);
    }

    protected int bonusValue(byte symbolCode) {
        return switch (symbolCode) {
            case 0 -> 100;  // cherries
            case 1 -> 300;  // strawberry
            case 2 -> 500;  // peach
            case 3 -> 700;  // apple
            case 4 -> 1000; // grapes
            case 5 -> 2000; // galaxian
            case 6 -> 3000; // bell
            case 7 -> 5000; // key
            default -> throw new IllegalArgumentException("Invalid symbol code: " + symbolCode);
        };
    }

    protected byte bonusSymbol(int levelNumber) {
        return switch (levelNumber) {
            case 1 -> 0;      // cherries
            case 2 -> 1;      // strawberry
            case 3, 4 -> 2;   // peach
            case 5, 6 -> 3;   // apple
            case 7, 8 -> 4;   // grapes
            case 9, 10 -> 5;  // galaxian
            case 11, 12 -> 6; // bell
            case 13 -> 7;     // key
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    // LevelCounter

    private final BooleanProperty levelCounterEnabled = new SimpleBooleanProperty(true);
    private final List<Byte> levelCounterSymbols = new ArrayList<>();

    @Override
    public void clearLevelCounter() {
        levelCounterSymbols.clear();
    }

    @Override
    public void updateLevelCounter(int levelNumber, byte symbol) {
        if (levelNumber == 1) {
            levelCounterSymbols.clear();
        }
        if (isLevelCounterEnabled()) {
            levelCounterSymbols.add(symbol);
            if (levelCounterSymbols.size() > MAX_LEVEL_COUNTER_SYMBOLS) {
                levelCounterSymbols.removeFirst();
            }
        }
    }

    @Override
    public void setLevelCounterEnabled(boolean enabled) {
        levelCounterEnabled.set(enabled);
    }

    @Override
    public boolean isLevelCounterEnabled() {
        return levelCounterEnabled.get();
    }

    @Override
    public List<Byte> levelCounterSymbols() {
        return Collections.unmodifiableList(levelCounterSymbols);
    }
}