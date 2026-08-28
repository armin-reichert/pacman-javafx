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
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.entities.pac.system.PacDigestionSystem;
import de.amr.pacmanfx.core.entities.score.comp.ScorePersistencyComp;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.gameplay.SpecialScoreEvent;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerEndsEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsFadingEvent;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.core.rules.ScoringRules;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * Common game play functionality. Can be modified by game-variant specific subclasses.
 */
public abstract class CommonGamePlay implements GamePlay {

    @Override
    public void prepareLevelForPlaying(GameContext game, GameLevel level) {
        final GameSystems systems = game.variant().systems();

        final WorldMap worldMap = level.worldMap();
        final House house = level.entities().house();
        final Pac pac = level.entities().pac();

        pac.reset(); // initially invisible!
        pac.pos().set(worldMap.terrainLayer().pacStartPosition());
        systems.pacState().setState(pac, PacState.SLEEPING);
        systems.pacPower().reset(pac);
        systems.motor().setVelocity(pac, 0, 0);
        systems.worldNavigator().setMoveDir(pac, Direction.LEFT);
        systems.worldNavigator().setWishDir(pac, Direction.LEFT);
        systems.pacAnimation().update(pac);

        level.entities().ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible and locked!
            ghost.pos().set(ghost.worldInfo().startPosition());
            final Direction direction = house.floorplan().ghostStartDirection(ghost.personality());
            systems.worldNavigator().setMoveDir(ghost, direction);
            systems.worldNavigator().setWishDir(ghost, direction);
            systems.actorSpriteAnimController().resetSelected(ghost);
        });

        // Blinking energizers are visible when state is ON
        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().reset();
    }

    @Override
    public GameLevel buildNormalLevel(GameContext game, int levelNumber) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameSession session = game.session();

        final GameLevel level = createLevel(game, levelNumber);

        session.setLevel(level);
        session.setAttractMode(false);
        ScoreSystem.setLevelNumber(session.hud().gameScore(), levelNumber);

        return level;
    }

    @Override
    public void startNextLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();

        final GameLevel currentLevel = session.level();
        final int lastLevelNumber = game.variant().rules().lastLevelNumber();

        if (currentLevel.number() < lastLevelNumber) {
            final GameLevel nextLevel = buildNormalLevel(game, currentLevel.number() + 1);
            game.eventManager().publishGameEvent(new LevelCreatedEvent(nextLevel));
            startLevel(game, nextLevel);
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    @Override
    public void showMessage(GameContext game, MessageType type) {
        game.session().hud().messageView().data().setMessageType(type);
    }

    @Override
    public void update(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameRules rules = game.variant().rules();
        final GameSession session = game.session();
        final GamePlayStep step = session.thisFrame().gamePlayStep();
        final Pac pac = level.entities().pac();

        final ActorCollisionHandler collisionHandler = new ActorCollisionHandler(step);
        collisionHandler.setStrategy(rules.actorCollisionRules().getCollisionStrategy());
        collisionHandler.setDoubleChecked(rules.actorCollisionRules().isCollisionDoubleChecked());

        level.huntingTimerStrategy().update(rules, level.number());

        level.gateKeeper().unlockGhostIfPossible(game, level);

        if (pac.power().ends()) {
            game.eventManager().publishGameEvent(new PacPowerEndsEvent(pac));
        }
        else if (pac.power().isFadingStart()) {
            game.eventManager().publishGameEvent(new PacPowerStartsFadingEvent(pac));
        }

        collisionHandler.detectCollisions(level);

        checkIfPacFoundEdibleItem(game, level, step);
        checkIfPacGetsKilled(game.session(), game.variant().rules(), step);
        if (step.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            checkIfGhostsGetKilled(game, level, step);
        }
    }

    @Override
    public void pacEatsGhost(GameContext game, GameLevel level, Ghost ghost) {
        requireNonNull(game);
        requireNonNull(level);
        requireNonNull(ghost);

        // Eating ghost wins 200, 400, 800, 1600 points
        final int nextIndex = level.ghostKillChainSize();
        final int points = game.variant().rules().scoringRules().pointsForGhost(nextIndex);

        scorePoints(game, points, level.number());

        game.variant().systems().ghostState().setState(ghost, GhostState.EATEN);
        //TODO use system
        ghost.state().setKillChainIndex(nextIndex);

        level.addToGhostKillChain(ghost);

        game.eventManager().publishGameEvent(new GhostEatenEvent(ghost));
    }

    // Scoring

    @Override
    public void scorePoints(GameContext game, int points, int levelNumber) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameSession session = game.session();
        final Score gameScore = session.hud().gameScore();
        final Score highScore = session.hud().highScore();

        ScoreSystem.scorePoints(gameScore, highScore, points, levelNumber, game.variant().rules().scoringRules());

        if (gameScore.data().extraLifeReached()) {
            session.setNumLives(session.numLives() + 1);
            game.eventManager().publishGameEvent(new SpecialScoreEvent(gameScore.data().points()));
            // Do not forget to clear the flag!
            gameScore.data().setExtraLifeReached(false);
        }
    }

    // private

    protected void initScores(GameSession session) {
        final Score gameScore = session.hud().gameScore();
        final Score highScore = session.hud().highScore();

        gameScore.reset();
        final File highScoreFile = highScore.reqComp(ScorePersistencyComp.class).file();
        try {
            ScoreSystem.load(highScore);
            ScoreSystem.enableScore(highScore, true);
        } catch (IOException x) {
            Logger.error(x, "Error loading high-score file {}", highScoreFile.getAbsolutePath());
        }
    }

    private void checkIfPacFoundEdibleItem(GameContext game, GameLevel level, GamePlayStep step) {
        final Pac pac = level.entities().pac();
        final GameSystems systems = game.variant().systems();
        final PacDigestionSystem digestionSystem = systems.pacDigestion();
        final ScoringRules scoringRules = game.variant().rules().scoringRules();

        if (step.foodFound()) {
            digestionSystem.endStarving(pac);
            final Vector2i foodTile = step.foodFoundTile();
            level.food().markFoodEatenAt(foodTile);
            if (scoringRules.isBonusAwarded(level)) {
                activateNextBonus(game, level);
            }
            game.eventManager().publishGameEvent(
                new PacEatsFoodEvent(pac, step.energizerFound(), false, game.session().thisFrame().tick()));
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
            step.ghostsKilled().forEach(ghost -> pacEatsGhost(game, level, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    private void fixPacPositionIfKilledInsidePortal(GameLevel level) {
        final Pac pac = level.entities().pac();

        level.worldMap().terrainLayer().hPortalContainingTile(pac.pos().tile()).ifPresent(hPortal -> {
            final Direction moveDir = pac.worldNavigation().moveDir();
            if (moveDir == Direction.LEFT) {
                final float rightmostX = hPortal.rightBorderEntryTile().x() * WorldMap.TS;
                pac.pos().setX(rightmostX);
            } else if (moveDir == Direction.RIGHT) {
                final float leftmostX = hPortal.leftBorderEntryTile().x() * WorldMap.TS;
                pac.pos().setX(leftmostX);
            }
            Logger.info("Detected Pac-Man collision while teleporting, moved Pac-Man back into world");
        });
    }
}
