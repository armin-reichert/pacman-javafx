/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.arcade.ArcadeAny_ActorSpeedControl;
import de.amr.games.pacman.arcade.ArcadeAny_GameModel;
import de.amr.games.pacman.arcade.ArcadePacMan_LevelCounter;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.Waypoint;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;
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
public class ArcadeMsPacMan_GameModel extends ArcadeAny_GameModel {

    // TODO: Are these values also correct for *Ms.* Pac-Man?
    // Level settings as specified in the "Pac-Man dossier"
    protected static final byte[][] LEVEL_DATA = {
            /* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
            /* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
            /* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
            /* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
            /* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
            /* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
            /* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
            /* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
            /* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
            /*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
            /*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
            /*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
            /*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
            /*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
            /*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
            /*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
            /*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
            /*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
            /*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
            /*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
            /*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
    };

    protected LevelData createLevelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    public ArcadeMsPacMan_GameModel() {
        this(new ArcadeMsPacMan_MapSelector());
    }

    /**
     * @param mapSelector map selector e.g. selector that selects custom maps before standard maps
     */
    protected ArcadeMsPacMan_GameModel(MapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        scoreManager.setHighScoreFile(new File(HOME_DIR, "highscore-ms_pacman.xml"));
        scoreManager.setExtraLifeScores(EXTRA_LIFE_SCORE);
        levelCounter = new ArcadePacMan_LevelCounter();

        /*
         * Details are from a conversation with user @damselindis on Reddit. I am not sure if they are correct.
         *
         * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
         * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
         */
        huntingTimer = new HuntingTimer(8) {
            static final int[] HUNTING_TICKS_LEVEL_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
            static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex] : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingTimer.phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAtNextOccasion);
        });

        gateKeeper = new GateKeeper();
        gateKeeper.setOnGhostReleased(prisoner -> {
            if (prisoner.id() == ORANGE_GHOST_ID && level.ghost(RED_GHOST_ID).cruiseElroy() < 0) {
                Logger.trace("Re-enable Blinky Cruise Elroy mode because {} exits house:", prisoner.name());
                level.ghost(RED_GHOST_ID).enableCruiseElroyMode(true);
            }
        });

        demoLevelSteering = new RuleBasedPacSteering(this);
        autopilot = new RuleBasedPacSteering(this);
    }

    @Override
    public void createLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.selectWorldMap(levelNumber);
        level = new GameLevel(this, levelNumber, worldMap);
        level.setData(createLevelData(levelNumber));
        level.setHuntingTimer(huntingTimer);
        level.setCutSceneNumber(switch (levelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        });
        level.setGameOverStateTicks(150);
        level.addArcadeHouse();

        var msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setGameLevel(level);
        msPacMan.setAutopilot(autopilot);
        level.setPac(msPacMan);

        level.setGhosts(
            ArcadeMsPacMan_ActorFactory.createRedGhost(),
            ArcadeMsPacMan_ActorFactory.createPinkGhost(),
            ArcadeMsPacMan_ActorFactory.createCyanGhost(),
            ArcadeMsPacMan_ActorFactory.createOrangeGhost()
        );
        level.ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setRevivalPosition(ghost.id() == RED_GHOST_ID
                ? level.ghostStartPosition(PINK_GHOST_ID)
                : level.ghostStartPosition(ghost.id()));
            ghost.setGameLevel(level);
        });

        level.setSpeedControl(new ArcadeAny_ActorSpeedControl());
        level.pac().setBaseSpeed(ArcadeAny_ActorSpeedControl.BASE_SPEED);
        level.ghosts().forEach(ghost -> ghost.setBaseSpeed(ArcadeAny_ActorSpeedControl.BASE_SPEED));
        Logger.debug("{} base speed: {0.00} px/tick", level.pac().name(), level.pac().baseSpeed());
        level.ghosts().forEach(ghost -> Logger.debug("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed()));

        level.setBonusSymbol(0, computeBonusSymbol(level.number()));
        level.setBonusSymbol(1, computeBonusSymbol(level.number()));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounter().setEnabled(levelNumber < 8);
    }

    @Override
    public long pacPowerFadingTicks(GameLevel level) {
        // Ghost flashing animation has frame length 14 so one full flash takes 28 ticks
        //TODO find better solution.
        return level != null ? level.data().numFlashes() * 28L : 0;
    }

    @Override
    public boolean isPacManSafeInDemoLevel() {
        float levelDurationInSec = (System.currentTimeMillis() - level.startTime()) / 1000f;
        if (level.isDemoLevel() && levelDurationInSec < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man remains alive, demo level has just been running for {} sec", levelDurationInSec);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBonusReached() {
        return level.eatenFoodCount() == 64 || level.eatenFoodCount() == 176;
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
    private byte computeBonusSymbol(int levelNumber) {
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
    public void activateNextBonus() {
        if (level.isBonusEdible()) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        level.selectNextBonus();

        List<Portal> portals = level.portals().toList();
        if (portals.isEmpty()) {
            return; // should not happen
        }

        Vector2i entryTile = level.worldMap().getTerrainTileProperty(WorldMapProperty.POS_BONUS, null);
        Vector2i exitTile;
        boolean crossingLeftToRight;
        if (entryTile != null) {
            int exitPortalIndex = THE_RNG.nextInt(portals.size());
            if (entryTile.x() == 0) { // enter maze at left border
                exitTile = portals.get(exitPortalIndex).rightTunnelEnd().plus(1, 0);
                crossingLeftToRight = true;
            } else { // enter maze  at right border
                exitTile = portals.get(exitPortalIndex).leftTunnelEnd().minus(1, 0);
                crossingLeftToRight = false;
            }
        }
        else { // choose random crossing direction and random entry and exit portals
            crossingLeftToRight = THE_RNG.nextBoolean();
            if (crossingLeftToRight) {
                entryTile = portals.get(THE_RNG.nextInt(portals.size())).leftTunnelEnd();
                exitTile  = portals.get(THE_RNG.nextInt(portals.size())).rightTunnelEnd().plus(1, 0);
            } else {
                entryTile = portals.get(THE_RNG.nextInt(portals.size())).rightTunnelEnd();
                exitTile = portals.get(THE_RNG.nextInt(portals.size())).leftTunnelEnd().minus(1, 0);
            }
        }

        Vector2i houseEntry = tileAt(level.houseEntryPosition());
        Vector2i backyard = houseEntry.plus(0, level.houseSizeInTiles().y() + 1);
        List<Waypoint> route = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile).map(Waypoint::new).toList();

        byte symbol = level.bonusSymbol(level.currentBonusIndex());
        var bonus = new MovingBonus(level, symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100);
        bonus.setEdibleTicks(TickTimer.INDEFINITE);
        bonus.setRoute(route, crossingLeftToRight);
        bonus.setBaseSpeed(ArcadeAny_ActorSpeedControl.BASE_SPEED); //TODO how fast is it really?
        Logger.info("Moving bonus created, route: {} (crossing {})", route, crossingLeftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.BONUS_ACTIVATED, bonus.actor().tile());
    }

    private final byte[] BONUS_VALUE_MULTIPLIERS = {1, 2, 5, 7, 10, 20, 50}; // points = value * 100

}