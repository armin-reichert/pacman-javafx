/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.*;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.CanvasDecorationPane;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameScene_Config.CommonSceneID;
import static de.amr.pacmanfx.ui.GameScene_Config.identifySceneSwitchType;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.uilib.Ufx.border;
import static de.amr.pacmanfx.uilib.Ufx.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView extends StackPane implements GameUI_View {

    private static final Dashboard.Style DASHBOARD_STYLE = new Dashboard.Style(
        Dashboard.DEFAULT_STYLE.labelWidth(),
        Dashboard.DEFAULT_STYLE.width(),
        Color.rgb(0, 0, 0x33),
        ArcadePalette.ARCADE_WHITE,
        Font.font("Sans", 12),
        Font.font("Sans", 12)
    );

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    private final ActionBindingsManager actionBindingsManager = new DefaultActionBindingsManager();
    private final Scene parentScene;
    private final Dashboard dashboard = new Dashboard(DASHBOARD_STYLE);
    private final CanvasDecorationPane canvasDecorator = new CanvasDecorationPane();
    private final MiniGameView miniView = new MiniGameView();
    private final BorderPane canvasLayer = new BorderPane();
    private final BorderPane widgetLayer = new BorderPane();
    private final HelpLayer helpLayer;
    private final GameUI_ContextMenu contextMenu;

    private GameUI ui;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    public PlayView(Scene parentScene) {
        this.parentScene = requireNonNull(parentScene);
        this.contextMenu = new GameUI_ContextMenu();
        this.helpLayer = new HelpLayer(canvasDecorator);

        canvasDecorator.setMinScaling(0.5);
        canvasDecorator.setUnscaledCanvasSize(ARCADE_MAP_SIZE_IN_PIXELS.x(), ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasDecorator.setBorderColor(ArcadePalette.ARCADE_WHITE);

        composeLayout();
        configureActionBindings();
        configurePropertyBindings();
        configureContextMenu();

        dashboard.setVisible(false);
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        contextMenu.setUI(ui);
        miniView.setUI(ui);
        ui.context().gameVariantNameProperty().addListener(
            (_, oldVariantName, newVariantName) -> handleGameVariantChange(oldVariantName, newVariantName));
    }

    private void handleGameVariantChange(String oldName, String newName) {
        if (oldName != null) {
            ui.context().gameByVariantName(oldName).removeGameEventListener(this);
            ui.configFactory().dispose(oldName);
        }
        if (newName != null) {
            final Game game = ui.context().gameByVariantName(newName);
            game.addGameEventListener(this);

            final GameUI_Config uiConfig = ui.config(newName);
            uiConfig.init();
            uiConfig.soundManager().muteProperty().bind(GameUI.PROPERTY_MUTED);

            final Image icon = uiConfig.assets().image("app_icon");
            if (icon != null) {
                ui.stage().getIcons().setAll(icon);
            } else {
                Logger.error("Could not find application icon for game variant {}", newName);
            }
        } else {
            Logger.error("No game selected");
        }
    }

    private void composeLayout() {
        widgetLayer.setLeft(dashboard);
        widgetLayer.setRight(miniView);
        canvasLayer.setCenter(canvasDecorator);
        getChildren().addAll(canvasLayer, widgetLayer, helpLayer);
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameScene;
    }

    public Optional<GameScene> optGameScene() {
        return Optional.ofNullable(currentGameScene.get());
    }

    public MiniGameView miniView() {
        return miniView;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public void showHelp(GameUI ui) {
        final double scaling = canvasDecorator.scalingProperty().get();
        helpLayer.showHelpPopup(ui, scaling, ui.context().gameVariantName());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI_View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsManager actionBindingsManager() {
        return actionBindingsManager;
    }

    @Override
    public void onKeyboardInput(GameUI ui) {
        actionBindingsManager.matchingAction(GameUI.KEYBOARD).ifPresentOrElse(
            action -> action.execute(ui),
            () -> optGameScene().ifPresent(GameScene::onKeyboardInput)
        );
    }

    @Override
    public void onEnter() {
        requestFocus();
        addListeners();
        canvasDecorator.updateLayout();
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
        final Game game = ui.context().currentGame();
        optGameScene().filter(GameScene2D.class::isInstance).map(GameScene2D.class::cast).ifPresent(gameScene2D -> {
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

    // GameEventListener

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        final Game game = gameEvent.game();
        final StateMachine.State<Game> gameState = game.control().state();
        switch (gameEvent.type()) {
            case LEVEL_CREATED -> {
                final GameLevel level = game.level();
                final GameUI_Config uiConfig = ui.currentConfig();

                //TODO this should be done elsewhere
                level.pac().setAnimationManager(uiConfig.createPacAnimations());
                level.ghosts().forEach(ghost -> ghost.setAnimationManager(uiConfig.createGhostAnimations(ghost.personality())));

                miniView.onLevelCreated(level);
                miniView.slideIn();
                // size of game scene might have changed, so re-embed
                optGameScene().ifPresent(gameScene -> embedGameScene(parentScene, gameScene));
            }
            case GAME_STATE_CHANGED -> {
                if (gameState.matches(GameControl.StateName.LEVEL_COMPLETE)) {
                    miniView.slideOut();
                }
            }
        }
        updateGameScene(ui.context().currentGame(), false);

        optGameScene().ifPresent(gameScene -> gameScene.onGameEvent(gameEvent));
    }

    /**
     * @param game the current game
     * @param forcedReload if {@code true} the game scene is (re-)embedded even if it doesn't change
     */
    public void updateGameScene(Game game, boolean forcedReload) {
        final GameScene currentGameScene = optGameScene().orElse(null);
        final GameScene intendedGameScene = ui.currentGameSceneConfig().selectGameScene(game).orElse(null);

        if (intendedGameScene == null) {
            ui.showFlashMessage(Duration.seconds(30), "Katastrophe! Could not determine game scene!");
            return;
        }

        if (!forcedReload && intendedGameScene == currentGameScene) {
            return;
        }

        if (currentGameScene != null) {
            currentGameScene.end(game);
            Logger.info("Game scene ended: {}", currentGameScene.getClass().getSimpleName());
            if (currentGameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoVisibleProperty().unbind();
            }
        }

        if (intendedGameScene instanceof GameScene2D gameScene2D) {
            gameScene2D.setContext(ui.context());
            gameScene2D.setUI(ui);
        }
        else if (intendedGameScene instanceof PlayScene3D playScene3D) {
            playScene3D.setContext(ui.context());
            playScene3D.setUI(ui);
        }

        embedGameScene(parentScene, intendedGameScene);
        if (intendedGameScene instanceof GameScene2D gameScene2D) {
            gameScene2D.debugInfoVisibleProperty().bind(GameUI.PROPERTY_DEBUG_INFO_VISIBLE);
        }
        intendedGameScene.init(game);
        Logger.info("Game scene initialized: {}", intendedGameScene.getClass().getSimpleName());

        // Handle switching between 2D and 3D scene variant (play scene)
        final byte sceneSwitchType = identifySceneSwitchType(currentGameScene, intendedGameScene);
        switch (sceneSwitchType) {
            case 23 -> intendedGameScene.onSwitch_2D_3D(currentGameScene);
            case 32 -> intendedGameScene.onSwitch_3D_2D(currentGameScene);
            case  0 -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }

        currentGameSceneProperty().set(intendedGameScene);
    }

    // Others

    private ChangeListener<GameScene> gameSceneChangeListener;
    private ChangeListener<? super Number> parentSceneWidthListener;
    private ChangeListener<? super Number> parentSceneHeightListener;

    private void addListeners() {
        removeListeners();

        gameSceneChangeListener = (_, _, gameScene) -> {
            contextMenu.hide();
            if (gameScene != null) {
                embedGameScene(parentScene, gameScene);
            }
        };
        currentGameSceneProperty().addListener(gameSceneChangeListener);

        parentSceneWidthListener = (_, _, w) -> canvasDecorator.resizeTo(w.doubleValue(), parentScene.getHeight());
        parentScene.widthProperty() .addListener(parentSceneWidthListener);

        parentSceneHeightListener = (_, _, h) -> canvasDecorator.resizeTo(parentScene.getWidth(), h.doubleValue());
        parentScene.heightProperty().addListener(parentSceneHeightListener);
    }

    private void removeListeners() {
        if (gameSceneChangeListener != null) {
            currentGameSceneProperty().removeListener(gameSceneChangeListener);
        }
        if (parentSceneWidthListener != null) {
            parentScene.widthProperty().removeListener(parentSceneWidthListener);
        }
        if (parentSceneHeightListener != null) {
            parentScene.heightProperty().removeListener(parentSceneHeightListener);
        }
    }

    private void configurePropertyBindings() {
        GameUI.PROPERTY_CANVAS_FONT_SMOOTHING.addListener((_, _, smooth) ->
            canvasDecorator.canvas().getGraphicsContext2D().setFontSmoothingType(
                smooth ? FontSmoothingType.LCD : FontSmoothingType.GRAY));

        GameUI.PROPERTY_DEBUG_INFO_VISIBLE.addListener((_, _, debug) -> {
            canvasLayer.setBackground(debug ? paintBackground(Color.TEAL) : null);
            canvasLayer.setBorder(debug ? border(Color.LIGHTGREEN, 1) : null);
        });

        widgetLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || GameUI.PROPERTY_MINI_VIEW_ON.get(),
            dashboard.visibleProperty(), GameUI.PROPERTY_MINI_VIEW_ON
        ));

        miniView.visibleProperty().bind(Bindings.createObjectBinding(
            () -> GameUI.PROPERTY_MINI_VIEW_ON.get() && ui.currentGameSceneHasID(CommonSceneID.PLAY_SCENE_3D),
            GameUI.PROPERTY_MINI_VIEW_ON, currentGameScene
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
        actionBindingsManager.useAnyBinding(ACTION_BOOT_SHOW_PLAY_VIEW, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_ENTER_FULLSCREEN, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_QUIT_GAME_SCENE, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SHOW_HELP, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SIMULATION_SLOWER, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SIMULATION_SLOWEST, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SIMULATION_FASTER, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SIMULATION_FASTEST, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SIMULATION_RESET, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SIMULATION_ONE_STEP, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_SIMULATION_TEN_STEPS, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_AUTOPILOT, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_DEBUG_INFO, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_MUTED, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_PAUSED, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_COLLISION_STRATEGY, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_DASHBOARD, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_IMMUNITY, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, GameUI.COMMON_BINDINGS);
        actionBindingsManager.useAnyBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D, GameUI.COMMON_BINDINGS);
    }

    private void handleContextMenuRequest(ContextMenuEvent event) {
        contextMenu.clear();
        optGameScene().ifPresent(gameScene -> {
            if (ui.currentGameSceneHasID(CommonSceneID.PLAY_SCENE_2D)) {
                contextMenu.addLocalizedTitleItem("scene_display");
                contextMenu.addLocalizedActionItem(ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_3D_scene");
            }
            gameScene.supplyContextMenu(ui.context().currentGame()).ifPresent(menu -> contextMenu.addAll(menu.itemsCopy()));
        });
        contextMenu.requestFocus();
        contextMenu.show(this, event.getScreenX(), event.getScreenY());
    }

    private void useDecoratedCanvas(GameUI ui, GameScene2D gameScene2D) {
        final Canvas canvas = new Canvas();

        canvasDecorator.setCanvas(canvas);

        gameScene2D.setCanvas(canvas);
        gameScene2D.backgroundProperty().bind(GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR);

        sceneRenderer = ui.currentConfig().createGameSceneRenderer(canvas, gameScene2D);
        hudRenderer = ui.currentConfig().createHUDRenderer(canvas, gameScene2D); // may return null!
    }

    private void embedGameScene(Scene parentScene, GameScene gameScene) {
        hudRenderer = null;
        if (gameScene.optSubScene().isPresent()) {
            // 1. Play scene with integrated sub-scene: 3D scene or 2D scene with camera as in Tengen Ms. Pac-Man:
            final SubScene subScene = gameScene.optSubScene().get();
            // Let sub-scene take full size of parent scene
            subScene.widthProperty().bind(parentScene.widthProperty());
            subScene.heightProperty().bind(parentScene.heightProperty());
            // Is it a 2D scene with canvas inside sub-scene with camera?
            if (gameScene instanceof GameScene2D gameScene2D) {
                useDecoratedCanvas(ui, gameScene2D);
            }
            getChildren().set(0, subScene);
        }
        else if (gameScene instanceof GameScene2D gameScene2D) {
            useDecoratedCanvas(ui, gameScene2D);
            Vector2i gameSceneSizePx = gameScene2D.unscaledSize();
            double aspect = (double) gameSceneSizePx.x() / gameSceneSizePx.y();
            if (ui.currentGameSceneConfig().sceneDecorationRequested(gameScene)) {
                // Decorated game scene scaled-down to give space for the decoration
                gameScene2D.scalingProperty().bind(canvasDecorator.scalingProperty().map(
                        scaling -> Math.min(scaling.doubleValue(), ui.preferences().getFloat("scene2d.max_scaling"))));
                canvasDecorator.setUnscaledCanvasSize(gameSceneSizePx.x(), gameSceneSizePx.y());
                canvasDecorator.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                canvasDecorator.backgroundProperty().bind(GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR.map(Ufx::paintBackground));
                canvasLayer.setCenter(canvasDecorator);
            }
            else {
                // Undecorated game scene taking complete height
                canvasDecorator.canvas().heightProperty().bind(parentScene.heightProperty());
                canvasDecorator.canvas().widthProperty().bind(parentScene.heightProperty().map(h -> h.doubleValue() * aspect));
                gameScene2D.scalingProperty().bind(parentScene.heightProperty().divide(gameSceneSizePx.y()));
                canvasLayer.setCenter(canvasDecorator.canvas());
            }
            getChildren().set(0, canvasLayer);
        }
        else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }
}