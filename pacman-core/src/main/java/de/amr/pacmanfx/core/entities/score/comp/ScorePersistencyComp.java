package de.amr.pacmanfx.core.entities.score.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;

import java.io.File;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;

public class ScorePersistencyComp implements EntityComponent {

    public static final String GITHUB_PACMAN_JAVAFX = "https://github.com/armin-reichert/pacman-javafx";

    public static final String ATTR_DATE = "date";
    public static final String ATTR_LEVEL = "level";
    public static final String ATTR_POINTS = "points";
    public static final String ATTR_URL = "url";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private File file;

    public ScorePersistencyComp() {}

    public void setFile(File file) {
        this.file = requireNonNull(file);
    }

    public File file() {
        return file;
    }
}
