/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Shows flash messages with varying duration. Message fades out and gets removed after this time.
 */
public class FlashMessageView {

    // Everything in nanoseconds
    record TemporaryMessage(String text, long start, long end)
    {
        public boolean hasExpired() {
            return System.nanoTime() > end;
        }

        public long passed() {
            return System.nanoTime() - start;
        }

        public long duration() {
            return end - start;
        }
    }

    private static FlashMessageView.TemporaryMessage createAndActivateMessage(String text, double durationSec) {
        long now = System.nanoTime();
        long duration = (long) (durationSec * 1_000_000_000);
        return new FlashMessageView.TemporaryMessage(text, now, now + duration);
    }

    private static final double HALF_PI = Math.PI / 2;
    private static final Font MESSAGE_FONT = Font.font("Sans", FontWeight.BOLD, 30);
    private static final Color TEXT_COLOR_PLAIN = Color.WHEAT;

    private final VBox rootPane = new VBox();

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> textFill = new SimpleObjectProperty<>();
    private final ObjectProperty<TemporaryMessage> message = new SimpleObjectProperty<>();

    private final AnimationTimer timer;

    public FlashMessageView() {
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setMouseTransparent(true);
        rootPane.visibleProperty().bind(message.isNotNull());

        final Text messageView = new Text();
        messageView.setFont(MESSAGE_FONT);
        messageView.setTextAlignment(TextAlignment.CENTER);
        messageView.fillProperty().bind(textFill);
        messageView.textProperty().bind(message.map(msg -> msg != null ? msg.text : ""));

        rootPane.getChildren().add(messageView);
        rootPane.backgroundProperty().bind(backgroundColor.map(Background::fill));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                final TemporaryMessage msg = message.get();
                if (msg != null) {
                    if (msg.hasExpired()) {
                        clearMessage();
                    }
                    else {
                        final double t = (double) msg.passed() / msg.duration();
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
    }

    public void clearMessage() {
        message.set(null);
        backgroundColor.set(Color.TRANSPARENT);
    }

    public void showMessage(String messageText, double seconds) {
        requireNonNull(messageText);
        TemporaryMessage tmpMessage = message.get();
        if (tmpMessage!= null && tmpMessage.text.equals(messageText)) {
            return; // already showing
        }
        message.set(createAndActivateMessage(messageText, seconds));
    }
}