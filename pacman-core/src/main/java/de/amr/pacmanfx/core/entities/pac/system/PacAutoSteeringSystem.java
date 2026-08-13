package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

public class PacAutoSteeringSystem {

    public void update(GameSession session, Pac pac) {
        final GameLevel level = session.assertLevel();
        if (pac.cheats().isUsingAutopilot() || session.isAttractMode()) {
            pac.autoSteering().steering().steer(pac, level);
        }
    }
}
