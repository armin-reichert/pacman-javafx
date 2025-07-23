/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Message view.
 */
public class MessageView extends ImageView implements Disposable {

    public static class Builder {
        private Font font;
        private Color borderColor;
        private float displaySeconds;
        private Color textColor;
        private String text;

        public Builder borderColor(Color color) {
            borderColor = requireNonNull(color);
            return this;
        }

        public Builder displaySeconds(float sec) {
            displaySeconds = sec;
            return this;
        }

        public Builder font(Font font) {
            this.font = requireNonNull(font);
            return this;
        }

        public Builder textColor(Color color) {
            this.textColor = requireNonNull(color);
            return this;
        }

        public Builder text(String text) {
            this.text = requireNonNull(text);
            return this;
        }

        public MessageView build(AnimationManager animationManager) {
            MessageView message = new MessageView(animationManager);
            double width = text.length() * font.getSize() + MARGIN;
            double height = font.getSize() + MARGIN;
            message.setDisplaySeconds(displaySeconds);
            message.setImage(createImage(width, height, text, font, textColor, borderColor));
            message.setFitWidth(width);
            message.setFitHeight(height);
            message.setRotationAxis(Rotate.X_AXIS);
            message.setRotate(90);
            return message;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static final int MARGIN = 3;
    private static final int QUALITY = 3;

    private static Image createImage(double width, double height, String text, Font font, Color textColor, Color borderColor) {
        var canvas = new Canvas(width * QUALITY, height * QUALITY);
        double canvasFontSize = font.getSize() * QUALITY;
        var g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(borderColor);
        g.setLineWidth(5);
        g.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFont(Font.font(font.getFamily(), canvasFontSize));
        g.setFill(textColor);
        g.fillText(text, 0.5 * QUALITY * MARGIN, 0.8 * QUALITY * height);
        return canvas.snapshot(null, null);
    }

    private AnimationManager animationManager;
    private ManagedAnimation movementAnimation;
    private float displaySeconds = 2;

    private MessageView(AnimationManager animationManager) {
        this.animationManager = requireNonNull(animationManager);
        movementAnimation = new ManagedAnimation(animationManager, "Message_Movement") {
            @Override
            protected Animation createAnimation() {
                double halfHeight = 0.5 * getBoundsInLocal().getHeight();

                var moveUpAnimation = new TranslateTransition(Duration.seconds(1), MessageView.this);
                moveUpAnimation.setToZ(-(halfHeight + 2));

                var moveDownAnimation = new TranslateTransition(Duration.seconds(1), MessageView.this);
                moveDownAnimation.setToZ(halfHeight);
                moveDownAnimation.setOnFinished(e -> setVisible(false));

                return new SequentialTransition(
                    moveUpAnimation,
                    new PauseTransition(Duration.seconds(displaySeconds)),
                    moveDownAnimation
                );
            }
        };
    }

    @Override
    public void dispose() {
        if (movementAnimation != null) {
            movementAnimation.stop();
            movementAnimation.dispose();
            movementAnimation = null;
        }
        animationManager = null;
    }

    public void setDisplaySeconds(float sec) {
        displaySeconds = sec;
    }

    public float displaySeconds() {
        return displaySeconds;
    }

    public ManagedAnimation movementAnimation() {
        return movementAnimation;
    }
}