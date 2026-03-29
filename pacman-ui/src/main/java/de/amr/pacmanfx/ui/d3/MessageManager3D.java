/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.widgets.MessageView;
import javafx.scene.Group;
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
 * @see MessageView
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

    private final AnimationRegistry animationRegistry;
    private final Group messageParent;
    private MessageView messageView;

    private final Map<MessageType, Vector2f> messageCenters = new EnumMap<>(MessageType.class);

    /**
     * Creates a new message manager for the given animation registry and container group.
     *
     * @param animationRegistry registry for message animations
     * @param messageParent  the group to which messages are added/removed
     */
    public MessageManager3D(AnimationRegistry animationRegistry, Group messageParent) {
        this.animationRegistry = requireNonNull(animationRegistry);
        this.messageParent = requireNonNull(messageParent);
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
            messageView.dispose();
            messageView = null;
        }
    }

    //TODO this is ugly
    public void showMessage(MessageType messageType, Object... args) {
        switch (messageType) {
            case READY ->  showAnimatedMessage(messageCenters.get(MessageType.READY), READY_MESSAGE_TEXT, READY_MESSAGE_DISPLAY_SECONDS);
            case TEST -> showAnimatedMessage(messageCenters.get(MessageType.TEST), TEST_MESSAGE_TEXT.formatted((int)args[0]), 5);
        }
    }

    /**
     * Hides the currently visible message without disposing it.
     */
    public void hideMessage() {
        if (messageView != null) {
            messageView.setVisible(false);
        }
    }

    /**
     * Shows a temporary animated message at the specified world coordinates.
     *
     * @param centerPos        center position in world coordinates
     * @param messageText      message content
     * @param displaySeconds   duration before fade-out
     */
    public void showAnimatedMessage(Vector2f centerPos, String messageText, float displaySeconds) {
        if (messageView != null) {
            messageView.dispose();
            messageParent.getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(GameUI_Resources.FONT_ARCADE_6)
            .text(messageText)
            .textColor(Color.YELLOW)
            .build(animationRegistry);

        messageView.showCenteredAt(centerPos.x(), centerPos.y());
        messageParent.getChildren().add(messageView);
    }
}