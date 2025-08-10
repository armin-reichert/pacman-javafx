/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public interface GameUI {

    double DEFAULT_FLASH_MESSAGE_SECONDS = 1.5;

//    static GameUI theUI() { return PacManGames_UI_Impl.THE_ONE; }

    /**
     * @return list of key to action bindings
     */
    List<ActionBinding> actionBindings();

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

    /**
     * Leaves the current game scene (if any) and displays the start page for the current game.
     */
    void quitCurrentGameScene();

    /**
     * Resets the game clock to normal speed and shows the boot screen for the selected game.
     */
    void restart();

    /**
     * Terminates the game and stops the game clock. Called when the application is terminated by closing the stage.
     */
    void terminate();

    /**
     * Shows the start page for the given game variant, loads its resources and initializes the game model.
     *
     * @param gameVariant game variant name ("PACMAN", "MS_PACMAN" etc.)
     */
    void selectGameVariant(String gameVariant);

    /**
     * Shows the UI and displays the start page view.
     */
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