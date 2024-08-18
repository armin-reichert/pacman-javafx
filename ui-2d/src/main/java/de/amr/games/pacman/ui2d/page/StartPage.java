package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKey;
import de.amr.games.pacman.ui2d.util.Keyboard;
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
import javafx.scene.input.KeyCode;
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
 * Got the flyer images from <a href="https://flyers.arcade-museum.com/">The Arcade Flyer Archive</a>.
 *
 * @author Armin Reichert
 */
public class StartPage extends StackPane implements Page {

    static final char ARROW_LEFT  = '\u2b98';
    static final char ARROW_RIGHT = '\u2b9a';

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
    private int msPacManFlyerPage = 0;

    private final Image[] pacManFlyerPages;
    private int pacManFlyerPage = 0;

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

        var btnPrevVariant = createCarouselButton(ARROW_LEFT);
        btnPrevVariant.setOnAction(e -> context.actionHandler().selectPrevGameVariant());
        VBox left = new VBox(btnPrevVariant);
        left.setAlignment(Pos.CENTER_LEFT);

        var btnNextVariant = createCarouselButton(ARROW_RIGHT);
        btnNextVariant.setOnAction(e -> context.actionHandler().selectNextGameVariant());
        VBox right = new VBox(btnNextVariant);
        right.setAlignment(Pos.CENTER_RIGHT);

        Node btnPlay = createPlayButton(context.locText("play_button"));
        BorderPane.setAlignment(btnPlay, Pos.BOTTOM_CENTER);
        btnPlay.setTranslateY(-10);
        var btnPlayContainer = new BorderPane();
        btnPlayContainer.setBottom(btnPlay);
        btnPlay.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                context.actionHandler().selectGamePage();
            }
            e.consume(); // do not propagate event to layout such that image changes
        });

        layout.setLeft(left);
        layout.setRight(right);
        layout.setCenter(btnPlayContainer);

        getChildren().add(layout);
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

    private void browseMsPacManFlyer() {
        int nextPage = (msPacManFlyerPage + 1) % msPacManFlyerPages.length;
        msPacManFlyerPage = selectFlyerPage(msPacManFlyerPages, nextPage);
    }

    private void browsePacManFlyer()
    {
        int nextPage = (pacManFlyerPage + 1) % pacManFlyerPages.length;
        pacManFlyerPage = selectFlyerPage(pacManFlyerPages, nextPage);
    }

    private void initPageForGameVariant(GameVariant variant) {
        if (variant != null && context != null) {
            switch (variant) {
                case MS_PACMAN -> {
                    setBackground(context.assets().get("wallpaper.background"));
                    msPacManFlyerPage = selectFlyerPage(msPacManFlyerPages, 0);
                    layout.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            browseMsPacManFlyer();
                        }
                    });
                }
                case PACMAN -> {
                    setBackground(context.assets().get("wallpaper.background"));
                    pacManFlyerPage = selectFlyerPage(pacManFlyerPages, 0);
                    layout.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            browsePacManFlyer();
                        }
                    });
                }
                case PACMAN_XXL -> {
                    setBackground(Ufx.coloredBackground(Color.BLACK));
                    Image bgImage = context.assets().image("pacman_xxl.startpage.image");
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
        return this;
    }

    @Override
    public void onSelected() {
        if (context.gameClock().isRunning()) {
            context.gameClock().stop();
        }
    }

    @Override
    public void handleKeyboardInput(ActionHandler handler) {
        if (GameKey.ENTER_GAME_PAGE.pressed()) {
            handler.selectGamePage();
        } else if (GameKey.NEXT_VARIANT.pressed()) {
            handler.selectNextGameVariant();
        } else if (GameKey.PREV_VARIANT.pressed()) {
            handler.selectPrevGameVariant();
        } else if (Keyboard.pressed(KeyCode.DOWN) || Keyboard.pressed(KeyCode.UP)) {
            if (context.game().variant() == GameVariant.MS_PACMAN) {
                browseMsPacManFlyer();
            } else if (context.game().variant() == GameVariant.PACMAN) {
                browsePacManFlyer();
            }
        }
    }

    @Override
    public void setSize(double width, double height) {
    }
}