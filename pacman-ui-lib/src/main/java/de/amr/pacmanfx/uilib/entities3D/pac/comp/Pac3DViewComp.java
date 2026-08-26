/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.comp;

import de.amr.basics.math.Vector3f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.GameEntityComp;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;

public class Pac3DViewComp implements GameEntityComp {

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private final Group root;

    private final Group bodyGroup;

    private final PointLight powerLight = new PointLight();

    private Vector3f center;

    public Pac3DViewComp() {
        root = new Group();
        bodyGroup = new Group();

        root.getChildren().setAll(bodyGroup);

        Ufx.bindDrawMode(bodyGroup, drawMode);
        //Ufx.bindDrawMode(jaw, drawMode);
    }

    public void setBodyAndJaw(Group body, Group jaw) {
        bodyGroup.getChildren().setAll(body, jaw);
    }

    public Group jaw() {
        return (Group) bodyGroup.getChildren().get(1);
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawMode;
    }

    public Group root() {
        return root;
    }

    public Group bodyGroup() {
        return bodyGroup;
    }

    public PointLight powerLight() {
        return powerLight;
    }

    public Vector3f center() {
        return center;
    }

    public void setCenter(Vector3f center) {
        this.center = center;
    }
}
