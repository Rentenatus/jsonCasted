/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects all validation diagnostics from the validation process. Provides methods to check for errors and retrieve
 * diagnostics.
 *
 * @author Janusch Rentenatus
 */
public class ValidationResult {

    private final List<ValidationDiagnostic> diagnostics = new ArrayList<>();

    /**
     * Adds a diagnostic to this result.
     *
     * @param diagnostic the diagnostic to add
     */
    public void add(ValidationDiagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    /**
     * Returns an unmodifiable list of all diagnostics.
     *
     * @return the list of diagnostics
     */
    public List<ValidationDiagnostic> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    /**
     * Returns all diagnostics filtered by severity.
     *
     * @param severity the severity to filter by
     * @return list of diagnostics with the specified severity
     */
    public List<ValidationDiagnostic> getDiagnosticsBySeverity(Severity severity) {
        return diagnostics.stream()
                .filter(d -> d.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /**
     * Checks if this result contains any errors.
     *
     * @return true if there are any diagnostics with ERROR severity
     */
    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(d -> d.getSeverity() == Severity.ERROR);
    }

    /**
     * Checks if this result contains any warnings.
     *
     * @return true if there are any diagnostics with WARNING severity
     */
    public boolean hasWarnings() {
        return diagnostics.stream().anyMatch(d -> d.getSeverity() == Severity.WARNING);
    }

    /**
     * Checks if this result contains any infos.
     *
     * @return true if there are any diagnostics with INFO severity
     */
    public boolean hasInfos() {
        return diagnostics.stream().anyMatch(d -> d.getSeverity() == Severity.INFO);
    }

    /**
     * Checks if this result is valid (no errors). Warnings are allowed and do not prevent a result from being valid.
     *
     * @return true if there are no errors (warnings may still exist)
     */
    public boolean isValid() {
        return !hasErrors();
    }

    /**
     * Checks if this result is clean (no errors and no warnings). This is a stricter check than {@link #isValid()}.
     *
     * @return true if there are no errors and no warnings
     */
    public boolean isClean() {
        return !hasErrors() && !hasWarnings();
    }

    /**
     * Returns the total count of diagnostics.
     *
     * @return the count of diagnostics
     */
    public int getDiagnosticCount() {
        return diagnostics.size();
    }

    /**
     * Returns the count of errors.
     *
     * @return the count of errors
     */
    public int getErrorCount() {
        return (int) diagnostics.stream().filter(d -> d.getSeverity() == Severity.ERROR).count();
    }

    /**
     * Returns the count of warnings.
     *
     * @return the count of warnings
     */
    public int getWarningCount() {
        return (int) diagnostics.stream().filter(d -> d.getSeverity() == Severity.WARNING).count();
    }

    /**
     * Clears all diagnostics from this result.
     */
    public void clear() {
        diagnostics.clear();
    }

    @Override
    public String toString() {
        return "ValidationResult{"
                + "errorCount=" + getErrorCount()
                + ", warningCount=" + getWarningCount()
                + ", infoCount=" + (getDiagnosticCount() - getErrorCount() - getWarningCount())
                + ", total=" + getDiagnosticCount()
                + '}';
    }
}
