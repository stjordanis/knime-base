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
 *   Aug 28, 2019 (Simon Schmid, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.preproc.probability.nominal.splitter;

import java.util.Arrays;
import java.util.Set;

import org.knime.base.node.preproc.probability.nominal.ExceptionHandling;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.probability.nominal.NominalDistributionValue;
import org.knime.core.data.probability.nominal.NominalDistributionValueMetaData;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.UniqueNameGenerator;

/**
 * Node model of the node that splits probability distributions into probability values.
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
final class NominalDistributionSplitterNodeModel extends SimpleStreamableFunctionNodeModel {

    private final SettingsModelString m_columnSelectionModel = createColumnSelectionModel();

    private final SettingsModelBoolean m_removeSelectedColModel = createRemoveSelectedColBooleanModel();

    private final SettingsModelString m_suffixModel = createSuffixModel();

    private final SettingsModelString m_missingValueHandling = createMissingValueHandlingModel();

    static SettingsModelString createColumnSelectionModel() {
        return new SettingsModelString("column_selection", null);
    }

    static SettingsModelBoolean createRemoveSelectedColBooleanModel() {
        return new SettingsModelBoolean("remove_selected_columns", false);
    }

    static SettingsModelString createSuffixModel() {
        return new SettingsModelString("suffix", "");
    }

    static SettingsModelString createMissingValueHandlingModel() {
        return new SettingsModelString("missing_value_handling", ExceptionHandling.FAIL.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        // check settings
        final int columnIndex = spec.findColumnIndex(m_columnSelectionModel.getStringValue());
        CheckUtils.checkSetting(columnIndex >= 0, "The selected column '%s' is not in the input.",
            m_columnSelectionModel.getStringValue());
        final DataColumnSpec colSpec = spec.getColumnSpec(columnIndex);
        CheckUtils.checkSetting(colSpec.getType().isCompatible(NominalDistributionValue.class),
            "The selected column '%s' must be a probability distribution column.",
            m_columnSelectionModel.getStringValue());

        final ExceptionHandling missingValueHandling;
        try {
            missingValueHandling = ExceptionHandling.valueOf(m_missingValueHandling.getStringValue());
        } catch (IllegalArgumentException e) {
            throw new InvalidSettingsException(e);
        }

        // create column rearranger
        final ColumnRearranger columnRearranger = new ColumnRearranger(spec);
        final Set<String> values = NominalDistributionValueMetaData.extractFromSpec(colSpec).getValues();
        final UniqueNameGenerator uniqueNameGenerator = new UniqueNameGenerator(spec);
        final String suffix = m_suffixModel.getStringValue();
        final DataColumnSpec[] newSpecs = values.stream()
            .map(n -> uniqueNameGenerator.newColumn(n + suffix, DoubleCell.TYPE)).toArray(DataColumnSpec[]::new);
        if (m_removeSelectedColModel.getBooleanValue()) {
            columnRearranger.remove(columnIndex);
        }
        columnRearranger
            .append(new SplitProbDistributionCellFactory(newSpecs, missingValueHandling, columnIndex, values));
        return columnRearranger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_columnSelectionModel.saveSettingsTo(settings);
        m_removeSelectedColModel.saveSettingsTo(settings);
        m_suffixModel.saveSettingsTo(settings);
        m_missingValueHandling.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_columnSelectionModel.validateSettings(settings);
        m_removeSelectedColModel.validateSettings(settings);
        m_suffixModel.validateSettings(settings);
        m_missingValueHandling.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_columnSelectionModel.loadSettingsFrom(settings);
        m_removeSelectedColModel.loadSettingsFrom(settings);
        m_suffixModel.loadSettingsFrom(settings);
        m_missingValueHandling.loadSettingsFrom(settings);
    }

    /**
     * Appends double columns extracted from a probability distribution column.
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    private final class SplitProbDistributionCellFactory extends AbstractCellFactory {

        private final ExceptionHandling m_missingHandling;

        private final int m_columnIndex;

        private final Set<String> m_values;

        boolean m_hasWarning = false;

        private final DataCell[] m_missing;

        private SplitProbDistributionCellFactory(final DataColumnSpec[] colSpecs,
            final ExceptionHandling missingValueHandling, final int columnIndex, final Set<String> values) {
            super(colSpecs);
            m_missingHandling = missingValueHandling;
            m_columnIndex = columnIndex;
            m_values = values;
            final DataCell missingCell = new MissingCell("Input row contains a missing value.");
            m_missing = new DataCell[values.size()];
            Arrays.fill(m_missing, missingCell);
        }

        private void setWarningIfNotSet(final String message) {
            if (!m_hasWarning) {
                setWarningMessage(message);
                m_hasWarning = true;
            }
        }

        @Override
        public DataCell[] getCells(final DataRow row) {
            DataCell cell = row.getCell(m_columnIndex);
            if (cell.isMissing()) {
                if (m_missingHandling == ExceptionHandling.FAIL) {
                    throw new IllegalArgumentException(
                        "The row '" + row.getKey().getString() + "' contains a missing value.");
                }
                setWarningIfNotSet("At least one row contains a missing value. Missing values will be in the output.");
                return m_missing.clone();
            }
            final NominalDistributionValue probDistrValue = (NominalDistributionValue)cell;
            return m_values.stream().mapToDouble(probDistrValue::getProbability).mapToObj(DoubleCell::new)
                .toArray(DoubleCell[]::new);
        }
    }

}
