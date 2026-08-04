package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;

public class PacAutoSteeringSystem {

    public void update(GameLevel level, Pac pac) {
        if (pac.cheats().isUsingAutopilot() || level.isDemoLevel()) {
            pac.autoSteering().steering().steer(pac, level);
        }
    }
}
