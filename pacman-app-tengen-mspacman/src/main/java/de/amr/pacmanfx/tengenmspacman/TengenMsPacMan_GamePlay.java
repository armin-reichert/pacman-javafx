/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.HPortal;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.score.PersistentScore;
import de.amr.pacmanfx.score.Score;
import de.amr.pacmanfx.simulation.GamePlay;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomBoolean;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay implements GamePlay {

    public static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    public TengenMsPacMan_GamePlay() {
    }

    // Game start

    @Override
    public void init(GameModel model) {
        requireNonNull(model);

        if (!(model instanceof TengenMsPacMan_GameModel tengenModel)) {
            throw new IllegalArgumentException("Illegal model type");
        }

        tengenModel.mapSelector().loadMapPrototypes();
        tengenModel.lives().setInitialCount(3);
        tengenModel.hudState().hideIt();
        resetForNewGame(tengenModel);

        tengenModel.setPacBoosterMode(DEFAULT_PAC_BOOSTER);
        tengenModel.setDifficulty(DEFAULT_DIFFICULTY);
        tengenModel.setMapCategory(DEFAULT_MAP_CATEGORY);
        tengenModel.setStartLevelNumber(DEFAULT_START_LEVEL);
        tengenModel.setNumContinues(DEFAULT_NUM_CONTINUES);
    }

    @Override
    public boolean canStartNewGame(GameContext context) {
        requireNonNull(context);

        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) context.model();
        return model.canStartNewGame();
    }

    @Override
    public void resetForNewGame(GameModel model) {

        if (!(model instanceof TengenMsPacMan_GameModel tengenModel)) {
            throw new IllegalArgumentException("Illegal model type");
        }

        tengenModel.lives().setCount(tengenModel.lives().initialCount());
        tengenModel.score().reset();

        final PersistentScore highScore = tengenModel.highScore();
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

        tengenModel.gateKeeper().reset();
        tengenModel.levelCounter().clear();

        tengenModel.setLevel(null);
        tengenModel.setPlaying(false);

        tengenModel.setBoosterActive(false);
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

        final GameLevel demoLevel = model.createLevel(1, true);
        demoLevel.setGameOverStateTicks(120);

        final Pac pac = demoLevel.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        final var demoLevelSteering = new RuleBasedPacSteering();
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        model.gateKeeper().setLevelNumber(1);
        model.score().setLevelNumber(1);

        return demoLevel;
    }

    @Override
    public boolean isDemoLevelRunning(GameContext context) {
        return context.model().optGameLevel().isPresent() && context.model().assertLevel().isDemoLevel();
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        return runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS;
    }

    @Override
    public void buildNormalLevel(GameEventManager eventManager, GameModel model, int levelNumber) {
        requireNonNull(eventManager);
        requireNonNull(model);
        Validations.requireValidLevelNumber(levelNumber);

        final GameLevel newLevel = model.createLevel(levelNumber, false);
        model.score().setLevelNumber(levelNumber);
        model.gateKeeper().setLevelNumber(levelNumber);
        model.setLevel(newLevel);

        eventManager.publishGameEvent(new LevelCreatedEvent(newLevel));
    }

    @Override
    public void startNextLevel(GameEventManager eventManager, GameModel model, GameLevel level) {
        requireNonNull(eventManager);
        requireNonNull(model);
        requireNonNull(level);

        if (level.number() < model.rules().lastLevelNumber()) {
            buildNormalLevel(eventManager, model, level.number() + 1);
            startLevel(eventManager, model, level);
            // Note: This event is very important because it triggers the creation of the actor animations!
            eventManager.publishGameEvent(new LevelStartedEvent(level));
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", model.rules().lastLevelNumber());
        }
    }

    @Override
    public void startLevel(GameEventManager eventManager, GameModel model, GameLevel level) {
        requireNonNull(eventManager);
        requireNonNull(model);
        requireNonNull(level);

        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(level);

        // In Tengen, actors are shown immediately
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        if (model instanceof TengenMsPacMan_GameModel tengenModel) {
            if (tengenModel.pacBoosterMode() == PacBooster.ALWAYS_ON) {
                tengenModel.activatePacBooster(level.entities().pac(), true);
            }
            tengenModel.showMessage(level, GameLevelMessageType.READY);
        }
        else {
            throw new IllegalArgumentException("Illegal model type");
        }

        model.levelCounter().update(level.number(), level.bonusSymbolCode(0));
        model.score().setEnabled(true);

        //TODO fixme
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
    public void eatPellet(GameEventManager eventManager, GameModel model, GameLevel level, Vector2i tile) {
        requireNonNull(eventManager);
        requireNonNull(model);
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(eventManager, model, model.rules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level, level.worldMap().terrainLayer().house());
        level.entities().pac().setRestingTicks(model.rules().restingTicksForPellet());
    }

    @Override
    public void eatEnergizer(GameEventManager eventManager, GameModel model, GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(eventManager, model, model.rules().pointsForEnergizer(), level.number());
        model.gateKeeper().registerFoodEaten(level, level.worldMap().terrainLayer().house());
        level.clearGhostKillChain();
        startPacPowerMode(eventManager, model, level, level.entities().pac());
    }

    @Override
    public void eatBonus(GameEventManager eventManager, GameModel model, GameLevel level, Bonus bonus) {
        requireNonNull(eventManager);
        requireNonNull(model);
        requireNonNull(level);
        requireNonNull(bonus);

        scorePoints(eventManager, model, bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(model.rules().eatenBonusDisplaySeconds());

        eventManager.publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public void eatGhost(GameEventManager eventManager, GameModel model, GameLevel level, Ghost eatenGhost) {
        requireNonNull(eventManager);
        requireNonNull(model);
        requireNonNull(level);
        requireNonNull(eatenGhost);

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
    public void activateNextBonus(GameEventManager eventManager, GameModel model, GameLevel level) {
        //TODO Find out how Tengen really implemented this
        if (level.optBonus().isPresent() && level.optBonus().get().state() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final TerrainLayer terrain = level.worldMap().terrainLayer();

        final House house = terrain.optHouse().orElse(null);
        if (house == null) {
            Logger.error("\"Cannot activate next bonus: No house exists in game level!");
            return;
        }

        if (terrain.horizontalPortals().isEmpty()) {
            Logger.error("Cannot activate next bonus: No portal exists in game level");
            return;
        }

        final Vector2i houseEntry = WorldMap.computeTileAt(house.entryPosition());
        final Vector2i houseEntryOpposite = houseEntry.plus(0, house.sizeInTiles().y() + 1);

        final List<HPortal> portals = terrain.horizontalPortals();
        final HPortal entryPortal = portals.get(randomInt(0, portals.size()));
        final HPortal exitPortal  = portals.get(randomInt(0, portals.size()));

        final boolean leftToRight = randomBoolean();
        final List<Vector2i> route = List.of(
            leftToRight ? entryPortal.leftBorderEntryTile() : entryPortal.rightBorderEntryTile(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightBorderEntryTile().plus(1, 0) : exitPortal.leftBorderEntryTile().minus(1, 0)
        );

        level.selectNextBonus();

        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final Bonus bonus = new Bonus(symbolCode, model.rules().pointsForBonus(symbolCode));
        bonus.setMazeRoute(route, leftToRight);
        bonus.showEdibleAndStartWandering(model.rules().actorSpeedControl().bonusSpeed(level));
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }

    @Override
    public void startPacPowerMode(GameEventManager eventManager, GameModel model, GameLevel level, Pac pac) {
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
    public void updatePacPowerMode(GameEventManager eventManager, GameModel model, GameLevel level, Pac pac) {
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
}
