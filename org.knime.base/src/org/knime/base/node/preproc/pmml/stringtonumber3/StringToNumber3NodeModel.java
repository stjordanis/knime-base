/*
 * ------------------------------------------------------------------------
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
 * --------------------------------------------------------------------
 *
 * History
 *   03.07.2007 (cebron): created
 */
package org.knime.base.node.preproc.pmml.stringtonumber3;

import java.util.Arrays;
import java.util.stream.Stream;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

/**
 * The NodeModel for the String to Number Node that converts strings to numbers.
 *
 * @author cebron, University of Konstanz
 * @since 3.8
 */
public class StringToNumber3NodeModel extends AbstractStringToNumberNodeModel<SettingsModelColumnFilter2>{
    /**
     * @return a SettingsModelColumnFilter2 for the included columns filtered for string values
     */
    @SuppressWarnings("unchecked")
    static SettingsModelColumnFilter2 createInclModel() {
        return new SettingsModelColumnFilter2(CFG_INCLUDED_COLUMNS, StringValue.class);
    }

    /**
     * Constructor
     */
    public StringToNumber3NodeModel() {
        this(true);
    }

    /**
     * Constructor
     * @param pmmlInEnabled true if an optional PMML input port should be present
     */
    public StringToNumber3NodeModel(final boolean pmmlInEnabled) {
        super(pmmlInEnabled, createInclModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getStoredInclCols(final DataTableSpec inSpec) {
        String[] inclCols = m_inclCols.applyTo(inSpec).getIncludes();
        String[] remInclCols = m_inclCols.applyTo(inSpec).getRemovedFromIncludes();
        return Stream.concat(Arrays.stream(inclCols), Arrays.stream(remInclCols)).toArray(String[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isKeepAllSelected() {
        return false;
    }
}
