/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.math.RandomNumberSupport;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.GamePlaySoundEffects;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.assets.Translator;
import de.amr.pacmanfx.uilib.model3D.actor.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.actor.GhostAppearance3D;
import de.amr.pacmanfx.uilib.model3D.actor.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.animation.SequentialTransition;
import javafx.scene.shape.Shape3D;
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

    public void init(GamePlaySoundEffects soundEffects, Translator translator) {
        this.soundEffects = requireNonNull(soundEffects);
        pickerGameOverMessages = RandomTextPicker.fromBundle(translator.localizedTexts(), "game.over");
    }

    /**
     * Dispatches game state change events to the appropriate handler method.
     *
     * @param ui the game UI
     * @param event   the state change event
     * @param level3D the current 3D level representation (may be null)
     */
    public void handleGameStateChange(GameUI ui, GameStateChangeEvent event, GameLevel3D level3D) {
        requireNonNull(event);
        if (level3D == null) {
            Logger.warn("Ignoring game state change event: level3D is null");
            return;
        }

        final State<Game> gameState = event.newState();
        if (matches(gameState, STARTING_GAME_OR_LEVEL)) {
            onStartingGame(level3D);
        } else if (matches(gameState, HUNTING)) {
            onHuntingStart(level3D);
        } else if (matches(gameState, PACMAN_DYING)) {
            onPacManDying(level3D);
        } else if (matches(gameState, EATING_GHOST)) {
            onEatingGhost(ui, level3D);
        } else if (matches(gameState, LEVEL_COMPLETE)) {
            onLevelComplete(level3D);
        } else if (matches(gameState, GAME_OVER)) {
            onGameOver(ui, level3D);
        }
    }

    private static boolean matches(State<Game> gameState, GameControl.CommonGameState expected) {
        return gameState.nameMatches(expected.name());
    }

    /**
     * Handles bonus activation: updates 3D representation and plays sound.
     */
    public void onBonusActivated(GameUI ui, BonusActivatedEvent gameEvent, GameLevel3D level3D) {
        level3D.addOrReplaceBonus3D(ui.currentConfig(), gameEvent.bonus());
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
            final Pac3D pac3D = level3D.pac3D().orElseThrow();
            level3D.resetPacZPosition(pac3D);
            level3D.messageManager().showReadyMessage();
        }
    }

    /**
     * Plays game ready sound unless in demo or test mode.
     */
    public void onGameStarts(GameStartedEvent event, GameLevel3D level3D) {
        final Game game = event.game();
        final State<Game> state = game.control().state();
        final boolean silent = game.isDemoLevelRunning() || state instanceof TestState;
        if (!silent) {
            soundEffects.playGameReadySound();
        }
        if (level3D != null) {
            final Pac3D pac3D = level3D.pac3D().orElseThrow();
            level3D.resetPacZPosition(pac3D);
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
    public void onPacEatsFood(PacEatsFoodEvent gameEvent, GameLevel3D level3D, long tick) {
        final MazeFood3D mazeFood3D = level3D.food3D();
        if (gameEvent.allPellets()) {
            mazeFood3D.removeAllPellets3D(level3D);
        } else {
            mazeFood3D.removeFoodAt(level3D, gameEvent.pac().tile());
            soundEffects.playPacMunchingSound(tick);
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
            final Pac3D pac3D = level3D.pac3D().orElseThrow();
            pac3D.setMovementPowerMode(true);
            level3D.animations().ifPresent(animations -> animations.wallColorFlashingAnimation().playFromStart());
            soundEffects.playPacPowerSound();
        }
    }

    /**
     * Handles Pac losing power: stops power animation/sound.
     */
    public void onPacLostPower(PacLostPowerEvent ignoredEvent, GameLevel3D level3D) {
        final Pac3D pac3D = level3D.pac3D().orElseThrow();
        pac3D.setMovementPowerMode(false);
        level3D.animations().ifPresent(animations -> animations.wallColorFlashingAnimation().stop());
        soundEffects.stopPacPowerSound();
    }

    public void onSpecialScoreReached(SpecialScoreReachedEvent ignoredEvent, GameLevel3D ignoredLevel3D) {
        soundEffects.playExtraLifeSound();
    }

    // Private state-specific handlers

    private void onStartingGame(GameLevel3D level3D) {
        level3D.food3D().energizers3D().forEach(Energizer3D::stopPumping);
        level3D.init(level3D.level());
    }

    private void onHuntingStart(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final Pac3D pac3D = level3D.pac3D().orElseThrow();
        pac3D.init(level);
        level3D.ghostAppearances3D().forEach(ghost3D -> ghost3D.init(level));
        level3D.food3D().energizers3D().forEach(Energizer3D::startPumping);
        level3D.food3D().startParticlesAnimation();
        level3D.animations().ifPresent(animations -> animations.ghostLightAnimation().playFromStart());
    }

    private void onPacManDying(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final TickTimer stateTimer = level.game().control().state().timer();
        final Pac3D pac3D = level3D.pac3D().orElseThrow();

        soundEffects.stopAll();
        level3D.animations().ifPresent(animations -> {
            animations.ghostLightAnimation().stop();
            animations.wallColorFlashingAnimation().stop();
        });
        level3D.ghostAppearances3D().forEach(GhostAppearance3D::stopAllAnimations);
        level3D.bonus3D().ifPresent(Bonus3D::expire);

        // One last update before dying animation
        pac3D.update(level);

        stateTimer.resetIndefiniteTime(); // freeze until animation ends
        final var dyingAnimation = new SequentialTransition(
            pauseSec(1.5),
            doNow(soundEffects::playPacDeadSound),
            pac3D.dyingAnimation().animationFX(),
            pauseSec(0.5)
        );
        dyingAnimation.setOnFinished(_ -> {
            pac3D.setVisible(false);
            level3D.resetPacZPosition(pac3D);
            stateTimer.expire();
        });
        dyingAnimation.play();
    }

    private void onEatingGhost(GameUI ui, GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            final int killedIndex = level.energizerVictims().indexOf(killedGhost);
            final GhostAppearance3D ghostAppearance3D = level3D.ghostAppearances3D().toList().get(killedGhost.personality());
            final Shape3D numberShape3D = ui.currentConfig().factory3D().createNumberShape3D(ui.currentConfig(), killedIndex);
            ghostAppearance3D.setNumberShape3D(numberShape3D);
        });
    }

    private void onLevelComplete(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final State<Game> gameState = level.game().control().state();
        final Maze3D maze3D = level3D.maze3D().orElseThrow();

        soundEffects.stopAll();
        level3D.animationRegistry().stopAllAnimations();
        cleanupFoodAndParticles(level3D);
        maze3D.house().hideDoors();
        level3D.bonus3D().ifPresent(Bonus3D::expire);
        level3D.messageManager().hideMessage();
        level3D.animations().ifPresentOrElse(
            animations -> animations.playLevelEndAnimation(maze3D, level, gameState),
            () -> pauseSecThen(2, () -> gameState.timer().expire()).play()
        );
    }

    private void onGameOver(GameUI ui, GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final State<Game> gameState = level.game().control().state();

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
        final MazeFood3D food3D = level3D.food3D();
        food3D.stopParticlesAnimation();
        food3D.energizers3D().forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        food3D.pellets3D().forEach(pellet3D -> pellet3D.shape().setVisible(false));
        level3D.maze3D().ifPresent(maze3D -> maze3D.particlesGroup().getChildren().clear());
    }
}