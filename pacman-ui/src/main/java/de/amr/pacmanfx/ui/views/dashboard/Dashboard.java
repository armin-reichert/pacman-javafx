/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.game.Game;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Dashboard {

    private final VBox rootPane = new VBox();
    private final Map<Identifier, DashboardSection> sections = new LinkedHashMap<>();

    private final ChangeListener<Boolean> visibilityChangeHandler = (_, _, _) -> updateLayout();

    private void onSectionExpandedStateChanged(DashboardSection section) {
        if (section.isDisplayedStandalone()) {
            if (section.isExpanded()) {
                sections().filter(s -> s != section).forEach(s -> s.setVisible(false));
                setCompactMode(true);
            } else {
                sections().forEach(s -> s.setVisible(true));
                setCompactMode(false);
            }
        }
    }

    public Dashboard() {
        rootPane.visibleProperty().addListener(visibilityChangeHandler);
        rootPane.setPadding(new Insets(10));
    }

    public Pane rootPane() {
        return rootPane;
    }

    public void connect(Game game) {
        sections.values().forEach(section -> section.connect(game));
    }

    public void update(Game game) {
        sections().filter(DashboardSection::isExpanded).forEach(section -> section.update(game));
    }

    public void toggleVisibility() {
        rootPane.setVisible(!rootPane.isVisible());
    }

    public Stream<DashboardSection> sections() { return sections.values().stream(); }

    public void removeSection(Identifier id) {
        requireNonNull(id);
        final DashboardSection section = sections.get(id);
        if (section != null) {
            section.visibleProperty().removeListener(visibilityChangeHandler);
            sections.remove(id);
            updateLayout();
        }
    }

    public void addSection(Identifier id, DashboardSection section) {
        requireNonNull(id);
        requireNonNull(section);
        sections.put(id, section);
        section.visibleProperty().addListener(visibilityChangeHandler);
        section.expandedProperty().addListener((_,_,_) -> onSectionExpandedStateChanged(section));
    }

    public void updateLayout() {
        final List<DashboardSection> reorderedSections = new ArrayList<>(sections.entrySet().stream()
            .filter(e -> e.getValue().isVisible())
            .filter(e -> e.getKey() != DashboardID.README)
            .filter(e -> e.getKey() != DashboardID.ABOUT)
            .map(Map.Entry::getValue)
            .toList());

        if (sections.containsKey(DashboardID.README)) {
            reorderedSections.addFirst(sections.get(DashboardID.README));
        }
        if (sections.containsKey(DashboardID.ABOUT)) {
            reorderedSections.addLast(sections.get(DashboardID.ABOUT));
        }

        rootPane.getChildren().clear();
        rootPane.getChildren().addAll(reorderedSections);
    }

    public void setCompactMode(boolean compactMode) {
        rootPane.getChildren().clear();
        if (compactMode) {
            sections().filter(Node::isVisible).forEach(rootPane.getChildren()::add);
        } else {
            sections().forEach(rootPane.getChildren()::add);
        }
    }
}