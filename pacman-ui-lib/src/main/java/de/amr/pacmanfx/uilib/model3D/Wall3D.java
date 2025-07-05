/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;

public class Wall3D extends Group implements  Destroyable {

    public Wall3D(Node base, Node top) {
        getChildren().setAll(base, top);
    }

    @Override
    public void destroy() {
        getChildren().forEach(child -> {
            switch (child) {
                case Box box -> {
                    box.depthProperty().unbind();
                    box.setMaterial(null);
                    box.translateXProperty().unbind();
                    box.translateYProperty().unbind();
                    box.translateZProperty().unbind();
                }
                case Cylinder cylinder -> {
                    cylinder.heightProperty().unbind();
                    cylinder.setMaterial(null);
                    cylinder.translateXProperty().unbind();
                    cylinder.translateYProperty().unbind();
                    cylinder.translateZProperty().unbind();
                }
                case Shape3D shape3D -> {
                    shape3D.setMaterial(null);
                    shape3D.translateXProperty().unbind();
                    shape3D.translateYProperty().unbind();
                    shape3D.translateZProperty().unbind();
                }
                default -> {}
            }
        });
        getChildren().clear();
    }
}
