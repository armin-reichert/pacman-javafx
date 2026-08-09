/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.entities3D.messageview.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.messageview.MessageView3DBuilder;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DComp;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static java.util.Objects.requireNonNull;

public class MessageView3DDisplaySystem {

    public enum MessageType { READY, TEST }

    /** Standard "READY!" message shown at level start */
    public static final String READY_MESSAGE_TEXT = "READY!";

    /** Test mode overlay message format */
    public static final String TEST_MESSAGE_TEXT = "LEVEL %d (TEST)";

    /** Default display duration for READY! message */
    public static final float READY_MESSAGE_DISPLAY_SECONDS = 2.5f;

    public static void showMessage(
        MessageView messageView,
        Group parent,
        Vector2f center,
        Font font,
        AnimationRegistry registry,
        MessageType messageType,
        Object... args) {

        requireNonNull(messageView);
        requireNonNull(center);
        requireNonNull(registry);
        requireNonNull(messageType);

        final MessageView3DComp view3D = MessageView3DBuilder.ensureView3DExists(messageView);
        if (!parent.getChildren().contains(view3D.root())) {
            parent.getChildren().add(view3D.root());
        }

        messageView.requireComp(MessageView3DAnimationComp.class).setRegistry(registry);

        switch (messageType) {

            case READY -> showAnimatedMessage(
                messageView,
                center,
                READY_MESSAGE_TEXT,
                font,
                READY_MESSAGE_DISPLAY_SECONDS);

            case TEST -> {
                //TODO this is ugly
                final int levelNumber = (args.length == 0 || args[0] == null) ?  0 : Integer.parseInt(args[0].toString());
                showAnimatedMessage(
                    messageView,
                    center,
                    TEST_MESSAGE_TEXT.formatted(levelNumber),
                    font,
                    5);
            }
        }
    }

    private static void showAnimatedMessage(MessageView messageView, Vector2f centerPos, String messageText, Font font, float displaySeconds) {
        messageView.data().setText(messageText);

        // view3D must be updated after text change!
        new MessageView3DBuilder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(font)
            .text(messageText)
            .textColor(Color.YELLOW)
            .build(messageView);

        final var anim3D = MessageView3DBuilder.ensureAnim3DExists(messageView);
        MessageView3DAnimationSystem.showMessageViewCenteredAt(anim3D, messageView, centerPos.x(), centerPos.y());
    }
}