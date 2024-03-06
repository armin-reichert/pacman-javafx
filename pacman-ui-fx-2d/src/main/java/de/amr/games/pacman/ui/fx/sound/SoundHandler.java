/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.sound;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.PauseTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class SoundHandler implements GameEventListener {

    private final Theme theme;
    private AudioClip voiceClip;
    private final Animation voiceClipExecution = new PauseTransition();

    public SoundHandler(Theme theme) {
        this.theme = theme;
    }

    public AudioClip audioClip(GameVariant gameVariant, String clipName) {
        checkNotNull(gameVariant);
        checkNotNull(clipName);
        return switch (gameVariant) {
            case MS_PACMAN -> theme.audioClip("mspacman." + clipName);
            case PACMAN -> theme.audioClip("pacman." + clipName);
        };
    }

    private GameLevel getLevel(GameEvent event) {
        return event.game.level().orElse(null);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            audioClip(event.game.variant(), "audio.bonus_eaten").play();
        }
    }

    @Override
    public void onCreditAdded(GameEvent event) {
        audioClip(event.game.variant(), "audio.credit").play();
    }

    @Override
    public void onExtraLifeWon(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            audioClip(event.game.variant(), "audio.extra_life").play();
        }
    }


    @Override
    public void onGameStateChange(GameStateChangeEvent stateChangeEvent) {
        switch (stateChangeEvent.newState) {
            case READY, PACMAN_DYING, LEVEL_COMPLETE -> stopAllSounds();
            case GAME_OVER -> {
                stopAllSounds();
                audioClip((stateChangeEvent.game.variant()), "audio.game_over").play();
            }
            default -> {}
        };
    }

    @Override
    public void onGhostEaten(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            audioClip(event.game.variant(), "audio.ghost_eaten").play();
        }
    }

    @Override
    public void onHuntingPhaseStarted(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            level.scatterPhase().ifPresent(phase -> ensureSirenStarted(event.game.variant(), phase));
        }
    }

    @Override
    public void onIntermissionStarted(GameEvent event) {
        int intermissionNumber = 0; // 0=undefined
        if (GameController.it().state() == GameState.INTERMISSION_TEST) {
            intermissionNumber = GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber");
        } else {
            GameLevel level = getLevel(event);
            if (level != null) {
                intermissionNumber = level.data().intermissionNumber();
            }
        }
        if (intermissionNumber != 0) {
            GameVariant variant = event.game.variant();
            switch (variant) {
                case MS_PACMAN -> audioClip(variant, "audio.intermission." + intermissionNumber).play();
                case PACMAN -> {
                    var clip = audioClip(variant, "audio.intermission");
                    clip.setCycleCount(intermissionNumber == 1 || intermissionNumber == 3 ? 2 : 1);
                    clip.play();
                }
            }
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel() && level.number() == 1) {
            audioClip(event.game.variant(), "audio.game_ready").play();
            Logger.info("Play READY sound");
        }
    }

    @Override
    public void onPacDied(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            audioClip(event.game.variant(), "audio.pacman_death").play();
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            //TODO (fixme) this does not sound 100% as in the original game
            ensureLoop(audioClip(event.game.variant(), "audio.pacman_munch"), AudioClip.INDEFINITE);
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            stopSirens(event.game.variant());
            var clip = audioClip(event.game.variant(), "audio.pacman_power");
            clip.stop();
            clip.setCycleCount(AudioClip.INDEFINITE);
            clip.play();
        }
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        var level = getLevel(event);
        if (level != null && !level.isDemoLevel()) {
            audioClip(event.game.variant(), "audio.pacman_power").stop();
            ensureSirenStarted(event.game.variant(), level.huntingPhaseIndex() / 2);
        }
    }

    @Override
    public void onStopAllSounds(GameEvent e) {
        stopAllSounds();
    }

    public void stopAllSounds() {
        theme.audioClips().filter(clip -> clip != voiceClip).forEach(AudioClip::stop);
        Logger.trace("All sounds stopped");
    }

    private void startSiren(GameVariant gameVariant, int sirenIndex) {
        stopSirens(gameVariant);
        var clip = audioClip(gameVariant, "audio.siren." + (sirenIndex + 1));
        clip.setCycleCount(AudioClip.INDEFINITE);
        clip.play();
    }

    private Stream<AudioClip> sirens(GameVariant gameVariant) {
        return IntStream.rangeClosed(1, 4).mapToObj(i -> audioClip(gameVariant, "audio.siren." + i));
    }

    /**
     * @param sirenIndex index of siren (0..3)
     */
    public void ensureSirenStarted(GameVariant gameVariant, int sirenIndex) {
        if (sirens(gameVariant).noneMatch(AudioClip::isPlaying)) {
            startSiren(gameVariant, sirenIndex);
        }
    }

    public void stopSirens(GameVariant gameVariant) {
        sirens(gameVariant).forEach(AudioClip::stop);
    }

    public void ensureLoop(AudioClip clip, int repetitions) {
        if (!clip.isPlaying()) {
            clip.setCycleCount(repetitions);
            clip.play();
        }
    }

    public void ensureLoopEndless(AudioClip clip) {
        ensureLoop(clip, AudioClip.INDEFINITE);
    }

    public void playVoice(String name) {
        playVoice(name, 0);
    }

    public void playVoice(String name, double delaySeconds) {
        if (voiceClip != null && voiceClip.isPlaying()) {
            return; // don't interrupt voice
        }
        Logger.trace("Voice will start in {} seconds", delaySeconds);
        voiceClip = theme.audioClip(name);
        voiceClipExecution.setDelay(Duration.seconds(delaySeconds));
        voiceClipExecution.setOnFinished(e -> {
            voiceClip.play();
            Logger.trace("Voice started");
        });
        voiceClipExecution.play();
    }

    public void stopVoice() {
        if (voiceClip != null && voiceClip.isPlaying()) {
            voiceClip.stop();
            Logger.trace("Voice stopped");
        }
        if (voiceClipExecution.getStatus() == Status.RUNNING) {
            voiceClipExecution.stop();
            Logger.trace("Scheduled voice clip stopped");
        }
    }
}