/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class Preview3D {

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();
    private final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    private final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    private final SubScene subScene;
    private final Maze3D maze3D;

    // for rotating 3D preview
    private double anchorX;
    private double anchorAngle;

    public Preview3D(EditorUI ui, Model3DRepository model3DRepository, double width, double height) {
        requireNonNull(model3DRepository);

        maze3D = new Maze3D(ui, model3DRepository);
        maze3D.foodVisibleProperty().bind(foodVisiblePy);
        maze3D.terrainVisibleProperty().bind(terrainVisiblePy);

        subScene = new SubScene(maze3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(maze3D.camera());
        subScene.setFill(Color.CORNFLOWERBLUE);

        subScene.setOnMouseClicked(e -> {
            subScene.requestFocus();
            if (e.getClickCount() == 2) reset();
        });
        subScene.setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorAngle = maze3D.getRotate();
        });
        subScene.setOnMouseDragged(e -> maze3D.setRotate(anchorAngle + anchorX - e.getSceneX()));
        subScene.setOnScroll(e -> maze3D.setTranslateY(maze3D.getTranslateY() + e.getDeltaY() * 0.25));
        subScene.setOnKeyPressed(this::onKeyPressed);
        subScene.setOnKeyTyped(this::onKeyTyped);
    }

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMapPy; }
    public BooleanProperty terrainVisibleProperty() { return terrainVisiblePy; }
    public BooleanProperty foodVisibleProperty() { return foodVisiblePy; }

    public SubScene getSubScene() { return subScene; }

    public void updateFood() {
        maze3D.updateFood(worldMapPy.get());
    }

    public void updateTerrain() {
        maze3D.updateMaze(worldMapPy.get());
    }

    public void reset() {
        double mapWidth = worldMapPy.get().numCols() * TS;
        double mapHeight = worldMapPy.get().numRows() * TS;
        PerspectiveCamera camera = maze3D.camera();
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(60);
        camera.setTranslateX(mapWidth * 0.5);
        camera.setTranslateY(mapHeight);
        camera.setTranslateZ(-mapWidth * 0.5);
        maze3D.setRotate(0);
        maze3D.setTranslateX(0);
        maze3D.setTranslateY(-0.5 * mapHeight);
    }

    private void onKeyPressed(KeyEvent e) {
        boolean control = e.isControlDown(), shift = e.isShiftDown();
        KeyCode key = e.getCode();
        if (control && !shift && key == KeyCode.LEFT) {
            maze3D.rotateBy(-2);
        }
        else if (control && !shift && key == KeyCode.RIGHT) {
            maze3D.rotateBy(2);
        }
        else if (control && shift && key == KeyCode.UP) {
            maze3D.moveTowardsUser(10);
        }
        else if (control && shift && key == KeyCode.DOWN) {
            maze3D.moveTowardsUser(-10);
        }
        else  if (control && shift && key == KeyCode.LEFT) {
            maze3D.moveRight(10);
        }
        else if (control && shift && key == KeyCode.RIGHT) {
            maze3D.moveLeft(10);
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String key = e.getCharacter();
        if (key.equals("w")) {
            maze3D.toggleWireframe();
        }
    }
}