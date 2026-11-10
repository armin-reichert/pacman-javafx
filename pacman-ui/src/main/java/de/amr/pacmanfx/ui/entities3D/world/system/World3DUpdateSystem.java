package de.amr.pacmanfx.ui.entities3D.world.system;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.systems.GhostSystems3D;
import de.amr.pacmanfx.ui.systems.PacSystems3D;

public class World3DUpdateSystem {

    public void updateEntities(GameContext game, AnimationRegistry animationRegistry) {
        final GameSession session = game.session();
        final GameLevelEntitySet entitySet = session.optLevel().orElseThrow().entitySet();

        updateLivesCounter3D(game.systems(), session.hud().livesCounter());
        updateHouse3D(game.systems(), entitySet.theOne(House.class), entitySet);
        updatePac3D(game.systems(), entitySet.pac());
        updateGhosts3D(game.systems(), entitySet);
        updateBonus3D(game.systems(), entitySet.anyOfTypeOrNull(Bonus.class), animationRegistry);
    }

    private void updatePac3D(CommonGameSystems systems, Pac pac) {
        if (pac.hasComponent(PacView3D.class)) {
            final PacSystems3D pacSystems3D = systems.assertSystem(PacSystems3D.class);
            pacSystems3D.transformSystem().update(pac);
            pacSystems3D.animationSystem().updateAnimations(pac);
        }
    }

    private void updateLivesCounter3D(CommonGameSystems systems, LivesCounter livesCounter) {
        // Lives counter shapes follow Pac location
        final LivesCounterView3DSystem livesCounterView3DSystem = systems.assertSystem(LivesCounterView3DSystem.class);
        livesCounterView3DSystem.update(livesCounter);
    }

    private void updateGhosts3D(CommonGameSystems systems, GameLevelEntitySet entitySet) {
        final var ghostSystems3D = systems.assertSystem(GhostSystems3D.class);
        entitySet.ghosts().forEach(ghost -> {
            ghostSystems3D.movementSystem().update(ghost);
            ghostSystems3D.viewSystem().updateAppearance(ghost);
        });
    }

    private void updateHouse3D(CommonGameSystems systems, House house, GameLevelEntitySet entitySet) {
        final var houseSystem3D = systems.assertSystem(House3DSystem.class);
        houseSystem3D.update(house, entitySet);
    }

    private void updateBonus3D(CommonGameSystems systems, Bonus bonus, AnimationRegistry animationRegistry) {
        if (bonus != null) {
            systems.assertSystem(Bonus3DUpdateSystem.class).update(bonus, animationRegistry);
        }
    }
}
