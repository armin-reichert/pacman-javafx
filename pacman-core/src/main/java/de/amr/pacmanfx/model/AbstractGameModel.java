/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Common base class of all Pac-Man game models.
 */
public abstract class AbstractGameModel implements Game {

    protected final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    protected final ObjectProperty<GameLevel> gameLevel = new SimpleObjectProperty<>();

    protected final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);

    protected final IntegerProperty lifeCount = new SimpleIntegerProperty(0);

    protected final BooleanProperty playing = new SimpleBooleanProperty(false);

    protected final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);

    protected final SimulationStepEvents thisStep = new SimulationStepEvents();

    protected final GameContext gameContext;
    protected final ScoreManager scoreManager;

    protected AbstractGameModel(GameContext gameContext) {
        this.gameContext = requireNonNull(gameContext);
        this.scoreManager = new ScoreManager(this);
        cheatUsed.addListener((py, ov, cheated) -> {
            if (cheated) {
                Score highScore = scoreManager.highScore();
                if (highScore.isEnabled()) {
                    highScore.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void prepareForNewGame() {
        cheatUsed.set(false);
    }

    protected abstract void checkPacFindsFood(GameLevel gameLevel);

    protected abstract void checkPacFindsBonus(GameLevel gameLevel);

    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    protected abstract void resetPacManAndGhostAnimations(GameLevel gameLevel);

    public void setLifeCount(int n) {
        if (n >= 0) {
            lifeCount.set(n);
        } else {
            Logger.error("Cannot set life count to negative number");
        }
    }

    public void setGameLevel(GameLevel gameLevel) {
        this.gameLevel.set(gameLevel);
    }

    public GameLevel gameLevel() {
        return gameLevel.get();
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(gameLevel.get());
    }

    @Override
    public SimulationStepEvents simulationStepResults() {
        return thisStep;
    }

    @Override
    public ScoreManager scoreManager() {
        return scoreManager;
    }

    @Override
    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    @Override
    public boolean cheatUsed() {
        return cheatUsed.get();
    }

    @Override
    public void setCheatUsed(boolean b) {
        cheatUsed.set(b);
    }

    @Override
    public int lifeCount() {
        return lifeCount.get();
    }

    @Override
    public void addLives(int n) {
        setLifeCount(lifeCount() + n);
    }

    @Override
    public boolean isPlaying() { return playing.get(); }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public void continueGame(GameLevel gameLevel) {
        resetPacManAndGhostAnimations(gameLevel);
        gameLevel.getReadyToPlay();
        gameLevel.showPacAndGhosts();
        eventManager().publishEvent(GameEventType.GAME_CONTINUED);
    }

    @Override
    public boolean hasPacManBeenKilled() {
        return thisStep.pacKiller != null;
    }

    @Override
    public boolean haveGhostsBeenKilled() {
        return !thisStep.killedGhosts.isEmpty();
    }

    @Override
    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    @Override
    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }

    @Override
    public int initialLifeCount() {
        return initialLifeCount.get();
    }

    @Override
    public void setInitialLifeCount(int count) {
        initialLifeCount.set(count);
    }

    @Override
    public boolean isLevelCompleted(GameLevel gameLevel) {
        return gameLevel.worldMap().foodLayer().uneatenFoodCount() == 0;
    }

    @Override
    public void startHunting(GameLevel gameLevel) {
        gameLevel.pac().optAnimationManager().ifPresent(AnimationManager::play);
        gameLevel.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
        gameLevel.blinking().setStartState(Pulse.State.ON);
        gameLevel.blinking().restart();
        gameLevel.huntingTimer().startFirstPhase();
        eventManager().publishEvent(GameEventType.HUNTING_PHASE_STARTED);
    }

    protected void makeHuntingStep(GameLevel gameLevel) {
        final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
        // Ghosts colliding with Pac? Collision is tile-based!
        thisStep.ghostsCollidingWithPac.clear();
        gameLevel.ghosts()
            .filter(ghost -> !terrain.isTileInPortalSpace(ghost.tile()))
            .filter(ghost -> ghost.onSameTileAs(gameLevel.pac()))
            .forEach(thisStep.ghostsCollidingWithPac::add);

        if (!thisStep.ghostsCollidingWithPac.isEmpty()) {
            // Pac killed? Might stay alive when immune or in demo level safe time!
            checkPacKilled(gameLevel);
            if (hasPacManBeenKilled()) return;

            // Ghost(s) killed?
            thisStep.ghostsCollidingWithPac.stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(thisStep.killedGhosts::add);
            if (haveGhostsBeenKilled()) {
                thisStep.killedGhosts.forEach(ghost -> onGhostKilled(gameLevel, ghost));
                return;
            }
        }

        checkPacFindsFood(gameLevel);
        checkPacFindsBonus(gameLevel);

        updatePacPower(gameLevel);
        gameLevel.blinking().tick();
        gameLevel.huntingTimer().update();
    }

    @Override
    public void onLevelCompleted(GameLevel gameLevel) {
        gameLevel.huntingTimer().stop();
        // If level was ended by cheat, there might still be food remaining, so eat it:
        gameLevel.worldMap().foodLayer().eatAll();
        gameLevel.blinking().setStartState(Pulse.State.OFF);
        gameLevel.blinking().reset();
        gameLevel.pac().onLevelCompleted();
        gameLevel.bonus().ifPresent(Bonus::setInactive);
    }

    protected void checkPacKilled(GameLevel gameLevel) {
        boolean demoLevel = gameLevel.isDemoLevel();
        if (demoLevel && isPacSafeInDemoLevel(gameLevel) || !demoLevel && gameLevel.pac().isImmune()) {
            return;
        }
        thisStep.pacKiller = thisStep.ghostsCollidingWithPac.stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst().orElse(null);
    }

    protected void updatePacPower(GameLevel gameLevel) {
        final Pac pac = gameLevel.pac();
        final TickTimer powerTimer = pac.powerTimer();
        powerTimer.doTick();
        if (pac.isPowerFadingStarting(gameLevel)) {
            thisStep.pacStartsLosingPower = true;
            Logger.info("{} starts losing power", pac.name());
            eventManager().publishEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            thisStep.pacLostPower = true;
            Logger.info("{} lost power", pac.name());
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            gameLevel.energizerVictims().clear();
            gameLevel.huntingTimer().start();
            Logger.info("Hunting timer restarted because {} lost power", pac.name());
            gameLevel.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
            eventManager().publishEvent(GameEventType.PAC_LOST_POWER);
        }
    }
}