/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
/**
 * Shared validation abstractions, independent of what is being validated. Contains the diagnostic contract
 * ({@link de.jare.jsoncasted.validation.core.Diagnostic}), the severity levels
 * ({@link de.jare.jsoncasted.validation.core.Severity}) and the generic result container
 * ({@link de.jare.jsoncasted.validation.core.ValidationResult}).
 *
 * <p>
 * Model-specific validators live in {@code de.jare.jsoncasted.validation.model}; the edit node validation of the
 * jsonCasted_edit project reuses this package and adds its own node-specific diagnostics and queries.
 * </p>
 */
package de.jare.jsoncasted.validation.core;
