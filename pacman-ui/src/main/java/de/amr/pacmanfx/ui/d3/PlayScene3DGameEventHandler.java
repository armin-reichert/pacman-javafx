/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.fsm.State;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.d3.animation.HideGhostShowPointsAnimation3D;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.NumberBox3D;
import javafx.animation.Animation;
import javafx.scene.image.Image;

import java.util.List;

import static de.amr.pacmanfx.model.CanonicalGameState.*;

public class PlayScene3DGameEventHandler extends GameScene.DefaultGameEventHandler {

    public PlayScene3DGameEventHandler(PlayScene3D playScene3D) {
        super(playScene3D);
    }

    @Override
    public PlayScene3D gameScene() {
        return (PlayScene3D) super.gameScene();
    }

    // TODO: remove (only used by GameState.TESTING_CUT_SCENES)
    @Override
    public void onGenericChange(GenericChangeEvent event) {
        ui().forceGameSceneUpdate();
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
        soundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent ignored) {
        assertLevel3D().entities().first(Bonus3D.class).ifPresent(Bonus3D::lookEaten);
        soundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent ignoredEvent) {
        assertLevel3D().entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        soundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent ignoredEvent) {
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGameStarted(GameStartedEvent event) {
        final State<Game> state = game().flow().state();
        final boolean silent = game().isDemoLevelRunning() || state instanceof TestState;
        if (!silent) {
            soundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGhostEaten(GhostEatenEvent ignoredEvent) {
        soundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        gameScene().replaceGameLevel3D(event.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent event) {
        final GameLevel level = event.level();
        final State<Game> gameState = game().flow().state();
        //TODO rethink
        if (gameState instanceof TestState) {
            gameScene().replaceGameLevel3D(level);
            final GameLevel3D level3D = assertLevel3D();
            level3D.entities().all(Energizer3D.class).forEach(Energizer3D::startPumping);
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level.number());
        }
        assertLevel3D().entities().all().forEach(e -> e.init(level));
        gameScene().replaceActionBindings(level);
        gameScene().fadeInAnimation().playFromStart();
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent event) {
        if (event.allPellets()) {
            assertLevel3D().removeAllPellets3D();
        } else {
            final GameLevel3D level3D = assertLevel3D();
            final long tick = gameContext().clock().tickCount();
            level3D.eatFoodAtTile(event.pac().tile());
            soundEffects().ifPresent(sfx -> {
                if (event.energizer()) {
                    sfx.playEnergizerExplosion();
                } else {
                    sfx.playPacMunchingSound(tick);
                }
            });
        }
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent event) {
        final GameLevel3D level3D = assertLevel3D();
        final Pac3D pac3D = level3D.entities().unique(Pac3D.class);
        soundEffects().ifPresent(GameSoundEffects::stopSiren);
        if (!game().isLevelCompleted()) {
            pac3D.setPowerMode(true);
            level3D.animationRegistry().animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).playFromStart();
            soundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent ignoredEvent) {
        final GameLevel3D level3D = assertLevel3D();
        level3D.entities().unique(Pac3D.class).setPowerMode(false);
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).stop();
        soundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent ignoredEvent) {
        soundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void onStartingGameOrLevel() {
        gameScene().optGameLevel3D().ifPresent(level3D ->
            level3D.entities().all().forEach(entity -> entity.init(level3D.level())));
    }

    private void onHuntingStart() {
        final GameLevel3D level3D = assertLevel3D();
        level3D.entities().unique(Pac3D.class).init(level3D.level());
        level3D.entities().all(Ghost3D.class).forEach(ghost3D -> ghost3D.init(level3D.level()));
        level3D.entities().all(Energizer3D.class).forEach(Energizer3D::startPumping);
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.ENERGIZER_PARTICLES_MOVEMENT).playFromStart();
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).playFromStart();
    }

    private void onPacManDying(State<Game> gameState) {
        final GameLevel3D level3D = assertLevel3D();
        final Pac3D pac3D = level3D.entities().unique(Pac3D.class);

        soundEffects().ifPresent(GameSoundEffects::stopAll);

        // Do not stop all animations!
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).stop();
        level3D.entities().all(Ghost3D.class).forEach(Ghost3D::stopAllAnimations);
        level3D.entities().all(Bonus3D.class).forEach(Bonus3D::lookExpired);

        gameState.lock();
        final Animation dyingAnimationSeq = level3D.createPacDyingAnimationSeq(pac3D);
        dyingAnimationSeq.setOnFinished(_ -> gameState.expire());
        dyingAnimationSeq.play();
    }

    private void onEatingGhost() {
        final GameLevel3D level3D = assertLevel3D();
        game().simulationStep().ghostsKilled.forEach(ghost -> {
            final Ghost3D ghost3D = level3D.ghost3D(ghost.personality()).orElseThrow();
            final int killIndex = level3D.level().energizerVictims().indexOf(ghost);
            final Image pointsImage = level3D.uiConfig().killedGhostPointsImage(killIndex);
            final NumberBox3D numberBox3D = createGhostPointsNumberBox3D(ghost3D, pointsImage);
            level3D.entities().add(numberBox3D);
            level3D.getChildren().add(numberBox3D);

            final double risingHeight = (killIndex + 1) * 12;
            final var animation = new HideGhostShowPointsAnimation3D(ghost3D, numberBox3D, risingHeight);
            animation.animationFX().setOnFinished(_ -> {
                level3D.entities().remove(numberBox3D);
                level3D.getChildren().remove(numberBox3D);
            });
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
        final State<Game> gameState = game().flow().state();

        gameScene().scoreOpacity.set(0);

        final Maze3D maze3D = level3D.entities().unique(Maze3D.class);
        maze3D.house().hideDoors();

        soundEffects().ifPresent(GameSoundEffects::stopAll);
        level3D.animationRegistry().stopAllAnimations();
        level3D.cleanupFoodAndParticles();
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.messageManager().hideMessage();
        level3D.playLevelEndAnimation(maze3D, gameState);
    }

    private void onGameOver() {
        GameLevel3D level3D = assertLevel3D();
        if (!level3D.level().isDemoLevel() && RandomNumberSupport.chance(0.25)) {
            gameScene().showRandomGameOverMessage();
        }
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.cleanupFoodAndParticles();
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
    }

    private void handleTestState() {
        gameScene().optGameLevel3D().ifPresent(level3D -> {
            gameScene().replaceGameLevel3D(level3D.level());
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level3D.level().number());
            GameUIConstants.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
        });
    }

    private GameLevel3D assertLevel3D() {
        return gameScene().optGameLevel3D().orElseThrow();
    }
}
