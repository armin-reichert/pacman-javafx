/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class VisibilityComp implements GameEntityComponent {

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
