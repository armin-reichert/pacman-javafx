/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.*;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.GameSystems;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessage;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.world.*;
import de.amr.pacmanfx.core.rules.CollisionStrategy;
import de.amr.pacmanfx.core.score.PropertyFileScore;
import de.amr.pacmanfx.core.score.Score;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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

        model.setLifeCount(model.initialLifeCount());
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
        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = terrain.optHouse().orElseThrow();

        final Pac pac = level.entities().pac();
        pac.reset(); // initially invisible!
        pac.position().set(terrain.pacStartPosition());
        pac.power().timer().resetToIndefiniteDuration();

        sys.navigator.setMoveDir(pac, Direction.LEFT);
        sys.navigator.setWishDir(pac, Direction.LEFT);

        sys.spriteAnim.resetSelected(pac);

        level.entities().ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.position().set(ghost.worldPlacement().startPosition());
            final Direction direction = house.ghostStartDirection(ghost.personality());
            sys.navigator.setMoveDir(ghost, direction);
            sys.navigator.setWishDir(ghost, direction);
            sys.ghostState.changeState(gameContext, ghost, GhostState.LOCKED);
            sys.spriteAnim.resetSelected(ghost);
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
    public void startNextLevel(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        final int lastLevelNumber = model.rules().lastLevelNumber();
        if (level.number() < lastLevelNumber) {
            buildNormalLevel(gameContext, level.number() + 1);
            startLevel(gameContext);
            // Note: This event is very important because it triggers the creation of the actor animations!
            eventManager.publishGameEvent(new LevelStartedEvent(level));
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    @Override
    public void showLevelMessage(GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        message.position().set(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
    }

    @Override
    public void hunt(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final ArcadeHouseGateKeeper gateKeeper = model.gateKeeper();
        final boolean doubleChecked = model.rules().actorCollisionRules().isCollisionDoubleChecked();

        level.heartbeat().triggerPulse();
        level.huntingRules().update(model.rules(), level.number());

        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(gameContext);
        }

        gameContext.systems().pacPower.update(gameContext, pac);

        // If double-check active, do an additional collision check before Pac has moved
        level.entities().forEach(entity -> {
            if (entity != pac) {
                entity.update(gameContext);
            }
        });

        if (doubleChecked) {
            detectCollisions(gameContext);
        }
        pac.update(gameContext);

        detectCollisions(gameContext);
        evalCollisions(gameContext);
    }

    private void evalCollisions(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final HuntingStepResult result = gameContext.thisFrame().huntingStep();

        checkFoodFound(gameContext);

        if (result.foundEdibleBonus()) {
            onEatBonus(gameContext, result.edibleBonus());
        }

        evalPacKilled(result, level);
        if (result.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(gameContext);
        }
        else {
            evalGhostsKilled(gameContext, result);
        }
    }

    private void checkFoodFound(GameContext gameContext) {
        final HuntingStepResult huntingResult = gameContext.thisFrame().huntingStep();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final PacDigestionSystem pacDigestionSystem = gameContext.systems().pacDigestion;

        if (huntingResult.foodFound()) {
            pacDigestionSystem.endStarving(pac);
            final Vector2i foodTile = huntingResult.foodFoundTile();
            level.worldMap().foodLayer().markFoodEatenAt(foodTile);
            if (huntingResult.energizerFound()) {
                onEatEnergizer(gameContext, foodTile);
            } else {
                onEatPellet(gameContext, foodTile);
            }
            if (gameContext.model().rules().scoringRules().isBonusAwarded(level)) {
                activateNextBonus(gameContext);
            }
            gameContext.eventManager().publishGameEvent(new PacEatsFoodEvent(pac, huntingResult.energizerFound(), false));
        }
        else {
            pacDigestionSystem.starve(pac);
        }
    }

    private void evalPacKilled(HuntingStepResult result, GameLevel level) {
        if (level.isDemoLevel() && isPacSafeInDemoLevel(level) || level.entities().pac().cheats().isImmune()) {
            return;
        }
        result.setPacKilled(
            result.ghostsCollidingWithPac().stream().anyMatch(ghost -> ghost.state() == GhostState.HUNTING_PAC)
        );
    }

    private void evalGhostsKilled(GameContext gameContext, HuntingStepResult result) {
        if (result.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            result.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(result.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            result.ghostsKilled().forEach(ghost -> onEatGhost(gameContext, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    private void fixPacPositionIfKilledInsidePortal(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final Vector2i pacTile = WorldNavigationSystem.computeTile(pac);
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        terrain.hPortalContainingTile(pacTile).ifPresent(hPortal -> {
            if (pac.worldNavigation().moveDir() == Direction.LEFT) {
                pac.position().setX(hPortal.rightBorderEntryTile().x() * WorldMap.TS + WorldMap.HTS);
            } else if (pac.worldNavigation().moveDir() == Direction.RIGHT) {
                pac.position().setX(hPortal.leftBorderEntryTile().x() * WorldMap.TS - WorldMap.HTS);
            }
            // Not sure if colliding ghosts should also be moved back to visible area
            Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
        });
    }

    @Override
    public void onEatPellet(GameContext gameContext, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(tile);

        final PacDigestionSystem pacDigestionSystem = gameContext.systems().pacDigestion;
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        scorePoints(gameContext, model.rules().scoringRules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        pacDigestionSystem.onPacEatsPellet(gameContext, pac);
    }

    @Override
    public void onEatEnergizer(GameContext gameContext, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(tile);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final PacDigestionSystem pacDigestionSystem = gameContext.systems().pacDigestion;

        scorePoints(gameContext, model.rules().scoringRules().pointsForEnergizer(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        pacDigestionSystem.onPacEatsEnergizer(gameContext, pac);
        level.clearGhostKillChain();

        gameContext.systems().pacPower.start(gameContext, pac);
    }

    @Override
    public void onEatBonus(GameContext gameContext, Bonus bonus) {
        requireNonNull(gameContext);
        requireNonNull(bonus);

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        bonus.showEatenForSeconds(gameContext, model.rules().eatenBonusDisplaySeconds());

        scorePoints(gameContext, bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);

        eventManager.publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public void onEatGhost(GameContext gameContext, Ghost eatenGhost) {
        requireNonNull(gameContext);
        requireNonNull(eatenGhost);

        final GameSystems sys = gameContext.systems();

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        final int killedBefore = level.ghostKillChainSize();
        final int points = model.rules().scoringRules().pointsForGhost(killedBefore);

        scorePoints(gameContext, points, level.number());
        Logger.info("Scored {} points for killing {}", points, eatenGhost.name());

        sys.ghostState.changeState(gameContext, eatenGhost, GhostState.EATEN);

        // Animation index is 0-based, animation frame 0 shows points for *first* killed ghost...
        sys.spriteAnim.selectAndSetFrame(eatenGhost, CommonAnimationID.GHOST_POINTS, killedBefore);

        level.addToGhostKillChain(eatenGhost);
        level.entities().pac().visibility().hide();
        level.entities().ghosts().forEach(sys.spriteAnim::stopSelected);

        eventManager.publishGameEvent(new GhostEatenEvent(eatenGhost));
    }

    @Override
    public void onLevelCompleted(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        level.huntingRules().stop();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        final Pac pac = level.entities().pac();
        pac.power().reset();

        sys.navigator.setSpeed(pac, 0);

        sys.spriteAnim.stopSelected(pac);
        sys.spriteAnim.select(pac, CommonAnimationID.PAC_FULL);

        level.entities().ghosts().forEach(ghost -> {
            sys.navigator.setSpeed(ghost, 0);

            //TODO check in emulator if ghost animation is reset to normal
            sys.spriteAnim.stopSelected(ghost);
            sys.spriteAnim.select(ghost, CommonAnimationID.GHOST_NORMAL);
        });

        level.optBonus().ifPresent(bonus -> bonus.setInactive(gameContext));
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

        if (model.rules().scoringRules().isExtraLifeAwarded(oldScore, newScore)) {
            model.addLives(1);
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

    private void detectCollisions(GameContext gameContext) {
        detectFoodCollision(gameContext);
        detectEdibleBonusCollision(gameContext);
        detectPacGhostCollision(gameContext);
    }

    private void detectPacGhostCollision(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final GameModel model = gameContext.model();
        final CollisionStrategy strategy = model.rules().actorCollisionRules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        gameContext.thisFrame().huntingStep().ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(gameContext.thisFrame().huntingStep().ghostsCollidingWithPac()::add);
    }

    private void detectEdibleBonusCollision(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final GameModel model = gameContext.model();
        final CollisionStrategy strategy = model.rules().actorCollisionRules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        gameContext.thisFrame().huntingStep().setEdibleBonus(null);
        if (bonus != null && bonus.state() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            gameContext.thisFrame().huntingStep().setEdibleBonus(bonus);
        }
    }

    private void detectFoodCollision(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = WorldNavigationSystem.computeTile(pac);
        if (foodLayer.hasFoodAtTile(pacTile)) {
            gameContext.thisFrame().huntingStep().setFoodFoundTile(pacTile);
            gameContext.thisFrame().huntingStep().setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}
