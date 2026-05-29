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

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Shows flash messages with varying duration. Message fades out and gets removed after this time.
 */
public class FlashMessageView {

    private static class TemporaryMessage {
        private final String text;
        private final double durationSeconds;
        private Instant activationBegin;
        private Instant activationEnd;

        TemporaryMessage(String text, double seconds) {
            this.text = text;
            this.durationSeconds = seconds;
        }

        void activate() {
            activationBegin = Instant.now();
            activationEnd = activationBegin.plusMillis(activationTimeMillis());
        }

        long activationTimeMillis() {
            return Math.round(durationSeconds * 1000);
        }

        boolean hasExpired() {
            return Instant.now().isAfter(activationEnd);
        }
    }


    private static final Font MESSAGE_FONT = Font.font("Sans", FontWeight.BOLD, 30);
    private static final Color TEXT_COLOR_PLAIN = Color.WHEAT;

    private final VBox rootPane = new VBox();

    private final DoubleProperty alpha = new SimpleDoubleProperty(1);
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> textFill = new SimpleObjectProperty<>();
    private final ObjectProperty<TemporaryMessage> message = new SimpleObjectProperty<>();

    private final AnimationTimer fadeTimer;

    public FlashMessageView() {
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setMouseTransparent(true);
        rootPane.visibleProperty().bind(message.map(Objects::nonNull));

        final Text messageView = new Text();
        messageView.setFont(MESSAGE_FONT);
        messageView.setTextAlignment(TextAlignment.CENTER);
        messageView.fillProperty().bind(textFill);
        messageView.textProperty().bind(message.map(msg -> msg != null ? msg.text : ""));

        rootPane.getChildren().add(messageView);
        rootPane.backgroundProperty().bind(backgroundColor.map(Background::fill));

        fadeTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                fade();
            }
        };
    }

    public Pane rootPane() {
        return rootPane;
    }

    public void start() {
        fadeTimer.start();
    }

    public void stop() {
        fadeTimer.stop();
    }

    public void clear() {
        message.set(null);
        backgroundColor.set(Color.TRANSPARENT);
    }

    public void showMessage(String messageText, double seconds) {
        requireNonNull(messageText);

        TemporaryMessage tmpMessage = message.get();
        if (tmpMessage!= null && tmpMessage.text.equals(messageText)) {
            return; // already showing
        }

        tmpMessage = new TemporaryMessage(messageText, seconds);
        tmpMessage.activate();

        message.set(tmpMessage);
    }

    private void fade() {
        final TemporaryMessage tmpMessage = message.get();
        if (tmpMessage != null) {
            if (tmpMessage.hasExpired()) {
                clear();
            } else {
                double activeMillis = Duration.between(message.get().activationBegin, Instant.now()).toMillis();
                alpha.set(Math.cos(0.5 * Math.PI * activeMillis / message.get().activationTimeMillis()));
                Color bgColor = Color.rgb(0, 0, 0, 0.2 + 0.5 * alpha.get());
                backgroundColor.set(bgColor);
                textFill.set(TEXT_COLOR_PLAIN.deriveColor(0, 1, 1, alpha.get()));
            }
        }
    }
}