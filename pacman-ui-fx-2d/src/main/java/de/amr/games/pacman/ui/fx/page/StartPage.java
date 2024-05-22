package de.amr.games.pacman.ui.fx.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.ActionHandler;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
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
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.variantKey;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * @author Armin Reichert
 */
public class StartPage implements Page {

    private final StackPane root = new StackPane();
    private final BorderPane layout = new BorderPane();
    private final Theme theme;
    private final Node btnPlay;
    private final Button btnNextVariant;
    private final Button btnPrevVariant;

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

    public StartPage(Theme theme, ActionHandler actionHandler) {
        checkNotNull(theme);
        this.theme = theme;

        //TODO use icons
        btnNextVariant = new Button(">");
        btnNextVariant.setFont(Font.font("Sans", FontWeight.BLACK, 20));
        btnNextVariant.setOpacity(0.3);
        btnNextVariant.setOnAction(e -> actionHandler.selectNextGameVariant());
        btnNextVariant.setOnMouseEntered(e -> btnNextVariant.setOpacity(1.0));
        btnNextVariant.setOnMouseExited(e -> btnNextVariant.setOpacity(0.3));

        btnPrevVariant = new Button("<");
        btnPrevVariant.setFont(Font.font("Sans", FontWeight.BLACK, 20));
        btnPrevVariant.setOpacity(0.3);
        btnPrevVariant.setOnAction(e -> actionHandler.selectPrevGameVariant());
        btnPrevVariant.setOnMouseEntered(e -> btnPrevVariant.setOpacity(1.0));
        btnPrevVariant.setOnMouseExited(e -> btnPrevVariant.setOpacity(0.3));

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

        root.setBackground(ResourceManager.coloredBackground(Color.BLACK));
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
        button.setBackground(ResourceManager.coloredRoundedBackground(theme.color("startpage.button.bgColor"), 20));

        return button;
    }
}