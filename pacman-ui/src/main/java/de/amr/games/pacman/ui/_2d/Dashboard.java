/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui.dashboard.InfoBox;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.assertNotNull;

public class Dashboard {

    public record DashboardEntry(String id, String title, InfoBox infoBox) {}

    public static final int INFOBOX_MIN_LABEL_WIDTH = 110;
    public static final int INFOBOX_MIN_WIDTH = 180;
    public static final Color INFO_BOX_CONTENT_BG_COLOR = Color.rgb(0, 0, 50, 1.0);

    private final AssetStorage assets;
    private final GameContext context;
    private final List<DashboardEntry> entries = new ArrayList<>();

    public Dashboard(GameContext context) {
        this.assets = new AssetStorage();
        ResourceManager rm = () -> PacManGamesUI.class;

        assets.store("font.monospaced",             rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        assets.store("icon.auto",                   rm.loadImage("graphics/icons/auto.png"));
        assets.store("icon.mute",                   rm.loadImage("graphics/icons/mute.png"));
        assets.store("icon.pause",                  rm.loadImage("graphics/icons/pause.png"));

        assets.store("infobox.text_color",          Color.WHITE);
        assets.store("infobox.label_font",          Font.font("Sans", 12));
        assets.store("infobox.text_font",           Font.font("Sans", 12));

        this.context = context;
    }

    private DashboardEntry createEntry(String id, String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(INFOBOX_MIN_LABEL_WIDTH);
        infoBox.setContentBackground(Background.fill(INFO_BOX_CONTENT_BG_COLOR));
        infoBox.setTextColor(assets.get("infobox.text_color"));
        infoBox.setContentTextFont(assets.get("infobox.text_font"));
        infoBox.setLabelFont(assets.get("infobox.label_font"));
        infoBox.init(context);
        return new DashboardEntry(id, title, infoBox);
    }

    public void addDashboardItem(String id, String title, InfoBox infoBox) {
        assertNotNull(id);
        assertNotNull(title);
        assertNotNull(infoBox);
        entries.add(createEntry(id, title, infoBox));
    }

    public Stream<DashboardEntry> entries() { return entries.stream(); }

    @SuppressWarnings("unchecked")
    public <I extends InfoBox> I getItem(String id) {
        return (I) entries.stream()
            .filter(entry -> id.equals(entry.id))
            .map(entry -> entry.infoBox)
            .findFirst().orElse(null);
    }
}