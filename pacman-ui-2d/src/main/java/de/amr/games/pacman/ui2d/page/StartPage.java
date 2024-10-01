package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.AssetStorage;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredRoundedBackground;

/**
 * Got the flyer images from <a href="https://flyers.arcade-museum.com/">The Arcade Flyer Archive</a>.
 *
 * @author Armin Reichert
 */
public class StartPage extends StackPane implements Page {

    private static Button createCarouselButton(Image image) {
        ImageView icon = new ImageView(image);
        icon.setFitHeight(32);
        icon.setFitWidth(32);
        var button = new Button();
        // Without this, button gets input focus after being clicked with the mouse and the LEFT, RIGHT keys stop working!
        button.setFocusTraversable(false);
        button.setGraphic(icon);
        button.setOpacity(0.1);
        button.setOnMouseEntered(e -> button.setOpacity(0.4));
        button.setOnMouseExited(e -> button.setOpacity(0.1));
        return button;
    }

    private static Node createPlayButton(String buttonText, AssetStorage assets) {
        var text = new Text(buttonText);
        text.setFill(assets.color("startpage.button.color"));
        text.setFont(assets.font("startpage.button.font"));

        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));
        text.setEffect(shadow);

        var pane = new BorderPane(text);
        pane.setMaxSize(200, 100);
        pane.setPadding(new Insets(10));
        pane.setCursor(Cursor.HAND);
        pane.setBackground(coloredRoundedBackground(assets.color("startpage.button.bgColor"), 20));

        return pane;
    }

    private class Flyer {
        Image[] images;
        int index;

        Flyer(Image... images) {
            this.images = images;
        }

        void setPage(int index) {
            this.index = index;
            layout.setBackground(new Background(new BackgroundImage(images[index],
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, Ufx.FIT_HEIGHT))
            );
        }

        void nextPage() {
            setPage((index + 1) % images.length);
        }

        void prevPage() {
            setPage((index - 1 + images.length) % images.length);
        }
    }

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            if (get() != null) {
                handleGameVariantChange(get());
            }
        }
    };

    private final GameContext context;
    private final BorderPane layout = new BorderPane();
    private final Flyer msPacManFlyer, pacManFlyer, tengenFlyer;

    public StartPage(GameContext context) {
        this.context = checkNotNull(context);
        AssetStorage assets = context.assets();

        msPacManFlyer = new Flyer(assets.image("ms_pacman.startpage.image1"), assets.image("ms_pacman.startpage.image2"));
        pacManFlyer   = new Flyer(assets.image("pacman.startpage.image1"),    assets.image("pacman.startpage.image2"));
        tengenFlyer   = new Flyer(assets.image("tengen.startpage.image1"),    assets.image("tengen.startpage.image2"));

        Button btnPrevVariant = createCarouselButton(assets.image("startpage.arrow.left"));
        btnPrevVariant.setOnAction(e -> GameAction.PREV_VARIANT.execute(context));
        VBox left = new VBox(btnPrevVariant);
        left.setAlignment(Pos.CENTER_LEFT);

        Button btnNextVariant = createCarouselButton(assets.image("startpage.arrow.right"));
        btnNextVariant.setOnAction(e -> GameAction.NEXT_VARIANT.execute(context));
        VBox right = new VBox(btnNextVariant);
        right.setAlignment(Pos.CENTER_RIGHT);

        Node btnPlay = createPlayButton(context.locText("play_button"), context.assets());
        BorderPane.setAlignment(btnPlay, Pos.BOTTOM_CENTER);
        btnPlay.setTranslateY(-40);
        btnPlay.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                context.selectGamePage();
            }
            e.consume(); // do not propagate event to layout such that source changes
        });
        var btnPlayContainer = new BorderPane();
        btnPlayContainer.setBottom(btnPlay);

        layout.setLeft(left);
        layout.setRight(right);
        layout.setCenter(btnPlayContainer);

        setBackground(context.assets().get("wallpaper.pacman"));
        getChildren().add(layout);
    }

    private Optional<Flyer> flyer(GameVariant variant) {
        return Optional.ofNullable(switch (variant) {
            case PACMAN -> pacManFlyer;
            case MS_PACMAN -> msPacManFlyer;
            case MS_PACMAN_TENGEN -> tengenFlyer;
            default -> null;
        });
    }

    private void handleGameVariantChange(GameVariant variant) {
        switch (variant) {
            case MS_PACMAN, MS_PACMAN_TENGEN, PACMAN -> flyer(variant).ifPresent(flyer -> {
                flyer.setPage(0);
                setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        flyer.nextPage();
                    }
                });
            });
            case PACMAN_XXL -> {
                Image xxlGameImage = context.assets().image("pacman_xxl.startpage.source");
                var xxlGameBackground = Ufx.imageBackground(xxlGameImage,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, Ufx.FILL_PAGE);
                layout.setBackground(xxlGameBackground);
                setOnMouseClicked(null);
            }
        }
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    @Override
    public void onPageSelected() {
        if (context.gameClock().isRunning()) {
            context.gameClock().stop();
        }
    }

    @Override
    public void handleInput() {
        if      (GameAction.ENTER_GAME_PAGE.called()) { context.selectGamePage(); }
        else if (GameAction.NEXT_VARIANT.called())    { GameAction.NEXT_VARIANT.execute(context); }
        else if (GameAction.PREV_VARIANT.called())    { GameAction.PREV_VARIANT.execute(context); }
        else if (GameAction.NEXT_FLYER_PAGE.called()) { flyer(context.game().variant()).ifPresent(Flyer::nextPage); }
        else if (GameAction.PREV_FLYER_PAGE.called()) { flyer(context.game().variant()).ifPresent(Flyer::prevPage); }
    }

    @Override
    public void setSize(double width, double height) {
    }
}