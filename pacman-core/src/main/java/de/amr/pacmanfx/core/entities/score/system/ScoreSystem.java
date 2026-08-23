package de.amr.pacmanfx.core.entities.score.system;

import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.comp.ScoreDataComp;
import de.amr.pacmanfx.core.entities.score.comp.ScorePersistencyComp;
import org.tinylog.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class ScoreSystem {

    /**
     * High score file for game variant "YYZ" is stored as "highscore-yyz.xml" inside user home directory.
     *
     * @param variantName name of the game variant e.g. "MS_PACMAN"
     * @return high score file name for this game variant
     */
    public static File highScoreFile(String variantName) {
        requireNonNull(variantName);
        final String fileName = "highscore-%s.xml".formatted(variantName.toLowerCase());
        return new File(GameConstants.USER_HOME_DIR, fileName);
    }

    public static void setPoints(Score score, int points) {
        score.data().setPoints(points);
    }

    public static void setLevelNumber(Score score, int levelNumber) {
        score.data().setLevelNumber(levelNumber);
    }

    public static void setDate(Score score, LocalDate date) {
        score.data().setDate(date);
    }

    public static void load(Score score) throws IOException {
        final ScorePersistencyComp persistency = score.reqComp(ScorePersistencyComp.class);

        if (!persistency.file().exists()) {
            save(score); // create default file
        }

        final var properties = new Properties();
        try (var inputStream = new BufferedInputStream(new FileInputStream(persistency.file()))) {
            properties.loadFromXML(inputStream);
        }

        final ScoreDataComp data = score.data();
        try {
            data.setPoints(Integer.parseInt(properties.getProperty(ScorePersistencyComp.ATTR_POINTS)));
            data.setLevelNumber(Integer.parseInt(properties.getProperty(ScorePersistencyComp.ATTR_LEVEL)));
            data.setDate(LocalDate.parse(
                properties.getProperty(ScorePersistencyComp.ATTR_DATE), DateTimeFormatter.ISO_LOCAL_DATE));
        }
        catch (Exception e) {
            throw new IOException("High score file is corrupted: " + persistency.file(), e);
        }

        Logger.info("Score loaded from file '{}': points={}, level={}",
            persistency.file(), data.points(), data.levelNumber());
    }

    /**
     * Saves the current score to the XML file using an atomic write.
     *
     * <p>The save process is:
     * <ol>
     *   <li>Ensure the parent directory exists</li>
     *   <li>Write the XML data to a temporary file</li>
     *   <li>Atomically replace the target file with the temporary file</li>
     * </ol>
     *
     * <p>This guarantees that the score file is never left in a partially written
     * or corrupted state, even if the JVM crashes during saving.</p>
     *
     * @throws IOException if saving fails
     */
    public static void save(Score score) throws IOException {
        final ScoreDataComp data = score.data();
        final ScorePersistencyComp persistency = score.reqComp(ScorePersistencyComp.class);

        final File parent = persistency.file().getParentFile();
        if (parent != null && !parent.exists()) {
            if (parent.mkdirs()) {
                Logger.info("Folder {} has been created", parent);
            }
        }

        final String dateTime = ScorePersistencyComp.DATE_TIME_FORMATTER.format(LocalDateTime.now());
        final var properties = new Properties();

        properties.setProperty(ScorePersistencyComp.ATTR_POINTS, String.valueOf(data.points()));
        properties.setProperty(ScorePersistencyComp.ATTR_LEVEL,  String.valueOf(data.levelNumber()));
        properties.setProperty(ScorePersistencyComp.ATTR_DATE,   data.date().format(DateTimeFormatter.ISO_LOCAL_DATE));
        properties.setProperty(ScorePersistencyComp.ATTR_URL,    ScorePersistencyComp.GITHUB_PACMAN_JAVAFX);

        // --- Atomic save logic ---
        Path target = persistency.file().toPath();
        Path temp = Files.createTempFile(target.getParent(), "score-", ".tmp");

        try (var outputStream = new BufferedOutputStream(Files.newOutputStream(temp))) {
            properties.storeToXML(outputStream, "High Score updated at %s".formatted(dateTime));
        }

        // Atomic move (best effort depending on filesystem)
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        Logger.info("High score saved in file '{}', points: {}, level: {}",
            persistency.file(), data.points(), data.levelNumber());
    }

    public static void enableScore(Score score, boolean enabled) {
        score.data().setEnabled(enabled);
    }

    public static Score createHighScore(File file) {
        Objects.requireNonNull(file);
        final Score score = new Score(Score.Type.HIGH_SCORE);
        final ScorePersistencyComp persistency = new ScorePersistencyComp();
        score.setComp(ScorePersistencyComp.class, persistency);
        persistency.setFile(file);
        return score;
    }

    public static void saveHighScoreIfNeeded(Score currentHighScore) throws IOException {
        final File file = currentHighScore.requirePersistency().file();
        final Score savedHighScore = createHighScore(file);
        load(savedHighScore);
        if (savedHighScore.data().points() < currentHighScore.data().points()) {
            save(currentHighScore);
        }
    }
}
