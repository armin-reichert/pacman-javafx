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
import de.amr.pacmanfx.core.entities.*;
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
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.ui.GlobalAssets;
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
import de.amr.pacmanfx.uilib.entities3D.messageview.system.LevelMessageType;
import de.amr.pacmanfx.uilib.entities3D.messageview.system.MessageView3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.messageview.system.MessageView3DDisplaySystem;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.world.Pellet3D;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.basics.util.Ufx.pauseSecThen;

public interface PlayScene3D_GameEventHandler extends DefaultGameEventListener {

    double PELLET_EATING_DELAY_SEC = 0.05;

    GameAppContext app();

    default Optional<GameSoundEffects> optSoundEffects() {
        return app().gameVariants().currentGameVariant().config().optSoundEffects();
    }

    default GameContext game() {
        return app().currentGame();
    }

    default GameSession session() {
        return game().session();
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
            handleTestState(app().ui().viewModel().common3D, game().session().assertLevel());
        }
        else if (CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(newState)) {
            onHuntingStart(assertLevel3D());
        }
        else if (CommonGameStateID.GAME_LEVEL_PACMAN_DYING.hasSameNameAs(newState)) {
            final GameLevel3D level3D = assertLevel3D();
            onPacManDying(level3D.animationManager().registry());
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
        final GameLevel3D level3D = assertLevel3D();
        final Bonus bonus = e.bonus();

        level3D.ensureBonus3DViewAddedToSceneGraph(bonus);
        Bonus3DViewSystem.lookEdible(bonus);
        optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }


    @Override
    default void onBonusEaten(BonusEatenEvent e) {
        final Bonus bonus = e.bonus();
        Bonus3DViewSystem.lookEaten(bonus, assertLevel3D().animationManager().registry());
        optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    default void onBonusExpired(BonusExpiredEvent e) {
        final Bonus bonus = e.bonus();
        Bonus3DViewSystem.lookExpired(bonus, assertLevel3D().animationManager().registry());
        optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    default void onGameContinued(GameContinuedEvent ignoredEvent) {
        final GameLevel3D level3D = assertLevel3D();
        showMessage(level3D, LevelMessageType.READY);
    }

    @Override
    default void onGameStarted(GameStartedEvent event) {
        final GameSession session = game().session();
        final GameState state = game().session().gameState();

        final boolean silent = session.isAttractMode() || state.id() instanceof TestStateID;

        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }

        final GameLevel3D level3D = assertLevel3D();
        showMessage(level3D, LevelMessageType.READY);
    }

    @Override
    default void onGhostEaten(GhostEatenEvent ignoredEvent) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    default void onLevelCreated(LevelCreatedEvent event) {
        gameScene().replaceGameLevel3D(game(), event.level());
    }

    @Override
    default void onLevelStarted(LevelStartedEvent event) {
        final GameLevel level = event.level();
        final GameSession session = game().session();
        final GameLevel3D level3D = assertLevel3D();
        final State<GameContext> newState = session.gameState();

        level3D.replaceLevelCounter3D(session.levelCounter());

        //TODO rethink this
        if (newState instanceof GameState gameState && gameState.id() instanceof TestStateID) {
            gameScene().replaceGameLevel3D(game(), level);
            level3D.animationManager().startEnergizerPumping();
            showMessage(level3D, LevelMessageType.TEST, level.number());
        }

        //TODO: workaround, check cause for invisible Pac-Man 3D after cut scene
        level.entities().pac().requireComp(Pac3DViewComp.class).root().setVisible(true);

        gameScene().replaceActionBindings(session, level);
        gameScene().fadeInAnimation().playFromStart();
    }

    @Override
    default void onPacEatsFood(PacEatsFoodEvent event) {
        final GameLevel3D level3D = assertLevel3D();
        final long tick = app().clock().currentTick();

        if (event.allPellets()) {
            level3D.pellets3D().map(Pellet3D::root).forEach(shape -> level3D.getChildren().remove(shape));
        }
        else {
            final Vector2i tile = WorldNavigationSystem.computeTile(event.pac());
            if (event.energizer()) {
                level3D.energizer3DAt(tile).ifPresent(energizer3D -> {
                    level3D.animationManager().stopPumping(energizer3D);
                    energizer3D.hide();
                    triggerEnergizerExplosion(level3D, energizer3D.root().localToScene(Point3D.ZERO));
                });
                optSoundEffects().ifPresent(GameSoundEffects::playEnergizerExplosion);
            }
            else {
                level3D.pellet3DAtTile(tile).ifPresent(pellet3D -> removePelletAfterDelay(level3D, pellet3D));
                optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
            }
        }
    }

    private void triggerEnergizerExplosion(GameLevel3D level3D, Point3D center) {
        level3D.animationManager().registry().optAnimation(GameLevel3DAnimationManager.AnimationID.PARTICLES, ParticlesAnimation3D.class)
            .ifPresent(animation -> animation.triggerExplosion(center));
    }

    private void removePelletAfterDelay(GameLevel3D level3D, Pellet3D pellet3D) {
        pauseSecThen(PELLET_EATING_DELAY_SEC, () -> level3D.getChildren().remove(pellet3D.root())).play();
    }


    @Override
    default void onPacGetsPower(PacGetsPowerEvent e) {
        final Pac pac = e.pac();
        final GameLevel level = game().session().assertLevel();
        final GameLevel3D level3D = assertLevel3D();

        optSoundEffects().ifPresent(GameSoundEffects::stopSiren);
        if (!game().rules().isLevelCompleted(level)) {
            optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
            Pac3DAnimationSystem.setPowerMode(pac, true);
            level3D.animationManager().startWallFlashing();
        }
    }

    @Override
    default void onPacLostPower(PacLostPowerEvent e) {
        final Pac pac = e.pac();
        final GameLevel3D level3D = assertLevel3D();

        optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
        level3D.animationManager().stopWallFlashing();
        Pac3DAnimationSystem.setPowerMode(pac, true);
    }

    @Override
    default void onSpecialScore(SpecialScoreEvent ignoredEvent) {
        optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void showMessage(GameLevel3D level3D, LevelMessageType type, Object... args) {
        final GameLevel level = level3D.level();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final var center = switch (type) {
            case READY -> level.entities().theOne(House.class).centerPositionUnderHouse();
            case TEST -> vec2_float(terrain.numCols() * WorldMap.HTS, (terrain.numRows() - 2) * WorldMap.TS);
        };
        MessageView3DDisplaySystem.showMessage(
            level.entities().theOne(MessageView.class),
            level3D,
            center,
            GlobalAssets.PredefinedFont.ARCADE6.font(),
            level3D.animationManager().registry(),
            type,
            args);
    }

    private void onHuntingStart(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        gameScene().initPac(level, level.entities().pac());

        level3D.animationManager().startEnergizerPumping();
        level3D.animationManager().startParticlesAnimation();
        level3D.animationManager().startGhostLightAnimation();
    }

    private void onPacManDying(AnimationRegistry animationRegistry) {
        final GameLevel level = session().assertLevel();
        final GameLevel3D level3D = assertLevel3D();

        session().gameState().waitForTimeout();

        optSoundEffects().ifPresent(GameSoundEffects::stopAll);

        level.entities().optBonus().ifPresent(
            bonus -> Bonus3DViewSystem.lookExpired(bonus, animationRegistry)
        );

        level3D.animationManager().stopAnimationsBeforePacManDies();
        Pac3DAnimationSystem.playDyingAnimation(
            level.entities().pac(),
            () -> optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound),
            session().gameState()::triggerTimeout
        );
    }

    private void onGhostsKilled() {
        final GameLevel3D level3D = assertLevel3D();
        final List<Ghost> ghostsKilled = game().session().thisFrame().huntingStep().ghostsKilled();
        ghostsKilled.forEach(level3D::addKilledGhostNumberBox);
    }

    private void onLevelComplete() {
        final GameUISettingsVM viewModel = app().ui().viewModel();
        final GameLevel level = session().assertLevel();
        final House house = level.entities().theOne(House.class);
        final boolean cutSceneFollows = !session().isAttractMode()
            && game().rules().cutSceneAfterLevel(level.number()).isPresent();

        gameScene().scoreOpacity.set(0);
        House3DSystem.hideDoors(house);

        optSoundEffects().ifPresent(GameSoundEffects::stopAll);

        final GameLevel3D level3D = assertLevel3D();
        level3D.animationManager().stopAll();
        level3D.cleanupFoodAndParticles();
        level.optBonus().ifPresent(bonus -> Bonus3DViewSystem.lookExpired(bonus, level3D.animationManager().registry()));

        MessageView3DAnimationSystem.hideMessageView(level.entities().theOne(MessageView.class));

        playLevelEndAnimation(level3D.animationManager().registry(),
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
        final GameLevel3DAnimationManager.AnimationID animationID = cutSceneFollows
            ? GameLevel3DAnimationManager.AnimationID.LEVEL_COMPLETED_SHORT
            : GameLevel3DAnimationManager.AnimationID.LEVEL_COMPLETED_FULL;

        final Optional<ManagedAnimation> levelEndAnimation = animationRegistry.optAnimation(animationID);

        if (levelEndAnimation.isEmpty()) {
            Ufx.pauseSecThen(2, () -> session().gameState().triggerTimeout()).play();
            return;
        }

        session().gameState().waitForTimeout();

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
        seq.setOnFinished(_ -> session().gameState().triggerTimeout());
        seq.play();
    }

    private void onGameOver() {
        final GameSession session = game().session();
        final GameLevel level = session.assertLevel();
        final GameLevel3D level3D = assertLevel3D();

        if (!session.isAttractMode() && RandomNumberSupport.chance(0.25)) {
            app().ui().shortMessage(Duration.seconds(2.5), textPicker().selectNextText());
        }

        level3D.animationManager().stopAll();
        level3D.cleanupFoodAndParticles();
        level.optBonus().ifPresent(bonus -> Bonus3DViewSystem.lookExpired(bonus, level3D.animationManager().registry()));
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
    }

    private void handleTestState(Game3DSettingsVM globals3D, GameLevel level) {
        gameScene().optGameLevel3D().ifPresent(level3D -> {
            gameScene().replaceGameLevel3D(game(), level);
            showMessage(level3D, LevelMessageType.TEST, level.number());
            globals3D.cameraPerspectiveIdProperty.set(PerspectiveID.TOTAL);
        });
    }

    private GameLevel3D assertLevel3D() {
        return gameScene().optGameLevel3D().orElseThrow();
    }
}
