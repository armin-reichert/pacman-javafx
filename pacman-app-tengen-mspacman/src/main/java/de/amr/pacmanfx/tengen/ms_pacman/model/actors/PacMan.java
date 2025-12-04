/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model.actors;

import de.amr.pacmanfx.model.actors.Pac;

public class PacMan extends Pac {

    public PacMan() {
        super("Pac-Man");
        reset();
        setRestingTicksAfterEnergizerEaten(0); //TODO correct?
        setRestingTicksAfterPelletEaten(0); //TODO correct?
    }
}
