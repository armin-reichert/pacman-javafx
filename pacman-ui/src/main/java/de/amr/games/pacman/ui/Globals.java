/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.ui._3d.scene3d.Perspective;
import de.amr.games.pacman.ui.input.GameKeyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.games.pacman.Globals.TS;

public class Globals {

    public static final GameAssets THE_ASSETS = new GameAssets();
    public static final GameClockFX THE_CLOCK = new GameClockFX();
    public static final GameKeyboard THE_KEYBOARD = new GameKeyboard();
    public static final GameSound THE_SOUND = new GameSound();
    public static final GameUIConfigManager THE_UI_CONFIGS = new GameUIConfigManager();
    public static GameUI THE_UI;

    public static void createUIAndSupport3D(boolean support3D) {
        THE_UI = new PacManGamesUI();
        if (support3D) {
            THE_ASSETS.addAssets3D();
        }
    }

    public static final Font DEBUG_TEXT_FONT           = Font.font("Sans", FontWeight.BOLD, 18);
    public static final int LIVES_COUNTER_MAX          = 5;
    public static final double MAX_SCENE_2D_SCALING    = 5;
    public static final Color STATUS_ICON_COLOR        = Color.LIGHTGRAY;
    public static final byte STATUS_ICON_SIZE          = 24;
    public static final byte STATUS_ICON_SPACING       = 5;
    public static final byte STATUS_ICON_PADDING       = 10;

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

    public static final BooleanProperty PY_AUTOPILOT                       = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Color> PY_CANVAS_BG_COLOR           = new SimpleObjectProperty<>(Color.BLACK);
    public static final BooleanProperty PY_CANVAS_FONT_SMOOTHING           = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_CANVAS_IMAGE_SMOOTHING          = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_DEBUG_INFO_VISIBLE              = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_IMMUNITY                        = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_HEIGHT                      = new SimpleIntegerProperty(400);
    public static final BooleanProperty PY_PIP_ON                          = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_OPACITY_PERCENT             = new SimpleIntegerProperty(100);
    public static final IntegerProperty PY_SIMULATION_STEPS                = new SimpleIntegerProperty(1);

    public static final BooleanProperty          PY_3D_AXES_VISIBLE        = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode> PY_3D_DRAW_MODE           = new SimpleObjectProperty<>(DrawMode.FILL);
    public static final BooleanProperty          PY_3D_ENABLED             = new SimpleBooleanProperty(false);
    public static final BooleanProperty          PY_3D_ENERGIZER_EXPLODES  = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Color>    PY_3D_FLOOR_COLOR         = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    public static final ObjectProperty<Color>    PY_3D_LIGHT_COLOR         = new SimpleObjectProperty<>(Color.WHITE);
    public static final BooleanProperty          PY_3D_PAC_LIGHT_ENABLED   = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Perspective.Name> PY_3D_PERSPECTIVE = new SimpleObjectProperty<>(Perspective.Name.TRACK_PLAYER);
    public static final DoubleProperty           PY_3D_WALL_HEIGHT         = new SimpleDoubleProperty(3.5);
    public static final DoubleProperty           PY_3D_WALL_OPACITY        = new SimpleDoubleProperty(1.0);
}