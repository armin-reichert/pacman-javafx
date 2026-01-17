/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.VoicePlayer;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.LocalizedTextAccessor;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.createImageBackground;

public interface GameUI extends LocalizedTextAccessor {

    Keyboard KEYBOARD = new Keyboard();

    Set<ActionBinding> CHEAT_BINDINGS = Set.of(
        new ActionBinding(CheatActions.ACTION_EAT_ALL_PELLETS,  alt(KeyCode.E)),
        new ActionBinding(CheatActions.ACTION_ADD_LIVES,        alt(KeyCode.L)),
        new ActionBinding(CheatActions.ACTION_ENTER_NEXT_LEVEL, alt(KeyCode.N)),
        new ActionBinding(CheatActions.ACTION_KILL_GHOSTS,      alt(KeyCode.X))
    );

    Set<ActionBinding> STEERING_BINDINGS = Set.of(
        new ActionBinding(ACTION_STEER_UP,    bare(KeyCode.UP),    control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,  bare(KeyCode.DOWN),  control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,  bare(KeyCode.LEFT),  control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT, bare(KeyCode.RIGHT), control(KeyCode.RIGHT))
    );

    Set<ActionBinding> SCENE_TESTS_BINDINGS = Set.of(
        new ActionBinding(TestActions.ACTION_CUT_SCENES_TEST,      alt(KeyCode.C)),
        new ActionBinding(TestActions.ACTION_SHORT_LEVEL_TEST,     alt(KeyCode.T)),
        new ActionBinding(TestActions.ACTION_MEDIUM_LEVEL_TEST,    alt_shift(KeyCode.T))
    );

    Set<ActionBinding> PLAY_3D_BINDINGS = Set.of(
        new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,             alt(KeyCode.LEFT)),
        new ActionBinding(ACTION_PERSPECTIVE_NEXT,                 alt(KeyCode.RIGHT)),
        new ActionBinding(ACTION_TOGGLE_DRAW_MODE,                 alt(KeyCode.W))
    );

    Set<ActionBinding> COMMON_BINDINGS = Set.of(
        new ActionBinding(ACTION_BOOT_SHOW_PLAY_VIEW,              bare(KeyCode.F3)),
        new ActionBinding(ACTION_ENTER_FULLSCREEN,                 bare(KeyCode.F11)),
        new ActionBinding(ACTION_OPEN_EDITOR,                      alt_shift(KeyCode.E)),
        new ActionBinding(ACTION_SHOW_HELP,                        bare(KeyCode.H)),
        new ActionBinding(ACTION_QUIT_GAME_SCENE,                  bare(KeyCode.Q)),
        new ActionBinding(ACTION_SIMULATION_SLOWER,                alt(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_SLOWEST,               alt_shift(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_FASTER,                alt(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_FASTEST,               alt_shift(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_RESET,                 alt(KeyCode.DIGIT0)),
        new ActionBinding(ACTION_SIMULATION_ONE_STEP,              shift(KeyCode.P), shift(KeyCode.F5)),
        new ActionBinding(ACTION_SIMULATION_TEN_STEPS,             shift(KeyCode.SPACE)),
        new ActionBinding(ACTION_TOGGLE_AUTOPILOT,                 alt(KeyCode.A)),
        new ActionBinding(ACTION_TOGGLE_COLLISION_STRATEGY,        alt(KeyCode.S)),
        new ActionBinding(ACTION_TOGGLE_DEBUG_INFO,                alt(KeyCode.D)),
        new ActionBinding(ACTION_TOGGLE_MUTED,                     alt(KeyCode.M)),
        new ActionBinding(ACTION_TOGGLE_PAUSED,                    bare(KeyCode.P), bare(KeyCode.F5)),
        new ActionBinding(ACTION_TOGGLE_DASHBOARD,                 bare(KeyCode.F1), alt(KeyCode.B)),
        new ActionBinding(ACTION_TOGGLE_IMMUNITY,                  alt(KeyCode.I)),
        new ActionBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY,      bare(KeyCode.F2)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,          alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))
    );

    ObjectProperty<Color>         PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty               PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);
    BooleanProperty               PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);
    IntegerProperty               PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);
    BooleanProperty               PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);
    IntegerProperty               PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);
    BooleanProperty               PROPERTY_MUTED = new SimpleBooleanProperty(false);
    IntegerProperty               PROPERTY_SIMULATION_STEPS = new SimpleIntegerProperty(1);
    BooleanProperty               PROPERTY_3D_AXES_VISIBLE = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>      PROPERTY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty               PROPERTY_3D_ENABLED = new SimpleBooleanProperty(false);
    ObjectProperty<Color>         PROPERTY_3D_FLOOR_COLOR = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color>         PROPERTY_3D_LIGHT_COLOR = new SimpleObjectProperty<>(Color.WHITE);
    ObjectProperty<PerspectiveID> PROPERTY_3D_PERSPECTIVE_ID = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    DoubleProperty                PROPERTY_3D_WALL_HEIGHT = new SimpleDoubleProperty();
    DoubleProperty                PROPERTY_3D_WALL_OPACITY = new SimpleDoubleProperty(1.0);

    ResourceManager GLOBAL_RESOURCES = () -> GameUI_Implementation.class;

    Media VOICE_IMMUNITY_ON        = GLOBAL_RESOURCES.loadMedia("sound/voice/immunity-on.mp3");
    Media VOICE_IMMUNITY_OFF       = GLOBAL_RESOURCES.loadMedia("sound/voice/immunity-off.mp3");
    Media VOICE_EXPLAIN_GAME_START = GLOBAL_RESOURCES.loadMedia("sound/voice/press-key.mp3");
    Media VOICE_AUTOPILOT_ON       = GLOBAL_RESOURCES.loadMedia("sound/voice/autopilot-on.mp3");
    Media VOICE_AUTOPILOT_OFF      = GLOBAL_RESOURCES.loadMedia("sound/voice/autopilot-off.mp3");

    Font FONT_PAC_FONT_GOOD        = GLOBAL_RESOURCES.loadFont("fonts/PacfontGood.ttf", 8);
    Font FONT_PAC_FONT             = GLOBAL_RESOURCES.loadFont("fonts/Pacfont.ttf", 8);
    Font FONT_CONDENSED            = GLOBAL_RESOURCES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12);
    Font FONT_MONOSPACED           = GLOBAL_RESOURCES.loadFont("fonts/fantasquesansmono-bold.otf", 12);
    Font FONT_HANDWRITING          = GLOBAL_RESOURCES.loadFont("fonts/Molle-Italic.ttf", 9);
    Font FONT_ARCADE_8             = GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 8);
    Font FONT_ARCADE_6             = GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 6);

    Background BACKGROUND_PAC_MAN_WALLPAPER = createImageBackground(GLOBAL_RESOURCES.loadImage("graphics/pacman_wallpaper.png"));

    ResourceBundle LOCALIZED_TEXTS = GLOBAL_RESOURCES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

    String STYLE_SHEET_PATH = "/de/amr/pacmanfx/ui/css/style.css";

    /**
     * @return watchdog process observing the directory where user-defined maps are stored
     */
    DirectoryWatchdog customDirWatchdog();

    /**
     * @return the clock driving the game
     */
    GameClock clock();

    /**
     * @return (non UI) context
     */
    GameContext context();

    /**
     * @return the primary stage provided by the JavaFX application
     */
    Stage stage();

    /**
     * @return the UI preferences (stored permanently in platform-specific way)
     */
    UIPreferences preferences();

    /**
     * @return voice player if this UI. Only one voice at a time can be played.
     */
    VoicePlayer voicePlayer();

    // Messages

    /** Default duration a flash message appears on the screen. */
    Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

    /**
     * Shows a message on the screen that slowly fades out.
     *
     * @param duration display duration before fading out
     * @param message the message text, may include formatting symbols (see {@link String#format(String, Object...)})
     * @param args arguments merged into the message text using String.format()
     */
    void showFlashMessage(Duration duration, String message, Object... args);

    /**
     * Shows a message on the screen that slowly fades out and displays for a default duration.
     *
     * @param message the message text
     * @param args arguments merged into the message text using {@link String#format(String, Object...)}
     */
    default void showFlashMessage(String message, Object... args) {
        showFlashMessage(DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }

    // Scene access

    GameScene_Config currentGameSceneConfig();

    Optional<GameScene> currentGameScene();

    boolean isCurrentGameSceneID(GameScene_Config.SceneID sceneID);

    void updateGameScene(boolean forceReloading);

    // View access

    GameUI_ViewManager views();

    void showEditorView();

    void showPlayView();

    void showStartView();

    // Config

    GameUI_ConfigFactory configFactory();

    /**
     * @param gameVariantName name of game variant
     * @return UI configuration for given game variant
     */
    GameUI_Config config(String gameVariantName);

    /**
     * @return UI configuration for the current game
     * @param <T> type of UI configuration
     */
    <T extends GameUI_Config> T currentConfig();

    // Lifecycle

    /**
     * Quits the current game scene (if any) and displays the start page for the current game.
     */
    void quitCurrentGameScene();

    /**
     * Resets the game clock to normal speed and shows the boot screen for the selected game.
     */
    void restart();

    /**
     * Shows the start page for the given game variant, loads its resources and initializes the game model.
     *
     * @param gameVariantName game variant name ("PACMAN", "MS_PACMAN" etc.)
     */
    void selectGameVariant(String gameVariantName);

    /**
     * Shows the UI centered on the screen and displays the first start page.
     */
    void show();

    /**
     * Terminates the game and stops the game clock. Called when the application is terminated by closing the stage.
     */
    void terminate();
}