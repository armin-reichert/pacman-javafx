/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostSpriteAnimationComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.system.GhostSpriteAnimationSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.pac.comp.PacPowerComp;
import de.amr.pacmanfx.core.entities.pac.system.PacDigestionSystem;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.SpecialScoreEvent;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacGetsPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacLostPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerFadesEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessage;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.model.rules.CollisionStrategy;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

/**
 * Common game play functionality. Can be modified by game-variant specific subclasses.
 */
public abstract class CommonGamePlay implements GamePlay {

    private static final Set<GhostState> TURNBACK_STATES = Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC);

    @Override
    public void resetForNewGame(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameModel model = gameContext.model();

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

        LevelCounterSystem.clear(model.levelCounter());

        model.setLevel(null);
        model.setPlaying(false);
    }

    @Override
    public void prepareLevelForPlaying(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        final House house = level.entities().theOne(House.class);
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        final Pac pac = level.entities().pac();
        pac.reset(); // initially invisible!
        pac.pos().set(terrain.pacStartPosition());
        sys.pacPower().reset(pac);

        sys.worldNavigator().setMoveDir(pac, Direction.LEFT);
        sys.worldNavigator().setWishDir(pac, Direction.LEFT);

        sys.spriteAnim().resetSelected(pac);

        level.entities().ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.pos().set(ghost.worldPlacement().startPosition());
            final Direction direction = house.floorplan().ghostStartDirection(ghost.personality());
            sys.worldNavigator().setMoveDir(ghost, direction);
            sys.worldNavigator().setWishDir(ghost, direction);
            sys.ghostState().changeState(ghost, GhostState.LOCKED);
            sys.spriteAnim().resetSelected(ghost);
        });

        level.heartbeat().setStartState(Pulse.State.ON); // Energizers are visible when ON
        level.heartbeat().reset();
    }

    @Override
    public boolean isDemoLevelRunning(GameContext gameContext) {
        return gameContext.optLevel().isPresent() && gameContext.assertLevel().isDemoLevel();
    }

    @Override
    public void buildNormalLevel(GameContext gameContext, int levelNumber, int numLives) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        final GameModel model = gameContext.model();
        final GameEventManager eventManager = gameContext.eventManager();

        final GameLevel level = createLevel(gameContext, levelNumber, false);

        final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
        livesCounter.data().setNumLives(numLives);

        model.score().setLevelNumber(levelNumber);
        model.gateKeeper().setLevelNumber(levelNumber);
        model.setLevel(level);

        eventManager.publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void startNextLevel(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameModel model = gameContext.model();
        final GameLevel oldLevel = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        final int lastLevelNumber = model.rules().lastLevelNumber();
        if (oldLevel.number() < lastLevelNumber) {
            final LivesCounter counter = oldLevel.entities().theOne(LivesCounter.class);
            buildNormalLevel(gameContext, oldLevel.number() + 1, counter.data().numLives());
            startLevel(gameContext);
            // Note: This event is very important because it triggers the creation of the actor animations!
            eventManager.publishGameEvent(new LevelStartedEvent(oldLevel));
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    @Override
    public void showLevelMessage(GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        message.pos().set(messageCenterPosition(level));
        level.setMessage(message);
    }

    @Override
    public void updateEntities(GameContext gameContext, GameLevel level) {
        final Pac pac = level.entities().pac();
        updatePac(gameContext, level, pac);
        updateGhosts(gameContext, level);
        gameContext.systems().bonusState().update(gameContext);
    }

    @Override
    public void hunt(GameContext gameContext, GameLevel level) {
        requireNonNull(gameContext);
        requireNonNull(level);

        final GameModel model = gameContext.model();
        final ArcadeHouseGateKeeper gateKeeper = model.gateKeeper();

        //TODO enable this later again
        //final boolean doubleChecked = model.rules().actorCollisionRules().isCollisionDoubleChecked();

        level.heartbeat().triggerPulse();
        level.huntingTimerStrategy().update(model.rules(), level.number());
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(gameContext);
        }

        updateEntities(gameContext, level);

        detectCollisions(gameContext);
        evalCollisions(gameContext);
    }

    private void updateGhosts(GameContext gameContext, GameLevel level) {
            if (gameContext.state().id().equals(CommonGameStateID.GAME_LEVEL_EATING_GHOST)) {
                level.ghostsInAnyOfStates(GhostStateSystem.UPDATED_GHOST_STATES_WHILE_EATEN)
                    .forEach(ghost -> updateGhost(gameContext, level, ghost));
            } else {
                level.entities().ghosts().forEach(ghost -> updateGhost(gameContext, level, ghost));
            }
    }

    private void updateGhost(GameContext gameContext, GameLevel level, Ghost ghost) {
        gameContext.systems().ghostState().update(gameContext, level, ghost);
        //TODO Add into global game systems interface
        GhostSpriteAnimationSystem.update(ghost, level.entities().pac());

        //TODO should this be here?
        final GhostSpriteAnimationComp ghostAnimation = ghost.ghostAnimation();
        if (ghostAnimation.ghostAnimationID() != null) {
            gameContext.systems().spriteAnim().select(ghost, ghostAnimation.ghostAnimationID());
            gameContext.systems().spriteAnim().playSelected(ghost);
        }
    }

    private void startPacPower(GameContext gameContext, GameLevel level, Pac pac) {
        level.ghostsInAnyOfStates(TURNBACK_STATES).forEach(gameContext.systems().worldNavigator()::requestTurnBack);

        if (level.pacPowerSeconds() > 0) {
            level.huntingTimerStrategy().stop();
            level.ghostsInState(GhostState.HUNTING_PAC)
                .forEach(ghost -> gameContext.systems().ghostState().changeState(ghost, GhostState.FRIGHTENED));
            gameContext.systems().pacPower().start(pac, TickTimer.secToTicks(level.pacPowerSeconds()));
            gameContext.eventManager().publishGameEvent(new PacGetsPowerEvent(pac));
        }
    }

    private void checkPacPower(GameContext gameContext, GameLevel level, Pac pac) {
        final PacPowerComp power = pac.power();
        if (power.isFading()) {
            gameContext.eventManager().publishGameEvent(new PacPowerFadesEvent(pac));
        }
        else if (power.isOver()) {
            power.reset();
            level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost ->
                gameContext.systems().ghostState().changeState(ghost, GhostState.HUNTING_PAC));
            level.clearGhostKillChain();
            level.huntingTimerStrategy().start();
            gameContext.eventManager().publishGameEvent(new PacLostPowerEvent(pac));
        }
    }

    private void updatePac(GameContext gameContext, GameLevel level, Pac pac) {
        gameContext.systems().pacDigestion().update(pac);
        gameContext.systems().pacPower().update(level, pac);
        gameContext.systems().pacState().update(pac);
        navigatePac(gameContext, level, pac);
        gameContext.systems().pacAnimation().update(pac);
        checkPacPower(gameContext, level, pac);
    }

    private void navigatePac(GameContext gameContext, GameLevel level, Pac pac) {
        final ActorSpeedRules speedRules = level.gameModel().rules().actorSpeedRules();
        final float speed = pac.power().isActive() ? speedRules.pacSpeedWhenHasPower(level) : speedRules.pacSpeed(level);
        gameContext.systems().pacAutoSteering().update(level, pac);
        gameContext.systems().worldNavigator().setSpeed(pac, speed);
        gameContext.systems().worldNavigator().tryMovingOrTeleporting(pac, level, gameContext.systems().pacWorldMovementPolicy());
    }

    private void evalCollisions(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final HuntingStepResult result = gameContext.thisFrame().huntingStep();

        checkFoodFound(gameContext);

        if (result.foundEdibleBonus()) {
            onEatBonus(gameContext, level, result.edibleBonus());
        }

        evalPacKilled(result, level);
        if (result.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(gameContext);
        }
        else {
            evalGhostsKilled(gameContext, level, result);
        }
    }

    private void checkFoodFound(GameContext gameContext) {
        final HuntingStepResult huntingResult = gameContext.thisFrame().huntingStep();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final PacDigestionSystem pacDigestionSystem = gameContext.systems().pacDigestion();

        if (huntingResult.foodFound()) {
            pacDigestionSystem.endStarving(pac);
            final Vector2i foodTile = huntingResult.foodFoundTile();
            level.worldMap().foodLayer().markFoodEatenAt(foodTile);
            if (huntingResult.energizerFound()) {
                onEatEnergizer(gameContext, level, foodTile);
            } else {
                onEatPellet(gameContext, level, foodTile);
            }
            if (gameContext.model().rules().scoringRules().isBonusAwarded(level)) {
                activateNextBonus(gameContext, level);
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
            result.ghostsCollidingWithPac().stream().anyMatch(ghost -> ghost.ghostStateEnum() == GhostState.HUNTING_PAC)
        );
    }

    private void evalGhostsKilled(GameContext gameContext, GameLevel level, HuntingStepResult result) {
        if (result.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            result.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.ghostStateEnum() == GhostState.FRIGHTENED)
                .forEach(result.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            result.ghostsKilled().forEach(ghost -> onEatGhost(gameContext, level, ghost));
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
                pac.pos().setX(hPortal.rightBorderEntryTile().x() * WorldMap.TS + WorldMap.HTS);
            } else if (pac.worldNavigation().moveDir() == Direction.RIGHT) {
                pac.pos().setX(hPortal.leftBorderEntryTile().x() * WorldMap.TS - WorldMap.HTS);
            }
            // Not sure if colliding ghosts should also be moved back to visible area
            Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
        });
    }

    @Override
    public void onEatPellet(GameContext gameContext, GameLevel level, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = gameContext.model();
        final GameRules rules = model.rules();
        final Pac pac = level.entities().pac();

        scorePoints(gameContext, rules.scoringRules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        gameContext.systems().pacDigestion().digestPellet(pac, rules);
    }

    @Override
    public void onEatEnergizer(GameContext gameContext, GameLevel level, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = gameContext.model();
        final GameRules rules = model.rules();
        final Pac pac = level.entities().pac();

        scorePoints(gameContext, rules.scoringRules().pointsForEnergizer(), level.number());
        model.gateKeeper().registerFoodEaten(level);
        level.clearGhostKillChain();
        gameContext.systems().pacDigestion().digestEnergizer(pac, rules);
        startPacPower(gameContext, level, pac);
    }

    @Override
    public void onEatBonus(GameContext gameContext, GameLevel level, Bonus bonus) {
        requireNonNull(gameContext);
        requireNonNull(level);
        requireNonNull(bonus);

        final GameSystems sys = gameContext.systems();
        final GameModel model = gameContext.model();

        sys.bonusState().showEatenForSeconds(bonus, model.rules().eatenBonusDisplaySeconds());

        scorePoints(gameContext, bonus.data().points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.data().points(), bonus);

        gameContext.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public void onEatGhost(GameContext gameContext, GameLevel level, Ghost eatenGhost) {
        requireNonNull(gameContext);
        requireNonNull(level);
        requireNonNull(eatenGhost);

        final GameSystems sys = gameContext.systems();
        final GameModel model = gameContext.model();

        final int killedBefore = level.ghostKillChainSize();
        final int points = model.rules().scoringRules().pointsForGhost(killedBefore);

        scorePoints(gameContext, points, level.number());
        Logger.info("Scored {} points for killing {}", points, eatenGhost.name());

        sys.ghostState().changeState(eatenGhost, GhostState.EATEN);

        // Animation index is 0-based, animation frame 0 shows points for *first* killed ghost...
        sys.spriteAnim().selectAndSetFrame(eatenGhost, CommonSpriteAnimationID.GHOST_POINTS, killedBefore);
        level.entities().ghosts().forEach(sys.spriteAnim()::stopSelected);

        level.addToGhostKillChain(eatenGhost);
        level.entities().pac().hide();

        gameContext.eventManager().publishGameEvent(new GhostEatenEvent(eatenGhost));
    }

    @Override
    public void onLevelCompleted(GameContext gameContext, GameLevel level) {
        requireNonNull(gameContext);
        requireNonNull(level);

        final GameSystems sys = gameContext.systems();

        level.huntingTimerStrategy().stop();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        final Pac pac = level.entities().pac();
        pac.power().reset();

        sys.worldNavigator().setSpeed(pac, 0);

        sys.spriteAnim().stopSelected(pac);
        sys.spriteAnim().select(pac, CommonSpriteAnimationID.PAC_FULL);

        level.entities().ghosts().forEach(ghost -> {
            sys.worldNavigator().setSpeed(ghost, 0);

            //TODO check in emulator if ghost animation is reset to normal
            sys.spriteAnim().stopSelected(ghost);
            sys.spriteAnim().select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
        });

        level.optBonus().ifPresent(bonus -> sys.bonusState().setInactive(bonus));
    }

    // Scoring

    @Override
    public void scorePoints(GameContext gameContext, int points, int levelNumber) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        final GameLevel level = gameContext.assertLevel();
        final GameModel model = gameContext.model();
        final GameEventManager eventManager = gameContext.eventManager();

        if (!model.score().isEnabled()) {
            return;
        }
        final int oldScore = model.score().points();
        final int newScore = oldScore + points;

        if (model.rules().scoringRules().isExtraLifeAwarded(oldScore, newScore)) {
            final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
            LivesCounterSystem.addLife(livesCounter);
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

    /**
     * @return position where level messages ("READY!", "GAME OVER") are displayed.
     */
    public Vector2f messageCenterPosition(GameLevel level) {
        final House house = level.entities().theOne(House.class);
        Vector2i houseSize = house.sizeInTiles();
        float cx = tilesPx(house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float cy = tilesPx(house.floorplan().minTile().y() + houseSize.y() + 1);
        return vec2_float(cx, cy);
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
        if (bonus != null && bonus.bonusState() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
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
