package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.WorldRenderer3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import static de.amr.games.pacman.lib.Globals.TS;

public class Preview3D {

    private final Group root = new Group();
    private final Stage stage;
    private final Scene scene;
    private final WorldRenderer3D r3D;
    private final PerspectiveCamera camera = new PerspectiveCamera();

    public Preview3D() {
        scene = new Scene(root, 800, 400, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.CORNFLOWERBLUE);
        scene.setCamera(camera);

        stage = new Stage();
        stage.setTitle("3D Preview");
        stage.setScene(scene);

        r3D = new WorldRenderer3D();

        root.getChildren().add(createSampleContent());
    }

    private Node createSampleContent() {
        var sphere = new Sphere(200);
        sphere.setMaterial(WorldRenderer3D.coloredMaterial(Color.GREEN));
        return sphere;
    }

    public void updateContent(WorldMap worldMap, Color wallBaseColor, Color wallTopColor) {
        root.getChildren().clear();

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        root.getChildren().add(ambientLight);

        double worldWidth = worldMap.terrain().numCols() * TS;
        double worldHeight = worldMap.terrain().numRows() * TS;

        Group og = new Group();
        root.getChildren().add(og);

        // Floor left-upper corner at origin
        Box floor = new Box(worldWidth, worldHeight, 0.1);
        floor.setTranslateX(0.5 * worldWidth);
        floor.setTranslateY(0.5 * worldHeight);
        floor.setMaterial(WorldRenderer3D.coloredMaterial(Color.BLACK));
        og.getChildren().add(floor);

        Group maze = new Group();
        root.getChildren().add(maze);

        r3D.setWallBaseMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setCornerMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setWallTopMaterial(WorldRenderer3D.coloredMaterial(wallTopColor));
        for (Obstacle obstacle : worldMap.obstacles()) {
            r3D.renderObstacle3D(maze, obstacle);
        }

        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(60);
        camera.translateXProperty().bind(stage.widthProperty().subtract(worldWidth).multiply(-0.5));
        camera.translateYProperty().bind(stage.heightProperty().multiply(-0.5));
        camera.setTranslateZ(50);
    }

    public boolean isVisible() {
        return stage.isShowing();
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }
}
