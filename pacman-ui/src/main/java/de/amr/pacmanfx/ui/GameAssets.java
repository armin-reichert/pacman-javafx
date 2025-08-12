/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.uilib.Ufx.createImageBackground;

/**
 * Stores assets used in the Pac-Man games UI.
 */
public class GameAssets extends AssetStorage {

    private static final ResourceManager GAME_UI_RES = () -> GameUI_Implementation.class;

    private final Model3DRepository model3DRepository;
    private final RandomTextPicker<String> pickGameOverText;
    private final RandomTextPicker<String> pickLevelCompleteText;

    public GameAssets() {
        model3DRepository = new Model3DRepository();

        textBundle = GAME_UI_RES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");
        pickGameOverText      = RandomTextPicker.fromBundle(textBundle, "game.over");
        pickLevelCompleteText = RandomTextPicker.fromBundle(textBundle, "level.complete");

        store("background.scene",        createImageBackground(GAME_UI_RES.loadImage("graphics/pacman_wallpaper.png")));
        store("background.play_scene3d", createImageBackground(GAME_UI_RES.loadImage("graphics/blue_sky.jpg")));

        store("font.arcade",             GAME_UI_RES.loadFont("fonts/emulogic.ttf", 8));
        store("font.handwriting",        GAME_UI_RES.loadFont("fonts/Molle-Italic.ttf", 9));
        store("font.monospaced",         GAME_UI_RES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));
        store("font.pacfont",            GAME_UI_RES.loadFont("fonts/Pacfont.ttf", 8));
        store("font.pacfontgood",        GAME_UI_RES.loadFont("fonts/PacfontGood.ttf", 8));

        store("voice.explain",           GAME_UI_RES.url("sound/voice/press-key.mp3"));
        store("voice.autopilot.off",     GAME_UI_RES.url("sound/voice/autopilot-off.mp3"));
        store("voice.autopilot.on",      GAME_UI_RES.url("sound/voice/autopilot-on.mp3"));
        store("voice.immunity.off",      GAME_UI_RES.url("sound/voice/immunity-off.mp3"));
        store("voice.immunity.on",       GAME_UI_RES.url("sound/voice/immunity-on.mp3"));
    }

    public Model3DRepository theModel3DRepository() { return model3DRepository; }

    public Font arcadeFont(double size) { return font("font.arcade", size); }

    public String translatedGameOverMessage() {
        return pickGameOverText.nextText();
    }

    public String translatedLevelCompleteMessage(int levelNumber) {
        return pickLevelCompleteText.hasEntries()
            ? pickLevelCompleteText.nextText() + "\n\n" + translated("level_complete", levelNumber)
            : "";
    }
}