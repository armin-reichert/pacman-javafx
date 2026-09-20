/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.props.messageview;

import de.amr.pacmanfx.core.ecs.GameEntity;

public class MessageView extends GameEntity {

    public MessageView() {
        setComp(MessageViewTypeComp.class, new MessageViewTypeComp());
        setComp(MessageViewTextsComp.class, new MessageViewTextsComp());
    }

    public MessageViewTypeComp type() {
        return reqComp(MessageViewTypeComp.class);
    }

    public MessageViewTextsComp texts() {
        return reqComp(MessageViewTextsComp.class);
    }
}
