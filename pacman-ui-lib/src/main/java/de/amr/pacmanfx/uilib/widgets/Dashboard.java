/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

//TODO make a control+skin+CSS of this
public class Dashboard<S extends DashboardSection> extends VBox {

    private final List<S> sectionList = FXCollections.observableArrayList();

    private final ChangeListener<Boolean> visibilityChangeHandler = (_, _, _) -> updateSectionOrder();

    private void onSectionExpandedStateChanged(S section) {
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
        getStyleClass().add("dashboard");
        visibleProperty().addListener(visibilityChangeHandler);
        setPadding(new Insets(10));
    }

    public Stream<S> sections() {
        return sectionList.stream();
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }

    public void updateSectionOrder() {
    }

    public void addSection(S section) {
        requireNonNull(section);
        checkExists(section);
        sectionList.add(section);
        section.visibleProperty().addListener(visibilityChangeHandler);
        section.expandedProperty().addListener((_,_,_) -> onSectionExpandedStateChanged(section));
    }

    public void removeSection(S section) {
        requireNonNull(section);
        if (sectionList.remove(section)) {
            section.visibleProperty().removeListener(visibilityChangeHandler);
            updateSectionOrder();
        }
        else Logger.error("Section {} not found", section.id());
    }

    public void setCompactMode(boolean compactMode) {
        getChildren().clear();
        if (compactMode) {
            sections().filter(Node::isVisible).forEach(getChildren()::add);
        } else {
            sections().forEach(getChildren()::add);
        }
    }

    private void checkExists(S section) {
        if (sectionList.contains(section)) {
            throw new IllegalArgumentException("Section already exists: " + section.id());
        }
    }
}
