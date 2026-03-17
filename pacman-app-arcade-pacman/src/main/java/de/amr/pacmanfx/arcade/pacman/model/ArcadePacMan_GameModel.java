/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.actors.*;
import de.amr.pacmanfx.arcade.pacman.model.actors.ElroyState;
import de.amr.pacmanfx.event.BonusActivatedEvent;
import de.amr.pacmanfx.lib.math.Vector2b;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.lib.math.Vector2b.vector2b;
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

    public static final int TOTAL_FOOD_COUNT_ARCADE_PAC_MAN = 244;
    public static final int BONUS_1_PELLETS_EATEN = 70;
    public static final int BONUS_2_PELLETS_EATEN = 170;

    public static PacMan createPacMan() {
        return new PacMan();
    }

    public static Ghost createGhost(byte personality) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> new Blinky();
            case PINK_GHOST_SPEEDY -> new Pinky();
            case CYAN_GHOST_BASHFUL -> new Inky();
            case ORANGE_GHOST_POKEY -> new Clyde();
            default -> throw new IllegalArgumentException("Illegal ghost personality: %d".formatted(personality));
        };
    }

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

    protected final LevelCounter levelCounter;
    protected final WorldMapSelector mapSelector;

    /**
     * @param coinMechanism the coin mechanism
     * @param highScoreFile file where high score is stored
     */
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
        this.levelCounter = new ArcadePacMan_LevelCounter();
        this.demoLevelSteering = new RouteBasedSteering(DEMO_LEVEL_ROUTE);
        this.automaticSteering = new RuleBasedPacSteering();
        createGateKeeper();
    }

    @Override
    public LevelData levelData(int levelNumber) {
        requireValidLevelNumber(levelNumber);
        final int rowIndex = Math.min(levelNumber - 1, LEVEL_DATA_TABLE.length - 1);
        return LEVEL_DATA_TABLE[rowIndex];
    }

    @Override
    public LevelCounter levelCounter() {
        return levelCounter;
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
        requireValidLevelNumber(levelNumber);

        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(POS_HOUSE_MIN_TILE, ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(POS_HOUSE_MIN_TILE,  String.valueOf(houseMinTile));

        final ArcadeHouse house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final LevelData levelData = levelData(levelNumber);
        final AbstractHuntingTimer huntingTimer = createHuntingTimer();

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, huntingTimer, levelData.numFlashes());
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * levelData.numFlashes()); //TODO correct?

        addPacMan(level);
        addGhosts(level, house);
        initBoni(level);

        levelCounter.setEnabled(true);

        return level;
    }

    @Override
    protected boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public boolean isBonusReached(GameLevel level) {
        final int pelletsEaten = level.worldMap().foodLayer().eatenFoodCount();
        return pelletsEaten == bonus1PelletsEaten || pelletsEaten == bonus2PelletsEaten;
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
        publishGameEvent(new BonusActivatedEvent(bonus));
    }

    // helpers

    protected void addPacMan(GameLevel level) {
        final PacMan pacMan = createPacMan();
        pacMan.setAutomaticSteering(automaticSteering);
        level.setPac(pacMan);
    }

    protected void addGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        // Special tiles where attacking ghosts cannot move up
        final List<Vector2i> oneWayDownTiles = terrain.tiles()
            .filter(tile -> terrain.content(tile) == TerrainTile.ONE_WAY_DOWN.$)
            .toList();

        final Ghost blinky = createGhost(RED_GHOST_SHADOW,   terrain, house, POS_GHOST_1_RED,    oneWayDownTiles);
        final Ghost pinky  = createGhost(PINK_GHOST_SPEEDY,  terrain, house, POS_GHOST_2_PINK,   oneWayDownTiles);
        final Ghost inky   = createGhost(CYAN_GHOST_BASHFUL, terrain, house, POS_GHOST_3_CYAN,   oneWayDownTiles);
        final Ghost clyde  = createGhost(ORANGE_GHOST_POKEY, terrain, house, POS_GHOST_4_ORANGE, oneWayDownTiles);

        level.setGhosts(blinky, pinky, inky, clyde);
    }

    protected Ghost createGhost(byte personality, TerrainLayer terrain, House house, String startTileProperty, List<Vector2i> specialTiles) {
        final Ghost ghost = createGhost(personality);
        ghost.setHome(house);
        ghost.setSpecialTerrainTiles(specialTiles);
        setGhostStartPosition(ghost, terrain.getTileProperty(startTileProperty));
        return ghost;
    }

    protected void createGateKeeper() {
        gateKeeper = new GateKeeper();
        gateKeeper.setOnGhostReleased((level, prisoner) -> {
            if (prisoner.personality() == ORANGE_GHOST_POKEY) {
                if (!(level.ghost(RED_GHOST_SHADOW) instanceof Blinky blinky)) {
                    throw new IllegalStateException("Red ghost is not blinky? WTF!");
                }
                final ElroyState elroyState = blinky.elroyState();
                if (elroyState.mode() != ElroyState.Mode.ZERO && !elroyState.enabled()) {
                    elroyState.setEnabled(true);
                    Logger.debug("Re-enabled Blinky 'Cruise Elroy' mode because {} got released:", prisoner.name());
                }
            }
        });
    }

    protected AbstractHuntingTimer createHuntingTimer() {
        final var huntingTimer = new ArcadePacMan_HuntingTimer();
        // On each phase start (except the initial phase), the ghosts reverse their move direction
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

    protected void initBoni(GameLevel level) {
        final int totalFoodCount = level.worldMap().foodLayer().totalFoodCount();
        if (totalFoodCount == TOTAL_FOOD_COUNT_ARCADE_PAC_MAN) {
            // Original Arcade map
            bonus1PelletsEaten = BONUS_1_PELLETS_EATEN;
            bonus2PelletsEaten = BONUS_2_PELLETS_EATEN;
        } else {
            // XXL maps may have different food count, use heuristic values
            bonus1PelletsEaten = totalFoodCount / 4;
            bonus2PelletsEaten = totalFoodCount * 3 / 4;
        }

        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (code=7, "key") appears. Klingt komisch? Is' aber so!
        final byte symbol = bonusSymbol(Math.min(level.number(), 13));
        level.setBonusSymbol(0, symbol);
        level.setBonusSymbol(1, symbol);
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
}