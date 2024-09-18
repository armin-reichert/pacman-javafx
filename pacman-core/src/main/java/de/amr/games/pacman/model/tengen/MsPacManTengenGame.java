/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.tengen;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Ms. Pac-Man game variant.
 *
 * <p>There are however some differences to the original.
 * <ul>
 *     <li>Attract mode is just a random hunting for at least 20 seconds.</li>
 *     <li>Timing of hunting phases unclear, just took all the information I had</li>
 *     <li>Bonus does not follow original "fruit paths" but randomly selects a portal to
 *     enter the maze, turns around the house and leaves the maze at a random portal on the other side</li>
 * </ul>
 * </p>
 *
 * @author Armin Reichert
 */
public class MsPacManTengenGame extends GameModel {

    private static final String NON_ARCADE_MAPS_PATH = "/de/amr/games/pacman/maps/tengen/non_arcade/map%02d.world";
    private static final String ARCADE_MAPS_PATH = "/de/amr/games/pacman/maps/tengen/arcade/map%02d.world";

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    /**
     * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
     *
     * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
     * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
     */
    private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    private static final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

    private final List<WorldMap> maps = new ArrayList<>();
    private int mapNumber;

    public MsPacManTengenGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;
        highScoreFile = new File(userDir, "highscore-ms_pacman_tengen.xml");
        for (int num = 1; num <= 9; ++num) {
            URL url = getClass().getResource(ARCADE_MAPS_PATH.formatted(num));
            if (url != null) {
                maps.add(new WorldMap(url));
            }
        }
        for (int num = 1; num <= 36; ++num) {
            URL url = getClass().getResource(NON_ARCADE_MAPS_PATH.formatted(num));
            if (url != null) {
                maps.add(new WorldMap(url));
            }
        }
    }

    private int mapNumberByLevelNumber(int levelNumber) {
        int numMaps = maps.size();
        return levelNumber <= numMaps ? levelNumber : Globals.randomInt(1, numMaps + 1);
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        mapNumber = mapNumberByLevelNumber(levelNumber);
        var map = maps.get(mapNumber - 1);
        setWorldAndCreatePopulation(createWorld(map));
        pac.setName("Ms. Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts[RED_GHOST].setName("Blinky");
        ghosts[PINK_GHOST].setName("Pinky");
        ghosts[CYAN_GHOST].setName("Inky");
        ghosts[ORANGE_GHOST].setName("Sue");
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void buildDemoLevel() {
        levelNumber = 1;
        mapNumber = randomInt(1, maps.size() + 1);
        setWorldAndCreatePopulation(createWorld(maps.get(mapNumber-1)));
        pac.setName("Ms. Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(true);
        ghosts[RED_GHOST].setName("Blinky");
        ghosts[PINK_GHOST].setName("Pinky");
        ghosts[CYAN_GHOST].setName("Inky");
        ghosts[ORANGE_GHOST].setName("Sue");
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    /** In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
     * (also inside a level) whenever a bonus score is reached. At least that's what I was told.
     */
    @Override
    protected boolean isLevelCounterEnabled() {
        return levelNumber < 8 && !demoLevel;
    }

    @Override
    public boolean isPacManKillingIgnoredInDemoLevel() {
        float levelRunningSeconds = (System.currentTimeMillis() - levelStartTime) / 1000f;
        if (demoLevel && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man killing ignored, demo level running for {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBonusReached() {
        return world.eatenFoodCount() == 64 || world.eatenFoodCount() == 176;
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
     * (TODO: what does never mean here? For the rest of the game?).
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
    @Override
    public byte computeBonusSymbol() {
        if (levelNumber <= 7) {
            return (byte) (levelNumber - 1);
        }
        int choice = randomInt(0, 320);
        if (choice <  50) return 0; // 5/32 probability
        if (choice < 100) return 1; // 5/32
        if (choice < 150) return 2; // 5/32
        if (choice < 200) return 3; // 5/32
        if (choice < 240) return 4; // 4/32
        if (choice < 280) return 5; // 4/32
        else              return 6; // 4/32
    }

    /**
     * Bonus symbol enters the world at a random portal, walks to the house entry, takes a tour around the
     * house and finally leaves the world through a random portal on the opposite side of the world.
     * <p>
     * Note: This is not the exact behavior from the original Arcade game.
     **/
    @Override
    public void activateNextBonus() {
        if (bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        nextBonusIndex += 1;

        boolean leftToRight = RND.nextBoolean();
        var houseEntry = tileAt(world.houseEntryPosition());
        var houseEntryOpposite = houseEntry.plus(0, world.houseSize().y() + 1);
        var portalList = world.portals().toList();
        var entryPortal = portalList.get(RND.nextInt(portalList.size()));
        var exitPortal  = portalList.get(RND.nextInt(portalList.size()));
        List<NavPoint> route = Stream.of(
            leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(NavPoint::np).toList();

        byte symbol = bonusSymbols[nextBonusIndex];
        var movingBonus = new MovingBonus(world, symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        movingBonus.setRoute(route, leftToRight);
        movingBonus.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);
        Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        bonus = movingBonus;
        bonus.setEdible(TickTimer.INDEFINITE);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    private void ghostHuntingBehaviour(Ghost ghost) {
        byte speed = huntingSpeedPct(ghost);
        if (huntingPhaseIndex == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(speed);
        } else {
            boolean chase = isChasingPhase(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
            ghost.followTarget(chase ? chasingTarget(ghost) : scatterTarget(ghost), speed);
        }
    }

    private GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        Vector2i minTile = map.terrain().getTileProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, v2i(10, 15));
        world.createArcadeHouse(minTile.x(), minTile.y());
        return world;
    }
}