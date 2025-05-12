/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.arcade.Arcade;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.Picker;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ResourceBundle;

public class GameAssets extends AssetStorage implements ResourceManager {

    public static final Color ARCADE_BLUE   = Color.web(Arcade.Palette.BLUE);
    public static final Color ARCADE_CYAN   = Color.web(Arcade.Palette.CYAN);
    public static final Color ARCADE_ORANGE = Color.web(Arcade.Palette.ORANGE);
    public static final Color ARCADE_PINK   = Color.web(Arcade.Palette.PINK);
    public static final Color ARCADE_RED    = Color.web(Arcade.Palette.RED);
    public static final Color ARCADE_ROSE   = Color.web(Arcade.Palette.ROSE);
    public static final Color ARCADE_WHITE  = Color.web(Arcade.Palette.WHITE);
    public static final Color ARCADE_YELLOW = Color.web(Arcade.Palette.YELLOW);

    protected Picker<String> pickerForGameOverTexts;
    protected Picker<String> pickerForLevelCompleteTexts;

    @Override
    public Class<?> resourceRootClass() { return GameAssets.class; }

    public void load() {
        ResourceBundle localizedTexts = getModuleBundle("de.amr.pacmanfx.ui.localized_texts");
        setLocalizedTexts(localizedTexts);

        pickerForGameOverTexts = Picker.fromBundle(localizedTexts, "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(localizedTexts, "level.complete");

        store("background.scene",        Ufx.imageBackground(loadImage("graphics/pacman_wallpaper.png")));
        store("background.play_scene3d", Ufx.imageBackground(loadImage("graphics/blue_sky.jpg")));

        store("font.arcade",             loadFont("fonts/emulogic.ttf", 8));
        store("font.handwriting",        loadFont("fonts/Molle-Italic.ttf", 9));
        store("font.monospaced",         loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        store("voice.explain",           url("sound/voice/press-key.mp3"));
        store("voice.autopilot.off",     url("sound/voice/autopilot-off.mp3"));
        store("voice.autopilot.on",      url("sound/voice/autopilot-on.mp3"));
        store("voice.immunity.off",      url("sound/voice/immunity-off.mp3"));
        store("voice.immunity.on",       url("sound/voice/immunity-on.mp3"));
    }

    public Font arcadeFontAtSize(float size) { return font("font.arcade", size); }

    public String localizedGameOverMessage() {
        return pickerForGameOverTexts.next();
    }

    public String localizedLevelCompleteMessage(int levelNumber) {
        return pickerForLevelCompleteTexts.hasEntries()
            ? pickerForLevelCompleteTexts.next() + "\n\n" + text("level_complete", levelNumber)
            : "";
    }
}