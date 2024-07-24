package de.amr.games.pacman.ui3d.model;

import de.amr.games.pacman.ui3d.level.PacModel3D;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
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
    private Node currentNode;
    private Rotate rotX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotY = new Rotate(0, Rotate.Y_AXIS);

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
        previewArea.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                rotX.setAngle(0);
                rotY.setAngle(0);
            }
        });
        previewArea.setOnMouseDragged(this::handleMouseDragged);
        previewArea.widthProperty().bind(layoutPane.widthProperty());
        previewArea.heightProperty().bind(layoutPane.heightProperty());

        layoutPane.setCenter(previewArea);
        return layoutPane;
    }

    private void openFile() {
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            Logger.info("File {} selected", selectedFile);
            Model3D model3D = new Model3D(selectedFile);
            Node shape = PacModel3D.createPacShape(model3D, 16, Color.YELLOW, Color.BLACK, Color.PINK);
            Model3D.meshViewById(shape, PacModel3D.MESH_ID_HEAD).setDrawMode(DrawMode.LINE);
            Model3D.meshViewById(shape, PacModel3D.MESH_ID_PALATE).setDrawMode(DrawMode.LINE);
            Model3D.meshViewById(shape, PacModel3D.MESH_ID_EYES).setDrawMode(DrawMode.LINE);
            setCurrentNode(new Group(shape));
        }
    }

    private void setCurrentNode(Node node) {
        currentNode = node;
        Group root = (Group) previewArea.getRoot();
        root.getChildren().setAll(currentNode, new AmbientLight());
        currentNode.getTransforms().setAll(rotX, rotY);
    }

    private void handleMouseDragged(MouseEvent e) {
        if (currentNode != null && e.getTarget() instanceof MeshView) {
            if (lastX != -1) {
                double hDist = 0.5 * Math.round(e.getX() - lastX);
                double vDist = 0.5 * Math.round(e.getY() - lastY);
                double rx = (rotX.getAngle() + vDist);
                double ry = (rotY.getAngle() + hDist);
                rotX.setAngle(rx % 360);
                rotY.setAngle(ry % 360);
                Logger.info("rx={} ry={}", rx, ry);
            }
            lastX = e.getX();
            lastY = e.getY();
        }
    }
}
