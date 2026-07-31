/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.comp.common;

import de.amr.pacmanfx.core.model.GameEntityComponent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class VisibilityComp implements GameEntityComponent {

    private final boolean defaultVisibility;

    private BooleanProperty visible;

    public VisibilityComp(boolean defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    @Override
    public void reset() {
        visibleProperty().set(defaultVisibility);
    }

    public BooleanProperty visibleProperty() {
        if (visible == null) {
            visible = new SimpleBooleanProperty(defaultVisibility);
        }
        return visible;
    }

    public boolean isVisible() {
        return visible == null ? defaultVisibility : visibleProperty().get();
    }

    public final void set(boolean value) {
        if (visible == null && defaultVisibility == value) return;
        visibleProperty().set(value);
    }

    @Override
    public String toString() {
        return "Visibility{" +
            "defaultVisibility=" + defaultVisibility +
            ", visible=" + isVisible() +
            '}';
    }
}
