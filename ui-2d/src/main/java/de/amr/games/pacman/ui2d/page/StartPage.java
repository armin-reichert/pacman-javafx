package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKeys;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredRoundedBackground;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * Got the flyer images from https://flyers.arcade-museum.com/.
 *
 * @author Armin Reichert
 */
public class StartPage implements Page {

    private static final BackgroundSize FIT_HEIGHT = new BackgroundSize(AUTO, 1,
        false, true,
        true, false);

    private static final BackgroundSize FILL = new BackgroundSize(AUTO, AUTO,
        false, false,
        true, true);

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            changeBackgroundImage();
        }
    };

    private final GameContext context;
    private final StackPane root = new StackPane();
    private final BorderPane layout = new BorderPane();
    private final Node btnPlay;

    private int msPacManImageNumber;
    private int pacManImageNumber;

    public StartPage(GameContext context) {
        this.context = checkNotNull(context);

        var btnPrevVariant = createCarouselButton('\u2b98');
        btnPrevVariant.setOnAction(e -> context.actionHandler().selectPrevGameVariant());
        VBox left = new VBox(btnPrevVariant);
        left.setAlignment(Pos.CENTER_LEFT);

        var btnNextVariant = createCarouselButton('\u2b9a');
        btnNextVariant.setOnAction(e -> context.actionHandler().selectNextGameVariant());
        VBox right = new VBox(btnNextVariant);
        right.setAlignment(Pos.CENTER_RIGHT);

        btnPlay = createPlayButton(context.tt("play_button"));
        BorderPane.setAlignment(btnPlay, Pos.BOTTOM_CENTER);
        btnPlay.setTranslateY(-10);
        var btnPlayContainer = new BorderPane();
        btnPlayContainer.setBottom(btnPlay);

        layout.setLeft(left);
        layout.setRight(right);
        layout.setCenter(btnPlayContainer);

        root.getChildren().add(layout);
    }

    private Button createCarouselButton(char arrow) {
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
        pane.setBackground(coloredRoundedBackground(context.theme().color("startpage.button.bgColor"), 20));

        return pane;
    }

    private void setMsPacManImage(int number) {
        Image bgImage = context.theme().image("ms_pacman.startpage.image" + number);
        var bg = new Background(new BackgroundImage(bgImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, FIT_HEIGHT));
        layout.setBackground(bg);
        msPacManImageNumber = number;
    }

    private void setPacManImage(int number) {
        Image bgImage = context.theme().image("pacman.startpage.image" + number);
        var bg = new Background(new BackgroundImage(bgImage,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, FIT_HEIGHT));
        layout.setBackground(bg);
        pacManImageNumber = number;
    }

    private void changeBackgroundImage() {
        var variant = gameVariantPy.get();
        if (variant != null && context != null) {
            switch (variant) {
                case MS_PACMAN -> {
                    root.setBackground(context.theme().get("wallpaper.background"));
                    setMsPacManImage(1);
                    layout.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            setMsPacManImage(msPacManImageNumber == 1 ? 2 : 1);
                        }
                    });
                }
                case PACMAN -> {
                    root.setBackground(context.theme().get("wallpaper.background"));
                    setPacManImage(1);
                    layout.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            setPacManImage(pacManImageNumber == 1 ? 2 : 1);
                        }
                    });
                }
                case PACMAN_XXL -> {
                    root.setBackground(Ufx.coloredBackground(Color.BLACK));
                    Image bgImage = context.theme().image("pacman_xxl.startpage.image");
                    var bg = new Background(new BackgroundImage(bgImage,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER, FILL));
                    layout.setBackground(bg);
                    layout.setOnMouseClicked(null);
                }
            }
        }
    }

    @Override
    public Pane rootPane() {
        return root;
    }

    public void setOnPlayButtonPressed(Runnable action) {
        btnPlay.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                action.run();
            }
            e.consume(); // do not propagate event to layout such that image changes
        });
    }

    @Override
    public void onSelected() {
        if (context.gameClock().isRunning()) {
            context.gameClock().stop();
        }
    }

    @Override
    public void handleKeyboardInput() {
        if (GameKeys.ENTER_GAME_PAGE.pressed()) {
            context.actionHandler().selectGamePage();
        } else if (GameKeys.NEXT_VARIANT.pressed()) {
            context.actionHandler().selectNextGameVariant();
        } else if (GameKeys.PREV_VARIANT.pressed()) {
            context.actionHandler().selectPrevGameVariant();
        }
    }
}