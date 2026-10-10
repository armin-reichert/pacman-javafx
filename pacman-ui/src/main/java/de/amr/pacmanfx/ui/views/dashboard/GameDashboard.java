/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Named;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
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

    public void setExecutionContext(GameEngineContext context) {
        requireNonNull(context);
        sections().forEach(section -> section.setExecutionContext(context));
    }

    public void populate(
        DashboardFactory factory,
        List<DashboardSectionSettings> sectionDefinitions,
        TranslationManager translations)
    {
        for (var def : sectionDefinitions) {
            factory.identify(def.id()).ifPresentOrElse(dashboardID -> {
                final GameDashboardSection section = factory.createSection(this, dashboardID, translations);
                section.setDisplayedStandalone(def.standalone());
                section.setExpanded(def.expanded());
                addSection(section);
            }, () -> Logger.error("Unknown dashboard ID: {}", def.id()));
        }
    }

    public void update(GameEngineContext context) {
        requireNonNull(context);
        sections()
            .filter(GameDashboardSection::isExpanded)
            .forEach(section -> section.update(context));
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

    private Optional<GameDashboardSection> findById(Named id) {
        return sections().filter(section -> id.equals(section.id())).findFirst();
    }
}