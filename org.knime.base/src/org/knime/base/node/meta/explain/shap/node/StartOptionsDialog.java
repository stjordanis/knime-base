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
 *   14.03.2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.meta.explain.shap.node;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.knime.base.node.meta.explain.node.OptionsDialog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class StartOptionsDialog implements OptionsDialog<ShapLoopStartSettings> {

    private final DataColumnSpecFilterPanel m_featureColumns = new DataColumnSpecFilterPanel();

    @SuppressWarnings("unchecked")
    private final ColumnSelectionPanel m_weightColumn =
        new ColumnSelectionPanel(null, new DataValueColumnFilter(DoubleValue.class), true);

    private final JSpinner m_explanationSetSize =
        new JSpinner(new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 1000));

    private final JTextField m_seedBox = new JTextField();

    private final JButton m_newSeedBtn = new JButton("New");

    private final JCheckBox m_useSeed = new JCheckBox("Use seed");

    private final JCheckBox m_treatCollectionsAsSingleFeature =
        new JCheckBox("Every collection column represents a single feature");

    /**
     *
     */
    public StartOptionsDialog() {
        setupListeners();
    }

    private void setupListeners() {
        m_newSeedBtn.addActionListener(e -> m_seedBox.setText(new Random().nextLong() + ""));
        m_useSeed.addActionListener(e -> reactToUseSeedCheckBox());
    }

    private void reactToUseSeedCheckBox() {
        final boolean useSeed = m_useSeed.isSelected();
        m_seedBox.setEnabled(useSeed);
        m_newSeedBtn.setEnabled(useSeed);
    }

    @Override
    public JPanel getPanel() {
        // === Options Tab ===

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGbc();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(createFeaturePanel(), gbc);

        gbc.gridy++;

        panel.add(createSamplingOptionsPanel(), gbc);

        return panel;
    }

    private JPanel createSamplingOptionsPanel() {
        final GridBagConstraints gbc = createGbc();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sampling Options"));
        addComponent(panel, gbc, "Explanation set size", m_explanationSetSize);
        addComponent(panel, gbc, "Sampling weight", m_weightColumn);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        panel.add(m_useSeed, gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        panel.add(m_seedBox, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        panel.add(m_newSeedBtn, gbc);
        return panel;
    }

    private static GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    private JPanel createFeaturePanel() {
        final GridBagConstraints gbc = createGbc();
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Feature columns"));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(m_featureColumns, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        panel.add(m_treatCollectionsAsSingleFeature, gbc);
        return panel;
    }

    private static void addComponent(final JPanel panel, final GridBagConstraints gbc, final String label,
        final Component component) {
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx++;
        panel.add(component, gbc);
    }

    @Override
    public void saveSettingsTo(final ShapLoopStartSettings cfg) throws InvalidSettingsException {
        m_featureColumns.saveConfiguration(cfg.getFeatureCols());
        cfg.setExplanationSetSize((int)m_explanationSetSize.getValue());
        cfg.setSeed(getSeedAsLong());
        cfg.setUseSeed(m_useSeed.isSelected());
        cfg.setTreatAllColumnsAsSingleFeature(m_treatCollectionsAsSingleFeature.isSelected());
        cfg.setWeightColumn(m_weightColumn.getSelectedColumn());
    }

    private long getSeedAsLong() throws InvalidSettingsException {
        final String longStr = m_seedBox.getText();
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            throw new InvalidSettingsException("The provided seed must be a long.");
        }
    }

    @Override
    public void loadSettingsFrom(final ShapLoopStartSettings cfg, final DataTableSpec[] inSpecs)
        throws NotConfigurableException {
        m_featureColumns.loadConfiguration(cfg.getFeatureCols(), inSpecs[0]);
        m_explanationSetSize.setValue(cfg.getExplanationSetSize());
        m_seedBox.setText(cfg.getManualSeed() + "");
        m_useSeed.setSelected(cfg.isUseSeed());
        reactToUseSeedCheckBox();
        m_treatCollectionsAsSingleFeature.setSelected(cfg.isTreatAllColumnsAsSingleFeature());
        final Optional<String> weightColumnName = cfg.getWeightColumn();
        m_weightColumn.update(inSpecs[1], weightColumnName.orElse(null));
    }

}