package de.amr.pacmanfx.uilib.entities3D.house.system;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.ghost.GhostState;
import de.amr.pacmanfx.core.entities.house.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;

import java.util.Set;

public class House3DSystem {

    public static void hideDoors(House house) {
        final House3DViewComp view3D = house.requireComponent(House3DViewComp.class);

        view3D.setDoorsVisible(false);
    }

    /**
     * Updates the house state based on the current game level.
     * <p>
     * This method:
     * <ul>
     *   <li>Activates the interior light when ghost access is required</li>
     *   <li>Opens the doors when a ghost approaches the entry</li>
     * </ul>
     */
    public static void update(House house, boolean accessRequested, boolean ghostNearHouseEntry) {
        final House3DViewComp view3D = house.requireComponent(House3DViewComp.class);
        view3D.light().lightOnProperty().set(accessRequested);
        view3D.doorsOpenProperty().set(ghostNearHouseEntry);
    }

}
