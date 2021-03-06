/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Dec 7, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.node.table.reader.util;

import org.knime.filehandling.core.node.table.reader.config.MultiTableReadConfig;
import org.knime.filehandling.core.node.table.reader.selector.RawSpec;
import org.knime.filehandling.core.node.table.reader.selector.TableTransformation;

/**
 * Creates {@link TableTransformation} objects either by adapting an existing transformation to a new {@link RawSpec} or
 * by creating a new one from scratch.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @param <T> the type used to identify external data types
 * @noimplement non-public API
 * @noreference non-public API
 */
public interface TableTransformationFactory<T> {

    /**
     * Creates a {@link TableTransformation} by adapting {@link TableTransformation existingModel} to the new
     * {@link RawSpec}.
     *
     * @param newRawSpec the new {@link RawSpec}
     * @param config the {@link MultiTableReadConfig} of the current execution
     * @param existingTransformation to adapt
     * @return anversion of {@link TableTransformation existingModel} that is adapted to {@link RawSpec newRawSpec}
     */
    TableTransformation<T> createFromExisting(final RawSpec<T> newRawSpec,
        MultiTableReadConfig<?, ?> config, final TableTransformation<T> existingTransformation);

    /**
     * Creates a new {@link TableTransformation} based on the provided {@link RawSpec} and {@link MultiTableReadConfig
     * config}.
     *
     * @param rawSpec the {@link RawSpec} for which to create the transformation
     * @param config for creation
     * @return a new {@link TableTransformation}
     */
    TableTransformation<T> createNew(final RawSpec<T> rawSpec, final MultiTableReadConfig<?, ?> config);

}
