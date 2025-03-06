package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class DashboardAssets extends AssetStorage {

    public static final DashboardAssets IT = new DashboardAssets();

    private DashboardAssets() {
        ResourceManager rm = () -> PacManGamesUI.class;

        store("font.handwriting",            rm.loadFont("fonts/Molle-Italic.ttf", 9));
        store("font.monospaced",             rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        store("photo.armin1970",             rm.loadImage("graphics/armin.jpg"));
        store("icon.auto",                   rm.loadImage("graphics/icons/auto.png"));
        store("icon.mute",                   rm.loadImage("graphics/icons/mute.png"));
        store("icon.pause",                  rm.loadImage("graphics/icons/pause.png"));
        store("icon.play",                   rm.loadImage("graphics/icons/play.png"));
        store("icon.stop",                   rm.loadImage("graphics/icons/stop.png"));
        store("icon.step",                   rm.loadImage("graphics/icons/step.png"));

        store("infobox.background_color",    Color.rgb(0, 0, 50, 1.0));
        store("infobox.min_label_width",     110);
        store("infobox.min_col_width",       180);
        store("infobox.text_color",          Color.WHITE);
        store("infobox.label_font",          Font.font("Sans", 12));
        store("infobox.text_font",           Font.font("Sans", 12));

        store("image.nes-controller",        rm.loadImage("graphics/nes-controller.jpg"));

    }
}
