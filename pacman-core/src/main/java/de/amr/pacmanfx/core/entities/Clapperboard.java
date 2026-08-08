/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.clapperboard.comp.ClapperboardInscriptionComp;
import de.amr.pacmanfx.core.entities.clapperboard.comp.ClapperboardStateComp;

/**
 * Animated movie clapperboard.
 */
public class Clapperboard extends GameEntity {

    public Clapperboard(String number, String text) {
        setComp(ClapperboardStateComp.class, new ClapperboardStateComp());
        setComp(ClapperboardInscriptionComp.class, new ClapperboardInscriptionComp());

        inscription().setNumber(number);
        inscription().setText(text);
    }

    public ClapperboardInscriptionComp inscription() {
        return requireComp(ClapperboardInscriptionComp.class);
    }

    public ClapperboardStateComp state() {
        return requireComp(ClapperboardStateComp.class);
    }
}
