/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class Ghost3DTransformController {

    private static final double HEIGHT_OVER_FLOOR = 2.0;

    private final Ghost3D ghost3D;
    private final Rotate facingRotation = new Rotate(0, Rotate.Z_AXIS);

    private final ChangeListener<Vector2f> positionListener;
    private final ChangeListener<Direction> wishDirListener;

    public Ghost3DTransformController(Ghost3D ghost3D, WorldMap worldMap) {
        this.ghost3D = requireNonNull(ghost3D);
        ghost3D.facingGroup().getTransforms().setAll(facingRotation, PacManWorld3D.ORIENTATION_ADJUSTMENT);
        final Ghost ghost = ghost3D.ghost();

        positionListener = (_,_,_) -> update(worldMap);
        ghost.positionProperty().addListener(new WeakChangeListener<>(positionListener));

        wishDirListener = (_,_,_) -> update(worldMap);
        ghost.wishDirProperty().addListener(new WeakChangeListener<>(wishDirListener));
    }

    public void update(WorldMap worldMap) {
        final Ghost ghost = ghost3D.ghost();
        final Vector2f center = ghost.center();

        ghost3D.setTranslateX(center.x());
        ghost3D.setTranslateY(center.y());
        ghost3D.setTranslateZ(-(ghost3D.config().size3D() / 2 + HEIGHT_OVER_FLOOR));

        facingRotation.setAngle(switch (ghost.wishDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });

        final boolean outsideWorld = ghost3D.getTranslateX() < HTS
            || ghost3D.getTranslateX() > TS * worldMap.numCols() - HTS;
        ghost3D.setVisible(ghost.isVisible() && !outsideWorld);
    }
}
