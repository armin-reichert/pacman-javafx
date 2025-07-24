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
import de.amr.pacmanfx.ui._2d.ArcadePalette;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_ENTER_FULLSCREEN;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_MUTED;
import static java.util.Objects.requireNonNull;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public class PacManGames_UI_Impl implements GameUI {

    // package-visible to allow access to GameUI interface
    static PacManGames_UI_Impl THE_ONE;

    final ObjectProperty<Color> propertyCanvasBgColor          = new SimpleObjectProperty<>(Color.BLACK);
    final BooleanProperty propertyCanvasFontSmoothing          = new SimpleBooleanProperty(false);
    final BooleanProperty propertyCanvasImageSmoothing         = new SimpleBooleanProperty(false);
    final BooleanProperty propertyDebugInfoVisible             = new SimpleBooleanProperty(false);
    final IntegerProperty propertyMiniViewHeight               = new SimpleIntegerProperty(400);
    final BooleanProperty propertyMiniViewOn                   = new SimpleBooleanProperty(false);
    final IntegerProperty propertyPipOpacityPercent            = new SimpleIntegerProperty(69);
    final IntegerProperty propertySimulationSteps              = new SimpleIntegerProperty(1);
    final BooleanProperty property3DAxesVisible                = new SimpleBooleanProperty(false);
    final ObjectProperty<DrawMode> property3DDrawMode          = new SimpleObjectProperty<>(DrawMode.FILL);
    final BooleanProperty property3DEnabled                    = new SimpleBooleanProperty(false);
    final BooleanProperty property3DEnergizerExplodes          = new SimpleBooleanProperty(true);
    final ObjectProperty<Color> property3DFloorColor           = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    final ObjectProperty<Color> property3DLightColor           = new SimpleObjectProperty<>(Color.WHITE);
    final BooleanProperty property3DPacLightEnabled            = new SimpleBooleanProperty(true);
    final ObjectProperty<PerspectiveID> property3DPerspective = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    final DoubleProperty property3DWallHeight                  = new SimpleDoubleProperty();
    final DoubleProperty property3DWallOpacity                 = new SimpleDoubleProperty(1.0);

    // private properties
    private final ObjectProperty<PacManGames_View> propertyCurrentView = new SimpleObjectProperty<>();
    private final ObjectProperty<GameScene> propertyCurrentGameScene = new SimpleObjectProperty<>();
    private final BooleanProperty propertyMuted = new SimpleBooleanProperty(false);

    private final PacManGames_Assets theAssets;
    private final DirectoryWatchdog  theCustomDirWatchdog;
    private final GameClock          theGameClock;
    private final GameContext        theGameContext;
    private final Keyboard           theKeyboard;
    private final Joypad             theJoypad;
    private final PreferenceManager  thePrefs;
    private final Stage              theStage;

    private final Map<String, GameUI_Config> configByGameVariant = new HashMap<>();

    private final StackPane rootPane = new StackPane();
    private final StartPagesView startPagesView;
    private final PlayView playView;
    private       EditorView editorView; // created on first access

    public PacManGames_UI_Impl(GameContext gameContext, Stage stage, double width, double height) {
        theGameContext = requireNonNull(gameContext);
        theStage = requireNonNull(stage);
        theAssets = new PacManGames_Assets();
        theGameClock = new GameClock();
        theKeyboard = new Keyboard();
        theJoypad = new Joypad(theKeyboard);
        thePrefs = new PreferenceManager(PacManGames_UI_Impl.class);
        theCustomDirWatchdog = new DirectoryWatchdog(gameContext.theCustomMapDir());

        storePreferenceDefaultValues();
        if (!thePrefs.isAccessible()) {
            Logger.error("User preferences could not be accessed, using default values!");
        } else {
            thePrefs.addMissingValues();
        }

        Scene mainScene = new Scene(rootPane, width, height);
        stage.setScene(mainScene);

        stage.setMinWidth(280);
        stage.setMinHeight(360);

        startPagesView = new StartPagesView(this);
        playView = new PlayView(this, gameContext, mainScene);

        rootPane.getChildren().add(startPagesView.rootNode());

        // "paused" icon appears at center of UI
        FontIcon pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == playView && theGameClock.isPaused(),
                propertyCurrentView, theGameClock.pausedProperty()));

        // status icon box appears at bottom-left corner of any view except editor
        var iconBox = new StatusIconBox(this);
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);
        iconBox.visibleProperty().bind(propertyCurrentView.map(view -> view != editorView));

        rootPane.getChildren().addAll(pausedIcon, iconBox);

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
                currentView().handleKeyboardInput(this);
            }
        });
        propertyCurrentView.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));

        rootPane.backgroundProperty().bind(propertyCurrentGameScene().map(gameScene ->
            currentGameSceneIsPlayScene3D()
                ? theAssets().get("background.play_scene3d")
                : theAssets().get("background.scene"))
        );
    }

    private void storePreferenceDefaultValues() {
        thePrefs.storeDefaultValue("3d.bonus.symbol.width", 8.0f);
        thePrefs.storeDefaultValue("3d.bonus.points.width", 1.8f * 8.0f);
        thePrefs.storeDefaultValue("3d.energizer.radius", 3.5f);
        thePrefs.storeDefaultValue("3d.energizer.scaling.min", 0.2f);
        thePrefs.storeDefaultValue("3d.energizer.scaling.max", 1.0f);
        thePrefs.storeDefaultValue("3d.floor.padding", 5.0f);
        thePrefs.storeDefaultValue("3d.floor.thickness", 0.5f);
        thePrefs.storeDefaultValue("3d.ghost.size", 15.5f);
        thePrefs.storeDefaultValue("3d.house.base_height", 12.0f);
        thePrefs.storeDefaultValue("3d.house.opacity", 0.4f);
        thePrefs.storeDefaultValue("3d.house.sensitivity", 1.5f * TS);
        thePrefs.storeDefaultValue("3d.house.wall_thickness", 2.5f);
        thePrefs.storeDefaultValue("3d.level_counter.symbol_size", 10.0f);
        thePrefs.storeDefaultValue("3d.level_counter.elevation", 6f);
        thePrefs.storeDefaultValue("3d.lives_counter.capacity", 5);
        thePrefs.storeDefaultColor("3d.lives_counter.pillar_color", Color.grayRgb(120));
        thePrefs.storeDefaultColor("3d.lives_counter.plate_color",  Color.grayRgb(180));
        thePrefs.storeDefaultValue("3d.lives_counter.shape_size", 12.0f);
        thePrefs.storeDefaultValue("3d.obstacle.base_height", 4.0f);
        thePrefs.storeDefaultValue("3d.obstacle.corner_radius", 5f);
        thePrefs.storeDefaultValue("3d.obstacle.wall_thickness", 2.25f);
        thePrefs.storeDefaultValue("3d.pac.size", 16.5f);
        thePrefs.storeDefaultValue("3d.pellet.radius", 1.0f);

        // "Kornblumenblau, sind die Augen der Frauen beim Weine..."
        thePrefs.storeDefaultColor("context_menu.title.fill", Color.CORNFLOWERBLUE);
        thePrefs.storeDefaultFont("context_menu.title.font", Font.font("Dialog", FontWeight.BLACK, 14.0f));

        thePrefs.storeDefaultColor("debug_text.fill", Color.YELLOW);
        thePrefs.storeDefaultFont("debug_text.font", Font.font("Sans", FontWeight.BOLD, 16.0f));

        thePrefs.storeDefaultValue("scene2d.max_scaling", 5.0f);
    }

    public void applyConfiguration(String gameVariant, Class<?> configClass) {
        try {
            GameUI_Config config = (GameUI_Config) configClass.getDeclaredConstructor(GameUI.class).newInstance(this);
            config.createGameScenes(this);
            Logger.info("Game scenes for game variant '{}' created", gameVariant);
            config.gameScenes().forEach(scene -> {
                if (scene instanceof GameScene2D gameScene2D) {
                    gameScene2D.debugInfoVisibleProperty().bind(propertyDebugInfoVisible);
                }
            });
            setConfig(gameVariant, config);
        } catch (Exception x) {
            Logger.error("Could not apply UI configuration of class {}", configClass);
            throw new IllegalStateException(x);
        }
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
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Here.</a>
     */
    private void ka_tas_trooo_phe(Throwable reason) {
        Logger.error(reason);
        Logger.error("SOMETHING VERY BAD HAPPENED!");
        showFlashMessageSec(10, "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            theGameContext.theGame().simulationStep().start(theGameClock.tickCount());
            theGameContext.theGameController().updateGameState();
            theGameContext.theGame().simulationStep().logState();
            currentGameScene().ifPresent(GameScene::update);
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private void drawGameView() {
        try {
            playView.draw();
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private EditorView editorView() {
        if (editorView == null) {
            var editor = new TileMapEditor(theStage, theAssets().theModel3DRepository());
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

    @Override public ObjectProperty<Color>            propertyCanvasBackgroundColor() { return propertyCanvasBgColor; }
    @Override public BooleanProperty                  propertyCanvasFontSmoothing() { return propertyCanvasFontSmoothing; }
    @Override public BooleanProperty                  propertyCanvasImageSmoothing(){ return propertyCanvasImageSmoothing; }
    @Override public ObjectProperty<GameScene>        propertyCurrentGameScene() {
        return propertyCurrentGameScene;
    }
    @Override public ObjectProperty<PacManGames_View> propertyCurrentView() {
        return propertyCurrentView;
    }
    @Override public BooleanProperty                  propertyDebugInfoVisible(){ return propertyDebugInfoVisible; }
    @Override public IntegerProperty                  propertyMiniViewHeight(){ return propertyMiniViewHeight; }
    @Override public BooleanProperty                  propertyMiniViewOn(){ return propertyMiniViewOn; }
    @Override public IntegerProperty                  propertyMiniViewOpacityPercent(){ return propertyPipOpacityPercent; }
    @Override public BooleanProperty                  propertyMuted() { return propertyMuted; }
    @Override public IntegerProperty                  propertySimulationSteps(){ return propertySimulationSteps; }
    @Override public BooleanProperty                  property3DAxesVisible(){ return property3DAxesVisible; }
    @Override public ObjectProperty<DrawMode>         property3DDrawMode(){ return property3DDrawMode; }
    @Override public BooleanProperty                  property3DEnabled(){ return property3DEnabled; }
    @Override public BooleanProperty                  property3DEnergizerExplodes(){ return property3DEnergizerExplodes; }
    @Override public ObjectProperty<Color>            property3DFloorColor(){ return property3DFloorColor; }
    @Override public ObjectProperty<Color>            property3DLightColor(){ return property3DLightColor; }
    @Override public BooleanProperty                  property3DPacLightEnabled(){ return property3DPacLightEnabled; }
    @Override public ObjectProperty<PerspectiveID>   property3DPerspective(){ return property3DPerspective; }
    @Override public DoubleProperty                   property3DWallHeight(){ return property3DWallHeight; }
    @Override public DoubleProperty                   property3DWallOpacity(){ return property3DWallOpacity; }

    @Override public Optional<GameScene> currentGameScene() { return Optional.ofNullable(propertyCurrentGameScene.get()); }
    @Override public PacManGames_View    currentView() {
        return propertyCurrentView.get();
    }
    @Override public PlayView thePlayView() {
        return playView;
    }
    @Override public StartPagesView theStartPagesView() { return startPagesView; }

    @Override public Stage theStage() {
        return theStage;
    }

    @Override
    public PreferenceManager thePrefs() { return thePrefs; }

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
            GameUI_Config previousConfig = config(previousVariant);
            Logger.info("Unloading assets for game variant {}", previousVariant);
            previousConfig.dispose();
            previousConfig.soundManager().mutedProperty().unbind();
        }

        GameUI_Config newConfig = config(gameVariant);
        Logger.info("Loading assets for game variant {}", gameVariant);
        newConfig.storeAssets(theAssets());
        newConfig.soundManager().mutedProperty().bind(propertyMuted);

        Image appIcon = theAssets.image(newConfig.assetNamespace() + ".app_icon");
        if (appIcon != null) {
            theStage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariant);
        }

        playView.canvasContainer().roundedBorderProperty().set(newConfig.hasGameCanvasRoundedBorder());

        // this triggers a game event and the event handlers:
        theGameContext.theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        Logger.info("JavaFX runtime: {}", System.getProperty("javafx.runtime.version"));

        propertyCurrentView.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
        playView.dashboard().init(this);

        theStage.centerOnScreen();
        theStage.show();
        theCustomDirWatchdog.startWatching();
    }

    @Override
    public void showEditorView() {
        if (!theGameContext.theGame().isPlaying() || theGameClock.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            theGameClock.stop();
            editorView().editor().start(theStage);
            propertyCurrentView.set(editorView());
        } else {
            Logger.info("Editor view cannot be opened, game is playing");
        }
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        playView.flashMessageLayer().showMessage(String.format(message, args), seconds);
    }

    @Override
    public void showPlayView() {
        propertyCurrentView.set(playView);
    }

    @Override
    public void showStartView() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        playView.dashboard().setVisible(false);
        propertyCurrentView.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        theGameClock.stop();
        theCustomDirWatchdog.dispose();
    }

    @Override
    public PacManGames_Assets theAssets() {
        return theAssets;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameUI_Config> T theConfiguration() {
        return (T) config(theGameContext.theGameController().selectedGameVariant());
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
    public Keyboard theKeyboard() { return theKeyboard; }

    @Override
    public Joypad theJoypad() {
        return theJoypad;
    }

    @Override
    public SoundManager theSound() {
        return theConfiguration().soundManager();
    }

    @Override
    public DirectoryWatchdog theCustomDirWatchdog() {
        return theCustomDirWatchdog;
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        playView.updateGameScene(reloadCurrent);
    }

    @Override
    public void setConfig(String variant, GameUI_Config config) {
        requireNonNull(variant);
        requireNonNull(config);
        configByGameVariant.put(variant, config);
    }

    @Override
    public GameUI_Config config(String gameVariant) {
        return configByGameVariant.get(gameVariant);
    }

    @Override
    public boolean currentGameSceneIsPlayScene2D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && theConfiguration().gameSceneHasID(currentGameScene, "PlayScene2D");
    }

    @Override
    public boolean currentGameSceneIsPlayScene3D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && theConfiguration().gameSceneHasID(currentGameScene, "PlayScene3D");
    }
}