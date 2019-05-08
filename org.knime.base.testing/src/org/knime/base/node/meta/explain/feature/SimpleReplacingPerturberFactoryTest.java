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
 *   Apr 29, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.meta.explain.feature;

import static org.junit.Assert.assertArrayEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knime.base.node.meta.explain.util.RowSampler;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Sets;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleReplacingPerturberFactoryTest {

    @Mock
    private RowSampler m_rowSampler;

    @Mock
    private DataRow m_samplingRow;

    @Mock
    private DataRow m_perturbee;

    @Mock
    private DataCell m_samplingCell;

    @Mock
    private DataCell m_originalCell;

    private Perturber<DataRow, Set<Integer>, DataCell[]> m_perturber;

    @Before
    public void init() {
        Mockito.when(m_rowSampler.sampleRow()).thenReturn(m_samplingRow);
        Mockito.when(m_samplingRow.getCell(ArgumentMatchers.anyInt())).thenReturn(m_samplingCell);
        Mockito.when(m_perturbee.getCell(ArgumentMatchers.anyInt())).thenReturn(m_originalCell);
        Mockito.when(m_samplingRow.getNumCells()).thenReturn(5);
        Mockito.when(m_perturbee.getNumCells()).thenReturn(5);
        m_perturber = new SimpleReplacingPerturberFactory(m_rowSampler).createPerturber();
    }

    private DataCell[] createExpected(final Set<Integer> config, final int numCells) {
        final DataCell[] cells = new DataCell[numCells];
        for (int i = 0; i < numCells; i++) {
            cells[i] = config.contains(i) ? m_samplingCell : m_originalCell;
        }
        return cells;
    }

    @Test
    public void testPerturber() throws Exception {
        final Set<Integer> config = Sets.newHashSet(1, 3);
        final DataCell[] expected = createExpected(config, 5);
        final DataCell[] actual = m_perturber.perturb(m_perturbee, config);
        assertArrayEquals(expected, actual);
    }
}