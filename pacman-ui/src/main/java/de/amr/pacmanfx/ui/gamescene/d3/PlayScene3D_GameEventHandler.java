/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.fsm.State;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacGetsPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacLostPowerEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.vm.Game3DSettingsVM;
import de.amr.pacmanfx.ui.vm.GameUISettingsVM;
import de.amr.pacmanfx.ui.vm.Maze3DSettingsVM;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.entities3D.bonus.system.Bonus3DViewSystem;
import de.amr.pacmanfx.uilib.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.entities3D.world.Pellet3D;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.basics.util.Ufx.pauseSecThen;

public interface PlayScene3D_GameEventHandler extends DefaultGameEventListener {

    double PELLET_EATING_DELAY_SEC = 0.05;

    GameAppContext appContext();

    default Optional<GameSoundEffects> optSoundEffects() {
        return appContext().variants().currentVariant().config().optSoundEffects();
    }

    default GameContext gameContext() {
        return appContext().currentGameContext();
    }

    RandomTextPicker textPicker();
    
    PlayScene3D gameScene();

    @Override
    default void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter game state '{}'", e.newState().name());
        final var newState = e.newState();

        if (!(newState instanceof GameState gameState)) {
            Logger.error("New state is not a game state?");
            return;
        }
        if (gameState.id() instanceof TestStateID) {
            handleTestState(appContext().ui().viewModel().common3D, gameContext());
        }
        else if (CommonGameStateID.GAME_OR_LEVEL_STARTING.hasSameNameAs(newState)) {
            //TODO anything?
        }
        else if (CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(newState)) {
            onHuntingStart();
        }
        else if (CommonGameStateID.GAME_LEVEL_PACMAN_DYING.hasSameNameAs(newState)) {
            onPacManDying(assertLevel3D().animationRegistry());
        }
        else if (CommonGameStateID.GAME_LEVEL_EATING_GHOST.hasSameNameAs(newState)) {
            onGhostsKilled();
        }
        else if (CommonGameStateID.GAME_LEVEL_COMPLETE.hasSameNameAs(newState)) {
            onLevelComplete();
        }
        else if (CommonGameStateID.GAME_OVER.hasSameNameAs(newState)) {
            onGameOver();
        }
    }

    @Override
    default void onBonusActivated(BonusActivatedEvent e) {
        assertLevel3D().activateBonus3D(e.bonus());
        optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    default void onBonusEaten(BonusEatenEvent e) {
        final Bonus bonus = e.bonus();
        Bonus3DViewSystem.lookEaten(bonus, assertLevel3D().animationRegistry());
        optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    default void onBonusExpired(BonusExpiredEvent e) {
        final Bonus bonus = e.bonus();
        Bonus3DViewSystem.lookExpired(bonus, assertLevel3D().animationRegistry());
        optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    default void onGameContinued(GameContinuedEvent ignoredEvent) {
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    default void onGameStarted(GameStartedEvent event) {
        final State<GameContext> state = gameContext().state();
        final boolean silent = gameContext().gamePlay().isDemoLevelRunning(gameContext())
            || (state instanceof GameState gameState && gameState.id() instanceof TestStateID);

        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    default void onGhostEaten(GhostEatenEvent ignoredEvent) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    default void onLevelCreated(LevelCreatedEvent event) {
        gameScene().replaceGameLevel3D(gameContext());
    }

    @Override
    default void onLevelStarted(LevelStartedEvent event) {
        final GameLevel level = event.level();
        final GameContext gameContext = gameContext();
        final State<GameContext> newState = gameContext.state();

        //TODO rethink this
        if (newState instanceof GameState gameState && gameState.id() instanceof TestStateID) {
            gameScene().replaceGameLevel3D(gameContext);
            final GameLevel3D level3D = assertLevel3D();
            level3D.energizers3D().forEach(Energizer3D::startPumping);
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level.number());
        }

        final GameLevel3D level3D = assertLevel3D();
        level3D.createLevelCounterView3D(gameContext.model().levelCounter());

        gameScene().replaceActionBindings(level);
        gameScene().fadeInAnimation().playFromStart();
    }

    @Override
    default void onPacEatsFood(PacEatsFoodEvent event) {
        final GameLevel3D level3D = assertLevel3D();
        if (event.allPellets()) {
            level3D.pellets3D().map(Pellet3D::shape).forEach(shape -> level3D.getChildren().remove(shape));
        } else {
            final Vector2i tile = WorldNavigationSystem.computeTile(event.pac());
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
                final long tick = appContext().clock().currentTick();
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
    default void onPacGetsPower(PacGetsPowerEvent event) {
        final GameLevel level = gameContext().assertLevel();
        final GameLevel3D level3D = assertLevel3D();
        final GameContext gameContext = gameContext();
        optSoundEffects().ifPresent(GameSoundEffects::stopSiren);
        if (!gameContext.model().rules().isLevelCompleted(level3D.level())) {
            Pac3DAnimationSystem.setPowerMode(level.entities().pac(), true);
            level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING)
                .ifPresent(ManagedAnimation::playFromStart);
            optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
    }

    @Override
    default void onPacLostPower(PacLostPowerEvent ignoredEvent) {
        final GameLevel level = gameContext().assertLevel();
        final GameLevel3D level3D = assertLevel3D();
        Pac3DAnimationSystem.setPowerMode(level.entities().pac(), true);
        optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING)
            .ifPresent(ManagedAnimation::stop);
    }

    @Override
    default void onSpecialScore(SpecialScoreEvent ignoredEvent) {
        optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void onHuntingStart() {
        final GameLevel level = gameContext().assertLevel();
        final GameLevel3D level3D = assertLevel3D();

        gameScene().initPac(level, level.entities().pac());

        level3D.energizers3D().forEach(Energizer3D::startPumping);

        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.PARTICLES)
            .ifPresent(ManagedAnimation::playFromStart);

        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.GHOST_LIGHT)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    private void onPacManDying(AnimationRegistry animationRegistry) {
        final GameLevel level = gameContext().assertLevel();

        gameContext().state().waitForTimeout();

        stopAnimationsBeforePacManDying();
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
        level.entities().optBonus().ifPresent(
            bonus -> Bonus3DViewSystem.lookExpired(bonus, animationRegistry));

        Pac3DAnimationSystem.playDyingAnimation(
            level.entities().pac(),
            () -> optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound),
            gameContext().state()::triggerTimeout
        );
    }

    private void stopAnimationsBeforePacManDying() {
        final GameLevel3D level3D = assertLevel3D();

        //TODO call ghost 3D animation system methods

        // Do not stop all animations!
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.GHOST_LIGHT).ifPresent(ManagedAnimation::stop);
        level3D.animationRegistry().optAnimation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::stop);

        //level3D.ghosts3D.forEach(Ghost3DWrapperToBeRemoved::stopAllAnimations);
    }

    private void onGhostsKilled() {
        final GameLevel3D level3D = assertLevel3D();
        final List<Ghost> ghostsKilled = gameContext().thisFrame().huntingStep().ghostsKilled();
        ghostsKilled.forEach(level3D::addKilledGhostNumberBox);
    }

    private void onLevelComplete() {
        final GameLevel3D level3D = assertLevel3D();
        final GameLevel level = gameContext().model().assertLevel();
        final House house = level.entities().theOne(House.class);

        final boolean cutSceneFollows = !level.isDemoLevel()
            && gameContext().model().rules().cutSceneAfterLevel(level.number()).isPresent();
        final GameUISettingsVM viewModel = appContext().ui().viewModel();

        gameScene().scoreOpacity.set(0);

        House3DSystem.hideDoors(house);

        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
        level3D.animationRegistry().stopAllAnimations();
        level3D.cleanupFoodAndParticles();

        level.optBonus().ifPresent(bonus -> Bonus3DViewSystem.lookExpired(bonus, level3D.animationRegistry()));

        level3D.messageManager().hideMessage();

        playLevelEndAnimation(level3D.animationRegistry(),
            viewModel.common3D, viewModel.maze3D,
            level3D.maze3D(),
            cutSceneFollows);
    }

    private void playLevelEndAnimation(
        AnimationRegistry animationRegistry,
        Game3DSettingsVM settings3D,
        Maze3DSettingsVM maze3DSettings,
        Maze3D maze3D,
        boolean cutSceneFollows)
    {
        final GameLevel3D.AnimationID animationID = cutSceneFollows
            ? GameLevel3D.AnimationID.LEVEL_COMPLETED_SHORT
            : GameLevel3D.AnimationID.LEVEL_COMPLETED_FULL;

        final Optional<ManagedAnimation> levelEndAnimation = animationRegistry.optAnimation(animationID);

        if (levelEndAnimation.isEmpty()) {
            Ufx.pauseSecThen(2, () -> gameContext().state().triggerTimeout()).play();
            return;
        }

        gameContext().state().waitForTimeout();

        final PerspectiveID perspectiveBeforeAnimation = settings3D.cameraPerspectiveIdProperty.get();

        final Animation resetCameraPerspective = pauseSecThen(2, () -> {
            settings3D.cameraPerspectiveIdProperty.set(PerspectiveID.TOTAL);
            maze3D.wallBaseHeightProperty().unbind();
        });

        final Animation restoreCameraPerspective = Ufx.pauseSecThen(0.25, () -> {
            settings3D.cameraPerspectiveIdProperty.set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(maze3DSettings.wallHeightProperty);
        });

        final var seq = new SequentialTransition(
            resetCameraPerspective,
            levelEndAnimation.get().delegate(),
            restoreCameraPerspective
        );
        seq.setOnFinished(_ -> gameContext().state().triggerTimeout());

        seq.play();
    }

    private void onGameOver() {
        final GameLevel3D level3D = assertLevel3D();
        final GameLevel level = gameContext().model().assertLevel();

        if (!level.isDemoLevel() && RandomNumberSupport.chance(0.25)) {
            appContext().ui().shortMessage(Duration.seconds(2.5), textPicker().selectNextText());
        }
        level3D.animationRegistry().requireAnimation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.cleanupFoodAndParticles();

        level.optBonus().ifPresent(bonus -> Bonus3DViewSystem.lookExpired(bonus, level3D.animationRegistry()));

        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
    }

    private void handleTestState(Game3DSettingsVM globals3D, GameContext gameContext) {
        gameScene().optGameLevel3D().ifPresent(level3D -> {
            gameScene().replaceGameLevel3D(gameContext);
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, gameContext.assertLevel().number());
            globals3D.cameraPerspectiveIdProperty.set(PerspectiveID.TOTAL);
        });
    }

    private GameLevel3D assertLevel3D() {
        return gameScene().optGameLevel3D().orElseThrow();
    }
}
