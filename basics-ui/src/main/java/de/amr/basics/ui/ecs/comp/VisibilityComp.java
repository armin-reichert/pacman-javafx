/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.ecs.comp;

import de.amr.basics.ui.ecs.GameEntityComp;

public class VisibilityComp implements GameEntityComp {

    private final boolean defaultVisibility;

    private boolean visible;

    public VisibilityComp(boolean defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    @Override
    public void reset() {
        visible = defaultVisibility;
    }

    public boolean isVisible() {
        return visible;
    }

    public final void set(boolean value) {
        visible = value;
    }

    @Override
    public String toString() {
        return "Visibility{" +
            "defaultVisibility=" + defaultVisibility +
            ", visible=" + isVisible() +
            '}';
    }
}
