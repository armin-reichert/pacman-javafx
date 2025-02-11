/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.model3D.Model3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.TS;

public class Preview3DSubScene extends SubScene {

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();
    private final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    private final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    private final MazePreview3D preview3D;

    // for rotating 3D preview
    private double anchorX;
    private double anchorAngle;

    public Preview3DSubScene(double width, double height) {
        super(new Group(), width, height, true, SceneAntialiasing.BALANCED);
        preview3D = new MazePreview3D();
        preview3D.foodVisibleProperty().bind(foodVisiblePy);
        preview3D.terrainVisibleProperty().bind(terrainVisiblePy);

        Group root = (Group) getRoot();
        root.getChildren().add(preview3D);

        setCamera(preview3D.camera());
        setFill(Color.CORNFLOWERBLUE);

        setOnMouseClicked(e -> {
            requestFocus();
            if (e.getClickCount() == 2) reset();
        });
        setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorAngle = preview3D.getRotate();
        });
        setOnMouseDragged(e -> preview3D.setRotate(anchorAngle + anchorX - e.getSceneX()));
        setOnScroll(e -> preview3D.setTranslateY(preview3D.getTranslateY() + e.getDeltaY() * 0.25));
        setOnKeyPressed(this::onKeyPressed);
        setOnKeyTyped(this::onKeyTyped);
    }

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMapPy; }
    public BooleanProperty terrainVisibleProperty() { return terrainVisiblePy; }
    public BooleanProperty foodVisibleProperty() { return foodVisiblePy; }

    public void updateFood() {
        preview3D.updateFood(worldMapPy.get());
    }

    public void updateTerrain() {
        preview3D.updateMaze(worldMapPy.get());
    }

    public void reset() {
        double mapWidth = worldMapPy.get().terrain().numCols() * TS;
        double mapHeight = worldMapPy.get().terrain().numRows() * TS;
        PerspectiveCamera camera = preview3D.camera();
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(60);
        camera.setTranslateX(mapWidth * 0.5);
        camera.setTranslateY(mapHeight);
        camera.setTranslateZ(-mapWidth * 0.5);
        preview3D.setRotate(0);
        preview3D.setTranslateY(-0.5 * mapHeight);
    }

    private void onKeyPressed(KeyEvent e) {
        boolean control = e.isControlDown();
        KeyCode key = e.getCode();
        if (control && key == KeyCode.UP) {
            preview3D.setTranslateY(preview3D.getTranslateY() + 10);
        } else if (control && key == KeyCode.DOWN) {
            preview3D.setTranslateY(preview3D.getTranslateY() - 10);
        } else if (control && key == KeyCode.LEFT) {
            preview3D.setRotate(preview3D.getRotate() - 2);
        } else if (control && key == KeyCode.RIGHT) {
            preview3D.setRotate(preview3D.getRotate() + 2);
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String key = e.getCharacter();
        if (key.equals("w")) {
            preview3D.wireframeProperty().set(!preview3D.wireframeProperty().get());
        }
    }
}
