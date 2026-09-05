/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.entities3D.messageview.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.messageview.MessageView3DBuilder;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DComp;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static java.util.Objects.requireNonNull;

public class MessageView3DDisplaySystem {

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
        AnimationRegistry animationRegistry,
        LevelMessageType messageType,
        Object... args) {

        requireNonNull(messageView);
        requireNonNull(parent);
        requireNonNull(center);
        requireNonNull(font);
        requireNonNull(animationRegistry);
        requireNonNull(messageType);

        MessageView3DBuilder.createAnim3D(messageView, animationRegistry);
        final Node root = messageView.reqComp(MessageView3DComp.class).root();
        if (!parent.getChildren().contains(root)) {
            parent.getChildren().add(root);
        }

        switch (messageType) {
            case READY -> {
                configureMessageView(messageView, READY_MESSAGE_TEXT, font, READY_MESSAGE_DISPLAY_SECONDS);
                showAnimatedMessage(messageView, center);
            }

            case TEST -> {
                //TODO this is ugly
                final int levelNumber = (args.length == 0 || args[0] == null) ?  0 : Integer.parseInt(args[0].toString());
                configureMessageView(messageView, TEST_MESSAGE_TEXT.formatted(levelNumber), font, 5);
                showAnimatedMessage(messageView, center);
            }
        }
    }

    private static void configureMessageView(MessageView messageView, String messageText, Font font, float displaySeconds) {
        // 3D view must be updated after text change!
        new MessageView3DBuilder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(font)
            .text(messageText)
            .textColor(Color.YELLOW)
            .build(messageView);
    }

    private static void showAnimatedMessage(MessageView messageView, Vector2f centerPos) {
        MessageView3DAnimationSystem.showMessageViewCenteredAt(messageView, centerPos.x(), centerPos.y());
    }
}