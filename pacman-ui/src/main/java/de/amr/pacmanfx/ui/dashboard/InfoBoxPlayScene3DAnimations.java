package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static de.amr.pacmanfx.ui.PacManGames.theUI;

public class InfoBoxPlayScene3DAnimations extends InfoBox {

    private static final float REFRESH_TIME_SEC = 0.5f;

    public static class AnimationData {
        private final StringProperty idPy;
        private final StringProperty animationStatusPy;

        AnimationData(String id, Animation animation) {
            String idPrefix = id.substring(0, id.indexOf("#") + 7);
            idPy = new SimpleStringProperty(idPrefix);
            animationStatusPy = new SimpleStringProperty(animation.getStatus().name());
        }

        public StringProperty idPy() {
            return idPy;
        }

        public StringProperty animationStatusProperty() {
            return animationStatusPy;
        }
    }

    private final ObjectProperty<AnimationManager> animationManagerPy = new SimpleObjectProperty<>();
    private final ObservableList<AnimationData> animationDataRows = FXCollections.observableArrayList();
    private final Timeline refreshTimer;

    public InfoBoxPlayScene3DAnimations() {
        TableView<AnimationData> table = new TableView<>();
        table.setItems(animationDataRows);
        table.setPrefWidth(300);
        table.setPrefHeight(400);
        table.setPlaceholder(new Text("No animations"));

        TableColumn<AnimationData, String> idColumn = new TableColumn<>("Animation ID");
        idColumn.setCellValueFactory(data -> data.getValue().idPy());
        idColumn.setSortable(false);

        TableColumn<AnimationData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().animationStatusProperty());
        statusColumn.setSortable(false);

        table.getColumns().add(idColumn);
        table.getColumns().add(statusColumn);

        addRow(table);

        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(REFRESH_TIME_SEC), e -> reloadData()));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
    }

    private void reloadData() {
        animationDataRows.clear();
        if (animationManagerPy.get() != null) {
            AnimationManager animationManager = animationManagerPy.get();
            animationDataRows.addAll(createTableRows(animationManager));
        }
    }

    private List<AnimationData> createTableRows(AnimationManager animationManager) {
        Map<String, Animation> animationMap = animationManager.animationMap();
        List<AnimationData> tableRows = new ArrayList<>();
        tableRows.addAll(createTableRows(animationMap, animation -> animation.getStatus() == Animation.Status.RUNNING));
        tableRows.addAll(createTableRows(animationMap, animation -> animation.getStatus() != Animation.Status.RUNNING));
        return tableRows;
    }

    private List<AnimationData> createTableRows(Map<String, Animation> animationMap, Predicate<Animation> filter) {
        return animationMap.entrySet().stream()
            .filter(entry -> filter.test(entry.getValue()))
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String id = entry.getKey();
                Animation animation = entry.getValue();
                return new AnimationData(id, animation);
            }).toList();
    }

    public Timeline refreshTimer() {
        return refreshTimer;
    }

    @Override
    public void init() {
        super.init();
        animationManagerProperty().bind(Bindings.createObjectBinding(
                () -> theUI().currentGameScene().isPresent() && theUI().currentGameScene().get() instanceof PlayScene3D playScene3D
                        ? playScene3D.animationManager() : null,
                theUI().currentGameSceneProperty()
        ));
        refreshTimer().play();
    }

    public ObjectProperty<AnimationManager> animationManagerProperty() {
        return animationManagerPy;
    }
}
