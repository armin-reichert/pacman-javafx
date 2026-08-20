/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.pac.comp.PacPowerComp;
import de.amr.pacmanfx.core.entities.pac.system.PacDigestionSystem;
import de.amr.pacmanfx.core.entities.score.comp.ScoreDataComp;
import de.amr.pacmanfx.core.entities.score.comp.ScorePersistencyComp;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.SpecialScoreEvent;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerEndsEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsFadingEvent;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessage;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import org.tinylog.Logger;

import java.io.File;
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

    private static final Set<GhostState> GHOST_TURNBACK_STATES = Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC);

    @Override
    public void prepareLevelForPlaying(GameContext game) {
        final GameLevel level = game.session().level();
        final GameSystems systems = game.variant().systems();

        preparePacForPlaying(
            level.entities().pac(),
            level.worldMap().terrainLayer(),
            systems);

        prepareGhostsForPlaying(
            level.entities().ghosts(),
            level.entities().house(),
            systems);

        // Blinking energizers are visible when state is ON
        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().reset();
    }

    @Override
    public GameLevel buildNormalLevel(GameContext game, int levelNumber, int numLives) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameSession session = game.session();

        final GameLevel level = createLevel(game, levelNumber);

        session.setLevel(level);
        session.setAttractMode(false);
        session.livesCounter().data().setNumLives(numLives);
        ScoreSystem.setLevelNumber(session.score(), levelNumber);
        session.gateKeeper().setLevelNumber(levelNumber);

        return level;
    }

    @Override
    public void startNextLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameEventManager eventManager = game.eventManager();

        final GameLevel oldLevel = game.session().level();
        final int lastLevelNumber = game.variant().rules().lastLevelNumber();

        if (oldLevel.number() < lastLevelNumber) {
            final GameLevel newLevel = buildNormalLevel(game, oldLevel.number() + 1, session.livesCounter().data().numLives());
            game.eventManager().publishGameEvent(new LevelCreatedEvent(newLevel));

            startLevel(game);
            // Note: This event is very important because it triggers the creation of the actor animations!
            eventManager.publishGameEvent(new LevelStartedEvent(oldLevel));
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    @Override
    public void showLevelMessage(GameContext game, GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        game.session().hud().setMessage(message);
    }

    @Override
    public void updateGamePlay(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameRules rules = game.variant().rules();
        final GameSession session = game.session();
        final GamePlayStep gamePlayStep = session.thisFrame().gamePlayStep();

        final ActorCollisionHandler collisionHandler = new ActorCollisionHandler(gamePlayStep);
        collisionHandler.setStrategy(rules.actorCollisionRules().getCollisionStrategy());
        collisionHandler.setDoubleChecked(rules.actorCollisionRules().isCollisionDoubleChecked());

        level.huntingTimerStrategy().update(rules, level.number());
        session.gateKeeper().unlockGhostIfPossible(game, level);
        updateRemainingPacPower(game, level, level.entities().pac());

        collisionHandler.detectCollisions(level);
        evalCollisions(game, level, gamePlayStep);
    }

    @Override
    public void onPacPowerStarts(GameContext game, GameLevel level, Pac pac, long ticks) {
        final GameSystems systems = game.variant().systems();

        Logger.info("Pac power started. Power ticks: {}", ticks);

        level.huntingTimerStrategy().stop();

        level.entities()
            .ghostsInState(GhostState.HUNTING_PAC)
            .forEach(ghost -> systems.ghostState().changeGhostState(ghost, GhostState.FRIGHTENED));

        systems.pacPower().start(pac, ticks);
    }

    @Override
    public void onPacPowerStartsFading(GameContext game, GameLevel level, Pac pac) {
        Logger.info("Pac power started fading. Power ticks remaining: {}", pac.power().ticksRemaining());
    }

    @Override
    public void onPacPowerEnds(GameContext game, GameLevel level, Pac pac) {
        final GameSystems systems = game.variant().systems();

        level.clearGhostKillChain();

        level.entities().ghostsInState(GhostState.FRIGHTENED).forEach(ghost ->
            systems.ghostState().changeGhostState(ghost, GhostState.HUNTING_PAC));

        level.huntingTimerStrategy().start();

        Logger.info("Pac power ended, hunting resumed. Power ticks remaining: {}", pac.power().ticksRemaining());
    }

    @Override
    public void onEatPellet(GameContext game, GameLevel level, Vector2i tile) {
        requireNonNull(game);
        requireNonNull(level);
        requireNonNull(tile);

        final GameSession session = game.session();
        final GameRules rules = game.variant().rules();
        final Pac pac = level.entities().pac();

        scorePoints(game, rules.scoringRules().pointsForPellet(), level.number());
        game.variant().systems().pacDigestion().digestPellet(pac, rules);
        session.gateKeeper().registerFoodEaten(level);
    }

    @Override
    public void onEatEnergizer(GameContext game, GameLevel level, Vector2i tile) {
        requireNonNull(game);
        requireNonNull(level);
        requireNonNull(tile);

        final GameSession session = game.session();
        final GameRules rules = game.variant().rules();
        final Pac pac = level.entities().pac();

        scorePoints(game, rules.scoringRules().pointsForEnergizer(), level.number());
        session.gateKeeper().registerFoodEaten(level);
        level.clearGhostKillChain();
        game.variant().systems().pacDigestion().digestEnergizer(pac, rules);
        startPacPower(game, level, pac);
    }

    @Override
    public void onEatBonus(GameContext game, GameLevel level, Bonus bonus) {
        requireNonNull(game);
        requireNonNull(level);
        requireNonNull(bonus);

        final GameSystems systems = game.variant().systems();
        systems.bonusState().showEatenForSeconds(bonus, game.variant().rules().eatenBonusDisplaySeconds());
        bonus.optComp(WorldNavigationComp.class).ifPresent(_ -> systems.worldNavigator().setMoveDirSpeed(bonus, 0));

        scorePoints(game, bonus.data().points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.data().points(), bonus);

        game.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public void onEatGhost(GameContext game, GameLevel level, Ghost eatenGhost) {
        requireNonNull(game);
        requireNonNull(level);
        requireNonNull(eatenGhost);

        final GameSystems systems = game.variant().systems();

        final int killedBefore = level.ghostKillChainSize();
        final int points = game.variant().rules().scoringRules().pointsForGhost(killedBefore);

        scorePoints(game, points, level.number());
        Logger.info("Scored {} points for killing {}", points, eatenGhost.name());

        systems.ghostState().changeGhostState(eatenGhost, GhostState.EATEN);

        // Animation index is 0-based, animation frame 0 shows points for *first* killed ghost...
        systems.spriteAnimController().selectAndSetFrame(eatenGhost, CommonSpriteAnimationID.GHOST_POINTS, killedBefore);
        level.entities().ghosts().forEach(systems.spriteAnimController()::stopSelected);

        level.addToGhostKillChain(eatenGhost);
        level.entities().pac().hide();

        game.eventManager().publishGameEvent(new GhostEatenEvent(eatenGhost));
    }

    @Override
    public void onLevelCompleted(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameSystems systems = game.variant().systems();

        level.huntingTimerStrategy().stop();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.food().eatAll();

        final Pac pac = level.entities().pac();
        pac.power().reset();

        systems.worldNavigator().setMoveDirSpeed(pac, 0);
        systems.spriteAnimController().stopSelected(pac);
        systems.spriteAnimController().select(pac, CommonSpriteAnimationID.PAC_FULL);

        level.entities().ghosts().forEach(ghost -> {
            systems.worldNavigator().setMoveDirSpeed(ghost, 0);
            //TODO check in emulator if ghost animation is reset to normal
            systems.spriteAnimController().stopSelected(ghost);
            systems.spriteAnimController().select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
        });

        level.entities().optBonus().ifPresent(bonus -> {
            systems.bonusState().setBonusInactive(bonus);
            if (bonus.hasComp(BonusMoveAndJumpComp.class)) {
                systems.bonusMoveAndJump().setBonusInactive(bonus);
            }
            level.entities().remove(bonus);
        });
    }

    // Scoring

    @Override
    public void scorePoints(GameContext game, int points, int levelNumber) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameSession session = game.session();
        final ScoreDataComp scoreData = session.score().data();

        if (!scoreData.isEnabled()) {
            return;
        }

        final int oldScore = scoreData.points();
        final int newScore = oldScore + points;
        ScoreSystem.setPoints(session.score(), newScore);

        final Score highScore = session.highScore();
        if (highScore != null && highScore.data().isEnabled() && newScore > highScore.data().points()) {
            ScoreSystem.setPoints(highScore, newScore);
            ScoreSystem.setLevelNumber(highScore, levelNumber);
            ScoreSystem.setDate(highScore, LocalDate.now());
        }

        if (game.variant().rules().scoringRules().isExtraLifeAwarded(oldScore, newScore)) {
            LivesCounterSystem.addLife(session.livesCounter());
            game.eventManager().publishGameEvent(new SpecialScoreEvent(newScore));
        }
    }

    // private

    private void preparePacForPlaying(Pac pac, TerrainLayer terrain, GameSystems systems) {
        pac.reset(); // initially invisible!
        pac.pos().set(terrain.pacStartPosition());
        systems.pacPower().reset(pac);
        systems.worldNavigator().setMoveDir(pac, Direction.LEFT);
        systems.worldNavigator().setWishDir(pac, Direction.LEFT);
    }

    private void prepareGhostsForPlaying(List<Ghost> ghosts, House house, GameSystems systems) {
        ghosts.forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.pos().set(ghost.worldInfo().startPosition());
            final Direction direction = house.floorplan().ghostStartDirection(ghost.personality());
            systems.worldNavigator().setMoveDir(ghost, direction);
            systems.worldNavigator().setWishDir(ghost, direction);
            systems.ghostState().changeGhostState(ghost, GhostState.LOCKED);
            systems.spriteAnimController().resetSelected(ghost);
        });
    }

    protected void initScores(GameSession session) {
        session.score().reset();
        final File highScoreFile = session.highScore().reqComp(ScorePersistencyComp.class).file();
        try {
            ScoreSystem.load(session.highScore());
            ScoreSystem.enableScore(session.highScore(), true);
        } catch (IOException x) {
            Logger.error(x, "Error loading high-score file {}", highScoreFile.getAbsolutePath());
        }
    }

    private void startPacPower(GameContext game, GameLevel level, Pac pac) {
        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();

        // Ghosts make turnback also in case pac power time is zero!
        level.entities().ghostsInAnyOfStates(GHOST_TURNBACK_STATES).forEach(systems.worldNavigator()::requestTurnBack);

        final long powerTicks = TickTimer.secToTicks(rules.pacPowerSeconds(level.number()));
        if (powerTicks > 0) {
            //TODO move to game event handler!
            onPacPowerStarts(game, level, pac, powerTicks);
            game.eventManager().publishGameEvent(new PacPowerStartsEvent(pac));
        }
    }

    private void evalCollisions(GameContext game, GameLevel level, GamePlayStep step) {
        checkFoodFound(game, level, step);
        if (step.foundEdibleBonus()) {
            onEatBonus(game, level, step.edibleBonus());
        }
        checkIfPacGetsKilled(game.session(), game.variant().rules(), step);
        if (step.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            checkIfGhostsGetKilled(game, level, step);
        }
    }

    private void checkFoodFound(GameContext game, GameLevel level, GamePlayStep step) {
        final GameSystems systems = game.variant().systems();
        final Pac pac = level.entities().pac();
        final PacDigestionSystem digestionSystem = systems.pacDigestion();

        if (step.foodFound()) {
            digestionSystem.endStarving(pac);
            final Vector2i foodTile = step.foodFoundTile();
            level.food().markFoodEatenAt(foodTile);
            if (step.energizerFound()) {
                onEatEnergizer(game, level, foodTile);
            } else {
                onEatPellet(game, level, foodTile);
            }
            if (game.variant().rules().scoringRules().isBonusAwarded(level)) {
                activateNextBonus(game, level);
            }
            game.eventManager().publishGameEvent(new PacEatsFoodEvent(pac, step.energizerFound(), false));
        }
        else {
            digestionSystem.starve(pac);
        }
    }

    private void checkIfPacGetsKilled(GameSession session, GameRules rules, GamePlayStep step) {
        // Check for optional attract mode safe period
        if (session.isAttractMode()) {
            if (rules.demoLevelMinDurationSec().isPresent()) {
                final long minDurationMillis = (long) (rules.demoLevelMinDurationSec().get() * 1000);
                final long levelDurationMillis = System.currentTimeMillis() - session.levelStartTimeMillis();
                if (levelDurationMillis <= minDurationMillis) {
                    return;
                }
            }
        }
        else if (session.level().entities().pac().cheats().isImmune()) {
            return;
        }

        final boolean pacMeetsKiller = step.ghostsCollidingWithPac().stream()
            .anyMatch(ghost -> ghost.ghostStateEnum() == GhostState.HUNTING_PAC);

        step.setPacKilled(pacMeetsKiller);
    }

    private void checkIfGhostsGetKilled(GameContext game, GameLevel level, GamePlayStep step) {
        if (step.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            step.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.ghostStateEnum() == GhostState.FRIGHTENED)
                .forEach(step.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            step.ghostsKilled().forEach(ghost -> onEatGhost(game, level, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    private void fixPacPositionIfKilledInsidePortal(GameLevel level) {
        final Pac pac = level.entities().pac();
        final Vector2i pacTile = pac.pos().tile();
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

    private void updateRemainingPacPower(GameContext game, GameLevel level, Pac pac) {
        final PacPowerComp power = pac.power();
        if (power.ends()) {
            //TODO move into event handler!
            onPacPowerEnds(game, level, pac);
            game.eventManager().publishGameEvent(new PacPowerEndsEvent(pac));
        }
        else if (power.isFadingStart()) {
            onPacPowerStartsFading(game, level, pac);
            game.eventManager().publishGameEvent(new PacPowerStartsFadingEvent(pac));
        }
    }
}
