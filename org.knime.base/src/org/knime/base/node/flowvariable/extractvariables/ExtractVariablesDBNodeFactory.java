/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   Apr 28, 2008 (wiswedel): created
 */
package org.knime.base.node.flowvariable.extractvariables;

import org.knime.core.node.port.database.DatabasePortObject;


/**
 * 
 * @author wiswedel, University of Konstanz
 */
public class ExtractVariablesDBNodeFactory extends ExtractVariablesNodeFactory {
    
    /** Creates factory for {@link DatabasePortObject} type ports. */
    public ExtractVariablesDBNodeFactory() {
        super(DatabasePortObject.TYPE);
    }
    
}
