package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.util.Keyboard;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import static de.amr.games.pacman.ui2d.PacManGames2dUI.*;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * @author Armin Reichert
 */
public class StartPage implements Page {

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            GameVariant variant = get();
            if (variant == null) {
                return;
            }
            String imageKey = variant.resourceKey() + ".startpage.image";
            Image image = checkNotNull(context.theme().image(imageKey));
            var background = new Background(
                new BackgroundImage(
                    image,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    variant == GameVariant.PACMAN_XXL ? FILL : FIT_HEIGHT)
            );
            layout.setBackground(background);
        }
    };

    private static final BackgroundSize FIT_HEIGHT = new BackgroundSize(AUTO, 1,
        false, true,
        true, false);

    private static final BackgroundSize FILL = new BackgroundSize(AUTO, AUTO,
        false, false,
        true, true);

    private static Button createCarouselButton(char arrow) {
        Button button = new Button();
        // Without this, button gets input focus after being clicked with the mouse and the LEFT, RIGHT keys stop working!
        button.setFocusTraversable(false);
        button.setStyle("-fx-text-fill: rgb(0,155,252); -fx-background-color: transparent; -fx-padding: 5");
        button.setFont(Font.font("Sans", FontWeight.BOLD, 80));
        button.setText(String.valueOf(arrow));
        button.setOpacity(0.2);
        button.setOnMouseEntered(e -> button.setOpacity(1.0));
        button.setOnMouseExited(e -> button.setOpacity(0.2));
        return button;
    }

    private static Node createPlayButton(Theme theme, String buttonText) {
        var text = new Text(buttonText);
        text.setFill(theme.color("startpage.button.color"));
        text.setFont(theme.font("startpage.button.font"));

        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));
        text.setEffect(shadow);

        var pane = new BorderPane(text);
        pane.setMaxSize(200, 100);
        pane.setPadding(new Insets(10));
        pane.setCursor(Cursor.HAND);
        pane.setBackground(Ufx.coloredRoundedBackground(theme.color("startpage.button.bgColor"), 20));

        return pane;
    }

    private final StackPane root = new StackPane();
    private final BorderPane layout = new BorderPane();
    private final Node btnPlay;
    private final GameSceneContext context;

    public StartPage(GameSceneContext context) {
        this.context = checkNotNull(context);

        var btnPrevVariant = createCarouselButton('\u2b98');
        btnPrevVariant.setOnAction(e -> context.actionHandler().selectPrevGameVariant());
        VBox left = new VBox(btnPrevVariant);
        left.setAlignment(Pos.CENTER_LEFT);

        var btnNextVariant = createCarouselButton('\u2b9a');
        btnNextVariant.setOnAction(e -> context.actionHandler().selectNextGameVariant());
        VBox right = new VBox(btnNextVariant);
        right.setAlignment(Pos.CENTER_RIGHT);

        btnPlay = createPlayButton(context.theme(), context.tt("play_button"));
        BorderPane.setAlignment(btnPlay, Pos.BOTTOM_CENTER);
        btnPlay.setTranslateY(-10);
        var btnPlayContainer = new BorderPane();
        btnPlayContainer.setBottom(btnPlay);

        layout.setLeft(left);
        layout.setRight(right);
        layout.setCenter(btnPlayContainer);

        root.setBackground(context.theme().get("wallpaper.background"));
        root.getChildren().add(layout);
    }

    @Override
    public void setSize(double width, double height) {
    }

    @Override
    public Pane rootPane() {
        return root;
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

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(KEYS_SHOW_GAME_PAGE)) {
            context.actionHandler().selectPage(GAME_PAGE);
        } else if (Keyboard.pressed(KEYS_NEXT_VARIANT)) {
            context.actionHandler().selectNextGameVariant();
        } else if (Keyboard.pressed(KEY_PREV_VARIANT)) {
            context.actionHandler().selectPrevGameVariant();
        } else if (Keyboard.pressed(KEY_FULLSCREEN)) {
            context.actionHandler().setFullScreen(true);
        } else if (Keyboard.pressed(KEY_PAUSE)) {
            context.actionHandler().togglePaused();
        }
    }
}