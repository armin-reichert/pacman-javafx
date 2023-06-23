/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.event.SoundEvent;
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
public class SoundHandler {

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

	public void onSoundEvent(SoundEvent event) {
		var gameVariant = event.game.variant();
		switch (event.id) {
		case SoundEvent.BONUS_EATEN:
			audioClip(gameVariant, "audio.bonus_eaten").play();
			break;
		case SoundEvent.CREDIT_ADDED:
			audioClip(gameVariant, "audio.credit").play();
			break;
		case SoundEvent.EXTRA_LIFE:
			audioClip(gameVariant, "audio.extra_life").play();
			break;
		case SoundEvent.GHOST_EATEN:
			audioClip(gameVariant, "audio.ghost_eaten").play();
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_0:
			ensureSirenStarted(gameVariant, 0);
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_2:
			ensureSirenStarted(gameVariant, 1);
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_4:
			ensureSirenStarted(gameVariant, 2);
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_6:
			ensureSirenStarted(gameVariant, 3);
			break;
		case SoundEvent.READY_TO_PLAY:
			audioClip(gameVariant, "audio.game_ready").play();
			break;
		case SoundEvent.PACMAN_DEATH:
			audioClip(gameVariant, "audio.pacman_death").play();
			break;
		case SoundEvent.PACMAN_FOUND_FOOD:
			// TODO this does not sound as in the original game
			ensureLoop(audioClip(gameVariant, "audio.pacman_munch"), AudioClip.INDEFINITE);
			break;
		case SoundEvent.PACMAN_POWER_ENDS: {
			audioClip(gameVariant, "audio.pacman_power").stop();
			event.game.level().ifPresent(level -> ensureSirenStarted(gameVariant, level.huntingPhase() / 2));
			break;
		}
		case SoundEvent.PACMAN_POWER_STARTS: {
			stopSirens(gameVariant);
			audioClip(gameVariant, "audio.pacman_power").stop();
			audioClip(gameVariant, "audio.pacman_power").setCycleCount(AudioClip.INDEFINITE);
			audioClip(gameVariant, "audio.pacman_power").play();
			break;
		}
		case SoundEvent.START_INTERMISSION_1: {
			if (gameVariant == GameVariant.MS_PACMAN) {
				audioClip(gameVariant, "audio.intermission.1").play();
			} else {
				audioClip(gameVariant, "audio.intermission").setCycleCount(2);
				audioClip(gameVariant, "audio.intermission").play();
			}
			break;
		}
		case SoundEvent.START_INTERMISSION_2: {
			if (gameVariant == GameVariant.MS_PACMAN) {
				audioClip(gameVariant, "audio.intermission.2").play();
			} else {
				audioClip(gameVariant, "audio.intermission").setCycleCount(1);
				audioClip(gameVariant, "audio.intermission").play();
			}
			break;
		}
		case SoundEvent.START_INTERMISSION_3: {
			if (gameVariant == GameVariant.MS_PACMAN) {
				audioClip(gameVariant, "audio.intermission.3").play();
			} else {
				audioClip(gameVariant, "audio.intermission").setCycleCount(2);
				audioClip(gameVariant, "audio.intermission").play();
			}
			break;
		}
		case SoundEvent.STOP_ALL_SOUNDS:
			stopAllSounds();
			break;
		default: {
			// ignore
		}
		}
	}

	public void stopAllSounds() {
		theme.audioClips().filter(clip -> clip != voiceClip).forEach(AudioClip::stop);
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