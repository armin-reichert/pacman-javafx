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

    static void main() {
        launch(WatchCustomMapsApp.class);
    }

    private final ObservableList<String> eventDescriptionList = FXCollections.observableList(new ArrayList<>());
    private File watchedDirectory;

    @Override
    public void init() {
        watchedDirectory = GameBox.CUSTOM_MAP_DIR;
    }

    @Override
    public void start(Stage stage) {
        final var root = new BorderPane();

        final var listView = new ListView<String>();
        listView.setItems(eventDescriptionList);
        root.setCenter(listView);

        final var scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Watch " + watchedDirectory);
        stage.show();

        final var dog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);
        dog.addEventListener(this::showEventsInList);
        dog.startWatching();
    }

    private void showEventsInList(List<WatchEvent<Path>> pathEvents) {
        Platform.runLater(() -> {
            final var now = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            for (WatchEvent<Path> pathEvent : pathEvents) {
                final Path relativePath = pathEvent.context();
                final File file = new File(watchedDirectory, relativePath.toString());
                final String fileType = file.isDirectory() ? "Directory" : "File";
                if (pathEvent.kind().equals(ENTRY_CREATE)) {
                    eventDescriptionList.add("%s: %s %s created".formatted(now, fileType, file));
                } else if (pathEvent.kind().equals(ENTRY_MODIFY)) {
                    eventDescriptionList.add("%s: %s %s modified".formatted(now, fileType, file));
                } else if (pathEvent.kind().equals(ENTRY_DELETE)) {
                    eventDescriptionList.add("%s: %s %s deleted".formatted(now, fileType, file));
                }
            }
        });
    }
}
