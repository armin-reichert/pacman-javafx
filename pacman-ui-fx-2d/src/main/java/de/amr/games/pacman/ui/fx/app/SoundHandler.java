/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.tinylog.Logger;

import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.model.GameVariant;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.PauseTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class SoundHandler {

	private PacManGames2dUI ui;
	protected AudioClip voiceClip;
	protected final Animation voiceClipExecution = new PauseTransition();

	public SoundHandler(PacManGames2dUI ui) {
		this.ui = ui;
	}

	public AudioClip audioClip(String clipName) {
		var prefix = ui.gameVariant() == GameVariant.MS_PACMAN ? "mspacman." : "pacman.";
		return ui.theme.audioClip(prefix + clipName);
	}

	public void onSoundEvent(SoundEvent event) {
		var msPacMan = event.game.variant() == GameVariant.MS_PACMAN;
		switch (event.id) {
		case SoundEvent.BONUS_EATEN:
			audioClip("audio.bonus_eaten").play();
			break;
		case SoundEvent.CREDIT_ADDED:
			audioClip("audio.credit").play();
			break;
		case SoundEvent.EXTRA_LIFE:
			audioClip("audio.extra_life").play();
			break;
		case SoundEvent.GHOST_EATEN:
			audioClip("audio.ghost_eaten").play();
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_0:
			ensureSirenStarted(0);
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_2:
			ensureSirenStarted(1);
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_4:
			ensureSirenStarted(2);
			break;
		case SoundEvent.HUNTING_PHASE_STARTED_6:
			ensureSirenStarted(3);
			break;
		case SoundEvent.READY_TO_PLAY:
			audioClip("audio.game_ready").play();
			break;
		case SoundEvent.PACMAN_DEATH:
			audioClip("audio.pacman_death").play();
			break;
		case SoundEvent.PACMAN_FOUND_FOOD:
			// TODO this does not sound as in the original game
			ensureLoop(audioClip("audio.pacman_munch"), AudioClip.INDEFINITE);
			break;
		case SoundEvent.PACMAN_POWER_ENDS: {
			audioClip("audio.pacman_power").stop();
			event.game.level().ifPresent(level -> ensureSirenStarted(level.huntingPhase() / 2));
			break;
		}
		case SoundEvent.PACMAN_POWER_STARTS: {
			stopSirens();
			audioClip("audio.pacman_power").stop();
			audioClip("audio.pacman_power").setCycleCount(AudioClip.INDEFINITE);
			audioClip("audio.pacman_power").play();
			break;
		}
		case SoundEvent.START_INTERMISSION_1: {
			if (msPacMan) {
				audioClip("audio.intermission.1").play();
			} else {
				audioClip("audio.intermission").setCycleCount(2);
				audioClip("audio.intermission").play();
			}
			break;
		}
		case SoundEvent.START_INTERMISSION_2: {
			if (msPacMan) {
				audioClip("audio.intermission.2").play();
			} else {
				audioClip("audio.intermission").setCycleCount(1);
				audioClip("audio.intermission").play();
			}
			break;
		}
		case SoundEvent.START_INTERMISSION_3: {
			if (event.game.variant() == GameVariant.MS_PACMAN) {
				audioClip("audio.intermission.3").play();
			} else {
				audioClip("audio.intermission").setCycleCount(2);
				audioClip("audio.intermission").play();
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
		ui.theme.audioClips().forEach(AudioClip::stop);
	}

	private void startSiren(int sirenIndex) {
		stopSirens();
		var clip = audioClip("audio.siren." + (sirenIndex + 1));
		clip.setCycleCount(AudioClip.INDEFINITE);
		clip.play();
	}

	private Stream<AudioClip> sirens() {
		return IntStream.rangeClosed(1, 4).mapToObj(i -> audioClip("audio.siren." + i));
	}

	/**
	 * @param sirenIndex index of siren (0..3)
	 */
	public void ensureSirenStarted(int sirenIndex) {
		if (sirens().noneMatch(AudioClip::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	public void stopSirens() {
		sirens().forEach(AudioClip::stop);
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
		Logger.info("Voice will start in {} seconds", delaySeconds);
		voiceClip = ui.theme.audioClip(name);
		voiceClipExecution.setDelay(Duration.seconds(delaySeconds));
		voiceClipExecution.setOnFinished(e -> {
			voiceClip.play();
			Logger.info("Voice started");
		});
		voiceClipExecution.play();
	}

	public void stopVoice() {
		if (voiceClip != null && voiceClip.isPlaying()) {
			voiceClip.stop();
			Logger.info("Voice stopped");
		}
		if (voiceClipExecution.getStatus() == Status.RUNNING) {
			voiceClipExecution.stop();
			Logger.info("Scheduled voice clip stopped");
		}
	}
}