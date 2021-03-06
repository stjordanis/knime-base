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
 *   18.02.2015 (tibuch): created
 */
package org.knime.base.node.preproc.ungroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.property.hilite.DefaultHiLiteMapper;
import org.knime.core.node.property.hilite.HiLiteTranslator;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;

/**
 * This class performs the ungroup operation.
 *
 * @author Oliver Buchholz, KNIME AG, Zurich, Switzerland
 * @author Martin Horn, KNIME GmbH, Konstanz, Germany
 * @since 3.6
 */
public class UngroupOperation2 {

    private final boolean m_skipMissingValues;

    private final boolean m_skipEmptyCollections;

    private final boolean m_removeCollectionCol;

    private final boolean m_enableHilite;

    private final int[] m_colIndices;

    /**
     * Creates a new ungroup operation.
     *
     * @param enableHilite hilite enable
     * @param skipMissingValues skip missing values
     * @param removeCollectionCol remove collection columns
     * @param colIndices the column indices to ungroup - if <code>null</code> or the length is 0 nothing will be
     *            ungrouped
     */
    public UngroupOperation2(final boolean enableHilite, final boolean skipMissingValues,
        final boolean removeCollectionCol, final int[] colIndices) {
        this(enableHilite, skipMissingValues, false, removeCollectionCol, colIndices);
    }

    /**
     * Constructor.
     *
     * @param enableHilite hilite enable
     * @param skipMissingValues skip missing values
     * @param skipEmptyCollections skip empty collections
     * @param removeCollectionCol remove collection columns
     * @param colIndices the column indices to ungroup - if <code>null</code> or the length is 0 nothing will be
     *            ungrouped
     * @since 4.2
     */
    public UngroupOperation2(final boolean enableHilite, final boolean skipMissingValues,
        final boolean skipEmptyCollections, final boolean removeCollectionCol, final int[] colIndices) {
        m_colIndices = colIndices;
        m_enableHilite = enableHilite;
        m_skipMissingValues = skipMissingValues;
        m_removeCollectionCol = removeCollectionCol;
        m_skipEmptyCollections = skipEmptyCollections;
    }

    /**
     * Performs the ungroup operation on the given data table.
     *
     * @param exec the execution context
     * @param table table to perform the ungroup operation on
     * @param trans the hilite translater, will be modified directly. Must be non-null if hiliting is enabled, can be
     *            <code>null</code> otherwise
     * @return the table with the ungrouped collections
     * @throws CanceledExecutionException if the execution has been canceled
     * @throws InterruptedException if the execution has been interrupted
     * @throws InvalidSettingsException thrown if the table doesn't contain a collection column at one of the column
     *             indices to be ungrouped
     * @throws IllegalArgumentException if hiliting is enabled and no hilite translater is given
     */
    public BufferedDataTable compute(final ExecutionContext exec, final BufferedDataTable table,
        final HiLiteTranslator trans)
        throws CanceledExecutionException, InterruptedException, InvalidSettingsException {
        final BufferedDataContainer dc =
            exec.createDataContainer(createTableSpec(table.getDataTableSpec(), m_removeCollectionCol, m_colIndices));
        if (table.size() == 0) {
            dc.close();
            return dc.getTable();
        }
        DataTableRowInput in = new DataTableRowInput(table);
        BufferedDataTableRowOutput out = new BufferedDataTableRowOutput(dc);
        try {
            compute(in, out, exec, table.size(), trans);
        } finally {
            in.close();
            out.close();
        }
        return out.getDataTable();
    }

    /**
     * Performs the ungroup operation on the given row input and pushes the result to the row output.
     *
     * @param in the row input, will NOT be closed when finished
     * @param out the row input, will NOT be closed when finished
     * @param exec the execution context to check cancellation and (optional) progress logging
     * @param rowCount row count to track the progress or <code>-1</code> without progress tracking
     * @param trans the hilite translater, will be modified directly. Must be non-null if hiliting is enabled, can be
     *            <code>null</code> otherwise
     * @throws CanceledExecutionException if the execution has been canceled
     * @throws InterruptedException if the execution has been interrupted
     * @throws IllegalArgumentException if hiliting is enabled and no hilite translater is given
     */
    public void compute(final RowInput in, final RowOutput out, final ExecutionContext exec, final long rowCount,
        final HiLiteTranslator trans) throws CanceledExecutionException, InterruptedException {
        if (m_enableHilite && trans == null) {
            throw new IllegalArgumentException("HiLiteTranslator must not be null when hiliting is enabled!");
        }
        final Map<RowKey, Set<RowKey>> hiliteMapping = new HashMap<>();
        @SuppressWarnings("unchecked")
        Iterator<DataCell>[] iterators = new Iterator[m_colIndices.length];
        final DataCell[] missingCells = new DataCell[m_colIndices.length];
        Arrays.fill(missingCells, DataType.getMissingCell());
        long rowCounter = 0;
        DataRow row = null;
        while ((row = in.poll()) != null) {
            rowCounter++;
            exec.checkCanceled();
            if (rowCount > 0) {
                exec.setProgress(rowCounter / (double)rowCount, "Processing row " + rowCounter + " of " + rowCount);
            }

            boolean allMissing = fillDataCellIterator(iterators, row);

            if (allMissing) {
                //all collection column cells are missing cells append a row
                //with missing cells as well if the skip missing value option is disabled
                if (!m_skipMissingValues) {
                    final DefaultRow newRow =
                        createClone(row.getKey(), row, m_colIndices, m_removeCollectionCol, missingCells);
                    if (m_enableHilite) {
                        //create the hilite entry
                        final Set<RowKey> keys = Collections.singleton(row.getKey());
                        hiliteMapping.put(row.getKey(), keys);
                    }
                    out.push(newRow);
                }
                continue;
            }
            long counter = 1;
            final Set<RowKey> keys;
            if (m_enableHilite) {
                keys = new HashSet<>();
            } else {
                keys = null;
            }
            boolean continueLoop = false;
            boolean allEmpty = true;
            do {
                //reset the loop flag
                allMissing = true;
                continueLoop = false;
                final DataCell[] newCells = new DataCell[iterators.length];
                for (int i = 0, length = iterators.length; i < length; i++) {
                    Iterator<DataCell> iterator = iterators[i];
                    DataCell newCell;
                    if (iterator != null && iterator.hasNext()) {
                        allEmpty = false;
                        continueLoop = true;
                        newCell = iterator.next();
                    } else {
                        if (iterator == null) {
                            allEmpty = false;
                        }
                        newCell = DataType.getMissingCell();
                    }
                    if (!newCell.isMissing()) {
                        allMissing = false;
                    }
                    newCells[i] = newCell;
                }
                if (!allEmpty && !continueLoop) {
                    break;
                }
                if ((!allEmpty && allMissing && m_skipMissingValues) || (allEmpty && m_skipEmptyCollections)) {
                    continue;
                }
                final RowKey oldKey = row.getKey();
                final RowKey newKey = new RowKey(oldKey.getString() + "_" + counter++);
                final DefaultRow newRow = createClone(newKey, row, m_colIndices, m_removeCollectionCol, newCells);
                out.push(newRow);
                if (keys != null) {
                    keys.add(newKey);
                }
            } while (continueLoop);
            if (keys != null && !keys.isEmpty()) {
                hiliteMapping.put(row.getKey(), keys);
            }
        }
        if (m_enableHilite) {
            trans.setMapper(new DefaultHiLiteMapper(hiliteMapping));
        }
    }

    /**
     * Fills the passed {@link Iterator}<{@link DataCell}> array with the {@link Iterator} of each {@link CollectionDataValue} cell per row.
     * @param iterators the {@link Iterator} array
     * @param row
     * @return true in case all cells in the passed row are missing and false if there are {@link CollectionDataValue}
     */
    private boolean fillDataCellIterator(final Iterator<DataCell>[] iterators, final DataRow row) {
        boolean allMissing = true;
        for (int i = 0, length = m_colIndices.length; i < length; i++) {
            final DataCell cell = row.getCell(m_colIndices[i]);
            final CollectionDataValue listCell;
            final Iterator<DataCell> iterator;
            if (cell instanceof CollectionDataValue) {
                listCell = (CollectionDataValue)cell;
                iterator = listCell.iterator();
                allMissing = false;
            } else {
                //In case the cell is not a CollectionDataValue and therefore must be a missing cell.
                iterator = null;
            }
            iterators[i] = iterator;
        }
        return allMissing;
    }

    private DefaultRow createClone(final RowKey newKey, final DataRow row, final int[] colIdxs,
        final boolean removeCollectionCol, final DataCell[] newCells) {
        assert colIdxs.length == newCells.length;
        final Map<Integer, DataCell> map = new HashMap<>(newCells.length);
        for (int i = 0, length = newCells.length; i < length; i++) {
            map.put(Integer.valueOf(colIdxs[i]), newCells[i]);
        }
        final int cellCount;
        if (removeCollectionCol) {
            cellCount = row.getNumCells();
        } else {
            cellCount = row.getNumCells() + colIdxs.length;
        }
        final DataCell[] cells = new DataCell[cellCount];
        int cellIdx = 0;
        int newCellidx = 0;
        for (int i = 0, length = row.getNumCells(); i < length; i++) {
            if (map.containsKey(Integer.valueOf(i))) {
                if (!removeCollectionCol) {
                    cells[cellIdx++] = row.getCell(i);
                }
                cells[cellIdx++] = newCells[newCellidx++];
            } else {
                cells[cellIdx++] = row.getCell(i);
            }
        }
        return new DefaultRow(newKey, cells);
    }

    /**
     * Creates the data table spec the ungroup operation would result in if applied on the given input table spec (i.e.
     * {@link #compute(ExecutionContext, BufferedDataTable, HiLiteTranslator)} returns a table of the same table spec).
     *
     * @param spec original spec
     * @param removeCollectionCol <code>true</code> if the collection column should be removed
     * @param colIndices the indices of the collection columns
     * @return the new spec
     * @throws InvalidSettingsException if an exception occurs, e.g. if the column at the a given index is not a
     *             collection column
     */
    public static DataTableSpec createTableSpec(final DataTableSpec spec, final boolean removeCollectionCol,
        final int[] colIndices) throws InvalidSettingsException {
        if (colIndices == null || colIndices.length <= 0) {
            //the user has not selected any column
            return spec;
        }
        final Collection<DataColumnSpec> specs = new LinkedList<>();
        final Map<String, DataType> collectionColsMap = new LinkedHashMap<>(colIndices.length);
        for (final Integer colIndex : colIndices) {
            final DataColumnSpec colSpec = spec.getColumnSpec(colIndex);
            final DataType type = colSpec.getType();
            final DataType basicType = type.getCollectionElementType();
            final String colName = spec.getColumnSpec(colIndex).getName();
            if (basicType == null) {
                throw new InvalidSettingsException("Column '" + colName + "' is not of collection type");
            }
            collectionColsMap.put(colName, basicType);
        }
        final DataColumnSpecCreator specCreator = new DataColumnSpecCreator("dummy", StringCell.TYPE);
        for (final DataColumnSpec origColSpec : spec) {
            final String origColName = origColSpec.getName();
            final DataType resultType = collectionColsMap.get(origColName);
            if (resultType != null) {
                if (!removeCollectionCol) {
                    specs.add(origColSpec);
                    specCreator.setName(DataTableSpec.getUniqueColumnName(spec, origColName));
                } else {
                    specCreator.setName(origColName);
                }
                specCreator.setType(resultType);
                specs.add(specCreator.createSpec());
            } else {
                specs.add(origColSpec);
            }
        }
        return new DataTableSpec(specs.toArray(new DataColumnSpec[0]));
    }
}
