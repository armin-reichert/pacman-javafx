/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.CustomMapSelectionMode;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui2d.action.GameActionProvider;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.GameSound;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import de.amr.games.pacman.ui2d.input.Keyboard;
import de.amr.games.pacman.ui2d.page.DashboardLayer;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.StartPageCarousel;
import de.amr.games.pacman.ui2d.scene.GameConfiguration;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.uilib.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.games.pacman.controller.GameController.TICKS_PER_SECOND;
import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_CANVAS_BG_COLOR;
import static de.amr.games.pacman.uilib.Ufx.createIcon;

/**
 * User interface for all Pac-Man game variants (2D only).
 *
 * @author Armin Reichert
 */
public class PacManGamesUI implements GameEventListener, GameContext {

    public static final ArcadeKeyBinding DEFAULT_ARCADE_KEY_BINDING = new ArcadeKeyBinding(
        new KeyCodeCombination(KeyCode.DIGIT5),
        new KeyCodeCombination(KeyCode.DIGIT1),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    // My current bindings, might be crap
    public static final JoypadKeyBinding JOYPAD_CURSOR_KEYS = new JoypadKeyBinding.Binding(
        new KeyCodeCombination(KeyCode.SPACE),
        new KeyCodeCombination(KeyCode.ENTER),
        new KeyCodeCombination(KeyCode.B),
        new KeyCodeCombination(KeyCode.N),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    // Like Mesen emulator key set #2
    public static final JoypadKeyBinding JOYPAD_WASD = new JoypadKeyBinding.Binding(
        new KeyCodeCombination(KeyCode.U),
        new KeyCodeCombination(KeyCode.I),
        new KeyCodeCombination(KeyCode.J),
        new KeyCodeCombination(KeyCode.K),
        new KeyCodeCombination(KeyCode.W),
        new KeyCodeCombination(KeyCode.S),
        new KeyCodeCombination(KeyCode.A),
        new KeyCodeCombination(KeyCode.D)
    );

    protected final GameAction actionOpenEditor = new GameAction() {
        @Override
        public void execute(GameContext context) {
            context.currentGameScene().ifPresent(GameScene::end);
            context.sound().stopAll();
            context.gameClock().stop();
            EditorPage editorPage = getOrCreateEditorPage();
            editorPage.startEditor(context.level().world().map());
            context.selectPage(editorPage);
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return context.gameVariant() == GameVariant.PACMAN_XXL &&
                context.game().level().isPresent() &&
                context.level().world() != null;
        }
    };

    protected final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            handleGameVariantChange(get());
        }
    };

    protected final ObjectProperty<Node> pagePy = new SimpleObjectProperty<>(this, "page");
    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");

    protected final Map<GameVariant, GameConfiguration> gameConfigByVariant = new EnumMap<>(GameVariant.class);
    protected final JoypadKeyBinding[] joypadKeyBindings;
    protected final Keyboard keyboard = new Keyboard();
    protected final GameClockFX clock = new GameClockFX();
    protected final AssetStorage assets = new AssetStorage();
    protected final GameSound gameSound = new GameSound();
    protected final FlashMessageView flashMessageLayer = new FlashMessageView();
    protected final StackPane sceneRoot = new StackPane();

    protected Stage stage;
    protected Scene mainScene;

    protected StartPageCarousel startPagesCarousel;
    protected GamePage gamePage;
    protected EditorPage editorPage;

    protected boolean scoreVisible;
    protected Picker<String> textPickerGameOverTexts;
    protected Picker<String> textPickerLevelCompleteTexts;
    protected int selectedJoypadIndex;
    protected ArcadeKeyBinding arcadeKeys;

    public PacManGamesUI() {
        arcadeKeys = DEFAULT_ARCADE_KEY_BINDING;
        joypadKeyBindings = new JoypadKeyBinding[] { JOYPAD_CURSOR_KEYS, JOYPAD_WASD };
        selectedJoypadIndex = 0;
    }

    public void loadAssets() {
        ResourceManager rm = () -> PacManGamesUI.class;

        ResourceBundle textResources = rm.getModuleBundle("de.amr.games.pacman.ui2d.texts.messages");
        textPickerGameOverTexts      = Picker.fromBundle(textResources, "game.over");
        textPickerLevelCompleteTexts = Picker.fromBundle(textResources, "level.complete");
        assets.addBundle(textResources);

        assets.store("blue_sky_background",  Ufx.imageBackground(rm.loadImage("graphics/blue_sky.jpg")));
        assets.store("scene_background",     Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        assets.store("font.arcade",          rm.loadFont("fonts/emulogic.ttf", 8));
        assets.store("font.handwriting",     rm.loadFont("fonts/Molle-Italic.ttf", 9));
        assets.store("font.monospaced",      rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        assets.store("icon.auto",            rm.loadImage("graphics/icons/auto.png"));
        assets.store("icon.mute",            rm.loadImage("graphics/icons/mute.png"));
        assets.store("icon.pause",           rm.loadImage("graphics/icons/pause.png"));

        assets.store("voice.explain",        rm.url("sound/voice/press-key.mp3"));
        assets.store("voice.autopilot.off",  rm.url("sound/voice/autopilot-off.mp3"));
        assets.store("voice.autopilot.on",   rm.url("sound/voice/autopilot-on.mp3"));
        assets.store("voice.immunity.off",   rm.url("sound/voice/immunity-off.mp3"));
        assets.store("voice.immunity.on",    rm.url("sound/voice/immunity-on.mp3"));

        gameSound.setAssets(assets);
    }

    /**
     * Stores the configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param gameConfiguration the configuration for this variant
     */
    public void setGameConfiguration(GameVariant variant, GameConfiguration gameConfiguration) {
        assertNotNull(variant);
        assertNotNull(gameConfiguration);
        gameConfigByVariant.put(variant, gameConfiguration);
        gameConfiguration.initGameScenes(this);
        gameConfiguration.gameScenes()
            .filter(GameScene2D.class::isInstance)
            .map(GameScene2D.class::cast)
            .forEach(gameScene2D -> gameScene2D.debugInfoVisibleProperty().bind(GlobalProperties2d.PY_DEBUG_INFO_VISIBLE));
    }

    /**
     * Called from application start method (on JavaFX application thread).
     *
     * @param stage primary stage (window)
     * @param initialSize initial UI size
     */
    public void create(Stage stage, Dimension2D initialSize) {
        this.stage = assertNotNull(stage);
        assertNotNull(initialSize);

        mainScene = createMainScene(initialSize);
        stage.setScene(mainScene);

        startPagesCarousel = new StartPageCarousel(this);

        createGamePage(mainScene);

        clock.setPauseableCallback(this::runIfNotPausedOnEveryTick);
        clock.setPermanentCallback(this::runOnEveryTick);

        selectGameVariant(gameController().currentGameVariant());

        //TODO This doesn't fit for NES aspect ratio
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        bindStageTitle();
        stage.centerOnScreen();
        stage.setOnShowing(e -> selectStartPage());
    }

    public void setStartPage(GameVariant variant, Node page) {
        startPagesCarousel.addStartPage(variant, page);
    }

    public void show() {
        stage.show();
    }

    public Scene getMainScene() {
        return mainScene;
    }

    public ObjectProperty<GameVariant> gameVariantProperty() { return gameVariantPy; }

    /**
     * Executed on clock tick if game is not paused.
     */
    protected void runIfNotPausedOnEveryTick() {
        try {
            gameController().update();
            currentGameScene().ifPresent(GameScene::update);
            logUpdateResult();
        } catch (Exception x) {
            clock.stop();
            Logger.error("Something very bad happened, game clock stopped!");
            Logger.error(x);
        }
    }

    /**
     * Executed on clock tick even if game is paused.
     */
    protected void runOnEveryTick() {
        try {
            if (pagePy.get() == gamePage) {
                gamePage.draw();
                gamePage.updateDashboard();
                flashMessageLayer.update();
            } else {
                Logger.warn("Should not happen: Cannot handle tick when not on game page");
            }
        } catch (Exception x) {
            clock.stop();
            Logger.error("Something very bad happened, game clock stopped!");
            Logger.error(x);
        }
    }

    public void stop() {
        clock.stop();
    }

    protected Scene createMainScene(Dimension2D size) {
        Scene mainScene = new Scene(sceneRoot, size.getWidth(), size.getHeight());

        ImageView mutedIcon = createIcon(assets.get("icon.mute"), 48, sound().mutedProperty());
        ImageView autoIcon = createIcon(assets.get("icon.auto"), 48, GlobalProperties2d.PY_AUTOPILOT);
        var bottomRightIcons = new HBox(autoIcon, mutedIcon);
        bottomRightIcons.setMaxWidth(128);
        bottomRightIcons.setMaxHeight(64);
        bottomRightIcons.visibleProperty().bind(Bindings.createBooleanBinding(() -> pagePy.get() != editorPage, pagePy));
        StackPane.setAlignment(bottomRightIcons, Pos.BOTTOM_RIGHT);

        ImageView pauseIcon = createIcon(assets.get("icon.pause"), 64, clock.pausedPy);
        StackPane.setAlignment(pauseIcon, Pos.CENTER);
        pauseIcon.visibleProperty().bind(
                Bindings.createBooleanBinding(() -> pagePy.get() != editorPage && clock.isPaused(), pagePy, clock.pausedPy));

        sceneRoot.getChildren().addAll(new Pane(), flashMessageLayer, pauseIcon, bottomRightIcons);

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
        // Global keyboard shortcuts
        mainScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(true);
            } else if (e.getCode() == KeyCode.M && e.isAltDown()) {
                sound().toggleMuted();
            } else {
                if (pagePy.get() instanceof GameActionProvider actionProvider) {
                    actionProvider.handleInput(this);
                }
            }
        });

        mainScene.widthProperty() .addListener((py,ov,nv) -> gamePage.setSize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gamePage.setSize(mainScene.getWidth(), mainScene.getHeight()));

        return mainScene;
    }

    private EditorPage getOrCreateEditorPage() {
        if (editorPage == null) {
            editorPage = new EditorPage(stage, this, game().customMapDir());
            editorPage.setCloseAction(editor -> {
                editor.executeWithCheckForUnsavedChanges(this::bindStageTitle);
                editor.stop();
                clock.setTargetFrameRate(TICKS_PER_SECOND);
                gameController().restart(GameState.BOOT);
                selectStartPage();
            });
        }
        return editorPage;
    }

    protected void createGamePage(Scene parentScene) {
        gamePage = new GamePage(this, parentScene);
        gamePage.gameScenePy.bind(gameScenePy);
    }

    public void addDashboardItem(DashboardItemID id) {
        switch (id) {
            case README -> {
                InfoBox readMeBox = new InfoBoxReadmeFirst();
                readMeBox.setExpanded(true);
                addDashboardItem(locText("infobox.readme.title"), readMeBox);
            }
            case GENERAL -> addDashboardItem(locText("infobox.general.title"), new InfoBoxGeneral());
            case GAME_CONTROL -> addDashboardItem(locText("infobox.game_control.title"), new InfoBoxGameControl());
            case GAME_INFO -> addDashboardItem(locText("infobox.game_info.title"), new InfoBoxGameInfo());
            case ACTOR_INFO -> addDashboardItem(locText("infobox.actor_info.title"), new InfoBoxActorInfo());
            case KEYBOARD -> addDashboardItem(locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
            case JOYPAD -> addDashboardItem(locText("infobox.joypad.title"), new InfoBoxJoypad());
            case ABOUT -> addDashboardItem(locText("infobox.about.title"), new InfoBoxAbout());
        }
    }

    public void addDashboardItem(String title, InfoBox infoBox) {
        gamePage.dashboardLayer().addDashboardItem(title, infoBox);
    }

    private void onCustomMapSelectionModeChange(GameModel game) {
        // We cannot use data binding to the game model classes because the game models are in project
        // "pacman-core" which has no dependency to JavaFX data binding.
        if (game.mapSelectionMode() != CustomMapSelectionMode.NO_CUSTOM_MAPS) {
            gamePage.setActionToOpenEditor(actionOpenEditor);
            game.setMapSelectionMode(GlobalProperties2d.PY_MAP_SELECTION_MODE.get());
            GlobalProperties2d.PY_MAP_SELECTION_MODE.addListener((py, ov, selectionMode) -> game.setMapSelectionMode(selectionMode));
        } else {
            gamePage.setActionToOpenEditor(null);
        }
    }

    protected void handleGameVariantChange(GameVariant variant) {
        gameController().selectGame(variant);
        gameController().restart(GameState.BOOT);
        Logger.info("Selected game variant: {}", variant);

        GameModel game = gameController().gameModel(variant);
        game.addGameEventListener(this);
        game.addGameEventListener(gamePage.dashboardLayer().pipView());

        // TODO: this does not belongs here
        onCustomMapSelectionModeChange(game);

        String assetKeyPrefix = gameConfiguration().assetKeyPrefix();
        sceneRoot.setBackground(assets.get("scene_background"));
        Image icon = assets.image(assetKeyPrefix + ".icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        try {
            sound().setGameVariant(variant, gameConfiguration().assetKeyPrefix());
        } catch (Exception x) {
            Logger.error(x);
        }
        gamePage.canvasContainer().decorationEnabledPy.set(gameConfiguration().isGameCanvasDecorated());
    }

    protected void bindStageTitle() {
        stage.titleProperty().bind(Bindings.createStringBinding(
            () -> {
                // "app.title.pacman" vs. "app.title.pacman.paused"
                String key = "app.title." + gameConfiguration().assetKeyPrefix();
                if (clock.isPaused()) { key += ".paused"; }
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return locText(key, "2D") + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return locText(key, "2D");
            },
            clock.pausedPy, gameVariantPy, gameScenePy, gamePage.heightProperty())
        );
    }

    private void configureGameScene2D(GameScene2D gameScene2D) {
        gameScene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
    }

    protected void updateGameScene(boolean reloadCurrent) {
        GameScene prevGameScene = gameScenePy.get();
        GameScene nextGameScene = gameConfiguration().selectGameScene(this);
        boolean sceneChanging = nextGameScene != prevGameScene;
        if (reloadCurrent || sceneChanging) {
            if (prevGameScene != null) {
                prevGameScene.end();
                Logger.info("Game scene ended: {}", sceneDisplayName(prevGameScene));
            }
            if (nextGameScene != null) {
                if (nextGameScene instanceof GameScene2D gameScene2D) {
                    configureGameScene2D(gameScene2D);
                    gamePage.embedGameScene(nextGameScene);
                }
                nextGameScene.init();
                if (is2D3DSwitch(prevGameScene, nextGameScene)) {
                    nextGameScene.onSceneVariantSwitch(prevGameScene);
                }
            }
            if (sceneChanging) {
                gameScenePy.set(nextGameScene);
            }
            sceneRoot.setBackground(currentGameSceneHasID("PlayScene3D")
                ? assets.get("blue_sky_background")
                : assets.get("scene_background"));
            Logger.info("Game scene is now: {}", sceneDisplayName(nextGameScene));
        }
    }

    private String sceneDisplayName(GameScene gameScene) {
        String text = gameScene != null ? gameScene.getClass().getSimpleName() : "NO GAME SCENE";
        return text + " (%s)".formatted(gameVariant());
    }

    private boolean is2D3DSwitch(GameScene oldGameScene, GameScene newGameScene) {
        var cfg = gameConfiguration();
        return
            cfg.gameSceneHasID(oldGameScene, "PlayScene2D") &&
            cfg.gameSceneHasID(newGameScene, "PlayScene3D") ||
            cfg.gameSceneHasID(oldGameScene, "PlayScene3D") &&
            cfg.gameSceneHasID(newGameScene, "PlayScene2D");
    }

    private void logUpdateResult() {
        var messageList = game().eventLog().createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("Simulation step #{}:", clock.getUpdateCount());
            for (var msg : messageList) {
                Logger.info("- " + msg);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameContext interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public Keyboard keyboard() {
        return keyboard;
    }

    @Override
    public ArcadeKeyBinding arcadeKeys() {
        return arcadeKeys;
    }

    @Override
    public JoypadKeyBinding currentJoypadKeyBinding() {
        return joypadKeyBindings[selectedJoypadIndex];
    }

    @Override
    public void selectNextJoypadKeyBinding() {
        selectedJoypadIndex = selectedJoypadIndex + 1;
        if (selectedJoypadIndex == joypadKeyBindings.length) {
            selectedJoypadIndex = 0;
        }
    }

    @Override
    public void registerJoypadKeyBinding() {
        Logger.info("Enable joypad key binding {}", currentJoypadKeyBinding());
        currentJoypadKeyBinding().register(keyboard);
    }

    @Override
    public void unregisterJoypadKeyBinding() {
        Logger.info("Disable joypad key binding {}", currentJoypadKeyBinding());
        currentJoypadKeyBinding().unregister(keyboard);
    }

    @Override
    public String locGameOverMessage() {
        return textPickerGameOverTexts.next();
    }

    @Override
    public String locLevelCompleteMessage(int levelNumber) {
        return textPickerLevelCompleteTexts.next() + "\n\n" + locText("level_complete", levelNumber);
    }

    @Override
    public GameSound sound() {
        return gameSound;
    }

    @Override
    public GameClockFX gameClock() {
        return clock;
    }

    @Override
    public GamePage gamePage() {
        return gamePage;
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public GameVariant gameVariant() {
        return gameVariantPy.get();
    }

    @Override
    public GameConfiguration gameConfiguration(GameVariant variant) {
        return gameConfigByVariant.get(variant);
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
    }

    @Override
    public boolean currentGameSceneHasID(String sceneID) {
        if (currentGameScene().isEmpty()) {
            return false;
        }
        return gameConfiguration().gameSceneHasID(currentGameScene().get(), sceneID);
    }

    @Override
    public void togglePlayScene2D3D() {}

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisible = visible;
    }

    @Override
    public void selectGameVariant(GameVariant variant) {
        gameVariantPy.set(variant);
    }

    @Override
    public void selectPage(Node page) {
        Node selectedPage = pagePy.get();
        if (page != selectedPage) {
            if (selectedPage instanceof GameActionProvider actionProvider) {
                actionProvider.unregisterGameActionKeyBindings(keyboard());
            }
            if (page instanceof GameActionProvider actionProvider) {
                actionProvider.registerGameActionKeyBindings(keyboard());
            }
            pagePy.set(page);
            gamePage.setSize(stage.getScene().getWidth(), stage.getScene().getHeight());
            sceneRoot.getChildren().set(0, page);
            page.requestFocus();
        }
    }

    @Override
    public void selectStartPage() {
        clock.stop();
        gameScenePy.set(null);
        //TODO check this
        gamePage.dashboardLayer().hideDashboard();
        sceneRoot.setBackground(assets.get("scene_background"));
        if (startPagesCarousel.currentSlide() != null) {
            startPagesCarousel.currentSlide().requestFocus();
        }
        selectPage(startPagesCarousel);
    }

    @Override
    public void selectGamePage() {
        selectPage(gamePage);
        clock.start();
        if (gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            sound().playVoice("voice.explain", 0);
        }
        GameActions2D.BOOT.execute(this);
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        flashMessageLayer.showMessage(String.format(message, args), seconds);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("Received: {}", event);
        // call event specific hook method:
        GameEventListener.super.onGameEvent(event);
        if (pagePy.get() == gamePage) {
            updateGameScene(false);
            // dispatch event to current game scene if any
            currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
        }
    }

    @Override
    public void onCustomMapsChanged(GameEvent e) {
        //TODO find a cleaner solution
        gamePage.dashboardLayer().dashboardEntries().stream()
            .map(DashboardLayer.DashboardEntry::infoBox)
            .filter(infoBox -> infoBox instanceof InfoBoxCustomMaps)
            .findFirst()
            .ifPresent(infoBox -> ((InfoBoxCustomMaps)infoBox).updateTableView());
        Logger.info("Custom maps table updated");
    }

    @Override
    public void onGameVariantChanged(GameEvent event) {
        gameVariantPy.set(gameVariant());
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        gameConfiguration().createActorAnimations(level());
        sound().setEnabled(!game().isDemoLevel());
        // size of game scene might have changed, so re-embed
        currentGameScene().ifPresent(gamePage::embedGameScene);

        GameScene2D pipScene = gameConfiguration().createPiPScene(this, gamePage.canvasContainer().canvas());
        gamePage.dashboardLayer().pipView().setScene2D(pipScene);

        Logger.info("Game level {} ({}) created", level().number, gameVariant());
        Logger.info("Actor animations created");
        Logger.info("Sounds {}", sound().isEnabled() ? "enabled" : "disabled");
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        sound().stopAll();
    }
}