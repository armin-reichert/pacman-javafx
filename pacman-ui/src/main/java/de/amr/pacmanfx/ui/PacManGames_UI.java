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
import static de.amr.pacmanfx.uilib.input.Keyboard.*;

public interface PacManGames_UI {
    Font DEBUG_TEXT_FONT           = Font.font("Sans", FontWeight.BOLD, 18);
    int LIVES_COUNTER_MAX          = 5;
    double MAX_SCENE_2D_SCALING    = 5;

    double BONUS_3D_SYMBOL_WIDTH  = TS;
    double BONUS_3D_POINTS_WIDTH  = 1.8 * TS;

    float ENERGIZER_3D_RADIUS      = 3.5f;
    float FLOOR_3D_THICKNESS       = 0.5f;
    float GHOST_3D_SIZE            = 16.0f;
    float HOUSE_3D_BASE_HEIGHT     = 12.0f;
    float HOUSE_3D_WALL_TOP_HEIGHT = 0.1f;
    float HOUSE_3D_WALL_THICKNESS  = 1.5f;
    float HOUSE_3D_OPACITY         = 0.4f;
    float HOUSE_3D_SENSITIVITY     = 1.5f * TS;
    float LIVES_COUNTER_3D_SIZE    = 12f;
    float OBSTACLE_3D_BASE_HEIGHT  = 7.0f;
    float OBSTACLE_3D_TOP_HEIGHT   = 0.1f;
    float OBSTACLE_3D_THICKNESS    = 1.25f;
    float PAC_3D_SIZE              = 17.0f;
    float PELLET_3D_RADIUS         = 1.0f;

    // Global key combinations
    KeyCombination KEY_FULLSCREEN  = nude(KeyCode.F11);
    KeyCombination KEY_MUTE        = alt(KeyCode.M);
    KeyCombination KEY_OPEN_EDITOR = alt_shift(KeyCode.E);

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