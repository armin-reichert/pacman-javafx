package de.amr.games.pacman.ui3d.model;

import de.amr.games.pacman.ui3d.entity.PacModel3D;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ModelViewer extends Application {

    static final String REPOSITORY_DIR = System.getProperty("user.home") + "/git/pacman-javafx/";
    static final String DIR = REPOSITORY_DIR + "ui-3d/src/main/resources/de/amr/games/pacman/ui3d/model3D";

    private Stage stage;
    private SubScene previewArea;
    private PerspectiveCamera cam;
    private final BorderPane layoutPane = new BorderPane();
    private FileChooser fileChooser;
    private double lastX = -1;
    private double lastY = -1;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        fileChooser = new FileChooser();
        var objFilter = new FileChooser.ExtensionFilter("Wavefront OBJ Files", "*.obj");
        var allFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().addAll(objFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(objFilter);
        fileChooser.setInitialDirectory(new File(DIR));
        Scene scene = new Scene(createSceneContent(), 800, 600);
        cam = new PerspectiveCamera(true);
        cam.setTranslateZ(-100);
        //cam.setTranslateY(-100);
        //cam.setRotationAxis(Rotate.X_AXIS);
        //cam.setRotate(30);
        previewArea.setCamera(cam);
        stage.setScene(scene);
        stage.setTitle("3D Model Viewer");
        stage.show();
    }

    private Parent createSceneContent() {
        MenuItem miOpenFile = new MenuItem("Open...");
        miOpenFile.setOnAction(e -> openFile());

        MenuItem miExit = new MenuItem("Exit");
        miExit.setOnAction(e -> stage.close());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(miOpenFile, miExit);

        MenuBar menuBar = new MenuBar(fileMenu);
        layoutPane.setTop(menuBar);

        previewArea = new SubScene(new Group(), 800, 600, true, SceneAntialiasing.BALANCED);
        previewArea.setFill(Color.BLACK);
        previewArea.setOnMouseClicked(e -> Logger.info(e));
        previewArea.setOnMouseDragged(e -> {
            if (e.getTarget() instanceof MeshView) {
                if (lastX != -1) {
                    double dx = e.getX() - lastX;
                    double dy = e.getY() - lastY;
                    Point3D axis = new Point3D(0, 0, cam.getTranslateZ());
                    cam.setRotationAxis(axis);
                    cam.setRotate(cam.getRotate() + dx);
                    Logger.info("dx {0.00} dy {0.00}", dx, dy);
                    Logger.info("Cam rotate: {}", cam.getRotate());
                }
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        layoutPane.setCenter(previewArea);
        return layoutPane;
    }

    private void openFile() {
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            Logger.info("File {} selected", selectedFile);
            Model3D model3D = new Model3D(selectedFile);
            Group pacMan3D = PacModel3D.createPacShape(model3D, 16, Color.YELLOW, Color.BLACK, Color.PINK);
            Group root = (Group) previewArea.getRoot();
            root.getChildren().setAll(pacMan3D);
        }
    }
}
