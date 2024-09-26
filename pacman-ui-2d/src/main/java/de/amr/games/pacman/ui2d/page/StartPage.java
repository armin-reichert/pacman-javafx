package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
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

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredRoundedBackground;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * Got the flyer images from <a href="https://flyers.arcade-museum.com/">The Arcade Flyer Archive</a>.
 *
 * @author Armin Reichert
 */
public class StartPage extends StackPane implements Page {

    static final BackgroundSize FIT_HEIGHT = new BackgroundSize(AUTO, 1, false, true, true, false);
    static final BackgroundSize FILL       = new BackgroundSize(AUTO, AUTO, false, false, true, true);

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            initPageForGameVariant(get());
        }
    };

    private final GameContext context;
    private final BorderPane layout = new BorderPane();
    private final Image[] msPacManFlyerPages;
    private final Image[] pacManFlyerPages;
    private final Image[] tengenFlyerPages;

    private int msPacManFlyerIndex;
    private int pacManFlyerIndex;
    private int tengenFlyerIndex;

    public StartPage(GameContext context) {
        this.context = checkNotNull(context);

        msPacManFlyerPages = new Image[] {
            context.assets().image("ms_pacman.startpage.image1"),
            context.assets().image("ms_pacman.startpage.image2")
        };
        pacManFlyerPages = new Image[] {
            context.assets().image("pacman.startpage.image1"),
            context.assets().image("pacman.startpage.image2")
        };
        tengenFlyerPages = new Image[] {
            context.assets().image("tengen.startpage.image1"),
            context.assets().image("tengen.startpage.image2")
        };

        Button btnPrevVariant = createCarouselButton(context.assets().image("startpage.arrow.left"));
        btnPrevVariant.setOnAction(e -> context.selectPrevGameVariant());
        VBox left = new VBox(btnPrevVariant);
        left.setAlignment(Pos.CENTER_LEFT);

        Button btnNextVariant = createCarouselButton(context.assets().image("startpage.arrow.right"));
        btnNextVariant.setOnAction(e -> context.selectNextGameVariant());
        VBox right = new VBox(btnNextVariant);
        right.setAlignment(Pos.CENTER_RIGHT);

        Node btnPlay = createPlayButton(context.locText("play_button"));
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

        getChildren().add(layout);
    }

    private Button createCarouselButton(Image image) {
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

    private Node createPlayButton(String buttonText) {
        var text = new Text(buttonText);
        text.setFill(context.assets().color("startpage.button.color"));
        text.setFont(context.assets().font("startpage.button.font"));

        var shadow = new DropShadow();
        shadow.setOffsetY(3.0f);
        shadow.setColor(Color.color(0.2f, 0.2f, 0.2f));
        text.setEffect(shadow);

        var pane = new BorderPane(text);
        pane.setMaxSize(200, 100);
        pane.setPadding(new Insets(10));
        pane.setCursor(Cursor.HAND);
        pane.setBackground(coloredRoundedBackground(context.assets().color("startpage.button.bgColor"), 20));

        return pane;
    }

    private int selectFlyerPage(Image[] flyerPages, int page) {
        layout.setBackground(new Background(new BackgroundImage(flyerPages[page],
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, FIT_HEIGHT)));
        return page;
    }

    private void browseMsPacManFlyer(boolean forward) {
        int n = msPacManFlyerPages.length, delta = forward ? 1 : n - 1;
        msPacManFlyerIndex = selectFlyerPage(msPacManFlyerPages, (msPacManFlyerIndex + delta) % n);
    }

    private void browsePacManFlyer(boolean forward) {
        int n = pacManFlyerPages.length, delta = forward ? 1 : n - 1;
        pacManFlyerIndex = selectFlyerPage(pacManFlyerPages, (pacManFlyerIndex + delta) % n);
    }

    private void browseTengenFlyer(boolean forward) {
        int n = tengenFlyerPages.length, delta = forward ? 1 : n - 1;
        tengenFlyerIndex = selectFlyerPage(tengenFlyerPages, (tengenFlyerIndex + delta) % n);
    }

    private void initPageForGameVariant(GameVariant variant) {
        setBackground(context.assets().get("wallpaper.pacman"));
        if (variant != null && context != null) {
            switch (variant) {
                case MS_PACMAN -> {
                    msPacManFlyerIndex = selectFlyerPage(msPacManFlyerPages, 0);
                    setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            browseMsPacManFlyer(true);
                        }
                    });
                }
                case MS_PACMAN_TENGEN -> {
                    tengenFlyerIndex = selectFlyerPage(tengenFlyerPages, 0);
                    setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            browseTengenFlyer(true);
                        }
                    });
                }
                case PACMAN -> {
                    pacManFlyerIndex = selectFlyerPage(pacManFlyerPages, 0);
                    setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            browsePacManFlyer(true);
                        }
                    });
                }
                case PACMAN_XXL -> {
                    Image xxlGameImage = context.assets().image("pacman_xxl.startpage.source");
                    var xxlGameBackground = new Background(new BackgroundImage(xxlGameImage,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER, FILL));
                    layout.setBackground(xxlGameBackground);
                    setOnMouseClicked(null);
                }
            }
        }
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    @Override
    public void onSelected() {
        if (context.gameClock().isRunning()) {
            context.gameClock().stop();
        }
    }

    @Override
    public void handleInput() {
        if (GameAction.ENTER_GAME_PAGE.triggered()) {
            context.selectGamePage();
        } else if (GameAction.NEXT_VARIANT.triggered()) {
            context.selectNextGameVariant();
        } else if (GameAction.PREV_VARIANT.triggered()) {
            context.selectPrevGameVariant();
        } else if (GameAction.NEXT_FLYER_PAGE.triggered()) {
            switch (context.game().variant()) {
                case MS_PACMAN        -> browseMsPacManFlyer(true);
                case MS_PACMAN_TENGEN -> browseTengenFlyer(true);
                case PACMAN           -> browsePacManFlyer(true);
                case PACMAN_XXL       -> {}
            }
        } else if (GameAction.PREV_FLYER_PAGE.triggered()) {
            switch (context.game().variant()) {
                case MS_PACMAN        -> browseMsPacManFlyer(false);
                case MS_PACMAN_TENGEN -> browseTengenFlyer(false);
                case PACMAN           -> browsePacManFlyer(false);
                case PACMAN_XXL       -> {}
            }
        }
    }

    @Override
    public void setSize(double width, double height) {
    }
}