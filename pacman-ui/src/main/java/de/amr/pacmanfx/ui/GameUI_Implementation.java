/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameCheats;
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

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    // Game model access
    private final GameBox gameBox;

    // Observes changes in custom map directory
    private final DirectoryWatchdog customDirWatchdog;

    // So many managers? I think I should fire some!
    private final GameSceneManager gameSceneManager = new GameSceneManager(this);
    private final PreferencesManager prefsManager;
    private final SoundManager soundManager = new SoundManager();
    private final TranslationManager translationManager = () -> GameUIConstants.LOCALIZED_TEXTS;
    private final UIConfigManager uiConfigManager = new UIConfigManager();
    private final ViewManager viewManager;

    // Sprite animation support
    private final SpriteAnimationTimer spriteAnimationTimer = new SpriteAnimationTimer(new SpriteAnimationSet());

    // UI components
    private final Stage stage;
    private final GameUI_MainScene scene;
    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final StatusIconBox statusIconBox = new StatusIconBox();

    private StringBinding stageTitleBinding;

    public GameUI_Implementation(GameBox gameBox, Stage stage, int mainSceneWidth, int mainSceneHeight) {
        this.gameBox = requireNonNull(gameBox);
        this.stage = requireNonNull(stage);
        this.scene = new GameUI_MainScene(requireNonNegative(mainSceneWidth), requireNonNegative(mainSceneHeight));
        this.customDirWatchdog = new DirectoryWatchdog(gameBox.customMapDir());
        this.prefsManager = createPrefsManager();

        viewManager = createViewManager();
        viewManager.setStartView(new StartPagesCarousel(this));
        viewManager.setEditorViewFactory(this::createEditorView);
        viewManager.setPlayView(createPlayView());
    }

    // GameUI interface

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return customDirWatchdog;
    }

    @Override
    public GameContext gameContext() {
        return gameBox;
    }

    @Override
    public UIConfig configForGameVariant(String gameVariantName) {
        return uiConfigManager.getOrCreateUIConfig(gameVariantName);
    }

    @Override
    public GameSceneManager gameSceneManager() {
        return gameSceneManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends UIConfig> T currentConfig() {
        final String gameVariantName = gameContext().gameVariantName();
        if (gameVariantName == null) {
            throw new IllegalStateException("Cannot access UI configuration: no game variant is selected");
        }
        return (T) configForGameVariant(gameContext().gameVariantName());
    }

    @Override
    public void openWorldMapFileInEditor(File worldMapFile) {
        viewManager.createEditorIfNotExisting(gameBox.customMapDir());
        viewManager.optEditorView().map(EditorView::editor).ifPresent(editor -> {
            try {
                if (worldMapFile != null) {
                    editor.editFile(worldMapFile);
                }
                viewManager.selectEditorView(this);
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
    public PreferencesManager preferencesManager() {
        return prefsManager;
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
        prefsManager.logPreferences();

        // Load 3D assets
        final var _ = PacManWorld3D.instance();

        // These need the current UI config to be initialized
        GameUIConstants.PROPERTY_3D_WALL_HEIGHT .set(currentConfig().worldConfig().maze().obstacleBaseHeight());
        GameUIConstants.PROPERTY_3D_WALL_OPACITY.set(currentConfig().worldConfig().maze().obstacleOpacity());

        viewManager.playView().dashboard().init(this);
        initMainScene();
        initPropertyBindings(gameContext().game());
        initGameClock(gameContext().clock());

        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        gameContext().gameVariantNameProperty().addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(gameContext().gameVariantName());

        stage.setScene(scene);
        stage.setMinWidth(GameUIConstants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUIConstants.MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(stageTitleBinding);

        final Image icon = currentConfig().assets().image("app_icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }

        viewManager.selectStartView();

        stage.centerOnScreen();
        stage.show();

        Platform.runLater(() -> {
            customDirWatchdog.startWatching();
            flashMessageView.start();
            spriteAnimationTimer.start();
        });
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
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
    public void stopGame() {
        gameContext().game().prepareNewGame();

        gameContext().clock().stop();
        gameContext().clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        soundManager.stopAll();

        gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.soundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene.deactivate();
            gameSceneManager.removeFromPlayView(gameScene);
            gameSceneManager.gameSceneProperty().set(null);
        });

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        spriteAnimationTimer.stop();
        spriteAnimationTimer.spriteAnimationSet().clear();
        flashMessageView.stop();
        customDirWatchdog.dispose();
    }

    @Override
    public TranslationManager translationManager() {
        return translationManager;
    }

    @Override
    public UIConfigManager uiConfigManager() {
        return uiConfigManager;
    }

    @Override
    public ViewManager viewManager() {
        return viewManager;
    }

    // private stuff

    private PreferencesManager createPrefsManager() {
        return new PreferencesManager(GameUI_Implementation.this.getClass()) {
            @Override
            protected void storeDefaultPrefValues() {}
        };
    }

    private ViewManager createViewManager() {
        final var viewManager = new ViewManager(scene.rootPane(), flashMessageView);

        viewManager.setEditorCanOpen(() -> {
            if (viewManager.isStartViewSelected()) return true;
            if (viewManager.isEditorViewSelected()) return false;
            if (viewManager.isPlayViewSelected()) {
                return !gameContext().game().isPlayingLevel();
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
            viewManager.selectStartView();
        });
        return editorView;
    }

    private void initGameClock(GameClock clock) {
        clock.setUpdateAction(() -> {
            final SimulationStep step = gameContext().game().doSimulationStep();
            step.clearInfo(clock.tickCount());
            gameContext().game().flow().update();
            step.printLog();
            gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock));
        });
        clock.setPermanentAction(() -> viewManager.currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initMainScene() {
        final KeyboardInfo keyboardInfo = new KeyboardInfo(Input.instance().keyboard);
        scene.rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            statusIconBox,
            flashMessageView,
            keyboardInfo.rootPane());

        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        scene.init(this);
    }

    private void initPropertyBindings(Game game) {
        soundManager.muteProperty().bind(GameUIConstants.PROPERTY_MUTED);

        statusIconBox.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> viewManager.isPlayViewSelected() || viewManager.isStartViewSelected(),
            viewManager.currentViewProperty()));

        bindStatusBoxIcons(game);

        stageTitleBinding = createStringBinding(
            this::computeStageTitle,
            gameContext().clock().updatesDisabledProperty(),
            gameContext().gameVariantNameProperty(),
            viewManager.currentViewProperty(),
            gameSceneManager.gameSceneProperty(),
            GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUIConstants.PROPERTY_3D_ENABLED
        );

        scene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> gameSceneManager.currentGameSceneHasID(CommonSceneID.PLAY_SCENE_3D)
                ? GameUIConstants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUIConstants.WALLPAPERS.length)]
                : GameUIConstants.BACKGROUND_PAC_MAN_WALLPAPER
            , viewManager.currentViewProperty(), gameSceneManager().gameSceneProperty()
        ));

        gameContext().gameVariantNameProperty().addListener((_, _, gameVariantName)
            -> bindStatusBoxIcons(gameBox.gameByVariantName(gameVariantName)));
    }

    private void bindStatusBoxIcons(Game game) {
        final GameCheats cheats = gameContext().game().cheats();

        statusIconBox.iconAutopilot().visibleProperty().unbind();
        statusIconBox.iconAutopilot().visibleProperty().bind(cheats.usingAutopilotProperty());

        statusIconBox.iconCheated()  .visibleProperty().unbind();
        statusIconBox.iconCheated()  .visibleProperty().bind(cheats.cheatUsedProperty());

        statusIconBox.iconImmune()   .visibleProperty().unbind();
        statusIconBox.iconImmune()   .visibleProperty().bind(cheats.immuneProperty());

        Logger.info("Icons autopilot, cheated and immune visibility bound to game model {}", game);
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Platform.runLater(() -> {
            final String errorMessage = translationManager.translate("error.oh_no_my_program");
            showFlashMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            stopGame();
            Logger.error("*** SOMETHING VERY BAD HAPPENED:");
            Logger.error(reason);
        });
    }

    private String computeStageTitle() {
        final View view = viewManager.currentView();
        return view == null
            ? translationManager.translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene());
    }

    private String titleForCurrentGameScene() {
        final GameScene gameScene = gameSceneManager.optCurrentGameScene().orElse(null);

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

        final String viewMode = translationManager.translate(is3D ? "threeD" : "twoD");

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