package experiments;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchCustomMapsApp extends Application {

    private final ObservableList<String> eventsDescriptions = FXCollections.observableList(new ArrayList<>());
    private File watchedDirectory;

    @Override
    public void init() {
        watchedDirectory = GameBox.CUSTOM_MAP_DIR;
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        var eventListView = new ListView<String>();
        eventListView.setItems(eventsDescriptions);
        root.setCenter(eventListView);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Watch " + watchedDirectory);
        stage.show();

        DirectoryWatchdog dog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);
        dog.addEventListener(this::showEventsInList);
        dog.startWatching();
    }

    private void showEventsInList(List<WatchEvent<?>> polledEvents) {
        Platform.runLater(() -> {
            var now = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            for (WatchEvent<?> we : polledEvents) {
                @SuppressWarnings("unchecked") WatchEvent<Path> event = (WatchEvent<Path>) we;
                Path relativePath = event.context();
                File file = new File(watchedDirectory, relativePath.toString());
                String fileType = file.isDirectory() ? "Directory" : "File";
                if (event.kind().equals(ENTRY_CREATE)) {
                    eventsDescriptions.add("%s: %s %s created".formatted(now, fileType, file));
                } else if (event.kind().equals(ENTRY_MODIFY)) {
                    eventsDescriptions.add("%s: %s %s modified".formatted(now, fileType, file));
                } else if (event.kind().equals(ENTRY_DELETE)) {
                    eventsDescriptions.add("%s: %s %s deleted".formatted(now, fileType, file));
                }
            }
        });
    }
}
