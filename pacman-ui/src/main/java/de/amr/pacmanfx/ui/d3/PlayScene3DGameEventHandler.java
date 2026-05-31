/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.fsm.State;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.d3.animation.HideGhostShowPointsAnimation3D;
import de.amr.pacmanfx.ui.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.NumberBox3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;

import java.util.Optional;

import static de.amr.pacmanfx.model.CanonicalGameState.*;
import static de.amr.pacmanfx.uilib.Ufx.pauseSecThen;

public class PlayScene3DGameEventHandler extends GameScene.DefaultGameEventHandler {

    public static final double PELLET_EATING_DELAY_SEC = 0.05;

    public PlayScene3DGameEventHandler(PlayScene3D playScene3D) {
        super(playScene3D);
    }

    @Override
    public PlayScene3D gameScene() {
        return (PlayScene3D) super.gameScene();
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent event) {
        final State<Game> gameState = event.newState();

        //TODO ugly
        if (gameState instanceof TestState) {
            handleTestState();
        }
        else if (STARTING_GAME_OR_LEVEL.matches(gameState)) {
            onStartingGameOrLevel();
        }
        else if (LEVEL_PLAYING.matches(gameState)) {
            onHuntingStart();
        }
        else if (PACMAN_DYING.matches(gameState)) {
            onPacManDying(gameState);
        }
        else if (EATING_GHOST.matches(gameState)) {
            onEatingGhost();
        }
        else if (LEVEL_COMPLETE.matches(gameState)) {
            onLevelComplete();
        }
        else if (GAME_OVER.matches(gameState)) {
            onGameOver();
        }
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        assertLevel3D().addOrReplaceBonus3D(e.bonus());
        services().currentSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent ignored) {
        assertLevel3D().entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookEaten);
        services().currentSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent ignoredEvent) {
        assertLevel3D().entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        services().currentSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent ignoredEvent) {
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGameStarted(GameStartedEvent event) {
        final State<Game> state = services().currentGameState();
        final boolean silent = services().currentGame().isDemoLevelRunning() || state instanceof TestState;
        if (!silent) {
            services().currentSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGhostEaten(GhostEatenEvent ignoredEvent) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        gameScene().replaceGameLevel3D(event.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent event) {
        final GameLevel level = event.level();
        final State<Game> gameState = services().currentGameState();
        //TODO rethink
        if (gameState instanceof TestState) {
            gameScene().replaceGameLevel3D(level);
            final GameLevel3D level3D = assertLevel3D();
            level3D.energizers3D().forEach(Energizer3D::startPumping);
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level.number());
        }
        assertLevel3D().entities().selectAll().forEach(e -> e.init(level));
        gameScene().replaceActionBindings(level);
        gameScene().fadeInAnimation().playFromStart();
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent event) {
        final GameLevel3D level3D = assertLevel3D();
        if (event.allPellets()) {
            level3D.pellets3D().map(Pellet3D::shape).forEach(shape -> level3D.getChildren().remove(shape));
        } else {
            final Vector2i tile = event.pac().computeTile();
            if (event.energizer()) {
                level3D.energizer3DAt(tile).ifPresent(energizer3D -> {
                    energizer3D.stopPumping();
                    energizer3D.hide();
                    triggerEnergizerExplosion(level3D, energizer3D.shape().localToScene(Point3D.ZERO));
                });
                services().currentSoundEffects().ifPresent(GameSoundEffects::playEnergizerExplosion);
            }
            else {
                level3D.pellet3DAtTile(tile).ifPresent(pellet3D -> removePelletAfterDelay(level3D, pellet3D));
                final long tick = services().gameClock().tickCount();
                services().currentSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
            }
        }
    }

    private void triggerEnergizerExplosion(GameLevel3D level3D, Point3D center) {
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.PARTICLES, ParticlesAnimation3D.class)
            .ifPresent(animation -> animation.triggerExplosion(center));
    }

    private void removePelletAfterDelay(GameLevel3D level3D, Pellet3D pellet3D) {
        pauseSecThen(PELLET_EATING_DELAY_SEC, () -> level3D.getChildren().remove(pellet3D.shape())).play();
    }


    @Override
    public void onPacGetsPower(PacGetsPowerEvent event) {
        final GameLevel3D level3D = assertLevel3D();
        services().currentSoundEffects().ifPresent(GameSoundEffects::stopSiren);
        if (!services().currentGame().isLevelCompleted()) {
            level3D.entities().pac3D().setPowerMode(true);
            level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING)
                .ifPresent(ManagedAnimation::playFromStart);
            services().currentSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent ignoredEvent) {
        final GameLevel3D level3D = assertLevel3D();
        level3D.entities().pac3D().setPowerMode(false);
        services().currentSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING)
            .ifPresent(ManagedAnimation::stop);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent ignoredEvent) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void onStartingGameOrLevel() {
        gameScene().optGameLevel3D().ifPresent(level3D ->
            level3D.entities().selectAll().forEach(entity -> entity.init(level3D.level())));
    }

    private void onHuntingStart() {
        final GameLevel3D level3D = assertLevel3D();
        level3D.entities().pac3D().init(level3D.level());
        level3D.entities().ghosts3D().forEach(ghost3D -> ghost3D.init(level3D.level()));
        level3D.energizers3D().forEach(Energizer3D::startPumping);

        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.PARTICLES)
            .ifPresent(ManagedAnimation::playFromStart);

        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.GHOST_LIGHT)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    private void onPacManDying(State<Game> gameState) {
        final GameLevel3D level3D = assertLevel3D();
        final Pac3D pac3D = level3D.entities().pac3D();

        services().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);

        // Do not stop all animations!
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.GHOST_LIGHT).ifPresent(ManagedAnimation::stop);
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::stop);

        level3D.entities().ghosts3D().forEach(Ghost3D::stopAllAnimations);
        level3D.entities().selectAllOfType(Bonus3D.class).forEach(Bonus3D::lookExpired);

        gameState.lock();
        final Animation dyingAnimationSeq = createPacDyingAnimationSeq(level3D.animationRegistry(), pac3D, level3D.level());
        dyingAnimationSeq.setOnFinished(_ -> gameState.expire());
        dyingAnimationSeq.play();
    }

    private Animation createPacDyingAnimationSeq(AnimationRegistry animationRegistry, Pac3D pac3D, GameLevel level) {
        final Animation pacStopping = Ufx.doNow(() -> {
            pac3D.update(level);
            animationRegistry.animation(Pac3D.AnimationID.CHEWING).stop();
            animationRegistry.animation(Pac3D.AnimationID.MOVING).stop();
        });

        final Animation pacDying = animationRegistry.animation(Pac3D.AnimationID.DYING).animationFX();

        return new SequentialTransition(
            pacStopping,
            Ufx.pauseSecThen(1.5, () -> services().currentSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound)),
            pacDying,
            Ufx.pauseSec(0.5)
        );
    }


    private void onEatingGhost() {
        final GameLevel3D level3D = assertLevel3D();
        services().currentGame().doSimulationStep().ghostsKilled.forEach(ghost -> {
            final Ghost3D ghost3D = level3D.ghost3D(ghost.personality());
            final int killIndex = level3D.level().energizerVictims().indexOf(ghost);
            final Image pointsImage = level3D.uiConfig().killedGhostPointsImage(killIndex);
            final NumberBox3D numberBox3D = createGhostPointsNumberBox3D(ghost3D, pointsImage);
            level3D.getChildren().add(numberBox3D);

            final double risingHeight = (killIndex + 1) * 12;
            final var animation = new HideGhostShowPointsAnimation3D(ghost3D, numberBox3D, risingHeight);
            animation.animationFX().setOnFinished(_ -> level3D.getChildren().remove(numberBox3D));
            animation.playFromStart();
        });
    }

    private NumberBox3D createGhostPointsNumberBox3D(Ghost3D ghost3D, Image pointsImage) {
        final NumberBox3D numberBox3D = new NumberBox3D(pointsImage);
        numberBox3D.setTranslateX(ghost3D.getTranslateX());
        numberBox3D.setTranslateY(ghost3D.getTranslateY());
        numberBox3D.setTranslateZ(ghost3D.getTranslateZ());
        return numberBox3D;
    }

    private void onLevelComplete() {
        final GameLevel3D level3D = assertLevel3D();
        final State<Game> gameState = services().currentGameState();

        gameScene().scoreOpacity.set(0);

        level3D.entities().maze3D().house().hideDoors();

        services().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
        level3D.animationRegistry().stopAllAnimations();
        level3D.cleanupFoodAndParticles();
        level3D.entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.messageManager().hideMessage();

        playLevelEndAnimation(level3D.animationRegistry(), level3D.entities().maze3D(), gameState, level3D.level().cutSceneNumber() != 0);
    }

    private void playLevelEndAnimation(AnimationRegistry animationRegistry, Maze3D maze3D, State<Game> gameState, boolean cutSceneAfter) {
        final GameLevel3D.AnimationID animationID = cutSceneAfter
            ? GameLevel3D.AnimationID.LEVEL_COMPLETED_SHORT
            : GameLevel3D.AnimationID.LEVEL_COMPLETED_FULL;

        final Optional<ManagedAnimation> levelEndAnimation = animationRegistry.optAnimation(animationID);

        if (levelEndAnimation.isEmpty()) {
            Ufx.pauseSecThen(2, gameState::expire).play();
            return;
        }

        gameState.lock();

        final PerspectiveID perspectiveBeforeAnimation = GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.get();

        final Animation resetCameraPerspective = pauseSecThen(2, () -> {
            GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            maze3D.wallBaseHeightProperty().unbind();
        });

        final Animation restoreCameraPerspective = Ufx.pauseSecThen(0.25, () -> {
            GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(GameUI_Constants.PROPERTY_3D_WALL_HEIGHT);
        });

        final var seq = new SequentialTransition(
            resetCameraPerspective,
            levelEndAnimation.get().animationFX(),
            restoreCameraPerspective
        );
        seq.setOnFinished(_ -> gameState.expire());

        seq.play();
    }

    private void onGameOver() {
        GameLevel3D level3D = assertLevel3D();
        if (!level3D.level().isDemoLevel() && RandomNumberSupport.chance(0.25)) {
            gameScene().showRandomGameOverMessage();
        }
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.cleanupFoodAndParticles();
        level3D.entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
    }

    private void handleTestState() {
        gameScene().optGameLevel3D().ifPresent(level3D -> {
            gameScene().replaceGameLevel3D(level3D.level());
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level3D.level().number());
            GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
        });
    }

    private GameLevel3D assertLevel3D() {
        return gameScene().optGameLevel3D().orElseThrow();
    }
}
