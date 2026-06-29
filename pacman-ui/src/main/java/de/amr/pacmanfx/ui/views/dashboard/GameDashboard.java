/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.widgets.Dashboard;
import de.amr.pacmanfx.uilib.widgets.DashboardSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class GameDashboard extends Dashboard<GameDashboardSection> {

    public GameDashboard() {
        setId("game-dashboard");
    }

    public void connect(Game game) {
        requireNonNull(game);
        sections().forEach(section -> section.connect(game));
    }

    public void update(Game game) {
        requireNonNull(game);
        sections()
            .filter(GameDashboardSection::isExpanded)
            .forEach(section -> section.update(game));
    }

    public void updateLayout() {
        final Map<Identifier, GameDashboardSection> sectionMap = sectionMap();
        final List<DashboardSection> reorderedSections = new ArrayList<>(sectionMap.entrySet().stream()
            .filter(e -> e.getValue().isVisible())
            .filter(e -> e.getKey() != DashboardID.README)
            .filter(e -> e.getKey() != DashboardID.ABOUT)
            .map(Map.Entry::getValue)
            .toList());

        if (sectionMap.containsKey(DashboardID.README)) {
            reorderedSections.addFirst(sectionMap.get(DashboardID.README));
        }
        if (sectionMap.containsKey(DashboardID.ABOUT)) {
            reorderedSections.addLast(sectionMap.get(DashboardID.ABOUT));
        }
        getChildren().setAll(reorderedSections);
    }
}