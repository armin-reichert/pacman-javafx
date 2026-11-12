/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.fsm.State;
import de.amr.basics.math.RandomNumbers;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerEndsEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.SimulationFrame;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3DSystem;
import de.amr.pacmanfx.ui.entities3D.messageview.system.LevelMessageType;
import de.amr.pacmanfx.ui.entities3D.messageview.system.MessageView3DAnimationSystem;
import de.amr.pacmanfx.ui.entities3D.messageview.system.MessageView3DDisplaySystem;
import de.amr.pacmanfx.ui.entities3D.world.PelletView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.PlaySceneView3D_FadeInAnimation;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.PerspectiveID;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import de.amr.pacmanfx.ui.systems.PacView3DSystem;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsViewModel;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.viewmodel.Maze3DSettingsViewModel;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.basics.TileDimension.HTS;
import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.basics.util.Ufx.pauseSecThen;

public class PlayScene3D_GameEventHandler implements DefaultGameEventListener {

    public static final double PELLET_EATING_DELAY_SEC = 0.05;

    private final PlayScene3D playScene3D;

    public PlayScene3D_GameEventHandler(PlayScene3D playScene3D) {
        this.playScene3D = playScene3D;
    }

    private GameContext assertGame() {
        return playScene3D.engine().optCurrentGame().orElseThrow();
    }

    private Optional<PacManGameSoundEffects> optSoundEffects() {
        return playScene3D.optComponent(PacManGameSoundEffects.class);
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter game state '{}'", e.newState().name());
        final var newState = e.newState();

        if (!(newState instanceof AbstractGameState gameState)) {
            Logger.error("New state is not a game state?");
            return;
        }
        if (gameState.id() instanceof TestStateID) {
            handleTestState(playScene3D.engine().ui().viewModel().common3DSettings(), assertGame().session().optLevel().orElseThrow());
        }
        else if (CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(newState)) {
            onHuntingStart(assertLevel3D());
        }
        else if (CommonGameStateID.GAME_LEVEL_PACMAN_DYING.hasSameNameAs(newState)) {
            onPacManDying();
        }
        else if (CommonGameStateID.GAME_LEVEL_EATING_GHOST.hasSameNameAs(newState)) {
            onGhostsKilled(assertLevel3D());
        }
        else if (CommonGameStateID.GAME_LEVEL_COMPLETE.hasSameNameAs(newState)) {
            onLevelComplete();
        }
        else if (CommonGameStateID.GAME_OVER.hasSameNameAs(newState)) {
            onGameOver();
        }
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        final GameLevelView3D level3D = assertLevel3D();
        final Bonus bonus = e.bonus();
        final Bonus3DUpdateSystem updateSystem3D = assertGame().systems().assertSystem(Bonus3DUpdateSystem.class);

        level3D.ensureBonus3DViewAddedToSceneGraph(bonus, playScene3D.assertView3D().animationRegistry());
//        updateSystem3D.view3D().lookEdible(bonus);
        updateSystem3D.update(bonus, playScene3D.assertView3D().animationRegistry());
        optSoundEffects().ifPresent(PacManGameSoundEffects::playBonusActiveSound);
    }


    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        final Bonus bonus = e.bonus();
        final Bonus3DUpdateSystem updateSystem3D = assertGame().systems().assertSystem(Bonus3DUpdateSystem.class);

        updateSystem3D.update(bonus, playScene3D.assertView3D().animationRegistry());
        //TODO integrate into update, set flag at animation component
        updateSystem3D.animation3D().lookExpired(bonus, playScene3D.assertView3D().animationRegistry());

        optSoundEffects().ifPresent(PacManGameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        final Bonus bonus = e.bonus();
        final Bonus3DUpdateSystem updateSystem3D = assertGame().systems().assertSystem(Bonus3DUpdateSystem.class);

        updateSystem3D.update(bonus, playScene3D.assertView3D().animationRegistry());
        optSoundEffects().ifPresent(PacManGameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent ignoredEvent) {
        final GameLevelView3D level3D = assertLevel3D();
        final MessageView messageView = level3D.level().entitySet().theOne(MessageView.class);
        showMessage(level3D, messageView, LevelMessageType.READY);
    }

    @Override
    public void onGameStarted(GameStartedEvent event) {
        final GameSession session = assertGame().session();
        final AbstractGameState state = assertGame().state();

        final boolean silent = session.booleanValue(SessionKey.DEMO_LEVEL_MODE) || state.id() instanceof TestStateID;

        if (!silent) {
            optSoundEffects().ifPresent(PacManGameSoundEffects::playGameReadySound);
        }

        final GameLevelView3D level3D = assertLevel3D();
        final MessageView messageView = level3D.level().entitySet().theOne(MessageView.class);
        showMessage(level3D, messageView, LevelMessageType.READY);
    }

    @Override
    public void onGhostEaten(GhostEatenEvent ignoredEvent) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        playScene3D.replaceLevel3D(assertGame(), event.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent event) {
        final GameLevelView3D level3D = assertLevel3D();
        final GameLevel level = level3D.level();
        final State<GameContext> newState = assertGame().state();

        level3D.replaceLevelCounterView3D(level, assertGame().session().hud().levelCounter());

        //TODO rethink this
        if (newState instanceof AbstractGameState gameState && gameState.id() instanceof TestStateID) {
            playScene3D.replaceLevel3D(assertGame(), level);
            playScene3D.assertView3D().animationSystem().startEnergizerPumping(level3D);
            final MessageView messageView = level.entitySet().theOne(MessageView.class);
            showMessage(level3D, messageView, LevelMessageType.TEST, level.number());
        }

        playScene3D.replaceActionBindings(assertGame().session(), level);

        playScene3D.assertView3D().animationRegistry().optAnimation(PlaySceneView3D_FadeInAnimation.ANIMATION_NAME)
            .ifPresent(ManagedAnimation::replay);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent event) {
        final GameLevelView3D level3D = assertLevel3D();
        final long tick = playScene3D.engine().clock().currentTick();

        if (event.allPellets()) {
            level3D.pellets3D().map(PelletView3D::root).forEach(shape -> level3D.root().getChildren().remove(shape));
        }
        else {
            final Vector2i tile = event.pac().position().tile();
            if (event.energizer()) {
                level3D.energizer3DAt(tile).ifPresent(energizer3D -> {
                    playScene3D.assertView3D().animationSystem().stopPumping(energizer3D);
                    energizer3D.hide();
                    triggerEnergizerExplosion(energizer3D.root().localToScene(Point3D.ZERO));
                });
                optSoundEffects().ifPresent(PacManGameSoundEffects::playEnergizerExplosion);
            }
            else {
                level3D.pellet3DAtTile(tile).ifPresent(pelletView3D -> removePelletAfterDelay(level3D, pelletView3D));
                optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
            }
        }
    }

    private void triggerEnergizerExplosion(Point3D center) {
        playScene3D.assertView3D().animationRegistry()
            .optAnimation(PlayScene3DAnimationID.PARTICLES, ParticlesAnimation3D.class)
            .ifPresent(animation -> animation.triggerExplosion(center));
    }

    private void removePelletAfterDelay(GameLevelView3D level3D, PelletView3D pelletView3D) {
        pauseSecThen(PELLET_EATING_DELAY_SEC, () -> level3D.root().getChildren().remove(pelletView3D.root())).play();
    }

    @Override
    public void onPacPowerStarts(PacPowerStartsEvent e) {
        final Pac pac = e.pac();
        final GameLevel level = assertLevel3D().level();

        optSoundEffects().ifPresent(PacManGameSoundEffects::stopSiren);
        if (!assertGame().rules().isLevelCompleted(level)) {
            final PacView3DSystem pacView3DSystem = assertGame().systems().assertSystem(PacView3DSystem.class);
            pacView3DSystem.setPowerModeAnimation(pac, true);
            playScene3D.assertView3D().animationSystem().startWallFlashing();
            optSoundEffects().ifPresent(PacManGameSoundEffects::playPacPowerSound);
        }
    }

    @Override
    public void onPacPowerEnds(PacPowerEndsEvent e) {
        final Pac pac = e.pac();
        final PacView3DSystem pacView3DSystem = assertGame().systems().assertSystem(PacView3DSystem.class);

        pacView3DSystem.setPowerModeAnimation(pac, false);
        playScene3D.assertView3D().animationSystem().stopWallFlashing();
        optSoundEffects().ifPresent(PacManGameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent ignoredEvent) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void showMessage(GameLevelView3D level3D, MessageView messageView, LevelMessageType type, Object... args) {
        final GameLevel level = level3D.level();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final var center = switch (type) {
            case READY -> {
                final House house = level.entitySet().theOne(House.class);
                yield house.centerPositionUnderHouse();
            }
            case TEST -> vec2_float(terrain.numCols() * HTS, (terrain.numRows() - 2) * TS);
        };
        MessageView3DDisplaySystem.showMessage(
            messageView,
            level3D.root(),
            center,
            GlobalFonts.ARCADE.font(),
            type,
            args);
    }

    private void onHuntingStart(GameLevelView3D level3D) {
        final GameLevel level = level3D.level();

        playScene3D.initPac3DProperties(assertGame().systems(), level, level.entitySet().pac());

        final PlaySceneView3DAnimationSystem animationSystem = playScene3D.assertView3D().animationSystem();
        animationSystem.startEnergizerPumping(level3D);
        animationSystem.startParticlesAnimation();
        animationSystem.startGhostLightAnimation();
    }

    private void onPacManDying() {
        final GameLevel level = assertLevel3D().level();

        assertGame().state().stateTimer().resetToIndefiniteDuration();

        optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);

        final Bonus bonus = level.entitySet().anyOfTypeOrNull(Bonus.class);
        if (bonus != null) {
            final Bonus3DUpdateSystem updateSystem3D = assertGame().systems().assertSystem(Bonus3DUpdateSystem.class);
            updateSystem3D.update(bonus, playScene3D.assertView3D().animationRegistry());
        }

        playScene3D.assertView3D().animationSystem().stopAnimationsBeforePacManDies();

        final PacView3DSystem pacView3DSystem = assertGame().systems().assertSystem(PacView3DSystem.class);
        pacView3DSystem.playDyingAnimation(
            level.entitySet().pac(),
            () -> optSoundEffects().ifPresent(PacManGameSoundEffects::playPacDeadSound),
            assertGame().state()::triggerTimeout
        );
    }

    private void onGhostsKilled(GameLevelView3D level3D) {
        final GameSession session = assertGame().session();
        final SimulationFrame thisFrame = session.thisFrame();
        thisFrame.ghostsKilled().forEach(ghost -> {
            final int index = ghost.state().killChainIndex();
            level3D.addKilledGhostNumberBox(ghost, playScene3D.uiConfig(), index);
        });
    }

    private void onLevelComplete() {
        final GameViewModel viewModel = playScene3D.engine().ui().viewModel();
        final GameLevel level = assertLevel3D().level();
        final House house = level.entitySet().theOne(House.class);
        final HouseView3DSystem houseSystem3D = assertGame().systems().assertSystem(HouseView3DSystem.class);
        final boolean cutSceneFollows = !assertGame().session().booleanValue(SessionKey.DEMO_LEVEL_MODE)
            && assertGame().rules().cutSceneAfterLevel(level.number()).isPresent();

        playScene3D.assertView3D().opacityProperty().set(0);

        houseSystem3D.hideDoors(house);

        optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);

        final GameLevelView3D level3D = assertLevel3D();
        playScene3D.assertView3D().animationSystem().stopAllAnimations();
        level3D.cleanupFoodAndParticles();
        final Bonus bonus = level.entitySet().anyOfTypeOrNull(Bonus.class);
        if (bonus != null) {
            final Bonus3DUpdateSystem updateSystem3D = assertGame().systems().assertSystem(Bonus3DUpdateSystem.class);
            updateSystem3D.update(bonus, playScene3D.assertView3D().animationRegistry());
        }

        final MessageView messageView = level.entitySet().theOne(MessageView.class);
        MessageView3DAnimationSystem.hideMessageView(messageView);

        playLevelEndAnimation(
            viewModel.common3DSettings(), viewModel.maze3DSettings(),
            level3D.maze3D(),
            cutSceneFollows);
    }

    private void playLevelEndAnimation(
        Game3DSettingsViewModel settings3D,
        Maze3DSettingsViewModel maze3DSettings,
        WorldMapView3D maze3D,
        boolean cutSceneFollows)
    {
        final PlayScene3DAnimationID animationID = cutSceneFollows
            ? PlayScene3DAnimationID.LEVEL_COMPLETED_SHORT
            : PlayScene3DAnimationID.LEVEL_COMPLETED_FULL;

        final Optional<ManagedAnimation> levelEndAnimation = playScene3D.assertView3D().animationRegistry().optAnimation(animationID);

        if (levelEndAnimation.isEmpty()) {
            Ufx.pauseSecThen(2, () -> assertGame().state().triggerTimeout()).play();
            return;
        }

        assertGame().state().stateTimer().resetToIndefiniteDuration();

        final PerspectiveID perspectiveBeforeAnimation = settings3D.cameraPerspectiveIDProperty().get();

        final Animation resetCameraPerspective = pauseSecThen(2, () -> {
            settings3D.cameraPerspectiveIDProperty().set(PerspectiveID.TOTAL);
            maze3D.wallBaseHeightProperty().unbind();
        });

        final Animation restoreCameraPerspective = Ufx.pauseSecThen(0.25, () -> {
            settings3D.cameraPerspectiveIDProperty().set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(maze3DSettings.wallHeightProperty());
        });

        final var seq = new SequentialTransition(
            resetCameraPerspective,
            levelEndAnimation.get().delegate(),
            restoreCameraPerspective
        );
        seq.setOnFinished(_ -> assertGame().state().triggerTimeout());
        seq.play();
    }

    private void onGameOver() {
        final GameSession session = assertGame().session();
        final GameLevelView3D level3D = assertLevel3D();

        if (!session.booleanValue(SessionKey.DEMO_LEVEL_MODE) && RandomNumbers.chance(0.25)) {
            playScene3D.engine().ui().showMessage(Duration.seconds(2.5), playScene3D.textPicker().selectNextText());
        }

        playScene3D.assertView3D().animationSystem().stopAllAnimations();
        level3D.cleanupFoodAndParticles();
        final Bonus bonus = level3D.level().entitySet().anyOfTypeOrNull(Bonus.class);
        if (bonus != null) {
            final Bonus3DUpdateSystem updateSystem3D = assertGame().systems().assertSystem(Bonus3DUpdateSystem.class);
            updateSystem3D.update(bonus, playScene3D.assertView3D().animationRegistry());
        }
        playScene3D.optComponent(PacManGameSoundEffects.class).ifPresent(PacManGameSoundEffects::playGameOverSound);
    }

    private void handleTestState(Game3DSettingsViewModel globals3D, GameLevel level) {
        playScene3D.optView3D().flatMap(PlaySceneView3D::optGameLevel3D).ifPresent(level3D -> {
            playScene3D.replaceLevel3D(assertGame(), level);
            final MessageView messageView = level.entitySet().theOne(MessageView.class);
            showMessage(level3D, messageView, LevelMessageType.TEST, level.number());
            globals3D.cameraPerspectiveIDProperty().set(PerspectiveID.TOTAL);
        });
    }

    private GameLevelView3D assertLevel3D() {
        return playScene3D.assertView3D().assertLevel3D();
    }
}
