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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchCustomMapsApp extends Application {

    private final File watchedDirectory = GameModel.CUSTOM_MAP_DIR;
    private final ObservableList<String> eventsDescriptions = FXCollections.observableList(new ArrayList<>());

    @Override
    public void start(Stage stage) throws IOException {
        BorderPane root = new BorderPane();
        var eventListView = new ListView<String>();
        eventListView.setItems(eventsDescriptions);
        root.setCenter(eventListView);

        Scene scene = new Scene(root, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Watch " + watchedDirectory);
        stage.show();

        CustomMapWatchdog dog = new CustomMapWatchdog(GameModel.CUSTOM_MAP_DIR);
        dog.setEventConsumer(this::showEventsInList);
        dog.startWatching();
    }

    private void showEventsInList(List<WatchEvent<?>> polledEvents) {
        List<String> descriptions = new ArrayList<>();
        for (WatchEvent<?> we : polledEvents) {
            @SuppressWarnings("unchecked") WatchEvent<Path> event = (WatchEvent<Path>) we;
            Path relativePath = event.context();
            File file = new File(watchedDirectory, relativePath.toString());
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
}
