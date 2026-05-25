/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.Globals;
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

import static de.amr.pacmanfx.ui.GameSceneConfig.CommonSceneID;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.border;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView implements View {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final GameSceneDecorationPane.Config DECORATION_CONFIG = new GameSceneDecorationPane.Config(
        0.85f,
        0.93f,
        1.0f,
        20,
        20,
        new GameSceneDecorationPane.FrameConfig(
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
    private GameSceneDecorationPane decorationPane;
    private MiniGameView miniView;
    private BorderPane canvasLayer;
    private BorderPane widgetLayer;
    private HelpLayer helpLayer;
    private Dashboard dashboard;
    private FontAwesomeIcon pausedIcon;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    private final PlayViewGameEventHandler gameEventHandler = new PlayViewGameEventHandler(this);
    private final ChangeListener<GameScene> gameSceneChangeHandler;
    private final ChangeListener<? super Number> parentSceneSizeChangeHandler;

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
                embedGameScene(gameScene);
            }
        };
        parentSceneSizeChangeHandler = (_, _, _) -> decorationPane.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
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
        miniView.draw();
        // Dashboard must also be updated if simulation is stopped
        if (widgetLayer.isVisible()) {
            dashboard.update(ui);
        }
    }

    // ---

    private void createLayout(DashboardConfig dashboardConfig) {
        rootPane = new StackPane();
        decorationPane = new GameSceneDecorationPane(
            DECORATION_CONFIG,
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );
        miniView = new MiniGameView();
        canvasLayer = new BorderPane();
        helpLayer = new HelpLayer(canvasLayer);
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
        embedGameScene(nextGameScene);
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

    public void embedGameScene(GameScene gameScene) {
        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(gameScene, gameScene.optSubSceneFX().get());
        }
        else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2D(gameScene2D);
        }
        else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(GameScene gameScene, SubScene subSceneFX) {

        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(parentSceneFX.widthProperty());
        subSceneFX.heightProperty().bind(parentSceneFX.heightProperty());

        if (gameScene instanceof GameScene2D gameScene2D) {
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            gameScene2D.setCanvas(decorationPane.canvas());
            updateRenderers(gameScene2D);
        }
        rootPane.getChildren().set(0, subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(GameScene2D gameScene2D) {
        final boolean decorated = ui.currentGameSceneConfig().sceneDecorationRequested(gameScene2D);

        if (decorated) {

            // set unscaled decoration pane size to game scene (=world map) size
            decorationPane.unscaledWidthProperty().bind(gameScene2D.unscaledWidthProperty());
            decorationPane.unscaledHeightProperty().bind(gameScene2D.unscaledHeightProperty());

            // scale decoration pane to available scene space
            decorationPane.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());

            // bind background color for canvas and decoration pane
            gameScene2D.backgroundColorProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR);
            decorationPane.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(UfxBackgrounds::paintBackground));

            // Limit scaling
            gameScene2D.scalingProperty().bind(decorationPane.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), MAX_GAME_SCENE_SCALING)));

            decorationPane.newCanvas(); //TODO check why creating a new canvas is needed
            gameScene2D.setCanvas(decorationPane.canvas());
            updateRenderers(gameScene2D);

            canvasLayer.setCenter(decorationPane);
        }
        else {
            // Undecorated game scene taking complete height

            final Canvas canvas = decorationPane.canvas();
            final double aspect = gameScene2D.getAspectRatio();

            canvas.heightProperty().bind(parentSceneFX.heightProperty());
            canvas.widthProperty().bind(parentSceneFX.heightProperty().map(h -> h.doubleValue() * aspect));

            gameScene2D.scalingProperty().bind(parentSceneFX.heightProperty().divide(gameScene2D.getUnscaledHeight()));

            // Game scene renderer can only be created if canvas is available
            gameScene2D.setCanvas(canvas);
            updateRenderers(gameScene2D);

            canvasLayer.setCenter(canvas);
        }

        rootPane.getChildren().set(0, canvasLayer);
    }

    private void updateRenderers(GameScene2D gameScene2D) {
        sceneRenderer = ui.currentConfig().createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
        hudRenderer   = ui.currentConfig().createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
    }

    // 2D-3D switch Gedöns

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