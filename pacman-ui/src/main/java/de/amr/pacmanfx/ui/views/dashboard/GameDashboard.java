/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.uilib.widgets.Dashboard;
import de.amr.pacmanfx.uilib.widgets.DashboardSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameDashboard extends Dashboard<GameDashboardSection> {

    public GameDashboard() {
        setId("game-dashboard");
    }

    public void connect(PacManGamesCollection game) {
        requireNonNull(game);
        sections().forEach(section -> section.connect(game));
    }

    public void update(PacManGamesCollection game) {
        requireNonNull(game);
        sections()
            .filter(GameDashboardSection::isExpanded)
            .forEach(section -> section.update(game));
    }

    public void updateSectionOrder() {
        final List<GameDashboardSection> reorderedSections = new ArrayList<>(sections()
            .filter(DashboardSection::isVisible)
            .filter(section -> section.id() != DashboardID.README)
            .filter(section -> section.id() != DashboardID.ABOUT)
            .toList());

        findById(DashboardID.README).ifPresent(reorderedSections::addFirst);
        findById(DashboardID.ABOUT).ifPresent(reorderedSections::addLast);
        getChildren().setAll(reorderedSections);
    }

    private Optional<GameDashboardSection> findById(Identifier id) {
        return sections().filter(section -> id.equals(section.id())).findFirst();
    }
}