/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.Picker;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.model3D.Model3D;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.ResourceBundle;

public class GameAssets extends AssetStorage implements ResourceManager {

    protected Picker<String> pickerForGameOverTexts;
    protected Picker<String> pickerForLevelCompleteTexts;

    @Override
    public Class<?> resourceRootClass() {
        return GameAssets.class;
    }

    public GameAssets() {
        ResourceBundle textResources = getModuleBundle("de.amr.games.pacman.ui.texts.messages2d");
        addBundle(textResources);

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

        pickerForGameOverTexts = Picker.fromBundle(textResources, "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(textResources, "level.complete");
    }

    public void addAssets3D() {
        addBundle(getModuleBundle("de.amr.games.pacman.ui.texts.messages3d"));

        ResourceManager uiLibResources = () -> Ufx.class;
        store("model3D.pacman", new Model3D(uiLibResources.url("model3D/pacman.obj")));
        store("model3D.pellet", new Model3D(uiLibResources.url("model3D/fruit.obj")));

        Model3D ghostModel3D = new Model3D(uiLibResources.url("model3D/ghost.obj"));
        store("model3D.ghost",               ghostModel3D);
        store("model3D.ghost.mesh.dress",    ghostModel3D.mesh("Sphere.004_Sphere.034_light_blue_ghost"));
        store("model3D.ghost.mesh.pupils",   ghostModel3D.mesh("Sphere.010_Sphere.039_grey_wall"));
        store("model3D.ghost.mesh.eyeballs", ghostModel3D.mesh("Sphere.009_Sphere.036_white"));

        pickerForGameOverTexts = Picker.fromBundle(bundles().getLast(), "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(bundles().getLast(), "level.complete");

        Logger.info("3D assets loaded");
    }

    public Font arcadeFontAtSize(float size) {
        return font("font.arcade", size);
    }

    public String localizedGameOverMessage() {
        return pickerForGameOverTexts.next();
    }

    public String localizedLevelCompleteMessage(int levelNumber) {
        return pickerForLevelCompleteTexts.next() + "\n\n" + text("level_complete", levelNumber);
    }
}
