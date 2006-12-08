/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   30.08.2006 (Fabian Dill): created
 */
package org.knime.base.node.viz.plotter.columns;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.viz.plotter.AbstractDrawingPane;
import org.knime.base.node.viz.plotter.AbstractPlotterProperties;
import org.knime.base.node.viz.plotter.Axis;
import org.knime.base.node.viz.plotter.DataProvider;
import org.knime.base.node.viz.plotter.basic.BasicDrawingPane;
import org.knime.base.node.viz.plotter.basic.BasicPlotter;
import org.knime.base.util.coordinate.Coordinate;
import org.knime.base.util.coordinate.NumericCoordinate;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeLogger;

/**
 * Wraps the functionality where the data of two columns have to be displayed.
 * It registeres the appropriate listeners to the column selection and calls the
 * corresponding methods dependend on whether the model has changed
 * ({@link #updatePaintModel()}) or the ranges have changed 
 * ({@link #updateSize()}). If only columns which are compatible to certain 
 * {@link org.knime.core.data.DataValue}s should be selectable, an instance of 
 * the {@link org.knime.base.node.viz.plotter.columns.TwoColumnProperties} has 
 * to be created, where the restricting {@link org.knime.core.data.DataValue}s
 *  can be passed to the constructor.
 * 
 * @author Fabian Dill, University of Konstanz
 */
public abstract class TwoColumnPlotter extends BasicPlotter {
    
    private static final NodeLogger LOGGER = NodeLogger.getLogger(
            TwoColumnPlotter.class);
    
    private DataColumnSpec m_selectedXColumn;
    
    private DataColumnSpec m_selectedYColumn;
    
    private DataTableSpec m_spec;
    

    /**
     * Constructor for extending classes.
     * 
     * @param panel the drawing pane
     * @param properties the properties
     */
    public TwoColumnPlotter(final AbstractDrawingPane panel, 
            final AbstractPlotterProperties properties) {
        super(panel, properties);
        if (properties instanceof TwoColumnProperties) {
            TwoColumnProperties properties2D = 
                (TwoColumnProperties)properties;
            properties2D.addXColumnListener(new ItemListener() {

                /**
                 * @see java.awt.event.ItemListener#itemStateChanged(
                 *      java.awt.event.ItemEvent)
                 */
                public void itemStateChanged(final ItemEvent e) {
                    DataColumnSpec x = ((TwoColumnProperties)
                            getProperties()).getSelectedXColumn();
                    if (x != null) {
                        xColumnChanged(x);
                    }
                }
            });
            properties2D.addYColumnListener(new ItemListener() {

                /**
                 * @see java.awt.event.ItemListener#itemStateChanged(
                 *      java.awt.event.ItemEvent)
                 */
                public void itemStateChanged(final ItemEvent e) {
                    DataColumnSpec y = ((TwoColumnProperties)
                            getProperties()).getSelectedYColumn();
                    if (y != null) {
                        yColumnChanged(y);
                    }
                }
            });
        }
        setXAxis(new Axis(Axis.HORIZONTAL, getDrawingPaneDimension().width));
        setYAxis(new Axis(Axis.VERTICAL, getDrawingPaneDimension().height));
        addRangeListener();
    }
    
    /**
     * Default two column plotter with 
     * {@link org.knime.base.node.viz.plotter.basic.BasicDrawingPane} and 
     * {@link TwoColumnProperties}.
     *
     */
    public TwoColumnPlotter() {
        this(new BasicDrawingPane(), new TwoColumnProperties());
    }
    
    /**
     * Adds the range listener to the range adjustment component of the 
     * properties.
     *
     */
    private void addRangeListener() {
        if (!(getProperties() instanceof TwoColumnProperties)) {
            return;
        }
        final TwoColumnProperties props = (TwoColumnProperties)getProperties();
        props.addXMinListener(new ChangeListener() {
            /**
             * @see javax.swing.event.ChangeListener#stateChanged(
             * javax.swing.event.ChangeEvent)
             */
            public void stateChanged(final ChangeEvent e) {
                double newXMin = props.getXMinValue();
                ((NumericCoordinate)getXAxis().getCoordinate())
                    .setMinDomainValue(newXMin);
                updateSize();
                getXAxis().repaint();
            }
            
        });
        props.addXMaxListener(new ChangeListener() {
            /**
             * @see javax.swing.event.ChangeListener#stateChanged(
             * javax.swing.event.ChangeEvent)
             */
            public void stateChanged(final ChangeEvent e) {
                double newXMax = props.getXMaxValue();
                ((NumericCoordinate)getXAxis().getCoordinate())
                    .setMaxDomainValue(newXMax);
                updateSize();
                getXAxis().repaint();
            }
            
        });
        props.addYMinListener(new ChangeListener() {
            /**
             * @see javax.swing.event.ChangeListener#stateChanged(
             * javax.swing.event.ChangeEvent)
             */
            public void stateChanged(final ChangeEvent e) {
                double newYMin = props.getYMinValue();
                ((NumericCoordinate)getYAxis().getCoordinate())
                    .setMinDomainValue(newYMin);
                updateSize();
                getYAxis().repaint();
            }
            
        });
        props.addYMaxListener(new ChangeListener() {
            /**
             * @see javax.swing.event.ChangeListener#stateChanged(
             * javax.swing.event.ChangeEvent)
             */
            public void stateChanged(final ChangeEvent e) {
                double newYMax = props.getYMaxValue();
                ((NumericCoordinate)getYAxis().getCoordinate())
                    .setMaxDomainValue(newYMax);
                updateSize();
                getYAxis().repaint();
            }
            
        });
    }
    
    /**
     * Updates the coordinates for both columns and calls  
     * {@link #updatePaintModel()}.
     * 
     * @param newXColumn the new {@link org.knime.core.data.DataColumnSpec}. 
     */
    private synchronized void xColumnChanged(final DataColumnSpec newXColumn) {
        if (!newXColumn.equals(m_selectedXColumn)) {
            LOGGER.debug("x column changed: " 
                    + newXColumn.getName());
            m_selectedXColumn = newXColumn;
            getXAxis().setCoordinate(Coordinate.createCoordinate(
                    m_selectedXColumn));
            getYAxis().setCoordinate(Coordinate.createCoordinate(
                    m_selectedYColumn));
            ((TwoColumnProperties)getProperties()).updateRangeSpinner(
                    m_selectedXColumn, m_selectedYColumn);
            updatePaintModel();
        }
    }
    
    /**
     * Updates the coordinates for both columns and calls  
     * {@link #updatePaintModel()}.
     * 
     * @param newYColumn the new {@link org.knime.core.data.DataColumnSpec}. 
     */
    private synchronized void yColumnChanged(final DataColumnSpec newYColumn) {
        if (!newYColumn.equals(m_selectedYColumn)) {
            LOGGER.debug("y column changed: " 
                    + newYColumn.getName());
            m_selectedYColumn = newYColumn;
//            m_yAxis.setPreferredLength(getDrawingPaneDimension().height);
            getYAxis().setCoordinate(Coordinate.createCoordinate(
                    m_selectedYColumn));
            getXAxis().setCoordinate(Coordinate.createCoordinate(
                    m_selectedXColumn));
            ((TwoColumnProperties)getProperties()).updateRangeSpinner(
                    m_selectedXColumn, m_selectedYColumn);
            updatePaintModel();
        }
    }
    
    /**
     * 
     * @return the currently selecteed x column.
     */
    public DataColumnSpec getSelectedXColumn() {
        return m_selectedXColumn;
    }
    
    /**
     * Returns the selected column index or 0 by default, that is if no 
     * {@link org.knime.core.data.DataTableSpec} is set and no column was 
     * selected before. If the stored {@link org.knime.core.data.DataTableSpec}
     * doesn't find the selected column name (which should never happen), 
     * then -1 is returned. 
     * 
     * @return selected x column index or -1
     */
    public int getSelectedXColumnIndex() {
        if (m_spec != null && getSelectedXColumn() != null) {
            return m_spec.findColumnIndex(getSelectedXColumn().getName());
        }
        return 0;
    }
    
    /**
     * Returns the selected column index or 0 by default, that is if no 
     * {@link org.knime.core.data.DataTableSpec} is set and no column was 
     * selected before. If the stored {@link org.knime.core.data.DataTableSpec}
     * doesn't find the selected column name (which should never happen), 
     * then -1 is returned. 
     * 
     * @return selected y column index or -1
     */
    public int getSelectedYColumnIndex() {
        if (m_spec != null && getSelectedYColumn() != null) {
            return m_spec.findColumnIndex(getSelectedYColumn().getName());    
        }
        return 1;
    }
    
    
    /**
     * 
     * @return the currently selected y column
     */
    public DataColumnSpec getSelectedYColumn() {
        return m_selectedYColumn;
    }
    
    /**
     * Updates the select boxes for the x and y columns.
     * @param spec the current data table spec.
     */
    public void setSelectableColumns(final DataTableSpec spec) {
        m_spec = spec;
        if (getProperties() instanceof TwoColumnProperties) {
            ((TwoColumnProperties)getProperties()).update(spec);
        }
    }
    
    
    /**
     * Updates the column selection with the 
     * {@link org.knime.core.data.DataTableSpec} of the 
     * {@link org.knime.base.node.util.DataArray} with index 0.
     * 
     * @see org.knime.base.node.viz.plotter.AbstractPlotter#setDataProvider(
     * DataProvider)
     */
    @Override
    public void setDataProvider(final DataProvider provider) {
        super.setDataProvider(provider);
        if (getDataProvider() != null 
                && getDataProvider().getDataArray(0) != null) {
            setSelectableColumns(getDataProvider().getDataArray(0)
                    .getDataTableSpec());
        }
    }
        
    
    /**
     * This method is called whenever the column selection has changed and
     * the view model has to be adapted to the currently selected columns.
     *
     */
    @Override
    public abstract void updatePaintModel();
     
    
}
