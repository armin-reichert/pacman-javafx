/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.arcade.ms_pacman.model.actors.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.model.actors.MsPacMan;
import de.amr.pacmanfx.arcade.ms_pacman.model.actors.Sue;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.arcade.pacman.model.actors.Blinky;
import de.amr.pacmanfx.arcade.pacman.model.actors.Inky;
import de.amr.pacmanfx.arcade.pacman.model.actors.Pinky;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.math.Vector2b;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;
import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
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
public class ArcadeMsPacMan_GameModel extends Arcade_GameModel implements LevelCounter {

    public static final int MAX_LEVEL_COUNTER_SYMBOLS = 7;

    private static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    protected static final int GAME_OVER_STATE_TICKS = 150;

    protected final WorldMapSelector mapSelector;

    /**
     * Called via reflection by builder.
     *
     * @param coinMechanism the coin mechanism
     * @param highScoreFile the high score file
     */
    public ArcadeMsPacMan_GameModel(CoinMechanism coinMechanism, File highScoreFile) {
        this(coinMechanism, new ArcadeMsPacMan_MapSelector(), highScoreFile);
    }

    public ArcadeMsPacMan_GameModel(CoinMechanism coinMechanism, WorldMapSelector mapSelector, File highScoreFile) {
        super(coinMechanism, highScoreFile);

        this.mapSelector = requireNonNull(mapSelector);

        bonus1PelletsEaten = 64;
        bonus2PelletsEaten = 176;

        this.gateKeeper = new GateKeeper();
        this.gateKeeper.setOnGhostReleased((level, prisoner) -> {
            final var blinky = (Blinky) level.ghost(RED_GHOST_SHADOW);
            if (prisoner.personality() == ORANGE_GHOST_POKEY
                && blinky.elroyMode() != Blinky.ElroyMode.NONE && !blinky.isCruiseElroyEnabled()) {
                blinky.setCruiseElroyEnabled(true);
                Logger.trace("'Cruise Elroy' mode enabled because {} exits house:", prisoner.name());
            }
        });

        this.demoLevelSteering = new RuleBasedPacSteering();
        this.automaticSteering = new RuleBasedPacSteering();

        mapSelector.loadAllMapPrototypes();
    }

    @Override
    public LevelData levelData(int levelNumber) {
        int row = Math.min(levelNumber - 1, LEVEL_DATA.length - 1);
        return LEVEL_DATA[row];
    }

    @Override
    public WorldMapSelector mapSelector() {
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
        // Just in case, property was not set in terrain layer
        terrain.propertyMap().put(POS_HOUSE_MIN_TILE,  houseMinTile.toString());

        final var house = new ArcadeHouse(houseMinTile);
        terrain.setHouse(house);

        final AbstractHuntingTimer huntingTimer = createHuntingTimer();
        final int numFlashes = levelData(levelNumber).numFlashes();

        final var level = new GameLevel(this, levelNumber, worldMap, huntingTimer, numFlashes);
        level.setDemoLevel(demoLevel);
        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * numFlashes); //TODO correct?

        final MsPacMan msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutomaticSteering(automaticSteering);
        level.setPac(msPacMan);

        final Blinky blinky = ArcadeMsPacMan_ActorFactory.createBlinky();
        blinky.setHome(house);
        setGhostStartPosition(blinky, terrain.getTileProperty(POS_GHOST_1_RED));

        final Pinky pinky = ArcadeMsPacMan_ActorFactory.createPinky();
        pinky.setHome(house);
        setGhostStartPosition(pinky, terrain.getTileProperty(POS_GHOST_2_PINK));

        final Inky inky = ArcadeMsPacMan_ActorFactory.createInky();
        inky.setHome(house);
        setGhostStartPosition(inky, terrain.getTileProperty(POS_GHOST_3_CYAN));

        final Sue sue = ArcadeMsPacMan_ActorFactory.createSue();
        sue.setHome(house);
        setGhostStartPosition(sue, terrain.getTileProperty(POS_GHOST_4_ORANGE));

        level.setGhosts(blinky, pinky, inky, sue);

        level.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        level.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        setLevelCounterEnabled(levelNumber < 8);

        return level;
    }

    private AbstractHuntingTimer createHuntingTimer() {
        final var huntingTimer = new ArcadeMsPacMan_HuntingTimer();
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
    public float ghostSpeed(GameLevel level, Ghost ghost) {
        final int levelNumber = level.number();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final boolean insideHouse = terrain.house().isVisitedBy(ghost);
        // In levels 3..., ghosts do not slow down in tunnel anymore!
        final boolean insideTunnel = terrain.isTunnel(ghost.tile()) && levelNumber <= 2;
        return switch (ghost.state()) {
            case LOCKED -> insideHouse ? 0.5f : 0;
            case LEAVING_HOUSE -> 0.5f;
            case HUNTING_PAC -> insideTunnel ? ghostSpeedTunnel(levelNumber) : ghostSpeedAttacking(level, ghost);
            case FRIGHTENED -> insideTunnel ? ghostSpeedTunnel(levelNumber) : ghostSpeedWhenFrightened(level);
            case EATEN -> 0;
            case RETURNING_HOME, ENTERING_HOUSE -> 2;
        };
    }

    @Override
    protected boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTimeMillis();
        if (runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS) {
            Logger.info("Pac-Man dead ignored, demo level is running since {} milliseconds", runningMillis);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBonusReached(GameLevel level) {
        final int eatenFoodCount = level.worldMap().foodLayer().eatenFoodCount();
        return eatenFoodCount == bonus1PelletsEaten || eatenFoodCount == bonus2PelletsEaten;
    }

    /**
     * <p>Got this information from
     * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>:
     * </p>
     * <p style="font-style:italic">
     * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
     * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
     * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
     * spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
     * (TODO: what does "never" mean here? For the rest of the game?).
     * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
     * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
     * the following table:
     *
     * <table>
     * <tr align="left">
     *   <th>Cherry</th><th>Strawberry</th><th>Peach</th><th>Pretzel</th><th>Apple</th><th>Pear&nbsp;</th><th>Banana</th>
     * </tr>
     * <tr align="right">
     *     <td>5/32</td><td>5/32</td><td>5/32</td><td>5/32</td><td>4/32</td><td>4/32</td><td>4/32</td>
     * </tr>
     * </table>
     * </p>
     *
     * See also <a href="https://umlautllama.com/projects/pacdocs/mspac/mspac.asm">Ms. Pac-Man disassembly</a>
     */
    protected byte computeBonusSymbol(int levelNumber) {
        if (levelNumber <= 7) return (byte) (levelNumber - 1);
        int coin = randomInt(0, 320);
        if (coin <  50) return 0; // 5/32 probability
        if (coin < 100) return 1; // 5/32
        if (coin < 150) return 2; // 5/32
        if (coin < 200) return 3; // 5/32
        if (coin < 240) return 4; // 4/32
        if (coin < 280) return 5; // 4/32
        else            return 6; // 4/32
    }

    /**
     * Bonus symbol that enters the world at some tunnel entry, walks to the house entry, takes a tour around the
     * house and finally leaves the world through a tunnel on the opposite side of the world.
     * <p>
     * Note: This is not the exact behavior from the original Arcade game that uses fruit paths.
     * <p>
     * According to <a href="https://strategywiki.org/wiki/Ms._Pac-Man/Walkthrough">this</a> Wiki,
     * some maps have a fixed entry tile for the bonus.
     * TODO: Not sure if that's correct.
     *
     **/
    @Override
    public void activateNextBonus(GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        if (level.optBonus().isPresent() && level.optBonus().get().state() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final House house = terrain.optHouse().orElse(null);
        if (house == null) {
            Logger.error("Moving bonus cannot be activated, no house exists in this level!");
            return;
        }

        level.selectNextBonus();
        final byte symbol = level.bonusSymbol(level.currentBonusIndex());
        final var bonus = new Bonus(symbol, bonusValue(symbol));
        if (terrain.horizontalPortals().isEmpty()) {
            final Vector2i bonusTile = terrain.getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
            bonus.setPosition(halfTileRightOf(bonusTile));
            bonus.setEdibleSeconds(randomFloat(9, 10));
        } else {
            computeBonusRoute(bonus, terrain, house);
            bonus.setEdibleAndStartJumpingAtSpeed(level.game().bonusSpeed(level));
        }

        level.setBonus(bonus);
        publishGameEvent(GameEvent.Type.BONUS_ACTIVATED, bonus.tile());
    }

    protected int bonusValue(byte symbolCode) {
        return switch (symbolCode) {
            case 0 -> 100;  // cherries
            case 1 -> 200;  // strawberry
            case 2 -> 500;  // orange
            case 3 -> 700;  // pretzel
            case 4 -> 1000; // apple
            case 5 -> 2000; // pear
            case 6 -> 5000; // banana
            default -> throw new IllegalArgumentException("Invalid symbol code: " + symbolCode);
        };
    }

    private void computeBonusRoute(Bonus bonus, TerrainLayer terrain, House house) {
        final List<HPortal> portals = terrain.horizontalPortals();
        if (portals.isEmpty()) {
            Logger.error("Moving bonus cannot be activated, game level does not contain any portals");
            return;
        }

        Vector2i entryTile = terrain.getTileProperty(WorldMapPropertyName.POS_BONUS);
        Vector2i exitTile;
        boolean leftToRight;
        if (entryTile != null) { // Map defines bonus entry tile
            final int exitPortalIndex = randomInt(0, portals.size());
            final HPortal exitPortal = portals.get(exitPortalIndex);
            if (entryTile.x() == 0) { // enter maze at left border
                exitTile = exitPortal.rightBorderEntryTile().plus(1, 0);
                leftToRight = true;
            } else { // bonus entry is at right map border
                exitTile = exitPortal.leftBorderEntryTile().minus(1, 0);
                leftToRight = false;
            }
        }
        else { // choose random crossing direction and random entry and exit portals
            final HPortal entryPortal = portals.get(randomInt(0, portals.size()));
            final HPortal exitPortal = portals.get(randomInt(0, portals.size()));
            leftToRight = randomBoolean();
            if (leftToRight) {
                entryTile = entryPortal.leftBorderEntryTile();
                exitTile  = exitPortal.rightBorderEntryTile().plus(1, 0);
            } else {
                entryTile = entryPortal.rightBorderEntryTile();
                exitTile = exitPortal.leftBorderEntryTile().minus(1, 0);
            }
        }

        final Vector2i houseEntry = tileAt(house.entryPosition());
        final Vector2i backyard = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        final List<Vector2b> route = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile).map(Vector2b::new).toList();

        bonus.initRoute(route, leftToRight);
        Logger.info("Moving bonus route: {} (crossing {})", route, leftToRight ? "left to right" : "right to left");
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
        if (levelNumber < MAX_LEVEL_COUNTER_SYMBOLS && isLevelCounterEnabled()) {
            levelCounterSymbols.add(symbol);
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
        return levelCounterSymbols;
    }
}