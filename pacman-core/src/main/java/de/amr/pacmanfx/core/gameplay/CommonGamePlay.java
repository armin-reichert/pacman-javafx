/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
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
import de.amr.pacmanfx.core.event.pac.PacPowerEndsEvent;
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
import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * Common game play functionality. Can be modified by game-variant specific subclasses.
 */
public abstract class CommonGamePlay implements GamePlay {

    public static final Set<GhostState> GHOST_TURNBACK_STATES = Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC);

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
        session.hudEntities().theOne(LivesCounter.class).data().setNumLives(numLives);
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
            final GameLevel newLevel = buildNormalLevel(game, oldLevel.number() + 1, session.hudEntities().theOne(LivesCounter.class).data().numLives());
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
        final Pac pac = level.entities().pac();

        final ActorCollisionHandler collisionHandler = new ActorCollisionHandler(gamePlayStep);
        collisionHandler.setStrategy(rules.actorCollisionRules().getCollisionStrategy());
        collisionHandler.setDoubleChecked(rules.actorCollisionRules().isCollisionDoubleChecked());

        level.huntingTimerStrategy().update(rules, level.number());
        session.gateKeeper().unlockGhostIfPossible(game, level);

        if (pac.power().ends()) {
            game.eventManager().publishGameEvent(new PacPowerEndsEvent(pac));
        }
        else if (pac.power().isFadingStart()) {
            game.eventManager().publishGameEvent(new PacPowerStartsFadingEvent(pac));
        }

        collisionHandler.detectCollisions(level);
        evalCollisions(game, level, gamePlayStep);
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
        systems.actorSpriteAnimController().selectAndSetFrame(eatenGhost, CommonSpriteAnimationID.GHOST_POINTS, killedBefore);
        level.entities().ghosts().forEach(systems.actorSpriteAnimController()::stopSelected);

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
        systems.actorSpriteAnimController().stopSelected(pac);
        systems.actorSpriteAnimController().select(pac, CommonSpriteAnimationID.PAC_FULL);

        level.entities().ghosts().forEach(ghost -> {
            systems.worldNavigator().setMoveDirSpeed(ghost, 0);
            //TODO check in emulator if ghost animation is reset to normal
            systems.actorSpriteAnimController().stopSelected(ghost);
            systems.actorSpriteAnimController().select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
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
        final Score gameScore = session.score();
        final Score highScore = session.highScore();

        ScoreSystem.scorePoints(gameScore, highScore, points, levelNumber, game.variant().rules().scoringRules());

        if (gameScore.data().extraLifeReached()) {
            LivesCounterSystem.addLife(session.hudEntities().theOne(LivesCounter.class));
            game.eventManager().publishGameEvent(new SpecialScoreEvent(gameScore.data().points()));
            // Do not forget to clear the flag!
            gameScore.data().setExtraLifeReached(false);
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
            systems.actorSpriteAnimController().resetSelected(ghost);
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

    private void evalCollisions(GameContext game, GameLevel level, GamePlayStep step) {
        checkIfPacFoundEdibleItem(game, level, step);
        checkIfPacGetsKilled(game.session(), game.variant().rules(), step);
        if (step.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            checkIfGhostsGetKilled(game, level, step);
        }
    }

    private void checkIfPacFoundEdibleItem(GameContext game, GameLevel level, GamePlayStep step) {
        final Pac pac = level.entities().pac();
        final GameSystems systems = game.variant().systems();
        final PacDigestionSystem digestionSystem = systems.pacDigestion();

        if (step.foodFound()) {
            digestionSystem.endStarving(pac);
            final Vector2i foodTile = step.foodFoundTile();
            level.food().markFoodEatenAt(foodTile);
            if (game.variant().rules().scoringRules().isBonusAwarded(level)) {
                activateNextBonus(game, level);
            }
            game.eventManager().publishGameEvent(new PacEatsFoodEvent(pac,
                step.energizerFound(), false, game.session().thisFrame().tick())
            );
        }
        else {
            digestionSystem.starve(pac);
        }

        if (step.foundEdibleBonus()) {
            game.eventManager().publishGameEvent(new BonusEatenEvent(step.edibleBonus()));
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
            .anyMatch(ghost -> ghost.state().enumValue() == GhostState.HUNTING_PAC);

        step.setPacKilled(pacMeetsKiller);
    }

    private void checkIfGhostsGetKilled(GameContext game, GameLevel level, GamePlayStep step) {
        if (step.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            step.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state().enumValue() == GhostState.FRIGHTENED)
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
}
