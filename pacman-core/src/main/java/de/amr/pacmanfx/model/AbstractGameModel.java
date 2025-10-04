/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Common base class of all Pac-Man game models.
 */
public abstract class AbstractGameModel implements Game {

    public static final double BONUS_EATEN_SECONDS = 2;
    public static final int MAX_LEVEL_COUNTER_SYMBOLS = 7;

    protected final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    protected final ObjectProperty<GameLevel> gameLevel = new SimpleObjectProperty<>();

    protected final BooleanProperty levelCounterEnabled = new SimpleBooleanProperty(true);

    protected final IntegerProperty lifeCount = new SimpleIntegerProperty(0);

    protected final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);

    protected final BooleanProperty playing = new SimpleBooleanProperty(false);

    protected final SimulationStep simulationStep = new SimulationStep();

    protected final List<Byte> levelCounterSymbols = new ArrayList<>();

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(gameLevel.get());
    }

    protected void setGameLevel(GameLevel gameLevel) {
        this.gameLevel.set(gameLevel);
    }

    protected GameLevel gameLevel() {
        return gameLevel.get();
    }

    public BooleanProperty levelCounterEnabledProperty() {
        return levelCounterEnabled;
    }

    @Override
    public List<Byte> levelCounterSymbols() {
        return Collections.unmodifiableList(levelCounterSymbols);
    }

    @Override
    public void clearLevelCounter() {
        levelCounterSymbols.clear();
    }

    @Override
    public void updateLevelCounter(int levelNumber, byte symbol) {
        if (levelNumber == 1) {
            levelCounterSymbols.clear();
        }
        if (levelCounterEnabled()) {
            levelCounterSymbols.add(symbol);
            if (levelCounterSymbols.size() > MAX_LEVEL_COUNTER_SYMBOLS) {
                levelCounterSymbols.removeFirst();
            }
        }
    }

    @Override
    public void setLevelCounterEnabled(boolean enabled) {
        levelCounterEnabledProperty().set(enabled);
    }

    @Override
    public boolean levelCounterEnabled() {
        return levelCounterEnabledProperty().get();
    }


    @Override
    public SimulationStep simulationStep() {
        return simulationStep;
    }

    protected void setLifeCount(int n) {
        if (n >= 0) {
            lifeCount.set(n);
        } else {
            Logger.error("Cannot set life count to negative number");
        }
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
    public void showMessage(GameLevel gameLevel, MessageType type) {
        requireNonNull(type);
        GameLevelMessage message = new GameLevelMessage(type);
        message.setPosition(gameLevel.worldMap().terrainLayer().defaultMessagePosition());
        gameLevel.setMessage(message);
    }

    @Override
    public boolean isPlaying() { return playing.get(); }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public void continueGame(GameLevel gameLevel) {
        resetPacManAndGhostAnimations();
        gameLevel.getReadyToPlay();
        gameLevel.showPacAndGhosts();
        eventManager().publishEvent(GameEventType.GAME_CONTINUED);
    }

    @Override
    public void startHunting(GameLevel gameLevel) {
        gameLevel.pac().animationManager().ifPresent(AnimationManager::play);
        gameLevel.ghosts().forEach(ghost -> ghost.animationManager().ifPresent(AnimationManager::play));
        gameLevel.blinking().setStartPhase(Pulse.ON);
        gameLevel.blinking().restart(Integer.MAX_VALUE);
        huntingTimer().startFirstHuntingPhase(gameLevel.number());
        eventManager().publishEvent(GameEventType.HUNTING_PHASE_STARTED);
    }

    @Override
    public void doHuntingStep(GameContext gameContext) {
        optGameLevel().ifPresent(gameLevel -> {
            optGateKeeper().ifPresent(gateKeeper -> gateKeeper.unlockGhosts(gameLevel));
            huntingTimer().update(gameLevel.number());
            gameLevel.blinking().tick();
            gameLevel.pac().tick(gameContext);
            gameLevel.ghosts().forEach(ghost -> ghost.tick(gameContext));
            gameLevel.bonus().ifPresent(bonus -> bonus.tick(gameContext));
            checkIfPacManGetsKilled(gameLevel.pac());
            if (hasPacManBeenKilled()) return;
            checkIfGhostsKilled();
            if (haveGhostsBeenKilled()) return;
            checkIfPacManFindsFood(gameLevel);
            updatePacPower();
            gameLevel.bonus().ifPresent(bonus -> checkIfPacManCanEatBonus(gameLevel, bonus));
        });
    }

    @Override
    public boolean hasPacManBeenKilled() {
        return simulationStep.pacKilledTile != null;
    }

    @Override
    public boolean haveGhostsBeenKilled() {
        return !simulationStep.killedGhosts.isEmpty();
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
    public boolean isLevelCompleted() {
        Optional<FoodLayer> optFoodLayer = optGameLevel().map(gameLevel -> gameLevel.worldMap().foodLayer());
        return optFoodLayer.isPresent() && optFoodLayer.get().uneatenFoodCount() == 0;
    }

    @Override
    public void onLevelCompleted() {
        optGameLevel().ifPresent(gameLevel -> {
            huntingTimer().stop();
            Logger.info("Hunting timer stopped.");
            gameLevel.blinking().setStartPhase(Pulse.OFF);
            gameLevel.blinking().reset();
            Logger.info("Energizer blinking stopped.");
            gameLevel.pac().stopAndShowInFullBeauty();
            gameLevel.pac().powerTimer().stop();
            gameLevel.pac().powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero.");
            gameLevel.bonus().ifPresent(Bonus::setInactive);
            // when cheat triggered end of level, there might still be remaining food:
            gameLevel.worldMap().foodLayer().eatAllFood();
            Logger.trace("Game level #{} completed.", gameLevel.number());
        });
    }

    protected void resetPacManAndGhostAnimations() {
        optGameLevel().ifPresent(gameLevel -> {
            gameLevel.pac().animationManager().ifPresent(am -> {
                am.select(AnimationSupport.ANIM_PAC_MUNCHING);
                am.reset();
            });
            gameLevel.ghosts().forEach(ghost -> ghost.animationManager().ifPresent(am -> {
                am.select(AnimationSupport.ANIM_GHOST_NORMAL);
                am.reset();
            }));
        });
    }

    protected void checkIfPacManGetsKilled(Pac pac) {
        optGameLevel().ifPresent(level -> level.ghosts(GhostState.HUNTING_PAC)
            .filter(pac::sameTilePosition)
            .findFirst().ifPresent(killer -> {
                boolean killed;
                if (level.isDemoLevel()) {
                    killed = !isPacManSafeInDemoLevel();
                } else {
                    killed = !pac.isImmune();
                }
                if (killed) {
                    simulationStep.pacKiller = killer;
                    simulationStep.pacKilledTile = killer.tile();
                }
            }));
    }

    protected abstract boolean isPacManSafeInDemoLevel();

    protected void updatePacPower() {
        optGameLevel().ifPresent(gameLevel -> {
            final TickTimer powerTimer = gameLevel.pac().powerTimer();
            powerTimer.doTick();
            if (gameLevel.pac().isPowerFadingStarting(gameLevel)) {
                simulationStep.pacStartsLosingPower = true;
                eventManager().publishEvent(GameEventType.PAC_STARTS_LOSING_POWER);
            } else if (powerTimer.hasExpired()) {
                powerTimer.stop();
                powerTimer.reset(0);
                Logger.info("Power timer stopped and reset to zero");
                gameLevel.victims().clear();
                huntingTimer().start();
                Logger.info("Hunting timer restarted because Pac-Man lost power");
                gameLevel.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                simulationStep.pacLostPower = true;
                eventManager().publishEvent(GameEventType.PAC_LOST_POWER);
            }
        });
    }

    protected void checkIfGhostsKilled() {
        optGameLevel().ifPresent(gameLevel -> gameLevel.ghosts(GhostState.FRIGHTENED)
            .filter(ghost -> gameLevel.pac().sameTilePosition(ghost))
            .forEach(this::onGhostKilled));
    }

    protected abstract void checkIfPacManFindsFood(GameLevel gameLevel);

    protected abstract boolean isBonusReached(GameLevel gameLevel);

    protected void checkIfPacManCanEatBonus(GameLevel gameLevel, Bonus bonus) {
        if (bonus.state() == BonusState.EDIBLE && gameLevel.pac().sameTilePosition(bonus)) {
            bonus.setEaten(TickTimer.secToTicks(BONUS_EATEN_SECONDS));
            scoreManager().scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            simulationStep.bonusEatenTile = bonus.tile();
            eventManager().publishEvent(GameEventType.BONUS_EATEN);
        }
    }
}