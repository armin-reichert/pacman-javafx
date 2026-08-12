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
import de.amr.pacmanfx.core.entities.score.comp.ScorePersistencyComp;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
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
import de.amr.pacmanfx.core.session.GameSession;
import org.tinylog.Logger;

import java.io.File;
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
    public void onSessionStart(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameModel model = game.model();

        model.worldMapManager().loadMapPrototypes();
        initScores(session);

        final LevelCounter levelCounter = game.session().levelCounter();
        LevelCounterSystem.setCapacity(levelCounter, 7);
        LevelCounterSystem.clear(levelCounter);
        LevelCounterSystem.enable(levelCounter, true);

        session.setGateKeeper(new ArcadeHouseGateKeeper()); // TODO not needed by Tengen
        session.gateKeeper().reset();
        session.setLevel(null);
        session.setPlaying(false);
    }

    private void initScores(GameSession session) {
        session.score().reset();
        final File highScoreFile = session.highScore().requireComp(ScorePersistencyComp.class).file();
        try {
            ScoreSystem.load(session.highScore());
            ScoreSystem.enableScore(session.highScore(), true);
        } catch (IOException x) {
            //TODO throw exception?
            Logger.error(x, "Error loading high-score file {}", highScoreFile.getAbsolutePath());
        }
    }

    @Override
    public void prepareLevelForPlaying(GameContext game) {
        final GameSystems systems = game.systems();
        final GameLevel level = game.session().assertLevel();
        final House house = level.entities().theOne(House.class);
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Pac pac = level.entities().pac();

        pac.reset(); // initially invisible!
        pac.pos().set(terrain.pacStartPosition());
        systems.pacPower().reset(pac);
        systems.worldNavigator().setMoveDir(pac, Direction.LEFT);
        systems.worldNavigator().setWishDir(pac, Direction.LEFT);

        level.entities().ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.pos().set(ghost.worldInfo().startPosition());
            final Direction direction = house.floorplan().ghostStartDirection(ghost.personality());
            systems.worldNavigator().setMoveDir(ghost, direction);
            systems.worldNavigator().setWishDir(ghost, direction);
            systems.ghostState().changeState(ghost, GhostState.LOCKED);
            systems.spriteAnim().resetSelected(ghost);
        });

        // Blinking energizers are visible when state is ON
        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().reset();
    }

    @Override
    public void buildNormalLevel(GameContext gameContext, int levelNumber, int numLives) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        final GameSession session = gameContext.session();

        final GameLevel level = createLevel(gameContext, levelNumber);

        session.setLevel(level);
        session.setAttractMode(false);

        session.livesCounter().data().setNumLives(numLives);
        ScoreSystem.setLevelNumber(session.score(), levelNumber);
        session.gateKeeper().setLevelNumber(levelNumber);

        gameContext.eventManager().publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void startNextLevel(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameSession session = gameContext.session();
        final GameModel model = gameContext.model();
        final GameLevel oldLevel = gameContext.session().assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        final int lastLevelNumber = model.rules().lastLevelNumber();
        if (oldLevel.number() < lastLevelNumber) {
            buildNormalLevel(gameContext, oldLevel.number() + 1, session.livesCounter().data().numLives());
            startLevel(gameContext);
            // Note: This event is very important because it triggers the creation of the actor animations!
            eventManager.publishGameEvent(new LevelStartedEvent(oldLevel));
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    @Override
    public void showLevelMessage(GameContext game, GameLevel level, GameLevelMessageType type) {
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

        final GameSession session = gameContext.session();
        final GameModel model = gameContext.model();
        final ArcadeHouseGateKeeper gateKeeper = session.gateKeeper();

        //TODO enable this later again
        //final boolean doubleChecked = model.rules().actorCollisionRules().isCollisionDoubleChecked();

        level.heartbeat().triggerPulse();
        level.huntingTimerStrategy().update(model.rules(), level.number());
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(gameContext);
        }

        updateEntities(gameContext, level);
        detectCollisions(gameContext, level);
        evalCollisions(gameContext, level);
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

    private void navigatePac(GameContext game, GameLevel level, Pac pac) {
        final GameSystems systems = game.systems();
        final GameSession session = game.session();
        final ActorSpeedRules speedRules = game.model().rules().actorSpeedRules();
        final float speed = pac.power().isActive()
            ? speedRules.pacSpeedWhenHasPower(game, level) : speedRules.pacSpeed(game, level);

        systems.pacAutoSteering().update(session, pac);
        systems.worldNavigator().setSpeed(pac, speed);
        systems.worldNavigator().tryMovingOrTeleporting(pac, level, systems.pacWorldMovementPolicy());
    }

    private void evalCollisions(GameContext gameContext, GameLevel level) {
        final HuntingStepResult result = gameContext.session().thisFrame().huntingStep();
        checkFoodFound(gameContext, level);
        if (result.foundEdibleBonus()) {
            onEatBonus(gameContext, level, result.edibleBonus());
        }
        evalPacKilled(gameContext.session(), result);
        if (result.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            evalGhostsKilled(gameContext, level, result);
        }
    }

    private void checkFoodFound(GameContext gameContext, GameLevel level) {
        final HuntingStepResult huntingResult = gameContext.session().thisFrame().huntingStep();
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

    private void evalPacKilled(GameSession session, HuntingStepResult result) {
        final GameLevel level = session.assertLevel();
        if (session.isAttractMode() && isPacSafeInDemoLevel(level)
            || level.entities().pac().cheats().isImmune()) {
            return;
        }
        result.setPacKilled(
            result.ghostsCollidingWithPac().stream()
                .anyMatch(ghost -> ghost.ghostStateEnum() == GhostState.HUNTING_PAC)
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
    private void fixPacPositionIfKilledInsidePortal(GameLevel level) {
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

        final GameSession session = gameContext.session();
        final GameModel model = gameContext.model();
        final GameRules rules = model.rules();
        final Pac pac = level.entities().pac();

        scorePoints(gameContext, rules.scoringRules().pointsForPellet(), level.number());
        gameContext.systems().pacDigestion().digestPellet(pac, rules);
        session.gateKeeper().registerFoodEaten(level);
    }

    @Override
    public void onEatEnergizer(GameContext gameContext, GameLevel level, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(level);
        requireNonNull(tile);

        final GameSession session = gameContext.session();
        final GameModel model = gameContext.model();
        final GameRules rules = model.rules();
        final Pac pac = level.entities().pac();

        scorePoints(gameContext, rules.scoringRules().pointsForEnergizer(), level.number());
        session.gateKeeper().registerFoodEaten(level);
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

        final GameSession session = gameContext.session();
        final GameModel model = gameContext.model();
        final GameEventManager eventManager = gameContext.eventManager();

        if (!session.score().data().isEnabled()) {
            return;
        }
        final int oldScore = session.score().data().points();
        final int newScore = oldScore + points;

        if (model.rules().scoringRules().isExtraLifeAwarded(oldScore, newScore)) {
            LivesCounterSystem.addLife(session.livesCounter());
            eventManager.publishGameEvent(new SpecialScoreEvent(newScore));
        }

        final Score highScore = session.highScore();
        if (highScore != null && highScore.data().isEnabled() && newScore > highScore.data().points()) {
            ScoreSystem.setPoints(highScore, newScore);
            ScoreSystem.setLevelNumber(highScore, levelNumber);
            ScoreSystem.setDate(highScore, LocalDate.now());
        }
        ScoreSystem.setPoints(session.score(), newScore);
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

    private void detectCollisions(GameContext gameContext, GameLevel level) {
        detectFoodCollision(gameContext, level);
        detectEdibleBonusCollision(gameContext, level);
        detectPacGhostCollision(gameContext, level);
    }

    private void detectPacGhostCollision(GameContext gameContext, GameLevel level) {
        final GameModel model = gameContext.model();
        final CollisionStrategy strategy = model.rules().actorCollisionRules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        gameContext.session().thisFrame().huntingStep().ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(gameContext.session().thisFrame().huntingStep().ghostsCollidingWithPac()::add);
    }

    private void detectEdibleBonusCollision(GameContext gameContext, GameLevel level) {
        final GameModel model = gameContext.model();
        final CollisionStrategy strategy = model.rules().actorCollisionRules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        gameContext.session().thisFrame().huntingStep().setEdibleBonus(null);
        if (bonus != null && bonus.bonusState() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            gameContext.session().thisFrame().huntingStep().setEdibleBonus(bonus);
        }
    }

    private void detectFoodCollision(GameContext gameContext, GameLevel level) {
        final Pac pac = level.entities().pac();
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = WorldNavigationSystem.computeTile(pac);
        if (foodLayer.hasFoodAtTile(pacTile)) {
            gameContext.session().thisFrame().huntingStep().setFoodFoundTile(pacTile);
            gameContext.session().thisFrame().huntingStep().setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}
