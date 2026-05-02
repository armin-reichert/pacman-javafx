package experiments.scooter;

import de.amr.meshbuilder.MeshBuilder;
import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Derived from
 * <a href="https://github.com/AlmasB/javafx3d-samples/blob/master/src/main/java/com/almasb/fx3d/modelloading/ModelLoadApp.java">Almas B's sample app</a>
 */
public class ScooterDemoApp extends Application {

    public static final double AUTO_ROTATE_SPEED = 0.1;

    public static final String DRAW_MODE_TOGGLE = "d";
    public static final String SCOOTER_ROTATION_TOGGLE = "r";
    public static final String WHEELS_ROTATION_TOGGLE = "w";

    private Group scooter3D;
    private PerspectiveCamera camera;

    private ParallelTransition wheelsAnimation;

    private Animation autoRotateAnimation;
    private final Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    @Override
    public void start(Stage stage) throws Exception {
        createScooter3D();
        stage.setScene(createScene());
        stage.setTitle("Scooter (Courtesy of Almas B)");
        createAutoRotateAnimation();
        createScooterWheelsAnimation();

        autoRotateAnimation.play();
        wheelsAnimation.play();

        stage.show();
    }

    private void createScooter3D() throws IOException {
        scooter3D = new Group();
        final URL url = getClass().getResource("/scooter/Scooter-smgrps.obj");
        if (url != null) {
            final ObjModel objModel = new ObjFileParser(url, StandardCharsets.UTF_8).parse();
            for (MeshView part : MeshBuilder.build(objModel, MeshBuilder.BuildMode.BY_GROUP).values()) {
                scooter3D.getChildren().add(part);
                part.drawModeProperty().bind(drawMode);
            }
            scooter3D.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
            scooter3D.getTransforms().add(new Rotate(180, Rotate.X_AXIS));
            scooter3D.getTransforms().add(autoRotateY);
            // move a bit down
            scooter3D.getTransforms().add(new Translate(0, -0.4, 0));
        }
    }

    private Scene createScene() {
        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-2.5);

        final Group root = new Group(scooter3D);

        final Scene scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.LIGHTBLUE);
        scene.setCamera(camera);

        scene.setOnKeyTyped(e -> {
            if (DRAW_MODE_TOGGLE.equals(e.getCharacter())) {
                drawMode.set(drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
            }
            else if (SCOOTER_ROTATION_TOGGLE.equals(e.getCharacter())) {
                boolean running = autoRotateAnimation.getStatus() == Animation.Status.RUNNING;
                if (running) {
                    autoRotateAnimation.pause();
                }  else {
                    autoRotateAnimation.play();
                }
            }
            else if (WHEELS_ROTATION_TOGGLE.equals(e.getCharacter())) {
                boolean running = wheelsAnimation.getStatus() == Animation.Status.RUNNING;
                if (running) {
                    wheelsAnimation.pause();
                }  else {
                    wheelsAnimation.play();
                }
            }
        });

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> zoomBy(0.1);
                case MINUS -> zoomBy(-0.1);
            }
        });

        scene.setOnScroll(e -> zoomBy(e.getDeltaY() * 0.01));

        return scene;
    }

    private void zoomBy(double amount) {
        camera.setTranslateZ(camera.getTranslateZ() + amount);
    }

    private void createScooterWheelsAnimation() {
        wheelsAnimation = new ParallelTransition();
        scooter3D.getChildren()
            .stream()
            .filter(part -> part.getId().endsWith(".RimFront") || part.getId().endsWith(".RimRear"))
            .forEach(part -> {
                RotateTransition rt = new RotateTransition(Duration.seconds(0.33), part);
                rt.setCycleCount(Animation.INDEFINITE);
                rt.setAxis(Rotate.X_AXIS);
                rt.setByAngle(360);
                rt.setInterpolator(Interpolator.LINEAR);
                wheelsAnimation.getChildren().add(rt);
            });
    }

    private void createAutoRotateAnimation() {
        autoRotateAnimation = new Timeline(
            new KeyFrame(Duration.millis(16), _ -> autoRotateY.setAngle(autoRotateY.getAngle() - AUTO_ROTATE_SPEED)) // ~60 FPS
        );
        autoRotateAnimation.setCycleCount(Animation.INDEFINITE);

    }
}