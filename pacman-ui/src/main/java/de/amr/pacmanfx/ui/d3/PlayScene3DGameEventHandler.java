/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.fsm.State;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.d3.animation.HideGhostShowPointsAnimation3D;
import de.amr.pacmanfx.ui.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.gamescene.BaseGameSceneHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
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
import javafx.util.Duration;

import java.util.Optional;

import static de.amr.pacmanfx.uilib.Ufx.pauseSecThen;
import static java.util.Objects.requireNonNull;

public class PlayScene3DGameEventHandler extends BaseGameSceneHandler {

    public static final double PELLET_EATING_DELAY_SEC = 0.05;

    private final PlayScene3D playScene3D;
    private final RandomTextPicker gameOverMessagePicker;

    public PlayScene3DGameEventHandler(PlayScene3D playScene3D) {
        super(playScene3D.game());
        this.playScene3D = requireNonNull(playScene3D);
        gameOverMessagePicker = new RandomTextPicker(game().ui().translations().textBundle(), "game.over");
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent event) {
        final var gameState = event.newState();

        if (gameState instanceof TestState) {
            handleTestState();
        }
        else if (GameStateID.GAME_OR_LEVEL_STARTING.identifies(gameState)) {
            onStartingGameOrLevel();
        }
        else if (GameStateID.GAME_LEVEL_PLAYING.identifies(gameState)) {
            onHuntingStart();
        }
        else if (GameStateID.GAME_LEVEL_PACMAN_DYING.identifies(gameState)) {
            onPacManDying();
        }
        else if (GameStateID.GAME_LEVEL_EATING_GHOST.identifies(gameState)) {
            onEatingGhost();
        }
        else if (GameStateID.GAME_LEVEL_COMPLETE.identifies(gameState)) {
            onLevelComplete();
        }
        else if (GameStateID.GAME_OVER.identifies(gameState)) {
            onGameOver();
        }
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        assertLevel3D().addOrReplaceBonus3D(e.bonus());
        optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent ignored) {
        assertLevel3D().entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookEaten);
        optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent ignoredEvent) {
        assertLevel3D().entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent ignoredEvent) {
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGameStarted(GameStartedEvent event) {
        final State<GameContext> state = gameContext().state();
        final boolean silent = gameContext().model().isDemoLevelRunning() || state instanceof TestState;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGhostEaten(GhostEatenEvent ignoredEvent) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        playScene3D.replaceGameLevel3D(event.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent event) {
        final GameLevel level = event.level();
        final GameContext gameContext = gameContext();
        final State<GameContext> gameState = gameContext.state();
        //TODO rethink
        if (gameState instanceof TestState) {
            playScene3D.replaceGameLevel3D(level);
            final GameLevel3D level3D = assertLevel3D();
            level3D.energizers3D().forEach(Energizer3D::startPumping);
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level.number());
        }
        assertLevel3D().entities().selectAll().forEach(e -> e.init(gameContext, level));
        playScene3D.replaceActionBindings(level);
        playScene3D.fadeInAnimation().playFromStart();
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
                optSoundEffects().ifPresent(GameSoundEffects::playEnergizerExplosion);
            }
            else {
                level3D.pellet3DAtTile(tile).ifPresent(pellet3D -> removePelletAfterDelay(level3D, pellet3D));
                final long tick = game().clock().tickCount();
                optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
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
        final GameContext gameContext = gameContext();
        optSoundEffects().ifPresent(GameSoundEffects::stopSiren);
        if (!gameContext.rules().isLevelCompleted(level3D.level())) {
            level3D.entities().pac3D().setPowerMode(true);
            level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING)
                .ifPresent(ManagedAnimation::playFromStart);
            optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent ignoredEvent) {
        final GameLevel3D level3D = assertLevel3D();
        level3D.entities().pac3D().setPowerMode(false);
        optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING)
            .ifPresent(ManagedAnimation::stop);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent ignoredEvent) {
        optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void onStartingGameOrLevel() {
        playScene3D.optGameLevel3D().ifPresent(level3D ->
            level3D.entities().selectAll().forEach(entity -> entity.init(gameContext(), level3D.level())));
    }

    private void onHuntingStart() {
        final GameLevel3D level3D = assertLevel3D();
        level3D.entities().pac3D().init(gameContext(), level3D.level());
        level3D.entities().ghosts3D().forEach(ghost3D -> ghost3D.init(gameContext(), level3D.level()));
        level3D.energizers3D().forEach(Energizer3D::startPumping);

        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.PARTICLES)
            .ifPresent(ManagedAnimation::playFromStart);

        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.GHOST_LIGHT)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    private void onPacManDying() {
        final GameLevel3D level3D = assertLevel3D();
        final Pac3D pac3D = level3D.entities().pac3D();

        optSoundEffects().ifPresent(GameSoundEffects::stopAll);

        // Do not stop all animations!
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.GHOST_LIGHT).ifPresent(ManagedAnimation::stop);
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::stop);

        level3D.entities().ghosts3D().forEach(Ghost3D::stopAllAnimations);
        level3D.entities().selectAllOfType(Bonus3D.class).forEach(Bonus3D::lookExpired);

        gameContext().state().lock();
        final Animation dyingAnimationSeq = createPacDyingAnimationSeq(level3D.animationRegistry(), pac3D, level3D.level());
        dyingAnimationSeq.setOnFinished(_ -> gameContext().state().expire());
        dyingAnimationSeq.play();
    }

    private Animation createPacDyingAnimationSeq(AnimationRegistry animationRegistry, Pac3D pac3D, GameLevel level) {
        final Animation pacStopping = Ufx.doNow(() -> {
            pac3D.update(gameContext(), level);
            animationRegistry.animation(Pac3D.AnimationID.CHEWING).stop();
            animationRegistry.animation(Pac3D.AnimationID.MOVING).stop();
        });

        final Animation pacDying = animationRegistry.animation(Pac3D.AnimationID.DYING).animationFX();

        return new SequentialTransition(
            pacStopping,
            Ufx.pauseSecThen(1.5, () -> optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound)),
            pacDying,
            Ufx.pauseSec(0.5)
        );
    }

    private void onEatingGhost() {
        final GameLevel3D level3D = assertLevel3D();
        gameContext().huntingResult().ghostsKilled().forEach(ghost -> {
            final Ghost3D ghost3D = level3D.ghost3D(ghost.personality());
            final int killIndex = level3D.level().indexInGhostKilledChain(ghost);
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

        playScene3D.scoreOpacity.set(0);

        level3D.entities().maze3D().house().hideDoors();

        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
        level3D.animationRegistry().stopAllAnimations();
        level3D.cleanupFoodAndParticles();
        level3D.entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.messageManager().hideMessage();

        playLevelEndAnimation(level3D.animationRegistry(), level3D.entities().maze3D(), level3D.level().cutSceneNumber() != 0);
    }

    private void playLevelEndAnimation(AnimationRegistry animationRegistry, Maze3D maze3D, boolean cutSceneAfter) {
        final GameLevel3D.AnimationID animationID = cutSceneAfter
            ? GameLevel3D.AnimationID.LEVEL_COMPLETED_SHORT
            : GameLevel3D.AnimationID.LEVEL_COMPLETED_FULL;

        final Optional<ManagedAnimation> levelEndAnimation = animationRegistry.optAnimation(animationID);

        if (levelEndAnimation.isEmpty()) {
            Ufx.pauseSecThen(2, () -> gameContext().state().expire()).play();
            return;
        }

        gameContext().state().lock();

        final PerspectiveID perspectiveBeforeAnimation = Globals_3D.PROPERTY_3D_PERSPECTIVE_ID.get();

        final Animation resetCameraPerspective = pauseSecThen(2, () -> {
            Globals_3D.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            maze3D.wallBaseHeightProperty().unbind();
        });

        final Animation restoreCameraPerspective = Ufx.pauseSecThen(0.25, () -> {
            Globals_3D.PROPERTY_3D_PERSPECTIVE_ID.set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(Globals_3D.PROPERTY_3D_WALL_HEIGHT);
        });

        final var seq = new SequentialTransition(
            resetCameraPerspective,
            levelEndAnimation.get().animationFX(),
            restoreCameraPerspective
        );
        seq.setOnFinished(_ -> gameContext().state().expire());

        seq.play();
    }

    private void onGameOver() {
        GameLevel3D level3D = assertLevel3D();
        if (!level3D.level().isDemoLevel() && RandomNumberSupport.chance(0.25)) {
            game().shortMessage(Duration.seconds(2.5), gameOverMessagePicker.selectNextText());
        }
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.cleanupFoodAndParticles();
        level3D.entities().optAnyOfType(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
    }

    private void handleTestState() {
        playScene3D.optGameLevel3D().ifPresent(level3D -> {
            playScene3D.replaceGameLevel3D(level3D.level());
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level3D.level().number());
            Globals_3D.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
        });
    }

    private GameLevel3D assertLevel3D() {
        return playScene3D.optGameLevel3D().orElseThrow();
    }
}
