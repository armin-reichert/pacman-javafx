/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class MessageView extends ImageView implements Disposable {

    public static class Builder {
        private Color borderColor = Color.BLUE;
        private Color backgroundColor = Color.grayRgb(88);
        private float displaySeconds = 2;
        private Font font = Font.font("SansSerif", FontWeight.BOLD, 10);
        private Color textColor = Color.WHITE;
        private String text = "Hello, World!";

        public Builder backgroundColor(Color color) {
            backgroundColor = requireNonNull(color);
            return this;
        }

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
            return new MessageView(animationRegistry, this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static final int MARGIN = 3;
    private static final int QUALITY = 3;

    private static Image createImage(double width, double height, String text, Font font,
                                     Color backgroundColor, Color textColor, Color borderColor) {
        var canvas = new Canvas(width * QUALITY, height * QUALITY);
        double canvasFontSize = font.getSize() * QUALITY;
        var g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(backgroundColor);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(borderColor);
        g.setLineWidth(5);
        g.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFont(Ufx.deriveFont(font, canvasFontSize));
        g.setFill(textColor);
        g.fillText(text, 0.5 * QUALITY * MARGIN, 0.8 * QUALITY * height);
        return canvas.snapshot(null, null);
    }

    private class MoveInOutAnimation extends RegisteredAnimation {

        public MoveInOutAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Message_Movement");
        }

        @Override
        protected Animation createAnimationFX() {
            double hiddenZ = hiddenZPosition();
            double visibleZ = -(hiddenZ + 2);
            var moveUp = new TranslateTransition(Duration.seconds(1), MessageView.this);
            moveUp.setToZ(visibleZ);
            var moveDown = new TranslateTransition(Duration.seconds(1), MessageView.this);
            moveDown.setToZ(hiddenZ);
            var movement = new SequentialTransition(
                moveUp,
                new PauseTransition(Duration.seconds(displaySeconds)),
                moveDown
            );
            movement.setOnFinished(e -> setVisible(false));
            return movement;
        }
    }

    private double hiddenZPosition() {
        return 0.5 * getBoundsInLocal().getHeight();
    }

    private RegisteredAnimation moveInOutAnimation;
    private final float displaySeconds;

    private MessageView(AnimationRegistry animationRegistry, Builder builder) {
        Text dummy = new Text(builder.text);
        dummy.setFont(builder.font);
        double width = dummy.getLayoutBounds().getWidth() + MARGIN;
        double height = dummy.getLayoutBounds().getHeight() + MARGIN;
        Image image = createImage(width, height, builder.text, builder.font,
            builder.backgroundColor, builder.textColor, builder.borderColor);
        setImage(image);
        setFitWidth(width);
        setFitHeight(height);
        setRotationAxis(Rotate.X_AXIS);
        setRotate(90);
        displaySeconds = builder.displaySeconds;
        moveInOutAnimation = new MoveInOutAnimation(animationRegistry);
    }

    @Override
    public void dispose() {
        if (moveInOutAnimation != null) {
            moveInOutAnimation.dispose();
            moveInOutAnimation = null;
        }
    }

    public void showCenteredAt(double centerX, double centerY) {
        setTranslateX(centerX - 0.5 * getFitWidth());
        setTranslateY(centerY);
        setTranslateZ(hiddenZPosition());
        moveInOutAnimation.playFromStart();
    }
}