/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import java.nio.file.Path;

/**
 * Thrown when a world map file cannot be parsed correctly.
 * <p>
 * This exception is used when:
 * <ul>
 *   <li>The file format is invalid or corrupted</li>
 *   <li>Required sections/fields are missing</li>
 *   <li>Values are out of expected range</li>
 *   <li>Syntax errors in the map definition</li>
 *   <li>Other semantic or structural problems</li>
 * </ul>
 */
public class WorldMapParseException extends Exception {

    private final Path filePath;
    private final int lineNumber;
    private final String offendingLine;

    /**
     * Creates a new parse exception without line context.
     *
     * @param message descriptive error message
     * @param filePath the map file that failed to parse (may be {@code null})
     */
    public WorldMapParseException(String message, Path filePath) {
        this(message, filePath, -1, null);
    }

    /**
     * Creates a new parse exception with line context.
     *
     * @param message        descriptive error message
     * @param filePath       the map file that failed to parse (may be {@code null})
     * @param lineNumber     the 1-based line number where the error occurred (-1 = unknown)
     * @param offendingLine  the actual line content that caused the error (may be {@code null})
     */
    public WorldMapParseException(String message, Path filePath, int lineNumber, String offendingLine) {
        super(buildDetailMessage(message, filePath, lineNumber, offendingLine));
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.offendingLine = offendingLine;
    }

    /**
     * Creates a new parse exception caused by another exception.
     *
     * @param message descriptive error message
     * @param filePath the map file that failed to parse (may be {@code null})
     * @param cause    the underlying exception
     */
    public WorldMapParseException(String message, Path filePath, Throwable cause) {
        this(message, filePath, -1, null, cause);
    }

    /**
     * Full constructor with eatsAll context information.
     */
    public WorldMapParseException(String message, Path filePath, int lineNumber,
                                  String offendingLine, Throwable cause) {
        super(buildDetailMessage(message, filePath, lineNumber, offendingLine), cause);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.offendingLine = offendingLine;
    }

    private static String buildDetailMessage(String message, Path filePath,
                                             int lineNumber, String offendingLine) {
        StringBuilder sb = new StringBuilder(message != null ? message : "Error parsing world map");

        if (filePath != null) {
            sb.append(" in file: ").append(filePath.toAbsolutePath());
        }

        if (lineNumber >= 1) {
            sb.append(" at line ").append(lineNumber);
            if (offendingLine != null && !offendingLine.isBlank()) {
                sb.append(": ").append(offendingLine.trim());
            }
        }

        return sb.toString();
    }

    /**
     * @return the path of the file that failed to parse, or {@code null} if unknown
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * @return the 1-based line number where the error occurred, or -1 if unknown
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return the content of the line that caused the error, or {@code null} if unknown
     */
    public String getOffendingLine() {
        return offendingLine;
    }

    /**
     * @return {@code true} if line number information is available
     */
    public boolean hasLineInfo() {
        return lineNumber >= 1;
    }
}