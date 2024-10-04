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
import de.amr.games.pacman.model.Portal;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Ms. Pac-Man Tengen game. Currently, this is just a copy of the Ms. Pac-Man Arcade game with the Tengen mazes.
 * <p>
 * TODO: use Tengen sprites all over the place
 * TODO: how do the ghosts in Tengen differ from the Arcade game? How can they handle non-Arcade maze structures?
 *
 * @author Armin Reichert
 */
public class MsPacManTengenGame extends GameModel {

    private static final int ARCADE_MAP_COUNT = 9;
    private static final int NON_ARCADE_MAP_COUNT = 37;
    private static final int MAP_COUNT = ARCADE_MAP_COUNT + NON_ARCADE_MAP_COUNT;

    private static final String ARCADE_MAP_PATTERN     = "/de/amr/games/pacman/maps/tengen/arcade/map%02d.world";
    private static final String NON_ARCADE_MAP_PATTERN = "/de/amr/games/pacman/maps/tengen/non_arcade/map%02d.world";

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    /**
     * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
     *
     * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
     * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
     */
    private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    private static final byte[] BONUS_VALUE_FACTORS = {1, 2, 5, 7, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};

    private static GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        // It seems that in all Tengen mazes the house is at the same location as in the Arcade mazes
        Vector2i houseTopLeftTile = map.terrain().getTileProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, v2i(10, 15));
        world.createArcadeHouse(houseTopLeftTile.x(), houseTopLeftTile.y());
        return world;
    }

    private final List<WorldMap> maps = new ArrayList<>();
    private int mapNumber;

    public MsPacManTengenGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;
        highScoreFile = new File(userDir, "highscore-ms_pacman_tengen.xml");
        for (int num = 1; num <= ARCADE_MAP_COUNT; ++num) {
            String path = ARCADE_MAP_PATTERN.formatted(num);
            URL url = getClass().getResource(path);
            if (url != null) {
                maps.add(new WorldMap(url));
            } else {
                Logger.error("Could not load Arcade map #{} using path {}", num, path);
            }
        }
        for (int num = 1; num <= NON_ARCADE_MAP_COUNT; ++num) {
            String path = NON_ARCADE_MAP_PATTERN.formatted(num);
            URL url = getClass().getResource(path);
            if (url != null) {
                maps.add(new WorldMap(url));
            } else {
                Logger.error("Could not load non-Arcade map #{} using path {}", num, path);
            }
        }
    }

    @Override
    public int currentMapNumber() {
        return mapNumber;
    }

    @Override
    public int mapCount() {
        return MAP_COUNT;
    }

    public int mapNumberByLevelNumber(int levelNumber) {
        int numMaps = maps.size();
        return levelNumber <= numMaps ? levelNumber : Globals.randomInt(1, numMaps + 1);
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    protected Pac createPac() {
        Pac msPacMan = new Pac();
        msPacMan.setName("Ms. Pac-Man");
        return msPacMan;
    }

    @Override
    protected Ghost[] createGhosts() {
        return new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = levelNumber;
        mapNumber = mapNumberByLevelNumber(levelNumber);
        var map = maps.get(mapNumber - 1);
        setWorldAndCreatePopulation(createWorld(map));
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void buildDemoLevel() {
        levelNumber = 1;
        mapNumber = mapNumberByLevelNumber(levelNumber);
        setWorldAndCreatePopulation(createWorld(maps.get(mapNumber - 1)));
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(true);
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

    @Override
    public byte computeBonusSymbol() {
        //TODO: no idea yet how Tengen does it
        if (levelNumber <= BONUS_VALUE_FACTORS.length) {
            return (byte) (levelNumber - 1);
        }
        return (byte) randomInt(0, BONUS_VALUE_FACTORS.length);
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
        Vector2i houseEntry = tileAt(world.houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, world.houseSize().y() + 1);
        List<Portal> portals = world.portals().toList();
        if (portals.isEmpty()) {
            return; // there should be no mazes without portal but who knows?
        }
        Portal entryPortal = portals.get(RND.nextInt(portals.size()));
        Portal exitPortal  = portals.get(RND.nextInt(portals.size()));
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
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
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
}