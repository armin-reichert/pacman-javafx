/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;

public interface PacManGames_UI {
    public static final Font DEBUG_TEXT_FONT           = Font.font("Sans", FontWeight.BOLD, 18);
    public static final int LIVES_COUNTER_MAX          = 5;
    public static final double MAX_SCENE_2D_SCALING    = 5;
    public static final Color STATUS_ICON_COLOR        = Color.LIGHTGRAY;
    public static final byte STATUS_ICON_SIZE          = 24;
    public static final byte STATUS_ICON_SPACING       = 5;
    public static final byte STATUS_ICON_PADDING       = 10;

    public static final double BONUS_3D_SYMBOL_WIDTH  = TS;
    public static final double BONUS_3D_POINTS_WIDTH  = 1.8 * TS;

    public static final float ENERGIZER_3D_RADIUS      = 3.5f;
    public static final float FLOOR_3D_THICKNESS       = 0.5f;
    public static final float GHOST_3D_SIZE            = 16.0f;
    public static final float HOUSE_3D_BASE_HEIGHT     = 12.0f;
    public static final float HOUSE_3D_WALL_TOP_HEIGHT = 0.1f;
    public static final float HOUSE_3D_WALL_THICKNESS  = 1.5f;
    public static final float HOUSE_3D_OPACITY         = 0.4f;
    public static final float HOUSE_3D_SENSITIVITY     = 1.5f * TS;
    public static final float LIVES_COUNTER_3D_SIZE    = 12f;
    public static final float OBSTACLE_3D_BASE_HEIGHT  = 7.0f;
    public static final float OBSTACLE_3D_TOP_HEIGHT   = 0.1f;
    public static final float OBSTACLE_3D_THICKNESS    = 1.25f;
    public static final float PAC_3D_SIZE              = 17.0f;
    public static final float PELLET_3D_RADIUS         = 1.0f;

    // Global key combinations
    public static final KeyCombination KEY_FULLSCREEN  = Keyboard.nude(KeyCode.F11);
    public static final KeyCombination KEY_MUTE        = Keyboard.alt(KeyCode.M);
    public static final KeyCombination KEY_OPEN_EDITOR = Keyboard.alt_shift(KeyCode.E);

    public static final ObjectProperty<Color>    PY_CANVAS_BG_COLOR        = new SimpleObjectProperty<>(Color.BLACK);
    public static final BooleanProperty PY_CANVAS_FONT_SMOOTHING  = new SimpleBooleanProperty(false);
    public static final BooleanProperty          PY_CANVAS_IMAGE_SMOOTHING = new SimpleBooleanProperty(false);
    public static final BooleanProperty          PY_DEBUG_INFO_VISIBLE     = new SimpleBooleanProperty(false);
    public static final BooleanProperty          PY_IMMUNITY               = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_HEIGHT             = new SimpleIntegerProperty(400);
    public static final BooleanProperty          PY_PIP_ON                 = new SimpleBooleanProperty(false);
    public static final IntegerProperty          PY_PIP_OPACITY_PERCENT    = new SimpleIntegerProperty(100);
    public static final IntegerProperty          PY_SIMULATION_STEPS       = new SimpleIntegerProperty(1);
    public static final BooleanProperty          PY_USING_AUTOPILOT        = new SimpleBooleanProperty(false);

    public static final BooleanProperty          PY_3D_AXES_VISIBLE        = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode> PY_3D_DRAW_MODE           = new SimpleObjectProperty<>(DrawMode.FILL);
    public static final BooleanProperty          PY_3D_ENABLED             = new SimpleBooleanProperty(false);
    public static final BooleanProperty          PY_3D_ENERGIZER_EXPLODES  = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Color>    PY_3D_FLOOR_COLOR         = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    public static final ObjectProperty<Color>    PY_3D_LIGHT_COLOR         = new SimpleObjectProperty<>(Color.WHITE);
    public static final BooleanProperty          PY_3D_PAC_LIGHT_ENABLED   = new SimpleBooleanProperty(true);
    public static final ObjectProperty<PerspectiveID> PY_3D_PERSPECTIVE    = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    public static final DoubleProperty           PY_3D_WALL_HEIGHT         = new SimpleDoubleProperty(3.5);
    public static final DoubleProperty           PY_3D_WALL_OPACITY        = new SimpleDoubleProperty(1.0);

    void buildUI(Stage stage, double width, double height, DashboardID... dashboardIDs);
    void restart();
    void selectGameVariant(GameVariant variant);
    void show();

    // Configuration
    PacManGames_UIConfiguration configuration(GameVariant gameVariant);
    PacManGames_UIConfiguration configuration();
    void setConfiguration(GameVariant variant, PacManGames_UIConfiguration configuration);

    // Game scenes
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();
    boolean currentGameSceneIsPlayScene2D();
    boolean currentGameSceneIsPlayScene3D();
    boolean currentGameSceneIs2D();
    void updateGameScene(boolean reload);

    // Views
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