/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui._3d.Perspective;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.ActionBindingSupport.binding;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public interface PacManGames_UI {

    static PacManGames_UI_Builder build(Stage stage, double width, double height) {
        return new PacManGames_UI_Builder(stage, width, height);
    }

    /** Predefined game variants */
    enum GameVariant {
        MS_PACMAN, MS_PACMAN_TENGEN, MS_PACMAN_XXL, PACMAN, PACMAN_XXL
    }

    float BONUS_3D_SYMBOL_WIDTH       = TS;
    float BONUS_3D_POINTS_WIDTH       = 1.8f * TS;
    float ENERGIZER_3D_MIN_SCALING    = 0.2f;
    float ENERGIZER_3D_MAX_SCALING    = 1.0f;
    float ENERGIZER_3D_RADIUS         = 3.5f;
    float FLOOR_3D_PADDING            = 5.0f;
    float FLOOR_3D_THICKNESS          = 0.5f;
    float GHOST_3D_SIZE               = 16.0f;
    float HOUSE_3D_BASE_HEIGHT        = 12.0f;
    float HOUSE_3D_OPACITY            = 0.4f;
    float HOUSE_3D_SENSITIVITY        = 1.5f * TS;
    float HOUSE_3D_WALL_THICKNESS     = 2.5f;
    byte  LIVES_COUNTER_3D_CAPACITY   = 5;
    float LIVES_COUNTER_3D_SHAPE_SIZE = 12f;
    Color LIVES_COUNTER_PILLAR_COLOR  = Color.grayRgb(120);
    Color LIVES_COUNTER_PLATE_COLOR   = Color.grayRgb(180);
    float OBSTACLE_3D_BASE_HEIGHT     = 4.0f;
    float OBSTACLE_3D_WALL_THICKNESS  = 2.25f;
    float PAC_3D_SIZE                 = 17.0f;
    float PELLET_3D_RADIUS            = 1.0f;

    // Global key combinations and action bindings
    KeyCombination KEY_FULLSCREEN  = nude(KeyCode.F11);
    KeyCombination KEY_MUTE        = alt(KeyCode.M);
    KeyCombination KEY_OPEN_EDITOR = alt_shift(KeyCode.E);

    Map<GameAction, Set<KeyCombination>> GLOBAL_ACTION_BINDING_MAP = Map.ofEntries(
        binding(ACTION_ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        binding(ACTION_ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        binding(ACTION_BOOT_SHOW_GAME_VIEW,     nude(KeyCode.F3)),
        binding(ACTION_CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        binding(ACTION_CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        binding(ACTION_CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        binding(ACTION_CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        binding(ACTION_ENTER_FULLSCREEN,        nude(KeyCode.F11)),
        binding(ACTION_PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        binding(ACTION_PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        binding(ACTION_SHOW_HELP,               nude(KeyCode.H)),
        binding(ACTION_STEER_UP,                nude(KeyCode.UP), control(KeyCode.UP)),
        binding(ACTION_STEER_DOWN,              nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        binding(ACTION_STEER_LEFT,              nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        binding(ACTION_STEER_RIGHT,             nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        binding(ACTION_QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        binding(ACTION_SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        binding(ACTION_SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        binding(ACTION_SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        binding(ACTION_SIMULATION_ONE_STEP,     shift(KeyCode.P), shift(KeyCode.F5)),
        binding(ACTION_SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        binding(ACTION_TEST_CUT_SCENES,         alt(KeyCode.C)),
        binding(ACTION_TEST_LEVELS_BONI,        alt(KeyCode.T)),
        binding(ACTION_TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        binding(ACTION_TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        binding(ACTION_TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        binding(ACTION_TOGGLE_MUTED,            alt(KeyCode.M)),
        binding(ACTION_TOGGLE_PAUSED,           nude(KeyCode.P), nude(KeyCode.F5)),
        binding(ACTION_TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        binding(ACTION_TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        binding(ACTION_TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        binding(ACTION_TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        binding(ACTION_TOGGLE_DRAW_MODE,        alt(KeyCode.W))
    );

    Color DEBUG_TEXT_FILL          = Color.YELLOW;
    Font DEBUG_TEXT_FONT           = Font.font("Sans", FontWeight.BOLD, 16);

    double MAX_SCENE_2D_SCALING    = 5;

    Color STATUS_ICON_COLOR        = Color.LIGHTGRAY;
    byte STATUS_ICON_PADDING       = 10;
    byte STATUS_ICON_SIZE          = 24;
    byte STATUS_ICON_SPACING       = 5;

    // Global properties
    ObjectProperty<Color>           PY_CANVAS_BG_COLOR        = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty                 PY_CANVAS_FONT_SMOOTHING  = new SimpleBooleanProperty(false);
    BooleanProperty                 PY_CANVAS_IMAGE_SMOOTHING = new SimpleBooleanProperty(false);
    BooleanProperty                 PY_DEBUG_INFO_VISIBLE     = new SimpleBooleanProperty(false);
    BooleanProperty                 PY_IMMUNITY               = new SimpleBooleanProperty(false);
    IntegerProperty                 PY_PIP_HEIGHT             = new SimpleIntegerProperty(400);
    BooleanProperty PY_MINI_VIEW_ON = new SimpleBooleanProperty(false);
    IntegerProperty                 PY_PIP_OPACITY_PERCENT    = new SimpleIntegerProperty(69);
    IntegerProperty                 PY_SIMULATION_STEPS       = new SimpleIntegerProperty(1);
    BooleanProperty                 PY_USING_AUTOPILOT        = new SimpleBooleanProperty(false);
    BooleanProperty                 PY_3D_AXES_VISIBLE        = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>        PY_3D_DRAW_MODE           = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty                 PY_3D_ENABLED             = new SimpleBooleanProperty(false);
    BooleanProperty                 PY_3D_ENERGIZER_EXPLODES  = new SimpleBooleanProperty(true);
    ObjectProperty<Color>           PY_3D_FLOOR_COLOR         = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color>           PY_3D_LIGHT_COLOR         = new SimpleObjectProperty<>(Color.WHITE);
    BooleanProperty                 PY_3D_PAC_LIGHT_ENABLED   = new SimpleBooleanProperty(true);
    ObjectProperty<Perspective.ID> PY_3D_PERSPECTIVE         = new SimpleObjectProperty<>(Perspective.ID.TRACK_PLAYER);
    DoubleProperty                 PY_3D_WALL_HEIGHT         = new SimpleDoubleProperty(OBSTACLE_3D_BASE_HEIGHT);
    DoubleProperty                 PY_3D_WALL_OPACITY        = new SimpleDoubleProperty(1.0);

    Stage stage();
    void restart();
    void selectGameVariant(String variant);
    void show();

    // UI configuration
    PacManGames_UIConfig configuration(String gameVariant);
    PacManGames_UIConfig configuration();
    void setConfiguration(String variant, PacManGames_UIConfig configuration);

    BooleanProperty mutedProperty();

    Model3DRepository model3DRepository();

    // Game scenes
    ObjectProperty<GameScene> currentGameSceneProperty();
    Optional<GameScene> currentGameScene();
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

    void terminateApp();
}