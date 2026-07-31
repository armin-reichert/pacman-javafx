/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.comp.pac;

import de.amr.pacmanfx.core.model.GameEntityComponent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class PacCheatsComp implements GameEntityComponent {

    private final BooleanProperty immune = new SimpleBooleanProperty(false);

    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);

    public BooleanProperty immuneProperty() {
        return immune;
    }

    public boolean isImmune() {
        return immune.get();
    }

    public void setImmune(boolean value) {
        immuneProperty().set(value);
    }

    public BooleanProperty usingAutopilotProperty() {
        return usingAutopilot;
    }

    public boolean isUsingAutopilot() {
        return usingAutopilot.get();
    }

    public void setUsingAutopilot(boolean value) {
        usingAutopilotProperty().set(value);
    }

    @Override
    public void reset() {
        immune.set(false);
        //usingAutopilot.set(false);
    }
}
