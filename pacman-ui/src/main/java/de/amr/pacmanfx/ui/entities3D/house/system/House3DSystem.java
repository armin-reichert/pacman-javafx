package de.amr.pacmanfx.ui.entities3D.house.system;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DViewComp;

import java.util.Set;

public class House3DSystem {

    private static final Set<GhostState> GHOST_STATES_WITH_ACCESS_TO_HOUSE = Set.of(
        GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    private static final Set<GhostState> GHOST_STATES_REQUIRING_HOUSE_LIGHTING = Set.of(
        GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    public void hideDoors(House house) {
        final House3DViewComp view3D = house.assertComponent(House3DViewComp.class);
        view3D.setDoorsVisible(false);
    }

    public void update(House house, GameLevelEntitySet entitySet) {
        final House3DAnimationComp animation = house.assertComponent(House3DAnimationComp.class);
        boolean accessRequested = entitySet.ghostsInAnyOfStates(GHOST_STATES_WITH_ACCESS_TO_HOUSE)
            .filter(ghost -> house.isDoorAt(ghost.pos().tile()))
            .anyMatch(GameEntity::isVisible);
        if (accessRequested) {
            if (!animation.doorsMeltingAnimation().isRunning()) {
                playDoorsMeltingAnimation(house);
            }
        }
        updateLight(house, entitySet);
    }

    private void updateLight(House house, GameLevelEntitySet entitySet) {
        final House3DViewComp view3D = house.assertComponent(House3DViewComp.class);
        boolean ghostNearHouseDoor = entitySet.ghostsInAnyOfStates(GHOST_STATES_REQUIRING_HOUSE_LIGHTING)
            .filter(ghost -> ghostIsNearHouseDoor(house, ghost))
            .anyMatch(GameEntity::isVisible);
        view3D.light().lightOnProperty().set(ghostNearHouseDoor);
    }

    private boolean ghostIsNearHouseDoor(House house, Ghost ghost) {
        final House3DViewComp view3D = house.assertComponent(House3DViewComp.class);
        final Vector2f houseEntryPos = house.floorplan().entryPosition();
        return ghost.pos().asVector2f().euclideanDist(houseEntryPos) <= view3D.doorSensitivity();
    }

    private void playDoorsMeltingAnimation(House house) {
        final House3DAnimationComp animation = house.assertComponent(House3DAnimationComp.class);
        animation.doorsMeltingAnimation().replay();
    }
}
