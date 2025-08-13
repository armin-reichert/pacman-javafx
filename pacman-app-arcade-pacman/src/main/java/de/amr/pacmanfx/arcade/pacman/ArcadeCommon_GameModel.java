/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GateKeeper;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 */
public abstract class ArcadeCommon_GameModel extends AbstractGameModel {

    public static final byte PELLET_VALUE = 10;
    public static final byte ENERGIZER_VALUE = 50;
    public static final int ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS = 12_000;
    public static final int EXTRA_LIFE_SCORE = 10_000;
    public static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    protected final GameContext gameContext;
    protected MapSelector mapSelector;
    protected GateKeeper gateKeeper;
    protected Steering autopilot;
    protected Steering demoLevelSteering;
    protected int cruiseElroy;

    protected ArcadeCommon_GameModel(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    // GameEvents interface

    @Override
    public void onPelletEaten() {
        scoreManager().scorePoints(PELLET_VALUE);
        level.pac().setRestingTicks(1);
        updateCruiseElroyMode();
    }

    @Override
    public void onEnergizerEaten(Vector2i tile) {
        simulationStep.foundEnergizerAtTile = tile;
        scoreManager().scorePoints(ENERGIZER_VALUE);
        level.pac().setRestingTicks(3);
        updateCruiseElroyMode();

        level.victims().clear();
        level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseAtNextOccasion);

        double powerSeconds = pacPowerSeconds(level);
        if (powerSeconds > 0) {
            huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            long ticks = TickTimer.secToTicks(powerSeconds);
            level.pac().powerTimer().restartTicks(ticks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", ticks, powerSeconds);
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            simulationStep.pacGotPower = true;
            eventManager().publishEvent(GameEventType.PAC_GETS_POWER);
        }
    }

    @Override
    public void onPacKilled() {
        gateKeeper.resetCounterAndSetEnabled(true);
        huntingTimer().stop();
        activateCruiseElroyMode(false);
        level.pac().powerTimer().stop();
        level.pac().powerTimer().reset(0);
        level.pac().sayGoodbyeCruelWorld();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        simulationStep.killedGhosts.add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager().scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        level.registerGhostKilled();
        if (level.numGhostsKilled() == 16) {
            scoreManager().scorePoints(ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS);
            Logger.info("Scored {} points for killing all ghosts in level {}",
                    ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS, level.number());
        }
    }

    @Override
    public void onGameEnding() {
        setPlaying(false);
        if (!gameContext.coinMechanism().isEmpty()) {
            gameContext.coinMechanism().consumeCoin();
        }
        scoreManager().updateHighScore();
        level.showMessage(GameLevel.MESSAGE_GAME_OVER);
    }

    // GameLifecycle interface

    @Override
    public void init() {
        setInitialLifeCount(3);
        //mapSelector.loadAllMaps();
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
        hudData().theLevelCounter().clear();
    }

    @Override
    public void prepareForNewGame() {
        setPlaying(false);
        setLifeCount(initialLifeCount());
        level = null;
        scoreManager().loadHighScore();
        scoreManager().resetScore();
        gateKeeper.reset();
        huntingTimer().reset();
    }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        hudData().theLevelCounter().clear();
        buildNormalLevel(1);
        eventManager().publishEvent(GameEventType.GAME_STARTED);
    }

    @Override
    public boolean canStartNewGame() { return !gameContext.coinMechanism().isEmpty(); }

    @Override
    public boolean canContinueOnGameOver() { return false; }

    @Override
    public Optional<GateKeeper> gateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public double pacPowerSeconds(GameLevel level) {
        return level != null ? level.data().pacPowerSeconds() : 0;
    }

    @Override
    public double pacPowerFadingSeconds(GameLevel level) {
        return level != null ? level.data().numFlashes() * 0.5 : 0;
    }

    @Override
    protected void checkIfPacManFindsFood() {
        Vector2i tile = level.pac().tile();
        if (level.tileContainsFood(tile)) {
            level.pac().starvingIsOver();
            level.registerFoodEatenAt(tile);
            if (level.isEnergizerPosition(tile)) {
                onEnergizerEaten(tile);
            } else {
                onPelletEaten();
            }
            gateKeeper.registerFoodEaten(level);
            if (isBonusReached()) {
                activateNextBonus();
                simulationStep.bonusIndex = level.currentBonusIndex();
            }
            eventManager().publishEvent(GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starve();
        }
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        createLevel(levelNumber);
        level.setDemoLevel(false);
        level.pac().immuneProperty().bind(gameContext.gameController().propertyImmunity());
        level.pac().usingAutopilotProperty().bind(gameContext.gameController().propertyUsingAutopilot());
        hudData().theLevelCounter().setEnabled(true);
        huntingTimer().reset();
        scoreManager().setScoreLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        level.house().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        int levelNumber = 1;
        createLevel(levelNumber);
        level.setDemoLevel(true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        hudData().theLevelCounter().setEnabled(true);
        huntingTimer().reset();
        scoreManager().setScoreLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        level.house().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void startLevel() {
        level.setStartTime(System.currentTimeMillis());
        level.getReadyToPlay();
        resetPacManAndGhostAnimations();
        if (level.isDemoLevel()) {
            level.showMessage(GameLevel.MESSAGE_GAME_OVER);
            scoreManager().score().setEnabled(false);
            scoreManager().highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            hudData().theLevelCounter().update(level.number(), level.bonusSymbol(0));
            level.showMessage(GameLevel.MESSAGE_READY);
            scoreManager().score().setEnabled(true);
            scoreManager().highScore().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        eventManager().publishEvent(GameEventType.LEVEL_STARTED);
    }

    @Override
    public void startNextLevel() {
        buildNormalLevel(level.number() + 1);
        startLevel();
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }

    /**
     * @return "Cruise Elroy" state (changes behavior of red ghost).
     * <p>0=off, 1=Elroy1, 2=Elroy2, -1=Elroy1 (disabled), -2=Elroy2 (disabled).</p>
     */
    public int cruiseElroy() { return cruiseElroy; }

    public boolean isCruiseElroyModeActive() { return cruiseElroy > 0; }

    protected void activateCruiseElroyMode(boolean active) {
        int absValue = Math.abs(cruiseElroy);
        cruiseElroy = active ? absValue : -absValue;
    }

    private void updateCruiseElroyMode() {
        if (level.uneatenFoodCount() == level.data().elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (level.uneatenFoodCount() == level.data().elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
    }
}