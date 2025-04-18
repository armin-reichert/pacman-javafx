/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.model3D.Model3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.Globals.HTS;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.uilib.model3D.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class PacShape3D extends Group {

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final double initialZ;
    private final Node jaw;
    private final Rotate moveRotation = new Rotate();
    private final Animation chewingAnimation;

    public PacShape3D(Model3D model3D, double size, Color headColor, Color palateColor) {
        initialZ = -0.5 * size;
        jaw = PacModel3D.createPacSkull(model3D, size, headColor, palateColor);
        meshViewById(jaw, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        getChildren().add(jaw);
        getTransforms().add(moveRotation);
        setTranslateZ(initialZ);

        chewingAnimation = createChewingTimeline();
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawModePy;
    }

    public void stopChewingAndOpenMouth() {
        stopChewing();
        jaw.setRotationAxis(Rotate.Y_AXIS);
        jaw.setRotate(0);
    }

    public void chew() {
        chewingAnimation.play();
    }

    public void stopChewing() {
        chewingAnimation.stop();
    }

    public void updatePosition(Pac pac) {
        Vector2f center = pac.position().plus(HTS, HTS);
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(initialZ);
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(pac.moveDir()));
    }

    public void updateVisibility(Pac pac, GameLevel level) {
        WorldMap worldMap = level.worldMap();
        boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * worldMap.numCols() - HTS;
        setVisible(pac.isVisible() && !outsideWorld);
    }

    // Maybe this should be implemented using a Timeline?
    private Animation createChewingAnimation() {
        final int openAngle = 0, closedAngle = -54;

        var closeMouth = new RotateTransition(Duration.millis(30), jaw);
        closeMouth.setAxis(Rotate.Y_AXIS);
        closeMouth.setFromAngle(openAngle);
        closeMouth.setToAngle(closedAngle);
        closeMouth.setInterpolator(Interpolator.LINEAR);

        var openMouth = new RotateTransition(Duration.millis(90), jaw);
        openMouth.setAxis(Rotate.Y_AXIS);
        openMouth.setFromAngle(closedAngle);
        openMouth.setToAngle(openAngle);
        openMouth.setInterpolator(Interpolator.LINEAR);

        var chewingAnimation = new SequentialTransition(openMouth, Ufx.pauseSec(0.1), closeMouth);
        chewingAnimation.setCycleCount(Animation.INDEFINITE);
        return chewingAnimation;
    }

    private Animation createChewingTimeline() {
        var closed = new KeyValue[] {
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), -54, Interpolator.LINEAR)
        };
        var open = new KeyValue[] {
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), 0, Interpolator.LINEAR)
        };
        Timeline animation = new Timeline(
            new KeyFrame(Duration.ZERO,        "Open on Start", open),
            new KeyFrame(Duration.millis(100), "Start Closing", open),
            new KeyFrame(Duration.millis(130), "Closed",        closed),
            new KeyFrame(Duration.millis(200), "Start Opening", closed),
            new KeyFrame(Duration.millis(280), "Open",          open)
        );
        animation.setCycleCount(Animation.INDEFINITE);
        return animation;
    }
}