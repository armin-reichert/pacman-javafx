package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;

public class PacAutoSteeringSystem {

    public void update(GameLevel level, Pac pac) {
        if (pac.cheats().isUsingAutopilot() || level.isDemoLevel()) {
            pac.autoSteering().steering().steer(pac, level);
        }
    }
}
