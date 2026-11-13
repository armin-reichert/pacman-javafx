package de.amr.pacmanfx.ui.entities3D.world.system;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
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

import java.util.List;

import static java.util.Objects.requireNonNull;

public class World3DUpdateSystem {

    public void updateEntities(GameContext game, AnimationRegistry animationRegistry) {
        requireNonNull(game);
        requireNonNull(animationRegistry);

        final CommonGameSystems systems = game.systems();
        final GameSession session = game.session();
        final GameLevelEntitySet entitySet = session.optLevel().orElseThrow().entitySet();

        updateLivesCounterView3D(systems.assertSystem(LivesCounterView3D_System.class), session.hud().livesCounter());
        updateHouseView3D(systems.assertSystem(HouseView3D_System.class), entitySet.theOne(House.class), entitySet);
        updatePacView3D(systems.assertSystem(PacView3D_System.class), entitySet.pac());
        updateGhosts3D(systems.assertSystem(GhostView3D_System.class), entitySet.ghosts().toList());
        entitySet.anyOfType(Bonus.class).ifPresent(
            bonus -> updateBonus3D(systems.assertSystem(BonusView3D_System.class), bonus, animationRegistry));
    }

    private void updatePacView3D(PacView3D_System system, Pac pac) {
        if (pac.hasComponent(PacView3D.class)) {
            system.updatePacTransform(pac);
            system.updatePacAnimations(pac);
        }
    }

    private void updateLivesCounterView3D(LivesCounterView3D_System system, LivesCounter livesCounter) {
        if (livesCounter.hasComponent(LivesCounterView3D.class)) {
            system.updateLivesCounterView3D(livesCounter);
        }
    }

    private void updateGhosts3D(GhostView3D_System system, List<Ghost> ghosts) {
        ghosts.forEach(ghost -> {
            if (ghost.hasComponent(GhostView3D.class)) {
                system.updateGhostTransform(ghost);
                system.updateGhostAppearance(ghost);
            }
        });
    }

    private void updateHouseView3D(HouseView3D_System system, House house, GameLevelEntitySet entitySet) {
        if (house.hasComponent(HouseView3D.class)) {
            system.update(house, entitySet);
        }
    }

    private void updateBonus3D(BonusView3D_System system, Bonus bonus, AnimationRegistry animationRegistry) {
        if (bonus.hasComponent(BonusView3D.class)) {
            system.update(bonus, animationRegistry);
        }
    }
}
