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
 *   Sep 7, 2020 (Mark Ortmann, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.base.node.flowvariable.converter.celltovariable;

import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.MissingValue;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;

/**
 * A converter that allows to translate a {@link DataCell} into a {@link FlowVariable}.
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 * @author Jannik Löscher, KNIME GmbH, Konstanz, Germany
 * @param <T> the simple type of the {@link FlowVariable}'s {@link VariableType} resulting from the conversion.
 */
public interface CellToVariableConverter<T> {

    /**
     * Converts the given {@link DataCell} into the matching {@link FlowVariable}.
     *
     * @param varName the name of the flow variable to create
     * @param cell the cell to be converted
     * @param missingValueHandler the {@link MissingValueHandler} that should be used. In a collection a omit value
     *            (return value of <code>null</code>) should not include the value. If a missing {@code cell} shall be
     *            omitted, the method should return an empty {@link Optional}.
     * @return the resulting {@link FlowVariable} or empty if {@code cell} is a {@link MissingValue} and should be
     *         omitted
     * @apiNote Implementations should at least implement this function.
     */
    Optional<FlowVariable> createFlowVariable(final String varName, final DataCell cell,
        final MissingValueHandler missingValueHandler);

    /**
     * Returns the {@link VariableType} of the {@link FlowVariable} when converting a {@link DataCell}
     *
     * @return the {@link VariableType} of the created {@link FlowVariable}
     */
    VariableType<T> getVariableType();

}
