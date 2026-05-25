/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameActionBindingsManager;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.layout.HelpLayer;
import de.amr.pacmanfx.ui.layout.View;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameSceneConfig.CommonSceneID;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.border;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView implements View {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    private final PlayViewGameEventHandler gameEventHandler = new PlayViewGameEventHandler(this);

    private final ObjectProperty<GameScene> gameScene = new SimpleObjectProperty<>();

    private final GameUI ui;
    private final ContextMenu contextMenu;
    private final Scene parentScene;
    private StackPane rootPane;
    private GameSceneDecorationPane decorationPane;
    private MiniGameView miniView;
    private BorderPane canvasLayer;
    private BorderPane widgetLayer;
    private HelpLayer helpLayer;
    private FontAwesomeIcon pausedIcon;

    private final ActionBindingsManager actionBindings = new GameActionBindingsManager(Input.instance().keyboard);

    private ChangeListener<GameScene> gameSceneChangeListener;
    private ChangeListener<? super Number> parentSceneWidthListener;
    private ChangeListener<? super Number> parentSceneHeightListener;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    private Dashboard dashboard;

    public PlayView(GameUI ui, Scene parentScene, DashboardConfig dashboardConfig) {
        requireNonNull(ui);
        requireNonNull(parentScene);
        requireNonNull(dashboardConfig);

        this.ui = ui;
        this.parentScene = parentScene;

        createLayout(dashboardConfig);

        configureGameSceneDecorationPane();
        configureActionBindings();
        configurePropertyBindings();

        miniView.setUI(ui);

        contextMenu = new ContextMenu();
        rootPane.setOnContextMenuRequested(new PlayViewContextMenuHandler(this, parentScene));

        ui.gameContext().gameVariantNameProperty().addListener(new PlayViewGameVariantChangeHandler(this));
    }

    public PlayViewGameEventHandler gameEventHandler() {
        return gameEventHandler;
    }

    public GameUI ui() {
        return ui;
    }

    public ContextMenu contextMenu() {
        return contextMenu;
    }

    public Scene parentScene() {
        return parentScene;
    }

    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScene;
    }

    public Optional<GameScene> optCurrentGameScene() {
        return Optional.ofNullable(gameScene.get());
    }

    public MiniGameView miniView() {
        return miniView;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public void showHelp(GameUI ui) {
        final double scaling = decorationPane.scalingProperty().get();
        helpLayer.showHelpPopup(ui, scaling, ui.gameContext().gameVariantName());
    }

    public void forceGameSceneUpdate() {
        updateGameScene(true);
    }

    public void updateGameScene() {
        updateGameScene(false);
    }

    public void resizeGameScene(GameScene2D gameScene2D) {
        decorationPane.setUnscaledSize(gameScene2D.getUnscaledWidth(), gameScene2D.getUnscaledHeight());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI_View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    @Override
    public void onKeyboardInput(GameUI ui) {
        actionBindings.matchingAction().ifPresentOrElse(
            action -> action.execute(ui),
            () -> optCurrentGameScene().ifPresent(GameScene::onUserInput)
        );
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        addListeners();
        decorationPane.updateLayout();
    }

    @Override
    public void onExit() {
        removeListeners();
    }

    @Override
    public StackPane root() {
        return rootPane;
    }

    @Override
    public void render() {
        final Game game = ui.gameContext().game();
        optCurrentGameScene().filter(GameScene2D.class::isInstance).map(GameScene2D.class::cast).ifPresent(gameScene2D -> {
            if (sceneRenderer != null) {
                sceneRenderer.draw(gameScene2D);
            }
            if (hudRenderer != null) {
                hudRenderer.draw(game.hud(), game, gameScene2D);
            }
        });
        miniView.draw();
        // Dashboard must also be updated if simulation is stopped
        if (widgetLayer.isVisible()) {
            dashboard.update(ui);
        }
    }

    // ---

    private void createLayout(DashboardConfig dashboardConfig) {
        rootPane = new StackPane();
        decorationPane = new GameSceneDecorationPane();
        miniView = new MiniGameView();
        canvasLayer = new BorderPane();
        helpLayer = new HelpLayer(decorationPane);
        widgetLayer = new BorderPane();
        pausedIcon  = FontAwesomeIcon.of(FontAwesomeIcon.Symbol.PAUSE, 80, ArcadePalette.ARCADE_WHITE);

        dashboard = new Dashboard(dashboardConfig);
        dashboard.setVisible(false);

        StackPane.setAlignment(pausedIcon, Pos.CENTER);
        widgetLayer.setLeft(dashboard);
        widgetLayer.setRight(miniView.container());
        canvasLayer.setCenter(decorationPane);

        rootPane.getChildren().addAll(canvasLayer, widgetLayer, helpLayer, pausedIcon);
    }

    private void updateGameScene(boolean forcedReload) {
        final Game game = ui.gameContext().game();
        final GameScene prevGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = ui.currentGameSceneConfig().selectGameScene(ui, game).orElseThrow();

        if (nextGameScene == prevGameScene && !forcedReload) {
            return;
        }

        if (prevGameScene != null) {
            prevGameScene.end();
            Logger.info("Game scene ended: {}", prevGameScene.getClass().getSimpleName());
        }

        nextGameScene.onEmbeddedIntoUI(); // Must be called *before* embedding
        embedGameScene(parentScene, nextGameScene);
        nextGameScene.init();
        Logger.info("Game scene initialized: {}", nextGameScene.getClass().getSimpleName());

        // Handle switching between 2D and 3D play scene view
        game.optGameLevel().ifPresent(level -> {
            final byte sceneSwitchType = identifySceneSwitchType(prevGameScene, nextGameScene);
            switch (sceneSwitchType) {
                case 23 -> switchPlaySceneTo3D(level, prevGameScene, nextGameScene);
                case 32 -> switchPlaySceneTo2D(prevGameScene, nextGameScene);
                case  0 -> {}
                default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
            }
        });

        gameSceneProperty().set(nextGameScene);
    }

    // Others

    private byte identifySceneSwitchType(GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case GameScene2D ignored when sceneAfter instanceof PlayScene3D -> 23;
            case PlayScene3D ignored when sceneAfter instanceof GameScene2D -> 32;
            case null, default -> 0; // may happen, it's ok
        };
    }

    private void addListeners() {
        removeListeners();

        gameSceneChangeListener = (_, _, gameScene) -> {
            contextMenu.hide();
            if (gameScene != null) {
                embedGameScene(parentScene, gameScene);
            }
        };
        gameSceneProperty().addListener(gameSceneChangeListener);

        parentSceneWidthListener = (_, _, w) -> decorationPane.resizeTo(w.doubleValue(), parentScene.getHeight());
        parentScene.widthProperty() .addListener(parentSceneWidthListener);

        parentSceneHeightListener = (_, _, h) -> decorationPane.resizeTo(parentScene.getWidth(), h.doubleValue());
        parentScene.heightProperty().addListener(parentSceneHeightListener);
    }

    private void removeListeners() {
        if (gameSceneChangeListener != null) {
            gameSceneProperty().removeListener(gameSceneChangeListener);
        }
        if (parentSceneWidthListener != null) {
            parentScene.widthProperty().removeListener(parentSceneWidthListener);
        }
        if (parentSceneHeightListener != null) {
            parentScene.heightProperty().removeListener(parentSceneHeightListener);
        }
    }

    private void configureGameSceneDecorationPane() {
        decorationPane.setMinScaling(0.5);
        decorationPane.setUnscaledSize(ARCADE_MAP_SIZE_IN_PIXELS.x(), ARCADE_MAP_SIZE_IN_PIXELS.y());
        decorationPane.setBorderColor(ArcadePalette.ARCADE_WHITE);
    }

    private void configurePropertyBindings() {
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ui.gameContext().clock().getUpdatesDisabled(),
            ui.gameContext().clock().updatesDisabledProperty())
        );

        GameUIConstants.PROPERTY_CANVAS_FONT_SMOOTHING.addListener((_, _, smooth) ->
            decorationPane.canvas().getGraphicsContext2D().setFontSmoothingType(
                smooth ? FontSmoothingType.LCD : FontSmoothingType.GRAY));

        GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE.addListener((_, _, debug) -> {
            canvasLayer.setBackground(debug ? paintBackground(Color.TEAL) : null);
            canvasLayer.setBorder(debug ? border(Color.LIGHTGREEN, 1) : null);
        });

        widgetLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || GameUIConstants.PROPERTY_MINI_VIEW_ON.get(),
            dashboard.visibleProperty(), GameUIConstants.PROPERTY_MINI_VIEW_ON
        ));

        miniView.container().visibleProperty().bind(Bindings.createObjectBinding(
            () -> GameUIConstants.PROPERTY_MINI_VIEW_ON.get() && ui.currentGameSceneHasID(CommonSceneID.PLAY_SCENE_3D),
            GameUIConstants.PROPERTY_MINI_VIEW_ON, gameScene
        ));
    }

    private void configureActionBindings() {
        actionBindings.addAny(ACTION_BOOT_SHOW_PLAY_VIEW, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_ENTER_FULLSCREEN, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_QUIT_GAME_SCENE, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SHOW_HELP, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_SLOWER, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_SLOWEST, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_FASTER, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_FASTEST, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_RESET, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_ONE_STEP, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_TEN_STEPS, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_AUTOPILOT, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_DEBUG_INFO, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_MUTED, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_PAUSED, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_COLLISION_STRATEGY, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_DASHBOARD, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_IMMUNITY, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_PLAY_SCENE_2D_3D, GameUIConstants.COMMON_BINDINGS);
    }

    private void useDecoratedCanvas(GameScene2D gameScene2D) {
        final Canvas canvas = new Canvas();
        decorationPane.setCanvas(canvas);

        gameScene2D.setCanvas(canvas);
        gameScene2D.backgroundProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR);

        updateGameScene2DRenderer(gameScene2D);
    }

    private void updateGameScene2DRenderer(GameScene2D gameScene2D) {
        sceneRenderer = ui.currentConfig().createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
        hudRenderer   = ui.currentConfig().createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
    }

    private void embedGameSceneWithSubscene(Scene parentSceneFX, GameScene gameScene, SubScene subSceneFX) {
        subSceneFX.widthProperty().bind(parentSceneFX.widthProperty());
        subSceneFX.heightProperty().bind(parentSceneFX.heightProperty());
        if (gameScene instanceof GameScene2D gameScene2D) {
            useDecoratedCanvas(gameScene2D);
        }
        rootPane.getChildren().set(0, subSceneFX);
    }

    private void embedGameScene2D(Scene parentSceneFX, GameScene2D gameScene2D) {
        useDecoratedCanvas(gameScene2D);
        double aspect = gameScene2D.getAspectRatio();
        if (ui.currentGameSceneConfig().sceneDecorationRequested(gameScene2D)) {
            // Decorated game scene scaled-down to give space for the decoration
            gameScene2D.scalingProperty().bind(decorationPane.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), MAX_GAME_SCENE_SCALING)));

            decorationPane.setUnscaledSize(gameScene2D.getUnscaledWidth(), gameScene2D.getUnscaledHeight());
            decorationPane.resizeTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
            decorationPane.backgroundProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR.map(UfxBackgrounds::paintBackground));

            canvasLayer.setCenter(decorationPane);
        }
        else {
            // Undecorated game scene taking complete height
            decorationPane.canvas().heightProperty().bind(parentSceneFX.heightProperty());
            decorationPane.canvas().widthProperty().bind(parentSceneFX.heightProperty().map(h -> h.doubleValue() * aspect));
            gameScene2D.scalingProperty().bind(parentSceneFX.heightProperty().divide(gameScene2D.getUnscaledHeight()));
            canvasLayer.setCenter(decorationPane.canvas());
        }
        rootPane.getChildren().set(0, canvasLayer);
    }

    public void embedGameScene(Scene parentSceneFX, GameScene gameScene) {
        hudRenderer = null;
        if (gameScene.optSubScene().isPresent()) {
            embedGameSceneWithSubscene(parentSceneFX, gameScene, gameScene.optSubScene().get());
        }
        else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2D(parentSceneFX, gameScene2D);
        }
        else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    private void switchPlaySceneTo3D(GameLevel level, GameScene currentScene, GameScene nextScene) {
        if (!(nextScene instanceof PlayScene3D playScene3D)) {
            throw new IllegalArgumentException("Expected PlayScene3D, but scene has class %s"
                .formatted(nextScene.getClass().getSimpleName()));
        }

        playScene3D.replaceGameLevel3D(level);
        playScene3D.updateHUD3D(level);
        playScene3D.replaceActionBindings(level);
        playScene3D.initFood3D(level.worldMap().foodLayer(), true);

        final GameLevel3D level3D = playScene3D.optGameLevel3D().orElseThrow();
        final Pac3D pac3D = level3D.pac3D();
        playScene3D.initPac3D(pac3D, level);
        level3D.startLivesCounterTrackingPac();

        if (level.pac().powerTimer().isRunning()) {
            ui.currentConfig().optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }

        Logger.info("3D scene {} entered from 3D scene {}", playScene3D.getClass().getSimpleName(), currentScene.getClass().getSimpleName());

        playScene3D.fadeInAnimation().playFromStart();
    }

    private void switchPlaySceneTo2D(GameScene currentScene, GameScene nextScene) {
        if (!(nextScene instanceof GameScene2D playScene2D)) {
            throw new IllegalArgumentException("Expected GameScene2D, but scene has class %s"
                .formatted(nextScene.getClass().getSimpleName()));
        }
        playScene2D.onEnteredFrom3DScene();
        Logger.info("2D scene {} entered from 3D scene {}", playScene2D.getClass().getSimpleName(), currentScene.getClass().getSimpleName());
    }
}