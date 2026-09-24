/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.validation.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects all validation diagnostics from the validation process. Provides methods to check for errors and retrieve
 * diagnostics. The container is generic over the diagnostic type so that other projects can reuse it with their own
 * diagnostic classes implementing {@link Diagnostic}.
 *
 * @param <D> the diagnostic type collected by this result
 * @author Janusch Rentenatus
 */
public class ValidationResult<D extends Diagnostic> {

    private final List<D> diagnostics = new ArrayList<>();

    /**
     * Adds a diagnostic to this result.
     *
     * @param diagnostic the diagnostic to add
     */
    public void add(D diagnostic) {
        diagnostics.add(diagnostic);
    }

    /**
     * Returns an unmodifiable list of all diagnostics.
     *
     * @return the list of diagnostics
     */
    public List<D> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    /**
     * Returns all diagnostics filtered by severity.
     *
     * @param severity the severity to filter by
     * @return list of diagnostics with the specified severity
     */
    public List<D> getDiagnosticsBySeverity(Severity severity) {
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
     * Returns the count of infos.
     *
     * @return the count of infos
     */
    public int getInfoCount() {
        return (int) diagnostics.stream().filter(d -> d.getSeverity() == Severity.INFO).count();
    }

    /**
     * Checks if this result has no diagnostics.
     *
     * @return true if there are no diagnostics
     */
    public boolean isEmpty() {
        return diagnostics.isEmpty();
    }

    /**
     * Clears all diagnostics from this result.
     */
    public void clear() {
        diagnostics.clear();
    }

    /**
     * Returns a multi-line, human-readable rendering of this result. Every diagnostic is printed on its own line via
     * its own pretty line rendering.
     *
     * @return the pretty printed result, never null
     */
    public String prettyPrint() {
        final StringBuilder sb = new StringBuilder("ValidationResult: ")
                .append(getErrorCount()).append(" error(s), ")
                .append(getWarningCount()).append(" warning(s), ")
                .append(getInfoCount()).append(" info(s)");
        for (D d : diagnostics) {
            sb.append("\n  ").append(d.prettyLine());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ValidationResult{"
                + "errorCount=" + getErrorCount()
                + ", warningCount=" + getWarningCount()
                + ", infoCount=" + getInfoCount()
                + ", total=" + getDiagnosticCount()
                + '}';
    }
}
