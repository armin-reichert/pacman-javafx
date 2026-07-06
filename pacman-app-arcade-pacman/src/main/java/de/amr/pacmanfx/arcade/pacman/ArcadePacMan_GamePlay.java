/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapPropertyName;
import de.amr.pacmanfx.score.PersistentScore;
import de.amr.pacmanfx.score.Score;
import de.amr.pacmanfx.simulation.CommonGamePlay;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
        model.mapSelector().loadMapPrototypes();
        model.lives().setInitialCount(3);
        model.hudState().hideIt();
        resetForNewGame(model);
    }

    @Override
    public boolean canStartNewGame(GameContext context) {
        return !context.coinMechanism().isEmpty();
    }

    @Override
    public void resetForNewGame(GameModel model) {
        requireNonNull(model);

        model.lives().setCount(model.lives().initialCount());
        model.score().reset();

        final PersistentScore highScore = model.highScore();
        if (highScore != null) {
            try {
                highScore.load();
                highScore.setEnabled(true);
            } catch (IOException x) {
                Logger.error(x, "Error loading high-score file {}", highScore.file().getAbsolutePath());
            }
        } else {
            Logger.error("No high-score file has been assigned");
        }

        model.gateKeeper().reset();
        model.levelCounter().clear();

        model.setLevel(null);
        model.setPlaying(false);
    }

    @Override
    public void prepareLevelForPlaying(GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = terrain.optHouse().orElseThrow();

        final Pac pac = level.entities().pac();
        pac.reset(); // initially invisible!
        pac.setPosition(terrain.pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetToIndefiniteDuration();
        pac.animations().resetSelected();

        level.entities().ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
            final Direction direction = house.ghostStartDirection(ghost.personality());
            ghost.setMoveDir(direction);
            ghost.setWishDir(direction);
            ghost.setState(GhostState.LOCKED);
            ghost.animations().resetSelected();
        });

        level.heartbeat().setStartState(Pulse.State.ON); // Energizers are visible when ON
        level.heartbeat().reset();
    }

    // Level building and level start

    @Override
    public GameLevel buildDemoLevel(GameEventManager eventManager, GameModel model) {
        requireNonNull(eventManager);
        requireNonNull(model);

        final int demoLevelNumber = 1;
        final GameLevel level = model.createLevel(demoLevelNumber, true);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        final var demoLevelSteering = new RouteBasedSteering(DEMO_LEVEL_ROUTE);
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        model.gateKeeper().setLevelNumber(demoLevelNumber);
        model.levelCounter().setEnabled(true);
        model.score().setLevelNumber(demoLevelNumber);

        return level;
    }

    @Override
    public boolean isDemoLevelRunning(GameContext context) {
        return context.model().optGameLevel().isPresent() && context.model().assertLevel().isDemoLevel();
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public void buildNormalLevel(GameEventManager eventManager, GameModel model, int levelNumber) {
        requireNonNull(eventManager);
        requireNonNull(model);
        Validations.requireValidLevelNumber(levelNumber);

        final GameLevel level = model.createLevel(levelNumber, false);
        model.levelCounter().setEnabled(true);
        model.score().setLevelNumber(levelNumber);
        model.gateKeeper().setLevelNumber(levelNumber);
        model.setLevel(level);

        eventManager.publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void startNextLevel(GameEventManager eventManager, GameLevel level) {
        requireNonNull(eventManager);
        requireNonNull(level);

        final GameModel model = level.gameModel();

        final int lastLevelNumber = model.rules().lastLevelNumber();

        if (level.number() < lastLevelNumber) {
            buildNormalLevel(eventManager, model, level.number() + 1);
            startLevel(eventManager, level);
            // Note: This event is very important because it triggers the creation of the actor animations!
            eventManager.publishGameEvent(new LevelStartedEvent(level));
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    @Override
    public void startLevel(GameEventManager eventManager, GameLevel level) {
        requireNonNull(eventManager);
        requireNonNull(level);

        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(level);
        showLevelMessage(level, GameLevelMessageType.READY);

        final GameModel model = level.gameModel();

        model.levelCounter().update(level.number(), level.bonusSymbolCode(0));
        model.score().setEnabled(true);

        //TODO
        //context.cheats().update(level);
    }

    @Override
    public void showLevelMessage(GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
    }

    // Playing level

    @Override
    public void onEatPellet(GameEventManager eventManager, GameLevel level, Vector2i tile) {
        requireNonNull(eventManager);
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = level.gameModel();

        scorePoints(eventManager, model, model.rules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        level.entities().pac().setRestingTicks(model.rules().restingTicksForPellet());
        checkRedGhostCruiseElroyActivation(level);
    }

    @Override
    public void onEatEnergizer(GameEventManager eventManager, GameLevel level, Vector2i tile) {
        requireNonNull(eventManager);
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = level.gameModel();

        scorePoints(eventManager, model, model.rules().pointsForEnergizer(), level.number());
        model.gateKeeper().registerFoodEaten(level);

        final Pac pac = level.entities().pac();
        pac.setRestingTicks(model.rules().restingTicksForEnergizer());

        checkRedGhostCruiseElroyActivation(level);

        level.clearGhostKillChain();

        startPacPowerMode(eventManager, level, pac);
    }

    @Override
    public void onEatBonus(GameEventManager eventManager, GameLevel level, Bonus bonus) {
        requireNonNull(eventManager);
        requireNonNull(level);
        requireNonNull(bonus);

        final GameModel model = level.gameModel();

        scorePoints(eventManager, model, bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(model.rules().eatenBonusDisplaySeconds());

        eventManager.publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public void onEatGhost(GameEventManager eventManager, GameLevel level, Ghost eatenGhost) {
        requireNonNull(eventManager);
        requireNonNull(level);
        requireNonNull(eatenGhost);

        final GameModel model = level.gameModel();

        final int killedBefore = level.ghostKillChainSize();
        final int points = model.rules().pointsForGhost(killedBefore);

        scorePoints(eventManager, model, points, level.number());
        Logger.info("Scored {} points for killing {} at tile {}", points, eatenGhost.name(), eatenGhost.computeTile());

        eatenGhost.setState(GhostState.EATEN);
        // Animation index is 0-based, so use animation frame 0 to show points for first killed ghost...
        eatenGhost.animations().selectAndSetFrame(ArcadePacMan_AnimationID.GHOST_POINTS, killedBefore);

        level.addToGhostKillChain(eatenGhost);
        level.entities().pac().hide();
        level.entities().ghosts().forEach(g -> g.animations().stopSelected());

        eventManager.publishGameEvent(new GhostEatenEvent(eatenGhost));
    }


    @Override
    public void activateNextBonus(GameEventManager eventManager, GameLevel level) {
        requireNonNull(eventManager);
        requireNonNull(level);

        final GameModel model = level.gameModel();

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

    @Override
    public void startPacPowerMode(GameEventManager eventManager, GameLevel level, Pac pac) {
        requireNonNull(eventManager);
        requireNonNull(level);
        requireNonNull(pac);

        level.ghostsInAnyOfStates(Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC)).forEach(MovingActor::requestTurnBack);
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            final long powerTicks = TickTimer.secToTicks(powerSeconds);
            pac.powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
            level.ghostsInState(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            eventManager.publishGameEvent(new PacGetsPowerEvent(pac));
        }
    }

    @Override
    public void updatePacPowerMode(GameEventManager eventManager, GameLevel level, Pac pac) {
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                eventManager.publishGameEvent(new PacPowerFadesEvent(pac));
            } else if (pac.powerTimer().hasExpired()) {
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.clearGhostKillChain();
                level.huntingTimer().start();
                level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                eventManager.publishGameEvent(new PacLostPowerEvent(pac));
            }
        }
    }

    @Override
    public void onLevelCompleted(GameLevel level) {
        requireNonNull(level);

        level.huntingTimer().stop();
        Logger.info("Hunting timer stopped.");

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        final Pac pac = level.entities().pac();
        pac.animations().stopSelected();
        pac.animations().select(ArcadePacMan_AnimationID.PAC_FULL);
        pac.setSpeed(0);
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero.");

        level.entities().ghosts().forEach(ghost -> {
            ghost.animations().stopSelected();
            //TODO check in emulator if ghost animation is reset to normal
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.setSpeed(0);
        });
        level.optBonus().ifPresent(Bonus::setInactive);
    }

    @Override
    public void scorePoints(GameEventManager eventManager, GameModel model, int points, int levelNumber) {
        requireNonNull(eventManager);
        requireNonNull(model);
        Validations.requireValidLevelNumber(levelNumber);

        if (!model.score().isEnabled()) {
            return;
        }
        final int oldScore = model.score().points();
        final int newScore = oldScore + points;

        if (model.rules().isExtraLifeAwarded(oldScore, newScore)) {
            model.lives().add(1);
            eventManager.publishGameEvent(new SpecialScoreEvent(newScore));
        }

        final Score highScore = model.highScore();
        if (highScore != null && highScore.isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(levelNumber);
            highScore.setDate(LocalDate.now());
        }

        model.score().setPoints(newScore);
    }

    @Override
    public void updateHighScore(GameEventManager eventManager, GameModel model) {
        final PersistentScore highScore;
        try {
            highScore = model.highScore();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (highScore == null) {
            Logger.error("Cannot update high-score, no high-score file has been assigned");
            return;
        }
        final PersistentScore savedHighScore = new PersistentScore(highScore.file());
        try {
            savedHighScore.load();
            if (highScore.points() > savedHighScore.points()) {
                highScore.save();
            }
        } catch (IOException x) {
            Logger.error(x, "Could not update high-score");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

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
