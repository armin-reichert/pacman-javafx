/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.sound;


import de.amr.basics.Named;

import java.net.URL;

public record SoundEntry(Class<? extends DefaultSoundManager.SoundResource> type, Named id, URL url) {

    public static SoundEntry audioClip(Named id, URL url) {
        return new SoundEntry(DefaultSoundManager.AudioClipResource.class, id, url);
    }

    public static SoundEntry mediaPlayer(Named id, URL url) {
        return new SoundEntry(DefaultSoundManager.MediaPlayerResource.class, id, url);
    }
}
