package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.ui.sound.SirenPlayer;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.net.URL;

public class SoundTest implements ResourceManager {

    static final int SINGLE_TEST_DURATION_SECONDS = 5;

    static final String SOUND_PATH = "/de/amr/pacmanfx/arcade/ms_pacman/sound/";

    @Override
    public Class<?> resourceRootClass() {
        return SoundTest.class;
    }

    static void main() {
        Platform.startup(() -> new SoundTest().runAllTests(1.0, 1.5));
    }

    private final Timeline timeline = new Timeline();
    private final SirenPlayer sirenPlayer;

    public SoundTest() {
        sirenPlayer = new SirenPlayer(sirenURL(1), sirenURL(2), sirenURL(3), sirenURL(4));
    }

    private URL sirenURL(int number) {
        return url(SOUND_PATH + "GhostNoise%d.wav".formatted(number));
    }

    private void runAllTests(double volume, double rate) {
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(SINGLE_TEST_DURATION_SECONDS), _ -> sirenPlayer.ensureSirenPlaying(1, volume)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2* SINGLE_TEST_DURATION_SECONDS), _ -> {
            sirenPlayer.stopCurrentSiren();
            sirenPlayer.ensureSirenPlaying(2, volume, rate);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3* SINGLE_TEST_DURATION_SECONDS), _ -> {
            sirenPlayer.stopCurrentSiren();
            sirenPlayer.ensureSirenPlaying(3, volume, rate);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(4* SINGLE_TEST_DURATION_SECONDS), _ -> {
            sirenPlayer.stopCurrentSiren();
            sirenPlayer.ensureSirenPlaying(4, volume, rate);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5* SINGLE_TEST_DURATION_SECONDS), _ -> {
            sirenPlayer.stopCurrentSiren();
            Platform.exit();
        }));
        timeline.play();
    }
}