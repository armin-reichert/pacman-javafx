/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.event.BonusActivatedEvent;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Elroy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapPropertyName;
import de.amr.pacmanfx.simulation.CommonGamePlay;
import de.amr.pacmanfx.simulation.GamePlayContext;
import de.amr.pacmanfx.steering.RouteBasedSteering;

import java.util.List;

import static de.amr.basics.math.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.model.world.WorldMap.tile;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GamePlay extends CommonGamePlay {

    static final List<Vector2i> DEMO_LEVEL_ROUTE = List.of(
        tile( 9,26), tile( 9,29), tile(12,29), tile(12,32), tile(26,32),
        tile(26,29), tile(24,29), tile(24,26), tile(26,26), tile(26,23),
        tile(21,23), tile(18,23), tile(18,14), tile( 9,14), tile( 9,17),
        tile( 6,17), tile( 6 ,4), tile( 1, 4), tile( 1, 8), tile(12, 8),
        tile(12, 4), tile( 6, 4), tile( 6,11), tile( 1,11), tile( 1, 8),
        tile( 9, 8), tile( 9,11), tile(12,11), tile(12,14), tile( 9,14),
        tile( 9,17), tile( 0,17), /*tunnel*/   tile(21,17), tile(21,29),
        tile(26,29), tile(26,32), tile( 1,32), tile( 1,29), tile( 3,29),
        tile( 3,26), tile( 1,26), tile( 1,23), tile(12,23), tile(12,26),
        tile(15,26), tile(15,23), tile(26,23), tile(26,26), tile(24,26),
        tile(24,29), tile(26,29), tile(26,32), tile( 1,32),
        tile( 1,29), tile( 3,29), tile( 3,26), tile( 1,26), tile( 1,23),
        tile( 6,23)
    );

    public ArcadePacMan_GamePlay() {}

    // Game start

    @Override
    public void init(GameModel model) {
        requireNonNull(model);
        model.init();
        resetForNewGame(model);
    }

    // Level building and level start

    @Override
    public GameLevel buildDemoLevel(GamePlayContext playContext) {
        requireNonNull(playContext);

        final GameModel model = playContext.model();
        final GameLevel demoLevel = model.createLevel(1, true);

        final Pac pac = demoLevel.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        final var demoLevelSteering = new RouteBasedSteering(DEMO_LEVEL_ROUTE);
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        model.gateKeeper().setLevelNumber(1);
        model.levelCounter().setEnabled(true);
        model.score().setLevelNumber(1);

        return demoLevel;
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public void startLevel(GamePlayContext playContext) {
        requireNonNull(playContext);

        final GameModel model = playContext.model();
        final GameLevel level = playContext.level();

        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(level);
        showLevelMessage(level, GameLevelMessageType.READY);
        model.levelCounter().update(level.number(), level.bonusSymbolCode(0));
        model.score().setEnabled(true);

        //TODO
        //context.cheats().update(level);
    }

    // Playing level

    @Override
    public void onEatPellet(GamePlayContext playContext, Vector2i tile) {
        super.onEatPellet(playContext, tile);
        checkRedGhostCruiseElroyActivation(playContext.level());
    }

    @Override
    public void onEatEnergizer(GamePlayContext playContext, Vector2i tile) {
        super.onEatEnergizer(playContext, tile);
        checkRedGhostCruiseElroyActivation(playContext.level());
    }

    @Override
    public void activateNextBonus(GamePlayContext playContext) {
        requireNonNull(playContext);

        final GameModel model = playContext.model();
        final GameLevel level = playContext.level();
        final GameEventManager eventManager = playContext.eventManager();

        level.selectNextBonus();
        final int bonusSymbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final Bonus bonus = new Bonus(bonusSymbolCode, model.rules().pointsForBonus(bonusSymbolCode));
        final Vector2i bonusTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, ArcadePacMan_GameModel.DEFAULT_BONUS_TILE);
        bonus.setPosition(WorldMap.halfTileRightOf(bonusTile));
        bonus.showEdibleForSeconds(randomFloat(9, 10));
        level.setBonus(bonus);

        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }

    protected void checkRedGhostCruiseElroyActivation(GameLevel level) {
        final Ghost redGhost = level.ghost(GameModel.RED_GHOST_SHADOW);
        if (redGhost != null) {
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
            final int uneatenFoodCount = level.worldMap().foodLayer().remainingFoodCount();
            if (uneatenFoodCount == data.numDotsLeftElroy1()) {
                redGhost.elroy().setBoost(Elroy.Boost.MEDIUM);
            } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
                redGhost.elroy().setBoost(Elroy.Boost.LARGE);
            }
        } else {
            throw new IllegalStateException("Red ghost not existing in this level");
        }
    }
}
