package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.scene.GameContext;
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

import java.util.EnumMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * @author Armin Reichert
 */
public class StartPage implements Page {

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

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            var variant = get();
            if (variant != null && context != null) {
                root.setBackground(switch (variant) {
                    case MS_PACMAN, PACMAN_XXL -> Ufx.coloredBackground(Color.BLACK);
                    case PACMAN -> context.theme().get("wallpaper.background");
                });
                layout.setBackground(layoutBackgrounds.get(variant));
            }
        }
    };

    private final GameContext context;
    private final StackPane root = new StackPane();
    private final BorderPane layout = new BorderPane();
    private final Node btnPlay;
    private final Map<GameVariant, Background> layoutBackgrounds = new EnumMap<>(GameVariant.class);

    public StartPage(GameContext context) {
        this.context = checkNotNull(context);

        for (var variant : GameVariant.values()) {
            layoutBackgrounds.put(variant, createLayoutBackground(variant));
        }

        gameVariantPy.set(context.game().variant());

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

        root.getChildren().add(layout);
    }

    private Background createLayoutBackground(GameVariant variant) {
        Image bgImage = context.theme().image(variant.resourceKey() + ".startpage.image");
        BackgroundSize size = switch (variant) {
            case MS_PACMAN, PACMAN -> FIT_HEIGHT;
            case PACMAN_XXL -> FILL;
        };
        return new Background(new BackgroundImage(bgImage,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, size));
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
        } else if (GameKeys.FULLSCREEN.pressed()) {
            context.actionHandler().setFullScreen(true);
        } else if (GameKeys.PAUSE.pressed()) {
            context.actionHandler().togglePaused();
        }
    }
}