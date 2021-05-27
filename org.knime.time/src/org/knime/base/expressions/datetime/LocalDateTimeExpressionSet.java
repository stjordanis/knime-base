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
package org.knime.base.expressions.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.knime.expressions.core.Expression;
import org.knime.expressions.core.ExpressionSet;

/**
 * An {@link ExpressionSet} for date and time expressions.
 *
 * @author Moritz Heine, KNIME GmbH, Konstanz, Germany
 */
public class LocalDateTimeExpressionSet implements ExpressionSet {

    static final String NAME = "Date&Time";
    private List<DateTimeExpression> m_expression;

    /**
     * Creates a set containing Data&Time expressions.
     */
    public LocalDateTimeExpressionSet() {
        m_expression = new ArrayList<>();

        m_expression.add(
            new SimpleDateTimeExpression("getDate", "Returns the LocalDate part of the input LocatDateTime object.",
                LocalDate.class, new String[]{"localDateTime"}));
        m_expression.add(
            new SimpleDateTimeExpression("getTime", "Returns the LocalTime part of the input LocatDateTime object.",
                LocalTime.class, new String[]{"localDateTime"}));
        m_expression.add(new TemporalExtractorDateTimeExpression(ChronoField.DAY_OF_WEEK));
        m_expression.add(new SimpleDateTimeExpression("getDayOfWeekName",
            "Creates an object of type String with the English name of the day of the given LocalDate value.",
            String.class, new String[]{"temporal"}));
        m_expression.add(new TemporalExtractorDateTimeExpression(ChronoField.DAY_OF_MONTH));
        m_expression.add(new TemporalExtractorDateTimeExpression(ChronoField.DAY_OF_YEAR));
        m_expression.add(new TemporalExtractorDateTimeExpression(ChronoField.MONTH_OF_YEAR));
        m_expression.add(new SimpleDateTimeExpression("getMonthOfYearName",
            "Creates an object of type String with the English name of the month of the given LocalDate value.",
            String.class, new String[]{"temporal"}));
        m_expression.add(new TemporalExtractorDateTimeExpression(ChronoField.YEAR));
        m_expression.add(new LocalDateFromString());
        m_expression.add(new LocalDateFromYMD());
        m_expression.add(new LocalDateTimeFromString());
        m_expression.add(new LocalDateTimeFromYMDHMS());
        m_expression.add(new LocalTimeFromHMS());
        m_expression.add(new LocalTimeFromString());
        m_expression.add(new SimpleDateTimeExpression("now",
            "Creates an object of type LocalDateTime with the current date and time.", LocalDateTime.class));
        m_expression.add(new DurationOfHours());
        m_expression.add(new DurationOfMinutes());
        m_expression.add(new DurationOfSeconds());
        m_expression.add(new PeriodOfDays());
        m_expression.add(new PeriodOfMonths());
        m_expression.add(new PeriodOfWeeks());
        m_expression.add(new PeriodOfYears());
        m_expression.add(new PlusTemporal());
        m_expression.add(new SimpleDateTimeExpression("today",
            "Creates an object of type LocalDate with the current date.", LocalDate.class));

        Comparator<Expression> comparator = (x, y) -> x.getName().compareTo(y.getName());

        m_expression.sort(comparator.thenComparing((x, y) -> Integer.compare(x.getNrArgs(), y.getNrArgs()))
                .thenComparing((x, y) -> x.getDisplayName().compareTo(y.getDisplayName())));
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategory() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends Expression> getExpressions() {
        return m_expression;
    }

}
