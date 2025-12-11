import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

public class SoundTest implements ResourceManager {

    static final String SOUND_PATH = "/de/amr/pacmanfx/arcade/ms_pacman/sound/";

    @Override
    public Class<?> resourceRootClass() {
        return SoundTest.class;
    }

    public static void main(String[] args) {
        SoundTest test = new SoundTest();
        Platform.startup(test::runAllTests);
    }

    static final int TEST_DURATION = 5;

    private final Timeline timeline = new Timeline();
    private final AudioClip[] players = new AudioClip[4];

    private void runAllTests() {
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(TEST_DURATION),  e -> testGhostNoise(1)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2*TEST_DURATION),  e -> {
            stopGhostNoise(1);
            testGhostNoise(2);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3*TEST_DURATION),  e -> {
            stopGhostNoise(2);
            testGhostNoise(3);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(4*TEST_DURATION),  e -> {
            stopGhostNoise(3);
            testGhostNoise(4);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5*TEST_DURATION),  e -> {
            stopGhostNoise(4);
            Platform.exit();
        }));
        timeline.play();
    }

    private void stopGhostNoise(int number) {
        players[number-1].stop();
    }

    private void testGhostNoise(int number) {
        String url = url(SOUND_PATH + "GhostNoise%d.wav".formatted(number)).toExternalForm();
        int i = number - 1;
        players[i] = new AudioClip(url);
        players[i].setCycleCount(10);
        players[i].play();
    }
}