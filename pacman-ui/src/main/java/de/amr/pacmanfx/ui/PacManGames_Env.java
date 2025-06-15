/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.ui.sound.PacManGames_SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Global environment accessible from every class in the UI layer.
 */
public class PacManGames_Env {

    private static PacManGames_Assets theAssets;
    private static GameClock theClock;
    private static Keyboard theKeyboard;
    private static Joypad theJoypad;
    private static PacManGames_SoundManager theSound;
            static PacManGames_UI theUI;

    public static PacManGames_Assets theAssets() { return theAssets; }
    public static GameClock theClock() { return theClock; }
    public static Keyboard theKeyboard() { return theKeyboard; }
    public static Joypad theJoypad() { return theJoypad; }
    public static PacManGames_SoundManager theSound() { return theSound; }
    public static PacManGames_UI theUI() { return theUI; }

    /**
     * Initializes the global game objects like game assets, clock, keyboard input etc.
     *
     * <p>Call this method at the start of the {@link Application#init()} method!</p>
     */
    public static void init() {
        checkUserDirsExistingAndWritable();
        theAssets = new PacManGames_Assets();
        theClock = new GameClock();
        theKeyboard = new Keyboard();
        theJoypad = new Joypad(theKeyboard);
        theSound = new PacManGames_SoundManager();
        Logger.info("Game environment initialized.");
    }

    /**
     * Creates the global UI instance and stores the configurations of the supported game variants.
     * <p>
     * Call this method in {@link javafx.application.Application#start(Stage)}!
     * </p>
     *
     * @param configClassesMap a map specifying the UI configuration for each supported game variant
     */
    public static void createUI(Map<String, Class<? extends PacManGames_UIConfig>> configClassesMap) {
        theUI = new PacManGames_UI_Impl(configClassesMap);
    }

    private static void checkUserDirsExistingAndWritable() {
        String homeDirDesc = "Pac-Man FX home directory";
        String customMapDirDesc = "Pac-Man FX custom map directory";
        boolean success = checkDirExistingAndWritable(Globals.HOME_DIR, homeDirDesc);
        if (success) {
            Logger.info(homeDirDesc + " is " + Globals.HOME_DIR);
            success = checkDirExistingAndWritable(Globals.CUSTOM_MAP_DIR, customMapDirDesc);
            if (success) {
                Logger.info(customMapDirDesc + " is " + Globals.CUSTOM_MAP_DIR);
            }
            Logger.info("User directories exist and are writable!");
        }
    }

    private static boolean checkDirExistingAndWritable(File dir, String description) {
        requireNonNull(dir);
        if (!dir.exists()) {
            Logger.info(description + " does not exist, create it...");
            if (!dir.mkdirs()) {
                Logger.error(description + " could not be created");
                return false;
            }
            Logger.info(description + " has been created");
            if (!dir.canWrite()) {
                Logger.error(description + " is not writable");
                return false;
            }
        }
        return true;
    }
}