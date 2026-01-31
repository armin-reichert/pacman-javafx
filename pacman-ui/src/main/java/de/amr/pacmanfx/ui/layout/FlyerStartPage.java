/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.StartPage;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_BOOT_SHOW_PLAY_VIEW;
import static java.util.Objects.requireNonNull;

public abstract class FlyerStartPage extends StackPane implements StartPage {

    public static final Font  DEFAULT_START_BUTTON_FONT = Ufx.deriveFont(GameUI_Resources.FONT_ARCADE_8, 32);
    public static final Color DEFAULT_START_BUTTON_BGCOLOR = Color.rgb(0, 155, 252, 0.7);
    public static final Color DEFAULT_START_BUTTON_FILLCOLOR = Color.rgb(255, 255, 255);

    public static final KeyCode SHUT_UP_KEYCODE = KeyCode.S;

    public static class DefaultStartButton extends FancyButton {

        public DefaultStartButton(String text, Runnable action) {
            super(text, DEFAULT_START_BUTTON_FONT, DEFAULT_START_BUTTON_BGCOLOR, DEFAULT_START_BUTTON_FILLCOLOR);
            StackPane.setAlignment(this, Pos.BOTTOM_CENTER);
            setAction(action);
        }
    }

    protected final Flyer flyer = new Flyer();
    protected String title;
    protected Node startButton;

    protected FlyerStartPage() {
        getChildren().add(flyer);

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP   -> flyer.prevFlyerPage();
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

    public Flyer flyer() {
        return flyer;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void init(GameUI ui) {
        requireNonNull(ui);
        startButton = createStartButton(ui);
        getChildren().add(startButton);

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == SHUT_UP_KEYCODE) {
                ui.voicePlayer().stop();
            }
        });
    }

    protected Node createStartButton(GameUI ui) {
        final Node button = new DefaultStartButton(ui.translate("play_button"), () -> ACTION_BOOT_SHOW_PLAY_VIEW.executeIfEnabled(ui));
        button.setTranslateY(-50);
        return button;
    }

    @Override
    public void onExitStartPage(GameUI ui) {
        ui.voicePlayer().stop();
    }

    @Override
    public Region layoutRoot() {
        return this;
    }

    @Override
    public String title() {
        return title;
    }
}