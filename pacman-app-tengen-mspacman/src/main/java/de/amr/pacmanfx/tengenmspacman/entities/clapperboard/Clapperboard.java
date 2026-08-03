/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.clapperboard;

import de.amr.pacmanfx.core.ecs.GameEntity;

/**
 * Animated movie clapperboard.
 */
public class Clapperboard extends GameEntity {

    public Clapperboard(String number, String text) {
        setComponent(ClapperboardStateComp.class, new ClapperboardStateComp());
        setComponent(ClapperboardInscriptionComp.class, new ClapperboardInscriptionComp());

        inscription().setNumber(number);
        inscription().setText(text);
    }

    public ClapperboardInscriptionComp inscription() {
        return requireComponent(ClapperboardInscriptionComp.class);
    }

    public ClapperboardStateComp state() {
        return requireComponent(ClapperboardStateComp.class);
    }
}