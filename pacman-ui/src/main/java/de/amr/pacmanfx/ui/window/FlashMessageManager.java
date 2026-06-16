/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
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

    private static final double HALF_PI = Math.PI / 2;

    // Everything time is in nanoseconds
    record FlashMessage(String text, long start, long end)
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

    private static FlashMessage activateNewMessage(String text, double durationSec) {
        final long now = System.nanoTime();
        final long duration = (long) (durationSec * 1_000_000_000);
        return new FlashMessage(text, now, now + duration);
    }

    public class FlashMessageView {

        private static final Font MESSAGE_FONT = Font.font("Sans", FontWeight.BOLD, 30);
        private static final Color TEXT_COLOR_PLAIN = Color.WHEAT;

        private final VBox rootPane = new VBox();
        private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();
        private final ObjectProperty<Color> textFill = new SimpleObjectProperty<>();

        public FlashMessageView() {
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
        }

        public VBox rootPane() {
            return rootPane;
        }

        public void initTextFill() {
            textFill.set(TEXT_COLOR_PLAIN);
        }

        public void setFillAlpha(double a) {
            textFill.set(FlashMessageView.TEXT_COLOR_PLAIN.deriveColor(0, 1, 1, a));
        }

        public void setBackgroundAlpha(double a) {
            backgroundColor.set(Color.rgb(0, 0, 0, 0.2 + 0.5 * a));
        }

        public void initBackground() {
            messageView.backgroundColor.set(Color.rgb(0, 0, 0, 0.7));
        }
    }

    private final ObjectProperty<FlashMessage> message = new SimpleObjectProperty<>();
    private final AnimationTimer timer;
    private final FlashMessageView messageView = new FlashMessageView();

    public FlashMessageManager() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                final FlashMessage msg = message.get();
                if (msg != null) {
                    if (msg.hasExpired(now)) {
                        clearMessage();
                    }
                    else {
                        final double t = (double) msg.passed(now) / msg.duration();
                        final double alpha = Math.cos(HALF_PI * t);
                        messageView.setBackgroundAlpha(alpha);
                        messageView.setFillAlpha(alpha);
                    }
                }
            }
        };
    }

    public FlashMessageView messageView() {
        return messageView;
    }

    public void startAnimationTimer() {
        timer.start();
    }

    public void stopAnimationTimer() {
        timer.stop();
        clearMessage();
    }

    public void clearMessage() {
        message.set(null);
        messageView.backgroundColor.set(Color.TRANSPARENT);
    }

    public void showMessage(String messageText, double seconds) {
        requireNonNull(messageText);
        FlashMessage tmpMessage = message.get();
        if (tmpMessage!= null && tmpMessage.text().equals(messageText)) {
            return; // already showing
        }
        messageView.initBackground();
        messageView.initTextFill();
        message.set(activateNewMessage(messageText, seconds));
    }
}