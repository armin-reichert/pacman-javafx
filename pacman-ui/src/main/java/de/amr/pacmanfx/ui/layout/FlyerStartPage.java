/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_StartPage;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_BOOT_SHOW_PLAY_VIEW;
import static java.util.Objects.requireNonNull;

public abstract class FlyerStartPage extends StackPane implements GameUI_StartPage {

    public static final Color DEFAULT_START_BUTTON_BGCOLOR = Color.rgb(0, 155, 252, 0.7);
    public static final Color DEFAULT_START_BUTTON_FILLCOLOR = Color.rgb(255, 255, 255);

    protected final String gameVariant;
    protected final Flyer flyer;
    protected final Node startButton;

    protected String title;

    protected FlyerStartPage(GameUI ui, String gameVariant) {
        requireNonNull(ui);
        this.gameVariant = requireNonNull(gameVariant);

        flyer = createFlyer();
        startButton = createStartButton(ui);
        getChildren().addAll(flyer, startButton);

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

    protected abstract Flyer createFlyer();

    protected Node createDefaultStartButton(GameUI ui, Runnable action) {
        var button = new FancyButton(
            ui.translated("play_button"),
            Font.font(GameUI.FONT_ARCADE_8.getFamily(), 30),
            DEFAULT_START_BUTTON_BGCOLOR, DEFAULT_START_BUTTON_FILLCOLOR);
        button.setAction(action);
        StackPane.setAlignment(button, Pos.BOTTOM_CENTER);
        return button;
    }

    protected Node createStartButton(GameUI ui) {
        Node button = createDefaultStartButton(ui, () -> {
            ui.soundManager().stopVoice();
            ACTION_BOOT_SHOW_PLAY_VIEW.executeIfEnabled(ui);
        });
        button.setTranslateY(-50);
        return button;
    }

    @Override
    public void onEnter(GameUI ui) {
        flyer.selectPage(0);
        ui.selectGameVariant(gameVariant);
        ui.soundManager().playVoiceAfterSec(1, SoundID.VOICE_FLYER_TEXT);
    }

    @Override
    public void onExit(GameUI ui) {
        ui.soundManager().stopVoice();
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