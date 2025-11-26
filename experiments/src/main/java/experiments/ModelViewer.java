package experiments;

import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ModelViewer extends Application {

    static final String REPOSITORY_DIR = System.getProperty("user.home") + "/git/pacman-javafx/";
    static final String DIR = REPOSITORY_DIR
        + "pacman-ui-lib/src/main/resources/de/amr/pacmanfx/uilib/model3D";

    private final PacManModel3DRepository model3DRepository = PacManModel3DRepository.theRepository();
    private Stage stage;
    private SubScene previewArea;
    private final BorderPane layoutPane = new BorderPane();
    private FileChooser fileChooser;
    private double lastX = -1;
    private double lastY = -1;
    private Node currentNode;
    private final Rotate rotX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotY = new Rotate(0, Rotate.Y_AXIS);

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        fileChooser = new FileChooser();
        var objFilter = new FileChooser.ExtensionFilter("Wavefront OBJ Files", "*.obj");
        var allFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().addAll(objFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(objFilter);
        fileChooser.setInitialDirectory(new File(DIR));

        Scene scene = new Scene(createSceneContent(), 800, 600);

        PerspectiveCamera cam = new PerspectiveCamera(true);
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
            try {
                Node shape = model3DRepository.createPacBody(16, Color.YELLOW, Color.BLACK, Color.PINK);
                //allMeshViewsUnder(shape).forEach(meshView -> meshView.setDrawMode(DrawMode.LINE));
                setCurrentNode(new Group(shape));
            } catch (Exception x) {
                Logger.error(x);
                Logger.error("Could not open 3D model from file {}", selectedFile);
            }
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
