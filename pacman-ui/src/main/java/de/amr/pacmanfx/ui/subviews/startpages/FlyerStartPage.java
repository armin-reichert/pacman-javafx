/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.startpages;

import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static java.util.Objects.requireNonNull;

public class FlyerStartPage implements StartPage {

    public record Config(
        Font startButtonFont,
        Color startButtonBgColor,
        Color startButtonTextColor
    ) {}

    public static final Config DEFAULT_CONFIG = new Config(
        Ufx.deriveFont(GameUI_Constants.FONT_ARCADE_8, 32),
        Color.rgb(0, 155, 252, 0.6),
        Color.WHITE);

    protected Config config = DEFAULT_CONFIG;

    protected final StackPane rootPane = new StackPane();
    protected final Flyer flyer = new Flyer();
    protected final String title;
    protected FancyButton startButton;
    protected Game game;
    protected Media voice;

    protected FlyerStartPage(String title) {
        this.title = requireNonNull(title);

        rootPane.getChildren().add(flyer);

        //TODO use global keyboard instead?
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
    public void connect(Game game) {
        this.game = requireNonNull(game);
        if (startButton == null) {
            createAndAddStartButton(game);
        }
    }

    @Override
    public void onEnter() {
        if (startButton != null) {
            startButton.requestFocus();
        }
    }

    @Override
    public void onExit() {
        stopTalking();
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public String title() {
        return title;
    }

    protected void setVoice(Media voice) {
        this.voice = voice;
    }

    protected void startTalking() {
        if (voice != null) {
            game.ui().sounds().playVoice(voice);
        }
    }

    protected void stopTalking() {
        game.ui().sounds().stopAndDisposeVoice();
    }

    private void createAndAddStartButton(Game game) {
        final String buttonText = game.ui().translations().translate("play_button");
        startButton = createAndAddStartButton(buttonText);
        StackPane.setAlignment(startButton, Pos.BOTTOM_CENTER);
        rootPane.getChildren().add(startButton);
        startButton.setAction(() -> game.actions().ACTION_START_GAME.execute());
    }

    private FancyButton createAndAddStartButton(String text) {
        final var button = new FancyButton(text, config.startButtonFont(), config.startButtonBgColor(), config.startButtonTextColor());
        button.translateYProperty().bind(rootPane.heightProperty().multiply(-0.1));
        button.fontProperty().bind(rootPane.heightProperty().map(
            pageHeight -> Font.font(
                config.startButtonFont().getFamily(),
                computeButtonHeight(pageHeight.doubleValue())))
        );
        return button;
    }

    private static double computeButtonHeight(double pageHeight) {
        return Math.min(pageHeight / 25, 48);
    }
}