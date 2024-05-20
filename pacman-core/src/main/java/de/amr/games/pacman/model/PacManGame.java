/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.RouteBasedSteering;
import de.amr.games.pacman.lib.RuleBasedPacSteering;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.Globals.randomInt;

public class PacManGame extends AbstractPacManGame{

    final int[] HUNTING_TICKS_1 = {420, 1200, 420, 1200, 300, 1200, 300, -1};
    final int[] HUNTING_TICKS_2_TO_4 = {420, 1200, 420, 1200, 300, 61980, 1, -1};
    final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 300, 1200, 300, 62262, 1, -1};
    final byte[] BONUS_SYMBOLS_BY_LEVEL_NUMBER = {-1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
    final byte[] BONUS_VALUE_FACTORS = {1, 3, 5, 7, 10, 20, 30, 50};

    public PacManGame() {
        initialLives = 3;
        highScoreFileName = "highscore-pacman.xml";
        reset();
        Logger.info("Game variant {} initialized.", this);
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN;
    }

    @Override
    public World createWorld(int mapNumber) {
        World world = createPacManWorld();
        world.setBonusPosition(halfTileRightOf(13, 20));
        return world;
    }

    @Override
    long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = switch (levelNumber) {
            case 1 -> HUNTING_TICKS_1[phaseIndex];
            case 2, 3, 4 -> HUNTING_TICKS_2_TO_4[phaseIndex];
            default -> HUNTING_TICKS_5_PLUS[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }

    @Override
    void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        mapNumber = 1;
        populateLevel(createWorld(mapNumber));
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    void buildDemoLevel() {
        levelNumber = 1;
        mapNumber = 1;
        populateLevel(createWorld(mapNumber));
        pac.setName("Pac-Man");
        pac.setAutopilot(world.getDemoLevelRoute().isEmpty()
            ? new RuleBasedPacSteering(this)
            : new RouteBasedSteering(world.getDemoLevelRoute()));
        pac.setUseAutopilot(true);
    }

    @Override
    void updateLevelCounter() {
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
    boolean isBonusReached() {
        return world.eatenFoodCount() == 70 || world.eatenFoodCount() == 170;
    }

    // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
    @Override
    byte computeBonusSymbol() {
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
