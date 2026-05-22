/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.UIConfig;
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
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.CanvasDecorationPane;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.AbstractGameSceneConfig.identifySceneSwitchType;
import static de.amr.pacmanfx.ui.GameSceneConfig.CommonSceneID;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.layout.ContextMenuSupport.addLocalizedActionItem;
import static de.amr.pacmanfx.ui.layout.ContextMenuSupport.addLocalizedTitleItem;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.border;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView extends StackPane implements View {

    private static final FontIcon PAUSED_ICON = FontIcon.of(FontAwesomeSolid.PAUSE, 80, ArcadePalette.ARCADE_WHITE);

    private final GameEventHandler gameEventHandler = new GameEventHandler(this);

    private final ObjectProperty<GameScene> gameScene = new SimpleObjectProperty<>();

    private final GameUI ui;
    private final Scene parentScene;
    private final CanvasDecorationPane canvasDecorationPane = new CanvasDecorationPane();
    private final MiniGameView miniView = new MiniGameView();
    private final BorderPane canvasLayer = new BorderPane();
    private final BorderPane widgetLayer = new BorderPane();
    private final HelpLayer helpLayer;
    private final ContextMenu contextMenu;

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
        this.contextMenu = new ContextMenu();
        this.helpLayer = new HelpLayer(canvasDecorationPane);

        createDashboard(dashboardConfig);
        configureCanvasDecorationPane();
        composeLayout();
        configureActionBindings();
        configurePropertyBindings();
        configureContextMenu();

        miniView.setUI(ui);
        ui.gameContext().gameVariantNameProperty().addListener(
            (_, oldVariantName, newVariantName) -> handleGameVariantNameChange(oldVariantName, newVariantName));
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
        final double scaling = canvasDecorationPane.scalingProperty().get();
        helpLayer.showHelpPopup(ui, scaling, ui.gameContext().gameVariantName());
    }

    public void forceGameSceneUpdate() {
        updateGameScene(true);
    }

    public void updateGameScene() {
        updateGameScene(false);
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
        requestFocus();
        addListeners();
        canvasDecorationPane.updateLayout();
    }

    @Override
    public void onExit() {
        removeListeners();
    }

    @Override
    public StackPane root() {
        return this;
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

    // GameEvent handling

    private static class GameEventHandler extends DefaultGameEventListener {

        private final PlayView playView;

        public GameEventHandler(PlayView playView) {
            this.playView = playView;
        }

        @Override
        public void onGameEvent(GameEvent gameEvent) {
            switch (gameEvent) {

                case LevelCreatedEvent levelCreatedEvent -> {
                    final GameLevel level = levelCreatedEvent.level();
                    final UIConfig uiConfig = playView.ui.currentConfig();

                    //TODO this should be done elsewhere
                    level.pac().setAnimationManager(uiConfig.createPacAnimations(playView.ui.spriteAnimator()));
                    level.ghosts().forEach(ghost ->
                        ghost.setAnimationManager(uiConfig.createGhostAnimations(playView.ui.spriteAnimator(), ghost.personality())));

                    playView.miniView.setGameLevel(level);
                    playView.miniView.slideIn();
                    // size of game scene might have changed, so re-embed
                    playView.optCurrentGameScene().ifPresent(gameScene -> playView.embedGameScene(playView.parentScene, gameScene));
                }

                case GameStateChangeEvent stateChangeEvent -> {
                    if (stateChangeEvent.newState().matchesByName(CanonicalGameState.LEVEL_COMPLETE.name())) {
                        playView.miniView.slideOut();
                    }
                }

                default -> {}
            }

            playView.updateGameScene();

            // Call game event handler for current game scene
            playView.optCurrentGameScene().ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
        }
    }

    // ---

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

    private void addListeners() {
        removeListeners();

        gameSceneChangeListener = (_, _, gameScene) -> {
            contextMenu.hide();
            if (gameScene != null) {
                embedGameScene(parentScene, gameScene);
            }
        };
        gameSceneProperty().addListener(gameSceneChangeListener);

        parentSceneWidthListener = (_, _, w) -> canvasDecorationPane.resizeTo(w.doubleValue(), parentScene.getHeight());
        parentScene.widthProperty() .addListener(parentSceneWidthListener);

        parentSceneHeightListener = (_, _, h) -> canvasDecorationPane.resizeTo(parentScene.getWidth(), h.doubleValue());
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

    private void handleGameVariantNameChange(String oldGameVariantName, String newGameVariantName) {
        if (oldGameVariantName != null) {
            Logger.info("Cleanup game variant {}...", oldGameVariantName);
            final Game game = ui.gameContext().gameByVariantName(oldGameVariantName);
            game.flow().removeGameEventListener(gameEventHandler);
            ui.uiConfigManager().dispose(oldGameVariantName);
            ui.soundManager().dispose();
            ui.stage().getIcons().removeAll();
            Logger.info("Cleanup of game variant {} complete.", oldGameVariantName);
        }
        if (newGameVariantName != null) {
            Logger.info("Initialize game variant {}...", newGameVariantName);
            final Game game = ui.gameContext().gameByVariantName(newGameVariantName);
            game.flow().addGameEventListener(gameEventHandler);

            final UIConfig uiConfig = ui.config(newGameVariantName);
            uiConfig.init(ui);

            final Image icon = uiConfig.assets().image("app_icon");
            if (icon != null) {
                ui.stage().getIcons().setAll(icon);
            } else {
                Logger.error("Could not find application icon for game variant {}", newGameVariantName);
            }
            Logger.info("Initialization of game variant {} complete.", newGameVariantName);
        } else {
            Logger.error("No game selected");
        }
    }

    private void createDashboard(DashboardConfig dashboardConfig) {
        dashboard = new Dashboard(dashboardConfig);
        dashboard.setVisible(false);
    }

    private void configureCanvasDecorationPane() {
        canvasDecorationPane.setMinScaling(0.5);
        canvasDecorationPane.setUnscaledCanvasSize(ARCADE_MAP_SIZE_IN_PIXELS.x(), ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasDecorationPane.setBorderColor(ArcadePalette.ARCADE_WHITE);
    }

    private void composeLayout() {
        StackPane.setAlignment(PAUSED_ICON, Pos.CENTER);
        widgetLayer.setLeft(dashboard);
        widgetLayer.setRight(miniView.container());
        canvasLayer.setCenter(canvasDecorationPane);
        getChildren().addAll(canvasLayer, widgetLayer, helpLayer, PAUSED_ICON);
    }

    private void configurePropertyBindings() {
        PAUSED_ICON.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ui.gameContext().clock().getUpdatesDisabled(),
            ui.gameContext().clock().updatesDisabledProperty())
        );

        GameUIConstants.PROPERTY_CANVAS_FONT_SMOOTHING.addListener((_, _, smooth) ->
            canvasDecorationPane.canvas().getGraphicsContext2D().setFontSmoothingType(
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

    private void configureContextMenu() {
        setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is there a better way to hide the context menu?
        parentScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });
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

    private void handleContextMenuRequest(ContextMenuEvent event) {
        contextMenu.getItems().clear();
        optCurrentGameScene().ifPresent(gameScene -> {
            if (ui.currentGameSceneHasID(CommonSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(contextMenu, ui,"scene_display");
                addLocalizedActionItem(contextMenu, ui,ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_3D_scene");
            }
            gameScene.supplyContextMenu().ifPresent(sceneMenu -> contextMenu.getItems().addAll(sceneMenu.getItems()));
        });
        if (!contextMenu.getItems().isEmpty()) {
            contextMenu.requestFocus();
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
        }
    }

    private void useDecoratedCanvas(GameScene2D gameScene2D) {
        final Canvas canvas = new Canvas();
        canvasDecorationPane.setCanvas(canvas);

        gameScene2D.setCanvas(canvas);
        gameScene2D.backgroundProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR);

        sceneRenderer = ui.currentConfig().createGameSceneRenderer(gameScene2D, canvas);
        hudRenderer   = ui.currentConfig().createHUDRenderer(gameScene2D, canvas); // may return null!
    }

    //TODO simplify
    private void embedGameScene(Scene parentSceneFX, GameScene gameScene) {
        hudRenderer = null;
        if (gameScene.optSubScene().isPresent()) {
            // 1. Play scene with integrated sub-scene: 3D scene or 2D scene with camera as in Tengen Ms. Pac-Man:
            final SubScene subScene = gameScene.optSubScene().get();
            // Let sub-scene take full size of parent scene
            subScene.widthProperty().bind(parentSceneFX.widthProperty());
            subScene.heightProperty().bind(parentSceneFX.heightProperty());
            // Is it a 2D scene with canvas inside sub-scene with camera?
            if (gameScene instanceof GameScene2D gameScene2D) {
                useDecoratedCanvas(gameScene2D);
            }
            getChildren().set(0, subScene);
        }
        else if (gameScene instanceof GameScene2D gameScene2D) {
            useDecoratedCanvas(gameScene2D);
            Vector2i gameSceneSizePx = gameScene2D.unscaledSceneSize();
            double aspect = (double) gameSceneSizePx.x() / gameSceneSizePx.y();
            if (ui.currentGameSceneConfig().sceneDecorationRequested(gameScene)) {
                final float maxScaling = GameScene2D.MAX_SCALING;
                // Decorated game scene scaled-down to give space for the decoration
                gameScene2D.scalingProperty().bind(canvasDecorationPane.scalingProperty().map(
                        scaling -> Math.min(scaling.doubleValue(), maxScaling)));
                canvasDecorationPane.setUnscaledCanvasSize(gameSceneSizePx.x(), gameSceneSizePx.y());
                canvasDecorationPane.resizeTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
                canvasDecorationPane.backgroundProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR.map(UfxBackgrounds::paintBackground));
                canvasLayer.setCenter(canvasDecorationPane);
            }
            else {
                // Undecorated game scene taking complete height
                canvasDecorationPane.canvas().heightProperty().bind(parentSceneFX.heightProperty());
                canvasDecorationPane.canvas().widthProperty().bind(parentSceneFX.heightProperty().map(h -> h.doubleValue() * aspect));
                gameScene2D.scalingProperty().bind(parentSceneFX.heightProperty().divide(gameSceneSizePx.y()));
                canvasLayer.setCenter(canvasDecorationPane.canvas());
            }
            getChildren().set(0, canvasLayer);
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

        // Pause simulation while switching
        ui.gameContext().clock().setUpdatesDisabled(true);

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

        ui.gameContext().clock().setUpdatesDisabled(false);
        Logger.info("3D scene {} entered from 3D scene {}", playScene3D.getClass().getSimpleName(), currentScene.getClass().getSimpleName());

        playScene3D.fadeInAnimation().playFromStart();
    }

    private void switchPlaySceneTo2D(GameScene currentScene, GameScene nextScene) {
        if (!(nextScene instanceof GameScene2D playScene2D)) {
            throw new IllegalArgumentException("Expected GameScene2D, but scene has class %s"
                .formatted(nextScene.getClass().getSimpleName()));
        }
        // Pause simulation during scene switching
        ui.gameContext().clock().setUpdatesDisabled(true);
        playScene2D.onEnteredFrom3DScene();
        ui.gameContext().clock().setUpdatesDisabled(false);
        Logger.info("2D scene {} entered from 3D scene {}", playScene2D.getClass().getSimpleName(), currentScene.getClass().getSimpleName());
    }
}