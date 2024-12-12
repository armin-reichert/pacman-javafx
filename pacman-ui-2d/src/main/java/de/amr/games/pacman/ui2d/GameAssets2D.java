/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GameAssets2D extends AssetStorage {

    public static final String PFX_PACMAN = "pacman";
    public static final String PFX_PACMAN_XXL = "pacman_xxl";
    public static final String PFX_MS_PACMAN = "ms_pacman";
    public static final String PFX_MS_PACMAN_TENGEN = "tengen";

    public static String assetPrefix(GameVariant variant) {
        return switch (variant) {
            case MS_PACMAN        -> PFX_MS_PACMAN;
            case MS_PACMAN_TENGEN -> PFX_MS_PACMAN_TENGEN;
            case PACMAN           -> PFX_PACMAN;
            case PACMAN_XXL       -> PFX_PACMAN_XXL;
        };
    }

    public static void addTo(AssetStorage assets) {
        ResourceManager rm = () -> GameAssets2D.class;

        assets.addBundle(rm.getModuleBundle("de.amr.games.pacman.ui2d.texts.messages"));

        // Dashboard

        assets.store("photo.armin1970",                      rm.loadImage("graphics/armin.jpg"));
        assets.store("icon.auto",                            rm.loadImage("graphics/icons/auto.png"));
        assets.store("icon.mute",                            rm.loadImage("graphics/icons/mute.png"));
        assets.store("icon.pause",                           rm.loadImage("graphics/icons/pause.png"));
        assets.store("icon.play",                            rm.loadImage("graphics/icons/play.png"));
        assets.store("icon.stop",                            rm.loadImage("graphics/icons/stop.png"));
        assets.store("icon.step",                            rm.loadImage("graphics/icons/step.png"));

        assets.store("infobox.background_color",             Color.rgb(0,0,50,1.0));
        assets.store("infobox.min_label_width",              110);
        assets.store("infobox.min_col_width",                180);
        assets.store("infobox.text_color",                   Color.WHITE);
        assets.store("infobox.label_font",                   Font.font("Sans", 12));
        assets.store("infobox.text_font",                    Font.font("Sans", 12));

        //
        // Common to all game variants
        //

        assets.store("startpage.arrow.left",                 rm.loadImage("graphics/icons/arrow-left.png"));
        assets.store("startpage.arrow.right",                rm.loadImage("graphics/icons/arrow-right.png"));

        assets.store("wallpaper.color",                      Color.rgb(72, 78, 135));

        assets.store("font.arcade",                          rm.loadFont("fonts/emulogic.ttf", 8));
        assets.store("font.handwriting",                     rm.loadFont("fonts/Molle-Italic.ttf", 9));
        assets.store("font.monospaced",                      rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        assets.store("voice.explain",                        rm.url("sound/voice/press-key.mp3"));
        assets.store("voice.autopilot.off",                  rm.url("sound/voice/autopilot-off.mp3"));
        assets.store("voice.autopilot.on",                   rm.url("sound/voice/autopilot-on.mp3"));
        assets.store("voice.immunity.off",                   rm.url("sound/voice/immunity-off.mp3"));
        assets.store("voice.immunity.on",                    rm.url("sound/voice/immunity-on.mp3"));
    }
}