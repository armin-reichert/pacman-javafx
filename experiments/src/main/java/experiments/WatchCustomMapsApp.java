package experiments;

import de.amr.games.pacman.model.GameModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchCustomMapsApp extends Application {

    private WatchKey customMapDirWatcher;

    private final ObservableList<String> eventsDescriptions = FXCollections.observableList(new ArrayList<>());

    @Override
    public void init() throws Exception {
        Logger.info("Watching {}", GameModel.CUSTOM_MAP_DIR);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        customMapDirWatcher = GameModel.CUSTOM_MAP_DIR.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    private void pollingLoop() {
        for (;;) {
            var result = customMapDirWatcher.pollEvents();
            if (!result.isEmpty()) {
                List<String> descriptions = new ArrayList<>();
                for (WatchEvent<?> we : result) {
                    @SuppressWarnings("unchecked") WatchEvent<Path> event = (WatchEvent<Path>) we;
                    Path relativePath = event.context();
                    File file = new File(GameModel.CUSTOM_MAP_DIR, relativePath.toString());
                    String fileType = file.isDirectory() ? "Directory" : "File";
                    if (event.kind().equals(ENTRY_CREATE)) {
                        descriptions.add("%s %s created".formatted(fileType, file));
                    } else if (event.kind().equals(ENTRY_MODIFY)) {
                        descriptions.add("%s %s modified".formatted(fileType, file));
                    } else if (event.kind().equals(ENTRY_DELETE)) {
                        descriptions.add("%s %s deleted".formatted(fileType, file));
                    }
                }
                Platform.runLater(() -> eventsDescriptions.addAll(descriptions));
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Logger.error("Interrupted");
            }
        }
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        var eventListView = new ListView<String>();
        eventListView.setItems(eventsDescriptions);
        root.setCenter(eventListView);

        Scene scene = new Scene(root, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Watch " + GameModel.CUSTOM_MAP_DIR);
        stage.show();

        Thread pollingThread = new Thread(this::pollingLoop);
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
}
