package de.amr.basics.ui.entities.props.messageview;

import java.util.Map;

public class MessageViewTextsComp {

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
