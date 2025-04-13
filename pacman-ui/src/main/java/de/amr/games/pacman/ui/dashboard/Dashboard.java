/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.ui.DashboardID;
import de.amr.games.pacman.ui._3d.dashboard.InfoBox3D;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui.Globals.THE_ASSETS;

public class Dashboard {

    public static final int INFOBOX_MIN_LABEL_WIDTH = 110;
    public static final int INFOBOX_MIN_WIDTH = 180;
    public static final Color INFO_BOX_CONTENT_BG_COLOR = Color.rgb(0, 0, 50, 1.0);
    public static final Color INFO_BOX_TEXT_COLOR = Color.WHITE;
    public static final Font INFO_BOX_FONT = Font.font("Sans", 12);

    private final Map<DashboardID, InfoBox> infoBoxMap = new LinkedHashMap<>();

    private static InfoBox configured(String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(INFOBOX_MIN_LABEL_WIDTH);
        infoBox.setContentBackground(Background.fill(INFO_BOX_CONTENT_BG_COLOR));
        infoBox.setTextColor(INFO_BOX_TEXT_COLOR);
        infoBox.setContentTextFont(INFO_BOX_FONT);
        infoBox.setLabelFont(INFO_BOX_FONT);
        infoBox.init();
        return infoBox;
    }

    public Stream<InfoBox> infoBoxes() { return infoBoxMap.values().stream(); }

    @SuppressWarnings("unchecked")
    public <I extends InfoBox> I getInfoBox(DashboardID id) {
        return (I) infoBoxMap.get(id);
    }

    public void addDefaultInfoBoxes(DashboardID... titles) {
        for (DashboardID title : titles) { addInfoBox(title); }
    }

    public void addInfoBox(DashboardID id) {
        switch (id) {
            case ABOUT        -> put(id, "infobox.about.title", new InfoBoxAbout());
            case ACTOR_INFO   -> put(id, "infobox.actor_info.title", new InfoBoxActorInfo());
            case CUSTOM_MAPS  -> put(id, "infobox.custom_maps.title", new InfoBoxCustomMaps());
            case GENERAL      -> put(id, "infobox.general.title", new InfoBoxGeneral());
            case GAME_CONTROL -> put(id, "infobox.game_control.title", new InfoBoxGameControl());
            case GAME_INFO    -> put(id, "infobox.game_info.title", new InfoBoxGameInfo());
            case JOYPAD       -> put(id, "infobox.joypad.title", new InfoBoxJoypad());
            case KEYBOARD     -> put(id, "infobox.keyboard_shortcuts.title", new InfoBoxKeys());
            case README -> {
                InfoBox infoBox = new InfoBoxReadmeFirst();
                infoBox.setExpanded(true);
                put(id, "infobox.readme.title", infoBox);
            }
            case SETTINGS_3D -> put(id, "infobox.3D_settings.title", new InfoBox3D());
        }
    }

    private void put(DashboardID id, String titleKey, InfoBox infoBox) {
        infoBoxMap.put(id, configured(THE_ASSETS.text(titleKey), infoBox));
    }
}