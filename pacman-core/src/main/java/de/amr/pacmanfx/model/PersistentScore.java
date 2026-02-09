/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import org.tinylog.Logger;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * A {@link Score} that can be persisted to and loaded from an XML file.
 *
 * <p>This class provides:
 * <ul>
 *   <li>loading score data from an XML-backed {@link Properties} file</li>
 *   <li>atomic saving using a temporary file and an atomic move operation</li>
 *   <li>automatic creation of the score file and its parent directory if missing</li>
 *   <li>validation of loaded data with clear error reporting</li>
 * </ul>
 *
 * <p>The XML format is intentionally simple and stable. If the file is missing,
 * a new one is created with the current score values.</p>
 */
public class PersistentScore extends Score {

    /** Timestamp format used in the XML comment header. */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** The file where the score is stored. */
    private final File file;

    /**
     * Creates a new persistent score bound to the given file.
     *
     * @param file the XML file used for persistence (must not be {@code null})
     */
    public PersistentScore(File file) {
        this.file = requireNonNull(file, "file must not be null");
    }

    /**
     * @return the file used for persistence
     */
    public File file() {
        return file;
    }

    /**
     * Loads the score from the XML file.
     *
     * <p>If the file does not exist, it is created by calling {@link #save()}.
     * If the file exists but is malformed or missing required fields, an
     * {@link IOException} is thrown.</p>
     *
     * @throws IOException if loading fails or the file is malformed
     */
    public void load() throws IOException {
        if (!file.exists()) {
            save(); // create default file
        }

        final var properties = new Properties();
        try (var inputStream = new BufferedInputStream(new FileInputStream(file))) {
            properties.loadFromXML(inputStream);
        }

        try {
            setPoints(Integer.parseInt(properties.getProperty(ATTR_POINTS)));
            setLevelNumber(Integer.parseInt(properties.getProperty(ATTR_LEVEL)));
            setDate(LocalDate.parse(properties.getProperty(ATTR_DATE), DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (Exception e) {
            throw new IOException("High score file is corrupted: " + file, e);
        }

        Logger.info("Score loaded from file '{}': points={}, level={}", file, points(), levelNumber());
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
    public void save() throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (parent.mkdirs()) {
                Logger.info("Folder {} has been created", parent);
            }
        }

        final String dateTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        final var properties = new Properties();

        properties.setProperty(ATTR_POINTS, String.valueOf(points()));
        properties.setProperty(ATTR_LEVEL,  String.valueOf(levelNumber()));
        properties.setProperty(ATTR_DATE,   date().format(DateTimeFormatter.ISO_LOCAL_DATE));
        properties.setProperty(ATTR_URL,    GITHUB_PACMAN_JAVAFX);

        // --- Atomic save logic ---
        Path target = file.toPath();
        Path temp = Files.createTempFile(target.getParent(), "score-", ".tmp");

        try (var outputStream = new BufferedOutputStream(Files.newOutputStream(temp))) {
            properties.storeToXML(outputStream, "High Score updated at %s".formatted(dateTime));
        }

        // Atomic move (best effort depending on filesystem)
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        Logger.info("High score saved in file '{}', points: {}, level: {}", file, points(), levelNumber());
    }
}
