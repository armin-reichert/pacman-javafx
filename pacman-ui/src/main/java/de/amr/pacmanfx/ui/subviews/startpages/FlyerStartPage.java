/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.startpages;

import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class FlyerStartPage implements StartPage {

    public static final Font  DEFAULT_START_BUTTON_FONT = Ufx.deriveFont(GameUI_Constants.FONT_ARCADE_8, 32);
    public static final Color DEFAULT_START_BUTTON_BGCOLOR = Color.rgb(0, 155, 252, 0.6);
    public static final Color DEFAULT_START_BUTTON_FILL = Color.WHITE;

    protected final StackPane rootPane = new StackPane();
    protected final Flyer flyer = new Flyer();
    protected final String title;
    protected FancyButton startButton;
    protected Game game;

    protected FlyerStartPage(String title) {
        this.title = requireNonNull(title);

        rootPane.getChildren().add(flyer);

        rootPane.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
                case S -> {
                    if (game != null) {
                        game.ui().sounds().stopAndDisposeVoice();
                        game.shortMessage("OK, I shut my mouth");
                    }
                }
            }
        });

        rootPane.addEventHandler(ScrollEvent.SCROLL, e-> {
            if (e.getDeltaY() < 0) {
                flyer.nextFlyerPage();
            } else if (e.getDeltaY() > 0) {
                flyer.prevFlyerPage();
            }
        });
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public void onEnterStartPage(Game game) {
        if (startButton != null) {
            startButton.requestFocus();
        }
    }

    @Override
    public void onExitStartPage(Game game) {
        game.ui().sounds().stopAndDisposeVoice();
    }

    protected void init(Game game) {
        requireNonNull(game);
        if (this.game == null) {
            final String buttonText = game.ui().translations().translate("play_button");
            startButton = createStartButton(buttonText);
            StackPane.setAlignment(startButton, Pos.BOTTOM_CENTER);
            rootPane.getChildren().add(startButton);
            startButton.setAction(() -> CommonActions.ACTION_START_GAME.execute(game));
            Logger.info("Start button for page {} created", this);
        }
        this.game = game;
    }

    private FancyButton createStartButton(String text) {
        final var button = new FancyButton(
            text,
            DEFAULT_START_BUTTON_FONT,
            DEFAULT_START_BUTTON_BGCOLOR,
            DEFAULT_START_BUTTON_FILL);

        button.translateYProperty().bind(rootPane.heightProperty().multiply(-0.1));

        button.fontProperty().bind(rootPane.heightProperty().map(
            pageHeight -> Font.font(
                DEFAULT_START_BUTTON_FONT.getFamily(),
                computeButtonHeight(pageHeight.doubleValue())))
        );

        return button;
    }

    private static double computeButtonHeight(double pageHeight) {
        return Math.min(pageHeight / 25, 48);
    }
}