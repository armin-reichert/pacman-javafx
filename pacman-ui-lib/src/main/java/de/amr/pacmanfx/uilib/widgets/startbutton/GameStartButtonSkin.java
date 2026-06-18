package de.amr.pacmanfx.uilib.widgets.startbutton;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.tinylog.Logger;

public class GameStartButtonSkin extends SkinBase<GameStartButton> {

    private final StackPane root = new StackPane();
    private final Animation pulse;

    public GameStartButtonSkin(GameStartButton control) {
        super(control);

        Text textNode = new Text();
        textNode.textProperty().bind(control.textProperty());
        textNode.fontProperty().bind(control.fontProperty());
        textNode.setFill(Color.WHITE);
        textNode.setEffect(new DropShadow(3, Color.color(0.2, 0.2, 0.2)));

        root.getChildren().add(textNode);
        root.getStyleClass().add("game-start-button-container");

        root.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        root.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

        Logger.debug(root.getCssMetaData());


        root.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                e.consume();
                control.onActionProperty().get().run();
            }
        });

        root.setOnMouseEntered(_ -> root.setOpacity(0.85));
        root.setOnMouseExited(_ -> root.setOpacity(1.0));
        root.setOnMousePressed(_ -> root.setScaleX(0.97));
        root.setOnMouseReleased(_ -> root.setScaleX(1.0));

        pulse = createPulseAnimation(root);

        control.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                root.setScaleX(1.0);
                root.setScaleY(1.0);
                pulse.playFromStart();
            } else {
                pulse.stop();
                root.setScaleX(1.0);
                root.setScaleY(1.0);
            }
        });

        getChildren().add(root);
    }

    private Animation createPulseAnimation(StackPane node) {
        var up = new ScaleTransition(Duration.seconds(0.4), node);
        up.setToX(1.15);
        up.setToY(1.15);
        up.setInterpolator(Interpolator.EASE_OUT);

        var down = new ScaleTransition(Duration.seconds(0.4), node);
        down.setToX(1.0);
        down.setToY(1.0);
        down.setInterpolator(Interpolator.EASE_IN);

        var seq = new SequentialTransition(up, down);
        seq.setCycleCount(Animation.INDEFINITE);
        return seq;
    }
}
