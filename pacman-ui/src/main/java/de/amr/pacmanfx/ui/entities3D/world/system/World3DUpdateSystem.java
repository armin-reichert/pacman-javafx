package de.amr.pacmanfx.ui.entities3D.world.system;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;

public class World3DUpdateSystem {

    public void updateEntities(GameContext game, AnimationRegistry animationRegistry) {
        final GameSession session = game.session();
        final GameLevelEntitySet entitySet = session.level().entitySet();

        updateLivesCounter3D(session.hud().livesCounter());
        updateHouse3D(entitySet.entities().theOne(House.class), entitySet);
        updatePac3D(entitySet.pac());
        updateGhosts3D(entitySet);
        updateBonus3D(entitySet.entities().anyOfTypeOrNull(Bonus.class), animationRegistry);
    }

    private void updatePac3D(Pac pac) {
        final GameSystems3D.PacSystems3D pacSystems3D = GameSystems3D.reqSystem(GameSystems3D.PacSystems3D.class);
        pacSystems3D.transform().update(pac);
        pacSystems3D.animation().updateAnimations(pac);
    }

    private void updateLivesCounter3D(LivesCounter livesCounter) {
        // Lives counter shapes follow Pac location
        final LivesCounter3DViewSystem livesCounter3DViewSystem = GameSystems3D.reqSystem(LivesCounter3DViewSystem.class);
        livesCounter3DViewSystem.update(livesCounter);
    }

    private void updateGhosts3D(GameLevelEntitySet entitySet) {
        final var ghostSystems3D = GameSystems3D.reqSystem(GameSystems3D.GhostSystems3D.class);
        entitySet.ghosts().forEach(ghost -> {
            ghostSystems3D.movement().update(ghost);
            ghostSystems3D.appearance().update(ghost);
        });
    }

    private void updateHouse3D(House house, GameLevelEntitySet entitySet) {
        final var houseSystem3D = GameSystems3D.reqSystem(House3DSystem.class);
        houseSystem3D.update(house, entitySet);
    }

    private void updateBonus3D(Bonus bonus, AnimationRegistry animationRegistry) {
        if (bonus != null) {
            GameSystems3D.reqSystem(Bonus3DUpdateSystem.class).update(bonus, animationRegistry);
        }
    }
}
