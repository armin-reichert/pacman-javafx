package de.amr.basics.ui.entities.props.messageview;

import de.amr.basics.ui.ecs.GameEntityComp;

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
