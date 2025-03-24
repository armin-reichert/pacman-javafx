/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.dashboard.InfoBox;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.assertNotNull;

public class Dashboard {

    public record DashboardEntry(String id, String title, InfoBox infoBox) {}

    public static final int INFOBOX_MIN_LABEL_WIDTH = 110;
    public static final int INFOBOX_MIN_WIDTH = 180;
    public static final Color INFO_BOX_CONTENT_BG_COLOR = Color.rgb(0, 0, 50, 1.0);
    public static final Color INFO_BOX_TEXT_COLOR = Color.WHITE;
    public static final Font INFO_BOX_FONT = Font.font("Sans", 12);

    private final List<DashboardEntry> entries = new ArrayList<>();

    public Dashboard() {
    }

    private DashboardEntry createEntry(String id, String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(INFOBOX_MIN_LABEL_WIDTH);
        infoBox.setContentBackground(Background.fill(INFO_BOX_CONTENT_BG_COLOR));
        infoBox.setTextColor(INFO_BOX_TEXT_COLOR);
        infoBox.setContentTextFont(INFO_BOX_FONT);
        infoBox.setLabelFont(INFO_BOX_FONT);
        infoBox.init();

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