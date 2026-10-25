/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Named;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.settings.ui.DashboardSectionSettings;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameDashboard extends Dashboard<GameDashboardSection> {

    public GameDashboard() {
        setId("game-dashboard");
    }

    public void populate(
        EngineContext engine,
        DashboardFactory dashboardFactory,
        List<DashboardSectionSettings> sectionSettings)
    {
        if (dashboardFactory == null) {
            Logger.error("Cannot populate dashboard: factory not set");
            return;
        }
        for (var def : sectionSettings) {
            dashboardFactory.identify(def.id()).ifPresentOrElse(dashboardID -> {
                final GameDashboardSection section = dashboardFactory.createSection(this, dashboardID, engine.translationManager());
                section.setDisplayedStandalone(def.standalone());
                section.setExpanded(def.expanded());
                addSection(section);
            }, () -> Logger.error("Unknown dashboard ID: {}", def.id()));
        }
    }

    public void init(EngineContext engine) {
        sections().forEach(section -> section.init(engine));
    }

    public void update(EngineContext engine) {
        requireNonNull(engine);
        sections()
            .filter(GameDashboardSection::isExpanded)
            .forEach(section -> section.update(engine));
    }

    public void reorderSections() {
        final List<GameDashboardSection> reorderedSections = new ArrayList<>(sections()
            .filter(DashboardSection::isVisible)
            .filter(section -> section.id() != DashboardID.README)
            .filter(section -> section.id() != DashboardID.ABOUT)
            .toList());

        findById(DashboardID.README).ifPresent(reorderedSections::addFirst);
        findById(DashboardID.ABOUT).ifPresent(reorderedSections::addLast);
        getChildren().setAll(reorderedSections);
    }

    private Optional<GameDashboardSection> findById(Named id) {
        return sections().filter(section -> id.equals(section.id())).findFirst();
    }
}