/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
 * -------------------------------------------------------------------
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
 *   25.11.2004 (ohl): created
 */
package de.unikn.knime.base.node.io.filereader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import de.unikn.knime.base.node.io.filetokenizer.Delimiter;
import de.unikn.knime.base.node.io.filetokenizer.FileTokenizerSettings;
import de.unikn.knime.core.data.DataTableSpec;
import de.unikn.knime.core.node.InvalidSettingsException;
import de.unikn.knime.core.node.NodeLogger;
import de.unikn.knime.core.node.NodeSettings;

/**
 * Contains all settings needed to read in a ASCII data file. This includes the
 * location of the data file, the settings for the tokenizer (like column
 * delimiter, comment patterns etc.) as well as the row headers and more. This
 * object combined with a <code>DataTableSpec</code> can be used to create a
 * <code>FileTable</code> from. A <code>FileTable</code> will represent then
 * the data of the file in a <code>DataTable</code>.
 * 
 * @author ohl, University of Konstanz
 */
public class FileReaderSettings extends FileTokenizerSettings {
    
    /** The node logger fot this class. */
    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(FileReaderSettings.class);

    /* the list of settings that are stored in here. */

    private URL m_dataFileLocation;

    /* the table name (derived from the filename if not overridden) */
    private String m_tableName;
    

    /*
     * if set, the first row in the file will be considered column names - and
     * discarded (we read rows, not column headers!)
     */
    private boolean m_fileHasColumnHeaders;

    /* delimiters ending a row, will be also added as token delimiters */
    private HashSet<String> m_rowDelimiters;

    /* true if the first column in the file should be considered a row header */
    private boolean m_fileHasRowHeaders;

    /* true if it ignores empty lines */
    private boolean m_ignoreEmptyLines;

    /*
     * if set, this will be used to generate a row header - and the one in the
     * file - if any - will be ignored
     */
    private String m_rowHeaderPrefix;

    /*
     * for each column a string (or null) that will be replaced - if read - with
     * a missing cell.
     */
    private Vector<String> m_missingPatterns;

    /**
     * this will be used if the file has not row headers and no row prefix is
     * set.
     */
    public static final String DEF_ROWPREFIX = "Row";

    /** key used to store data file location in a config object. */
    static final String CFGKEY_DATAURL = "DataURL";

    private static final String CFGKEY_HASCOL = "hasColHdr";

    private static final String CFGKEY_HASROW = "hasRowHdr";

    private static final String CFGKEY_ROWPREF = "rowPrefix";

    private static final String CFGKEY_IGNOREEMPTY = "ignoreEmptyLines";

    private static final String CFGKEY_ROWDELIMS = "RowDelims";

    private static final String CFGKEY_ROWDELIM = "RDelim";

    private static final String CFGKEY_RDCOMB = "SkipEmptyLine";

    private static final String CFGKEY_MISSINGS = "MissingPatterns";

    private static final String CFGKEY_MISSING = "MissPattern";
    
    private static final String CFGKEY_TABLENAME = "TableName";
    

    /**
     * Creates a new object holding all settings needed to read the specified
     * file. The file must be an ASCII representation of the data to read. We
     * are not specifying any default behaviour of that newly created object,
     * you really need to set all parameters before reading the file with these
     * settings.
     */
    public FileReaderSettings() {

        init();
    }

    // initializes private members. Needs to be called from two constructors.
    private void init() {
        m_dataFileLocation = null;
        m_tableName = null;

        m_fileHasColumnHeaders = false;
        m_fileHasRowHeaders = false;
        m_ignoreEmptyLines = false;

        m_rowHeaderPrefix = null;

        m_rowDelimiters = new HashSet<String>();
        m_missingPatterns = new Vector<String>();
    }

    /**
     * Creates a new FileReaderSettings object initializing its settings from
     * the passed config object.
     * 
     * @param cfg the config object containing all settings this object will be
     *            initialized with.
     * @throws InvalidSettingsException if the passed conf object contains
     *             invalid or insufficient settings.
     */
    public FileReaderSettings(final NodeSettings cfg) 
        throws InvalidSettingsException {

        // set the tokenizer settings first. The rowDelimiter reader depends
        // on the fact that the tokenizer reads its settings first.
        super(cfg);
        init();
        if (cfg != null) {
            try {
                URL dataFileLocation = new URL(cfg.getString(CFGKEY_DATAURL));
                setDataFileLocationAndUpdateTableName(dataFileLocation);
            } catch (MalformedURLException mfue) {
                throw new IllegalArgumentException(
                        "Cannot create URL of data file" + " from '"
                                + cfg.getString(CFGKEY_DATAURL)
                                + "' in filereader config");
            } catch (InvalidSettingsException ice) {
                throw new InvalidSettingsException("Illegal config object for "
                        + "file reader settings! Key '" + CFGKEY_DATAURL
                        + "' missing!");
            }
            // see if we got a tablename. For backwardcompatibility reasons
            // don't fail if its missing.
            try {
                setTableName(cfg.getString(CFGKEY_TABLENAME));
            } catch (InvalidSettingsException ise) {
                // when we set the data location (above) we already set a name
            }
            try {
                m_fileHasColumnHeaders = cfg.getBoolean(CFGKEY_HASCOL);
            } catch (InvalidSettingsException ice) {
                throw new InvalidSettingsException("Illegal config object for "
                        + "file reader settings! Key '" + CFGKEY_HASCOL
                        + "' missing!");
            }

            try {
                m_fileHasRowHeaders = cfg.getBoolean(CFGKEY_HASROW);
            } catch (InvalidSettingsException ice) {
                throw new InvalidSettingsException("Illegal config object for "
                        + "file reader settings! Key '" + CFGKEY_HASROW
                        + "' missing!");
            }

            try {
                m_ignoreEmptyLines = cfg.getBoolean(CFGKEY_IGNOREEMPTY);
            } catch (InvalidSettingsException ice) {
                throw new InvalidSettingsException("Illegal config object for "
                        + "file reader settings! Key '" + CFGKEY_IGNOREEMPTY
                        + "' missing!");
            }

            // set the row header prefix - if specified. It's optional.
            if (cfg.containsKey(CFGKEY_ROWPREF)) {
                try {
                    m_rowHeaderPrefix = cfg.getString(CFGKEY_ROWPREF);
                } catch (InvalidSettingsException ice) {
                    throw new InvalidSettingsException(
                            "Illegal config object for file"
                                    + " reader settings! Wrong type of key '"
                                    + CFGKEY_HASROW + "'!");
                }
            }

            if (cfg.containsKey(CFGKEY_MISSINGS)) {
                NodeSettings missPattConf;
                try {
                    missPattConf = cfg.getConfig(CFGKEY_MISSINGS);
                } catch (InvalidSettingsException ice) {
                    throw new InvalidSettingsException(
                            "Illegal config object for file "
                                    + "reader settings! Wrong type of key '"
                                    + CFGKEY_MISSINGS + "'!");
                }
                readMissingPatternsFromConfig(missPattConf);
            }

            NodeSettings rowDelimConf = null;
            try {
                rowDelimConf = cfg.getConfig(CFGKEY_ROWDELIMS);
            } catch (InvalidSettingsException ice) {
                throw new InvalidSettingsException(
                        "Illegal config object for file reader settings!"
                                + " Not existing or wrong type of key '"
                                + CFGKEY_ROWDELIMS + "'!");

            }

            readRowDelimitersFromConfig(rowDelimConf);

        } // if (cfg != null)

    }

    /**
     * Saves all settings into a <code>NodeSettings</code> object. Using the cfg
     * object to construct a new FileReaderSettings object should lead to an
     * object identical to this.
     * 
     * @param cfg the config object the settings are stored into.
     */
    public void saveToConfiguration(final NodeSettings cfg) {
        if (cfg == null) {
            throw new NullPointerException("Can't save 'file "
                    + "reader settings' to null config!");
        }

        super.saveToConfiguration(cfg);

        if (m_dataFileLocation != null) {
            cfg.addString(CFGKEY_DATAURL, m_dataFileLocation.toString());
        }
        cfg.addString(CFGKEY_TABLENAME, m_tableName);
        cfg.addBoolean(CFGKEY_HASCOL, m_fileHasColumnHeaders);
        cfg.addBoolean(CFGKEY_HASROW, m_fileHasRowHeaders);
        cfg.addBoolean(CFGKEY_IGNOREEMPTY, m_ignoreEmptyLines);

        if (m_rowHeaderPrefix != null) {
            cfg.addString(CFGKEY_ROWPREF, m_rowHeaderPrefix);
        }

        saveRowDelimitersToConfig(cfg.addConfig(CFGKEY_ROWDELIMS));
        saveMissingPatternsToConfig(cfg.addConfig(CFGKEY_MISSINGS));
    }

    /*
     * read the patterns, one for each column, that will be replaced by missing
     * cells from the configuration object.
     */
    private void readMissingPatternsFromConfig(
            final NodeSettings missPattConf) {
        if (missPattConf == null) {
            throw new NullPointerException(
                    "Can't read missing patterns from null config object");
        }
        int m = 0;
        for (String key : missPattConf.keySet()) {
            // they should all start with "MissPattern"...
            if (key.indexOf(CFGKEY_MISSING) != 0) {
                LOGGER.warn("Illegal missing pattern "
                        + "configuration '" + key + "'. Ignoring it!");
                continue;
            }
            try {
                String missi = missPattConf.getString(CFGKEY_MISSING + m);
                setMissingValueForColumn(m, missi);
            } catch (InvalidSettingsException ice) {
                LOGGER.warn("Illegal missing pattern "
                        + "configuration '" + key + "' (should be of type"
                        + " string). Ignoring it!");
                continue;
            }
            m++;
        }
    }

    /*
     * saves all currently set missing patterns for each column to a config
     * object
     */
    private void saveMissingPatternsToConfig(final NodeSettings cfg) {
        if (cfg == null) {
            throw new NullPointerException(
                    "Can't save missing patterns to null config object");
        }

        for (int m = 0; m < m_missingPatterns.size(); m++) {
            cfg.addString(CFGKEY_MISSING + m, m_missingPatterns.get(m));
        }

    }

    /*
     * reads the Row delimiters and settings from a config object or reads them
     * from it (next function). The crux with the row delimtiers is, that they
     * are ordinary delimiters for the file tokenizers (just returned as
     * separate token). Thus they will be read in already! - And they will be
     * saved before we save our row delimiters. So, we need to be a bit careful
     * here.
     */
    private void readRowDelimitersFromConfig(final NodeSettings rowDelims)
            throws InvalidSettingsException {

        for (int rowDelIdx = 0; rowDelims.containsKey(CFGKEY_ROWDELIM
                + rowDelIdx); rowDelIdx++) {

            boolean combine;
            String rowDelim;

            try {
                rowDelim = rowDelims.getString(CFGKEY_ROWDELIM + rowDelIdx);
            } catch (InvalidSettingsException ice) {
                LOGGER.warn("Invalid configuration for"
                        + " row delimiter '" + CFGKEY_ROWDELIM + rowDelIdx
                        + "' (must be of type string). Ignoring it!");
                continue;
            }

            if (rowDelims.containsKey(CFGKEY_RDCOMB + rowDelIdx)) {
                try {
                    combine = rowDelims.getBoolean(CFGKEY_RDCOMB + rowDelIdx);
                } catch (InvalidSettingsException ice) {
                    // shouldn't happen anyway
                    combine = false;
                }
            } else {
                combine = false;
            }

            // the row delimiter should already be set as delimiter (as the
            // super reads its settings first and all row delims are also
            // token delims).
            Delimiter delim = getDelimiterPattern(rowDelim);
            if (delim == null) {
                throw new InvalidSettingsException("Row delimiter must be "
                        + "defined as delimiter.");
            }
            if (!delim.returnAsToken()) {
                throw new InvalidSettingsException("Row delimiter must be "
                        + "returned as token.");
            }
            if (!(delim.combineConsecutiveDelims() == combine)) {
                throw new InvalidSettingsException("Delimiter definition "
                        + "doesn't match row delim definition.");
            }

            // we just add the pattern to the list of row delim patterns
            m_rowDelimiters.add(rowDelim);

        }
    }

    /*
     * See comment above previous method
     */
    private void saveRowDelimitersToConfig(final NodeSettings cfg) {
        if (cfg == null) {
            throw new NullPointerException("Can't save 'row delimiters' "
                    + "to null config!");
        }
        int rowDelIdx = 0;
        for (String rowDelim : m_rowDelimiters) {
            boolean combineMultiple;
            Delimiter delim = getDelimiterPattern(rowDelim);
            if (delim == null) {
                LOGGER.error("Row delimiter '" + rowDelim
                        + "' was not defined with the tokenizer.");
                LOGGER.error(
                        "Storing the property 'skip empty lines' "
                        + "with 'false'.");
                combineMultiple = false;
            } else {
                // we should really not include row delimiters in tokens but
                // always return them as separate token - otherwise we can't
                // recognize them when reading the file.
                assert !delim.includeInToken();
                assert delim.returnAsToken();
                combineMultiple = delim.combineConsecutiveDelims();
            }

            cfg.addString(CFGKEY_ROWDELIM + rowDelIdx, rowDelim);
            if (combineMultiple) {
                cfg.addBoolean(CFGKEY_RDCOMB + rowDelIdx, combineMultiple);
            }
            rowDelIdx++;
        }

    }

    /**
     * Sets the location of the file to read data from. Won't check correctness.
     * 
     * @param dataFileLocation the URL of the data file these settings are for
     */
    public void setDataFileLocationAndUpdateTableName(
            final URL dataFileLocation) {
        if (dataFileLocation == null) {
            setTableName("");
        } else {
            /* don't override a (possibly user set) name if it's 
             * not a new location */ 
            if (!dataFileLocation.equals(m_dataFileLocation)) {
                setTableName(getPureFileNameWithExtension(dataFileLocation));
            }
        }
        m_dataFileLocation = dataFileLocation;

    }

    /**
     * @return the location of the file these settings are meant for.
     */
    public URL getDataFileLocation() {
        return m_dataFileLocation;
    }

    /**
     * sets a new name for the table created by this node.
     * @param newName the new name to set. Valid names are not null.
     */
    public void setTableName(final String newName) {
        m_tableName = newName;
    }
    
    /**
     * @return the currently set name of the table created by this node. 
     *          Valid names are not null, but the method could return null, if
     *          no name was set yet.
     */
    public String getTableName() {
        return m_tableName;
    }

    /**
     * @param loc the location to extract the filename from.
     * @return the filename part of the URL without path. Or null if the URL is 
     *          null.  
     */
    private String getPureFileNameWithExtension(final URL loc) {
        if (loc != null) {
            String name = loc.getPath();
            int firstIdx = name.lastIndexOf('/') + 1;
            if (firstIdx == name.length()) {
                // last character is '/' ?!?! Filename is empty. Weird anyway.
                return "";
            }
            return name.substring(firstIdx);
        }
        return null;
    }
    
    /**
     * Tells whether the first line in the file should be considered column
     * headers, or not.
     * 
     * @param flag if true the first line in the file will not be considered
     *            data, but either ignored or used as column headers, depending
     *            on the column headers set (or not) in this object.
     */
    public void setFileHasColumnHeaders(final boolean flag) {
        m_fileHasColumnHeaders = flag;
    }

    /**
     * @return a flag telling if the first line in the file will not be
     *         considered data, but either ignored or used as column headers,
     *         depending on the column headers set (or not) in this object.
     */
    public boolean getFileHasColumnHeaders() {
        return m_fileHasColumnHeaders;
    }

    /**
     * Tells whether the first token in each line in the file should be
     * considered row header, or not.
     * 
     * @param flag if true the first item in each line in the file will not be
     *            considered data, but either ignored or used as row header,
     *            depending on the row header prefix set (or not) in this
     *            object.
     */
    public void setFileHasRowHeaders(final boolean flag) {
        m_fileHasRowHeaders = flag;
    }

    /**
     * @return a flag telling if the first item in each line in the file will
     *         not be considered data, but either ignored or used as row header,
     *         depending on the row header prefix set (or not) in this object.
     */
    public boolean getFileHasRowHeaders() {
        return m_fileHasRowHeaders;
    }

    /**
     * Set a string that will be used as a prefix for each row header. The
     * header generated will have the row number added to the prefix. This
     * prefix - if set - will be used, regardless of any row header read from
     * the file - if there is any.
     * 
     * @param rowPrefix the string that will be used to construct the header for
     *            each row. The actual row header will have the row number
     *            added. Specify <code>null</code> to clear the prefix.
     */
    public void setRowHeaderPrefix(final String rowPrefix) {
        m_rowHeaderPrefix = rowPrefix;
    }

    /**
     * @return the string that will be used to construct the header for each
     *         row. The actual row header will have the row number added. If
     *         this returns <code>null</code>, the row header from the file
     *         will be used - if any, otherwise the DEF_ROWPREFIX.
     */
    public String getRowHeaderPrefix() {
        return m_rowHeaderPrefix;
    }

    /**
     * Will add a delimiter pattern that will terminate a row. Row delimiters
     * are always token (=column) delimiters. Row delimiters will always be
     * returned as separate token by the filereader. You can define a row
     * delimiter that was previously defined a token delimiter. But only, if the
     * delimiter was not set to be included in the token. Otherwise you will get
     * a IllegalArgumentException.
     * 
     * @param rowDelimPattern the row delimiter pattern. Row delimiters will
     *            always be token delimiters and will always be returned as
     *            separate token.
     * @param skipEmptyRows if set true, multiple consecutive row delimiters
     *            will be combined and returned as one.
     */
    public void addRowDelimiter(final String rowDelimPattern,
            final boolean skipEmptyRows) {

        Delimiter existingDelim = getDelimiterPattern(rowDelimPattern);

        if (existingDelim != null) {
            if (existingDelim.includeInToken()) {
                // can't do that! Row delimiters need to be returned as
                // separate token. Can't include a delimiter in a token and
                // return it as separate token at the same time.
                throw new IllegalArgumentException("Can't define a row "
                        + "delimiter ('" + rowDelimPattern
                        + "') that was defined as token delimiter before"
                        + " that should be included in the tokens");
            }
        }

        Delimiter newDelim = new Delimiter(rowDelimPattern, skipEmptyRows,
                true, false);
        // returnAsSeparate, includeInToken);

        m_rowDelimiters.add(rowDelimPattern);
        addDelimiterPattern(newDelim);

    }

    /**
     * Removes the row delimiter with the specified pattern. Eventhough the
     * above method changes an existing column delimiter to being a row delim,
     * this function completely deletes the row delimiter (instead of being
     * aware that it might have been a col delim before and changing it back to
     * a col delim).
     * 
     * @param pattern the row delimiter to delete must not be null. Null is
     *            always a row delimiter.
     * @return a Delimiter object specifying the deleted delimiter, or null if
     *         no row delimiter with the pattern existed.
     */
    public Delimiter removeRowDelimiter(final String pattern) {
        if (pattern == null) {
            throw new NullPointerException(
                    "Can't remove <null> as row delimiter.");
        }
        if (isRowDelimiter(pattern)) {
            return removeDelimiterPattern(pattern);
        }
        return null;
    }

    /**
     * Blows away all defined row delimiters! After a call to this function no
     * row delimiter will be defined (except null).
     */
    public void removeAllRowDelimiters() {
        for (String delim : m_rowDelimiters) {
            removeDelimiterPattern(delim);
        }
        m_rowDelimiters.clear();
    }

    /**
     * @param pattern the pattern to test
     * @return true if the pattern is a row delimiter. null is always a row
     *         delimiter.
     */
    public boolean isRowDelimiter(final String pattern) {
        if (pattern == null) {
            return true;
        }
        assert (!m_rowDelimiters.contains(pattern))
                || (getDelimiterPattern(pattern) != null);
        return m_rowDelimiters.contains(pattern);

    }

    /**
     * @return true if the file reader ignores empty lines (or lines with only
     *         comment)
     */
    public boolean getIgnoreEmtpyLines() {
        return m_ignoreEmptyLines;
    }

    /**
     * @param ignoreEm pass true to have the file reader not return empty lines
     *            from the data file.
     */
    public void setIgnoreEmptyLines(final boolean ignoreEm) {
        m_ignoreEmptyLines = ignoreEm;
    }

    /**
     * returns true if the file reader combines multiple consecutive row
     * delimiters with this pattern (i.e. it skips empty rows if it finds
     * multiple if these (and only these) row delimiters). The method throws an
     * IllegalArgumentException at you if the specified pattern is not a row
     * delimiter.
     * 
     * @param pattern the pattern to test for.
     * @return true if the filereader skips empty rows for this row delimiter
     */
    public boolean combinesMultipleRowDelimiters(final String pattern) {
        if (!m_rowDelimiters.contains(pattern)) {
            throw new IllegalArgumentException("The specified pattern '"
                    + pattern + "' is not a row delimiter.");
        }

        return getDelimiterPattern(pattern).combineConsecutiveDelims();

    }

    /**
     * Specifies a pattern that, if read in for the specified column, will be
     * considered placeholder for a missing value, and the data table will
     * contain a missing cell instead of that value then.
     * 
     * @param colIdx the index of the column this missing value is set for.
     * @param pattern the pattern specifying the missing value in the data file
     *            for the specified column. Can be null to delete a previously
     *            set pattern.
     */
    public void setMissingValueForColumn(final int colIdx, 
            final String pattern) {
        if (m_missingPatterns.size() <= colIdx) {
            m_missingPatterns.setSize(colIdx + 1);
        }
        m_missingPatterns.set(colIdx, pattern);
    }

    /**
     * Returns the pattern that, if read in for the specified column, will be
     * considered placeholder for a missing value, and the data table will
     * contain a missing cell instead of that value then.
     * 
     * @param colIdx the index of the column the missing value is asked for.
     * @return the pattern that will be considered placeholder for a missing
     *         value in the specified column. Or null if no patern is set for
     *         that column.
     */
    public String getMissingValueOfColumn(final int colIdx) {
        if (m_missingPatterns.size() <= colIdx) {
            return null;
        }
        return m_missingPatterns.get(colIdx);
    }

    /**
     * Method to check consistency and completeness of the current settings. It
     * will return a <code>SettingsStatus</code> object which contains info,
     * warning and error messages. Or if the settings are alright it will return
     * null.
     * 
     * @param openDataFile tells wether or not this method should try to access
     *            the data file. This will - if set true - verify the
     *            accessability of the data.
     * @param tableSpec the spec of the DataTable these settings are for. If set
     *            null only a few checks will be performed - the ones that are
     *            possible without the knowledge of the structure of the table
     * @return a SettingsStatus object containing info, warning and error
     *         messages, or null if no messages were generated (i.e. all
     *         settings are just fine).
     */
    public SettingsStatus getStatusOfSettings(final boolean openDataFile,
            final DataTableSpec tableSpec) {

        SettingsStatus status = new SettingsStatus();

        addStatusOfSettings(status, openDataFile, tableSpec);

        return status;
    }

    /**
     * Overriding super's method.
     * 
     * @see FileTokenizerSettings#getStatusOfSettings()
     */
    public SettingsStatus getStatusOfSettings() {
        return getStatusOfSettings(false, null);
    }

    /**
     * adds its status messages to a passed status object.
     * 
     * @param status the object to add messages to - if any.
     * @param openDataFile specifies if we should check the accessability of the
     *            data file.
     * @param tableSpec the spec of the DataTable these settings are for. If set
     *            null only a few checks will be performed - the ones that are
     *            possible without the knowledge of the structure of the table
     */
    protected void addStatusOfSettings(final SettingsStatus status,
            final boolean openDataFile, final DataTableSpec tableSpec) {

        // check the data file location. It's required.
        if (m_dataFileLocation == null) {
            status.addError("No data file location specified.");
        } else {
            // see if we can access the data file - if permitted.
            if (openDataFile) {
                try {
                    if (m_dataFileLocation.openStream() == null) {
                        status.addError("I/O Error while connecting to '"
                                + m_dataFileLocation.toString() + "'.");
                    }
                } catch (IOException ioe) {
                    status.addError("I/O Error while connecting to '"
                            + m_dataFileLocation.toString() + "'.");
                } catch (NullPointerException npe) {
                    // thats a bug in the windows open stream
                    // a path like c:\blah\ \ (space as dir) causes a NPE.
                    status.addError("I/O Error while connecting to '"
                            + m_dataFileLocation.toString() + "'.");
                }
            }
        }

        if (m_tableName == null) {
            status.addError("No table name set.");
        }

        // check the row headers.
        if (!m_fileHasRowHeaders) {
            // we tell them when we would use the default row header.
            if (m_rowHeaderPrefix == null) {
                status.addInfo("The default row header ('" + DEF_ROWPREFIX
                        + "+RowIdx') will be used as no row header prefix"
                        + " is specified and the file doesn't contain row"
                        + " headers.");
            }
        } else {
            if (m_rowHeaderPrefix != null) {
                status.addInfo("The specified row header will be used"
                        + " overriding the ones in the data file.");
            }
        }

        // check the row delimiters
        for (String rowDelim : m_rowDelimiters) {
            Delimiter delim = getDelimiterPattern(rowDelim);
            if (delim == null) {
                status.addError("Row delimiter '" + rowDelim
                        + " is not defined" + "being a token delimiter.");
                continue;
            }
            if (delim.includeInToken()) {
                status.addError("Row delimiter '" + rowDelim + "' is set to"
                        + " be included in the token.");
            }
            if (!delim.returnAsToken()) {
                status
                        .addError("Row delimiter '" + rowDelim
                                + "' is not set to"
                                + " be returned as separate token.");
            }
        }
        if (m_rowDelimiters.size() == 0) {
            status.addWarning("No row delimiters are defined! The table will"
                    + " be read into one row (and supernumerous cells will"
                    + " be ignored).");
        }

        // check missing patterns
        if (tableSpec != null) {
            int numCols = tableSpec.getNumColumns();
            if (numCols > m_missingPatterns.size()) {
                status.addInfo("Not all columns have patterns for missing"
                        + "values assigned.");
            } else if (numCols < m_missingPatterns.size()) {
                status.addError("There are more patterns for missing values"
                        + " defined than columns in the table.");
            } else {
                for (Iterator pIter = m_missingPatterns.iterator(); pIter
                        .hasNext();) {
                    if (pIter.next() == null) {
                        status.addInfo("Not all columns have patterns for "
                                + "missing values assigned.");
                    }
                    // adding the message once is enough
                    break;
                }
            }
        } else {
            for (Iterator pIter = m_missingPatterns.iterator(); 
                pIter.hasNext();) {
                if (pIter.next() == null) {
                    status.addInfo("Not all columns have patterns for missing"
                            + " values assigned.");
                }
                // adding the message once is enough
                break;
            }
        }

        // let the filetokenizer add its blurb
        super.addStatusOfSettings(status);

    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
       StringBuffer res = new StringBuffer(super.toString());
       res.append("\nReading from:'");
       if (m_dataFileLocation == null) {
           res.append("<null>");
       } else {
           res.append(m_dataFileLocation.toString());
       }
       res.append("'\n");
       res.append("RowPrefix:");
       res.append(m_rowHeaderPrefix + "\n");
       res.append("RowHeaders:" + m_fileHasRowHeaders);
       res.append(", ColHeaders:" + m_fileHasColumnHeaders);
       res.append(", Ignore empty lines:" + m_ignoreEmptyLines + "\n");
       res.append("Row delimiters: ");
       for (Iterator r = m_rowDelimiters.iterator(); r.hasNext();) {
           res.append(printableStr((String)r.next()));
           if (r.hasNext()) {
               res.append(", ");
           }
       }
       res.append("\n");
       res.append("MissValue patterns: ");
       for (int p = 0; p < m_missingPatterns.size(); p++) {
           res.append(m_missingPatterns.get(p));
           if (p < m_missingPatterns.size() - 1) {
               res.append(", ");
           }
       }
       res.append("\n");
       return res.toString();
    }
}
