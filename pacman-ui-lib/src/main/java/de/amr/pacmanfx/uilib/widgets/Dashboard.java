/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import de.amr.basics.Identifier;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

//TODO make a control+skin+CSS of this
public class Dashboard<S extends DashboardSection> extends VBox {

    private final Map<Identifier, S> sectionMap = new LinkedHashMap<>();

    private final ChangeListener<Boolean> visibilityChangeHandler = (_, _, _) -> updateLayout();

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

    //TODO return read-only copy?
    public Map<Identifier, S> sectionMap() { return Collections.unmodifiableMap(sectionMap); }

    public Stream<S> sections() { return sectionMap.values().stream(); }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }

    public void updateLayout() {
    }

    public void addSection(Identifier id, S section) {
        requireNonNull(id);
        requireNonNull(section);
        sectionMap.put(id, section);
        section.visibleProperty().addListener(visibilityChangeHandler);
        section.expandedProperty().addListener((_,_,_) -> onSectionExpandedStateChanged(section));
    }

    public void removeSection(Identifier id) {
        requireNonNull(id);
        final S section = sectionMap.get(id);
        if (section != null) {
            section.visibleProperty().removeListener(visibilityChangeHandler);
            sectionMap.remove(id);
            updateLayout();
        }
    }

    public void setCompactMode(boolean compactMode) {
        getChildren().clear();
        if (compactMode) {
            sections().filter(Node::isVisible).forEach(getChildren()::add);
        } else {
            sections().forEach(getChildren()::add);
        }
    }
}
