/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameActionBindingsManager;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.layout.HelpLayer;
import de.amr.pacmanfx.ui.layout.View;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.ui.GameSceneConfig.CommonSceneID;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.border;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView implements View {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = border(Color.LIGHTGREEN, 1);

    public static final DecorationPane.Config DECORATION_CONFIG = new DecorationPane.Config(
        0.85f,
        0.93f,
        0.5f,
        20,
        20,
        new DecorationPane.FrameConfig(
            26,
            10,
            5,
            55.0,
            ArcadePalette.ARCADE_WHITE)
    );

    private final ActionBindingsManager actionBindings = new GameActionBindingsManager(Input.instance().keyboard);

    private final ObjectProperty<GameScene> gameScene = new SimpleObjectProperty<>();

    private final GameUI ui;
    private final ContextMenu contextMenu;
    private final Scene parentSceneFX;
    private StackPane rootPane;

    private BorderPane gameSceneLayer;
    private DecorationPane gameSceneDecorationPane;

    private BorderPane overlayLayer;
    private Dashboard dashboard;
    private MiniGameView miniView;

    private HelpLayer helpLayer;

    private FontAwesomeIcon pausedIcon;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    private final PlayViewGameEventHandler gameEventHandler = new PlayViewGameEventHandler(this);
    private final ChangeListener<GameScene> gameSceneChangeHandler;
    private final ChangeListener<? super Number> parentSceneSizeChangeHandler;

    private final PlayViewGameSceneEmbedder gameSceneEmbedder = new PlayViewGameSceneEmbedder();
    private final GameSceneSwitchHandler gameSceneSwitchHandler = new GameSceneSwitchHandler();

    public PlayView(GameUI ui, Scene parentSceneFX, DashboardConfig dashboardConfig) {
        requireNonNull(ui);
        requireNonNull(parentSceneFX);
        requireNonNull(dashboardConfig);

        this.ui = ui;
        this.parentSceneFX = parentSceneFX;

        createLayout(dashboardConfig);
        configurePropertyBindings();

        miniView.setUI(ui);

        contextMenu = new ContextMenu();
        rootPane.setOnContextMenuRequested(new PlayViewContextMenuHandler(this, parentSceneFX));

        ui.gameContext().gameVariantNameProperty().addListener(new PlayViewGameVariantChangeHandler(this));

        gameSceneChangeHandler = (_, _, gameScene) -> {
            contextMenu.hide();
            if (gameScene != null) {
                gameSceneEmbedder.embedGameScene(ui, this, gameScene);
            }
        };
        parentSceneSizeChangeHandler = (_, _, _) -> gameSceneDecorationPane.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
    }

    public PlayViewGameEventHandler gameEventHandler() {
        return gameEventHandler;
    }

    public PlayViewGameSceneEmbedder embedder() {
        return gameSceneEmbedder;
    }

    public GameUI ui() {
        return ui;
    }

    public ContextMenu contextMenu() {
        return contextMenu;
    }

    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScene;
    }

    public Scene parentSceneFX() {
        return parentSceneFX;
    }

    public DecorationPane decorationPane() {
        return gameSceneDecorationPane;
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
        final double scaling = gameSceneDecorationPane.scalingProperty().get();
        helpLayer.showHelpPopup(ui, scaling, ui.gameContext().gameVariantName());
    }

    public void forceGameSceneUpdate() {
        updateGameScene(true);
    }

    public void setGameSceneContent(Node gameSceneContent) {
        gameSceneLayer.setCenter(gameSceneContent);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    @Override
    public void onKeyboardInput(GameUI ui) {
        actionBindings.matchingAction().ifPresentOrElse(
            action -> action.execute(ui),
            () -> optCurrentGameScene().ifPresent(GameScene::onInput)
        );
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        addListeners();
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

        // Render current 2D game scene
        final GameScene gameScene = optCurrentGameScene().orElse(null);
        if (gameScene instanceof GameScene2D gameScene2D) {
            final Game game = ui.gameContext().game();
            if (sceneRenderer != null) {
                sceneRenderer.draw(gameScene2D);
            }
            if (hudRenderer != null) {
                hudRenderer.draw(game.hud(), game, gameScene2D);
            }
        }

        // Render mini view content
        if (miniView().canDraw()) {
            miniView.draw();
        }

        // Dashboard must always be updated even if simulation is stopped!
        if (overlayLayer.isVisible()) {
            dashboard.update(ui);
        }
    }

    public void updateGameScene(boolean forceReload) {
        final Game game = ui.gameContext().game();
        final GameScene prevGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = ui.currentGameSceneConfig().selectGameScene(ui, game).orElseThrow();

        if (nextGameScene == prevGameScene && !forceReload) {
            return;
        }

        if (prevGameScene != null) {
            prevGameScene.deactivate();
        }

        nextGameScene.onEmbedded(); // Must be called *before* embedding
        gameSceneEmbedder.embedGameScene(ui, this, nextGameScene);
        nextGameScene.activate();

        game.optGameLevel().ifPresent(level -> gameSceneSwitchHandler.handleGameSceneSwitch(
            ui.currentConfig(), level, prevGameScene, nextGameScene));

        gameSceneProperty().set(nextGameScene);
    }

    public void updateGameSceneRenderers(GameScene2D gameScene2D) {
        if (gameScene2D.canvas() != null) {
            sceneRenderer = ui.currentConfig().createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
            setFontSmoothing(GameUIConstants.PROPERTY_CANVAS_FONT_SMOOTHING.get());
            hudRenderer = ui.currentConfig().createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    // Private

    private void createLayout(DashboardConfig dashboardConfig) {

        // Layer 1: Game scene with or without decoration
        gameSceneDecorationPane = new DecorationPane(
            DECORATION_CONFIG,
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );
        gameSceneLayer = new BorderPane();
        gameSceneLayer.setCenter(gameSceneDecorationPane);

        // Layer 2: Overlay layer with dashboard and mini-view for 3D scene
        miniView = new MiniGameView();

        dashboard = new Dashboard(dashboardConfig);
        dashboard.setVisible(false);

        overlayLayer = new BorderPane();
        overlayLayer.setLeft(dashboard);
        overlayLayer.setRight(miniView.rootPane());

        // Layer 3: Help info
        helpLayer = new HelpLayer(gameSceneLayer);

        // Layer 4: "Paused" icon
        pausedIcon  = FontAwesomeIcon.of(FontAwesomeIcon.Symbol.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        pausedIcon.setFocusTraversable(false);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        rootPane = new StackPane(gameSceneLayer, overlayLayer, helpLayer, pausedIcon);
    }

    private void addListeners() {
        gameScene.addListener(gameSceneChangeHandler);
        parentSceneFX.widthProperty() .addListener(parentSceneSizeChangeHandler);
        parentSceneFX.heightProperty().addListener(parentSceneSizeChangeHandler);
        gameSceneDecorationPane.installBindings();
    }

    private void removeListeners() {
        gameScene.removeListener(gameSceneChangeHandler);
        parentSceneFX.widthProperty().removeListener(parentSceneSizeChangeHandler);
        parentSceneFX.heightProperty().removeListener(parentSceneSizeChangeHandler);
        gameSceneDecorationPane.uninstallBindings();
    }

    private void configurePropertyBindings() {
        pausedIcon.visibleProperty().bind(ui.gameContext().clock().updatesDisabledProperty());

        GameUIConstants.PROPERTY_CANVAS_FONT_SMOOTHING.addListener((_, _, smoothing) -> setFontSmoothing(smoothing));

        GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE.addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(Bindings.or(dashboard.visibleProperty(), GameUIConstants.PROPERTY_MINI_VIEW_ON));

        miniView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> GameUIConstants.PROPERTY_MINI_VIEW_ON.get() && ui.currentGameSceneHasID(CommonSceneID.PLAY_SCENE_3D),
            GameUIConstants.PROPERTY_MINI_VIEW_ON, gameScene
        ));
    }

    private void setFontSmoothing(boolean smoothing) {
        if (sceneRenderer != null) {
            sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
        }
    }
}