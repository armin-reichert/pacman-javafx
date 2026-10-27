/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.world.Energizer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;

import static java.util.Objects.requireNonNull;

public final class GameSimulation {

    public void updateGame(GameContext game) {
        requireNonNull(game);
        final GameVariantRuntime runtime = game.runtime();
        game.session().optLevel().ifPresent(level -> updateLevel(runtime.systems(), game, level));
        runtime.gameFlow().update(game);
        runtime.systems().hudUpdateSystem().updateHUD(game, game.session().hud());
    }

    private void updateLevel(GameSystems systems, GameContext game, GameLevel level) {
        level.heartbeat().triggerPulse();
        updateEnergizers(level);
        updateActors(systems, game, level);
    }

    private void updateActors(GameSystems systems, GameContext game, GameLevel level) {
        final GameLevelEntitySet entities = level.entitySet();
        systems.pacUpdateSystem().update(game, level, entities.pac());
        systems.ghostUpdateSystem().update(game, level);
        entities.anyOfType(Bonus.class).ifPresent(bonus -> systems.bonusUpdateSystem().update(game, level, bonus));
        systems.lifetimeSystem().update(entities);
    }

    private void updateEnergizers(GameLevel level) {
        level.entitySet().elementsOfType(Energizer.class).forEach(
            energizer -> updateEnergizer(level, energizer));
    }

    private void updateEnergizer(GameLevel level, Energizer energizer) {
        final Vector2i tile = energizer.position().tile();
        final boolean eaten = level.food().hasEatenFoodAtTile(tile);
        final boolean pulse = level.heartbeat().state() == Pulse.State.ON;
        energizer.state().setOn(!eaten && pulse);
    }
}
