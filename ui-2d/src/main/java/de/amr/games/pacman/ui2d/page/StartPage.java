package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * @author Armin Reichert
 */
public class StartPage extends StackPane implements Page {

    static final BackgroundSize SIZE_TO_FIT = new BackgroundSize(AUTO, AUTO,
        false, false,
        true, false);

    private final BorderPane layout = new BorderPane();
    private final Node btnPlay;
    private final GameSceneContext context;

    private static Button createCarouselButton(Direction dir) {
        double dimmed = 0.25;
        Button button = new Button();
        button.setStyle("-fx-text-fill: rgb(0,155,252); -fx-background-color: transparent; -fx-padding: 5");
        button.setFont(Font.font("Sans", FontWeight.BOLD, 80));
        button.setText(dir == Direction.LEFT ? "\u2b98" : "\u2b9a");
        button.setOpacity(dimmed);
        button.setOnMouseEntered(e -> button.setOpacity(1.0));
        button.setOnMouseExited(e -> button.setOpacity(dimmed));
        return button;
    }

    public StartPage(GameSceneContext context) {
        this.context = checkNotNull(context);

        var btnNextVariant = createCarouselButton(Direction.RIGHT);
        btnNextVariant.setOnAction(e -> context.actionHandler().selectNextGameVariant());

        var btnPrevVariant = createCarouselButton(Direction.LEFT);
        btnPrevVariant.setOnAction(e -> context.actionHandler().selectPrevGameVariant());

        btnPlay = createPlayButton(context.tt("play_button"));

        VBox left = new VBox(btnPrevVariant);
        left.setAlignment(Pos.CENTER);
        layout.setLeft(left);

        VBox right = new VBox(btnNextVariant);
        right.setAlignment(Pos.CENTER_RIGHT);
        layout.setRight(right);

        HBox bottom = new HBox(btnPlay);
        bottom.setAlignment(Pos.BOTTOM_CENTER);
        bottom.setTranslateY(-10);
        layout.setBottom(bottom);

        updateBackgroundImage(context.game().variant());

        setBackground(Ufx.coloredBackground(Color.BLACK));
        getChildren().add(layout);
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    public Node playButton() {
        return btnPlay;
    }

    @Override
    public void onSelected() {
        if (context.gameClock().isRunning()) {
            context.gameClock().stop();
            Logger.info("Clock stopped.");
        }
    }

    public void updateBackgroundImage(GameVariant variant) {
        String imageKey = variant.resourceKey() + ".startpage.image";
        Image image = checkNotNull(context.theme().image(imageKey));
        var background = new Background(
            new BackgroundImage(image,BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, SIZE_TO_FIT)
        );
        layout.setBackground(background);
    }

    private Node createPlayButton(String buttonText) {
        var text = new Text(buttonText);
        text.setFill(context.theme().color("startpage.button.color"));
        text.setFont(context.theme().font("startpage.button.font"));

        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));
        text.setEffect(shadow);

        var pane = new BorderPane(text);
        pane.setMaxSize(200, 100);
        pane.setPadding(new Insets(10));
        pane.setCursor(Cursor.HAND);
        pane.setBackground(Ufx.coloredRoundedBackground(context.theme().color("startpage.button.bgColor"), 20));

        return pane;
    }
}