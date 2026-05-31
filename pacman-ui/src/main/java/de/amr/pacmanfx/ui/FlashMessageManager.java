/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static java.util.Objects.requireNonNull;

/**
 * Shows flash messages with varying duration. Message fades out and gets removed after this time.
 */
public class FlashMessageManager {

    // Everything in nanoseconds
    record TemporaryMessage(String text, long start, long end)
    {
        public boolean hasExpired(long now) {
            return now > end;
        }

        public long passed(long now) {
            return now - start;
        }

        public long duration() {
            return end - start;
        }
    }

    private static FlashMessageManager.TemporaryMessage createAndActivateMessage(String text, double durationSec) {
        long now = System.nanoTime();
        long duration = (long) (durationSec * 1_000_000_000);
        return new FlashMessageManager.TemporaryMessage(text, now, now + duration);
    }

    private static final double HALF_PI = Math.PI / 2;
    private static final Font MESSAGE_FONT = Font.font("Sans", FontWeight.BOLD, 30);
    private static final Color TEXT_COLOR_PLAIN = Color.WHEAT;

    private final VBox rootPane = new VBox();

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> textFill = new SimpleObjectProperty<>();
    private final ObjectProperty<TemporaryMessage> message = new SimpleObjectProperty<>();

    private final AnimationTimer timer;

    public FlashMessageManager() {
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setMouseTransparent(true);
        rootPane.visibleProperty().bind(message.isNotNull());

        final Text messageView = new Text();
        messageView.setFont(MESSAGE_FONT);
        messageView.setTextAlignment(TextAlignment.CENTER);
        messageView.fillProperty().bind(textFill);
        messageView.textProperty().bind(Bindings.createStringBinding(
            () -> {
                var msg = message.get();
                return msg != null ? msg.text() : "";
            },
            message
        ));

        rootPane.getChildren().add(messageView);
        rootPane.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> Background.fill(backgroundColor.get()),
            backgroundColor
        ));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                final TemporaryMessage msg = message.get();
                if (msg != null) {
                    if (msg.hasExpired(now)) {
                        clearMessage();
                    }
                    else {
                        final double t = (double) msg.passed(now) / msg.duration();
                        final double a = Math.cos(HALF_PI * t);
                        backgroundColor.set(Color.rgb(0, 0, 0, 0.2 + 0.5 * a));
                        textFill.set(TEXT_COLOR_PLAIN.deriveColor(0, 1, 1, a));
                    }
                }
            }
        };
    }

    public Pane rootPane() {
        return rootPane;
    }

    public void startTimer() {
        timer.start();
    }

    public void stopTimer() {
        timer.stop();
        clearMessage();
    }

    public void clearMessage() {
        message.set(null);
        backgroundColor.set(Color.TRANSPARENT);
    }

    public void showMessage(String messageText, double seconds) {
        requireNonNull(messageText);
        TemporaryMessage tmpMessage = message.get();
        if (tmpMessage!= null && tmpMessage.text().equals(messageText)) {
            return; // already showing
        }
        backgroundColor.set(Color.rgb(0, 0, 0, 0.7));
        textFill.set(TEXT_COLOR_PLAIN);
        message.set(createAndActivateMessage(messageText, seconds));
    }
}