package de.amr.pacmanfx.core.entities.score.system;

import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.comp.ScoreDataComp;
import de.amr.pacmanfx.core.entities.score.comp.ScorePersistencyComp;
import de.amr.pacmanfx.core.rules.ScoringRules;
import org.tinylog.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static Score createHighScore(File file) {
        final Score score = new Score(Score.Type.HIGH_SCORE);
        score.setComp(ScorePersistencyComp.class, new ScorePersistencyComp(file));
        return score;
    }

    public static Score createHighScore(String variantName) {
        requireNonNull(variantName);
        return createHighScore(highScoreFile(variantName));
    }

    public void scorePoints(Score score, Score highScore, int points, int levelNumber, ScoringRules rules) {
        if (!score.data().isEnabled()) {
            return;
        }

        final int oldPoints = score.data().points();
        final int newPoints = oldPoints + points;
        setPoints(score, newPoints);
        score.data().setExtraLifeReached(rules.isExtraLifeAwarded(oldPoints, newPoints));

        if (highScore.data().isEnabled() && newPoints > highScore.data().points()) {
            setPoints(highScore, newPoints);
            setLevelNumber(highScore, levelNumber);
            setDate(highScore, LocalDate.now());
        }
    }

    private void setPoints(Score score, int points) {
        score.data().setPoints(points);
    }

    public void setLevelNumber(Score score, int levelNumber) {
        score.data().setLevelNumber(levelNumber);
    }

    public void setDate(Score score, LocalDate date) {
        score.data().setDate(date);
    }

    public boolean extraLifeReached(Score score) {
        return score.data().extraLifeReached();
    }

    public void clearExtraLife(Score score) {
        score.data().setExtraLifeReached(false);
    }

    public void load(Score score) throws IOException {
        final ScorePersistencyComp persistency = score.reqComp(ScorePersistencyComp.class);

        if (!persistency.file().exists()) {
            save(score); // create default file
        }

        final var properties = new Properties();
        try (var inputStream = new BufferedInputStream(new FileInputStream(persistency.file()))) {
            properties.loadFromXML(inputStream);
        }

        final ScoreDataComp data = score.data();
        data.setPoints(Integer.parseInt(properties.getProperty(ScorePersistencyComp.ATTR_POINTS)));
        data.setLevelNumber(Integer.parseInt(properties.getProperty(ScorePersistencyComp.ATTR_LEVEL)));
        data.setDate(LocalDate.parse(properties.getProperty(ScorePersistencyComp.ATTR_DATE), DateTimeFormatter.ISO_LOCAL_DATE));

        Logger.info("Score loaded from file '{}': points={}, level={}", persistency.file(), data.points(), data.levelNumber());
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
    public void save(Score score) throws IOException {
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

    public void enableScore(Score score, boolean enabled) {
        score.data().setEnabled(enabled);
    }

    public void saveHighScoreIfNeeded(Score currentHighScore) throws IOException {
        final File file = currentHighScore.requirePersistency().file();
        final Score savedHighScore = createHighScore(file);
        load(savedHighScore);
        if (savedHighScore.data().points() < currentHighScore.data().points()) {
            save(currentHighScore);
        }
    }
}
