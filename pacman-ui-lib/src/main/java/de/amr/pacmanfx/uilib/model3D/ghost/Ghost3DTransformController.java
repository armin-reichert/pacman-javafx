/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class Ghost3DTransformController {

    private static final double HEIGHT_OVER_FLOOR = 2.0;

    private ChangeListener<Vector2f> positionListener;
    private ChangeListener<Direction> wishDirListener;


    private boolean listenersRegistered;

    public Ghost3DTransformController() {}

    public void init(Ghost3D ghost3D, WorldMap worldMap) {
        addListeners(ghost3D, worldMap);
        update(ghost3D, worldMap);
    }

    private void addListeners(Ghost3D ghost3D, WorldMap worldMap) {
        if (listenersRegistered) return;

        positionListener = (_,_,_) -> update(ghost3D, worldMap);
        ghost3D.ghost().positionProperty().addListener(new WeakChangeListener<>(positionListener));

        wishDirListener = (_,_,_) -> update(ghost3D, worldMap);
        ghost3D.ghost().wishDirProperty().addListener(new WeakChangeListener<>(wishDirListener));

        listenersRegistered = true;
    }

    public void update(Ghost3D ghost3D, WorldMap worldMap) {
        final Ghost ghost = ghost3D.ghost();
        final Vector2f center = ghost.center();

        ghost3D.setTranslateX(center.x());
        ghost3D.setTranslateY(center.y());
        ghost3D.setTranslateZ(-(ghost3D.config().size3D() / 2 + HEIGHT_OVER_FLOOR));

        ghost3D.facingRotation().setAngle(switch (ghost.wishDir()) {
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
