/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.pacman;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.steering.RouteBasedSteering;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;

/**
 * Classic Arcade Pac-Man.
 *
 * <p>There are however some differences to the original.
 *     <ul>
 *         <li>Only single player mode supported</li>
 *         <li>Attract mode (demo level) not identical to Arcade version because ghosts move randomly</li>
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
public class PacManGameModel extends GameModel {

    // The Pac-Man Arcade game map
    private static final WorldMap WORLD_MAP = new WorldMap(PacManGameModel.class.getResource("/de/amr/games/pacman/maps/pacman.world"));

    private static final List<Vector2i> CANNOT_MOVE_UP_TILES = List.of(
        v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26)
    );

    protected static final NavPoint[] PACMAN_DEMO_LEVEL_ROUTE = {
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

    // Ticks of scatter and chasing phases, -1 = forever
    protected static final int[] HUNTING_TICKS_1     = {420, 1200, 420, 1200, 300,  1200, 300, -1};
    protected static final int[] HUNTING_TICKS_2_3_4 = {420, 1200, 420, 1200, 300, 61980,   1, -1};
    protected static final int[] HUNTING_TICKS_5     = {300, 1200, 300, 1200, 300, 62262,   1, -1};

    // Note: First level number is 1
    protected static final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {42, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
    // Bonus value = factor * 100
    protected static final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

    protected static final Vector2f BONUS_POS = halfTileRightOf(13, 20);

    public PacManGameModel(File userDir) {
        super(userDir);
        initialLives = 3;
        highScoreFile = new File(userDir, "highscore-pacman.xml");
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN;
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        checkLevelNumber(levelNumber);
        checkHuntingPhaseIndex(phaseIndex);
        long ticks = switch (levelNumber) {
            case 1 -> HUNTING_TICKS_1[phaseIndex];
            case 2, 3, 4 -> HUNTING_TICKS_2_3_4[phaseIndex];
            default -> HUNTING_TICKS_5[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        setWorldAndCreatePopulation(createWorld());
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts[RED_GHOST].setName("Blinky");
        ghosts[PINK_GHOST].setName("Pinky");
        ghosts[CYAN_GHOST].setName("Inky");
        ghosts[ORANGE_GHOST].setName("Clyde");
        ghosts().forEach(ghost -> {
            ghost.setHuntingBehaviour(this::ghostHuntingBehaviour);
            ghost.setCannotMoveUpTiles(CANNOT_MOVE_UP_TILES);
        });
    }

    @Override
    public void buildDemoLevel() {
        buildRegularLevel(1);
        pac.setAutopilot(new RouteBasedSteering(List.of(PACMAN_DEMO_LEVEL_ROUTE)));
        pac.setUseAutopilot(true);
    }

    @Override
    protected boolean isLevelCounterEnabled() {
        return !demoLevel;
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
    public void activateNextBonus() {
        nextBonusIndex += 1;
        byte symbol = bonusSymbols[nextBonusIndex];
        bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        bonus.setEdible(bonusEdibleTicks());
        bonus.entity().setPosition(BONUS_POS);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
    }

    protected int bonusEdibleTicks() {
        return randomInt(9 * 60, 10 * 60); // 9-10 seconds
    }

    protected void ghostHuntingBehaviour(Ghost ghost) {
        boolean chasing = isChasingPhase(huntingPhaseIndex) || ghost.id() == RED_GHOST && cruiseElroy > 0;
        ghost.followTarget(chasing ? chasingTarget(ghost) : scatterTarget(ghost), huntingSpeedPct(ghost));
    }

    private GameWorld createWorld() {
        var world = new GameWorld(WORLD_MAP);
        world.createArcadeHouse(10, 15);
        return world;
    }
}