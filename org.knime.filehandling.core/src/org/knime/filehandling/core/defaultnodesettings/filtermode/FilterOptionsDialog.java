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
 *   Apr 14, 2020 (Simon Schmid, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.filtermode;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.knime.core.node.KNIMEConstants;

/**
 * Dialog for file and folder filtering options.
 *
 * @author Björn Lohrmann, KNIME GmbH, Berlin, Germany
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
final class FilterOptionsDialog extends JDialog {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** Panel holding the file filtering components */
    private final FilterOptionsPanel m_filterPanel;

    /** Title for the dialog */
    private static final String TITLE_STRING = "Filter options";

    /** Ok button label */
    private static final String OK_BUTTON_LABEL = "OK";

    /** Close button label */
    private static final String CLOSE_BUTTON_LABEL = "Cancel";

    /**
     * This field holds information on how the dialog was closed. Its value is one of {@link JOptionPane#OK_OPTION} or
     * {@link JOptionPane#CANCEL_OPTION} or -1 if the dialog is not been closed yet.
     */
    private int m_resultStatus = -1;

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param panel the filter options panel that is shown in the dialog
     */
    FilterOptionsDialog(final Frame owner, final FilterOptionsPanel panel) {
        super(owner, TITLE_STRING, true);
        KNIMEConstants.getKNIMEIcon16X16().ifPresent(i -> setIconImage(i.getImage()));
        m_filterPanel = panel;

        // filter options panel
        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new GridBagLayout());

        final GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.CENTER;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        rootPanel.add(m_filterPanel, gc);

        // buttons
        gc.anchor = GridBagConstraints.LINE_END;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 1;
        gc.weighty = 0;
        gc.ipadx = 20;
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 1;
        gc.insets = new Insets(0, 10, 10, 0);
        final JButton okButton = new JButton(OK_BUTTON_LABEL);
        okButton.addActionListener((e) -> onOk());
        rootPanel.add(okButton, gc);

        gc.anchor = GridBagConstraints.LINE_START;
        gc.weightx = 0;
        gc.ipadx = 10;
        gc.gridx = 1;
        gc.insets = new Insets(0, 5, 10, 10);
        final JButton cancelButton = new JButton(CLOSE_BUTTON_LABEL);
        cancelButton.addActionListener((e) -> onCancel());
        rootPanel.add(cancelButton, gc);

        setContentPane(rootPanel);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we) {
                //handle all window closing events triggered by none of
                //the given buttons
                onCancel();
            }
        });
        pack();
    }

    /** Closes the dialog */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    /** Method that defines what happens when hitting the OK button */
    private void onOk() {
        m_resultStatus = JOptionPane.OK_OPTION;
        closeDialog();
    }

    /** Method that defines what happens when hitting the Close button */
    private void onCancel() {
        m_resultStatus = JOptionPane.CANCEL_OPTION;
        closeDialog();
    }

    /**
     * Returns how the dialog was closed by the user.
     *
     * @return {@link JOptionPane#OK_OPTION} if the user pressed OK, {@link JOptionPane#CANCEL_OPTION} if the user
     *         canceled or closed the dialog, or -1 if the dialog is not been closed yet.
     */
    int getResultStatus() {
        return m_resultStatus;
    }

}
