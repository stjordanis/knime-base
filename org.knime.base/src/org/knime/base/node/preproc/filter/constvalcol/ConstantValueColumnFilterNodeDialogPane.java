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
 *   4 Apr 2018 (Marc): created
 */
package org.knime.base.node.preproc.filter.constvalcol;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

/**
 * The dialog for the constant value column filter. The user can specify which columns should be checked for containing
 * only identical values.
 *
 * @author Marc Bux, KNIME AG, Zurich, Switzerland
 * @since 3.6
 */
public class ConstantValueColumnFilterNodeDialogPane extends DefaultNodeSettingsPane {

    /**
     * The name of the settings tag which holds the names of the columns the user has selected in the dialog as to-be-filtered
     */
    public static final String SELECTED_COLS = "filter-list";

    /**
     * The title of the list of columns that is selected to be considered for filtering
     */
    public static final String INCLUDE_LIST_TITLE = "Filter";

    //
    /**
     * The title of the list of columns that is selected to be passed through
     */
    public static final String EXCLUDE_LIST_TITLE = "Pass Through";

    /**
     * Creates a new {@link DefaultNodeSettingsPane} for the column filter in order to set the desired columns.
     */
    public ConstantValueColumnFilterNodeDialogPane() {
        SettingsModelColumnFilter2 settings = new SettingsModelColumnFilter2(SELECTED_COLS);
        DialogComponentColumnFilter2 dialog = new DialogComponentColumnFilter2(settings, 0);
        dialog.setIncludeTitle(INCLUDE_LIST_TITLE);
        dialog.setExcludeTitle(EXCLUDE_LIST_TITLE);
        dialog.setToolTipText("Select which columns to consider for filtering and which columns to pass through.");
        addDialogComponent(dialog);
    }

}