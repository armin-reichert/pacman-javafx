/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.ui.ActionBindingSupport.createBinding;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public interface PacManGames_UI {

    Map<GameAction, Set<KeyCombination>> COMMON_ACTION_BINDINGS = Map.ofEntries(
        createBinding(ACTION_ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        createBinding(ACTION_ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        createBinding(ACTION_BOOT_SHOW_GAME_VIEW,     nude(KeyCode.F3)),
        createBinding(ACTION_CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        createBinding(ACTION_CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        createBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        createBinding(ACTION_CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        createBinding(ACTION_ENTER_FULLSCREEN,        nude(KeyCode.F11)),
        createBinding(ACTION_PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        createBinding(ACTION_PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        createBinding(ACTION_SHOW_HELP,               nude(KeyCode.H)),
        createBinding(ACTION_STEER_UP,                nude(KeyCode.UP), control(KeyCode.UP)),
        createBinding(ACTION_STEER_DOWN,              nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        createBinding(ACTION_STEER_LEFT,              nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        createBinding(ACTION_STEER_RIGHT,             nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        createBinding(ACTION_QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        createBinding(ACTION_SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        createBinding(ACTION_SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        createBinding(ACTION_SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        createBinding(ACTION_SIMULATION_ONE_STEP,     shift(KeyCode.P), shift(KeyCode.F5)),
        createBinding(ACTION_SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        createBinding(ACTION_TEST_CUT_SCENES,         alt(KeyCode.C)),
        createBinding(ACTION_TEST_LEVELS_BONI,        alt(KeyCode.T)),
        createBinding(ACTION_TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        createBinding(ACTION_TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        createBinding(ACTION_TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        createBinding(ACTION_TOGGLE_MUTED,            alt(KeyCode.M)),
        createBinding(ACTION_TOGGLE_PAUSED,           nude(KeyCode.P), nude(KeyCode.F5)),
        createBinding(ACTION_TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        createBinding(ACTION_TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        createBinding(ACTION_TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        createBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        createBinding(ACTION_TOGGLE_DRAW_MODE,        alt(KeyCode.W))
    );

    Color DEBUG_TEXT_FILL          = Color.YELLOW;
    Font DEBUG_TEXT_FONT           = Font.font("Sans", FontWeight.BOLD, 16);

    double MAX_SCENE_2D_SCALING    = 5;

    Color STATUS_ICON_COLOR        = Color.LIGHTGRAY;
    byte STATUS_ICON_PADDING       = 10;
    byte STATUS_ICON_SIZE          = 24;
    byte STATUS_ICON_SPACING       = 5;

    // Global key combinations
    KeyCombination KEY_FULLSCREEN  = nude(KeyCode.F11);
    KeyCombination KEY_MUTE        = alt(KeyCode.M);
    KeyCombination KEY_OPEN_EDITOR = alt_shift(KeyCode.E);

    // Global properties
    ObjectProperty<Color>         PY_CANVAS_BG_COLOR        = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty               PY_CANVAS_FONT_SMOOTHING  = new SimpleBooleanProperty(false);
    BooleanProperty               PY_CANVAS_IMAGE_SMOOTHING = new SimpleBooleanProperty(false);
    BooleanProperty               PY_DEBUG_INFO_VISIBLE     = new SimpleBooleanProperty(false);
    BooleanProperty               PY_IMMUNITY               = new SimpleBooleanProperty(false);
    IntegerProperty               PY_PIP_HEIGHT             = new SimpleIntegerProperty(400);
    BooleanProperty               PY_PIP_ON                 = new SimpleBooleanProperty(false);
    IntegerProperty               PY_PIP_OPACITY_PERCENT    = new SimpleIntegerProperty(100);
    IntegerProperty               PY_SIMULATION_STEPS       = new SimpleIntegerProperty(1);
    BooleanProperty               PY_USING_AUTOPILOT        = new SimpleBooleanProperty(false);
    BooleanProperty               PY_3D_AXES_VISIBLE        = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>      PY_3D_DRAW_MODE           = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty               PY_3D_ENABLED             = new SimpleBooleanProperty(false);
    BooleanProperty               PY_3D_ENERGIZER_EXPLODES  = new SimpleBooleanProperty(true);
    ObjectProperty<Color>         PY_3D_FLOOR_COLOR         = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color>         PY_3D_LIGHT_COLOR         = new SimpleObjectProperty<>(Color.WHITE);
    BooleanProperty               PY_3D_PAC_LIGHT_ENABLED   = new SimpleBooleanProperty(true);
    ObjectProperty<PerspectiveID> PY_3D_PERSPECTIVE         = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    DoubleProperty                PY_3D_WALL_HEIGHT         = new SimpleDoubleProperty(3.5);
    DoubleProperty                PY_3D_WALL_OPACITY        = new SimpleDoubleProperty(1.0);

    void restart();
    void selectGameVariant(String variant);
    void show();

    // UI configuration
    PacManGames_UIConfig configuration(String gameVariant);
    PacManGames_UIConfig configuration();
    void setConfiguration(String variant, PacManGames_UIConfig configuration);

    // Game scenes
    ObjectProperty<GameScene> currentGameSceneProperty();
    Optional<GameScene> currentGameScene();
    void setCurrentGameScene(GameScene gameScene);
    boolean currentGameSceneIsPlayScene2D();
    boolean currentGameSceneIsPlayScene3D();
    void updateGameScene(boolean reload);

    // Views
    ObjectProperty<PacManGames_View> currentViewProperty();
    PacManGames_View currentView();
    GameView gameView();
    StartPagesView startPagesView();
    void showEditorView();
    void showGameView();
    void showStartView();

    // Flash messages
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1.5, message, args); }
    void showFlashMessageSec(double seconds, String message, Object... args);
}