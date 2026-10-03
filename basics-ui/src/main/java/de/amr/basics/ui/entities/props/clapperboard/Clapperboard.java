/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.clapperboard;

import de.amr.basics.ecs.GameEntity;

/**
 * Animated movie clapperboard.
 */
public class Clapperboard extends GameEntity {

    public Clapperboard(String number, String text) {
        setComponent(ClapperboardStateComp.class, new ClapperboardStateComp());
        setComponent(ClapperboardInscriptionComp.class, new ClapperboardInscriptionComp(number, text));
    }

    public ClapperboardInscriptionComp inscription() {
        return assertComponent(ClapperboardInscriptionComp.class);
    }

    public ClapperboardStateComp state() {
        return assertComponent(ClapperboardStateComp.class);
    }
}
