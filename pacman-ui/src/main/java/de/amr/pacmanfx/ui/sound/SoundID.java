/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

public enum SoundID {
    BONUS_ACTIVE,
    BONUS_EATEN,
    COIN_INSERTED,
    EXTRA_LIFE,
    GAME_OVER,
    GAME_READY,
    GHOST_EATEN,
    GHOST_RETURNS,
    INTERMISSION_1,
    INTERMISSION_2,
    INTERMISSION_3,
    INTERMISSION_4,
    LEVEL_CHANGED,
    LEVEL_COMPLETE,
    PAC_MAN_DEATH,
    PAC_MAN_MUNCHING,
    PAC_MAN_POWER,
    SIREN_1,
    SIREN_2,
    SIREN_3,
    SIREN_4,
    VOICE_AUTOPILOT_ON,
    VOICE_AUTOPILOT_OFF,
    VOICE_EXPLAIN,
    VOICE_IMMUNITY_ON,
    VOICE_IMMUNITY_OFF;

    public boolean isSirenID() {
        return this == SIREN_1 || this == SIREN_2 || this == SIREN_3 || this == SIREN_4;
    }

    public boolean isVoiceID() {
        return this == VOICE_AUTOPILOT_ON
            || this == VOICE_AUTOPILOT_OFF
            || this == VOICE_EXPLAIN
            || this == VOICE_IMMUNITY_ON
            || this == VOICE_IMMUNITY_OFF;
    }
}