/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.textdisplay;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;

public class TextView extends GameEntity {

    public TextView() {
        setComp(TextViewDataComp.class, new TextViewDataComp());
        setComp(MovementComp.class, new MovementComp());
    }

    public TextViewDataComp data() {
        return reqComp(TextViewDataComp.class);
    }

    public MovementComp movement() {
        return reqComp(MovementComp.class);
    }
}
