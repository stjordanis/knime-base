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
 *   May 26, 2021 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.node.table.reader.config.tablespec;

import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.filehandling.core.node.table.reader.selector.ImmutableUnknownColumnsTransformation;
import org.knime.filehandling.core.node.table.reader.selector.UnknownColumnsTransformation;

/**
 * Serializer for {@link UnknownColumnsTransformation}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class UnknownColumnsTransformationSerializer {

    private static final String CFG_FORCED_TYPE = "forced_type";

    private static final String CFG_FORCE_TYPE = "force_type";

    private static final String CFG_KEEP = "keep";

    private static final String CFG_POSITION = "position";

    private UnknownColumnsTransformationSerializer() {

    }

    static void save(final UnknownColumnsTransformation unknownColumnsTransformation, final NodeSettingsWO settings) {
        settings.addInt(CFG_POSITION, unknownColumnsTransformation.getPosition());
        settings.addBoolean(CFG_KEEP, unknownColumnsTransformation.keep());
        settings.addBoolean(CFG_FORCE_TYPE, unknownColumnsTransformation.forceType());
        final DataType forcedType = unknownColumnsTransformation.getForcedType();
        if (forcedType != null) {
            forcedType.save(settings.addNodeSettings(CFG_FORCED_TYPE));
        }
    }

    static ImmutableUnknownColumnsTransformation load(final NodeSettingsRO settings) throws InvalidSettingsException {
        final int position = settings.getInt(CFG_POSITION);
        final boolean keep = settings.getBoolean(CFG_KEEP);
        final boolean forceType = settings.getBoolean(CFG_FORCE_TYPE);
        final DataType forcedType;
        if (settings.containsKey(CFG_FORCED_TYPE)) {
            forcedType = DataType.load(settings.getNodeSettings(CFG_FORCED_TYPE));
        } else {
            forcedType = null;
        }
        return new ImmutableUnknownColumnsTransformation(position, keep, forceType, forcedType);
    }
}
