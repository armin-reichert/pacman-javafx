package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.AbstractGameAction;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.KeyInput;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.util.KeyInput.key;
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
        Color bgColor = Color.rgb(0, 155, 252, 0.7);
        Color fillColor = Color.WHITE;

        var text = new Text(buttonText);
        text.setFill(fillColor);
        text.setFont(assets.font("font.arcade", 30));

        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));
        text.setEffect(shadow);

        var pane = new BorderPane(text);
        pane.setMaxSize(200, 100);
        pane.setPadding(new Insets(10));
        pane.setCursor(Cursor.HAND);
        pane.setBackground(coloredRoundedBackground(bgColor, 20));

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

    private final GameAction actionPrevFlyerPage = new AbstractGameAction(KeyInput.key(KeyCode.UP)) {
        @Override
        public void execute(GameContext context) {
            flyer(context.gameVariant()).ifPresent(Flyer::prevPage);
        }
    };

    private final GameAction actionNextFlyerPage = new AbstractGameAction(KeyInput.key(KeyCode.DOWN)) {
        @Override
        public void execute(GameContext context) {
            flyer(context.gameVariant()).ifPresent(Flyer::nextPage);
        }
    };

    private final GameAction actionPrevVariant = new AbstractGameAction(key(KeyCode.LEFT)) {
        @Override
        public void execute(GameContext context) {
            List<GameVariant> variantsInOrder = PacManGames2dUI.GAME_VARIANTS_IN_ORDER;
            int prevIndex = variantsInOrder.indexOf(context.gameVariant()) - 1;
            context.selectGameVariant(variantsInOrder.get(prevIndex < 0 ? variantsInOrder.size() - 1 : prevIndex));
        }
    };

    private final GameAction actionNextVariant = new AbstractGameAction(key(KeyCode.V), key(KeyCode.RIGHT)) {
        @Override
        public void execute(GameContext context) {
            List<GameVariant> variantsInOrder = PacManGames2dUI.GAME_VARIANTS_IN_ORDER;
            int nextIndex = variantsInOrder.indexOf(context.gameVariant()) + 1;
            context.selectGameVariant(variantsInOrder.get(nextIndex == variantsInOrder.size() ? 0 : nextIndex));
        }
    };

    private final GameAction actionEnterGamePage = new AbstractGameAction(key(KeyCode.SPACE), key(KeyCode.ENTER)) {
        @Override
        public void execute(GameContext context) {
            context.selectGamePage();
        }
    };

    private final GameContext context;
    private final List<GameAction> actions = new ArrayList<>();
    private final BorderPane layout = new BorderPane();
    private final Flyer msPacManFlyer, pacManFlyer, tengenFlyer;

    public StartPage(GameContext context) {
        this.context = checkNotNull(context);
        AssetStorage assets = context.assets();

        actions.addAll(List.of(actionPrevFlyerPage, actionNextFlyerPage, actionPrevVariant, actionNextVariant, actionEnterGamePage));
        actions.forEach(action -> context.keyboard().register(action.trigger()));

        msPacManFlyer = new Flyer(assets.image("ms_pacman.startpage.image1"), assets.image("ms_pacman.startpage.image2"));
        pacManFlyer   = new Flyer(assets.image("pacman.startpage.image1"),    assets.image("pacman.startpage.image2"));
        tengenFlyer   = new Flyer(assets.image("tengen.startpage.image1"),    assets.image("tengen.startpage.image2"));

        Button btnPrevVariant = createCarouselButton(assets.image("startpage.arrow.left"));
        btnPrevVariant.setOnAction(e -> actionPrevVariant.execute(context));
        VBox left = new VBox(btnPrevVariant);
        left.setAlignment(Pos.CENTER_LEFT);

        Button btnNextVariant = createCarouselButton(assets.image("startpage.arrow.right"));
        btnNextVariant.setOnAction(e -> actionNextVariant.execute(context));
        VBox right = new VBox(btnNextVariant);
        right.setAlignment(Pos.CENTER_RIGHT);

        Node btnPlay = createPlayButton(context.locText("play_button"), context.assets());
        BorderPane.setAlignment(btnPlay, Pos.BOTTOM_CENTER);
        switch (context.gameVariant()) {
            case MS_PACMAN        -> {btnPlay.setTranslateY(-60);}
            case MS_PACMAN_TENGEN -> {btnPlay.setTranslateY(-60);}
            case PACMAN           -> {btnPlay.setTranslateY(-60);}
            case PACMAN_XXL       -> {btnPlay.setTranslateY(-60);}
        }
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
        context.doFirstCalledAction(actions);
    }

    @Override
    public void setSize(double width, double height) {
    }
}