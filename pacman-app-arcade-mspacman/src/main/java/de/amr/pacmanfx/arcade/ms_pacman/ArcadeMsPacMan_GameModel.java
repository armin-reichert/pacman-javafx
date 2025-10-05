/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.actors.*;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.Arcade_LevelData;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomBoolean;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
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
public class ArcadeMsPacMan_GameModel extends Arcade_GameModel {

    private static final byte[] BONUS_VALUE_MULTIPLIERS = {1, 2, 5, 7, 10, 20, 50}; // points = value * 100

    // Level settings as specified in the "Pac-Man dossier" for Pac-Man game
    // TODO: Are these values also correct for *Ms.* Pac-Man?
    protected static final Arcade_LevelData[] LEVEL_DATA = {
        /* 1*/ new Arcade_LevelData( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
        /* 2*/ new Arcade_LevelData( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
        /* 3*/ new Arcade_LevelData( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
        /* 4*/ new Arcade_LevelData( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
        /* 5*/ new Arcade_LevelData(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
        /* 6*/ new Arcade_LevelData(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
        /* 7*/ new Arcade_LevelData(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 8*/ new Arcade_LevelData(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 9*/ new Arcade_LevelData(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
        /*10*/ new Arcade_LevelData(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
        /*11*/ new Arcade_LevelData(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
        /*12*/ new Arcade_LevelData(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*13*/ new Arcade_LevelData(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*14*/ new Arcade_LevelData(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
        /*15*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*16*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*17*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
        /*18*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*19*/ new Arcade_LevelData(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*20*/ new Arcade_LevelData(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*21*/ new Arcade_LevelData( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
    };

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    protected static final Map<Integer, Integer> CUT_SCENE_AFTER_LEVEL = Map.of(
        2, 1, // after level #2, play cut scene #1
        5, 2,
        9, 3,
        13, 3,
        17, 3
    );

    protected final MapSelector mapSelector;
    protected final ArcadeMsPacMan_LevelCounter levelCounter;
    protected final HUD hud = new DefaultHUD();
    protected final HuntingTimer huntingTimer;

    /**
     * Called via reflection by builder.
     *
     * @param gameContext the game context
     * @param highScoreFile the high score file
     */
    public ArcadeMsPacMan_GameModel(GameContext gameContext, File highScoreFile) {
        this(gameContext, new ArcadeMsPacMan_MapSelector(), highScoreFile);
    }

    /**
     * @param gameContext the game context
     * @param mapSelector map selector e.g. selector that selects custom maps before standard maps
     */
    public ArcadeMsPacMan_GameModel(GameContext gameContext, MapSelector mapSelector, File highScoreFile) {
        super(gameContext);
        this.mapSelector = requireNonNull(mapSelector);

        scoreManager.setHighScoreFile(highScoreFile);
        scoreManager.setExtraLifeScores(EXTRA_LIFE_SCORE);

        levelCounter = new ArcadeMsPacMan_LevelCounter();
        /*
         * Details are from a conversation with user @damselindis on Reddit. I am not sure if they are correct.
         *
         * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
         * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
         */
        huntingTimer = new HuntingTimer("ArcadeMsPacMan-HuntingTimer", 8) {
            static final int[] HUNTING_TICKS_LEVEL_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
            static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex] : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingTimer.phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) {
                gameLevel().ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::requestTurnBack);
            }
        });

        gateKeeper = new GateKeeper(this);
        gateKeeper.setOnGhostReleased(prisoner -> {
            if (prisoner.personality() == ORANGE_GHOST_POKEY && !isCruiseElroyModeActive()) {
                Logger.trace("Re-enable 'Cruise Elroy' mode because {} exits house:", prisoner.name());
                activateCruiseElroyMode(true);
            }
        });

        demoLevelSteering = new RuleBasedPacSteering(gameContext);
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
    public ArcadeMsPacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public GameEventManager eventManager() {
        return gameContext.eventManager();
    }

    @Override
    public ScoreManager scoreManager() {
        return scoreManager;
    }

    @Override
    public HUD hud() {
        return hud;
    }

    @Override
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    @Override
    public Optional<Integer> optCutSceneNumber(int levelNumber) {
        Integer cutSceneNumber = CUT_SCENE_AFTER_LEVEL.get(levelNumber);
        return Optional.ofNullable(cutSceneNumber);
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final WorldMap worldMap = mapSelector.getWorldMapCopy(levelNumber);
        final ArcadeHouse house = new ArcadeHouse(ARCADE_MAP_HOUSE_MIN_TILE);
        worldMap.terrainLayer().setHouse(house);

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap);
        newGameLevel.setDemoLevel(demoLevel);
        newGameLevel.setGameOverStateTicks(150);

        final MsPacMan msPacMan = new MsPacMan();
        msPacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(msPacMan);

        final Blinky blinky = new Blinky();
        final Pinky pinky = new Pinky();
        final Inky inky = new Inky();
        final Sue sue = new Sue();

        blinky.setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_1_RED)));
        pinky .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_2_PINK)));
        inky  .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_3_CYAN)));
        sue   .setStartPosition(halfTileRightOf(worldMap.terrainLayer().getTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, sue);

        newGameLevel.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        newGameLevel.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounter().setEnabled(levelNumber < 8);

        return newGameLevel;
    }

    @Override
    protected boolean isPacManSafeInDemoLevel() {
        float levelDurationInSec = (System.currentTimeMillis() - gameLevel().startTime()) / 1000f;
        if (gameLevel().isDemoLevel() && levelDurationInSec < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man remains alive, demo level has just been running for {} sec", levelDurationInSec);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBonusReached(GameLevel gameLevel) {
        int eatenFoodCount = gameLevel.worldMap().foodLayer().eatenFoodCount();
        return eatenFoodCount == 64 || eatenFoodCount == 176;
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
    public void activateNextBonus(GameLevel gameLevel) {
        if (gameLevel.isBonusEdible()) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final List<Portal> portals = gameLevel.worldMap().terrainLayer().portals();
        if (portals.isEmpty()) {
            Logger.error("Moving bonus cannot be activated, game level does not contain any portals");
            return;
        }

        final House house = gameLevel.worldMap().terrainLayer().optHouse().orElse(null);
        if (house == null) {
            Logger.error("Moving bonus cannot be activated, no house exists in this level!");
            return;
        }

        Vector2i entryTile = gameLevel.worldMap().terrainLayer().getTileProperty(DefaultWorldMapPropertyName.POS_BONUS);
        Vector2i exitTile;
        boolean leftToRight;
        if (entryTile != null) { // Map defines bonus entry tile
            int exitPortalIndex = randomInt(0, portals.size());
            Portal exitPortal = portals.get(exitPortalIndex);
            if (entryTile.x() == 0) { // enter maze at left border
                exitTile = exitPortal.rightTunnelEnd().plus(1, 0);
                leftToRight = true;
            } else { // bonus entry is at right map border
                exitTile = exitPortal.leftTunnelEnd().minus(1, 0);
                leftToRight = false;
            }
        }
        else { // choose random crossing direction and random entry and exit portals
            Portal entryPortal = portals.get(randomInt(0, portals.size()));
            Portal exitPortal = portals.get(randomInt(0, portals.size()));
            leftToRight = randomBoolean();
            if (leftToRight) {
                entryTile = entryPortal.leftTunnelEnd();
                exitTile  = exitPortal.rightTunnelEnd().plus(1, 0);
            } else {
                entryTile = entryPortal.rightTunnelEnd();
                exitTile = exitPortal.leftTunnelEnd().minus(1, 0);
            }
        }

        Vector2i houseEntry = tileAt(house.entryPosition());
        Vector2i backyard = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        List<Waypoint> route = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile).map(Waypoint::new).toList();

        gameLevel.selectNextBonus();
        byte symbol = gameLevel.bonusSymbol(gameLevel.currentBonusIndex());
        Pulse jumpAnimation = new Pulse(10, Pulse.State.OFF);
        var bonus = new Bonus(symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100, jumpAnimation);
        bonus.setEdibleTicks(TickTimer.INDEFINITE);
        bonus.setEatenTicks(TickTimer.secToTicks(BONUS_EATEN_SECONDS));
        bonus.setRoute(route, leftToRight);
        bonus.setEdible();
        gameLevel.setBonus(bonus);

        Logger.info("Moving bonus created, route: {} (crossing {})", route, leftToRight ? "left to right" : "right to left");

        eventManager().publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
    }
}