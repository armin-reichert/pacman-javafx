/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.beans.property.*;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public interface GameUI {

    double DEFAULT_FLASH_MESSAGE_SECONDS = 1.5;

    static GameUI theUI() { return PacManGames_UI_Impl.THE_ONE; }

    ObjectProperty<Color>            PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty                  PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);
    ObjectProperty<GameScene>        PROPERTY_CURRENT_GAME_SCENE = new SimpleObjectProperty<>();
    ObjectProperty<PacManGames_View> PROPERTY_CURRENT_VIEW = new SimpleObjectProperty<>();
    BooleanProperty                  PROPERTY_CANVAS_IMAGE_SMOOTHING = new SimpleBooleanProperty(false);
    BooleanProperty                  PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);
    IntegerProperty                  PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);
    BooleanProperty                  PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);
    IntegerProperty                  PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);
    BooleanProperty                  PROPERTY_MUTED = new SimpleBooleanProperty(false);
    IntegerProperty                  PROPERTY_SIMULATION_STEPS = new SimpleIntegerProperty(1);
    BooleanProperty                  PROPERTY_3D_AXES_VISIBLE = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>         PROPERTY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty                  PROPERTY_3D_ENABLED = new SimpleBooleanProperty(false);
    ObjectProperty<Color>            PROPERTY_3D_FLOOR_COLOR = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color>            PROPERTY_3D_LIGHT_COLOR = new SimpleObjectProperty<>(Color.WHITE);
    ObjectProperty<PerspectiveID>    PROPERTY_3D_PERSPECTIVE = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    DoubleProperty                   PROPERTY_3D_WALL_HEIGHT = new SimpleDoubleProperty();
    DoubleProperty                   PROPERTY_3D_WALL_OPACITY = new SimpleDoubleProperty(1.0);

    List<ActionBinding> DEFAULT_ACTION_BINDINGS = List.of(
        new ActionBinding(ACTION_ARCADE_INSERT_COIN,          nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        new ActionBinding(ACTION_ARCADE_START_GAME,           nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        new ActionBinding(ACTION_BOOT_SHOW_PLAY_VIEW,         nude(KeyCode.F3)),
        new ActionBinding(ACTION_CHEAT_EAT_ALL_PELLETS,       alt(KeyCode.E)),
        new ActionBinding(ACTION_CHEAT_ADD_LIVES,             alt(KeyCode.L)),
        new ActionBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,      alt(KeyCode.N)),
        new ActionBinding(ACTION_CHEAT_KILL_GHOSTS,           alt(KeyCode.X)),
        new ActionBinding(ACTION_ENTER_FULLSCREEN,            nude(KeyCode.F11)),
        new ActionBinding(ACTION_OPEN_EDITOR,                 alt_shift(KeyCode.E)),
        new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,        alt(KeyCode.LEFT)),
        new ActionBinding(ACTION_PERSPECTIVE_NEXT,            alt(KeyCode.RIGHT)),
        new ActionBinding(ACTION_SHOW_HELP,                   nude(KeyCode.H)),
        new ActionBinding(ACTION_STEER_UP,                    nude(KeyCode.UP), control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,                  nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,                  nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT,                 nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        new ActionBinding(ACTION_QUIT_GAME_SCENE,             nude(KeyCode.Q)),
        new ActionBinding(ACTION_SIMULATION_SLOWER,           alt(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_FASTER,           alt(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_RESET,            alt(KeyCode.DIGIT0)),
        new ActionBinding(ACTION_SIMULATION_ONE_STEP,         shift(KeyCode.P), shift(KeyCode.F5)),
        new ActionBinding(ACTION_SIMULATION_TEN_STEPS,        shift(KeyCode.SPACE)),
        new ActionBinding(ACTION_TEST_CUT_SCENES,             alt(KeyCode.C)),
        new ActionBinding(ACTION_TEST_LEVELS_BONI,            alt(KeyCode.T)),
        new ActionBinding(ACTION_TEST_LEVELS_TEASERS,         alt_shift(KeyCode.T)),
        new ActionBinding(ACTION_TOGGLE_AUTOPILOT,            alt(KeyCode.A)),
        new ActionBinding(ACTION_TOGGLE_DEBUG_INFO,           alt(KeyCode.D)),
        new ActionBinding(ACTION_TOGGLE_MUTED,                alt(KeyCode.M)),
        new ActionBinding(ACTION_TOGGLE_PAUSED,               nude(KeyCode.P), nude(KeyCode.F5)),
        new ActionBinding(ACTION_TOGGLE_DASHBOARD,            nude(KeyCode.F1), alt(KeyCode.B)),
        new ActionBinding(ACTION_TOGGLE_IMMUNITY,             alt(KeyCode.I)),
        new ActionBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, nude(KeyCode.F2)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,     alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        new ActionBinding(ACTION_TOGGLE_DRAW_MODE,            alt(KeyCode.W))
    );

    PacManGames_Assets          theAssets();
    <T extends GameUI_Config> T theConfiguration();
    DirectoryWatchdog           theCustomDirWatchdog();
    GameClock                   theGameClock();
    GameContext                 theGameContext();
    Joypad                      theJoypad();
    Keyboard                    theKeyboard();
    SoundManager                theSound();
    Stage                       theStage();
    UIPreferences               theUIPrefs();

    void restart();
    void selectGameVariant(String variant);
    void show();

    /**
     * @param gameVariant name of game variant
     * @return UI configuration for given game variant
     */
    GameUI_Config config(String gameVariant);

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param config the UI configuration for this variant
     */
    void setConfig(String variant, GameUI_Config config);

    // Game scenes
    Optional<GameScene> currentGameScene();
    boolean isCurrentGameSceneID(String id);
    void updateGameScene(boolean reload);

    // Views
    PacManGames_View      currentView();
    PlayView              thePlayView();
    StartPagesView        theStartPagesView();
    Optional<EditorView>  theEditorView();

    void showEditorView();
    void showPlayView();
    void showStartView();

    // Flash messages
    void showFlashMessageSec(double seconds, String message, Object... args);
    default void showFlashMessage(String message, Object... args) {
        showFlashMessageSec(DEFAULT_FLASH_MESSAGE_SECONDS, message, args);
    }

    void terminate();

    /**
     * @param titleKey resource bundle key of title text
     * @return CustomMenuItem representing a context menu title item
     */
    default MenuItem createContextMenuTitle(String titleKey) {
        Font font = theUIPrefs().getFont("context_menu.title.font");
        Color fillColor = theUIPrefs().getColor("context_menu.title.fill");
        var text = new Text(theAssets().text(titleKey));
        text.setFont(font);
        text.setFill(fillColor);
        text.getStyleClass().add("custom-menu-title");
        return new CustomMenuItem(text);
    }
}