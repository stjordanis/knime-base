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
 *   Aug 15, 2019 (bjoern): created
 */
package org.knime.filehandling.core.defaultnodesettings;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.FSConnectionFlowVariableProvider;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.FileSystemBrowser.DialogType;
import org.knime.core.node.util.FileSystemBrowser.FileSelectionMode;
import org.knime.core.node.util.LocalFileSystemBrowser;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.core.util.Pair;
import org.knime.filehandling.core.filefilter.FileFilterDialog;

/**
 *
 * @author bjoern
 */
public class DialogComponentFileChooserGen3 extends DialogComponent {

    private static final NodeLogger LOG = NodeLogger.getLogger(DialogComponentFileChooserGen3.class);

    private FSConnectionFlowVariableProvider m_connectionFlowVariableProvider;

    private final SettingsModelFileChooserGen2 m_settingsModel;

    private final NodeDialogPane m_dialogPane;

    private final FlowVariableModel m_pathFlowVariableModel;

    private final String[] m_defaultSuffixes;

    private final JCheckBox m_useConnection;

    private final JComboBox<String> m_connections;

    private final JPanel m_connectionSettingsPanel = new JPanel();

    private final CardLayout m_connectionSettingsCardLayout = new CardLayout();

    private final JComboBox<KNIMEConnection> m_knimeConnections;

    private final JCheckBox m_includeSubfolders;

    private final JCheckBox m_filterFiles;

    private final JLabel m_fileFolderLabel;

    private final FilesHistoryPanel m_fileHistoryPanel;

    private final JButton m_configureFilter;

    private final JLabel m_statusMessage;

    public DialogComponentFileChooserGen3(final SettingsModelFileChooserGen2 settingsModel,
        final NodeDialogPane dialogPane, final String... suffixes) {

        super(settingsModel);

        m_settingsModel = settingsModel;
        m_dialogPane = dialogPane;
        m_pathFlowVariableModel = m_dialogPane.createFlowVariableModel(m_settingsModel.getPath().getKey(), Type.STRING);
        m_defaultSuffixes = suffixes;

        m_useConnection = new JCheckBox("Read from: ");

        m_connections = new JComboBox<>(new String[0]);
        m_connections.setEnabled(false);

        m_knimeConnections = new JComboBox<>();

        m_fileFolderLabel = new JLabel("File/Folder:");

        m_fileHistoryPanel = new FilesHistoryPanel(m_pathFlowVariableModel,
            "filechoosergen2",
            new LocalFileSystemBrowser(),
            FileSelectionMode.FILES_AND_DIRECTORIES, DialogType.OPEN_DIALOG, suffixes);

        m_includeSubfolders = new JCheckBox("Include subfolders");
        m_filterFiles = new JCheckBox("Filter files in folder");
        m_configureFilter = new JButton("Configure");

        m_statusMessage = new JLabel(" ");

        initEventHandlers();
        initLayout();
        updateEnabledness(null);
    }

    private void initLayout() {
        final JPanel panel = getComponentPanel();
        panel.setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0, 5, 5);
        panel.add(m_useConnection, gbc);

        gbc.gridx++;
        panel.add(m_connections, gbc);

        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        initConnectionSettingsPanelLayout();
        panel.add(m_connectionSettingsPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 0);
        panel.add(m_fileFolderLabel, gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(m_fileHistoryPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(m_includeSubfolders, gbc);

        gbc.gridx++;
        panel.add(m_filterFiles, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor= GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(m_configureFilter, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        panel.add(m_statusMessage, gbc);
    }

    private void initConnectionSettingsPanelLayout() {
        m_connectionSettingsPanel.setLayout(m_connectionSettingsCardLayout);

        m_connectionSettingsPanel.add(initKNIMEConnectionPanel(), "KNIME");
        m_connectionSettingsPanel.add(new JPanel(), "empty");
    }

    private Component initKNIMEConnectionPanel() {
        final JPanel knimeConnectionPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        knimeConnectionPanel.add(m_knimeConnections, gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        knimeConnectionPanel.add(Box.createHorizontalGlue(), gbc);

        return knimeConnectionPanel;
    }

    private void initEventHandlers() {
        m_useConnection.addChangeListener(this::updateEnabledness);
        m_connections.addActionListener(this::updateEnabledness);
        m_fileHistoryPanel.addChangeListener(this::updateEnabledness);
        m_includeSubfolders.addChangeListener(this::updateEnabledness);
        m_filterFiles.addChangeListener(this::updateEnabledness);
        m_configureFilter.addActionListener((e) -> showFileFilterConfigurationDialog());
    }

    private void showFileFilterConfigurationDialog() {
        Frame f = null;
        Container c = getComponentPanel().getParent();
        while (c != null) {
            if (c instanceof Frame) {
                f = (Frame)c;
                break;
            }
            c = c.getParent();
        }

        final FileFilterDialog dialog = new FileFilterDialog(f, m_defaultSuffixes);
        dialog.setLocationRelativeTo(c);
        dialog.setVisible(true);
        updateStatusLine();
    }

    private void updateEnabledness(final Object event) {
        m_connections.setEnabled(m_useConnection.isSelected());

        if (m_useConnection.isSelected() && m_connections.getSelectedItem().equals("KNIME")) {
            // KNIME connections are selected
            m_connectionSettingsCardLayout.show(m_connectionSettingsPanel, "KNIME");

            m_fileFolderLabel.setEnabled(true);
            m_fileFolderLabel.setText("File/Folder:");
            m_fileHistoryPanel.setEnabled(true);
            m_fileHistoryPanel.setBrowseable(true);

            updateFolderFilterEnabledness();
        } else if (m_useConnection.isSelected() && m_connections.getSelectedItem().equals("Custom URL")) {
            // Custom URLs are selected
            m_connectionSettingsCardLayout.show(m_connectionSettingsPanel, "empty");

            m_fileFolderLabel.setEnabled(true);
            m_fileFolderLabel.setText("URL:");
            m_fileHistoryPanel.setEnabled(true);
            m_fileHistoryPanel.setBrowseable(false);

            m_includeSubfolders.setEnabled(false);
            m_filterFiles.setEnabled(false);
            m_configureFilter.setEnabled(false);
        } else {
            // some flow variable connection is selected, or we are using the local FS
            m_connectionSettingsCardLayout.show(m_connectionSettingsPanel, "empty");

            m_fileFolderLabel.setEnabled(true);
            m_fileFolderLabel.setText("File/Folder:");
            m_fileHistoryPanel.setEnabled(true);
            m_fileHistoryPanel.setBrowseable(true);

            updateFolderFilterEnabledness();
        }

        updateStatusLine();
        getComponentPanel().repaint();
    }

    private void updateStatusLine() {
        // FIXME: this is a hack to demonstrate status line functionality. We need to work with an actual remote file system
        // here. AND THIS NEEDS TO BE PUT INTO A SWINGER WORKER TO RUN IN THE BACKGROUND!
        String message = " ";
        Color messageColor = Color.GREEN;

        if (!(m_useConnection.isSelected() && m_connections.getSelectedItem().equals("Custom URL"))) {
            if (!m_fileHistoryPanel.getSelectedFile().isEmpty()) {
                final Path fileOrFolder = Paths.get(m_fileHistoryPanel.getSelectedFile());
                if (Files.isDirectory(fileOrFolder)) {
                    final Pair<Integer,Integer> matchPair = applyFolderFilters(fileOrFolder);
                    if (matchPair.getFirst() > 0) {
                        message = String.format("Will read %d files out of %d", matchPair.getFirst(), matchPair.getSecond());
                    } else {
                        message = String.format("No files matched the filters", matchPair.getSecond());
                        messageColor = Color.RED;
                    }
                } else if (!Files.isReadable(fileOrFolder)) {
                    message = "Cannot read file";
                    messageColor = Color.RED;
                }
            }
        }

        m_statusMessage.setText(message);
        m_statusMessage.setForeground(messageColor);
    }

    private Pair<Integer, Integer> applyFolderFilters(final Path folder) {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(makePathMatcherPattern());
        final boolean recurse = m_includeSubfolders.isSelected();

        final AtomicInteger matches = new AtomicInteger();
        final AtomicInteger totalVisited = new AtomicInteger();

        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                    throws IOException {
                    return (recurse || folder.equals(dir)) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
                    totalVisited.incrementAndGet();
                    if (Files.isRegularFile(path) && pathMatcher.matches(path)) {
                        matches.incrementAndGet();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        return new Pair<>(matches.get(), totalVisited.get());
    }

    private String makePathMatcherPattern() {
        return "glob:**/*.{txt,TXT}";
    }

    /**
     * @param fileOrFolder
     * @return
     */
    private int countFilterMatches(final Path fileOrFolder) {
        // TODO Auto-generated method stub
        return 0;
    }

    private void updateFolderFilterEnabledness() {
        // FIXME: this is a small hack in order to demonstrate when the folder options get
        // activated. We actually need some way to check for file or folder
        final boolean folderSelected = !m_fileHistoryPanel.getSelectedFile().isEmpty()
            && Files.isDirectory(Paths.get(m_fileHistoryPanel.getSelectedFile()));
        m_includeSubfolders.setEnabled(folderSelected);
        m_filterFiles.setEnabled(folderSelected);
        m_configureFilter.setEnabled(folderSelected && m_filterFiles.isSelected());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateComponent() {
        updateConnectionsCombo();
        updateKNIMEConnectionsCombo();
    }

    private void updateConnectionsCombo() {
        final DefaultComboBoxModel<String> connectionsModel = (DefaultComboBoxModel<String>)m_connections.getModel();
        connectionsModel.removeAllElements();
        connectionsModel.addElement("KNIME");
        connectionsModel.addElement("Custom URL");

        m_connectionFlowVariableProvider = new FSConnectionFlowVariableProvider(m_dialogPane);
        for (final String connectionName : m_connectionFlowVariableProvider.allConnectionNames()) {
            connectionsModel.addElement(connectionName);
        }
    }

    private void updateKNIMEConnectionsCombo() {
        final DefaultComboBoxModel<KNIMEConnection> knimeConnectionsModel = (DefaultComboBoxModel<KNIMEConnection>)m_knimeConnections.getModel();
        knimeConnectionsModel.removeAllElements();
        knimeConnectionsModel.addElement(KNIMEConnection.getOrCreateMountpointAbsoluteConnection("My-KNIME-Hub"));
        knimeConnectionsModel.addElement(KNIMEConnection.getOrCreateMountpointAbsoluteConnection("Testflows"));
        knimeConnectionsModel.addElement(KNIMEConnection.getOrCreateMountpointAbsoluteConnection("LOCAL"));
        knimeConnectionsModel.addElement(KNIMEConnection.MOUNTPOINT_RELATIVE);
        knimeConnectionsModel.addElement(KNIMEConnection.WORKFLOW_RELATIVE);
        knimeConnectionsModel.addElement(KNIMEConnection.NODE_RELATIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs) throws NotConfigurableException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setEnabledComponents(final boolean enabled) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToolTipText(final String text) {
        // TODO Auto-generated method stub
    }
}
