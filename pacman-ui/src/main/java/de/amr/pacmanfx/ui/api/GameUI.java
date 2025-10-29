/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.GameUI_View;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;
import java.util.Set;

public interface GameUI extends GameUI_Lifecycle {

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

    /**
     * @return set of key-to-action bindings
     */
    Set<ActionBinding> actionBindings();

    /**
     * @return assets (fonts, sounds, colors, localized texts etc.) for the different games
     */
    GlobalAssets assets();

    /**
     * @return watchdog process observing the directory where user-defined maps are stored
     */
    DirectoryWatchdog directoryWatchdog();

    /**
     * @return the clock driving the game
     */
    GameClock clock();

    /**
     * @return Context giving access to important entities like game controller, game state etc.
     */
    GameContext gameContext();

    /**
     * @return key emulation of NES joypad
     */
    Joypad joypad();

    /**
     * @return global keyboard state
     */
    Keyboard keyboard();

    /**
     * @return sound manager for the current game
     */
    SoundManager soundManager();

    /**
     * @return the primary stage provided by the JavaFX application
     */
    Stage stage();

    /**
     * @return the UI preferences (stored permanently in platform-specific way)
     */
    UIPreferences preferences();

    // Messages

    /** Default duration a flash message appears on the screen. */
    Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

    /**
     * Shows a message on the screen that slowly fades out.
     *
     * @param duration display duration before fading out
     * @param message the message text
     * @param args arguments merged into the message text using String.format()
     */
    void showFlashMessage(Duration duration, String message, Object... args);

    /**
     * Shows a message on the screen that slowly fades out and displays for a default duration.
     *
     * @param message the message text
     * @param args arguments merged into the message text using String.format()
     */
    default void showFlashMessage(String message, Object... args) {
        showFlashMessage(DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }

    void updateTitle();

    // Scene access

    Optional<GameScene> currentGameScene();

    boolean isCurrentGameSceneID(String id);

    void updateGameScene(boolean forceReloading);

    // View access

    GameUI_View currentView();

    Optional<EditorView> optEditorView();

    PlayView playView();

    StartPagesView startPagesView();

    void showEditorView();

    void showPlayView();

    void showStartView();

    // Config

    /**
     * @param gameVariant name of game variant
     * @return UI configuration for given game variant
     */
    GameUI_Config config(String gameVariant);

    /**
     * @return UI configuration for the current game
     * @param <T> type of UI configuration
     */
    <T extends GameUI_Config> T currentConfig();

}