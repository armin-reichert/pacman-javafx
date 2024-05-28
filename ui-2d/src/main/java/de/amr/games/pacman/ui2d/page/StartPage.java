package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.variantKey;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * @author Armin Reichert
 */
public class StartPage implements Page {

    private final StackPane root = new StackPane();
    private final BorderPane layout = new BorderPane();
    private final Theme theme;
    private final Node btnPlay;

    private static Background createBackground(Image image) {
        var backgroundImage = new BackgroundImage(image,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(
                AUTO, AUTO, // width, height
                false, false, // as percentage
                true, // contain
                false // cover
            ));
        return new Background(backgroundImage);
    }

    private Button createCarouselButton(Direction dir, Runnable action) {
        double dimmed = 0.25;
        Button button = new Button();
        button.setStyle("-fx-text-fill: rgb(0,155,252); -fx-background-color: transparent; -fx-padding: 5");
        button.setFont(Font.font("Sans", FontWeight.BOLD, 80));
        button.setText(dir == Direction.LEFT ? "\u2b98" : "\u2b9a");
        button.setOpacity(dimmed);
        button.setOnMouseEntered(e -> button.setOpacity(1.0));
        button.setOnMouseExited(e -> button.setOpacity(dimmed));
        button.setOnAction(e -> action.run());
        return button;
    }

    public StartPage(Theme theme, ActionHandler actionHandler) {
        checkNotNull(theme);
        this.theme = theme;

        var btnNextVariant = createCarouselButton(Direction.RIGHT, actionHandler::selectNextGameVariant);
        var btnPrevVariant = createCarouselButton(Direction.LEFT, actionHandler::selectPrevGameVariant);
        btnPlay = createPlayButton();

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

        root.setBackground(Ufx.coloredBackground(Color.BLACK));
        root.getChildren().add(layout);
    }

    @Override
    public void setSize(double width, double height) {
    }

    @Override
    public Pane rootPane() {
        return root;
    }

    public void setGameVariant(GameVariant variant) {
        String vk = variantKey(variant);
        layout.setBackground(createBackground(theme.image(vk + ".startpage.image")));
    }

    public void setPlayButtonAction(Runnable action) {
        btnPlay.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                action.run();
            }
        });
    }

    public void setOnKeyPressed(EventHandler<KeyEvent> handler) {
        root.setOnKeyPressed(handler);
    }

    // TODO This should be a real button but it seems WebFX/GWT has issues with graphic buttons
    private Node createPlayButton() {
        var label = new Text("Play!");
        label.setFill(theme.color("startpage.button.color"));
        label.setFont(theme.font("startpage.button.font"));

        var ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.2f, 0.2f, 0.2f));
        label.setEffect(ds);

        var button = new BorderPane(label);
        button.setMaxSize(200, 100);
        button.setPadding(new Insets(10));
        button.setCursor(Cursor.HAND);
        button.setBackground(Ufx.coloredRoundedBackground(theme.color("startpage.button.bgColor"), 20));

        return button;
    }
}