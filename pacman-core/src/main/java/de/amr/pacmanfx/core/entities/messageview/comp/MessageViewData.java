/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.messageview.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class MessageViewData implements GameEntityComponent {

    private String text;

    public MessageViewData() {
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void reset() {

    }
}
