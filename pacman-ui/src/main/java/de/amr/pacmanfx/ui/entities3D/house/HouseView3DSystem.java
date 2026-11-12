package de.amr.pacmanfx.ui.entities3D.house;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;

import java.util.Set;

public class HouseView3DSystem {

    private static final Set<GhostState> GHOST_STATES_WITH_ACCESS_TO_HOUSE = Set.of(
        GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    private static final Set<GhostState> GHOST_STATES_REQUIRING_HOUSE_LIGHTING = Set.of(
        GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    public void hideDoors(House house) {
        final HouseView3D view3D = house.assertComponent(HouseView3D.class);
        view3D.setDoorsVisible(false);
    }

    public void update(House house, GameLevelEntitySet entitySet) {
        final House3DAnimation animation = house.assertComponent(House3DAnimation.class);
        boolean accessRequested = entitySet.ghostsInAnyOfStates(GHOST_STATES_WITH_ACCESS_TO_HOUSE)
            .filter(ghost -> house.isDoorAt(ghost.position().tile()))
            .anyMatch(GameEntity::isVisible);
        if (accessRequested) {
            if (!animation.isRunning()) {
                animation.replay();
            }
        }
        updateLight(house, entitySet);
    }

    private void updateLight(House house, GameLevelEntitySet entitySet) {
        final HouseView3D view3D = house.assertComponent(HouseView3D.class);
        boolean ghostNearHouseDoor = entitySet.ghostsInAnyOfStates(GHOST_STATES_REQUIRING_HOUSE_LIGHTING)
            .filter(ghost -> ghostIsNearHouseDoor(house, ghost))
            .anyMatch(GameEntity::isVisible);
        view3D.light().lightOnProperty().set(ghostNearHouseDoor);
    }

    private boolean ghostIsNearHouseDoor(House house, Ghost ghost) {
        final HouseView3D view3D = house.assertComponent(HouseView3D.class);
        final Vector2f houseEntryPos = house.floorplan().entryPosition();
        return ghost.position().asVector2f().euclideanDist(houseEntryPos) <= view3D.doorSensitivity();
    }
}
