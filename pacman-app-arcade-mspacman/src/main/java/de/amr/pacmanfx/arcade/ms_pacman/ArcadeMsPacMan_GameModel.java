/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.ArcadeCommon_GameModel;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
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
public class ArcadeMsPacMan_GameModel extends ArcadeCommon_GameModel {

    private static final byte[] BONUS_VALUE_MULTIPLIERS = {1, 2, 5, 7, 10, 20, 50}; // points = value * 100

    public static Pac createMsPacMan(GameContext gameContext) {
        var pac = new Pac(gameContext, "Ms. Pac-Man");
        pac.reset();
        return pac;
    }

    public static Pac createPacMan(GameContext gameContext) {
        var pac = new Pac(gameContext, "Pac-Man");
        pac.reset();
        return pac;
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     *
     * @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>.
     * */
    public static Ghost createGhost(GameContext gameContext, byte personality) {
        requireValidGhostPersonality(personality);
        return switch (personality) {

            case RED_GHOST_SHADOW -> new Ghost(gameContext, RED_GHOST_SHADOW, "Blinky") {
                @Override
                public void hunt() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    var game = gameContext.<ArcadeCommon_GameModel>game();
                    GameLevel level = gameContext.gameLevel();

                    float speed = game.actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                    setSpeed(speed);
                    if (game.huntingTimer().phaseIndex() == 0) {
                        roam();
                    } else {
                        boolean chase = game.huntingTimer().phase() == HuntingPhase.CHASING || game.isCruiseElroyModeActive();
                        Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                        tryMovingTowardsTargetTile(targetTile);
                    }
                }
                @Override
                public Vector2i chasingTargetTile() {
                    if (gameContext.optGameLevel().isEmpty()) return null;
                    // Blinky (red ghost) attacks Pac-Man directly
                    return gameContext.gameLevel().pac().tile();
                }
            };

            case PINK_GHOST_SPEEDY -> new Ghost(gameContext, PINK_GHOST_SPEEDY, "Pinky") {
                    @Override
                    public void hunt() {
                        if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                        GameLevel level = gameContext.gameLevel();

                        float speed = gameContext.game().actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                        setSpeed(speed);
                        if (gameContext.game().huntingTimer().phaseIndex() == 0) {
                            roam();
                        } else {
                            boolean chase = gameContext.game().huntingTimer().phase() == HuntingPhase.CHASING;
                            Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                            tryMovingTowardsTargetTile(targetTile);
                        }
                    }
                    @Override
                    public Vector2i chasingTargetTile() {
                        if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                        // Pinky (pink ghost) ambushes Pac-Man
                        return gameContext.gameLevel().pac().tilesAheadWithOverflowBug(4);
                    }
                };

            case CYAN_GHOST_BASHFUL -> new Ghost(gameContext, CYAN_GHOST_BASHFUL, "Inky") {
                @Override
                public void hunt() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.gameLevel();

                    float speed = gameContext.game().actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                    boolean chase = gameContext.game().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                    setSpeed(speed);
                    tryMovingTowardsTargetTile(targetTile);
                }
                @Override
                public Vector2i chasingTargetTile() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    GameLevel level = gameContext.gameLevel();
                    // Inky (cyan ghost) attacks from opposite side as Blinky
                    return level.pac().tilesAheadWithOverflowBug(2).scaled(2).minus(level.ghost(RED_GHOST_SHADOW).tile());
                }
            };
            case ORANGE_GHOST_POKEY -> new Ghost(gameContext, ORANGE_GHOST_POKEY, "Sue") {
                @Override
                public void hunt() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.gameLevel();

                    float speed = gameContext.game().actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                    boolean chase = gameContext.game().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                    setSpeed(speed);
                    tryMovingTowardsTargetTile(targetTile);
                }
                @Override
                public Vector2i chasingTargetTile() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    GameLevel level = gameContext.gameLevel();

                    // Attacks directly or retreats towards scatter target if Pac is near
                    return tile().euclideanDist(level.pac().tile()) < 8
                        ? level.ghostScatterTile(personality())
                        : level.pac().tile();
                }
            };
            default -> throw new IllegalArgumentException("Illegal ghost personality " + personality);
        };
    }

    // Level settings as specified in the "Pac-Man dossier" for Pac-Man game
    // TODO: Are these values also correct for *Ms.* Pac-Man?
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

    private final ArcadeMsPacMan_GameData hud = new ArcadeMsPacMan_GameData();
    private final ScoreManager scoreManager;

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

        scoreManager = new DefaultScoreManager(gameContext, highScoreFile);
        scoreManager.setExtraLifeScores(Set.of(EXTRA_LIFE_SCORE));

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
                level.ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::reverseAtNextOccasion);
            }
        });

        gateKeeper = new GateKeeper(this);
        gateKeeper.setOnGhostReleased(prisoner -> {
            if (prisoner.personality() == ORANGE_GHOST_POKEY && !isCruiseElroyModeActive()) {
                Logger.trace("Re-enable 'Cruise Elroy' mode because {} exits house:", prisoner.name());
                activateCruiseElroyMode(true);
            }
        });

        demoLevelSteering = new RuleBasedPacSteering(this);
        autopilot = new RuleBasedPacSteering(this);
        mapSelector.loadAllMaps();
    }

    @Override
    public ScoreManager scoreManager() {
        return scoreManager;
    }

    @Override
    public ArcadeMsPacMan_GameData hudData() {
        return hud;
    }

    @Override
    public OptionalInt cutSceneNumber(int levelNumber) {
        return switch (levelNumber) {
            case 2 -> OptionalInt.of(1);
            case 5 -> OptionalInt.of(2);
            case 9, 13, 17 -> OptionalInt.of(3);
            default -> OptionalInt.empty();
        };
    }

    @Override
    public void createLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.getWorldMap(levelNumber);
        level = new GameLevel(levelNumber, worldMap, createLevelData(levelNumber));
        level.setGameOverStateTicks(150);
        level.addHouseContent();

        level.setPac(createMsPacMan(gameContext));
        level.pac().setAutopilotSteering(autopilot);

        level.setGhosts(
            createGhost(gameContext, RED_GHOST_SHADOW),
            createGhost(gameContext, PINK_GHOST_SPEEDY),
            createGhost(gameContext, CYAN_GHOST_BASHFUL),
            createGhost(gameContext, ORANGE_GHOST_POKEY)
        );
        level.ghosts().forEach(MovingActor::reset);

        level.setBonusSymbol(0, computeBonusSymbol(level.number()));
        level.setBonusSymbol(1, computeBonusSymbol(level.number()));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        hudData().theLevelCounter().setEnabled(levelNumber < 8);
    }

    @Override
    public double pacPowerFadingSeconds(GameLevel level) {
        return level != null ? level.data().numFlashes() * 0.5 : 0;
    }

    @Override
    protected boolean isPacManSafeInDemoLevel() {
        float levelDurationInSec = (System.currentTimeMillis() - level.startTime()) / 1000f;
        if (level.isDemoLevel() && levelDurationInSec < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man remains alive, demo level has just been running for {} sec", levelDurationInSec);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isBonusReached() {
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

        if (level.portals().isEmpty()) {
            return; // should not happen
        }
        House house = level.house().orElse(null);
        if (house == null) {
            Logger.error("No house exists in this level!");
            return;
        }

        Vector2i entryTile = level.worldMap().getTerrainTileProperty(WorldMapProperty.POS_BONUS);
        Vector2i exitTile;
        boolean crossingLeftToRight;
        if (entryTile != null) {
                int exitPortalIndex = new Random().nextInt(level.portals().size());
            if (entryTile.x() == 0) { // enter maze at left border
                exitTile = level.portals().get(exitPortalIndex).rightTunnelEnd().plus(1, 0);
                crossingLeftToRight = true;
            } else { // enter maze  at right border
                exitTile = level.portals().get(exitPortalIndex).leftTunnelEnd().minus(1, 0);
                crossingLeftToRight = false;
            }
        }
        else { // choose random crossing direction and random entry and exit portals
            crossingLeftToRight = new Random().nextBoolean();
            if (crossingLeftToRight) {
                entryTile = randomPortal(level).leftTunnelEnd();
                exitTile  = randomPortal(level).rightTunnelEnd().plus(1, 0);
            } else {
                entryTile = randomPortal(level).rightTunnelEnd();
                exitTile = randomPortal(level).leftTunnelEnd().minus(1, 0);
            }
        }

        Vector2i houseEntry = tileAt(house.entryPosition());
        Vector2i backyard = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        List<Waypoint> route = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile)
            .map(Waypoint::new).toList();

        byte symbol = level.bonusSymbol(level.currentBonusIndex());
        var bonus = new Bonus(gameContext, symbol, BONUS_VALUE_MULTIPLIERS[symbol] * 100, new Pulse(10, false));
        bonus.setEdibleTicks(TickTimer.INDEFINITE);
        bonus.setRoute(route, crossingLeftToRight);
        Logger.info("Moving bonus created, route: {} (crossing {})", route,
            crossingLeftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        eventManager().publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
    }

    private Portal randomPortal(GameLevel level) {
        return level.portals().get(new Random().nextInt(level.portals().size()));
    }

    @Override
    protected GameEventManager eventManager() {
        return gameContext.eventManager();
    }
}