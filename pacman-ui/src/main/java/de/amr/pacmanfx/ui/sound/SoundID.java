/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

public enum SoundID {
    BONUS_BOUNCING    ("bonus_bouncing",   SoundType.MEDIA_PLAYER),
    BONUS_EATEN       ("bonus_eaten",      SoundType.CLIP),
    COIN_INSERTED     ("credit",           SoundType.CLIP),
    EXTRA_LIFE        ("extra_life",       SoundType.CLIP),
    GAME_OVER         ("game_over",        SoundType.MEDIA_PLAYER),
    GAME_READY        ("game_ready",       SoundType.MEDIA_PLAYER),
    GHOST_EATEN       ("ghost_eaten",      SoundType.CLIP),
    GHOST_RETURNS     ("ghost_returns",    SoundType.MEDIA_PLAYER),
    LEVEL_CHANGED     ("sweep",            SoundType.CLIP),
    LEVEL_COMPLETE    ("level_complete",   SoundType.MEDIA_PLAYER),
    PAC_MAN_DEATH     ("pacman_death",     SoundType.MEDIA_PLAYER),
    PAC_MAN_MUNCHING  ("pacman_munch",     SoundType.MEDIA_PLAYER),
    PAC_MAN_POWER     ("pacman_power",     SoundType.MEDIA_PLAYER);

    SoundID(String key, SoundType type) {
        this.key = key;
        this.type = type;
    }

    public String key() {
        return key;
    }

    public SoundType type() {
        return type;
    }

    private final String key;
    private final SoundType type;
}
