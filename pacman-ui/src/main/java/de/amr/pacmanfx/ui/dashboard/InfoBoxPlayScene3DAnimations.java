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
import javafx.util.Duration;

import static de.amr.pacmanfx.ui.PacManGames.theUI;

public class InfoBoxPlayScene3DAnimations extends InfoBox {

    public static class AnimationData {
        private final StringProperty idPy;
        private final StringProperty animationStatusPy;

        AnimationData(String id, Animation animation) {
            idPy = new SimpleStringProperty(id);
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

        TableColumn<AnimationData, String> idColumn = new TableColumn<>("Animation ID");
        idColumn.setCellValueFactory(data -> data.getValue().idPy());

        TableColumn<AnimationData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().animationStatusProperty());

        table.getColumns().add(idColumn);
        table.getColumns().add(statusColumn);

        addRow(table);

        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> reloadData()));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
    }

    private void reloadData() {
        animationDataRows.clear();
        if (animationManagerPy.get() != null) {
            AnimationManager animationManager = animationManagerPy.get();
            animationManager.animationMap().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String id = entry.getKey();
                    Animation animation = entry.getValue();
                    animationDataRows.add(new AnimationData(id, animation));
            });
        }
    }

    public Timeline refreshTimer() {
        return refreshTimer;
    }

    @Override
    public void init() {
        super.init();
        // Experimental
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
