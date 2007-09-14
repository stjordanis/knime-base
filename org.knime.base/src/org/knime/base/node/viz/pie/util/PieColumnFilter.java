/*
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * -------------------------------------------------------------------
 *
 * History
 *    12.09.2007 (Tobias Koetter): created
 */

package org.knime.base.node.viz.pie.util;

import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.IntValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.util.ColumnFilter;


/**
 *
 * @author Tobias Koetter, University of Konstanz
 */
public final class PieColumnFilter implements ColumnFilter {

    private static PieColumnFilter instance;

    private static final int MAX_RANGE = 25;

    private PieColumnFilter() {
        //avoid object creation
    }

    /**
     * @return the only instance of this singleton
     */
    public static PieColumnFilter getInstance() {
        if (instance == null) {
            instance = new PieColumnFilter();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public String allFilteredMsg() {
        return "No column matches filter criteria";
    }

    /**
     * {@inheritDoc}
     */
    public boolean includeColumn(final DataColumnSpec colSpec) {
        if (colSpec == null) {
            throw new NullPointerException(
                    "Column specification must not be null");
        }
        final DataColumnDomain domain = colSpec.getDomain();
        if (domain == null) {
            return false;
        }
        if (colSpec.getType().isCompatible(NominalValue.class)) {
            if (domain.getValues() == null || domain.getValues().size() < 1) {
                return false;
            }
            return true;
        } else if (colSpec.getType().isCompatible(IntValue.class)) {
              if (domain.getLowerBound() == null
                      || domain.getUpperBound() == null) {
                  return false;
              }
              final int lower = ((IntCell)domain.getLowerBound()).getIntValue();
              final int upper = ((IntCell)domain.getUpperBound()).getIntValue();
              return (upper - lower < MAX_RANGE);
          }
        return false;
    }

}
