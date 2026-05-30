/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static de.amr.pacmanfx.core.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    // So many managers? I think I should fire some!
    public record ManagementBoard(
        UIConfigManager configManager,
        GameSceneManager gameSceneManager,
        PreferencesManager prefsManager,
        SoundManager soundManager,
        TranslationManager translationManager,
        ViewManager viewManager)
    {}

    // Game model access
    private final GameBox gameBox;

    // Observes changes in custom map directory
    private final DirectoryWatchdog customDirWatchdog;

    private final ManagementBoard management;

    // Sprite animation support
    private final SpriteAnimationTimer spriteAnimationTimer = new SpriteAnimationTimer(new SpriteAnimationSet());

    // UI components
    private final Stage stage;
    private final GameUI_MainScene scene;
    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final StatusIconBox statusIconBox;

    private StringBinding stageTitleBinding;

    public GameUI_Implementation(de.amr.pacmanfx.core.GameBox gameBox, Stage stage, int mainSceneWidth, int mainSceneHeight) {
        this.gameBox = requireNonNull(gameBox);
        this.stage = requireNonNull(stage);
        this.scene = new GameUI_MainScene(requireNonNegative(mainSceneWidth), requireNonNegative(mainSceneHeight));
        this.customDirWatchdog = new DirectoryWatchdog(gameBox.customMapDir());

        final ViewManager viewManager = createViewManager(scene, flashMessageView, gameBox);
        viewManager.setStartView(new StartPagesCarousel(this));
        viewManager.setPlayView(createPlayView());
        viewManager.setEditorViewFactory(this::createEditorView);

        management = new ManagementBoard(
            new UIConfigManager(),
            new GameSceneManager(this),
            createPrefsManager(),
            new SoundManager(),
            () -> GameUIConstants.LOCALIZED_TEXTS,
            viewManager
        );

        viewManager.playView().configurePropertyBindings(this);
        statusIconBox = new StatusIconBox(management.translationManager());
    }


    // GameUI interface

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return customDirWatchdog;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends UIConfig> T currentConfig() {
        final String gameVariantName = gameContext().gameVariantName();
        if (gameVariantName == null) {
            throw new IllegalStateException("Cannot access UI configuration: no game variant is selected");
        }
        return (T) management.configManager().getOrCreateUIConfig(gameVariantName);
    }

    @Override
    public GameContext gameContext() {
        return gameBox;
    }

    @Override
    public void openWorldMapFileInEditor(File worldMapFile) {
        management.viewManager().createEditorIfNotExisting(gameBox.customMapDir());
        management.viewManager().optEditorView().map(EditorView::editor).ifPresent(editor -> {
            try {
                if (worldMapFile != null) {
                    editor.editFile(worldMapFile);
                }
                management.viewManager().selectEditorView(this);
            } catch (IOException x) {
                Logger.error(x, "Could not open map file {}", worldMapFile);
                showFlashMessage("Cannot open world map file");
            }
            catch (WorldMapParseException x) {
                Logger.error(x, "Error reading map file data from {}", worldMapFile);
                showFlashMessage("Cannot read world map file data");
            }
        });
    }

    @Override
    public void restart() {
        stopGame();
        gameContext().game().flow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(gameContext().clock()::start);
    }

    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public void show() {
        initGameVariantAndRegisterChangeHandler();
        load3DAssets();
        management.viewManager().playView().dashboard().init(this);
        initMainScene();
        initProperties();
        initGameClock();
        displayStage();
        startServices();
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
    }

    @Override
    public SpriteAnimationSet spriteAnimationSet() {
        return spriteAnimationTimer.spriteAnimationSet();
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    @Override
    public void stopGame() {
        gameContext().game().prepareNewGame();

        gameContext().clock().stop();
        gameContext().clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        management.soundManager().stopAll();

        management.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.soundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene.deactivate();
            management.gameSceneManager().removeFromPlayView(management.viewManager.playView(), gameScene);
            management.gameSceneManager().gameSceneProperty().set(null);
        });

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        spriteAnimationTimer.stop();
        spriteAnimationTimer.spriteAnimationSet().clear();
        flashMessageView.stopTimer();
        customDirWatchdog.dispose();
    }


    // Management board (totally overpaid)

    @Override
    public UIConfigManager configManager() {
        return management.configManager();
    }

    @Override
    public GameSceneManager gameSceneManager() {
        return management.gameSceneManager();
    }

    @Override
    public PreferencesManager preferencesManager() {
        return management.prefsManager();
    }

    @Override
    public SoundManager soundManager() {
        return management.soundManager();
    }

    @Override
    public TranslationManager translationManager() {
        return management.translationManager();
    }

    @Override
    public ViewManager viewManager() {
        return management.viewManager();
    }

    // private stuff

    private static void load3DAssets() {
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing singleton
    }

    private static PreferencesManager createPrefsManager() {
        return new PreferencesManager(GameUI_Implementation.class) {
            @Override
            protected void storeDefaultPrefValues() {}
        };
    }

    private static ViewManager createViewManager(GameUI_MainScene scene, FlashMessageView flashMessageView, GameContext gameContext) {
        final var viewManager = new ViewManager(scene.rootPane(), flashMessageView);

        viewManager.setEditorCanOpen(() -> {
            if (viewManager.isStartViewSelected()) return true;
            if (viewManager.isEditorViewSelected()) return false;
            if (viewManager.isPlayViewSelected()) {
                return !gameContext.game().isPlayingLevel();
            }
            return false;
        });
        return viewManager;
    }

    private PlayView createPlayView() {
        final var playView = new PlayView(this, GameUIConstants.DEFAULT_DASHBOARD_CONFIG);
        final ChangeListener<? super Number> playViewResizer = (_,_,_) -> playView.resizeToFit(scene);
        scene.widthProperty().addListener(playViewResizer);
        scene.heightProperty().addListener(playViewResizer);
        return playView;
    }

    private EditorView createEditorView() {
        final var editorView = new EditorView(stage, this);
        editorView.editor().setOnQuit(_ -> {
            // restore title (editor changed it)
            stage.titleProperty().unbind();
            stage.titleProperty().bind(stageTitleBinding);
            management.viewManager().selectStartView();
        });
        return editorView;
    }

    private void initGameClock() {
        final GameClock clock = gameContext().clock();
        clock.setUpdateAction(() -> {
            final SimulationStep step = gameContext().game().doSimulationStep();
            step.clearInfo(clock.tickCount());
            gameContext().game().flow().update();
            step.printLog();
            management.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock));
        });
        clock.setPermanentAction(() -> management.viewManager().currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initMainScene() {
        final KeyboardInfo keyboardInfo = new KeyboardInfo(Input.instance().keyboard);

        scene.rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            statusIconBox.rootPane(),
            flashMessageView.rootPane(),
            keyboardInfo.rootPane());

        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        scene.init(this);

        statusIconBox.bind(gameContext().game());
    }

    private void initProperties() {

        // These need the current UI config to be initialized
        GameUIConstants.PROPERTY_3D_WALL_HEIGHT .set(currentConfig().worldConfig().maze().obstacleBaseHeight());
        GameUIConstants.PROPERTY_3D_WALL_OPACITY.set(currentConfig().worldConfig().maze().obstacleOpacity());

        management.soundManager().muteProperty().bind(GameUIConstants.PROPERTY_MUTED);

        statusIconBox.rootPane().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> management.viewManager().isPlayViewSelected() || management.viewManager().isStartViewSelected(),
            management.viewManager().currentViewProperty()));

        stageTitleBinding = createStringBinding(
            this::computeStageTitle,
            gameContext().clock().updatesDisabledProperty(),
            gameContext().gameVariantNameProperty(),
            management.viewManager().currentViewProperty(),
            management.gameSceneManager().gameSceneProperty(),
            GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUIConstants.PROPERTY_3D_ENABLED
        );

        scene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> management.gameSceneManager().currentGameSceneHasID(this, CommonSceneID.PLAY_SCENE_3D)
                ? GameUIConstants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUIConstants.WALLPAPERS.length)]
                : GameUIConstants.BACKGROUND_PAC_MAN_WALLPAPER
            , management.viewManager().currentViewProperty(), gameSceneManager().gameSceneProperty()
        ));
    }

    private void startServices() {
        Platform.runLater(() -> {
            customDirWatchdog.startWatching();
            flashMessageView.startTimer();
            spriteAnimationTimer.start();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        gameContext().gameVariantNameProperty().addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(gameContext().gameVariantName());
    }

    private void displayStage() {
        stage.setScene(scene);
        stage.setMinWidth(GameUIConstants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUIConstants.MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(stageTitleBinding);
        final Image icon = currentConfig().assets().image("app_icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        stage.centerOnScreen();
        stage.show();
        management.viewManager().selectStartView();
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Platform.runLater(() -> {
            final String errorMessage = management.translationManager().translate("error.oh_no_my_program");
            showFlashMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            stopGame();
            Logger.error("*** SOMETHING VERY BAD HAPPENED:");
            Logger.error(reason);
        });
    }

    private String computeStageTitle() {
        final View view = management.viewManager().currentView();
        return view == null
            ? management.translationManager().translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene());
    }

    private String titleForCurrentGameScene() {
        final GameScene gameScene = management.gameSceneManager().optCurrentGameScene().orElse(null);

        final boolean debug = GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D = GameUIConstants.PROPERTY_3D_ENABLED.get();
        final boolean paused = gameContext().clock().getUpdatesDisabled();

        final String normalTitle = appTitle(paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String appTitle(boolean paused, boolean is3D) {
        final String gameVariantName = gameContext().gameVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewMode = management.translationManager().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager appSpecificTranslator = currentConfig().assets();
        final String appTitleKey = paused ? "app.title.paused" : "app.title";
        if (appSpecificTranslator.bundle() != null
            && appSpecificTranslator.bundle().containsKey(appTitleKey)) {
            return appSpecificTranslator.translate(appTitleKey, viewMode);
        } else {
            return "Unspecified Game";
        }
    }
}