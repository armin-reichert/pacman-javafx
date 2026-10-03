/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.messageview;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;

public class MessageView extends GameEntity {

    public MessageView() {
        setComponent(MovementComp.class, new MovementComp());
        setComponent(MessageViewTypeComp.class, new MessageViewTypeComp());
        setComponent(MessageViewTextsComp.class, new MessageViewTextsComp());
    }

    public MovementComp movement() {
        return assertComponent(MovementComp.class);
    }

    public MessageViewTypeComp type() {
        return assertComponent(MessageViewTypeComp.class);
    }

    public MessageViewTextsComp texts() {
        return assertComponent(MessageViewTextsComp.class);
    }
}
