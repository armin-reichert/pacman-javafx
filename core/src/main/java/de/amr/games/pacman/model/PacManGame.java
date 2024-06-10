/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.steering.RouteBasedSteering;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.GameModel.checkLevelNumber;

/**
 * Classic Arcade Pac-Man.
 *
 * <p>There are however some differences to the original.
 *     <ul>
 *         <li>Attract mode (demo level) not identical to Arcade version</li>
 *         <li>Only single player can play</li>
 *         <li>Pac-Man steering more comfortable because next direction can be selected before intersection is reached</li>
 *         <li>Cornering behavior is different</li>
 *         <li>Accuracy only about 90% (estimated) so patterns can not be used</li>
 *     </ul>
 * </p>
 *
 * @author Armin Reichert
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public class PacManGame extends AbstractPacManGame {

    private static final NavPoint[] PACMAN_DEMO_LEVEL_ROUTE = {
        np(12, 26), np(9, 26), np(12, 32), np(15, 32), np(24, 29), np(21, 23),
        np(18, 23), np(18, 20), np(18, 17), np(15, 14), np(12, 14), np(9, 17),
        np(6, 17), np(6, 11), np(6, 8), np(6, 4), np(1, 8), np(6, 8),
        np(9, 8), np(12, 8), np(6, 4), np(6, 8), np(6, 11), np(1, 8),
        np(6, 8), np(9, 8), np(12, 14), np(9, 17), np(6, 17), np(0, 17),
        np(21, 17), np(21, 23), np(21, 26), np(24, 29), /* avoid moving up: */ np(26, 29),
        np(15, 32), np(12, 32), np(3, 29), np(6, 23), np(9, 23), np(12, 26),
        np(15, 26), np(18, 23), np(21, 23), np(24, 29), /* avoid moving up: */ np(26, 29),
        np(15, 32), np(12, 32), np(3, 29), np(6, 23)
    };

    private static final int[] HUNTING_TICKS_1 = {420, 1200, 420, 1200, 300, 1200, 300, -1};
    private static final int[] HUNTING_TICKS_2_TO_4 = {420, 1200, 420, 1200, 300, 61980, 1, -1};
    private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 300, 1200, 300, 62262, 1, -1};
    private static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {-1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
    private static final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

    @Override
    public void init() {
        initialLives = 3;
        highScoreFile = new File(GAME_DIR,"highscore-pacman.xml");
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN;
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = switch (levelNumber) {
            case 1 -> HUNTING_TICKS_1[phaseIndex];
            case 2, 3, 4 -> HUNTING_TICKS_2_TO_4[phaseIndex];
            default -> HUNTING_TICKS_5_PLUS[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        setWorldAndCreatePopulation(createPacManWorld());
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    public void buildDemoLevel() {
        levelNumber = 1; // determines speed etc
        setWorldAndCreatePopulation(createPacManWorld());
        pac.setName("Pac-Man");
        pac.setAutopilot(new RouteBasedSteering(List.of(PACMAN_DEMO_LEVEL_ROUTE)));
        pac.setUseAutopilot(true);
    }

    //TODO: store some of this info in map?
    protected World createPacManWorld() {
        URL mapURL = getClass().getResource("/de/amr/games/pacman/maps/pacman.world");
        var map = new WorldMap(mapURL);
        var world = new World(map);
        world.addHouse(House.createArcadeHouse(), v2i(10, 15));
        world.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP});
        List<Direction> up = List.of(UP);
        Map<Vector2i, List<Direction>> fp = new HashMap<>();
        Stream.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26)).forEach(tile -> fp.put(tile, up));
        world.setForbiddenPassages(fp);
        world.setBonusPosition(halfTileRightOf(13, 20));
        return world;
    }

    @Override
    public void updateLevelCounter() {
        if (levelNumber == 1) {
            levelCounter.clear();
        }
        if (!demoLevel) {
            levelCounter.add(bonusSymbols[0]);
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
                levelCounter.removeFirst();
            }
        }
    }

    @Override
    public void letGhostHunt(Ghost ghost) {
        byte speed = huntingSpeedPct(ghost);
        // even phase: scattering, odd phase: chasing
        boolean chasing = isOdd(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
        ghost.followTarget(chasing ? chasingTarget(ghost) : scatterTarget(ghost), speed);
    }

    @Override
    public boolean isPacManKillingIgnoredInDemoLevel() {
        return false;
    }

    @Override
    public boolean isBonusReached() {
        return world.eatenFoodCount() == 70 || world.eatenFoodCount() == 170;
    }

    // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
    @Override
    public byte computeBonusSymbol() {
        return levelNumber > 12 ? 7 : BONUS_SYMBOLS_BY_LEVEL_NUMBER[levelNumber];
    }

    @Override
    public void createNextBonus() {
        nextBonusIndex += 1;
        byte symbol = bonusSymbols[nextBonusIndex];
        bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        bonus.entity().setPosition(world.bonusPosition());
        bonus.setEdible(randomInt(540, 600));
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
    }
}