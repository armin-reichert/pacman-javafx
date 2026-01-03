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
import static de.amr.pacmanfx.lib.math.Vector2b.vector2b;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
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

    public static Blinky createBlinky() {
        return new Blinky();
    }

    public static Clyde createClyde() {
        return new Clyde();
    }

    public static Inky createInky() {
        return new Inky();
    }

    public static Pinky createPinky() {
        return new Pinky();
    }

    public static PacMan createPacMan() {
        return new PacMan();
    }

    protected static final int MAX_LEVEL_COUNTER_SYMBOLS = 7;

    protected static final List<Vector2b> DEMO_LEVEL_ROUTE = List.of(
        vector2b(9, 26), vector2b(9, 29), vector2b(12,29), vector2b(12, 32), vector2b(26,32),
        vector2b(26,29), vector2b(24,29), vector2b(24,26), vector2b(26,26),  vector2b(26,23),
        vector2b(21,23), vector2b(18,23), vector2b(18,14), vector2b(9,14),   vector2b(9,17),
        vector2b(6,17),  vector2b(6,4),   vector2b(1,4),   vector2b(1,8),    vector2b(12,8),
        vector2b(12,4),  vector2b(6,4),   vector2b(6,11),  vector2b(1,11),   vector2b(1,8),
        vector2b(9,8),   vector2b(9,11),  vector2b(12,11), vector2b(12,14),  vector2b(9,14),
        vector2b(9,17),  vector2b(0,17), /*warp tunnel*/   vector2b(21,17),  vector2b(21,29),
        vector2b(26,29), vector2b(26,32), vector2b(1,32),  vector2b(1,29),   vector2b(3,29),
        vector2b(3,26),  vector2b(1,26),  vector2b(1,23),  vector2b(12,23),  vector2b(12,26),
        vector2b(15,26), vector2b(15,23), vector2b(26,23), vector2b(26,26),  vector2b(24,26),
        vector2b(24,29), vector2b(26,29), vector2b(26,32), vector2b(1,32),
        vector2b(1,29),  vector2b(3,29),  vector2b(3,26),  vector2b(1,26),   vector2b(1,23),
        vector2b(6,23)   /* Pac-Man gets eaten at tile (3,23) in Arcade game demo level */
    );

    protected static final int GAME_OVER_STATE_TICKS = 90;

    protected static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    protected final WorldMapSelector mapSelector;

    public ArcadePacMan_GameModel(CoinMechanism coinMechanism, File highScoreFile) {
        this(coinMechanism, new ArcadePacMan_MapSelector(), highScoreFile);
    }

    /**
     * @param coinMechanism the coin mechanism
     * @param mapSelector e.g. selector that selects custom maps before standard maps
     * @param highScoreFile file where high score is stored
     */
    public ArcadePacMan_GameModel(CoinMechanism coinMechanism, WorldMapSelector mapSelector, File highScoreFile) {
        super(coinMechanism, highScoreFile);

        this.mapSelector = requireNonNull(mapSelector);

        this.gateKeeper = new GateKeeper();
        this.gateKeeper.setOnGhostReleased((level, prisoner) -> {
            if (prisoner.personality() == ORANGE_GHOST_POKEY && level.ghost(RED_GHOST_SHADOW) instanceof Blinky blinky) {
                if (blinky.elroyMode() != Blinky.ElroyMode.NONE && !blinky.isCruiseElroyEnabled()) {
                    Logger.debug("Re-enable Blinky 'Cruise Elroy' mode because {} got released:", prisoner.name());
                    blinky.setCruiseElroyEnabled(true);
                }
            }
        });

        this.demoLevelSteering = new RouteBasedSteering(DEMO_LEVEL_ROUTE);
        this.automaticSteering = new RuleBasedPacSteering();

        this.mapSelector.loadAllMapPrototypes();
    }

    @Override
    public LevelData levelData(int levelNumber) {
        final int row = Math.min(levelNumber - 1, LEVEL_DATA.length - 1);
        return LEVEL_DATA[row];
    }

    @Override
    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public HeadsUpDisplay hud() {
        return hud;
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final LevelData levelData = levelData(levelNumber);

        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber);
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

        final PacMan pacMan = createPacMan();
        pacMan.setAutomaticSteering(automaticSteering);
        level.setPac(pacMan);

        final Blinky blinky = createBlinky();
        blinky.setHome(house);
        setGhostStartPosition(blinky, terrain.getTileProperty(POS_GHOST_1_RED));

        final Pinky pinky = createPinky();
        pinky.setHome(house);
        setGhostStartPosition(pinky, terrain.getTileProperty(POS_GHOST_2_PINK));

        final Inky inky = createInky();
        inky.setHome(house);
        setGhostStartPosition(inky, terrain.getTileProperty(POS_GHOST_3_CYAN));

        final Clyde clyde = createClyde();
        clyde.setHome(house);
        setGhostStartPosition(clyde, terrain.getTileProperty(POS_GHOST_4_ORANGE));

        level.setGhosts(blinky, pinky, inky, clyde);

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .toList();
        level.ghosts().forEach(ghost -> ghost.setSpecialTerrainTiles(oneWayDownTiles));

        final int totalFoodCount = worldMap.foodLayer().totalFoodCount();
        if (totalFoodCount == 244) {
            // Original Arcade map
            bonus1PelletsEaten = 70;
            bonus2PelletsEaten = 170;
        } else {
            // XXL maps may have different food count
            bonus1PelletsEaten = totalFoodCount / 4;
            bonus2PelletsEaten = totalFoodCount * 3 / 4;
        }

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
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, DEFAULT_BONUS_TILE);
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