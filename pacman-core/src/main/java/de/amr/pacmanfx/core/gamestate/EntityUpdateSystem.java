/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

// Preliminary central place for calling entity updates
public class EntityUpdateSystem {

    public void updateEntities(GameContext game) {
        requireNonNull(game);
        final GameSession session = game.session();
        session.optLevel().ifPresent(level -> updateLevel(game, level));
        game.variant().systems().hudUpdateSystem().update(session.hud(), game);
    }

    private void updateLevel(GameContext game, GameLevel level) {
        final GameSystems systems = game.variant().systems();

        level.heartbeat().triggerPulse();
        systems.pacUpdateSystem().update(game, level, level.entities().pac());
        systems.ghostUpdateSystem().update(game, level);
        level.entities().optBonus().ifPresent(bonus -> systems.bonusUpdateSystem().update(game, level, bonus));

        // Handle entities with limited lifetime like ghost points, bonus points etc.
        systems.lifetime().update(level.entities());
    }

}
