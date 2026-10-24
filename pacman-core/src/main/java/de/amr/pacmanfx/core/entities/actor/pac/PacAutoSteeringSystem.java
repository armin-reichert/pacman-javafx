package de.amr.pacmanfx.core.entities.actor.pac;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.SessionKey;

public class PacAutoSteeringSystem {

    public void update(GameSession session, Pac pac) {
        session.optLevel().ifPresent(level -> {
            if (pac.cheats().isUsingAutopilot() || session.booleanValue(SessionKey.DEMO_LEVEL_MODE)) {
                pac.autoSteering().steering().steer(pac, level);
            }
        });
    }
}
