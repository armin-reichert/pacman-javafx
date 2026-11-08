/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.ui.assets.RandomTextPicker;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.rendering.Renderable;
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
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.BonusView3D;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.entities3D.score.Score3DSystem;
import de.amr.pacmanfx.ui.entities3D.score.ScoresView;
import de.amr.pacmanfx.ui.entities3D.world.system.World3DUpdateSystem;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.view3d.PlaySceneView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.PlaySceneView3DAnimations;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.PlaySceneView3DFadeInAnimation;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import de.amr.pacmanfx.ui.systems.PacSystems3D;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsViewModel;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import javafx.beans.value.ChangeListener;
import javafx.scene.SubScene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.engine.action.ActionExecutor.executeMatchingAction;
import static java.util.Objects.requireNonNull;

public class PlayScene3D extends AbstractGameScene {

    private ChangeListener<DrawMode> drawModeChangeListener;

    private PlayScene3D_GameEventHandler gameEventHandler;

    private PacEatingEventHandler pacEatingEventHandler;

    private PacPowerEventHandler pacPowerEventHandler;

    private GameLevelView3D level3D;

    private PlaySceneContextMenu contextMenu;

    private RandomTextPicker textPicker;

    private final PlayScene3DAnimationSystem animationSystem;

    public PlayScene3D() {
        setComponent(PlaySceneView3D.class, new PlaySceneView3D());

        final var animations = new PlaySceneView3DAnimations();
        setComponent(PlaySceneView3DAnimations.class, animations);

        final var fadeInAnimation = new PlaySceneView3DFadeInAnimation(Duration.seconds(3), view3D());
        animations.registry().register(fadeInAnimation.name(), fadeInAnimation);

        animationSystem = new PlayScene3DAnimationSystem(animations);

        gameEventHandler = new PlayScene3D_GameEventHandler(this);

        drawModeChangeListener = (_, _, drawMode) -> {
            if (level3D != null) {
                level3D.setDrawMode(drawMode);
            }
        };
    }

    public PlaySceneView3D view3D() {
        return assertComponent(PlaySceneView3D.class);
    }

    public PlaySceneView3DAnimations animations3D() {
        return assertComponent(PlaySceneView3DAnimations.class);
    }

    @Override
    protected void onEngineConnected() {
        final GameViewModel viewModel = engine.ui().viewModel();

        textPicker = new RandomTextPicker(engine().translationManager().textBundle(), "game.over");

        final var view3D = assertComponent(PlaySceneView3D.class);
        view3D.coordinateSystem().visibleProperty().bind(viewModel.common3DSettings().axesVisibleProperty());
        view3D.ambientLight().colorProperty().bind(viewModel.maze3DSettings().lightColorProperty());
    }

    public Optional<GameEventListener> optGameEventHandler() {
        return Optional.of(gameEventHandler);
    }

    @Override
    public Stream<Renderable> renderables() {
        return Stream.empty();
    }

    public RandomTextPicker textPicker() {
        return textPicker;
    }

    @Override
    public void dispose() {
        // Dispose the scene components
        super.dispose();

        disposeContextMenu();

        if (level3D != null) {
            level3D.dispose();
            level3D = null;
        }

        drawModeChangeListener = null;
        gameEventHandler = null;

        animationSystem.dispose();

    }

    @Override
    public void onBeforeEmbedded() {
        engine().optCurrentGame().ifPresent(game ->
            game.systems().assertSystem(Score3DSystem.class).replaceScoresView(
                this,
                game.session().hud().gameScore(),
                game.session().hud().highScore(),
                engine.translationManager()
            ));
    }

    @Override
    public void onActivate() {
        final Game3DSettingsViewModel settings3D = engine.ui().viewModel().common3DSettings();

        view3D().perspectiveManager().activeIDProperty().bind(settings3D.cameraPerspectiveIDProperty());
        view3D().subScene().setFill(Color.BLACK);

        settings3D.drawModeProperty().addListener(drawModeChangeListener);

        registerActionBindings();

        engine().optCurrentGame().ifPresent(game -> {
            pacEatingEventHandler = new PacEatingEventHandler(game);
            pacPowerEventHandler  = new PacPowerEventHandler(game);
            game.eventManager().addSubscriber(pacEatingEventHandler);
            game.eventManager().addSubscriber(pacPowerEventHandler);
        });
    }

    @Override
    public void onDeactivate() {
        final Game3DSettingsViewModel settings3D = engine.ui().viewModel().common3DSettings();

        view3D().perspectiveManager().activeIDProperty().unbind();

        settings3D.drawModeProperty().removeListener(drawModeChangeListener);

        disposeContextMenu();

        // Remove actor 3D view components
        engine().optCurrentGame().ifPresent(game -> {
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
        });
    }

    @Override
    public void onInput() {
        executeMatchingAction(actionBindingsRegistry(), engine).ifPresentOrElse(
            _ -> {}, // action matching keyboard state has been executed
            () -> view3D().handleKeyboardInput(engine().input().keyboard())
        );
    }

    @Override
    public void onTick(GameContext game) {
        final GameSession session = game.session();
        final SimulationFrame thisFrame = session.thisFrame();
        final long tick = thisFrame.tickCount();

        if (session.optLevel().isEmpty()) {
            Logger.info("Tick {}: Game level not yet created, update ignored", tick);
            return;
        }

        final GameLevel level = session.optLevel().orElseThrow();
        if (level3D == null) {
            Logger.info("Tick {}: Game level 3D not yet created, update ignored", tick);
            return;
        }

        level.entitySet().anyOfType(Bonus.class).ifPresent(bonus ->
            level3D.ensureBonus3DViewAddedToSceneGraph(bonus, animations3D().registry()));

        //TODO move out of this class?
        final World3DUpdateSystem updateSystem = game.systems().assertSystem(World3DUpdateSystem.class);
        updateSystem.updateEntities(game, animations3D().registry());

        updateScoresView(game);

        ensureAnimationsRunning(game);

        view3D().perspectiveManager().updatePerspective(level);

        optComponent(PacManGameSoundEffects.class).ifPresent(soundEffects -> {
            soundEffects.setEnabled(!session.booleanValue(SessionKey.DEMO_LEVEL_MODE));
            soundEffects.playAmbientGameLevelSound(game, level);
        });
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        view3D().handleScrollEvent(scrollEvent);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(assertComponent(PlaySceneView3D.class).subScene());
    }

    @Override
    public List<MenuItem> contextMenuItems() {
        contextMenu = new PlaySceneContextMenu(this);
        return contextMenu.getItems();
    }

    @Override
    public void onQuit() {
        onDeactivate();
        engine().soundManager().setEnabled(false);
        engine().optCurrentGame().ifPresent(game -> game.flow().enterGameState(game, CommonGameStateID.GAME_OVER));
    }

    // Other stuff

    public PlayScene3DAnimationSystem animationSystem() {
        return animationSystem;
    }


    public Optional<GameLevelView3D> optGameLevel3D() {
        return Optional.ofNullable(level3D);
    }

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

    public void initPac3DProperties(GameContext game, GameLevel level, Pac pac) {
        requireNonNull(pac);
        requireNonNull(level);

        final PacSystems3D systems3D = game.systems().assertSystem(PacSystems3D.class);
        systems3D.transformSystem().init(pac, level.worldMap());
        systems3D.animationSystem().stopAnimations(pac);
        systems3D.animationSystem().setPowerMode(pac, false);
    }

    public void initFood3D(GameLevel level, boolean startEnergizerPumping) {
        level3D.pellets3D().forEach(pellet3D -> pellet3D.root().setVisible(!level.food().hasEatenFoodAtTile(pellet3D.tile())));

        if (startEnergizerPumping) {
            animationSystem.startEnergizerPumping(level3D);
        }
        level3D.energizers3D()
            .forEach(energizer3D -> energizer3D.root().setVisible(!level.food().hasEatenFoodAtTile(energizer3D.tile())));
    }

    public void replaceGameLevel3D(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameViewModel viewModel = engine.ui().viewModel();
        final GameSession session     = game.session();

        if (level3D != null) {
            Logger.info("Old 3D game level is disposed...");
            level3D.dispose();
        }

        // Create a new 3D game level representation
        level3D = new GameLevelView3D(game, viewModel, uiConfig());

        addAdditional3DLevelElements(level3D);

        level3D.replaceLevelCounter3D(game, session.hud().levelCounter());

        view3D().level3DHolder().getChildren().setAll(level3D.root());

        animationSystem.createAnimations(engine, runtime(), game.session(), level3D);

        //TODO check this
        final Pac pac = level.entitySet().pac();
        initPac3DProperties(game, level, pac);

        // Lives counter shapes follow Pac location
        final LivesCounter livesCounter = session.hud().livesCounter();
        final LivesCounterView3DSystem livesCounterView3DSystem = game.systems().assertSystem(LivesCounterView3DSystem.class);
        livesCounterView3DSystem.startTrackingPac(livesCounter, pac);
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
        }
    }

    private void ensureAnimationsRunning(GameContext game) {
        if (game.state().hasSameNameAs(CommonGameStateID.DEMO_LEVEL_PLAYING) ||
            game.state().hasSameNameAs(CommonGameStateID.GAME_LEVEL_PLAYING)) {
            animationSystem.startEnergizerPumping(level3D);
        }
    }
}