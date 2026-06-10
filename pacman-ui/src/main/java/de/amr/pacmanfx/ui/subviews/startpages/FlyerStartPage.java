/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.startpages;

import de.amr.pacmanfx.ui.GlobalsUI;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.ui.action.CommonActions.ACTION_BOOT_SHOW_PLAY_VIEW;
import static java.util.Objects.requireNonNull;

public class FlyerStartPage extends StackPane implements StartPage {

    public static final Font  DEFAULT_START_BUTTON_FONT = Ufx.deriveFont(GlobalsUI.FONT_ARCADE_8, 32);
    public static final Color DEFAULT_START_BUTTON_BGCOLOR = Color.rgb(0, 155, 252, 0.6);
    public static final Color DEFAULT_START_BUTTON_FILLCOLOR = Color.WHITE;

    public static final KeyCode SHUT_UP_KEYCODE = KeyCode.S;

    protected final Flyer flyer = new Flyer();
    protected final String title;
    protected Node startButton;

    protected FlyerStartPage(String title) {
        this.title = requireNonNull(title);
        getChildren().add(flyer);
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
        addEventHandler(ScrollEvent.SCROLL, e-> {
            if (e.getDeltaY() < 0) {
                flyer.nextFlyerPage();
            } else if (e.getDeltaY() > 0) {
                flyer.prevFlyerPage();
            }
        });
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public void init(Game game) {
        requireNonNull(game);
        startButton = createStartButton(game);
        getChildren().add(startButton);

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == SHUT_UP_KEYCODE) {
                game.ui().sounds().stopAndDisposeVoice();
            }
        });
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

    public Node createStartButton(Game game) {
        final var startButton = new FancyButton(game.ui().translations().translate("play_button"),
            DEFAULT_START_BUTTON_FONT, DEFAULT_START_BUTTON_BGCOLOR, DEFAULT_START_BUTTON_FILLCOLOR);
        startButton.setAction(() -> ACTION_BOOT_SHOW_PLAY_VIEW.execute(game));
        startButton.translateYProperty().bind(heightProperty().multiply(-0.1));
        startButton.fontProperty().bind(heightProperty()
            .map(h -> Font.font(DEFAULT_START_BUTTON_FONT.getFamily(), Math.min(h.doubleValue() / 25, 48))));
        StackPane.setAlignment(startButton, Pos.BOTTOM_CENTER);
        return startButton;
    }
}