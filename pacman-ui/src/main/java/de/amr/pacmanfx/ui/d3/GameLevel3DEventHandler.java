/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.math.RandomNumberSupport;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.GamePlaySoundEffects;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.model3D.actor.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.actor.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.animation.SequentialTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.model.GameControl.CommonGameState.*;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * Event handler that reacts to game model events and updates the 3D representation
 * accordingly (animations, visibility, sounds, messages, etc.).
 * <p>
 * This class is stateless except for injected dependencies and serves as a clean
 * bridge between the game model and the 3D scene components.
 * </p>
 */
public class GameLevel3DEventHandler {

    private GamePlaySoundEffects soundEffects;
    private RandomTextPicker<String> pickerGameOverMessages;
    private GameUI ui;

    public void init(GameUI ui, GamePlaySoundEffects soundEffects) {
        this.ui = requireNonNull(ui);
        this.soundEffects = requireNonNull(soundEffects);
        pickerGameOverMessages = RandomTextPicker.fromBundle(ui.localizedTexts(), "game.over");
    }

    /**
     * Dispatches game state change events to the appropriate handler method.
     *
     * @param event   the state change event
     * @param level3D the current 3D level representation (may be null)
     */
    public void handleGameStateChange(GameStateChangeEvent event, GameLevel3D level3D) {
        requireNonNull(event);
        if (level3D == null) {
            Logger.warn("Ignoring game state change event: level3D is null");
            return;
        }

        final State<Game> gameState = event.newState();
        if (stateMatches(gameState, STARTING_GAME_OR_LEVEL)) {
            onStartingGame(level3D);
        } else if (stateMatches(gameState, HUNTING)) {
            onHuntingStart(level3D);
        } else if (stateMatches(gameState, PACMAN_DYING)) {
            onPacManDying(level3D);
        } else if (stateMatches(gameState, EATING_GHOST)) {
            onEatingGhost(level3D);
        } else if (stateMatches(gameState, LEVEL_COMPLETE)) {
            event.game().optGameLevel().ifPresent(level -> onLevelComplete(level3D, level));
        } else if (stateMatches(gameState, GAME_OVER)) {
            onGameOver(level3D);
        }
    }

    private static boolean stateMatches(State<Game> gameState, GameControl.CommonGameState expected) {
        return gameState.nameMatches(expected.name());
    }

    /**
     * Handles bonus activation: updates 3D representation and plays sound.
     */
    public void onBonusActivated(BonusActivatedEvent gameEvent, GameLevel3D level3D) {
        level3D.replaceBonus3D(ui.currentConfig(), gameEvent.bonus());
        soundEffects.playBonusActiveSound();
    }

    /**
     * Handles bonus eaten: shows eaten animation and plays sound.
     */
    public void onBonusEaten(BonusEatenEvent ignoredEvent, GameLevel3D level3D) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        soundEffects.playBonusEatenSound();
    }

    /**
     * Handles bonus expiration: expires 3D bonus and plays sound.
     */
    public void onBonusExpired(BonusExpiredEvent ignoredEvent, GameLevel3D level3D) {
        level3D.bonus3D().ifPresent(Bonus3D::expire);
        soundEffects.playBonusExpiredSound();
    }

    /**
     * Shows the "READY!" message when the game continues.
     */
    public void onGameContinues(GameContinuedEvent ignoredEvent, GameLevel3D level3D) {
        if (level3D != null) {
            level3D.messageManager().showReadyMessage();
        }
    }

    /**
     * Plays game ready sound unless in demo or test mode.
     */
    public void onGameStarts(GameStartedEvent ignoredEvent, GameLevel3D ignoredLevel3D) {
        final Game game = ui.gameContext().currentGame();
        final State<Game> state = game.control().state();
        final boolean silent = game.level().isDemoLevel() || state instanceof TestState;
        if (!silent) {
            soundEffects.playGameReadySound();
        }
    }

    /**
     * Plays sound when a ghost is eaten.
     */
    public void onGhostEaten(GhostEatenEvent ignoredEvent, GameLevel3D ignoredLevel3D) {
        soundEffects.playGhostEatenSound();
    }

    /**
     * Handles Pac eating food: updates 3D food and plays munching sound (with rate limiting).
     */
    public void onPacEatsFood(PacEatsFoodEvent gameEvent, GameLevel3D level3D) {
        final MazeFood3D mazeFood3D = level3D.maze3D().food();
        if (gameEvent.allPellets()) {
            mazeFood3D.removeAllPellets3D(level3D);
        } else {
            mazeFood3D.removeFoodAt(level3D, gameEvent.pac().tile());
            soundEffects.playPacMunchingSound(ui.gameContext().clock().tickCount());
        }
    }

    /**
     * Handles Pac gaining power: stops siren, starts power animation/sound.
     */
    public void onPacGetsPower(PacGetsPowerEvent ignoredEvent, GameLevel3D level3D) {
        final GameLevel gameLevel = level3D.level();
        final Game game = gameLevel.game();
        soundEffects.stopSiren();
        if (!game.isLevelCompleted(gameLevel)) {
            level3D.pac3D().setMovementPowerMode(true);
            level3D.animations().ifPresent(animations -> animations.wallColorFlashingAnimation().playFromStart());
            soundEffects.playPacPowerSound();
        }
    }

    /**
     * Handles Pac losing power: stops power animation/sound.
     */
    public void onPacLostPower(PacLostPowerEvent ignoredEvent, GameLevel3D level3D) {
        level3D.pac3D().setMovementPowerMode(false);
        level3D.animations().ifPresent(animations -> animations.wallColorFlashingAnimation().stop());
        soundEffects.stopPacPowerSound();
    }

    public void onSpecialScoreReached(SpecialScoreReachedEvent ignoredEvent, GameLevel3D ignoredLevel3D) {
        soundEffects.playExtraLifeSound();
    }

    // Private state-specific handlers

    private void onStartingGame(GameLevel3D level3D) {
        level3D.maze3D().food().energizers3D().forEach(Energizer3D::stopPumping);
        level3D.rebuildLevelCounter3D(ui.currentConfig().entityConfig().levelCounter());
    }

    private void onHuntingStart(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        level3D.pac3D().init(level);
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(level));
        level3D.maze3D().food().energizers3D().forEach(Energizer3D::startPumping);
        level3D.maze3D().food().startParticlesAnimation();
        level3D.animations().ifPresent(animations -> animations.ghostLightAnimation().playFromStart());
    }

    private void onPacManDying(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final State<Game> gameState = ui.gameContext().currentGameState();

        soundEffects.stopAll();
        level3D.animations().ifPresent(animations -> {
            animations.ghostLightAnimation().stop();
            animations.wallColorFlashingAnimation().stop();
        });
        level3D.ghosts3D().forEach(MutableGhost3D::stopAllAnimations);
        level3D.bonus3D().ifPresent(Bonus3D::expire);

        // One last update before dying animation
        level3D.pac3D().update(level);

        gameState.timer().resetIndefiniteTime(); // freeze until animation ends
        final var dyingAnimation = new SequentialTransition(
            pauseSec(1.5),
            doNow(soundEffects::playPacDeadSound),
            level3D.pac3D().dyingAnimation().animationFX(),
            pauseSec(0.5)
        );
        dyingAnimation.setOnFinished(_ -> gameState.timer().expire());
        dyingAnimation.play();
    }

    private void onEatingGhost(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            final int killedIndex = level.energizerVictims().indexOf(killedGhost);
            final Image numberImage = ui.currentConfig().killedGhostPointsImage(killedIndex);
            level3D.ghosts3D().get(killedGhost.personality()).setNumberImage(numberImage);
        });
    }

    private void onLevelComplete(GameLevel3D level3D, GameLevel level) {
        final State<Game> gameState = level.game().control().state();

        soundEffects.stopAll();
        level3D.animationRegistry().stopAllAnimations();

        cleanupFoodAndParticles(level3D);

        level3D.maze3D().house().hideDoors();
        level3D.bonus3D().ifPresent(Bonus3D::expire);

        level3D.messageManager().hideMessage();

        level3D.animations().ifPresentOrElse(
            animations -> animations.playLevelEndAnimation(level3D.maze3D(), level, gameState),
            () -> pauseSecThen(2, () -> gameState.timer().expire()).play()
        );
    }

    private void onGameOver(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final State<Game> gameState = ui.gameContext().currentGameState();

        gameState.timer().restartSeconds(3);

        level3D.animations().ifPresent(animations -> animations.ghostLightAnimation().stop());

        cleanupFoodAndParticles(level3D);
        level3D.bonus3D().ifPresent(Bonus3D::expire);

        soundEffects.playGameOverSound();

        final boolean showMsg = RandomNumberSupport.chance(0.25);
        if (!level.isDemoLevel() && showMsg) {
            ui.showFlashMessage(Duration.seconds(2.5), pickerGameOverMessages.nextText());
        }
    }

    private static void cleanupFoodAndParticles(GameLevel3D level3D) {
        final MazeFood3D food3D = level3D.maze3D().food();
        food3D.stopParticlesAnimation();
        food3D.energizers3D().forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        food3D.pellets3D().forEach(pellet3D -> pellet3D.shape().setVisible(false));
        level3D.maze3D().particlesGroup().getChildren().clear();
    }
}