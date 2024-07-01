/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.animation.ColorChangeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;

import static de.amr.games.pacman.ui3d.PacManGames3dUI.PY_3D_DRAW_MODE;

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

        var leftWing = new DoorWing3D(leftWingTile);
        leftWing.colorPy.bind(animatedColorPy);
        leftWing.drawModePy.bind(drawModePy);

        var rightWing = new DoorWing3D(rightWingTile);
        rightWing.colorPy.bind(animatedColorPy);
        rightWing.drawModePy.bind(PY_3D_DRAW_MODE);

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
}