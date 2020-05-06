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
 */
package org.knime.filehandling.core.testing.integrationtests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.knime.filehandling.core.testing.FSTestInitializer;
import org.knime.filehandling.core.testing.FSTestInitializerManager;
import org.knime.filehandling.core.testing.FSTestInitializerProvider;

/**
 * Helper class which parameterizes the {@link AbstractParameterizedFSTest} class.
 * 
 * Automatically detects all registered {@link FSTestInitializerProvider} implementations and uses them to initialize
 * and configure corresponding {@link FSTestInitializer}. The configuration is a properties file resolved by {link
 * {@link FSTestPropertiesResolver}.
 * 
 * @author Tobias Urhaug, KNIME GmbH, Berlin, Germany
 */
public class FSTestParameters {

    /**
     * Returns a collection with a single two dimensional array, where each row in the array contains a file system name
     * (which is helpful for naming the parameterized tests) and the corresponding test initializer.
     * 
     * @return all registered test initializers in a format suitable for the Parameterized runner
     */
    public static Collection<Object[]> get() {
        final Properties fsTestProperties = FSTestPropertiesResolver.forIntegrationTests();

        final FSTestInitializerManager manager = FSTestInitializerManager.instance();
        final List<String> testInitializerKeys = new ArrayList<>();
        if (fsTestProperties.containsKey("test-fs")) {
            testInitializerKeys.add(fsTestProperties.getProperty("test-fs"));
        } else {
            testInitializerKeys.addAll(manager.getAllTestInitializerKeys());
        }

        final int numberOfFS = testInitializerKeys.size();
        final Object[][] fsTestInitializers = new Object[numberOfFS][2];

        for (int i = 0; i < numberOfFS; i++) {
            final String fsType = testInitializerKeys.get(i);
            fsTestInitializers[i][0] = fsType;
            fsTestInitializers[i][1] =
                manager.createInitializer(fsType, FSTestConfigurationReader.read(fsType, fsTestProperties));
        }

        return Arrays.asList(fsTestInitializers);
    }
}
