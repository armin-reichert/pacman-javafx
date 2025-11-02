/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.uilib.Ufx.createImageBackground;

/**
 * Global assets used in the Pac-Man games UI.
 */
public class GameAssets extends AssetStorage {

    private static final ResourceManager GLOBAL_RESOURCES = () -> GameUI_Implementation.class;

    private final Model3DRepository model3DRepository;

    public GameAssets() {
        model3DRepository = new Model3DRepository();

        setTextResources(GLOBAL_RESOURCES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts"));

        set("background.scene",        createImageBackground(GLOBAL_RESOURCES.loadImage("graphics/pacman_wallpaper.png")));

        set("font.arcade",             GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 8));
        set("font.handwriting",        GLOBAL_RESOURCES.loadFont("fonts/Molle-Italic.ttf", 9));
        set("font.monospaced",         GLOBAL_RESOURCES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));
        set("font.pacfont",            GLOBAL_RESOURCES.loadFont("fonts/Pacfont.ttf", 8));
        set("font.pacfontgood",        GLOBAL_RESOURCES.loadFont("fonts/PacfontGood.ttf", 8));

        set("voice.explain",           GLOBAL_RESOURCES.url("sound/voice/press-key.mp3"));
        set("voice.autopilot.off",     GLOBAL_RESOURCES.url("sound/voice/autopilot-off.mp3"));
        set("voice.autopilot.on",      GLOBAL_RESOURCES.url("sound/voice/autopilot-on.mp3"));
        set("voice.immunity.off",      GLOBAL_RESOURCES.url("sound/voice/immunity-off.mp3"));
        set("voice.immunity.on",       GLOBAL_RESOURCES.url("sound/voice/immunity-on.mp3"));
    }

    public Model3DRepository theModel3DRepository() { return model3DRepository; }

    public Font arcadeFont(double size) { return font("font.arcade", size); }
}