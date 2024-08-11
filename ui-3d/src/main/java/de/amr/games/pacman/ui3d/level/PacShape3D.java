/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui3d.model.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class PacShape3D extends Group {

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final double initialZ;
    private final Node jaw;
    private final PointLight light = new PointLight();
    private final Rotate moveRotation = new Rotate();
    private final Animation chewingAnimation;

    public PacShape3D(Model3D model3D, double size, Color headColor, Color palateColor) {
        initialZ = -0.5 * size;
        jaw = PacModel3D.createPacSkull(model3D, size, headColor, palateColor);
        meshViewById(jaw, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        light.translateXProperty().bind(translateXProperty());
        light.translateYProperty().bind(translateYProperty());
        light.setTranslateZ(-2.5 * size);

        getChildren().add(jaw);
        getTransforms().add(moveRotation);
        setTranslateZ(initialZ);

        chewingAnimation = createChewingTimeline();
    }

    public void init(Pac pac) {
        updatePosition(pac);
        setVisible(pac.isVisible());
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        stopChewingAndOpenMouth();
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawModePy;
    }

    public PointLight light() {
        return light;
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
        Vector2f center = pac.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(initialZ);
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(pac.moveDir()));
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
     public void updateLight(Pac pac, GameModel game) {
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(PY_3D_PAC_LIGHT_ENABLED.get() && pac.isVisible() && hasPower);
    }

    public void updateVisibility(Pac pac, GameWorld world) {
        WorldMap map = world.map();
        boolean outsideWorld = getTranslateX() < HTS
            || getTranslateX() > TS * map.terrain().numCols() - HTS;
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
            new KeyFrame(Duration.ZERO,        "Open", open),
            new KeyFrame(Duration.millis(100), "Still Open", open),
            new KeyFrame(Duration.millis(130), "Closed", closed),
            new KeyFrame(Duration.millis(200), "Still Closed", closed),
            new KeyFrame(Duration.millis(250), "Open Again", open)
        );
        animation.setCycleCount(Animation.INDEFINITE);
        return animation;
    }
}