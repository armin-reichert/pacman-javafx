/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.actors.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.actors.MsPacMan;
import de.amr.pacmanfx.arcade.ms_pacman.actors.Sue;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.Arcade_LevelData;
import de.amr.pacmanfx.arcade.pacman.actors.Blinky;
import de.amr.pacmanfx.arcade.pacman.actors.Inky;
import de.amr.pacmanfx.arcade.pacman.actors.Pinky;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;
import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
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

    private static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    protected final MapSelector mapSelector;
    protected final ArcadeMsPacMan_LevelCounter levelCounter;
    protected final BaseHUD hud = new BaseHUD();

    /**
     * Called via reflection by builder.
     *
     * @param coinMechanism the coin mechanism
     * @param highScoreFile the high score file
     */
    public ArcadeMsPacMan_GameModel(CoinMechanism coinMechanism, File highScoreFile) {
        this(coinMechanism, new ArcadeMsPacMan_MapSelector(), highScoreFile);
        hud.numCoinsProperty().bind(coinMechanism.numCoinsProperty());
    }

    public ArcadeMsPacMan_GameModel(CoinMechanism coinMechanism, MapSelector mapSelector, File highScoreFile) {
        super(coinMechanism);

        this.mapSelector = requireNonNull(mapSelector);

        hud.numCoinsProperty().bind(coinMechanism.numCoinsProperty());

        scoreManager.setHighScoreFile(requireNonNull(highScoreFile));
        scoreManager.setExtraLifeScores(EXTRA_LIFE_SCORE);

        levelCounter = new ArcadeMsPacMan_LevelCounter();

        gateKeeper = new GateKeeper(this);
        gateKeeper.setOnGhostReleased((gameLevel, prisoner) -> {
            Blinky blinky = (Blinky) gameLevel.ghost(RED_GHOST_SHADOW);
            Sue sue = (Sue) gameLevel.ghost(ORANGE_GHOST_POKEY);
            if (prisoner == sue && blinky.cruiseElroyValue() > 0 && !blinky.isCruiseElroyActive()) {
                blinky.setCruiseElroyActive(true);
                Logger.trace("'Cruise Elroy' mode enabled because {} exits house:", prisoner.name());
            }
        });

        demoLevelSteering = new RuleBasedPacSteering();
        autopilot = new RuleBasedPacSteering();
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
    public HUD hud() {
        return hud;
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final WorldMap worldMap = mapSelector.provideWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();
        final ArcadeHouse house = new ArcadeHouse(ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.setHouse(house);

        final GameLevel newGameLevel = new GameLevel(this, levelNumber, worldMap, new ArcadeMsPacMan_HuntingTimer());
        newGameLevel.setDemoLevel(demoLevel);
        newGameLevel.setGameOverStateTicks(150);

        final MsPacMan msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutopilotSteering(autopilot);
        newGameLevel.setPac(msPacMan);

        final Blinky blinky = ArcadeMsPacMan_ActorFactory.createBlinky();
        blinky.setHome(house);
        blinky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_1_RED)));

        final Pinky pinky = ArcadeMsPacMan_ActorFactory.createPinky();
        pinky.setHome(house);
        pinky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_2_PINK)));

        final Inky inky = ArcadeMsPacMan_ActorFactory.createInky();
        inky.setHome(house);
        inky.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_3_CYAN)));

        final Sue sue = ArcadeMsPacMan_ActorFactory.createSue();
        sue.setHome(house);
        sue.setStartPosition(halfTileRightOf(terrain.getTileProperty(POS_GHOST_4_ORANGE)));

        newGameLevel.setGhosts(blinky, pinky, inky, sue);

        newGameLevel.setBonusSymbol(0, computeBonusSymbol(levelNumber));
        newGameLevel.setBonusSymbol(1, computeBonusSymbol(levelNumber));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounter().setEnabled(levelNumber < 8);

        return newGameLevel;
    }

    /**
     * In Ms. Pac-Man, ghosts slow down in tunnel only in first two levels!
     */
    @Override
    public float ghostSpeedWhenAttacking(GameLevel gameLevel, Ghost ghost) {
        if (gameLevel.number() <= 2 && gameLevel.worldMap().terrainLayer().isTunnel(ghost.tile())) {
            return ghostSpeedInsideTunnel(gameLevel, ghost);
        }
        if (ghost instanceof Blinky blinky) {
            if (blinky.cruiseElroyValue() == 1) {
                return levelData(gameLevel).pctElroy1Speed() * BASE_SPEED_1_PERCENT;
            }
            if (blinky.cruiseElroyValue() == 2) {
                return levelData(gameLevel).pctElroy2Speed() * BASE_SPEED_1_PERCENT;
            }
        }
        return levelData(gameLevel).pctGhostSpeed() * BASE_SPEED_1_PERCENT;
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
     *
     * See also <a href="https://umlautllama.com/projects/pacdocs/mspac/mspac.asm">Ms. Pac-Man disasssembly</a>
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
        final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();

        if (gameLevel.isBonusEdible()) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final House house = terrain.optHouse().orElse(null);
        if (house == null) {
            Logger.error("Moving bonus cannot be activated, no house exists in this level!");
            return;
        }

        gameLevel.selectNextBonus();
        byte symbol = gameLevel.bonusSymbol(gameLevel.currentBonusIndex());
        var bonus = new Bonus(symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100);
        if (terrain.horizontalPortals().isEmpty()) {
            Vector2i bonusTile = terrain.getTileProperty(DefaultWorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
            bonus.setPosition(halfTileRightOf(bonusTile));
            bonus.setEdible(randomFloat(9, 10));
        } else {
            computeBonusRoute(bonus, terrain, house);
            bonus.setEdibleAndStartJumpingAtSpeed(gameLevel.game().bonusSpeed(gameLevel));
        }

        gameLevel.setBonus(bonus);
        publishGameEvent(GameEvent.Type.BONUS_ACTIVATED, bonus.tile());
    }

    private void computeBonusRoute(Bonus bonus, TerrainLayer terrain, House house) {
        final List<HPortal> portals = terrain.horizontalPortals();
        if (portals.isEmpty()) {
            Logger.error("Moving bonus cannot be activated, game level does not contain any portals");
            return;
        }

        Vector2i entryTile = terrain.getTileProperty(DefaultWorldMapPropertyName.POS_BONUS);
        Vector2i exitTile;
        boolean leftToRight;
        if (entryTile != null) { // Map defines bonus entry tile
            int exitPortalIndex = randomInt(0, portals.size());
            HPortal exitPortal = portals.get(exitPortalIndex);
            if (entryTile.x() == 0) { // enter maze at left border
                exitTile = exitPortal.rightBorderEntryTile().plus(1, 0);
                leftToRight = true;
            } else { // bonus entry is at right map border
                exitTile = exitPortal.leftBorderEntryTile().minus(1, 0);
                leftToRight = false;
            }
        }
        else { // choose random crossing direction and random entry and exit portals
            HPortal entryPortal = portals.get(randomInt(0, portals.size()));
            HPortal exitPortal = portals.get(randomInt(0, portals.size()));
            leftToRight = randomBoolean();
            if (leftToRight) {
                entryTile = entryPortal.leftBorderEntryTile();
                exitTile  = exitPortal.rightBorderEntryTile().plus(1, 0);
            } else {
                entryTile = entryPortal.rightBorderEntryTile();
                exitTile = exitPortal.leftBorderEntryTile().minus(1, 0);
            }
        }

        Vector2i houseEntry = tileAt(house.entryPosition());
        Vector2i backyard = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        List<Waypoint> route = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile).map(Waypoint::new).toList();

        bonus.initRoute(route, leftToRight);
        Logger.info("Moving bonus route: {} (crossing {})", route, leftToRight ? "left to right" : "right to left");
    }
}