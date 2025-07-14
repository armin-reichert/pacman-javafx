/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.Perspective;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_ENTER_FULLSCREEN;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_MUTED;
import static java.util.Objects.requireNonNull;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public class PacManGames_UI_Impl implements GameUI {

    // package-visible to allow access to GameUI interface
    static PacManGames_UI_Impl THE_ONE;

    ObjectProperty<Color> pyCanvasBgColor = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty pyCanvasFontSmoothing = new SimpleBooleanProperty(false);
    BooleanProperty pyCanvasImageSmoothing = new SimpleBooleanProperty(false);
    BooleanProperty pyDebugInfoVisible = new SimpleBooleanProperty(false);
    IntegerProperty pyPipHeight = new SimpleIntegerProperty(400);
    BooleanProperty pyMiniViewOn = new SimpleBooleanProperty(false);
    IntegerProperty pyPipOpacityPercent = new SimpleIntegerProperty(69);
    IntegerProperty pySimulationSteps = new SimpleIntegerProperty(1);
    BooleanProperty py3DAxesVisible = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode> py3DDrawMode = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty py3DEnabled = new SimpleBooleanProperty(false);
    BooleanProperty py3DEnergizerExplodes = new SimpleBooleanProperty(true);
    ObjectProperty<Color> py3DFloorColor = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color> py3DLightColor = new SimpleObjectProperty<>(Color.WHITE);
    BooleanProperty py3DPacLightEnabled = new SimpleBooleanProperty(true);
    ObjectProperty<Perspective.ID> py3DPerspective = new SimpleObjectProperty<>(Perspective.ID.TRACK_PLAYER);
    DoubleProperty py3DWallHeight = new SimpleDoubleProperty(Settings3D.OBSTACLE_3D_BASE_HEIGHT);
    DoubleProperty py3DWallOpacity = new SimpleDoubleProperty(1.0);

    private final PacManGames_Assets theAssets;
    private final GameClock          theGameClock;
    private final GameContext        theGameContext;
    private final Keyboard           theKeyboard;
    private final Model3DRepository  theModel3DRepository;
    private final Joypad             theJoypad;
    private final Stage              theStage;
    private final DirectoryWatchdog  theWatchdog;

    private final Map<String, PacManGames_UIConfig> configByGameVariant = new HashMap<>();

    private final ObjectProperty<PacManGames_View> currentViewProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<GameScene> currentGameSceneProperty   = new SimpleObjectProperty<>();
    private final BooleanProperty mutedProperty                        = new SimpleBooleanProperty(false);

    private final StackPane rootPane = new StackPane();
    private final StartPagesView startPagesView;
    private final GameView gameView;
    private       EditorView editorView; // created on first access
    private final StatusIconBox iconBox;

    public PacManGames_UI_Impl(GameContext gameContext, Stage stage, double width, double height) {
        this.theGameContext = requireNonNull(gameContext);

        theAssets = new PacManGames_Assets();
        theGameClock = new GameClock();
        theKeyboard = new Keyboard();
        theJoypad = new Joypad(theKeyboard);
        theModel3DRepository = new Model3DRepository();
        theWatchdog = new DirectoryWatchdog(gameContext.theCustomMapDir());

        this.theStage = requireNonNull(stage);

        Scene mainScene = new Scene(rootPane, width, height);
        stage.setScene(mainScene);

        stage.setMinWidth(280);
        stage.setMinHeight(360);

        startPagesView = new StartPagesView(this);
        startPagesView.setBackground(theAssets().background("background.scene"));
        gameView = new GameView(this, gameContext, mainScene);

        rootPane.getChildren().add(startPagesView.rootNode());

        // "paused" icon appears on center of game view
        FontIcon iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
        iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == gameView && theGameClock.isPaused(),
            currentViewProperty, theGameClock.pausedProperty()));
        StackPane.setAlignment(iconPaused, Pos.CENTER);

        // status icon box appears at bottom-left corner of any view except editor
        iconBox = new StatusIconBox(this);
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);

        rootPane.getChildren().addAll(iconPaused, iconBox);

        theGameClock.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        theGameClock.setPermanentAction(this::drawGameView);

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, theKeyboard()::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, theKeyboard()::onKeyReleased);

        //TODO should I use key binding for global actions too?
        mainScene.setOnKeyPressed(e -> {
            if (KEY_FULLSCREEN.match(e)) {
                ACTION_ENTER_FULLSCREEN.execute(this);
            }
            else if (KEY_MUTE.match(e)) {
                ACTION_TOGGLE_MUTED.execute(this);
            }
            else if (KEY_OPEN_EDITOR.match(e)) {
                showEditorView();
            }
            else {
                currentView().handleKeyboardInput(gameContext);
            }
        });
        currentViewProperty.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));

        rootPane.backgroundProperty().bind(currentGameSceneProperty().map(gameScene ->
            currentGameSceneIsPlayScene3D()
                ? theAssets().get("background.play_scene3d")
                : theAssets().get("background.scene"))
        );
    }

    public void configure(Map<String, Class<? extends PacManGames_UIConfig>> configClassesMap) {
        configClassesMap.forEach((gameVariant, configClass) -> {
            try {
                PacManGames_UIConfig config = configClass.getDeclaredConstructor().newInstance();
                setUIConfig(gameVariant, config);
            } catch (Exception x) {
                Logger.error("Could not create UI configuration of class {}", configClass);
                throw new IllegalStateException(x);
            }
        });
        configByGameVariant.forEach((gameVariant, config) -> {
            config.createGameScenes(theGameContext);
            config.gameScenes().forEach(scene -> {
                if (scene instanceof GameScene2D gameScene2D) {
                    gameScene2D.debugInfoVisibleProperty().bind(pyDebugInfoVisible);
                }
            });
            Logger.info("Game scenes for game variant {} created", gameVariant);
        });
    }

    private void handleViewChange(PacManGames_View oldView, PacManGames_View newView) {
        requireNonNull(newView);
        if (oldView != null) {
            oldView.actionBindingMap().removeFromKeyboard();
            theGameContext.theGameEventManager().removeEventListener(oldView);
        }
        newView.actionBindingMap().updateKeyboard();
        newView.rootNode().requestFocus();
        theStage.titleProperty().bind(newView.title());
        theGameContext.theGameEventManager().addEventListener(newView);

        rootPane.getChildren().set(0, newView.rootNode());
    }

    /**
     * @param x what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Here.</a>
     */
    private void ka_tas_trooo_phe(Throwable x) {
        Logger.error(x);
        Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
        showFlashMessageSec(10, "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            theGameContext.theSimulationStep().start(theGameClock.tickCount());
            theGameContext.theGameController().updateGameState();
            theGameContext.theSimulationStep().log();
            currentGameScene().ifPresent(GameScene::update);
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private void drawGameView() {
        try {
            gameView.draw();
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private EditorView getEditorViewCreateIfNeeded() {
        if (editorView == null) {
            var editor = new TileMapEditor(theStage, theModel3DRepository);
            var miReturnToGame = new MenuItem(theAssets().text("back_to_game"));
            miReturnToGame.setOnAction(e -> {
                editor.stop();
                editor.executeWithCheckForUnsavedChanges(this::showStartView);
            });
            editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miReturnToGame);
            editor.init(theGameContext.theCustomMapDir());
            editorView = new EditorView(editor);
        }
        return editorView;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // ----------------------------------------------------------------------------------------------------

    public ObjectProperty<Color>          propertyCanvasBackgroundColor() { return pyCanvasBgColor; };
    public BooleanProperty                propertyCanvasFontSmoothing() { return pyCanvasFontSmoothing; };
    public BooleanProperty                propertyCanvasImageSmoothing(){ return pyCanvasImageSmoothing; }
    public BooleanProperty                propertyDebugInfoVisible(){ return pyDebugInfoVisible; }
    public IntegerProperty                propertyPipHeight(){ return pyPipHeight; }
    public BooleanProperty                propertyMiniViewOn(){ return pyMiniViewOn; }
    public IntegerProperty                propertyPipOpacityPercent(){ return pyPipOpacityPercent; }
    public IntegerProperty                propertySimulationSteps(){ return pySimulationSteps; }
    public BooleanProperty                property3DAxesVisible(){ return py3DAxesVisible; }
    public ObjectProperty<DrawMode>       property3DDrawMode(){ return py3DDrawMode; }
    public BooleanProperty                property3DEnabled(){ return py3DEnabled; }
    public BooleanProperty                property3DEnergizerExplodes(){ return py3DEnergizerExplodes; }
    public ObjectProperty<Color>          property3DFloorColor(){ return py3DFloorColor; }
    public ObjectProperty<Color>          property3DLightColor(){ return py3DLightColor; }
    public BooleanProperty                property3DPacLightEnabled(){ return py3DPacLightEnabled; }
    public ObjectProperty<Perspective.ID> property3DPerspective(){ return py3DPerspective; }
    public DoubleProperty                 property3DWallHeight(){ return py3DWallHeight; }
    public DoubleProperty                 property3DWallOpacity(){ return py3DWallOpacity; }

    @Override
    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameSceneProperty;
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(currentGameSceneProperty.get());
    }

    @Override
    public ObjectProperty<PacManGames_View> currentViewProperty() {
        return currentViewProperty;
    }

    @Override
    public PacManGames_View currentView() {
        return currentViewProperty.get();
    }

    @Override
    public GameView gameView() {
        return gameView;
    }

    @Override
    public Model3DRepository theModel3DRepository() {
        return theModel3DRepository;
    }

    @Override
    public BooleanProperty mutedProperty() {
        return mutedProperty;
    }

    @Override
    public void restart() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theGameClock.pausedProperty().set(false);
        theGameClock.start();
        theGameContext.theGameController().restart(GameState.BOOT);
    }

    @Override
    public void selectGameVariant(String gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }
        String previousVariant = theGameContext.theGameController().selectedGameVariant();
        if (previousVariant != null && !previousVariant.equals(gameVariant)) {
            PacManGames_UIConfig previousConfig = uiConfig(previousVariant);
            Logger.info("Unloading assets for game variant {}", previousVariant);
            previousConfig.destroy();
            previousConfig.soundManager().mutedProperty().unbind();
        }

        PacManGames_UIConfig newConfig = uiConfig(gameVariant);
        Logger.info("Loading assets for game variant {}", gameVariant);
        newConfig.storeAssets(theAssets());
        newConfig.soundManager().mutedProperty().bind(mutedProperty);

        Image appIcon = theAssets.image(newConfig.assetNamespace() + ".app_icon");
        if (appIcon != null) {
            theStage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariant);
        }

        gameView.canvasContainer().roundedBorderProperty().set(newConfig.hasGameCanvasRoundedBorder());

        // this triggers a game event and the event handlers:
        theGameContext.theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        currentViewProperty.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
        gameView.dashboard().init(this);

        iconBox.iconMuted().visibleProperty().bind(mutedProperty());
        iconBox.icon3D().visibleProperty().bind(py3DEnabled);
        iconBox.iconAutopilot().visibleProperty().bind(theGameContext().propertyUsingAutopilot());
        iconBox.iconImmune().visibleProperty().bind(theGameContext.propertyImmunity());

        theStage.centerOnScreen();
        theStage.show();
        theWatchdog.startWatching();
    }

    @Override
    public void showEditorView() {
        if (!theGameContext.theGame().isPlaying() || theGameClock.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            theGameClock.stop();
            getEditorViewCreateIfNeeded().editor().start(theStage);
            currentViewProperty.set(getEditorViewCreateIfNeeded());
        } else {
            Logger.info("Editor view cannot be opened, game is playing");
        }
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        gameView.flashMessageLayer().showMessage(String.format(message, args), seconds);
    }

    @Override
    public void showGameView() {
        currentViewProperty.set(gameView);
    }

    @Override
    public void showStartView() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        gameView.dashboard().setVisible(false);
        currentViewProperty.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
    }

    @Override
    public Stage theStage() {
        return theStage;
    }

    @Override
    public StartPagesView startPagesView() { return startPagesView; }

    @Override
    public void terminateApp() {
        Platform.exit();
    }

    @Override
    public PacManGames_Assets theAssets() {
        return theAssets;
    }

    @Override
    public GameClock theGameClock() {
        return theGameClock;
    }

    @Override
    public GameContext theGameContext() {
        return theGameContext;
    }

    @Override
    public Keyboard theKeyboard() {
        return theKeyboard;
    }

    @Override
    public Joypad theJoypad() {
        return theJoypad;
    }

    @Override
    public SoundManager theSound() {
        return theUIConfiguration().soundManager();
    }

    @Override
    public DirectoryWatchdog theWatchdog() {
        return theWatchdog;
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        gameView.updateGameScene(reloadCurrent);
    }

    // UI configuration

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param configuration the UI configuration for this variant
     */
    @Override
    public void setUIConfig(String variant, PacManGames_UIConfig configuration) {
        requireNonNull(variant);
        requireNonNull(configuration);
        configByGameVariant.put(variant, configuration);
    }

    @Override
    public PacManGames_UIConfig uiConfig(String gameVariant) {
        return configByGameVariant.get(gameVariant);
    }

    @Override
    public PacManGames_UIConfig theUIConfiguration() {
        return configByGameVariant.get(theGameContext.theGameController().selectedGameVariant());
    }

    @Override
    public boolean currentGameSceneIsPlayScene2D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && theUIConfiguration().gameSceneHasID(currentGameScene, "PlayScene2D");
    }

    @Override
    public boolean currentGameSceneIsPlayScene3D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && theUIConfiguration().gameSceneHasID(currentGameScene, "PlayScene3D");
    }
}