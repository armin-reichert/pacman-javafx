/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2i;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * @author Armin Reichert
 */
public class Door3D extends Group {

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    private final DoubleProperty barThicknessPy = new SimpleDoubleProperty(0.75);
    private final PhongMaterial barMaterial;

    public Door3D(Vector2i leftWingTile, Vector2i rightWingTile, Color color) {
        barMaterial = new PhongMaterial();
        barMaterial.setDiffuseColor(color);
        barMaterial.specularColorProperty().bind(barMaterial.diffuseColorProperty().map(Color::brighter));

        var leftWing = createDoorWing(leftWingTile);
        var rightWing = createDoorWing(rightWingTile);
        getChildren().addAll(leftWing, rightWing);
    }

    public void playTraversalAnimation() {
        new Timeline(
            new KeyFrame(Duration.seconds(0.5),
                new KeyValue(barThicknessPy, 0)),
            new KeyFrame(Duration.seconds(1),
                        new KeyValue(barThicknessPy, 0)),
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(barThicknessPy, 0.75))
        ).play();
    }

    private Group createDoorWing(Vector2i tile) {
        var group = new Group();

        group.setTranslateX(tile.x() * TS);
        group.setTranslateY(tile.y() * TS);

        for (int i = 0; i < 2; ++i) {
            var verticalBar = new Cylinder(barThicknessPy.get(), GameLevel3D.HOUSE_HEIGHT);
            verticalBar.radiusProperty().bind(barThicknessPy);
            verticalBar.setMaterial(barMaterial);
            verticalBar.setTranslateX(i * 4 + 2);
            verticalBar.setTranslateY(4);
            verticalBar.translateZProperty().bind(verticalBar.heightProperty().multiply(-0.5));
            verticalBar.setRotationAxis(Rotate.X_AXIS);
            verticalBar.setRotate(90);
            verticalBar.drawModeProperty().bind(drawModePy);
            group.getChildren().add(verticalBar);
        }

        var horizontalBar = new Cylinder(barThicknessPy.get(), 14);
        horizontalBar.radiusProperty().bind(barThicknessPy);
        horizontalBar.setMaterial(barMaterial);
        horizontalBar.setTranslateX(4);
        horizontalBar.setTranslateY(4);
        horizontalBar.setTranslateZ(0.25 - horizontalBar.getHeight() * 0.5);
        horizontalBar.setRotationAxis(Rotate.Z_AXIS);
        horizontalBar.setRotate(90);
        horizontalBar.drawModeProperty().bind(drawModePy);
        group.getChildren().add(horizontalBar);

        return group;
    }
}