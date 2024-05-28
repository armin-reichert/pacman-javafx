/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui3d.PacManGames3dUI;
import de.amr.games.pacman.ui3d.animation.ColorChangeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Left/right wing of ghost house door.
 *
 * @author Armin Reichert
 */
public class DoorWing3D extends Group {

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final PhongMaterial barMaterial = new PhongMaterial();
    private final Color doorColor;
    private Transition animation;

    public DoorWing3D(Vector2i tile, Color doorColor, Color floorColor) {
        checkTileNotNull(tile);
        checkNotNull(doorColor);

        this.doorColor = doorColor;
        setTranslateX(tile.x() * TS);
        setTranslateY(tile.y() * TS);

        barMaterial.setDiffuseColor(doorColor);
        barMaterial.setSpecularColor(doorColor.brighter());

        for (int i = 0; i < 2; ++i) {
            var verticalBar = new Cylinder(1, 8);
            verticalBar.setMaterial(barMaterial);
            verticalBar.setTranslateX(i * 4 + 2);
            verticalBar.setTranslateY(4);
            verticalBar.setTranslateZ(-4);
            verticalBar.setRotationAxis(Rotate.X_AXIS);
            verticalBar.setRotate(90);
            verticalBar.drawModeProperty().bind(drawModePy);
            getChildren().add(verticalBar);
        }

        var horizontalBar = new Cylinder(0.5, 14);
        horizontalBar.setMaterial(barMaterial);
        horizontalBar.setTranslateX(4);
        horizontalBar.setTranslateY(4);
        horizontalBar.setTranslateZ(-4);
        horizontalBar.setRotationAxis(Rotate.Z_AXIS);
        horizontalBar.setRotate(90);
        getChildren().add(horizontalBar);
    }

    public Color getDoorColor() {
        return doorColor;
    }

    public Transition traversalAnimation()
    {
        if (animation == null) {
            Color color = ResourceManager.opaqueColor(PacManGames3dUI.PY_3D_FLOOR_COLOR.get(), 0.5);
            var fadeOut = new ColorChangeTransition(Duration.seconds(0.5),
                doorColor, color, barMaterial.diffuseColorProperty()
            );
            var fadeIn = new ColorChangeTransition(Duration.seconds(0.5),
                color, doorColor, barMaterial.diffuseColorProperty()
            );
            fadeIn.setDelay(Duration.seconds(0.2));
            animation = new SequentialTransition(fadeOut, fadeIn);
        }
        return animation;
    }
}