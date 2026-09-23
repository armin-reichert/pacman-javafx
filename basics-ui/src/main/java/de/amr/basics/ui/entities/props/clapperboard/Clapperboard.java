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
        setComp(ClapperboardStateComp.class, new ClapperboardStateComp());
        setComp(ClapperboardInscriptionComp.class, new ClapperboardInscriptionComp());

        inscription().setNumber(number);
        inscription().setText(text);
    }

    public ClapperboardInscriptionComp inscription() {
        return reqComp(ClapperboardInscriptionComp.class);
    }

    public ClapperboardStateComp state() {
        return reqComp(ClapperboardStateComp.class);
    }
}
