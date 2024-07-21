package de.amr.games.pacman.ui3d.model;

import de.amr.games.pacman.ui3d.entity.PacModel3D;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ModelViewer extends Application {

    static final String DIR = "C:\\Users\\armin\\git\\pacman-javafx\\ui-3d\\src\\main\\resources\\de\\amr\\games\\pacman\\ui3d\\model3D";

    private Stage stage;
    private Scene scene;
    private SubScene previewArea;
    private PerspectiveCamera cam;
    private BorderPane layoutPane = new BorderPane();
    private FileChooser fileChooser;
    private Model3D model3D;
    private Group pacMan3D;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        fileChooser = new FileChooser();
        var objFilter = new FileChooser.ExtensionFilter("Wavefront OBJ Files", "*.obj");
        var allFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().addAll(objFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(objFilter);
        fileChooser.setInitialDirectory(new File(DIR));
        scene = new Scene(createSceneContent(), 800, 600);
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
        layoutPane.setCenter(previewArea);
        return layoutPane;
    }

    private void openFile() {
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            Logger.info("File {} selected", selectedFile);
            model3D = new Model3D(selectedFile);
            pacMan3D = PacModel3D.createPacShape(model3D, 16, Color.YELLOW, Color.BLACK, Color.PINK);
            Group root = (Group) previewArea.getRoot();
            root.getChildren().setAll(pacMan3D);
        }
    }
}
