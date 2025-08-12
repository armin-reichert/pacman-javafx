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
public class GameAssets extends AssetStorage {

    private static final ResourceManager RES = () -> GameAssets.class;

    private final Model3DRepository model3DRepository;

    private final Picker<String> pickeOverTexts;
    private final Picker<String> pickLevelCompleteTexts;

    public GameAssets() {
        model3DRepository = new Model3DRepository();

        textBundle = RES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

        pickeOverTexts = Picker.fromBundle(textBundle, "game.over");
        pickLevelCompleteTexts = Picker.fromBundle(textBundle, "level.complete");

        store("background.scene",        Ufx.createBackground(RES.loadImage("graphics/pacman_wallpaper.png")));
        store("background.play_scene3d", Ufx.createBackground(RES.loadImage("graphics/blue_sky.jpg")));

        store("font.arcade",             RES.loadFont("fonts/emulogic.ttf", 8));
        store("font.handwriting",        RES.loadFont("fonts/Molle-Italic.ttf", 9));
        store("font.monospaced",         RES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));
        store("font.pacfont",            RES.loadFont("fonts/Pacfont.ttf", 8));
        store("font.pacfontgood",        RES.loadFont("fonts/PacfontGood.ttf", 8));

        store("voice.explain",           RES.url("sound/voice/press-key.mp3"));
        store("voice.autopilot.off",     RES.url("sound/voice/autopilot-off.mp3"));
        store("voice.autopilot.on",      RES.url("sound/voice/autopilot-on.mp3"));
        store("voice.immunity.off",      RES.url("sound/voice/immunity-off.mp3"));
        store("voice.immunity.on",       RES.url("sound/voice/immunity-on.mp3"));
    }

    public Model3DRepository theModel3DRepository() { return model3DRepository; }

    public Font arcadeFont(float size) { return font("font.arcade", size); }

    public String localizedGameOverMessage() {
        return pickeOverTexts.next();
    }

    public String localizedLevelCompleteMessage(int levelNumber) {
        return pickLevelCompleteTexts.hasEntries()
            ? pickLevelCompleteTexts.next() + "\n\n" + translated("level_complete", levelNumber)
            : "";
    }
}