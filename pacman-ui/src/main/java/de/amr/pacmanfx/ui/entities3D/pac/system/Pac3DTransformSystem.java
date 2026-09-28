/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.pac.system;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DViewComp;
import javafx.scene.transform.Rotate;

import static de.amr.basics.TileDimension.HTS;
import static de.amr.basics.TileDimension.TS;
import static java.util.Objects.requireNonNull;

public class Pac3DTransformSystem {

    public void init(Pac pac, WorldMap worldMap) {
        requireNonNull(pac);
        requireNonNull(worldMap);

        final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
        view3D.root().setScaleX(1.0);
        view3D.root().setScaleY(1.0);
        view3D.root().setScaleZ(1.0);

        update(pac, worldMap);
    }

    public void update(Pac pac, WorldMap worldMap) {
        requireNonNull(pac);
        requireNonNull(worldMap);

        final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
        final Vector2f center = pac.pos().bodyCenter();

        if (pac.state().enumValue() == PacState.ACTIVE) {
            updateVisibility(pac, center, worldMap);
            updatePosition(view3D, center);
            final Direction moveDir = pac.worldNavigation().moveDir();
            if (moveDir != null) {
                updateFacing(view3D, moveDir);
            }
        }
    }

    private void updateVisibility(Pac pac, Vector2f center, WorldMap worldMap) {
        final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
        final boolean outside = center.x() < HTS
            || center.x() > TS * worldMap.numCols() - HTS;
        view3D.root().setVisible(pac.isVisible() && !outside);
    }

    private void updatePosition(Pac3DViewComp view3D, Vector2f center) {
        view3D.root().setTranslateX(center.x());
        view3D.root().setTranslateY(center.y());
        view3D.root().setTranslateZ(-8); //TODO should depend on size
    }
    
    private void updateFacing(Pac3DViewComp view3D, Direction dir) {
        final int angle = switch (dir) {
            case LEFT -> 0;
            case UP -> 90;
            case RIGHT -> 180;
            case DOWN -> 270;
        };
        view3D.root().setRotationAxis(Rotate.Z_AXIS);
        view3D.root().setRotate(angle);
    }
}
