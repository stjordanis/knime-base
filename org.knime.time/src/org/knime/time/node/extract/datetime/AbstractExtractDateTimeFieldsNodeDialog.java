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
 *   May 11, 2021 (ortmann): created
 */
package org.knime.time.node.extract.datetime;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.data.time.localtime.LocalTimeValue;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @author Marcel Wiedenmann, KNIME.com, Konstanz, Germany
 */
abstract class AbstractExtractDateTimeFieldsNodeDialog extends NodeDialogPane {

    private final DialogComponentColumnNameSelection m_dialogCompColSelect;

    private final DialogComponentBoolean[] m_dialogCompDateFields;

    private final DialogComponentBoolean[] m_dialogCompTimeFields;

    private final DialogComponentStringSelection m_dialogCompSubsecondUnits;

    private final DialogComponentBoolean[] m_dialogCompTimeZoneFields;

    private final SettingsModelString m_localeModel;

    private final JComboBox<Locale> m_localeComboBox;

    private final LocaleProvider m_localeProvider;

    private final Locale m_defaultLocale;

    /**
     * Creates a new dialog.
     */
    @SuppressWarnings("unchecked")
    AbstractExtractDateTimeFieldsNodeDialog(final LocaleProvider localeProvider, final Locale defaultLocale) {
        // dialog components:

        m_dialogCompColSelect = new DialogComponentColumnNameSelection(
            AbstractExtractDateTimeFieldsNodeModel.createColSelectModel(), "Date&Time column", 0, true,
            LocalDateValue.class, LocalTimeValue.class, LocalDateTimeValue.class, ZonedDateTimeValue.class);

        final String[] fieldsDate = new String[]{AbstractExtractDateTimeFieldsNodeModel.YEAR,
            AbstractExtractDateTimeFieldsNodeModel.YEAR_WEEK_BASED, AbstractExtractDateTimeFieldsNodeModel.QUARTER,
            AbstractExtractDateTimeFieldsNodeModel.MONTH_NUMBER, AbstractExtractDateTimeFieldsNodeModel.MONTH_NAME,
            AbstractExtractDateTimeFieldsNodeModel.WEEK, AbstractExtractDateTimeFieldsNodeModel.DAY_OF_YEAR,
            AbstractExtractDateTimeFieldsNodeModel.DAY_OF_MONTH,
            AbstractExtractDateTimeFieldsNodeModel.DAY_OF_WEEK_NUMBER,
            AbstractExtractDateTimeFieldsNodeModel.DAY_OF_WEEK_NAME};

        m_dialogCompDateFields = new DialogComponentBoolean[fieldsDate.length];
        for (int i = 0; i < fieldsDate.length; i++) {
            final String field = fieldsDate[i];
            m_dialogCompDateFields[i] = new DialogComponentBoolean(
                AbstractExtractDateTimeFieldsNodeModel.createFieldBooleanModel(field), field);
        }

        final String[] fieldsTime =
            new String[]{AbstractExtractDateTimeFieldsNodeModel.HOUR, AbstractExtractDateTimeFieldsNodeModel.MINUTE,
                AbstractExtractDateTimeFieldsNodeModel.SECOND, AbstractExtractDateTimeFieldsNodeModel.SUBSECOND};

        m_dialogCompTimeFields = new DialogComponentBoolean[fieldsTime.length];
        for (int i = 0; i < fieldsTime.length; i++) {
            final String field = fieldsTime[i];
            m_dialogCompTimeFields[i] = new DialogComponentBoolean(
                AbstractExtractDateTimeFieldsNodeModel.createFieldBooleanModel(field), field);
        }

        final String[] subsecondUnits = new String[]{AbstractExtractDateTimeFieldsNodeModel.MILLISECOND,
            AbstractExtractDateTimeFieldsNodeModel.MICROSECOND, AbstractExtractDateTimeFieldsNodeModel.NANOSECOND};

        m_dialogCompSubsecondUnits = new DialogComponentStringSelection(
            AbstractExtractDateTimeFieldsNodeModel.createSubsecondUnitsModel(
                (SettingsModelBoolean)m_dialogCompTimeFields[m_dialogCompTimeFields.length - 1].getModel()),
            null, subsecondUnits);

        final String[] fieldsTimeZone = new String[]{AbstractExtractDateTimeFieldsNodeModel.TIME_ZONE_NAME,
            AbstractExtractDateTimeFieldsNodeModel.TIME_ZONE_OFFSET};

        m_dialogCompTimeZoneFields = new DialogComponentBoolean[fieldsTimeZone.length];
        for (int i = 0; i < fieldsTimeZone.length; i++) {
            final String field = fieldsTimeZone[i];
            m_dialogCompTimeZoneFields[i] = new DialogComponentBoolean(
                AbstractExtractDateTimeFieldsNodeModel.createFieldBooleanModel(field), field);
        }

        m_localeProvider = localeProvider;
        m_defaultLocale = defaultLocale;
        m_localeModel = AbstractExtractDateTimeFieldsNodeModel.createLocaleModel(defaultLocale);
        m_localeComboBox = new JComboBox<>(m_localeProvider.getLocales());
    }

    protected final void initPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints constr = new GridBagConstraints();
        constr.insets = new Insets(5, 5, 5, 5);
        constr.fill = GridBagConstraints.BOTH;
        constr.gridx = 0;
        constr.gridy = 0;
        constr.weightx = 1;

        // column selection:
        constr.gridwidth = 2;
        panel.add(createColSelectionPanel(), constr);
        constr.gridwidth = 1;
        constr.gridy++;

        // date checkboxes:
        constr.weightx = 0;
        constr.gridheight = 2;
        constr.ipadx = 100;
        panel.add(createDatePanel(), constr);
        constr.weightx = 1;
        constr.gridheight = 1;
        constr.ipadx = 0;
        constr.gridx++;

        // time checkboxes:
        constr.ipadx = 30;
        panel.add(createTimePanel(), constr);
        constr.ipadx = 0;
        constr.gridy++;

        constr.ipadx = 100;
        panel.add(createTimeZonePanel(), constr);
        constr.ipadx = 0;
        constr.gridy++;

        // output settings:
        constr.gridx = 0;
        constr.gridwidth = 2;
        constr.weighty = 1;
        panel.add(createOutputSettingsPanel(), constr);

        // register change listeners:
        m_dialogCompColSelect.getModel().addChangeListener(l -> refreshFieldsSelectionsEnabled());
        final DialogComponent comp = m_dialogCompTimeFields[m_dialogCompTimeFields.length - 1];
        comp.getModel().addChangeListener(l -> m_dialogCompSubsecondUnits.getModel()
            .setEnabled(comp.getModel().isEnabled() && ((SettingsModelBoolean)comp.getModel()).getBooleanValue()));

        // add panel to dialog:

        addTab("Options", panel);
    }

    /**
     * @return
     */
    private JPanel createColSelectionPanel() {
        final JPanel panelColSelect = new JPanel(new GridBagLayout());
        panelColSelect.setBorder(BorderFactory.createTitledBorder("Column Selection"));
        final GridBagConstraints constrColSelect = new GridBagConstraints();
        constrColSelect.insets = new Insets(5, 5, 5, 5);
        constrColSelect.fill = GridBagConstraints.VERTICAL;
        constrColSelect.gridx = 0;
        constrColSelect.gridy = 0;
        constrColSelect.weightx = 1;
        constrColSelect.anchor = GridBagConstraints.WEST;
        panelColSelect.add(m_dialogCompColSelect.getComponentPanel(), constrColSelect);
        return panelColSelect;
    }

    /**
     * @return
     */
    private JPanel createDatePanel() {
        final JPanel panelDateSelect = new JPanel(new GridBagLayout());
        panelDateSelect.setBorder(BorderFactory.createTitledBorder("Date Fields"));
        final GridBagConstraints constrDateSelect = new GridBagConstraints();
        constrDateSelect.fill = GridBagConstraints.VERTICAL;
        constrDateSelect.gridx = 0;
        constrDateSelect.gridy = 0;
        constrDateSelect.weightx = 1;
        constrDateSelect.anchor = GridBagConstraints.WEST;
        for (final DialogComponentBoolean dc : m_dialogCompDateFields) {
            panelDateSelect.add(dc.getComponentPanel(), constrDateSelect);
            constrDateSelect.gridy++;
        }
        return panelDateSelect;
    }

    private Component createTimePanel() {
        final JPanel panelTimeSelect = new JPanel(new GridBagLayout());
        panelTimeSelect.setBorder(BorderFactory.createTitledBorder("Time Fields"));
        final GridBagConstraints constrTimeSelect = new GridBagConstraints();
        constrTimeSelect.fill = GridBagConstraints.VERTICAL;
        constrTimeSelect.gridx = 0;
        constrTimeSelect.gridy = 0;
        constrTimeSelect.weightx = 0;
        constrTimeSelect.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < m_dialogCompTimeFields.length - 1; i++) {
            final DialogComponentBoolean dc = m_dialogCompTimeFields[i];
            panelTimeSelect.add(dc.getComponentPanel(), constrTimeSelect);
            constrTimeSelect.gridy++;
        }
        constrTimeSelect.weightx = 0;
        final DialogComponentBoolean dialogCompSubsecond = m_dialogCompTimeFields[m_dialogCompTimeFields.length - 1];
        panelTimeSelect.add(dialogCompSubsecond.getComponentPanel(), constrTimeSelect);
        constrTimeSelect.gridx++;
        constrTimeSelect.weightx = 1;
        panelTimeSelect.add(m_dialogCompSubsecondUnits.getComponentPanel(), constrTimeSelect);
        return panelTimeSelect;
    }

    private JPanel createTimeZonePanel() {
        final JPanel panelTimeZoneSelect = new JPanel(new GridBagLayout());
        panelTimeZoneSelect.setBorder(BorderFactory.createTitledBorder("Time Zone Fields"));
        final GridBagConstraints constrTimeZoneSelect = new GridBagConstraints();
        constrTimeZoneSelect.fill = GridBagConstraints.NONE;
        constrTimeZoneSelect.gridx = 0;
        constrTimeZoneSelect.gridy = 0;
        constrTimeZoneSelect.weightx = 1;
        constrTimeZoneSelect.anchor = GridBagConstraints.NORTHWEST;
        for (final DialogComponentBoolean dc : m_dialogCompTimeZoneFields) {
            panelTimeZoneSelect.add(dc.getComponentPanel(), constrTimeZoneSelect);
            constrTimeZoneSelect.gridy++;
        }
        constrTimeZoneSelect.weighty = 1;
        constrTimeZoneSelect.fill = GridBagConstraints.VERTICAL;
        panelTimeZoneSelect.add(new JPanel(), constrTimeZoneSelect);
        return panelTimeZoneSelect;
    }

    private Component createOutputSettingsPanel() {
        final JPanel panelOutput = new JPanel(new GridBagLayout());
        panelOutput.setBorder(BorderFactory.createTitledBorder("Localization (month and day names, etc.)"));
        final GridBagConstraints constrOutput = new GridBagConstraints();
        constrOutput.insets = new Insets(5, 5, 0, 5);
        constrOutput.fill = GridBagConstraints.VERTICAL;
        constrOutput.gridx = 0;
        constrOutput.gridy = 0;
        constrOutput.weightx = 1;
        constrOutput.weighty = 1;
        constrOutput.anchor = GridBagConstraints.WEST;
        panelOutput.add(createLocalePane(), constrOutput);
        return panelOutput;
    }

    protected Component createLocalePane() {
        final JPanel p = new JPanel();
        p.add(new JLabel("Locale"));
        p.add(m_localeComboBox);
        extendLocalePanel(p);
        return p;
    }

    abstract void extendLocalePanel(final JPanel localePanel);

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_dialogCompColSelect.saveSettingsTo(settings);
        for (final DialogComponentBoolean dc : m_dialogCompDateFields) {
            dc.saveSettingsTo(settings);
        }
        for (final DialogComponentBoolean dc : m_dialogCompTimeFields) {
            dc.saveSettingsTo(settings);
        }
        m_dialogCompSubsecondUnits.saveSettingsTo(settings);
        for (final DialogComponentBoolean dc : m_dialogCompTimeZoneFields) {
            dc.saveSettingsTo(settings);
        }
        m_localeModel.setStringValue(
            LocaleProvider.localeToString(m_localeComboBox.getItemAt(m_localeComboBox.getSelectedIndex())));
        m_localeModel.saveSettingsTo(settings);
        saveAdditionalSettings(settings);
    }

    abstract void saveAdditionalSettings(final NodeSettingsWO settings) throws InvalidSettingsException;

    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        m_dialogCompColSelect.loadSettingsFrom(settings, specs);
        for (final DialogComponentBoolean dc : m_dialogCompDateFields) {
            dc.loadSettingsFrom(settings, specs);
        }
        for (final DialogComponentBoolean dc : m_dialogCompTimeFields) {
            dc.loadSettingsFrom(settings, specs);
        }
        m_dialogCompSubsecondUnits.loadSettingsFrom(settings, specs);
        for (final DialogComponentBoolean dc : m_dialogCompTimeZoneFields) {
            dc.loadSettingsFrom(settings, specs);
        }
        try {
            m_localeModel.loadSettingsFrom(settings);
        } catch (final InvalidSettingsException ex) { //NOSONAR
            m_localeModel.setStringValue(LocaleProvider.localeToString(m_defaultLocale));
        }
        Locale l;
        try {
            l = m_localeProvider.stringToLocale(m_localeModel.getStringValue());
        } catch (final InvalidSettingsException e) { // NOSONAR
            l = m_defaultLocale;
        }
        m_localeComboBox.setSelectedItem(l);
        loadAdditionalSettings(settings, specs);
        refreshFieldsSelectionsEnabled();
    }

    abstract void loadAdditionalSettings(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException;

    private void refreshFieldsSelectionsEnabled() {
        if (m_dialogCompColSelect.getSelectedAsSpec() != null) {
            final DataType type = m_dialogCompColSelect.getSelectedAsSpec().getType();
            final boolean isDate = AbstractExtractDateTimeFieldsNodeModel.isDateType(type);
            for (final DialogComponentBoolean dc : m_dialogCompDateFields) {
                dc.getModel().setEnabled(isDate);
            }
            final boolean isTime = AbstractExtractDateTimeFieldsNodeModel.isTimeType(type);
            for (final DialogComponentBoolean dc : m_dialogCompTimeFields) {
                dc.getModel().setEnabled(isTime);
            }
            final boolean isZoned = AbstractExtractDateTimeFieldsNodeModel.isZonedType(type);
            for (final DialogComponentBoolean dc : m_dialogCompTimeZoneFields) {
                dc.getModel().setEnabled(isZoned);
            }
        }
    }
}
