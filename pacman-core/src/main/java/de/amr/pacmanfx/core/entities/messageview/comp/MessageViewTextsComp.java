package de.amr.pacmanfx.core.entities.messageview.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.level.MessageType;

import java.util.Map;

public class MessageViewTextsComp implements GameEntityComp {

    private Map<MessageType, String> texts;

    public MessageViewTextsComp() {
    }

    public Map<MessageType, String> texts() {
        return texts;
    }

    public void setTexts(Map<MessageType, String> texts) {
        this.texts = texts;
    }
}
