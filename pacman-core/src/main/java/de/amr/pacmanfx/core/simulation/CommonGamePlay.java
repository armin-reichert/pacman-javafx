/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.simulation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.*;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessage;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.world.*;
import de.amr.pacmanfx.core.score.PropertyFileScore;
import de.amr.pacmanfx.core.score.Score;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * Common game play functionality. Can be modified by game-variant specific subclasses.
 */
public abstract class CommonGamePlay implements GamePlay {

    @Override
    public void resetForNewGame(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameModel model = gameContext.model();

        model.lives().setCount(model.lives().initialCount());
        model.score().reset();

        final PropertyFileScore highScore = model.highScore();
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
    public void prepareLevelForPlaying(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
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
    public boolean isDemoLevelRunning(GameContext gameContext) {
        return gameContext.optLevel().isPresent() && gameContext.assertLevel().isDemoLevel();
    }

    @Override
    public void buildNormalLevel(GameContext gameContext, int levelNumber) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        final GameModel model = gameContext.model();
        final GameEventManager eventManager = gameContext.eventManager();

        final GameLevel level = createLevel(gameContext, levelNumber, false);

        model.levelCounter().setEnabled(true);
        model.score().setLevelNumber(levelNumber);
        model.gateKeeper().setLevelNumber(levelNumber);
        model.setLevel(level);

        eventManager.publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void startNextLevel(GameContext context) {
        requireNonNull(context);

        final GameModel model = context.model();
        final GameLevel level = context.assertLevel();
        final GameEventManager eventManager = context.eventManager();

        final int lastLevelNumber = model.rules().lastLevelNumber();
        if (level.number() < lastLevelNumber) {
            buildNormalLevel(context, level.number() + 1);
            startLevel(context);
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
    public void hunt(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();
        final Pac pac = level.entities().pac();
        final ArcadeHouseGateKeeper gateKeeper = model.gateKeeper();
        final boolean doubleChecked = model.rules().collisionDoubleCheckedProperty().get();

        level.heartbeat().triggerPulse();
        level.huntingTimer().update(model.rules(), level.number());

        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level);
        }

        updatePacPowerMode(gameContext, pac);

        // If double-check active, do an additional collision check before Pac has moved
        level.entities().forEach(entity -> {
            if (entity != pac) {
                entity.update(level, eventManager);
            }
        });
        if (doubleChecked) {
            detectCollisions(gameContext);
        }
        pac.update(level, eventManager);

        detectCollisions(gameContext);
        evalCollisions(gameContext);
    }

    private void evalCollisions(GameContext context) {
        final GameLevel level = context.assertLevel();
        final HuntingStepResult result = context.thisFrame().huntingStepResult();
        evalFoodFound(context);

        if (result.foundEdibleBonus()) {
            onEatBonus(context, result.edibleBonus());
        }

        evalPacKilled(result, level);
        if (result.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            evalGhostsKilled(context, result);
        }
    }

    private void evalFoodFound(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = context.assertLevel();
        final GameEventManager eventManager = context.eventManager();
        final Pac pac = level.entities().pac();
        final HuntingStepResult hunting = context.thisFrame().huntingStepResult();

        if (!hunting.foodFound()) {
            pac.continueStarving();
            return;
        }

        pac.endStarving();

        final Vector2i foodTile = hunting.foodFoundTile();
        level.worldMap().foodLayer().markFoodEatenAt(foodTile);

        if (hunting.energizerFound()) {
            onEatEnergizer(context, foodTile);
        } else {
            onEatPellet(context, foodTile);
        }

        eventManager.publishGameEvent(new PacEatsFoodEvent(pac, hunting.energizerFound(), false));

        if (model.rules().isBonusAwarded(level)) {
            activateNextBonus(context);
        }
    }

    private void evalPacKilled(HuntingStepResult result, GameLevel level) {
        if (level.isDemoLevel() && isPacSafeInDemoLevel(level) || level.entities().pac().isImmune()) {
            return;
        }
        result.setPacKilled(
            result.ghostsCollidingWithPac().stream().anyMatch(ghost -> ghost.state() == GhostState.HUNTING_PAC)
        );
    }

    private void evalGhostsKilled(GameContext context, HuntingStepResult result) {
        if (result.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            result.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(result.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            result.ghostsKilled().forEach(ghost -> onEatGhost(context, ghost));
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
    public void onEatPellet(GameContext gameContext, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(tile);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        scorePoints(gameContext, model.rules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        level.entities().pac().setRestingTicks(model.rules().restingTicksForPellet());
    }

    @Override
    public void onEatEnergizer(GameContext gameContext, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(tile);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        scorePoints(gameContext, model.rules().pointsForEnergizer(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        pac.setRestingTicks(model.rules().restingTicksForEnergizer());
        level.clearGhostKillChain();
        startPacPowerMode(gameContext, pac);
    }

    @Override
    public void onEatBonus(GameContext gameContext, Bonus bonus) {
        requireNonNull(gameContext);
        requireNonNull(bonus);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        bonus.showEatenForSeconds(model.rules().eatenBonusDisplaySeconds());

        scorePoints(gameContext, bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);

        eventManager.publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public void onEatGhost(GameContext context, Ghost eatenGhost) {
        requireNonNull(context);
        requireNonNull(eatenGhost);

        final GameModel model = context.model();
        final GameLevel level = context.assertLevel();
        final GameEventManager eventManager = context.eventManager();
        final int killedBefore = level.ghostKillChainSize();
        final int points = model.rules().pointsForGhost(killedBefore);

        scorePoints(context, points, level.number());
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
    public void startPacPowerMode(GameContext gameContext, Pac pac) {
        requireNonNull(gameContext);
        requireNonNull(pac);

        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

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
    public void updatePacPowerMode(GameContext gameContext, Pac pac) {
        requireNonNull(gameContext);
        requireNonNull(pac);

        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();
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
    public void scorePoints(GameContext gameContext, int points, int levelNumber) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        final GameModel model = gameContext.model();
        final GameEventManager eventManager = gameContext.eventManager();

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
    public void updateHighScore(GameContext gameContext) {
        final GameModel model = gameContext.model();

        final PropertyFileScore highScore;
        try {
            highScore = model.highScore();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (highScore == null) {
            Logger.error("Cannot update high-score, no high-score file has been assigned");
            return;
        }
        final PropertyFileScore savedHighScore = new PropertyFileScore(highScore.file());
        try {
            savedHighScore.load();
            if (highScore.points() > savedHighScore.points()) {
                highScore.save();
            }
        } catch (IOException x) {
            Logger.error(x, "Could not update high-score");
        }
    }

    // private

    private void detectCollisions(GameContext context) {
        detectFoodCollision(context);
        detectEdibleBonusCollision(context);
        detectPacGhostCollision(context);
    }

    private void detectPacGhostCollision(GameContext context) {
        final GameLevel level = context.assertLevel();
        final GameModel model = context.model();
        final CollisionStrategy strategy = model.rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        context.thisFrame().huntingStepResult().ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(context.thisFrame().huntingStepResult().ghostsCollidingWithPac()::add);
    }

    private void detectEdibleBonusCollision(GameContext context) {
        final GameLevel level = context.assertLevel();
        final GameModel model = context.model();
        final CollisionStrategy strategy = model.rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        context.thisFrame().huntingStepResult().setEdibleBonus(null);
        if (bonus != null && bonus.state() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            context.thisFrame().huntingStepResult().setEdibleBonus(bonus);
        }
    }

    private void detectFoodCollision(GameContext context) {
        final GameLevel level = context.assertLevel();
        final Pac pac = level.entities().pac();
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = pac.computeTile();
        if (foodLayer.hasFoodAtTile(pacTile)) {
            context.thisFrame().huntingStepResult().setFoodFoundTile(pacTile);
            context.thisFrame().huntingStepResult().setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}
