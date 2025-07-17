/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui._3d.Perspective;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;

import static de.amr.pacmanfx.ui.ActionBindingMap.createActionBinding;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public interface GameUI {

    static GameUI theUI() { return PacManGames_UI_Impl.THE_ONE; }

    static GameUI_Builder build(GameContext gameContext, Stage stage, double width, double height) {
        PacManGames_UI_Impl.THE_ONE = new PacManGames_UI_Impl(gameContext, stage, width, height);
        return new GameUI_Builder(PacManGames_UI_Impl.THE_ONE);
    }

    Preferences prefs();

    default Color colorFromPrefs(String colorKey, Color defaultColor) {
        return Color.web(prefs().get(colorKey, formatColorHex(defaultColor)));
    }

    default Font fontFromPrefs(
        String familyKey, String familyDefault,
        String weightKey, int weightDefault,
        String sizeKey, float sizeDefault)
    {
        String family = prefs().get(familyKey, familyDefault);
        int weight = prefs().getInt(weightKey, weightDefault);
        float size = prefs().getFloat(sizeKey, sizeDefault);
        return Font.font(family, FontWeight.findByWeight(weight), size);
    }

    // Global key combinations and action bindings
    KeyCombination KEY_FULLSCREEN  = nude(KeyCode.F11);
    KeyCombination KEY_MUTE        = alt(KeyCode.M);
    KeyCombination KEY_OPEN_EDITOR = alt_shift(KeyCode.E);

    Map<GameAction, Set<KeyCombination>> GLOBAL_ACTION_BINDINGS = Map.ofEntries(
        createActionBinding(ACTION_ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        createActionBinding(ACTION_ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        createActionBinding(ACTION_BOOT_SHOW_PLAY_VIEW,     nude(KeyCode.F3)),
        createActionBinding(ACTION_CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        createActionBinding(ACTION_CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        createActionBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        createActionBinding(ACTION_CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        createActionBinding(ACTION_ENTER_FULLSCREEN,        nude(KeyCode.F11)),
        createActionBinding(ACTION_PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        createActionBinding(ACTION_PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        createActionBinding(ACTION_SHOW_HELP,               nude(KeyCode.H)),
        createActionBinding(ACTION_STEER_UP,                nude(KeyCode.UP), control(KeyCode.UP)),
        createActionBinding(ACTION_STEER_DOWN,              nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        createActionBinding(ACTION_STEER_LEFT,              nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        createActionBinding(ACTION_STEER_RIGHT,             nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        createActionBinding(ACTION_QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        createActionBinding(ACTION_SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        createActionBinding(ACTION_SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        createActionBinding(ACTION_SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        createActionBinding(ACTION_SIMULATION_ONE_STEP,     shift(KeyCode.P), shift(KeyCode.F5)),
        createActionBinding(ACTION_SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        createActionBinding(ACTION_TEST_CUT_SCENES,         alt(KeyCode.C)),
        createActionBinding(ACTION_TEST_LEVELS_BONI,        alt(KeyCode.T)),
        createActionBinding(ACTION_TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        createActionBinding(ACTION_TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        createActionBinding(ACTION_TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        createActionBinding(ACTION_TOGGLE_MUTED,            alt(KeyCode.M)),
        createActionBinding(ACTION_TOGGLE_PAUSED,           nude(KeyCode.P), nude(KeyCode.F5)),
        createActionBinding(ACTION_TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        createActionBinding(ACTION_TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        createActionBinding(ACTION_TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        createActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        createActionBinding(ACTION_TOGGLE_DRAW_MODE,        alt(KeyCode.W))
    );

    default MenuItem createContextMenuTitleItem(String title) {
        Font font = fontFromPrefs(
            "context_menu.title.font.family", "Dialog",
            "context_menu.title.font.weight", 850,
            "context_menu.title.font.size", 14
        );
        Color fillColor = colorFromPrefs("context_menu.title.fill", Color.CORNFLOWERBLUE);
        var text = new Text(title);
        text.setFont(font);
        text.setFill(fillColor);
        return new CustomMenuItem(text);
    }

    // Global properties
    ObjectProperty<Color>            propertyCanvasBackgroundColor();
    BooleanProperty                  propertyCanvasFontSmoothing();
    BooleanProperty                  propertyCanvasImageSmoothing();
    ObjectProperty<GameScene>        propertyCurrentGameScene();
    ObjectProperty<PacManGames_View> propertyCurrentView();
    BooleanProperty                  propertyDebugInfoVisible();
    IntegerProperty                  propertyMiniViewHeight();
    BooleanProperty                  propertyMiniViewOn();
    IntegerProperty                  propertyMiniViewOpacityPercent();
    BooleanProperty                  propertyMuted();
    IntegerProperty                  propertySimulationSteps();
    BooleanProperty                  property3DAxesVisible();
    ObjectProperty<DrawMode>         property3DDrawMode();
    BooleanProperty                  property3DEnabled();
    BooleanProperty                  property3DEnergizerExplodes();
    ObjectProperty<Color>            property3DFloorColor();
    ObjectProperty<Color>            property3DLightColor();
    BooleanProperty                  property3DPacLightEnabled();
    ObjectProperty<Perspective.ID>   property3DPerspective();
    DoubleProperty                   property3DWallHeight();
    DoubleProperty                   property3DWallOpacity();

    PacManGames_Assets               theAssets();
    <T extends GameUI_Config> T      theConfiguration();
    GameClock                        theGameClock();
    GameContext                      theGameContext();
    Joypad                           theJoypad();
    Keyboard                         theKeyboard();
    SoundManager                     theSound();
    Stage                            theStage();
    DirectoryWatchdog                theWatchdog();

    void restart();
    void selectGameVariant(String variant);
    void show();

    GameUI_Config uiConfig(String gameVariant);
    void setUIConfig(String variant, GameUI_Config configuration);

    // Game scenes
    Optional<GameScene> currentGameScene();
    boolean currentGameSceneIsPlayScene2D();
    boolean currentGameSceneIsPlayScene3D();
    void updateGameScene(boolean reload);

    // Views
    PacManGames_View currentView();
    PlayView thePlayView();
    StartPagesView theStartPagesView();

    void showEditorView();
    void showPlayView();
    void showStartView();

    // Flash messages
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1.5, message, args); }
    void showFlashMessageSec(double seconds, String message, Object... args);

    void terminateApp();
}