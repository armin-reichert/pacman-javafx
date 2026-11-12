/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.fsm.State;
import de.amr.basics.ui.assets.RandomTextPicker;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gameplay.PacEatingEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.SimulationFrame;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameVariantUIConfig;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.BonusView3D;
import de.amr.pacmanfx.ui.entities3D.ghost.GhostView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D_System;
import de.amr.pacmanfx.ui.entities3D.score.Score3DSystem;
import de.amr.pacmanfx.ui.entities3D.score.ScoresView;
import de.amr.pacmanfx.ui.entities3D.world.system.World3DUpdateSystem;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsViewModel;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import javafx.scene.SubScene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.engine.action.ActionExecutor.executeMatchingAction;
import static java.util.Objects.requireNonNull;

public class PlayScene3D extends AbstractGameScene {

    private PlayScene3D_GameEventHandler gameEventHandler;

    private PacEatingEventHandler pacEatingEventHandler;

    private PacPowerEventHandler pacPowerEventHandler;

    private PlaySceneContextMenu contextMenu;

    private RandomTextPicker textPicker;

    public PlayScene3D() {
        gameEventHandler = new PlayScene3D_GameEventHandler(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeContextMenu();
        gameEventHandler = null;
    }

    // Accessors

    public Optional<PlaySceneView3D> optView3D() {
        return optComponent(PlaySceneView3D.class);
    }

    public PlaySceneView3D assertView3D() {
        return assertComponent(PlaySceneView3D.class);
    }

    public Optional<GameEventListener> optGameEventHandler() {
        return Optional.of(gameEventHandler);
    }

    public RandomTextPicker textPicker() {
        return textPicker;
    }

    // Hook methods

    @Override
    protected void onEngineConnected(EngineUIContext engine) {
        final GameViewModel viewModel = engine.ui().viewModel();

        final PlaySceneView3D view3D = new PlaySceneView3D(viewModel.common3DSettings());
        view3D.coordinateSystem().visibleProperty().bind(viewModel.common3DSettings().axesVisibleProperty());
        view3D.ambientLight().colorProperty().bind(viewModel.maze3DSettings().lightColorProperty());
        setComponent(PlaySceneView3D.class, view3D);

        textPicker = new RandomTextPicker(engine.translationManager().textBundle(), "game.over");
    }

    // GameScene interface

    @Override
    public void onActivate(GameContext game) {
        final Game3DSettingsViewModel settings3D = engine.ui().viewModel().common3DSettings();

        assertView3D().perspectiveManager().activeIDProperty().bind(settings3D.cameraPerspectiveIDProperty());
        assertView3D().subScene().setFill(Color.BLACK);

        registerActionBindings();

        pacEatingEventHandler = new PacEatingEventHandler(game);
        pacPowerEventHandler  = new PacPowerEventHandler(game);
        game.eventManager().addSubscriber(pacEatingEventHandler);
        game.eventManager().addSubscriber(pacPowerEventHandler);
    }

    @Override
    public void onDeactivate(GameContext game) {
        assertView3D().perspectiveManager().activeIDProperty().unbind();

        disposeContextMenu();

        // Remove actor 3D view components
        game.session().optLevel().ifPresent(level -> {
            level.entitySet().pac().removeComponent(PacView3D.class);
            level.entitySet().ghosts().forEach(ghost -> ghost.removeComponent(GhostView3D.class));
            level.entitySet().anyOfType(Bonus.class).ifPresent(bonus -> bonus.removeComponent(BonusView3D.class));
        });

        if (pacEatingEventHandler != null) {
            game.eventManager().removeSubscriber(pacEatingEventHandler);
            pacEatingEventHandler = null;
        }
        if (pacPowerEventHandler != null) {
            game.eventManager().removeSubscriber(pacPowerEventHandler);
            pacPowerEventHandler = null;
        }
    }

    @Override
    public void onBeforeEmbedded(GameContext game) {
        game.systems().assertSystem(Score3DSystem.class).replaceScoresView(
            this,
            game.session().hud().gameScore(),
            game.session().hud().highScore(),
            engine.translationManager()
        );
    }

    @Override
    public void onInput(Input input) {
        executeMatchingAction(actionBindingsRegistry(), engine).ifPresentOrElse(
            _ -> {}, // action matching keyboard state has been executed
            () -> assertView3D().handleKeyboardInput(input.keyboard())
        );
    }

    @Override
    public void onTick(GameContext game) {
        final GameSession session = game.session();
        final SimulationFrame thisFrame = session.thisFrame();
        final long tick = thisFrame.tickCount();

        final GameLevel level = session.optLevel().orElse(null);
        if (level == null) {
            Logger.info("Tick {}: Game level not yet created, update ignored", tick);
            return;
        }

        final PlaySceneView3D view3D = optView3D().orElse(null);
        if (view3D == null) {
            Logger.info("Tick {}: Play Scene 3D view not yet created, update ignored", tick);
            return;
        }

        final GameLevelView3D levelView3D = view3D.optGameLevel3D().orElse(null);
        if (levelView3D == null) {
            Logger.info("Tick {}: Game level 3D view not yet created, update ignored", tick);
            return;
        }

        //TODO check this
        level.entitySet().anyOfType(Bonus.class).ifPresent(bonus ->
            levelView3D.ensureBonus3DViewAddedToSceneGraph(bonus, view3D.animationRegistry()));

        //TODO move out of this class?
        final World3DUpdateSystem updateSystem = game.systems().assertSystem(World3DUpdateSystem.class);
        updateSystem.updateEntities(game, view3D.animationRegistry());

        updateScoresView(game);

        ensureAnimationsRunning(game.state());

        view3D.perspectiveManager().updatePerspective(level);

        optComponent(PacManGameSoundEffects.class).ifPresent(soundEffects -> soundEffects.playAmbientGameLevelSound(game, level));
    }

    @Override
    public void onGameLevelConnected(GameContext game, GameLevel level) {
        optComponent(PacManGameSoundEffects.class).ifPresent(soundEffects
            -> soundEffects.setEnabled(!game.session().booleanValue(SessionKey.DEMO_LEVEL_MODE)));
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        assertView3D().handleScrollEvent(scrollEvent);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(assertView3D().subScene());
    }

    @Override
    public void onQuit() {
        engine.optCurrentGame().ifPresent(game -> {
            onDeactivate(game);
            engine.soundManager().setEnabled(false);
            game.flow().enterGameState(game, CommonGameStateID.GAME_OVER);
        });
    }

    @Override
    public List<MenuItem> contextMenuItems() {
        contextMenu = new PlaySceneContextMenu(this);
        return contextMenu.getItems();
    }

    @Override
    public Stream<Renderable> renderables() {
        return Stream.empty();
    }

    // Other stuff

    public void replaceActionBindings(GameSession session, GameLevel level) {
        // No-op — override in subclasses if variant needs different bindings
    }

    public void updateScoresView(GameContext game) {
        requireNonNull(game);
        final ScoresView scoresView = assertComponent(ScoresView.class);
        game.systems().assertSystem(Score3DSystem.class).updateScoresView(
            scoresView,
            game.session().hud().gameScore(),
            game.session().hud().highScore(),
            engine.translationManager(),
            uiConfig().assets()
        );
    }

    public void initPac3DProperties(CommonGameSystems systems, GameLevel level, Pac pac) {
        requireNonNull(pac);
        requireNonNull(level);

        final PacView3D_System pacView3DSystem = systems.assertSystem(PacView3D_System.class);
        pacView3DSystem.initTransform(pac);
        pacView3DSystem.stopAnimations(pac);
        pacView3DSystem.setPowerModeAnimation(pac, false);
    }

    public void replaceLevel3D(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        optView3D().ifPresent(view3D -> {
            view3D.removeLevel3D();

            final GameVariantUIConfig uiConfig = engine.uiConfig(game.runtime());
            final GameViewModel gameViewModel = engine.ui().viewModel();
            view3D.createLevel3D(uiConfig, gameViewModel, game, level);

            //TODO Replace by Tengen-specific view3D implementation
            addAdditional3DLevelElements(assertView3D().assertLevel3D());

            // Let lives counter shapes track Pac-Man location
            final LivesCounter livesCounter = game.session().hud().livesCounter();
            final LivesCounterView3DSystem livesCounterView3DSystem = game.systems().assertSystem(LivesCounterView3DSystem.class);
            livesCounterView3DSystem.startTrackingPac(livesCounter, level.entitySet().pac());
        });
    }

    /**
     * Can be overridden by 3D scenes that e.g. decorate the 3D level with additional stuff as done by the
     * Tengen Ms. Pac-Man game that displays the level number, game difficulty, map category, booster mode etc.
     */
    protected void addAdditional3DLevelElements(GameLevelView3D level3D) {}

    protected void registerActionBindings() {
        actionBindingsRegistry().registerAllBindings(CommonGameActions.instance().camera3DActions().bindings());
    }

    private void disposeContextMenu() {
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }
    }

    private void ensureAnimationsRunning(State<GameContext> gameState) {
        assertView3D().optGameLevel3D().ifPresent(level3D -> {
            if (gameState.hasSameNameAs(CommonGameStateID.DEMO_LEVEL_PLAYING) ||
                gameState.hasSameNameAs(CommonGameStateID.GAME_LEVEL_PLAYING)) {
                assertView3D().animationSystem().startEnergizerPumping(level3D);
            }
        });
    }
}