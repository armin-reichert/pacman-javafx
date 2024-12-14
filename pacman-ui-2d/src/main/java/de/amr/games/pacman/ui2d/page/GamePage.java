/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.*;
import de.amr.games.pacman.ui2d.PacManGames2dApp;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.SceneDisplayMode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.GlobalProperties.PY_AUTOPILOT;
import static de.amr.games.pacman.ui.GlobalProperties.PY_IMMUNITY;
import static de.amr.games.pacman.ui.Keyboard.*;
import static de.amr.games.pacman.ui.Ufx.*;

/**
 * @author Armin Reichert
 */
public class GamePage extends StackPane implements GameActionProvider {

    static final double MAX_SCENE_SCALING = 5;

    private final GameAction actionToggleDebugInfo = context -> Ufx.toggle(PacManGames2dApp.PY_DEBUG_INFO_VISIBLE);

    private final GameAction actionShowHelp = context -> showHelp();

    private final GameAction actionSimulationSlower = context -> {
        double newRate = context.gameClock().getTargetFrameRate() - 5;
        if (newRate > 0) {
            context.gameClock().setTargetFrameRate(newRate);
            context.showFlashMessageSec(0.75, newRate + "Hz");
        }
    };

    private final GameAction actionSimulationFaster = context -> {
        double newRate = context.gameClock().getTargetFrameRate() + 5;
        if (newRate > 0) {
            context.gameClock().setTargetFrameRate(newRate);
            context.showFlashMessageSec(0.75, newRate + "Hz");
        }
    };

    private final GameAction actionSimulationNormalSpeed = context -> {
        context.gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
        context.showFlashMessageSec(0.75, context.gameClock().getTargetFrameRate() + "Hz");
    };

    private final GameAction actionSimulationOneStep = context -> {
        if (context.gameClock().isPaused()) {
            context.gameClock().makeStep(true);
        }
    };

    private final GameAction actionSimulationTenSteps = context -> {
        if (context.gameClock().isPaused()) {
            context.gameClock().makeSteps(10, true);
        }
    };

    private final GameAction actionToggleAutopilot = context -> {
        toggle(PY_AUTOPILOT);
        boolean auto = PY_AUTOPILOT.get();
        context.showFlashMessage(context.locText(auto ? "autopilot_on" : "autopilot_off"));
        context.sound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
    };

    private final GameAction actionToggleDashboard = context -> toggleDashboard();

    private final GameAction actionToggleImmunity = context -> {
        toggle(PY_IMMUNITY);
        context.showFlashMessage(context.locText(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
        context.sound().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
    };

    private final GameAction actionOpenEditor = new GameAction() {
        @Override
        public void execute(GameContext context) {
            if (context.level().world() == null) {
                Logger.error("Map editor cannot be opened because no world is available");
                return;
            }
            context.currentGameScene().ifPresent(GameScene::end);
            context.sound().stopAll();
            context.gameClock().stop();
            EditorPage editorPage = getOrCreateEditorPage();
            editorPage.startEditor(context.level().world().map());
            context.selectPage(editorPage);
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return context.gameVariant() == GameVariant.PACMAN_XXL;
        }
    };

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            handleGameSceneChange(get());
        }
    };

    protected final GameContext context;
    protected final Scene parentScene;
    protected final Canvas canvas = new Canvas();
    protected final TooFancyGameCanvasContainer canvasContainer;

    protected final BorderPane canvasLayer = new BorderPane();
    protected final DashboardLayer dashboardLayer; // dashboard, picture-in-picture view
    protected final PopupLayer popupLayer; // help, signature

    protected final ContextMenu contextMenu = new ContextMenu();

    public GamePage(GameContext context, Scene parentScene) {
        this.context = checkNotNull(context);
        this.parentScene = checkNotNull(parentScene);

        bindGameActions();
        setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> contextMenu.hide());

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFontSmoothingType(FontSmoothingType.GRAY);
        g.setImageSmoothing(false);

        canvasContainer = new TooFancyGameCanvasContainer(canvas);
        canvasContainer.setMinScaling(0.5);
        // default: Arcade games aspect ratio
        canvasContainer.setUnscaledCanvasWidth(ARCADE_MAP_SIZE_IN_PIXELS.x());
        canvasContainer.setUnscaledCanvasHeight(ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasContainer.setBorderColor(Color.valueOf(Arcade.Palette.WHITE));
        canvasContainer.decorationEnabledPy.addListener((py, ov, nv) -> embedGameScene(gameScenePy.get()));

        canvasLayer.setCenter(canvasContainer);

        dashboardLayer = new DashboardLayer(context);
        InfoBox readMeBox = new InfoBoxReadmeFirst();
        readMeBox.setExpanded(true);
        dashboardLayer.addDashboardItem("Welcome to the Pleasuredome!", readMeBox);
        dashboardLayer.addDashboardItem(context.locText("infobox.general.title"), new InfoBoxGeneral());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_control.title"), new InfoBoxGameControl());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        dashboardLayer.addDashboardItem(context.locText("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboardLayer.addDashboardItem(
            /*context.locText("infobox.keyboard_shortcuts_tengen.title"*/
            "Joypad Settings", new InfoBoxJoypad());
        dashboardLayer.addDashboardItem(context.locText("infobox.about.title"), new InfoBoxAbout());

        popupLayer = new PopupLayer(context, canvasContainer);
        popupLayer.setMouseTransparent(true);
        popupLayer.sign(canvasContainer,
            context.assets().font("font.monospaced", 8), Color.LIGHTGRAY,
            context.locText("app.signature"));

        getChildren().addAll(canvasLayer, dashboardLayer, popupLayer);

        PacManGames2dApp.PY_CANVAS_FONT_SMOOTHING.addListener((py, ov, nv) -> g.setFontSmoothingType(nv ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        PacManGames2dApp.PY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, nv) -> g.setImageSmoothing(nv));
        PacManGames2dApp.PY_DEBUG_INFO_VISIBLE.addListener((py, ov, debug) -> {
            if (debug) {
                canvasLayer.setBackground(coloredBackground(Color.DARKGREEN));
                canvasLayer.setBorder(border(Color.LIGHTGREEN, 2));
            } else {
                canvasLayer.setBackground(null);
                canvasLayer.setBorder(null);
            }
        });
    }

    @Override
    public void bindGameActions() {
        bind(GameActions2D.BOOT,            KeyCode.F3);
        bind(GameActions2D.SHOW_START_PAGE, KeyCode.Q);
        bind(GameActions2D.TOGGLE_PAUSED,   KeyCode.P);
        bind(actionOpenEditor,              shift_alt(KeyCode.E));
        bind(actionToggleDebugInfo,         alt(KeyCode.D));
        bind(actionShowHelp,                KeyCode.H);
        bind(actionSimulationSlower,        alt(KeyCode.MINUS));
        bind(actionSimulationFaster,        alt(KeyCode.PLUS));
        bind(actionSimulationNormalSpeed,   alt(KeyCode.DIGIT0));
        bind(actionSimulationOneStep,       shift(KeyCode.P));
        bind(actionSimulationTenSteps,      shift(KeyCode.SPACE));
        bind(actionToggleAutopilot,         alt(KeyCode.A));
        bind(actionToggleDashboard,         naked(KeyCode.F1), alt(KeyCode.B));
        bind(actionToggleImmunity,          alt(KeyCode.I));
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public TooFancyGameCanvasContainer gameCanvasContainer() {
        return canvasContainer;
    }

    public void setSize(double width, double height) {
        canvasContainer.resizeTo(width, height);
    }

    @Override
    public void handleInput(GameContext context) {
        context.ifGameActionTriggeredRunItElse(this,
            () -> context.currentGameScene().ifPresent(gameScene -> gameScene.handleInput(context)));
    }

    private void handleContextMenuRequest(ContextMenuEvent event) {
        if (!context.currentGameSceneHasID("PlayScene2D")) {
            return;
        }
        contextMenu.getItems().clear();

        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            contextMenu.getItems().add(PacManGamesUI.contextMenuTitleItem(context.locText("scene_display")));
            // Switching scene display mode
            var miScaledToFit = new RadioMenuItem(context.locText("scaled_to_fit"));
            miScaledToFit.selectedProperty().addListener(
                (py,ov,nv) -> PacManGames2dApp.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(nv? SceneDisplayMode.SCALED_TO_FIT:SceneDisplayMode.SCROLLING));
            PacManGames2dApp.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, nv) -> miScaledToFit.setSelected(nv == SceneDisplayMode.SCALED_TO_FIT));
            contextMenu.getItems().add(miScaledToFit);

            var miScrolling = new RadioMenuItem(context.locText("scrolling"));
            miScrolling.selectedProperty().addListener(
                (py,ov,nv) -> PacManGames2dApp.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(nv? SceneDisplayMode.SCROLLING:SceneDisplayMode.SCALED_TO_FIT));
            PacManGames2dApp.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, nv) -> miScrolling.setSelected(nv == SceneDisplayMode.SCROLLING));
            contextMenu.getItems().add(miScrolling);

            ToggleGroup exclusion = new ToggleGroup();
            miScaledToFit.setToggleGroup(exclusion);
            miScrolling.setToggleGroup(exclusion);
            if (PacManGames2dApp.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get() == SceneDisplayMode.SCALED_TO_FIT) {
                miScaledToFit.setSelected(true);
            } else {
                miScrolling.setSelected(true);
            }
        }
        contextMenu.getItems().add(PacManGamesUI.contextMenuTitleItem(context.locText("pacman")));

        var miAutopilot = new CheckMenuItem(context.locText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.locText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(context.locText("muted"));
        miMuted.selectedProperty().bindBidirectional(context.sound().mutedProperty());
        contextMenu.getItems().add(miMuted);

        if (context.gameVariant() == GameVariant.PACMAN_XXL || context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(e -> actionOpenEditor.execute(context));
            contextMenu.getItems().add(miOpenMapEditor);
        }

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> GameActions2D.SHOW_START_PAGE.execute(context));
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
        event.consume();
    }

    protected void handleGameSceneChange(GameScene gameScene) {
        if (gameScene != null) {
            embedGameScene(gameScene);
        }
        contextMenu.hide();
    }

    public void embedGameScene(GameScene gameScene) {
        // new switch feature
        switch (gameScene) {
            case null -> Logger.error("No game scene to embed");
            case CameraControlledView cameraControlledView -> {
                getChildren().set(0, cameraControlledView.viewPort());
                cameraControlledView.viewPortWidthProperty().bind(parentScene.widthProperty());
                cameraControlledView.viewPortHeightProperty().bind(parentScene.heightProperty());
            }
            case GameScene2D gameScene2D -> {
                Vector2f sceneSize = gameScene2D.size();
                canvasContainer.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(Ufx::coloredBackground));
                canvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                canvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                gameScene2D.scalingProperty().bind(
                    canvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_SCALING)));
                GameRenderer renderer = context.currentGameSceneConfig().createRenderer(canvas);
                gameScene2D.setCanvas(canvas);
                gameScene2D.setGameRenderer(renderer);
                getChildren().set(0, canvasLayer);
            }
            default -> Logger.error("Cannot embed game scene of class {}", gameScene.getClass().getName());
        }
    }

    public void draw() {
        context.currentGameScene()
            .filter(GameScene2D.class::isInstance)
            .map(GameScene2D.class::cast)
            .ifPresent(GameScene2D::draw);
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    public void showSignature() {
        popupLayer.showSignature(1, 2, 1);
    }

    public void hideSignature() {
        popupLayer.hideSignature();
    }

    public void toggleDashboard() {
        dashboardLayer.toggleDashboardVisibility();
    }

    public void updateDashboard() {
        dashboardLayer.update();
    }

    public DashboardLayer dashboardLayer() {
        return dashboardLayer;
    }

    public void showHelp() {
        if (context.gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            if (isCurrentGameScene2D()) {
                popupLayer.showHelp(canvasContainer.scaling());
            }
        }
    }

    private EditorPage getOrCreateEditorPage() {
        throw new UnsupportedOperationException();
    }

    /*
    private EditorPage getOrCreateEditorPage() {
        if (editorPage == null) {
            editorPage = new EditorPage(stage, this, game().customMapDir());
            editorPage.setCloseAction(editor -> {
                editor.showSaveConfirmationDialog(editor::showSaveDialog, () -> stage.titleProperty().bind(stageTitleBinding()));
                editor.stop();
                clock.setTargetFrameRate(GameModel.TICKS_PER_SECOND);
                gameController().restart(GameState.BOOT);
                selectStartPage();
            });
        }
        return editorPage;
    }
*/

}