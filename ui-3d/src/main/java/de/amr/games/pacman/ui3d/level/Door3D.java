/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.animation.ColorChangeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
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

    private final ObjectProperty<Color> animatedColorPy = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> fadedOutColorPy = new SimpleObjectProperty<>();
    private final Color normalColor;

    public Door3D(Vector2i leftWingTile, Vector2i rightWingTile, Color normalColor, ObjectProperty<Color> fadedOutColorPy) {
        this.normalColor = normalColor;
        this.animatedColorPy.set(normalColor);
        this.fadedOutColorPy.bind(fadedOutColorPy.map(color -> Ufx.opaqueColor(color, 0.1)));

        var material = new PhongMaterial();
        material.diffuseColorProperty().bind(animatedColorPy);
        material.specularColorProperty().bind(material.diffuseColorProperty().map(Color::brighter));

        var leftWing = createDoorWing(leftWingTile, material, drawModePy);
        var rightWing = createDoorWing(rightWingTile, material, drawModePy);
        getChildren().addAll(leftWing, rightWing);
    }

    public void playTraversalAnimation() {
        var fadeOut = new ColorChangeTransition(Duration.seconds(0.5),
            normalColor, fadedOutColorPy.get(), animatedColorPy
        );
        var fadeIn = new ColorChangeTransition(Duration.seconds(0.5),
            fadedOutColorPy.get(), normalColor, animatedColorPy
        );
        fadeIn.setDelay(Duration.seconds(0.25));
        new SequentialTransition(fadeOut, fadeIn).play();
    }

    private static Group createDoorWing(Vector2i tile, Material material, ObjectProperty<DrawMode> drawModePy) {
        var group = new Group();

        group.setTranslateX(tile.x() * TS);
        group.setTranslateY(tile.y() * TS);

        for (int i = 0; i < 2; ++i) {
            var verticalBar = new Cylinder(0.75, GameLevel3D.HOUSE_HEIGHT);
            verticalBar.setMaterial(material);
            verticalBar.setTranslateX(i * 4 + 2);
            verticalBar.setTranslateY(4);
            verticalBar.translateZProperty().bind(verticalBar.heightProperty().multiply(-0.5));
            verticalBar.setRotationAxis(Rotate.X_AXIS);
            verticalBar.setRotate(90);
            verticalBar.drawModeProperty().bind(drawModePy);
            group.getChildren().add(verticalBar);
        }

        var horizontalBar = new Cylinder(0.5, 14);
        horizontalBar.setMaterial(material);
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