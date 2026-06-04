package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.TerrainLayer;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.Globals.HTS;
import static de.amr.pacmanfx.core.Globals.TS;

public class HuntingResolver {

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    public static void fixPacPositionIfKilledInsidePortal(GameLevel level, Pac pac) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.hPortalContainingTile(pac.computeTile()).ifPresent(hPortal -> {
            if (pac.moveDir() == Direction.LEFT) {
                pac.setX(hPortal.rightBorderEntryTile().x() * TS + HTS);
            } else if (pac.moveDir() == Direction.RIGHT) {
                pac.setX(hPortal.leftBorderEntryTile().x() * TS - HTS);
            }
            // Not sure if colliding ghosts should also be moved back to visible area
            Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
        });
    }

}
