/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.ui.DashboardItemID;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.dashboard.DashboardAssets;
import de.amr.games.pacman.ui.dashboard.InfoBox;
import de.amr.games.pacman.uilib.AssetStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.assertNotNull;

public class Dashboard {

    public record DashboardEntry(DashboardItemID id, String title, InfoBox infoBox) {}

    private final AssetStorage assets = DashboardAssets.IT;
    private final GameContext context;
    private final List<DashboardEntry> entries = new ArrayList<>();

    public Dashboard(GameContext context) {
        this.context = context;
    }

    private DashboardEntry createEntry(DashboardItemID id, String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(assets.get("infobox.min_label_width"));
        infoBox.setTextColor(assets.get("infobox.text_color"));
        infoBox.setContentTextFont(assets.get("infobox.text_font"));
        infoBox.setLabelFont(assets.get("infobox.label_font"));
        infoBox.init(context);
        return new DashboardEntry(id, title, infoBox);
    }

    public void addDashboardItem(DashboardItemID id, String title, InfoBox infoBox) {
        assertNotNull(id);
        assertNotNull(title);
        assertNotNull(infoBox);
        entries.add(createEntry(id, title, infoBox));
    }

    public Stream<DashboardEntry> entries() { return entries.stream(); }

    @SuppressWarnings("unchecked")
    public <I extends InfoBox> I getItem(DashboardItemID id) {
        return (I) entries.stream()
            .filter(entry -> id.equals(entry.id))
            .map(entry -> entry.infoBox)
            .findFirst().orElse(null);
    }
}