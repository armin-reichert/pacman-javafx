/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.model.world.WorldMap;
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

    private final EditorMaze3D editorMaze3D;

    // for rotating 3D preview
    private double anchorX;
    private double anchorAngle;

    public Preview3D(TileMapEditorUI ui, double width, double height) {
        requireNonNull(ui);
        requireNonNull(ui.editor());

        editorMaze3D = new EditorMaze3D();
        editorMaze3D.actorsVisibleProperty().bind(ui.actorsVisibleProperty());
        editorMaze3D.worldMapProperty().bind(ui.editor().currentWorldMapProperty());
        editorMaze3D.foodVisibleProperty().bind(foodVisible);
        editorMaze3D.terrainVisibleProperty().bind(terrainVisible);

        subScene = new SubScene(editorMaze3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(editorMaze3D.camera());
        subScene.setFill(Color.CORNFLOWERBLUE);

        subScene.setOnMouseClicked(e -> {
            subScene.requestFocus();
            if (e.getClickCount() == 2) reset();
        });
        subScene.setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorAngle = editorMaze3D.getRotate();
        });
        subScene.setOnMouseDragged(e -> editorMaze3D.setRotate(anchorAngle + anchorX - e.getSceneX()));
        subScene.setOnScroll(e -> editorMaze3D.setTranslateY(editorMaze3D.getTranslateY() + e.getDeltaY() * DEFAULT_SCROLL_SPEED));
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
        editorMaze3D.updateFood();
    }

    public void updateMaze() {
        editorMaze3D.rebuildMaze();
    }

    public void reset() {
        PerspectiveCamera camera = editorMaze3D.camera();
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(DEFAULT_CAMERA_ROTATE);
        if (worldMap() != null) {
            double mapWidth = worldMap().numCols() * TS;
            double mapHeight = worldMap().numRows() * TS;
            camera.setTranslateX(mapWidth * 0.5);
            camera.setTranslateY(mapHeight);
            camera.setTranslateZ(-mapWidth * 0.5);
            editorMaze3D.setRotate(0);
            editorMaze3D.setTranslateX(0);
            editorMaze3D.setTranslateY(-0.5 * mapHeight);
        }
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        boolean control = keyEvent.isControlDown(), shift = keyEvent.isShiftDown();
        switch (keyEvent.getCode()) {
            case KeyCode.LEFT -> {
                if (control && shift) editorMaze3D.actionMoveLeft.run();
                else if (control)     editorMaze3D.actionRotateLeft.run();
            }
            case KeyCode.RIGHT -> {
                if (control && shift) editorMaze3D.actionMoveRight.run();
                else if (control)     editorMaze3D.actionRotateRight.run();
            }
            case KeyCode.UP -> {
                if (control && shift) editorMaze3D.actionMoveTowardsUser.run();
            }
            case KeyCode.DOWN -> {
                if (control && shift) editorMaze3D.actionMoveAwayFromUser.run();
            }
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String key = e.getCharacter();
        if (key.equals("w")) {
            editorMaze3D.actionToggleWireframe.run();
        }
    }
}