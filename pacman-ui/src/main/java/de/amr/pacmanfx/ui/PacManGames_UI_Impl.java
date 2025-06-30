/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.sound.PacManGames_Sound;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Mesh;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames.theKeyboard;
import static java.util.Objects.requireNonNull;

/**
 * User interface for all Pac-Man game variants.
 */
public class PacManGames_UI_Impl implements PacManGames_UI {

    // package-private access for PacManGames API class
    static final PacManGames_Assets ASSETS = new PacManGames_Assets();
    static final GameClock          GAME_CLOCK = new GameClock();
    static final Keyboard           KEYBOARD = new Keyboard();
    static final Joypad             JOYPAD = new Joypad(KEYBOARD);
    static final PacManGames_Sound  SOUND_MANAGER = new PacManGames_Sound();
    static final DirectoryWatchdog  WATCHDOG = new DirectoryWatchdog(CUSTOM_MAP_DIR);

    // the single instance
    static PacManGames_UI_Impl THE_ONE;

    public static class Builder {

        public static String MS_PACMAN = "MS_PACMAN";
        public static String MS_PACMAN_TENGEN = "MS_PACMAN_TENGEN";
        public static String MS_PACMAN_XXL = "MS_PACMAN_XXL";
        public static String PACMAN = "PACMAN";
        public static String PACMAN_XXL = "PACMAN_XXL";

        private static void checkUserDirsExistingAndWritable() {
            String homeDirDesc = "Pac-Man FX home directory";
            String customMapDirDesc = "Pac-Man FX custom map directory";
            boolean success = checkDirExistingAndWritable(Globals.HOME_DIR, homeDirDesc);
            if (success) {
                Logger.info(homeDirDesc + " is " + Globals.HOME_DIR);
                success = checkDirExistingAndWritable(Globals.CUSTOM_MAP_DIR, customMapDirDesc);
                if (success) {
                    Logger.info(customMapDirDesc + " is " + Globals.CUSTOM_MAP_DIR);
                }
                Logger.info("User directories exist and are writable!");
            }
        }

        private static boolean checkDirExistingAndWritable(File dir, String description) {
            requireNonNull(dir);
            if (!dir.exists()) {
                Logger.info(description + " does not exist, create it...");
                if (!dir.mkdirs()) {
                    Logger.error(description + " could not be created");
                    return false;
                }
                Logger.info(description + " has been created");
                if (!dir.canWrite()) {
                    Logger.error(description + " is not writable");
                    return false;
                }
            }
            return true;
        }

        private final Stage stage;
        private final double width;
        private final double height;
        private final Map<String, GameModel> models = new HashMap<>();
        private final Map<String, Class<? extends PacManGames_UIConfig>> uiConfigClasses = new HashMap<>();
        private StartPage[] startPages;
        private int selectedStartPageIndex;
        private DashboardID[] dashboardIDs = new DashboardID[0];

        public Builder(Stage stage, double width, double height) {
            this.stage = stage;
            this.width = width;
            this.height = height;
        }

        public Builder game(String variant, GameModel model, Class<? extends PacManGames_UIConfig> configClass) {
            models.put(variant, model);
            uiConfigClasses.put(variant, configClass);
            return this;
        }

        public Builder startPages(StartPage... startPages) {
            this.startPages = startPages;
            return this;
        }

        public Builder selectStartPage(int index) {
            this.selectedStartPageIndex = index;
            return this;
        }

        public Builder dashboardEntries(DashboardID... dashboardIDs) {
            this.dashboardIDs = dashboardIDs;
            return this;
        }

        public PacManGames_UI build() {
            validate();
            final var ui = new PacManGames_UI_Impl(stage, width, height);
            ui.configure(uiConfigClasses);
            models.forEach((variant, model) -> theGameController().registerGame(variant, model));
            theGameController().setEventsEnabled(true);
            ui.gameView().dashboard().configure(dashboardIDs);
            for (StartPage startPage : startPages) ui.startPagesView().addStartPage(startPage);
            ui.startPagesView().selectStartPage(selectedStartPageIndex);
            ui.startPagesView().currentStartPage()
                .map(StartPage::currentGameVariant)
                .ifPresent(theGameController()::selectGameVariant);
            THE_ONE = ui;
            return THE_ONE;
        }

        private void validate() {
            checkUserDirsExistingAndWritable();
            if (stage == null) {
                error("Stage is null");
            }
            if (width <= 0) {
                error("Stage width (%.2f) must be a positive number".formatted(width));
            }
            if (height <= 0) {
                error("Stage height (%.2f) must be a positive number".formatted(height));
            }
            if (models.isEmpty()) {
                error("No game models specified");
            }
            models.forEach((variant, model) -> {
                validateGameVariant(variant);
                if (model == null) {
                    error("Game model is null");
                }
            });
            if (uiConfigClasses.isEmpty()) {
                error("No UI configurations specified");
            }
            uiConfigClasses.forEach((variant, configClass) -> {
                validateGameVariant(variant);
                if (configClass == null) {
                    error("UI configuration class is null");
                }
            });
            if (dashboardIDs == null) {
                error("Dashboard entry list is null");
            }
            if (startPages == null) {
                error("Start pages list is null");
            }
            if (startPages.length == 0) {
                error("Start pages list is empty");
            }
            if (selectedStartPageIndex < 0 || selectedStartPageIndex >= startPages.length) {
                error("Selected start page index (%d) is out of range 0..%d".formatted(selectedStartPageIndex, startPages.length - 1));
            }
        }

        private void validateGameVariant(String variant) {
            if (variant == null) {
                error("Game variant is null");
            }
            if (variant.isBlank()) {
                error("Game variant is blank string");
            }
            if (!variant.matches("[a-zA-Z_$][a-zA-Z_$0-9]*")) {
                error("Game variant ('%s') is not a valid identifier".formatted(variant));
            }
        }

        private void error(String message) {
            throw new RuntimeException("Application building failed: %s".formatted(message));
        }
    }


    private final Map<String, PacManGames_UIConfig> configByGameVariant = new HashMap<>();

    private final ObjectProperty<PacManGames_View> currentViewPy      = new SimpleObjectProperty<>();
    private final ObjectProperty<GameScene>        currentGameScenePy = new SimpleObjectProperty<>();

    private final StackPane rootPane = new StackPane();
    private final Stage stage;
    private final StartPagesView startPagesView;
    private final GameView gameView;
    private EditorView editorView; // created on first access

    public PacManGames_UI_Impl(Stage stage, double width, double height) {
        this.stage = requireNonNull(stage);

        Scene mainScene = new Scene(rootPane, width, height);
        stage.setScene(mainScene);

        stage.setMinWidth(280);
        stage.setMinHeight(360);

        startPagesView = new StartPagesView(this);
        startPagesView.setBackground(theAssets().background("background.scene"));
        gameView = new GameView(this, mainScene);

        rootPane.getChildren().add(startPagesView.rootNode());

        {
            var iconBox = new StatusIconBox(this);
            StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);

            var iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
            iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> currentView() == gameView() && GAME_CLOCK.isPaused(),
                currentViewProperty(), GAME_CLOCK.pausedProperty()));
            StackPane.setAlignment(iconPaused, Pos.CENTER);

            rootPane.getChildren().addAll(iconPaused, iconBox);
        }

        GAME_CLOCK.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        GAME_CLOCK.setPermanentAction(this::drawGameView);

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, theKeyboard()::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, theKeyboard()::onKeyReleased);
        //TODO use actions and key binding instead?
        mainScene.setOnKeyPressed(e -> {
            if (KEY_FULLSCREEN.match(e)) {
                PacManGames_GameActions.ACTION_ENTER_FULLSCREEN.execute(this);
            }
            else if (KEY_MUTE.match(e)) {
                PacManGames_GameActions.ACTION_TOGGLE_MUTED.execute(this);
            }
            else if (KEY_OPEN_EDITOR.match(e)) {
                showEditorView();
            }
            else {
                currentView().handleKeyboardInput();
            }
        });
        currentViewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));

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
                setConfiguration(gameVariant, config);
            } catch (Exception x) {
                Logger.error("Could not create UI configuration of class {}", configClass);
                throw new IllegalStateException(x);
            }
        });
        configByGameVariant.forEach((gameVariant, config) -> {
            config.createGameScenes();
            config.gameScenes().forEach(scene -> {
                if (scene instanceof GameScene2D gameScene2D) {
                    gameScene2D.debugInfoVisibleProperty().bind(PY_DEBUG_INFO_VISIBLE);
                }
            });
            Logger.info("Game scenes for game variant {} created", gameVariant);
        });
    }

    private void handleViewChange(PacManGames_View oldView, PacManGames_View newView) {
        requireNonNull(newView);
        if (oldView != null) {
            oldView.clearActionBindings();
            theGameEventManager().removeEventListener(oldView);
        }
        newView.updateActionBindings();
        newView.rootNode().requestFocus();
        stage.titleProperty().bind(newView.title());
        theGameEventManager().addEventListener(newView);

        rootPane.getChildren().set(0, newView.rootNode());
    }

    /**
     * @param x cause of catastrophe
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
            theSimulationStep().start(GAME_CLOCK.tickCount());
            theGameController().updateGameState();
            theSimulationStep().log();
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

    private EditorView lazyGetEditorView() {
        if (editorView == null) {
            var editor = new TileMapEditor(stage);
            var miQuit = new MenuItem(theAssets().text("back_to_game"));
            miQuit.setOnAction(e -> {
                editor.stop();
                editor.executeWithCheckForUnsavedChanges(this::showStartView);
            });
            editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuit);
            editor.init(CUSTOM_MAP_DIR);
            editorView = new EditorView(editor);
        }
        return editorView;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameScenePy;
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(currentGameScenePy.get());
    }

    @Override
    public ObjectProperty<PacManGames_View> currentViewProperty() {
        return currentViewPy;
    }

    @Override
    public PacManGames_View currentView() {
        return currentViewPy.get();
    }

    @Override
    public GameView gameView() {
        return gameView;
    }

    @Override
    public void restart() {
        GAME_CLOCK.stop();
        GAME_CLOCK.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        GAME_CLOCK.pausedProperty().set(false);
        GAME_CLOCK.start();
        theGameController().restart(GameState.BOOT);
    }

    @Override
    public void selectGameVariant(String gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }
        String previousVariant = theGameController().selectedGameVariant();
        if (previousVariant != null) {
            Logger.info("Unloading assets for game variant {}", previousVariant);
            configuration(previousVariant).unloadAssets(theAssets());
            Logger.info(theAssets().summary(Map.of(
                Image.class, "Images",
                AudioClip.class, "Sounds")
            ));
        }
        PacManGames_UIConfig newConfig = configuration(gameVariant);

        Logger.info("Loading assets for game variant {}", gameVariant);
        newConfig.loadAssets(theAssets());
        Logger.info(theAssets().summary(Map.of(
            Image.class, "Images",
            AudioClip.class, "Sounds")
        ));
        SOUND_MANAGER.selectGameVariant(gameVariant, newConfig.assetNamespace());
        Image appIcon = ASSETS.image(newConfig.assetNamespace() + ".app_icon");
        if (appIcon != null) {
            stage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariant);
        }
        gameView.canvasContainer().roundedBorderProperty().set(newConfig.hasGameCanvasRoundedBorder());
        // this triggers a game event and the event handlers:
        theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        currentViewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
        gameView.dashboard().init();
        stage.centerOnScreen();
        stage.show();
        WATCHDOG.startWatching();
    }

    @Override
    public void showEditorView() {
        if (!theGame().isPlaying() || GAME_CLOCK.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            SOUND_MANAGER.stopAll();
            GAME_CLOCK.stop();
            lazyGetEditorView().editor().start(stage);
            currentViewPy.set(lazyGetEditorView());
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
        currentViewPy.set(gameView);
    }

    @Override
    public void showStartView() {
        GAME_CLOCK.stop();
        GAME_CLOCK.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        SOUND_MANAGER.stopAll();
        gameView.dashboard().setVisible(false);
        currentViewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public StartPagesView startPagesView() { return startPagesView; }

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
    public void setConfiguration(String variant, PacManGames_UIConfig configuration) {
        requireNonNull(variant);
        requireNonNull(configuration);
        configByGameVariant.put(variant, configuration);
    }

    @Override
    public PacManGames_UIConfig configuration(String gameVariant) {
        return configByGameVariant.get(gameVariant);
    }

    @Override
    public PacManGames_UIConfig configuration() {
        return configByGameVariant.get(theGameController().selectedGameVariant());
    }

    @Override
    public boolean currentGameSceneIsPlayScene2D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && configuration().gameSceneHasID(currentGameScene, "PlayScene2D");
    }

    @Override
    public boolean currentGameSceneIsPlayScene3D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && configuration().gameSceneHasID(currentGameScene, "PlayScene3D");
    }
}