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
 *   Aug 5, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.node.table.reader.preview.dialog;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.NativeNodeContainer;
import org.knime.core.node.workflow.NodeContext;
import org.knime.core.util.SwingWorkerWithContext;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.ReadPathAccessor;
import org.knime.filehandling.core.node.table.reader.MultiTableReadFactory;
import org.knime.filehandling.core.node.table.reader.SourceGroup;
import org.knime.filehandling.core.node.table.reader.config.ImmutableMultiTableReadConfig;
import org.knime.filehandling.core.node.table.reader.config.MultiTableReadConfig;
import org.knime.filehandling.core.node.table.reader.config.ReaderSpecificConfig;
import org.knime.filehandling.core.node.table.reader.config.tablespec.TableSpecConfig;
import org.knime.filehandling.core.node.table.reader.preview.dialog.AnalysisComponentModel.MessageType;
import org.knime.filehandling.core.node.table.reader.selector.ObservableTransformationModelProvider;
import org.knime.filehandling.core.node.table.reader.selector.TableTransformation;
import org.knime.filehandling.core.node.table.reader.util.MultiTableRead;
import org.knime.filehandling.core.node.table.reader.util.StagedMultiTableRead;
import org.knime.filehandling.core.util.CheckedExceptionSupplier;

/**
 * Controls the communication between the transformation model and the preview table.</br>
 * All I/O operations are executed in the background with {@link SwingWorkerWithContext swing workers}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @param <I> the item type to read from
 * @param <C> the type of {@link ReaderSpecificConfig}
 * @param <T> the type used to identify external types
 * @noinstantiate non-public API
 * @noreference non-public API
 */
public final class TableReaderPreviewTransformationCoordinator<I, C extends ReaderSpecificConfig<C>, T> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TableReaderPreviewTransformationCoordinator.class);

    private final MultiTableReadFactory<I, C, T> m_readFactory;

    private final AnalysisComponentModel m_analysisComponent;

    private final TableReaderPreviewModel m_previewModel;

    private final ObservableTransformationModelProvider<T> m_transformationModel;

    private final CheckedExceptionSupplier<MultiTableReadConfig<C, T>, InvalidSettingsException> m_configSupplier;

    private final Supplier<GenericItemAccessor<I>> m_readPathAccessorSupplier;

    private final boolean m_bufferPreview;

    private PreviewRun m_currentRun;

    /**
     * @param readFactory GenericMultiTableReadFactory to use
     * @param transformationModel ObservableTransformationModelProvider
     * @param analysisComponentModel AnalysisComponentModel
     * @param tableReaderPreviewModel {@link TableReaderPreviewModel}
     * @param configSupplier {@link CheckedExceptionSupplier}
     * @param itemAccessorSupplier GenericItemAccessor supplier
     * @param bufferPreview controls whether the preview is buffered or not (only set to {@code true} for readers where
     *            reading individual rows is very expensive e.g. JSON)
     */
    public TableReaderPreviewTransformationCoordinator(final MultiTableReadFactory<I, C, T> readFactory,
        final ObservableTransformationModelProvider<T> transformationModel,
        final AnalysisComponentModel analysisComponentModel, final TableReaderPreviewModel tableReaderPreviewModel,
        final CheckedExceptionSupplier<MultiTableReadConfig<C, T>, InvalidSettingsException> configSupplier,
        final Supplier<GenericItemAccessor<I>> itemAccessorSupplier, final boolean bufferPreview) {
        m_readFactory = readFactory;
        m_transformationModel = transformationModel;
        m_analysisComponent = analysisComponentModel;
        m_previewModel = tableReaderPreviewModel;
        m_configSupplier = configSupplier;
        m_readPathAccessorSupplier = itemAccessorSupplier;
        m_transformationModel.addChangeListener(e -> handleTransformationModelChange());
        m_bufferPreview = bufferPreview;
    }

    /**
     * Updates the transformation model and consequently the preview if the config changed.
     */
    public void configChanged() {
        cancelCurrentRun();
        m_analysisComponent.reset();
        try {
            m_currentRun = new PreviewRun(m_configSupplier.get());
        } catch (InvalidSettingsException ex) {// NOSONAR, the exception is displayed in the dialog
            m_analysisComponent.setErrorLabel(ex.getMessage());
            m_previewModel.setDataTable(null);
            m_transformationModel.clearRawSpec();
        }
    }

    /**
     * Sets the info message displayed when opened in remote job view.
     */
    public void setDisabledInRemoteJobViewInfo() {
        m_analysisComponent.setInfo("Preview is disabled in remote job view.");
    }

    private void cancelCurrentRun() {
        if (m_currentRun != null) {
            m_currentRun.close();
            m_currentRun = null;
        }
    }

    /**
     * Loads the given {@link TableTransformation} into the {@link ObservableTransformationModelProvider} that is
     * managed by this controller.
     *
     * @param transformationModel to load
     */
    public void load(final TableTransformation<T> transformationModel) {
        m_transformationModel.load(transformationModel);
    }

    /**
     * Returns the {@link TableSpecConfig} for the current settings
     *
     * @return the {@link TableSpecConfig} for the current settings
     */
    public TableSpecConfig<T> getTableSpecConfig() {
        return m_currentRun != null ? m_currentRun.getTableSpecConfig() : null;
    }

    /**
     * To be called when the dialog is closed.
     */
    public void onClose() {
        cancelCurrentRun();
        m_previewModel.onClose();
    }

    private void handleTransformationModelChange() {
        // changes to the model while we are updating can only be caused by us in #consumeNewStagedMultiRead
        // and are therefore handled in PreviewRun#consumeNewStagedMultiRead
        if (m_currentRun != null && !m_currentRun.isUpdatingPreview()) {
            /* if a previous user-specified transformation caused an error
            * (e.g. if the selected converter is not applicable for the column)
            * then this error needs to be cleared, otherwise it will still be displayed
            * even though the new transformation is valid
            */
            m_analysisComponent.resetErrorLabel();
            m_currentRun.updatePreviewTable();
        }
    }

    /**
     * Represents the calculations corresponding to one set of {@link MultiTableReadConfig} and
     * {@link ReadPathAccessor}.
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    private final class PreviewRun implements AutoCloseable {

        private ImmutableMultiTableReadConfig<C, T> m_config;

        private SpecGuessingSwingWorker<I, C, T> m_specGuessingWorker = null;

        private SourceGroupSwingWorker<I> m_pathAccessWorker = null;

        private TableUpdateSwingWorker m_tableUpdateWorker = null;

        private StagedMultiTableRead<I, T> m_currentRead = null;

        private GenericItemAccessor<I> m_readPathAccessor = null;

        private TableSpecConfig<T> m_currentTableSpecConfig = null;

        private final AtomicBoolean m_closed = new AtomicBoolean(false);

        private boolean m_updatingPreview = true;

        private SourceGroup<I> m_sourceGroup;

        PreviewRun(final MultiTableReadConfig<C, T> config) {
            m_config = new ImmutableMultiTableReadConfig<>(config);
            m_readPathAccessor = m_readPathAccessorSupplier.get();
            m_pathAccessWorker =
                new SourceGroupSwingWorker<>(m_readPathAccessor, this::startSpecGuessingWorker, this::displayPathError);
            m_pathAccessWorker.execute();
        }

        TableSpecConfig<T> getTableSpecConfig() {
            return m_currentTableSpecConfig;
        }

        @Override
        public void close() {
            m_closed.set(true);
            cancelIfNotNull(m_pathAccessWorker);
            cancelIfNotNull(m_specGuessingWorker);
            cancelTableUpdate();
            // the preview must be closed before we close the readPathAccessor
            // otherwise the iterator might throw a ClosedFileSystemException
            m_previewModel.setDataTable(null);
            if (m_readPathAccessor != null) {
                new CloserSwingWorker(m_readPathAccessor).execute();
            }
        }

        private void cancelIfNotNull(final SwingWorker<?, ?> worker) {
            if (worker != null) {
                worker.cancel(true);
            }
        }

        private void displayPathError(final ExecutionException exception) {
            m_analysisComponent.setErrorLabel(MessageType.ERROR, exception.getCause().getMessage());
            m_previewModel.setDataTable(null);
            m_transformationModel.clearRawSpec();
        }

        boolean isUpdatingPreview() {
            return m_updatingPreview;
        }

        /**
         * Executed by the m_pathAccessWorker once it resolved the list of paths.
         *
         * @param rootPathAndPaths the list of paths resolved by m_pathAccessWorker
         */
        private void startSpecGuessingWorker(final SourceGroup<I> sourceGroup) {
            if (m_closed.get()) {
                // this method is called in the EDT so it might be the case that
                // the run got cancelled between the completion of the path access worker
                // and the invocation of its background worker
                return;
            }
            m_analysisComponent.startUpdate()//
                .showProgressBar(true)//
                .showQuickScanButton(true)//
                .finishUpdate();
            m_sourceGroup = sourceGroup;
            m_specGuessingWorker = new SpecGuessingSwingWorker<>(m_readFactory, m_sourceGroup, m_config,
                m_analysisComponent, this::consumeNewStagedMultiRead, e -> calculatingRawSpecFailed());
            m_specGuessingWorker.execute();
        }

        private void calculatingRawSpecFailed() {
            // the raw spec could not be calculated because of some configuration problem
            // (not the path though because in that case retrieving the path would have already failed)
            m_transformationModel.clearRawSpec();
        }

        private void consumeNewStagedMultiRead(final StagedMultiTableRead<I, T> stagedMultiTableRead) {
            if (m_closed.get()) {
                // this method is called in the EDT so it might be the case that
                // the run got cancelled between the completion of the StagedMultiTableRead
                // and the invocation of its background worker
                return;
            }
            m_currentRead = stagedMultiTableRead;
            // we disable the transformation view during this update to avoid concurrent manipulation
            m_transformationModel.setEnabled(false);
            m_transformationModel.updateRawSpec(m_currentRead.getRawSpec(), m_config);
            // the table spec might not change but the read accessor will be closed therefore we need to
            // update the preview table otherwise we risk IOExceptions because the paths are no longer valid
            // In addition to this issue, it might also be the case that a config change might not result in
            // a different table spec but in a different table content e.g. if some rows are skipped
            updatePreviewTable();
            m_transformationModel.setEnabled(true);
            m_updatingPreview = false;
        }

        private void updatePreviewTable() {
            if (m_closed.get()) {
                return;
            }
            try {
                cancelTableUpdate();
                final MultiTableRead<T> mtr = createMultiTableRead();
                m_currentTableSpecConfig = mtr.getTableSpecConfig();
                if (m_bufferPreview) {
                    bufferPreviewInBackground(mtr);
                } else {
                    setPreviewDirectly(mtr);
                }
            } catch (Exception ex) {// NOSONAR we need to handle all exceptions in the same way
                LOGGER.debug("Updating the preview table failed.", ex);
                m_analysisComponent.setErrorLabel(MessageType.ERROR, ex.getMessage());
                m_previewModel.setDataTable(null);
            }
        }

        private void cancelTableUpdate() {
            cancelIfNotNull(m_tableUpdateWorker);
        }

        private void bufferPreviewInBackground(final MultiTableRead<T> mtr) {
            m_analysisComponent.startUpdate()//
                .reset()//
                .showProgressBar(true)//
                .setProgressIndeterminate(true)//
                .finishUpdate();
            m_tableUpdateWorker = new TableUpdateSwingWorker(mtr);
            m_tableUpdateWorker.execute();
        }

        private void setPreviewDirectly(final MultiTableRead<T> mtr) {
            @SuppressWarnings("resource") // the m_preview must make sure that the PreviewDataTable is closed
            final PreviewDataTable pdt =
                PreviewDataTable.createLazyPreviewDataTable(mtr.getOutputSpec(), mtr::createPreviewIterator);
            m_previewModel.setDataTable(pdt);
        }

        private MultiTableRead<T> createMultiTableRead() {
            if (m_config.saveTableSpecConfig()) {
                return m_currentRead.withTransformation(m_sourceGroup, m_transformationModel.getTableTransformation());
            } else {
                return m_currentRead.withoutTransformation(m_sourceGroup);
            }
        }

        private class TableUpdateSwingWorker extends SwingWorkerWithContext<PreviewDataTable, Void> {

            private final MultiTableRead<?> m_multiTableRead;

            private final ExecutionContext m_exec;

            TableUpdateSwingWorker(final MultiTableRead<?> multiTableRead) {
                m_multiTableRead = multiTableRead;
                NodeContext nodeContext = NodeContext.getContext();
                NativeNodeContainer nodeContainer = (NativeNodeContainer)nodeContext.getNodeContainer();
                m_exec = nodeContainer.createExecutionContext();
            }

            @Override
            protected PreviewDataTable doInBackgroundWithContext() throws Exception {
                // necessary because otherwise the ExecutionContext is already canceled
                m_exec.getProgressMonitor().reset();
                return PreviewDataTable.createBufferedPreviewDataTable(m_multiTableRead.getOutputSpec(),
                    m_multiTableRead::createPreviewIterator, m_exec);
            }

            @Override
            protected void doneWithContext() {
                if (isCancelled()) {
                    m_exec.getProgressMonitor().setExecuteCanceled();
                } else {
                    try {
                        @SuppressWarnings("resource") // closing previewTable is the responsibility of m_previewModel
                        PreviewDataTable previewTable = get();
                        m_previewModel.setDataTable(previewTable);
                    } catch (InterruptedException ex) {//NOSONAR
                        LOGGER.debug("Table preview update interrupted before it could run.", ex);
                    } catch (ExecutionException ex) {
                        LOGGER.debug("Buffering Table preview failed.", ex);
                        m_analysisComponent.startUpdate()//
                            .setErrorMessage(MessageType.ERROR, ex.getCause().getMessage())//
                            .showProgressBar(false)//
                            .finishUpdate();
                        return;
                    }
                    m_analysisComponent.startUpdate()//
                        .reset()//
                        .showProgressBar(false)//
                        .finishUpdate();
                }
            }

        }

    }
}
