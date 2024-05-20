/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.model.world.WorldMap;
import org.tinylog.Logger;

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
public class MsPacManGame extends AbstractPacManGame{

    byte DEMO_LEVEL_MIN_DURATION_SEC = 20;

    /**
     * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
     *
     * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
     * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
     */
    final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 50};

    public MsPacManGame() {
        initialLives = 3;
        highScoreFileName = "highscore-ms_pacman.xml";
        reset();
        Logger.info("Game variant {} initialized.", this);
    }

    @Override
    public GameVariant variant() {
        return GameVariant.MS_PACMAN;
    }

    /**
     * <p>In Ms. Pac-Man, there are 4 maps and 6 mazes. Let (num_map, num_maze) denote the combination
     * map #num_map and maze #num_maze.
     * </p>
     * <ul>
     * <li>Levels 1-2: (1, 1): pink maze, white dots
     * <li>Levels 3-5: (2, 2)): light blue maze, yellow dots
     * <li>Levels 6-9: (3, 3): orange maze, red dots
     * <li>Levels 10-13: (4, 4): blue maze, white dots
     * </ul>
     * For level 14 and later, (map, maze) alternates every 4th level between (3, 5) and (4, 6):
     * <ul>
     * <li>(3, 5): pink maze, cyan dots
     * <li>(4, 6): orange maze, white dots
     * </ul>
     * <p>
     */
    WorldMap mapByLevelNumber(int levelNumber) {
        checkLevelNumber(levelNumber);
        switch (levelNumber) {
            case 1, 2 -> {
                mapNumber = 1;
                return loadMap("/maps/mspacman/mspacman_1.world");
            }
            case 3, 4, 5 -> {
                mapNumber = 2;
                return loadMap("/maps/mspacman/mspacman_2.world");
            }
            case 6, 7, 8, 9 -> {
                mapNumber = 3;
                return loadMap("/maps/mspacman/mspacman_3.world");
            }
            case 10, 11, 12, 13 -> {
                mapNumber = 4;
                return loadMap("/maps/mspacman/mspacman_4.world");
            }
            default -> {
                if ((levelNumber - 14) % 8 < 4) {
                    mapNumber = 5;
                    return loadMap("/maps/mspacman/mspacman_5.world");
                } else {
                    mapNumber = 6;
                    return loadMap("/maps/mspacman/mspacman_6.world");
                }
            }
        }
    }

    // sprite sheet renderer needs this
    private int mapNumber;

    public int mapNumber() {
        return mapNumber;
    }

    @Override
    long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        setWorldAndCreatePopulation(createMsPacManWorld(mapByLevelNumber(levelNumber)));
        pac.setName("Ms. Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts[ORANGE_GHOST].setName("Sue");
    }

    @Override
    void buildDemoLevel() {
        levelNumber = 1;
        var map = loadMap(String.format("/maps/mspacman/mspacman_%d.world", randomInt(1, 7)));
        setWorldAndCreatePopulation(createMsPacManWorld(map));
        pac.setName("Ms. Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(true);
        ghosts[ORANGE_GHOST].setName("Sue");
    }

    World createMsPacManWorld(WorldMap map) {
        var world = new World(map);
        world.setHouse(createArcadeHouse());
        world.house().setTopLeftTile(v2i(10, 15));
        world.setPacPosition(halfTileRightOf(13, 26));
        world.setGhostPositions(new Vector2f[] {
            halfTileRightOf(13, 14), // red ghost
            halfTileRightOf(13, 17), // pink ghost
            halfTileRightOf(11, 17), // cyan ghost
            halfTileRightOf(15, 17)  // orange ghost
        });
        world.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP});
        world.setGhostScatterTiles(new Vector2i[] {
            v2i(25,  0), // near right-upper corner
            v2i( 2,  0), // near left-upper corner
            v2i(27, 34), // near right-lower corner
            v2i( 0, 34)  // near left-lower corner
        });
        return world;
    }

    /** In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
     * (also inside a level) whenever a bonus score is reached. At least that's what I was told.
     */
    @Override
    void updateLevelCounter() {
        if (levelNumber == 1) {
            levelCounter.clear();
        }
        if (!demoLevel && levelNumber < 8) {
            levelCounter.add(bonusSymbols[0]);
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
                levelCounter.removeFirst();
            }
        }
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    @Override
    public void letGhostHunt(Ghost ghost) {
        byte speed = huntingSpeedPct(ghost);
        if (huntingPhaseIndex == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(speed);
        } else {
            // even phase: scattering, odd phase: chasing
            boolean chasing = isOdd(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
            ghost.followTarget(chasing ? chasingTarget(ghost) : scatterTarget(ghost), speed);
        }
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
    boolean isBonusReached() {
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
    byte computeBonusSymbol() {
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
    public void createNextBonus() {
        if (bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        nextBonusIndex += 1;

        boolean leftToRight = RND.nextBoolean();
        var houseEntry = tileAt(world.house().door().entryPosition());
        var houseEntryOpposite = houseEntry.plus(0, world.house().size().y() + 1);
        var entryPortal = world.portals().get(RND.nextInt(world.portals().size()));
        var exitPortal  = world.portals().get(RND.nextInt(world.portals().size()));
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
}
