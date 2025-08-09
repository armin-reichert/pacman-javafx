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
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static java.util.Objects.requireNonNull;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public class PacManGames_UI_Impl implements GameUI {

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

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
    final ObjectProperty<PerspectiveID> property3DPerspective  = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    final DoubleProperty property3DWallHeight                  = new SimpleDoubleProperty();
    final DoubleProperty property3DWallOpacity                 = new SimpleDoubleProperty(1.0);

    // private properties
    private final BooleanProperty propertyMuted = new SimpleBooleanProperty(false);

    private final PacManGames_Assets theAssets;
    private final DirectoryWatchdog  theCustomDirWatchdog;
    private final GameClock          theGameClock;
    private final GameContext        theGameContext;
    private final Keyboard           theKeyboard;
    private final Joypad             theJoypad;
    private final UserUIPreferences  thePrefs;
    private final Stage              theStage;

    private final ActionBindingManager globalActionBindings = new DefaultActionBindingManager();
    private final Map<String, GameUI_Config> configByGameVariant = new HashMap<>();
    private final MainScene mainScene;
    private final StartPagesView startPagesView;
    private final PlayView playView;
    private       EditorView editorView; // created on first access

    public PacManGames_UI_Impl(GameContext gameContext, Stage stage, double width, double height) {
        requireNonNull(gameContext, "Game context is null");
        requireNonNull(stage, "Stage is null");

        // Input
        theKeyboard = new Keyboard();
        theJoypad = new Joypad(theKeyboard);

        // Game context
        theCustomDirWatchdog = new DirectoryWatchdog(gameContext.theCustomMapDir());
        theGameClock = new GameClock();
        theGameContext = gameContext;

        // Game UI
        theAssets = new PacManGames_Assets();
        thePrefs = new UserUIPreferences(PacManGames_UI_Impl.class);
        theStage = stage;

        initPreferences();
        initGlobalActionBindings();

        mainScene = new MainScene(this, width, height);
        // First, check if a global action binding is defined for the key, if not, delegate to the current view:
        mainScene.setOnKeyPressed(e -> runActionOrElse(
            globalActionBindings.matchingAction(theKeyboard).orElse(null),
            () -> currentView().handleKeyboardInput(this)));

        configureStage(stage);

        startPagesView = new StartPagesView(this);
        playView = new PlayView(this, gameContext, mainScene);

        theGameClock.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        theGameClock.setPermanentAction(this::drawCurrentView);

        property3DWallHeight.set(thePrefs.getFloat("3d.obstacle.base_height"));
        property3DWallOpacity.set(thePrefs.getFloat("3d.obstacle.opacity"));
    }

    private void configureStage(Stage stage) {
        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);

        var title = Bindings.createStringBinding(
            () -> {
                PacManGames_View currentView = currentView();
                return currentView == null
                    ? "No View?"
                    : currentView.title().map(ObservableObjectValue::get)
                        .orElse(mainScene.computeTitle(property3DEnabled().get(), propertyDebugInfoVisible().get()));
            },
            propertyCurrentGameScene(),
            propertyCurrentView(),
            propertyDebugInfoVisible(),
            property3DEnabled(),
            theGameClock().pausedProperty(),
            mainScene.heightProperty()
        );
        stage.titleProperty().bind(title);
    }

    private void initGlobalActionBindings() {
        globalActionBindings.use(ACTION_ENTER_FULLSCREEN, DEFAULT_ACTION_BINDINGS);
        globalActionBindings.use(ACTION_OPEN_EDITOR,      DEFAULT_ACTION_BINDINGS);
        globalActionBindings.use(ACTION_TOGGLE_MUTED,     DEFAULT_ACTION_BINDINGS);
        globalActionBindings.updateKeyboard(theKeyboard);
    }

    private void initPreferences() {
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
        thePrefs.storeDefaultValue("3d.obstacle.corner_radius", 4.0f);
        thePrefs.storeDefaultValue("3d.obstacle.opacity", 1.0f);
        thePrefs.storeDefaultValue("3d.obstacle.wall_thickness", 2.25f);
        thePrefs.storeDefaultValue("3d.pac.size", 16.0f);
        thePrefs.storeDefaultValue("3d.pellet.radius", 1.0f);

        // "Kornblumenblau, sind die Augen der Frauen beim Weine. Hicks!"
        thePrefs.storeDefaultColor("context_menu.title.fill", Color.CORNFLOWERBLUE);
        thePrefs.storeDefaultFont("context_menu.title.font", Font.font("Dialog", FontWeight.BLACK, 14.0f));

        thePrefs.storeDefaultColor("debug_text.fill", Color.YELLOW);
        thePrefs.storeDefaultFont("debug_text.font", Font.font("Sans", FontWeight.BOLD, 16.0f));

        thePrefs.storeDefaultValue("scene2d.max_scaling", 5.0f);

        if (!thePrefs.isBackingStoreAccessible()) {
            Logger.error("User preferences could not be accessed, using default values!");
        } else {
            thePrefs.addMissingValues();
        }
    }

    private void showView(PacManGames_View view) {
        requireNonNull(view);

        final PacManGames_View oldView = mainScene.currentView();
        if (oldView != null) {
            oldView.actionBindingMap().removeFromKeyboard(theKeyboard);
            theGameContext.theGameEventManager().removeEventListener(oldView);
        }
        view.actionBindingMap().updateKeyboard(theKeyboard);
        theGameContext.theGameEventManager().addEventListener(view);
        mainScene.setCurrentView(view);
    }

    public void applyConfiguration(String gameVariant, Class<?> configClass) {
        try {
            GameUI_Config config = (GameUI_Config) configClass.getDeclaredConstructor(GameUI.class).newInstance(this);
            config.createGameScenes();
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

    private void drawCurrentView() {
        try {
            if (currentView() == playView) {
                playView.draw();
            }
            mainScene.flashMessageLayer().update();
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
    // -----------------------------------------------------------------------------------------------------------------

    @Override public ObjectProperty<Color>            propertyCanvasBackgroundColor() { return propertyCanvasBgColor; }
    @Override public BooleanProperty                  propertyCanvasFontSmoothing() { return propertyCanvasFontSmoothing; }
    @Override public BooleanProperty                  propertyCanvasImageSmoothing(){ return propertyCanvasImageSmoothing; }
    @Override public ObjectProperty<GameScene>        propertyCurrentGameScene() { return mainScene.currentGameSceneProperty();}
    @Override public ObjectProperty<PacManGames_View> propertyCurrentView() { return mainScene.currentViewProperty(); }
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
    @Override public ObjectProperty<PerspectiveID>    property3DPerspective(){ return property3DPerspective; }
    @Override public DoubleProperty                   property3DWallHeight(){ return property3DWallHeight; }
    @Override public DoubleProperty                   property3DWallOpacity(){ return property3DWallOpacity; }

    @Override public Optional<GameScene>              currentGameScene() { return mainScene.currentGameScene(); }
    @Override public PacManGames_View                 currentView() { return mainScene.currentView(); }

    @Override public Optional<EditorView>             theEditorView() { return Optional.ofNullable(editorView); }
    @Override public PlayView                         thePlayView() { return playView; }
    @Override public StartPagesView                   theStartPagesView() { return startPagesView; }

    @Override
    public boolean isCurrentGameSceneID(String id) {
        return mainScene.isCurrentGameSceneID(id);
    }

    @Override public Stage theStage() { return theStage; }

    @Override
    public UserUIPreferences theUserPrefs() { return thePrefs; }

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
        playView.dashboard().init(this);
        showStartView();
        theStage.centerOnScreen();
        theStage.show();
        Platform.runLater(theCustomDirWatchdog::startWatching);
    }

    @Override
    public void showEditorView() {
        if (!theGameContext.theGame().isPlaying() || theGameClock.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            theGameClock.stop();
            editorView().editor().start(theStage);
            showView(editorView());
        } else {
            Logger.info("Editor view cannot be opened, game is playing");
        }
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        mainScene.flashMessageLayer().showMessage(String.format(message, args), seconds);
    }

    @Override
    public void showPlayView() {
        showView(playView);
    }

    @Override
    public void showStartView() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        playView.dashboard().setVisible(false);
        startPagesView.currentStartPage().ifPresent(startPage -> {
            startPage.layoutRoot().requestFocus();
            startPage.onEnter(this); // sets game variant!
        });
        showView(startPagesView);
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
}