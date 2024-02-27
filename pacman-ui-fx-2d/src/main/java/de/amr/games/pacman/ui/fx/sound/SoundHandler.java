/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.sound;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
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

/**
 * @author Armin Reichert
 */
public class SoundHandler implements GameEventListener {

    private final Theme theme;
    protected AudioClip voiceClip;
    protected final Animation voiceClipExecution = new PauseTransition();

    public SoundHandler(Theme theme) {
        this.theme = theme;
    }

    public AudioClip audioClip(GameVariant gameVariant, String clipName) {
        var prefix = gameVariant == GameVariant.MS_PACMAN ? "mspacman." : "pacman.";
        return theme.audioClip(prefix + clipName);
    }

    private boolean isPlayingLevel(GameEvent e) {
        return !e.game.level().map(GameLevel::isDemoLevel).orElse(true);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        if (isPlayingLevel(event)) {
            audioClip(event.game.variant(), "audio.bonus_eaten").play();
        }
    }

    @Override
    public void onCreditAdded(GameEvent event) {
        audioClip(event.game.variant(), "audio.credit").play();
    }

    @Override
    public void onExtraLifeWon(GameEvent event) {
        if (isPlayingLevel(event)) {
            audioClip(event.game.variant(), "audio.extra_life").play();
        }
    }

    @Override
    public void onGhostEaten(GameEvent event) {
        if (isPlayingLevel(event)) {
            audioClip(event.game.variant(), "audio.ghost_eaten").play();
        }
    }

    @Override
    public void onHuntingPhaseStarted(GameEvent event) {
        event.game.level().ifPresent(level -> {
            if (!level.isDemoLevel()) {
                level.scatterPhase().ifPresent(phase -> ensureSirenStarted(event.game.variant(), phase));
            }
        });
    }

    @Override
    public void onIntermissionStarted(GameEvent event) {
        int intermissionNumber = 0;
        if (GameController.it().state() == GameState.INTERMISSION_TEST) {
            intermissionNumber = GameController.it().intermissionTestNumber;
        } else if (event.game.level().isPresent()) {
            intermissionNumber = event.game.level().get().intermissionNumber();
        }
        if (intermissionNumber > 0) {
            GameVariant variant = event.game.variant();
            if (variant == GameVariant.MS_PACMAN) {
                audioClip(variant, "audio.intermission." + intermissionNumber).play();
            } else {
                var clip = audioClip(variant, "audio.intermission");
                int cycleCount = intermissionNumber == 1 || intermissionNumber == 3 ? 2 : 1;
                clip.setCycleCount(cycleCount);
                clip.play();
            }
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        event.game.level().ifPresent(level -> {
            if (!level.isDemoLevel() && level.number() == 1) {
                audioClip(event.game.variant(), "audio.game_ready").play();
            }
        });
    }

    @Override
    public void onPacDied(GameEvent event) {
        if (isPlayingLevel(event)) {
            audioClip(event.game.variant(), "audio.pacman_death").play();
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        if (isPlayingLevel(event)) {
            //TODO (fixme) this does not sound 100% as in the original game
            ensureLoop(audioClip(event.game.variant(), "audio.pacman_munch"), AudioClip.INDEFINITE);
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        if (isPlayingLevel(event)) {
            stopSirens(event.game.variant());
            var clip = audioClip(event.game.variant(), "audio.pacman_power");
            clip.stop();
            clip.setCycleCount(AudioClip.INDEFINITE);
            clip.play();
        }
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        event.game.level().ifPresent(level -> {
            if (!level.isDemoLevel()) {
                audioClip(event.game.variant(), "audio.pacman_power").stop();
                ensureSirenStarted(event.game.variant(), level.huntingPhase() / 2);
            }
        });
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