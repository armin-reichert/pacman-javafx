/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.math.RandomNumberSupport;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.PlayingSoundEffects;
import de.amr.pacmanfx.uilib.model3D.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.animation.SequentialTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.tinylog.Supplier;

import static de.amr.pacmanfx.model.GameControl.CommonGameState.*;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.*;
import static java.util.Objects.requireNonNull;

public class GameLevel3DGameEventHandler {

    public record Payload(
        GameUI ui,
        State<Game> gameState,
        PlayingSoundEffects soundEffects, Supplier<String> messageCreator
    ) {}

    private boolean stateMatches(State<Game> gameState, GameControl.CommonGameState expected) {
        return gameState.nameMatches(expected.name());
    }

    public void onGameStateChange(GameStateChangeEvent event, GameLevel3D level3D, Payload payload) {
        requireNonNull(event);

        if (level3D == null) {
            return;
        }

        final State<Game> gameState = event.newState();
        if (stateMatches(gameState, STARTING_GAME_OR_LEVEL)) {
            onStartingGame(level3D, payload);
        }
        else if (stateMatches(gameState, HUNTING)) {
            onHuntingStart(level3D, payload);
        }
        else if (stateMatches(gameState, PACMAN_DYING)) {
            onPacManDying(level3D, payload);
        }
        else if (stateMatches(gameState, EATING_GHOST)) {
            onEatingGhost(level3D, payload);
        }
        else if (stateMatches(gameState, LEVEL_COMPLETE)) {
            onLevelComplete(level3D, payload);
        }
        else if (stateMatches(gameState, GAME_OVER)) {
            onGameOver(level3D, payload);
        }
    }

    public void onStartingGame(GameLevel3D level3D, Payload ignored) {
        level3D.maze3D().food().energizers3D().forEach(Energizer3D::stopPumping);
        if (level3D.levelCounter3D() != null) {
            level3D.levelCounter3D().rebuild(level3D.config3D().levelCounter(), level3D.level());
        }
    }

    public void onHuntingStart(GameLevel3D level3D, Payload ignored) {
        final GameLevel level = level3D.level();
        level3D.pac3D().ifPresent(pac3D -> pac3D.init(level));
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(level));
        level3D.maze3D().food().energizers3D().forEach(Energizer3D::startPumping);
        level3D.maze3D().food().startParticlesAnimation();
        level3D.animations().ifPresent(animations -> animations.ghostLightAnimation().playFromStart());
    }

    public void onPacManDying(GameLevel3D level3D, Payload payload) {
        final GameLevel level = level3D.level();
        final State<Game> gameState = payload.gameState();
        final PlayingSoundEffects soundEffects = payload.soundEffects();

        soundEffects.stopAll();
        level3D.animations().ifPresent(animations -> {
            animations.ghostLightAnimation().stop();
            animations.wallColorFlashingAnimation().stop();
        });
        level3D.ghosts3D().forEach(MutableGhost3D::stopAllAnimations);
        level3D.bonus3D().ifPresent(Bonus3D::expire);

        final PacBase3D pac3D = level3D.pac3D().orElseThrow(() -> new IllegalStateException("No Pac3D in level?"));

        // Do one last update before "dying" animation starts
        pac3D.update(level);

        gameState.timer().resetIndefiniteTime(); // freeze game state until Pac-Man animation ends
        final var dyingAnimation = new SequentialTransition(
            pauseSec(1.5),
            doNow(soundEffects::playPacDeadSound),
            pac3D.dyingAnimation().animationFX(),
            pauseSec(0.5)
        );
        dyingAnimation.setOnFinished(_ -> gameState.timer().expire());
        dyingAnimation.play();
    }

    public void onEatingGhost(GameLevel3D level3D, Payload ignored) {
        final GameLevel level = level3D.level();
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            byte personality = killedGhost.personality();
            int killedIndex = level.energizerVictims().indexOf(killedGhost);
            Image pointsImage = level3D.uiConfig().killedGhostPointsImage(killedIndex);
            level3D.ghosts3D().get(personality).setNumberImage(pointsImage);
        });
    }

    public void onLevelComplete(GameLevel3D level3D, Payload payload) {
        final State<Game> gameState = payload.gameState();
        final PlayingSoundEffects soundEffects = payload.soundEffects();

        soundEffects.stopAll();
        level3D.animationRegistry().stopAllAnimations();

        cleanupFoodAndParticles(level3D);

        level3D.maze3D().house().hideDoors();
        level3D.bonus3D().ifPresent(Bonus3D::expire);

        if (level3D.messageView() != null) {
            level3D.messageView().setVisible(false);
        }

        level3D.animations().ifPresentOrElse(
            _ -> level3D.playLevelEndAnimation(gameState),
            () -> pauseSecThen(2, () -> gameState.timer().expire()).play()
        );
    }

    public void onGameOver(GameLevel3D level3D, Payload payload) {
        final GameLevel level = level3D.level();
        final GameUI ui = payload.ui();
        final State<Game> gameState = payload.gameState();
        final PlayingSoundEffects soundEffects = payload.soundEffects();
        final Supplier<String> messageCreator = payload.messageCreator();

        gameState.timer().restartSeconds(3);

        level3D.animations().ifPresent(animations -> animations.ghostLightAnimation().stop());

        cleanupFoodAndParticles(level3D);
        level3D.bonus3D().ifPresent(Bonus3D::expire);

        soundEffects.playGameOverSound();

        final boolean showMsg = RandomNumberSupport.chance(0.25);
        if (!level.isDemoLevel() && showMsg) {
            ui.showFlashMessage(Duration.seconds(2.5), messageCreator.get());
        }
    }

    private static void cleanupFoodAndParticles(GameLevel3D level3D) {
        final MazeFood3D food3D = level3D.maze3D().food();
        food3D.stopParticlesAnimation();
        food3D.energizers3D().forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // hide 3D food explicitly because level might have been completed using cheat!
        food3D.pellets3D().forEach(pellet3D -> pellet3D.setVisible(false));
        level3D.maze3D().particlesGroup().getChildren().clear();
    }
}
