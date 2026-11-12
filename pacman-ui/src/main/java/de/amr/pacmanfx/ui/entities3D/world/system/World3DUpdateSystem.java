package de.amr.pacmanfx.ui.entities3D.world.system;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.BonusView3D;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3D;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3DSystem;
import de.amr.pacmanfx.ui.entities3D.levelcounter.LevelCounterView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D;
import de.amr.pacmanfx.ui.entities3D.ghost.system.GhostSystems3D;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D_System;

import static java.util.Objects.requireNonNull;

public class World3DUpdateSystem {

    public void updateEntities(GameContext game, AnimationRegistry animationRegistry) {
        requireNonNull(game);
        requireNonNull(animationRegistry);

        final CommonGameSystems systems = game.systems();
        final GameSession session = game.session();
        final GameLevelEntitySet entitySet = session.optLevel().orElseThrow().entitySet();

        updateLivesCounterView3D(systems.assertSystem(LivesCounterView3DSystem.class), session.hud().livesCounter());
        updateHouseView3D(systems.assertSystem(HouseView3DSystem.class), entitySet.theOne(House.class), entitySet);
        updatePacView3D(systems.assertSystem(PacView3D_System.class), entitySet.pac());
        updateGhosts3D(systems.assertSystem(GhostSystems3D.class), entitySet);
        updateBonus3D(systems.assertSystem(Bonus3DUpdateSystem.class), entitySet.anyOfTypeOrNull(Bonus.class), animationRegistry);
    }

    private void updatePacView3D(PacView3D_System pacView3DSystem, Pac pac) {
        if (pac.hasComponent(PacView3D.class)) {
            pacView3DSystem.updateTransform(pac);
            pacView3DSystem.updateAnimations(pac);
        }
    }

    private void updateLivesCounterView3D(LivesCounterView3DSystem livesCounterView3DSystem, LivesCounter livesCounter) {
        if (livesCounter.hasComponent(LevelCounterView3D.class)) {
            livesCounterView3DSystem.update(livesCounter);
        }
    }

    private void updateGhosts3D(GhostSystems3D ghostSystems3D, GameLevelEntitySet entitySet) {
        entitySet.ghosts().forEach(ghost -> {
            ghostSystems3D.movementSystem().update(ghost);
            ghostSystems3D.viewSystem().updateAppearance(ghost);
        });
    }

    private void updateHouseView3D(HouseView3DSystem houseSystem3D, House house, GameLevelEntitySet entitySet) {
        if (house.hasComponent(HouseView3D.class)) {
            houseSystem3D.update(house, entitySet);
        }
    }

    private void updateBonus3D(Bonus3DUpdateSystem bonusView3DSystem, Bonus bonus, AnimationRegistry animationRegistry) {
        if (bonus != null && bonus.hasComponent(BonusView3D.class)) {
            bonusView3DSystem.update(bonus, animationRegistry);
        }
    }
}
