/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

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

    public static final int DEFAULT_CAMERA_ROTATE = 60;
    public static final double DEFAULT_SCROLL_SPEED = 0.25;

    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);

    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);

    private final SubScene subScene;

    private final Maze3D maze3D;

    // for rotating 3D preview
    private double anchorX;
    private double anchorAngle;

    public Preview3D(EditorUI ui, Model3DRepository model3DRepository, double width, double height) {
        requireNonNull(ui);
        requireNonNull(ui.editor());
        requireNonNull(model3DRepository);

        maze3D = new Maze3D(model3DRepository);
        maze3D.actorsVisibleProperty().bind(ui.actorsVisibleProperty());
        maze3D.worldMapProperty().bind(ui.editor().currentWorldMapProperty());
        maze3D.foodVisibleProperty().bind(foodVisible);
        maze3D.terrainVisibleProperty().bind(terrainVisible);

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
        subScene.setOnScroll(e -> maze3D.setTranslateY(maze3D.getTranslateY() + e.getDeltaY() * DEFAULT_SCROLL_SPEED));
        subScene.setOnKeyPressed(this::onKeyPressed);
        subScene.setOnKeyTyped(this::onKeyTyped);
    }

    public ObjectProperty<WorldMap> worldMapProperty() {
        return worldMap;
    }

    public WorldMap worldMap() {
        return worldMap.get();
    }

    public BooleanProperty terrainVisibleProperty() {
        return terrainVisible;
    }

    public BooleanProperty foodVisibleProperty() {
        return foodVisible;
    }

    public SubScene subScene() {
        return subScene;
    }

    public void updateFood() {
        maze3D.updateFood();
    }

    public void updateMaze() {
        maze3D.updateMaze();
    }

    public void reset() {
        PerspectiveCamera camera = maze3D.camera();
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(DEFAULT_CAMERA_ROTATE);
        if (worldMap() != null) {
            double mapWidth = worldMap().numCols() * TS;
            double mapHeight = worldMap().numRows() * TS;
            camera.setTranslateX(mapWidth * 0.5);
            camera.setTranslateY(mapHeight);
            camera.setTranslateZ(-mapWidth * 0.5);
            maze3D.setRotate(0);
            maze3D.setTranslateX(0);
            maze3D.setTranslateY(-0.5 * mapHeight);
        }
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        boolean control = keyEvent.isControlDown(), shift = keyEvent.isShiftDown();
        switch (keyEvent.getCode()) {
            case KeyCode.LEFT -> {
                if (control && shift) maze3D.actionMoveLeft.execute();
                else if (control)     maze3D.actionRotateLeft.execute();
            }
            case KeyCode.RIGHT -> {
                if (control && shift) maze3D.actionMoveRight.execute();
                else if (control)     maze3D.actionRotateRight.execute();
            }
            case KeyCode.UP -> {
                if (control && shift) maze3D.actionMoveTowardsUser.execute();
            }
            case KeyCode.DOWN -> {
                if (control && shift) maze3D.actionMoveAwayFromUser.execute();
            }
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String key = e.getCharacter();
        if (key.equals("w")) {
            maze3D.actionToggleWireframe.execute();
        }
    }
}