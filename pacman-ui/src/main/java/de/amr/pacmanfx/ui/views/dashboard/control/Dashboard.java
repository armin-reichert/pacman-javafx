/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard.control;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboardSection;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

//TODO make a control+skin+CSS of this
public class Dashboard {

    private final Map<Identifier, GameDashboardSection> sectionMap = new LinkedHashMap<>();
    private final VBox rootPane = new VBox();

    private final ChangeListener<Boolean> visibilityChangeHandler = (_, _, _) -> updateLayout();

    private void onSectionExpandedStateChanged(GameDashboardSection section) {
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

    //TODO return read-only copy?
    public Map<Identifier, GameDashboardSection> sectionMap() { return sectionMap; }

    public Stream<GameDashboardSection> sections() { return sectionMap.values().stream(); }

    public void toggleVisibility() {
        rootPane.setVisible(!rootPane.isVisible());
    }

    public void updateLayout() {
    }

    public void addSection(Identifier id, GameDashboardSection section) {
        requireNonNull(id);
        requireNonNull(section);
        sectionMap.put(id, section);
        section.visibleProperty().addListener(visibilityChangeHandler);
        section.expandedProperty().addListener((_,_,_) -> onSectionExpandedStateChanged(section));
    }

    public void removeSection(Identifier id) {
        requireNonNull(id);
        final GameDashboardSection section = sectionMap.get(id);
        if (section != null) {
            section.visibleProperty().removeListener(visibilityChangeHandler);
            sectionMap.remove(id);
            updateLayout();
        }
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
