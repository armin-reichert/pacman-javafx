/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.views.dashboard.control.Dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GameDashboard extends Dashboard {

    public GameDashboard() {}

    public void connect(Game game) {
        sectionMap().values().forEach(section -> section.connect(game));
    }

    public void update(Game game) {
        sectionMap().values().stream()
            .filter(GameDashboardSection::isExpanded)
            .forEach(section -> section.update(game));
    }



    public void updateLayout() {
        final Map<Identifier, GameDashboardSection> sectionMap = sectionMap();
        final List<GameDashboardSection> reorderedSections = new ArrayList<>(sectionMap.entrySet().stream()
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

        rootPane().getChildren().clear();
        rootPane().getChildren().addAll(reorderedSections);
    }

}