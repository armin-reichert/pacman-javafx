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
import de.amr.pacmanfx.ui.d3.PlayScene3D;
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
    private DecorationPane decorationPane;
    private MiniGameView miniView;
    private BorderPane gameSceneContentLayer;
    private BorderPane widgetLayer;
    private HelpLayer helpLayer;
    private Dashboard dashboard;
    private FontAwesomeIcon pausedIcon;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    private final PlayViewGameEventHandler gameEventHandler = new PlayViewGameEventHandler(this);
    private final ChangeListener<GameScene> gameSceneChangeHandler;
    private final ChangeListener<? super Number> parentSceneSizeChangeHandler;

    private final PlayViewGameSceneEmbedder embedder = new PlayViewGameSceneEmbedder();
    private final SceneSwitcher sceneSwitcher = new SceneSwitcher();

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
                embedder.embedGameScene(ui, this, gameScene);
            }
        };
        parentSceneSizeChangeHandler = (_, _, _) -> decorationPane.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
    }

    public PlayViewGameEventHandler gameEventHandler() {
        return gameEventHandler;
    }

    public PlayViewGameSceneEmbedder embedder() {
        return embedder;
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
        return decorationPane;
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

    public void setGameSceneContent(Node gameSceneContent) {
        gameSceneContentLayer.setCenter(gameSceneContent);
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
            () -> optCurrentGameScene().ifPresent(GameScene::onUserInput)
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
        final Game game = ui.gameContext().game();
        optCurrentGameScene().filter(GameScene2D.class::isInstance).map(GameScene2D.class::cast).ifPresent(gameScene2D -> {
            if (sceneRenderer != null) {
                sceneRenderer.draw(gameScene2D);
            }
            if (hudRenderer != null) {
                hudRenderer.draw(game.hud(), game, gameScene2D);
            }
        });

        if (miniView().canDraw()) {
            miniView.draw();
        }

        // Dashboard must also be updated if simulation is stopped
        if (widgetLayer.isVisible()) {
            dashboard.update(ui);
        }
    }

    // ---

    private void createLayout(DashboardConfig dashboardConfig) {
        rootPane = new StackPane();

        decorationPane = new DecorationPane(
            DECORATION_CONFIG,
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );

        miniView = new MiniGameView();

        gameSceneContentLayer = new BorderPane();

        helpLayer = new HelpLayer(gameSceneContentLayer);

        widgetLayer = new BorderPane();

        pausedIcon  = FontAwesomeIcon.of(FontAwesomeIcon.Symbol.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        pausedIcon.setFocusTraversable(false);

        dashboard = new Dashboard(dashboardConfig);
        dashboard.setVisible(false);

        StackPane.setAlignment(pausedIcon, Pos.CENTER);
        widgetLayer.setLeft(dashboard);
        widgetLayer.setRight(miniView.container());

        gameSceneContentLayer.setCenter(decorationPane);

        rootPane.getChildren().addAll(gameSceneContentLayer, widgetLayer, helpLayer, pausedIcon);
    }

    public void updateGameScene(boolean forceReload) {
        final Game game = ui.gameContext().game();
        final GameScene prevGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = ui.currentGameSceneConfig().selectGameScene(ui, game).orElseThrow();

        if (nextGameScene == prevGameScene && !forceReload) {
            return;
        }

        if (prevGameScene != null) {
            prevGameScene.end();
            Logger.info("Game scene ended: {}", prevGameScene.getClass().getSimpleName());
        }

        nextGameScene.onEmbeddedIntoUI(); // Must be called *before* embedding
        embedder.embedGameScene(ui, this, nextGameScene);
        nextGameScene.init();
        Logger.info("Game scene initialized: {}", nextGameScene.getClass().getSimpleName());

        // Handle switching between 2D and 3D play scene view
        game.optGameLevel().ifPresent(level -> {
            final GameSceneSwitchType sceneSwitchType = identifySceneSwitchType(prevGameScene, nextGameScene);
            switch (sceneSwitchType) {
                case FROM_2D_TO_3D -> sceneSwitcher.switchPlaySceneTo3D(ui, level, prevGameScene, nextGameScene);
                case FROM_3D_TO_2D -> sceneSwitcher.switchPlaySceneTo2D(prevGameScene, nextGameScene);
                case NONE -> {}
                default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
            }
        });

        gameSceneProperty().set(nextGameScene);
    }

    // Others

    private GameSceneSwitchType identifySceneSwitchType(GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case GameScene2D ignored when sceneAfter instanceof PlayScene3D -> GameSceneSwitchType.FROM_2D_TO_3D;
            case PlayScene3D ignored when sceneAfter instanceof GameScene2D -> GameSceneSwitchType.FROM_3D_TO_2D;
            case null, default -> GameSceneSwitchType.NONE; // may happen, it's ok
        };
    }

    private void addListeners() {
        gameScene.addListener(gameSceneChangeHandler);
        parentSceneFX.widthProperty() .addListener(parentSceneSizeChangeHandler);
        parentSceneFX.heightProperty().addListener(parentSceneSizeChangeHandler);
    }

    private void removeListeners() {
        gameScene.removeListener(gameSceneChangeHandler);
        parentSceneFX.widthProperty().removeListener(parentSceneSizeChangeHandler);
        parentSceneFX.heightProperty().removeListener(parentSceneSizeChangeHandler);
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
            gameSceneContentLayer.setBackground(debug ? paintBackground(Color.TEAL) : null);
            gameSceneContentLayer.setBorder(debug ? border(Color.LIGHTGREEN, 1) : null);
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

    public void updateRenderers(GameScene2D gameScene2D) {
        sceneRenderer = ui.currentConfig().createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
        hudRenderer   = ui.currentConfig().createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
    }
}