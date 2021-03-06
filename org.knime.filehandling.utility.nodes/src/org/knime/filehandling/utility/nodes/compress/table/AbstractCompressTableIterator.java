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
 *   Apr 13, 2021 (Mark Ortmann, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.filehandling.utility.nodes.compress.table;

import java.io.IOException;
import java.nio.file.Path;

import org.knime.core.node.BufferedDataTable;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.util.CheckedExceptionFunction;
import org.knime.filehandling.utility.nodes.compress.iterator.CompressEntry;
import org.knime.filehandling.utility.nodes.compress.iterator.CompressFileFolderEntry;
import org.knime.filehandling.utility.nodes.compress.iterator.CompressIterator;
import org.knime.filehandling.utility.nodes.truncator.PathToStringTruncator;
import org.knime.filehandling.utility.nodes.utils.iterators.FsCellColumnIterator;

/**
 * Abstract implementation of a {@link CompressIterator} that processed an input table.
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 */
abstract class AbstractCompressTableIterator implements CompressIterator {

    private final FsCellColumnIterator m_fsCellIterator;

    private final boolean m_includeEmptyFolders;

    /**
     * Constructor.
     *
     * @param table the input table
     * @param pathColIdx the column containing the paths that need to be compressed
     * @param connection the {@link FSConnection}
     * @param includeEmptyFolders flag indicating whether or not empty folder should be compressed
     */
    AbstractCompressTableIterator(final BufferedDataTable table, final int pathColIdx, final FSConnection connection,
        final boolean includeEmptyFolders) {
        m_fsCellIterator = new FsCellColumnIterator(table, pathColIdx, connection);
        m_includeEmptyFolders = includeEmptyFolders;
    }

    @Override
    public final long size() {
        return m_fsCellIterator.size();
    }

    @Override
    public final boolean hasNext() {
        return m_fsCellIterator.hasNext();
    }

    final CompressEntry
        createEntry(final CheckedExceptionFunction<Path, PathToStringTruncator, IOException> truncatorFac) {
        return new CompressFileFolderEntry(m_fsCellIterator.next(), m_includeEmptyFolders, truncatorFac);
    }

    @Override
    public void close() {
        m_fsCellIterator.close();
    }

}
