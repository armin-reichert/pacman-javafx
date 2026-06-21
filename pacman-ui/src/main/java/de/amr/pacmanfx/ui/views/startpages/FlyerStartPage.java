/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.startpages;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.uilib.controls.GameStartButton;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class FlyerStartPage implements StartPage {

    protected final StackPane rootPane = new StackPane();
    protected final Flyer flyer = new Flyer();
    protected final String title;
    protected GameStartButton startButton;
    protected Game game;
    protected Media voice;

    public FlyerStartPage(String title) {
        this.title = requireNonNull(title);

        rootPane.getStyleClass().add("flyer-start-page");

        rootPane.getChildren().add(flyer);

        // Let scroll wheel scroll through flyer pages
        rootPane.addEventHandler(ScrollEvent.SCROLL, e-> {
            if (e.getDeltaY() < 0) {
                flyer.nextFlyerPage();
            } else if (e.getDeltaY() > 0) {
                flyer.prevFlyerPage();
            }
        });
    }

    @Override
    public void onInput(Input input) {
        final Keyboard keyboard = input.keyboard();
        if (keyboard.isKeyPressed(KeyCode.DOWN)) {
            flyer.nextFlyerPage();
        }
        else if (keyboard.isKeyPressed(KeyCode.UP)) {
            flyer.prevFlyerPage();
        }
        else if (keyboard.isKeyPressed(KeyCode.S)) {
            if (game != null) {
                game.ui().sounds().stopAndDisposeVoice();
                game.ui().shortMessage(game.ui().translations().translate("flash.shut_up"));
            }
        }
        else if (keyboard.isKeyPressed(KeyCode.ENTER) && startButton != null) {
            startButton.fire();
        }
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);
        if (startButton == null) {
            final String text = game.ui().translations().translate("startpage.play_button");
            startButton = new GameStartButton(text);
            startButton.setOnAction(_ -> game.actions().gameFlowActions().actionStartGame().execute());
            rootPane.getChildren().add(startButton);

            StackPane.setAlignment(startButton, Pos.BOTTOM_CENTER);
            startButton.translateYProperty().bind(rootPane.heightProperty().divide(10).negate());
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
    public Optional<Node> startButton() {
        return Optional.of(startButton);
    }

    @Override
    public String title() {
        return title;
    }

    public void setVoice(Media voice) {
        this.voice = requireNonNull(voice);
    }

    public void startTalking() {
        if (voice != null) {
            game.ui().sounds().playVoice(voice);
        }
    }

    public void stopTalking() {
        game.ui().sounds().stopAndDisposeVoice();
    }
}