package de.amr.pacmanfx.core.entities.actor.pac;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.level.GameLevel;

public class PacAutoSteeringSystem {

    public void update(GameSession session, Pac pac) {
        final GameLevel level = session.level();
        if (pac.cheats().isUsingAutopilot() || session.booleanValue(SessionKey.DEMO_LEVEL_MODE)) {
            pac.autoSteering().steering().steer(pac, level);
        }
    }
}
