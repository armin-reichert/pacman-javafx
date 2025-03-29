/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.*;
import de.amr.games.pacman.ui.input.GameKeyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.uilib.Ufx.createIcon;

/**
 * User interface for all Pac-Man game variants (2D only).
 *
 * @author Armin Reichert
 */
public class PacManGamesUI implements GameUI {

    protected final GameAction actionOpenEditorView = new GameAction() {
        @Override
        public void execute() {
            currentGameScene().ifPresent(GameScene::end);
            clock.stop();
            sound.stopAll();
            EditorView editorView = getOrCreateEditorView();
            stage.titleProperty().bind(editorView.editor().titleProperty());
            editorView.editor().start();
            viewPy.set(editorView);
        }

        @Override
        public boolean isEnabled() {
            return !THE_GAME_CONTROLLER.game().isPlaying();
        }
    };

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>();

    protected final ObjectProperty<View> viewPy = new SimpleObjectProperty<>();

    protected final GameAssets assets = new GameAssets();
    protected final GameClockFX clock = new GameClockFX();
    protected final GameKeyboard keyboard = new GameKeyboard();
    protected final GameSound sound = new GameSound();
    protected final UIConfigurationManager uiConfigurationManager = new UIConfigurationManager();

    protected Stage stage;
    protected Scene mainScene;
    protected final StackPane sceneRoot = new StackPane();

    protected StartPagesCarousel startPagesCarousel;
    protected GameView gameView;
    protected EditorView editorView;

    protected boolean scoreVisible;

    public PacManGamesUI() {
        clock.setPauseableCallback(this::runOnEveryTickExceptWhenPaused);
        clock.setPermanentCallback(this::runOnEveryTick);
        viewPy.addListener((py, oldView, newView) -> {
            if (oldView != null) {
                oldView.disableActionBindings();
                if (oldView instanceof GameEventListener listener) {
                    THE_GAME_CONTROLLER.game().removeGameEventListener(listener);
                }
            }
            newView.enableActionBindings();
            sceneRoot.getChildren().set(0, newView.node());
            newView.node().requestFocus();
            if (newView instanceof GameEventListener listener) {
                THE_GAME_CONTROLLER.game().addGameEventListener(listener);
            }
        });
    }

    /**
     * Called from application start method (on JavaFX application thread).
     *
     * @param stage primary stage (window)
     * @param initialSize initial UI size
     */
    @Override
    public void build(Stage stage, Dimension2D initialSize) {
        this.stage = assertNotNull(stage);
        createMainScene(assertNotNull(initialSize));
        createStartPagesCarousel();
        createGameView(mainScene);
        bindStageTitle();
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.setScene(mainScene);
        stage.centerOnScreen();
        stage.setOnShowing(e -> showStartView());
        init(THE_GAME_CONTROLLER.selectedGameVariant());
    }

    @Override
    public void addStartPage(GameVariant gameVariant, StartPage startPage) {
        startPagesCarousel.addStartPage(gameVariant, startPage);
    }

    @Override
    public void addDefaultDashboardItems(String... ids) {
        gameView.dashboard().addDefaultInfoBoxes(ids);
    }

    @Override
    public void show() {
        stage.show();
    }

    protected void runOnEveryTickExceptWhenPaused() {
        try {
            THE_GAME_CONTROLLER.update();
            currentGameScene().ifPresent(GameScene::update);
            THE_GAME_CONTROLLER.game().eventLog().print(clock.tickCount());
        } catch (Exception x) {
            clock.stop();
            Logger.error(x);
            Logger.error("Something very bad happened, game clock has been stopped!");
        }
    }

    protected void runOnEveryTick() {
        try {
            //TODO this code should probably move into GameView
            if (viewPy.get() == gameView) {
                gameView.onTick();
            } else {
                Logger.warn("Should not happen: tick received when not on game view");
            }
        } catch (Exception x) {
            clock.stop();
            Logger.error(x);
            Logger.error("Something very bad happened, game clock has been stopped!");
        }
    }

    //TODO use nice font icons instead
    private Pane createIconPane() {
        ImageView mutedIcon = createIcon(assets.get("icon.mute"), 48, sound.mutedProperty());
        ImageView autoIcon  = createIcon(assets.get("icon.auto"), 48, GlobalProperties2d.PY_AUTOPILOT);

        var pane = new HBox(autoIcon, mutedIcon);
        pane.setMaxWidth(128);
        pane.setMaxHeight(64);
        pane.visibleProperty().bind(Bindings.createBooleanBinding(() -> viewPy.get() != editorView, viewPy));
        return pane;
    }

    // icons indicating autopilot, mute state
    private void addIconPane() {
        Pane iconPane = createIconPane();
        ImageView pauseIcon = createIcon(assets.get("icon.pause"), 64, clock.pausedProperty());
        pauseIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> viewPy.get() != editorView && clock.isPaused(), viewPy, clock.pausedProperty()));
        StackPane.setAlignment(pauseIcon, Pos.CENTER);
        StackPane.setAlignment(iconPane, Pos.BOTTOM_RIGHT);
        sceneRoot.getChildren().addAll(pauseIcon, iconPane);
    }

    protected void createMainScene(Dimension2D size) {
        sceneRoot.getChildren().addAll(new Pane());
        sceneRoot.setBackground(assets.get("background.scene"));
        sceneRoot.backgroundProperty().bind(gameScenePy.map(
            gameScene -> uiConfigurationManager.currentGameSceneIsPlayScene3D()
                ? assets.get("background.play_scene3d")
                : assets.get("background.scene"))
        );
        addIconPane();

        mainScene = new Scene(sceneRoot, size.getWidth(), size.getHeight());
        mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

        mainScene.setOnKeyPressed(keyPress -> {
            if (GameKeyboard.KEY_FULLSCREEN.match(keyPress)) {
                enterFullScreenMode();
            }
            else if (GameKeyboard.KEY_MUTE.match(keyPress)) {
                sound.toggleMuted();
            }
            else if (GameKeyboard.KEY_OPEN_EDITOR.match(keyPress)) {
                openEditor();
            }
            else if (viewPy.get() instanceof GameActionProvider actionProvider) {
                actionProvider.handleInput();
            }
        });
    }

    private EditorView getOrCreateEditorView() {
        if (editorView == null) {
            editorView = new EditorView(stage);
            editorView.setCloseAction(editor -> {
                editor.executeWithCheckForUnsavedChanges(this::bindStageTitle);
                editor.stop();
                clock.setTargetFrameRate(Globals.TICKS_PER_SECOND);
                THE_GAME_CONTROLLER.restart(GameState.BOOT);
                showStartView();
            });
        }
        return editorView;
    }

    protected void createStartPagesCarousel() {
        startPagesCarousel = new StartPagesCarousel();
        startPagesCarousel.setBackground(assets.background("background.scene"));
        viewPy.addListener((py, ov, view) -> {
            if (view == startPagesCarousel) {
                startPagesCarousel.currentStartPage().ifPresent(startPage ->
                    startPage.onSelected(THE_GAME_CONTROLLER.selectedGameVariant()));
            }
        });
    }

    protected void createGameView(Scene parentScene) {
        gameView = new GameView(parentScene);
        gameView.gameSceneProperty().bind(gameScenePy);
        gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
    }

    protected void bindStageTitle() {
        stage.titleProperty().bind(Bindings.createStringBinding(
            () -> {
                // "app.title.pacman" vs. "app.title.pacman.paused"
                String key = "app.title." + uiConfigurationManager.current().assetNamespace();
                if (clock.isPaused()) { key += ".paused"; }
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return assets.text(key, "2D") + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return assets.text(key, "2D");
            },
            clock.pausedProperty(), gameScenePy, gameView.heightProperty())
        );
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        final GameScene nextGameScene = uiConfigurationManager.current().selectGameScene();
        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }
        final GameScene currentGameScene = gameScenePy.get();
        final boolean changing = nextGameScene != currentGameScene;
        if (!changing && !reloadCurrent) {
            return;
        }
        if (currentGameScene != null) {
            currentGameScene.end();
            Logger.info("Game scene ended: {}", currentGameScene.displayName());
        }
        gameView.embedGameScene(nextGameScene);
        nextGameScene.init();
        if (uiConfigurationManager.current().is2D3DPlaySceneSwitch(currentGameScene, nextGameScene)) {
            nextGameScene.onSceneVariantSwitch(currentGameScene);
        }
        if (changing) {
            gameScenePy.set(nextGameScene);
            Logger.info("Game scene is now: {}", nextGameScene.displayName());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public GameAssets assets() {
        return assets;
    }

    @Override
    public GameClockFX clock() {
        return clock;
    }

    @Override
    public UIConfigurationManager configurations() {
        return uiConfigurationManager;
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
    }

    @Override
    public void enterFullScreenMode() {
        stage.setFullScreen(true);
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public GameView gameView() {
        return gameView;
    }

    @Override
    public ReadOnlyDoubleProperty heightProperty() {
        return mainScene.heightProperty();
    }

    @Override
    public void init(GameVariant gameVariant) {
        Logger.info("Init UI for game variant {}...", gameVariant);
        THE_GAME_CONTROLLER.selectGameVariant(gameVariant);
        onGameVariantChange(gameVariant);
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public GameKeyboard keyboard() {
        return keyboard;
    }

    @Override
    public void onGameVariantChange(GameVariant gameVariant) {
        GameUIConfiguration uiConfig = uiConfigurationManager.configuration(gameVariant);
        sound.selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
    }

    @Override
    public void openEditor() {
        if (actionOpenEditorView.isEnabled()) {
            actionOpenEditorView.execute();
        }
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisible = visible;
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        gameView.flashMessageOverlay().showMessage(String.format(message, args), seconds);
    }

    @Override
    public void showGameView() {
        viewPy.set(gameView);
        if (!THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)) {
            sound.playVoice("voice.explain", 0);
        }
        gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
        GameActions2D.BOOT.execute();
    }

    @Override
    public void showStartView() {
        clock.stop();
        gameScenePy.set(null);
        gameView.setDashboardVisible(false);
        viewPy.set(startPagesCarousel);
        //TODO this is needed for XXL option menu
        startPagesCarousel.currentStartPage().ifPresent(startPage -> startPage.onSelected(THE_GAME_CONTROLLER.selectedGameVariant()));
    }

    @Override
    public GameSound sound() {
        return sound;
    }

    @Override
    public void togglePlayScene2D3D() {}
}