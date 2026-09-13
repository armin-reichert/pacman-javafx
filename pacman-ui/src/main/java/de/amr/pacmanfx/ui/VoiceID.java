/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public enum VoiceID {
    AUTOPILOT_ON          ("/de/amr/pacmanfx/ui/sound/voice/autopilot-on.mp3"),
    AUTOPILOT_OFF         ("/de/amr/pacmanfx/ui/sound/voice/autopilot-off.mp3"),
    IMMUNITY_ON           ("/de/amr/pacmanfx/ui/sound/voice/immunity-on.mp3"),
    IMMUNITY_OFF          ("/de/amr/pacmanfx/ui/sound/voice/immunity-off.mp3"),
    START_HINT            ("/de/amr/pacmanfx/ui/sound/voice/press-key.mp3");

    VoiceID(String path) {
        final ResourceManager resourceManager = this::getClass;
        media = resourceManager.loadMedia(path);
    }

    public Media media() {
        return media;
    }

    private final Media media;
}
