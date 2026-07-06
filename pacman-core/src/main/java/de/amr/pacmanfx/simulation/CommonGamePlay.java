/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.score.PersistentScore;
import de.amr.pacmanfx.score.Score;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Common game play functionality. Can be modofied by game-variant specific subclasses.
 */
public abstract class CommonGamePlay implements GamePlay {

    @Override
    public HuntingStepResult hunt(GameEventManager eventManager, GameLevel level) {
        final GameModel model = level.gameModel();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = model.gateKeeper();
        final boolean doubleChecked = model.rules().collisionDoubleCheckedProperty().get();

        level.heartbeat().triggerPulse();
        level.huntingTimer().update(model.rules(), level.number());

        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level);
        }

        updatePacPowerMode(eventManager, level, pac);

        final EntityCollisionDetector collisionDetector = new EntityCollisionDetector();
        // If double-check active, do an additional collision check before Pac has moved
        level.entities().forEach(entity -> {
            if (entity != pac) {
                entity.update(level, eventManager);
            }
        });
        if (doubleChecked) {
            collisionDetector.detectCollisions(level);
        }
        pac.update(level, eventManager);

        final HuntingStepResult result = collisionDetector.detectCollisions(level);
        evaluateCollisions(result, eventManager, level);

        return result;
    }

    private void evaluateCollisions(HuntingStepResult huntingStepResult, GameEventManager eventManager, GameLevel level) {
        final Pac pac = level.entities().pac();

        evalFoodFound(huntingStepResult, eventManager, level);
        if (huntingStepResult.foodFound()) {
            eventManager.publishGameEvent(new PacEatsFoodEvent(pac, huntingStepResult.energizerFound(), false));
        }

        evalBonusFound(huntingStepResult, eventManager, level);

        evalPacKilled(huntingStepResult, level);
        if (huntingStepResult.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            evalGhostsKilled(huntingStepResult, eventManager, level);
        }
    }

    private void evalFoodFound(
        HuntingStepResult huntingStepResult,
        GameEventManager eventManager,
        GameLevel level
    ) {
        final Pac pac = level.entities().pac();
        final GameModel model = level.gameModel();
        final Vector2i foodTile = huntingStepResult.foodFoundTile();

        if (!huntingStepResult.foodFound()) {
            pac.continueStarving();
            return;
        }

        pac.endStarving();

        level.worldMap().foodLayer().markFoodEatenAt(foodTile);
        if (huntingStepResult.energizerFound()) {
            onEatEnergizer(eventManager, level, foodTile);
        } else {
            onEatPellet(eventManager, level, foodTile);
        }

        if (model.rules().isBonusAwarded(level)) {
            activateNextBonus(eventManager, level);
        }
    }

    private void evalBonusFound(
        HuntingStepResult huntingStepResult,
        GameEventManager eventManager,
        GameLevel level
    ) {
        if (huntingStepResult.foundEdibleBonus()) {
            onEatBonus(eventManager, level, huntingStepResult.edibleBonus());
        }
    }

    private void evalPacKilled(HuntingStepResult huntingStepResult, GameLevel level) {
        if (level.isDemoLevel() && isPacSafeInDemoLevel(level) || level.entities().pac().isImmune()) {
            return;
        }
        huntingStepResult.ghostsCollidingWithPac().stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst().ifPresent(_ -> huntingStepResult.setPacKilled(true));
    }

    private void evalGhostsKilled(HuntingStepResult huntingStepResult, GameEventManager eventManager, GameLevel level) {
        if (huntingStepResult.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            huntingStepResult.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(huntingStepResult.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            huntingStepResult.ghostsKilled().forEach(ghost -> onEatGhost(eventManager, level, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    private void fixPacPositionIfKilledInsidePortal(GameLevel level) {
        final Pac pac = level.entities().pac();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.hPortalContainingTile(pac.computeTile()).ifPresent(hPortal -> {
            if (pac.moveDir() == Direction.LEFT) {
                pac.setX(hPortal.rightBorderEntryTile().x() * WorldMap.TS + WorldMap.HTS);
            } else if (pac.moveDir() == Direction.RIGHT) {
                pac.setX(hPortal.leftBorderEntryTile().x() * WorldMap.TS - WorldMap.HTS);
            }
            // Not sure if colliding ghosts should also be moved back to visible area
            Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
        });
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

    @Override
    public boolean isDemoLevelRunning(GameModel model) {
        return model.optGameLevel().isPresent() && model.assertLevel().isDemoLevel();
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
    public void showLevelMessage(GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
    }

    @Override
    public void onEatPellet(GameEventManager eventManager, GameLevel level, Vector2i tile) {
        requireNonNull(eventManager);
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = level.gameModel();

        scorePoints(eventManager, model, model.rules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        level.entities().pac().setRestingTicks(model.rules().restingTicksForPellet());
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

    // Scoring

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
}
