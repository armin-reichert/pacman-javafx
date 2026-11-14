/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.world;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.ui.entities3D.bonus.BonusView3D;
import de.amr.pacmanfx.ui.entities3D.bonus.BonusView3D_System;
import de.amr.pacmanfx.ui.entities3D.ghost.GhostView3D;
import de.amr.pacmanfx.ui.entities3D.ghost.GhostView3D_System;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3D;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3D_System;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3D_System;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D_System;

import static java.util.Objects.requireNonNull;

public class GameLevelView3D_UpdateSystem {

    public GameLevelView3D_UpdateSystem() {
    }

    public void updateGameLevelView3D(GameLevelView3D levelView3D, GameContext game, AnimationRegistry animationRegistry) {
        requireNonNull(levelView3D);
        requireNonNull(game);
        requireNonNull(animationRegistry);

        final CommonGameSystems systems = game.systems();
        final GameSession session = game.session();
        final GameLevelEntitySet entitySet = session.optLevel().orElseThrow().entitySet();

        final LivesCounter livesCounter = session.hud().livesCounter();
        if (livesCounter.hasComponent(LivesCounterView3D.class)) {
            final var system = systems.assertSystem(LivesCounterView3D_System.class);
            system.updateLivesCounterView3D(livesCounter);
        }

        final House house = entitySet.theOne(House.class);
        if (house.hasComponent(HouseView3D.class)) {
            final var system = systems.assertSystem(HouseView3D_System.class);
            system.update(house, entitySet);
        }

        final Pac pac = entitySet.pac();
        if (pac.hasComponent(PacView3D.class)) {
            final var system = systems.assertSystem(PacView3D_System.class);
            system.updatePacTransform(pac);
            system.updatePacAnimations(pac);
        }

        entitySet.ghosts().forEach(ghost -> {
            if (ghost.hasComponent(GhostView3D.class)) {
                final var system = systems.assertSystem(GhostView3D_System.class);
                system.updateGhostTransform(ghost);
                system.updateGhostAppearance(ghost);
            }
        });

        entitySet.anyOfType(Bonus.class).ifPresent(bonus -> {
            if (bonus.hasComponent(BonusView3D.class)) {
                final var system = systems.assertSystem(BonusView3D_System.class);
                system.updateBonus(bonus, animationRegistry);
                //TODO check this
                levelView3D.ensureBonus3DViewAddedToChildren(game, bonus, animationRegistry);
            }
        });
    }
}
