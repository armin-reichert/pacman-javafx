/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.uilib.Ufx.createImageBackground;

/**
 * Global assets used in the Pac-Man games UI.
 */
public class GlobalAssets extends AssetStorage {

    private static final ResourceManager GAME_UI_RES = () -> GameUI_Implementation.class;

    private final Model3DRepository model3DRepository;
    private final RandomTextPicker<String> pickGameOverText;
    private final RandomTextPicker<String> pickLevelCompleteText;

    public GlobalAssets() {
        model3DRepository = new Model3DRepository();

        setTextResources(GAME_UI_RES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts"));
        pickGameOverText      = RandomTextPicker.fromBundle(textResources(), "game.over");
        pickLevelCompleteText = RandomTextPicker.fromBundle(textResources(), "level.complete");

        set("background.play_scene3d", Background.fill(Ufx.createTopLeftToBottomRightGradient(Color.web("#1e90ff"), Color.web("#99badd"))));

        set("background.scene",        createImageBackground(GAME_UI_RES.loadImage("graphics/pacman_wallpaper.png")));

        set("font.arcade",             GAME_UI_RES.loadFont("fonts/emulogic.ttf", 8));
        set("font.handwriting",        GAME_UI_RES.loadFont("fonts/Molle-Italic.ttf", 9));
        set("font.monospaced",         GAME_UI_RES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));
        set("font.pacfont",            GAME_UI_RES.loadFont("fonts/Pacfont.ttf", 8));
        set("font.pacfontgood",        GAME_UI_RES.loadFont("fonts/PacfontGood.ttf", 8));

        set("voice.explain",           GAME_UI_RES.url("sound/voice/press-key.mp3"));
        set("voice.autopilot.off",     GAME_UI_RES.url("sound/voice/autopilot-off.mp3"));
        set("voice.autopilot.on",      GAME_UI_RES.url("sound/voice/autopilot-on.mp3"));
        set("voice.immunity.off",      GAME_UI_RES.url("sound/voice/immunity-off.mp3"));
        set("voice.immunity.on",       GAME_UI_RES.url("sound/voice/immunity-on.mp3"));
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