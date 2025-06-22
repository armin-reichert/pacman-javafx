/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui._3d.InfoBox3D;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui.PacManGames.theAssets;

public class Dashboard {

    public static final int INFOBOX_MIN_LABEL_WIDTH = 110;
    public static final int INFOBOX_MIN_WIDTH = 180;
    public static final Color INFO_BOX_CONTENT_BG_COLOR = Color.rgb(0, 0, 50, 1.0);
    public static final Color INFO_BOX_TEXT_COLOR = Color.WHITE;
    public static final Font INFO_BOX_FONT = Font.font("Sans", 12);

    private final Map<DashboardID, InfoBox> infoBoxMap = new LinkedHashMap<>();

    public Stream<InfoBox> infoBoxes() { return infoBoxMap.values().stream(); }

    @SuppressWarnings("unchecked")
    public <I extends InfoBox> I getInfoBox(DashboardID id) {
        return (I) infoBoxMap.get(id);
    }

    public void removeInfoBox(DashboardID id) {
        infoBoxMap.remove(id);
    }

    public void addInfoBox(DashboardID id) {
        switch (id) {
            case ABOUT        -> addInfoBox(id, "infobox.about.title", new InfoBoxAbout());
            case ACTOR_INFO   -> addInfoBox(id, "infobox.actor_info.title", new InfoBoxActorInfo());
            case CUSTOM_MAPS  -> addInfoBox(id, "infobox.custom_maps.title", new InfoBoxCustomMaps());
            case GENERAL      -> addInfoBox(id, "infobox.general.title", new InfoBoxGeneral());
            case GAME_CONTROL -> addInfoBox(id, "infobox.game_control.title", new InfoBoxGameControl());
            case GAME_INFO    -> addInfoBox(id, "infobox.game_info.title", new InfoBoxGameInfo());
            case JOYPAD       -> addInfoBox(id, "infobox.joypad.title", new InfoBoxJoypad());
            case KEYBOARD_SHORTCUTS_GLOBAL -> addInfoBox(id, "infobox.keyboard_shortcuts_global.title", new InfoBoxGlobalKeyShortcuts());
            case KEYBOARD_SHORTCUTS_LOCAL ->  addInfoBox(id, "infobox.keyboard_shortcuts_local.title", new InfoBoxLocalKeyShortcuts());
            case README -> {
                InfoBox infoBox = new InfoBoxReadmeFirst();
                infoBox.setExpanded(true);
                addInfoBox(id, "infobox.readme.title", infoBox);
            }
            case SETTINGS_3D -> addInfoBox(id, "infobox.3D_settings.title", new InfoBox3D());
        }
    }

    private void addInfoBox(DashboardID id, String titleKey, InfoBox infoBox) {
        infoBoxMap.put(id, preconfiguredInfoBox(theAssets().text(titleKey), infoBox));
    }

    private InfoBox preconfiguredInfoBox(String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(INFOBOX_MIN_LABEL_WIDTH);
        infoBox.setContentBackground(Background.fill(INFO_BOX_CONTENT_BG_COLOR));
        infoBox.setTextColor(INFO_BOX_TEXT_COLOR);
        infoBox.setContentTextFont(INFO_BOX_FONT);
        infoBox.setLabelFont(INFO_BOX_FONT);
        infoBox.init();
        return infoBox;
    }
}