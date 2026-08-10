package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.gamescene.d3.entities.livescounter.LivesCounterView3DSystem;
import de.amr.pacmanfx.uilib.entities3D.bonus.system.Bonus3DMovementSystem;
import de.amr.pacmanfx.uilib.entities3D.bonus.system.Bonus3DViewSystem;
import de.amr.pacmanfx.uilib.entities3D.ghost.system.Ghost3DAppearanceSystem;
import de.amr.pacmanfx.uilib.entities3D.ghost.system.Ghost3DMovementSystem;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.house.system.House3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DTransformSystem;

import java.util.Set;

public class GameLevel3DUpdateController {

    private static final Set<GhostState> GHOST_STATES_WITH_ACCESS_TO_HOUSE = Set.of(
        GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    private static final Set<GhostState> GHOST_STATES_REQUIRING_HOUSE_LIGHTING = Set.of(
        GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);


    public static void update3DSceneEntities(GameLevel3D level3D) {
        updateLivesCounter3D(level3D);
        updateHouse3D(level3D);
        updatePac3D(level3D);
        updateGhosts3D(level3D);
        updateBonus3D(level3D);
    }

    private static void updatePac3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final Pac pac = level.entities().pac();
        Pac3DTransformSystem.update(pac, level);
        Pac3DAnimationSystem.update(pac);
        Pac3DAnimationSystem.updatePowerLight(pac);
    }

    private static void updateLivesCounter3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
        LivesCounterView3DSystem.update(livesCounter);
    }

    private static void updateGhosts3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        level.entities().ghosts().forEach(ghost -> {
            Ghost3DMovementSystem.update(ghost);
            Ghost3DAppearanceSystem.update(ghost);
        });
    }

    private static void updateHouse3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final House house = level.entities().theOne(House.class);

        boolean accessRequested = level.ghostsInAnyOfStates(GHOST_STATES_WITH_ACCESS_TO_HOUSE)
            .filter(ghost -> house.isDoorAt(WorldNavigationSystem.computeTile(ghost)))
            .anyMatch(GameEntity::isVisible);

        boolean ghostNearHouseDoor = level.ghostsInAnyOfStates(GHOST_STATES_REQUIRING_HOUSE_LIGHTING)
            .filter(ghost -> ghostIsNearHouseDoor(house, ghost))
            .anyMatch(GameEntity::isVisible);

        House3DSystem.showLight(house, ghostNearHouseDoor);
        House3DAnimationSystem.update(house, accessRequested);
    }

    private static boolean ghostIsNearHouseDoor(House house, Ghost ghost) {
        final House3DViewComp view3D = house.requireComp(House3DViewComp.class);
        final Vector2f houseEntryPos = house.floorplan().entryPosition();
        return ghost.pos().asVector2f().euclideanDist(houseEntryPos) <= view3D.doorSensitivity();
    }

    private static void updateBonus3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        level.optBonus().ifPresent(bonus -> {
            level3D.ensureBonus3DViewExists(bonus);
            Bonus3DMovementSystem.update(bonus);
            Bonus3DViewSystem.update(bonus, level3D.animationManager().registry());
        });
    }
}
