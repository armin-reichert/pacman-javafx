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

import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.GameUI.THE_ASSETS;

public class GameAssets extends AssetStorage {

    protected Picker<String> pickerForGameOverTexts;
    protected Picker<String> pickerForLevelCompleteTexts;

    public GameAssets() {
        ResourceManager rm = this::getClass;

        ResourceBundle textResources = rm.getModuleBundle("de.amr.games.pacman.ui.texts.messages2d");
        addBundle(textResources);

        store("background.scene",        Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        store("background.play_scene3d", Ufx.imageBackground(rm.loadImage("graphics/blue_sky.jpg")));

        store("font.arcade",             rm.loadFont("fonts/emulogic.ttf", 8));
        store("font.handwriting",        rm.loadFont("fonts/Molle-Italic.ttf", 9));
        store("font.monospaced",         rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        store("icon.auto",               rm.loadImage("graphics/icons/auto.png"));
        store("icon.mute",               rm.loadImage("graphics/icons/mute.png"));
        store("icon.pause",              rm.loadImage("graphics/icons/pause.png"));

        store("voice.explain",           rm.url("sound/voice/press-key.mp3"));
        store("voice.autopilot.off",     rm.url("sound/voice/autopilot-off.mp3"));
        store("voice.autopilot.on",      rm.url("sound/voice/autopilot-on.mp3"));
        store("voice.immunity.off",      rm.url("sound/voice/immunity-off.mp3"));
        store("voice.immunity.on",       rm.url("sound/voice/immunity-on.mp3"));

        pickerForGameOverTexts = Picker.fromBundle(textResources, "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(textResources, "level.complete");
    }

    public void addAssets3D() {
        ResourceManager rm = this::getClass;
        THE_ASSETS.addBundle(rm.getModuleBundle("de.amr.games.pacman.ui.texts.messages3d"));

        ResourceManager uiLibResources = () -> Ufx.class;
        THE_ASSETS.store("model3D.pacman", new Model3D(uiLibResources.url("model3D/pacman.obj")));
        THE_ASSETS.store("model3D.pellet", new Model3D(uiLibResources.url("model3D/fruit.obj")));

        Model3D ghostModel3D = new Model3D(uiLibResources.url("model3D/ghost.obj"));
        THE_ASSETS.store("model3D.ghost",               ghostModel3D);
        THE_ASSETS.store("model3D.ghost.mesh.dress",    ghostModel3D.mesh("Sphere.004_Sphere.034_light_blue_ghost"));
        THE_ASSETS.store("model3D.ghost.mesh.pupils",   ghostModel3D.mesh("Sphere.010_Sphere.039_grey_wall"));
        THE_ASSETS.store("model3D.ghost.mesh.eyeballs", ghostModel3D.mesh("Sphere.009_Sphere.036_white"));

        pickerForGameOverTexts = Picker.fromBundle(THE_ASSETS.bundles().getLast(), "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(THE_ASSETS.bundles().getLast(), "level.complete");
    }

    public String localizedGameOverMessage() {
        return pickerForGameOverTexts.next();
    }

    public String localizedLevelCompleteMessage(int levelNumber) {
        return pickerForLevelCompleteTexts.next() + "\n\n" + localizedText("level_complete", levelNumber);
    }
}
