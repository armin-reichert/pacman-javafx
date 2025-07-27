/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
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

        public MessageView build(AnimationRegistry animationRegistry) {
            MessageView message = new MessageView(animationRegistry);
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

    private static class MovementAnimation extends ManagedAnimation {

        private MessageView messageView;

        public MovementAnimation(AnimationRegistry animationRegistry, MessageView messageView) {
            super(animationRegistry, "Message_Movement");
            this.messageView = messageView;
        }

        @Override
        protected Animation createAnimationFX() {
            double hiddenZ = messageView.hiddenZPosition();
            double visibleZ = -(hiddenZ + 2);
            var moveUp = new TranslateTransition(Duration.seconds(1), messageView);
            moveUp.setToZ(visibleZ);
            var moveDown = new TranslateTransition(Duration.seconds(1), messageView);
            moveDown.setToZ(hiddenZ);
            var movement = new SequentialTransition(
                moveUp,
                new PauseTransition(Duration.seconds(messageView.displaySeconds())),
                moveDown
            );
            movement.setOnFinished(e -> messageView.setVisible(false));
            return movement;
        }
    }

    private double hiddenZPosition() {
        return 0.5 * getBoundsInLocal().getHeight();
    }

    private ManagedAnimation movementAnimation;
    private float displaySeconds = 2;

    private MessageView(AnimationRegistry animationRegistry) {
        movementAnimation = new MovementAnimation(animationRegistry, this);
    }

    @Override
    public void dispose() {
        if (movementAnimation != null) {
            movementAnimation.stop();
            movementAnimation.dispose();
            movementAnimation = null;
        }
    }

    public void setDisplaySeconds(float sec) {
        displaySeconds = sec;
    }

    public float displaySeconds() {
        return displaySeconds;
    }

    public void showCenteredAt(double centerX, double centerY) {
        setTranslateX(centerX - 0.5 * getFitWidth());
        setTranslateY(centerY);
        setTranslateZ(hiddenZPosition());
        movementAnimation.playFromStart();
    }
}