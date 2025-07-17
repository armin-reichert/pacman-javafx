/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.Picker;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.scene.text.Font;

/**
 * Stores assets used in the Pac-Man games UI.
 */
public class PacManGames_Assets extends AssetStorage implements ResourceManager {

    private final Picker<String> pickerForGameOverTexts;
    private final Picker<String> pickerForLevelCompleteTexts;
    private final Model3DRepository model3DRepository;

    @Override
    public Class<?> resourceRootClass() { return PacManGames_Assets.class; }

    public PacManGames_Assets() {
        model3DRepository = new Model3DRepository();

        localizedTextBundle = getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

        pickerForGameOverTexts = Picker.fromBundle(localizedTextBundle, "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(localizedTextBundle, "level.complete");

        store("background.scene",        Ufx.imageBackground(loadImage("graphics/pacman_wallpaper.png")));
        store("background.play_scene3d", Ufx.imageBackground(loadImage("graphics/blue_sky.jpg")));

        store("font.arcade",             loadFont("fonts/emulogic.ttf", 8));
        store("font.handwriting",        loadFont("fonts/Molle-Italic.ttf", 9));
        store("font.monospaced",         loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));
        store("font.pacfont",            loadFont("fonts/Pacfont.ttf", 8));
        store("font.pacfontgood",        loadFont("fonts/PacfontGood.ttf", 8));

        store("voice.explain",           url("sound/voice/press-key.mp3"));
        store("voice.autopilot.off",     url("sound/voice/autopilot-off.mp3"));
        store("voice.autopilot.on",      url("sound/voice/autopilot-on.mp3"));
        store("voice.immunity.off",      url("sound/voice/immunity-off.mp3"));
        store("voice.immunity.on",       url("sound/voice/immunity-on.mp3"));
    }

    public Model3DRepository theModel3DRepository() {
        return model3DRepository;
    }

    public Font arcadeFont(float size) { return font("font.arcade", size); }

    public String localizedGameOverMessage() {
        return pickerForGameOverTexts.next();
    }

    public String localizedLevelCompleteMessage(int levelNumber) {
        return pickerForLevelCompleteTexts.hasEntries()
            ? pickerForLevelCompleteTexts.next() + "\n\n" + text("level_complete", levelNumber)
            : "";
    }
}