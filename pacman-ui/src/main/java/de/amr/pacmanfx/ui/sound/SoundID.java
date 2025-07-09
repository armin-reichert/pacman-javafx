/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

public enum SoundID {
    BONUS_BOUNCING    (".audio.bonus_bouncing",   SoundType.MEDIA_PLAYER),
    BONUS_EATEN       (".audio.bonus_eaten",      SoundType.CLIP),
    COIN_INSERTED     (".audio.credit",           SoundType.CLIP),
    EXTRA_LIFE        (".audio.extra_life",       SoundType.CLIP),
    GAME_OVER         (".audio.game_over",        SoundType.MEDIA_PLAYER),
    GAME_READY        (".audio.game_ready",       SoundType.MEDIA_PLAYER),
    GHOST_EATEN       (".audio.ghost_eaten",      SoundType.CLIP),
    GHOST_RETURNS     (".audio.ghost_returns",    SoundType.MEDIA_PLAYER),
    LEVEL_CHANGED     (".audio.sweep",            SoundType.CLIP),
    LEVEL_COMPLETE    (".audio.level_complete",   SoundType.MEDIA_PLAYER),
    PAC_MAN_DEATH     (".audio.pacman_death",     SoundType.MEDIA_PLAYER),
    PAC_MAN_MUNCHING  (".audio.pacman_munch",     SoundType.MEDIA_PLAYER),
    PAC_MAN_POWER     (".audio.pacman_power",     SoundType.MEDIA_PLAYER);

    SoundID(String keySuffix, SoundType type) {
        this.keySuffix = keySuffix;
        this.type = type;
    }

    /**
     * @return suffix of asset key without asset namespace prefix
     */
    public String keySuffix() {
        return keySuffix;
    }

    public SoundType type() {
        return type;
    }

    private final String keySuffix;
    private final SoundType type;
}
