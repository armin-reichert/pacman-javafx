/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.system.MessageViewAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.MessageViewBuilder;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Manages temporary overlay messages in the 3D game level (e.g. "READY!", test mode overlays).
 * <p>
 * This class creates, positions, displays, and disposes animated message views.
 * It is owned by {@link GameLevel3D} and disposed together with the level.
 * <p>
 * Messages are positioned in world coordinates (centered at the given point).
 * The container group is provided at construction time.
 *
 * @see GameLevel3D
 * @see MessageView3DComp
 * @see DisposableGraphicsObject
 */
public class MessageManager3D implements DisposableGraphicsObject {

    public enum MessageType { READY, TEST }

    /** Standard "READY!" message shown at level start */
    public static final String READY_MESSAGE_TEXT = "READY!";

    /** Test mode overlay message format */
    public static final String TEST_MESSAGE_TEXT = "LEVEL %d (TEST)";

    /** Default display duration for READY! message */
    public static final float READY_MESSAGE_DISPLAY_SECONDS = 2.5f;

    private final Group messageParent;

    private MessageView messageView;

    private final Map<MessageType, Vector2f> messageCenters = new EnumMap<>(MessageType.class);

    /**
     * Creates a new message manager for the given animation registry and container group.
     *
     * @param messageParent  the group to which messages are added/removed
     */
    public MessageManager3D(Group messageParent) {
        this.messageParent = requireNonNull(messageParent);
    }

    public MessageView messageView() {
        return messageView;
    }

    /**
     * Sets the world position where the "READY!" message should appear (centered).
     *
     * @param messageType the message type
     * @param center center position in world coordinates
     */
    public void setMessageCenter(MessageType messageType, Vector2f center) {
        requireNonNull(messageType);
        requireNonNull(center);
        messageCenters.put(messageType, center);
    }

    /**
     * Releases any currently displayed message view.
     * <p>
     * Called automatically when the level is disposed.
     */
    @Override
    public void dispose() {
        if (messageView != null) {
            if (messageView.hasComp(MessageView3DComp.class)) {
                messageView.requireComp(MessageView3DComp.class).dispose();
            }
            messageView = null;
        }
    }

    public void showMessage(AnimationRegistry registry, MessageType messageType, Object... args) {
        switch (messageType) {

            case READY -> showAnimatedMessage(
                registry,
                messageCenters.get(MessageType.READY),
                READY_MESSAGE_TEXT,
                READY_MESSAGE_DISPLAY_SECONDS);

            case TEST -> {
                //TODO this is ugly
                final int levelNumber = (args.length == 0 || args[0] == null) ?  0 : Integer.parseInt(args[0].toString());
                showAnimatedMessage(
                    registry,
                    messageCenters.get(MessageType.TEST),
                    TEST_MESSAGE_TEXT.formatted(levelNumber),
                    5);
            }
        }
    }

    /**
     * Shows a temporary animated message at the specified world coordinates.
     *
     * @param centerPos        center position in world coordinates
     * @param messageText      message content
     * @param displaySeconds   duration before fade-out
     */
    public void showAnimatedMessage(AnimationRegistry registry, Vector2f centerPos, String messageText, float displaySeconds) {
        if (messageView != null) {
            messageView.dispose();
            final Node root = messageView.requireComp(MessageView3DComp.class).root();
            messageParent.getChildren().remove(root);
        }

        messageView = new MessageViewBuilder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(GlobalAssets.PredefinedFont.ARCADE6.font())
            .text(messageText)
            .textColor(Color.YELLOW)
            .build();

        if (!messageView.hasComp(MessageView3DComp.class)) {
            messageView.setComp(MessageView3DComp.class, new MessageView3DComp());
        }
        final MessageView3DComp view3D = messageView.requireComp(MessageView3DComp.class);
        messageParent.getChildren().add(view3D.root());

        if (!messageView.hasComp(MessageView3DAnimationComp.class)) {
            messageView.setComp(MessageView3DAnimationComp.class, new MessageView3DAnimationComp(registry, view3D));
        }
        final var anim3D = messageView.requireComp(MessageView3DAnimationComp.class);
        MessageViewAnimationSystem.showMessageViewCenteredAt(anim3D, messageView, centerPos.x(), centerPos.y());
    }
}