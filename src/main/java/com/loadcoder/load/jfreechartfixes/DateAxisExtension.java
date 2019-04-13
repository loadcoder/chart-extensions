/*
 * ===========================================
 * THIS FILE CONTAINS COPIED AND MODIFIED CODE FROM THE JFREECHART LIBRARY
 * http://www.jfree.org/jfreechart/index.html
 * ===========================================
 *
 * Copyright (C) 2018 Stefan Vahlgren at Loadcoder
 * (C) Copyright 2000-2014, by Object Refinery Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * (C) Copyright 2004-2014, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Stefan Vahlgren (at Loadcoder)
 */
package com.loadcoder.load.jfreechartfixes;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTick;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Year;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateAxisExtension extends DateAxis {

	Logger log = LoggerFactory.getLogger(DateAxisExtension.class);

	protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		List result = new ArrayList();

		Font tickLabelFont = getTickLabelFont();
		g2.setFont(tickLabelFont);

		if (isAutoTickUnitSelection()) {
			selectAutoTickUnit(g2, dataArea, edge);
		}

		DateTickUnit unit = getTickUnit();

		/*
		 * jfreechart diff. There is a bug in jfreechart. Following if statement will
		 * due as a workaround in loadcoder until the bug is properly solved.
		 */
		if (unit.getUnitType().equals(DateTickUnitType.MILLISECOND)) {
			log.trace("Protection from problem in DateAxis. Can safely be ignored");
			return result;
		}

		/*
		 * These two methods invocations suffers from the bug. The variables used to
		 * produce the two dates has to be done in a serialization block so that they
		 * are not updated between the calls.
		 */
		Date tickDate = calculateLowestVisibleTickValue(unit);
		Date upperDate = getMaximumDate();

		boolean hasRolled = false;
		while (tickDate.before(upperDate)) {
			if (!(hasRolled)) {
				tickDate = correctTickDateForPosition(tickDate, unit, getTickMarkPosition());
			}

			long lowestTickTime = tickDate.getTime();
			long distance = unit.addToDate(tickDate, getTimeZone()).getTime() - lowestTickTime;

			int minorTickSpaces = getMinorTickCount();
			if (minorTickSpaces <= 0) {
				minorTickSpaces = unit.getMinorTickCount();
			}
			for (int minorTick = 1; minorTick < minorTickSpaces; ++minorTick) {
				long minorTickTime = lowestTickTime - (distance * minorTick / minorTickSpaces);

				if ((minorTickTime <= 0L) || (!(getRange().contains(minorTickTime))) || (isHiddenValue(minorTickTime)))
					continue;
				result.add(new DateTick(TickType.MINOR, new Date(minorTickTime), "", TextAnchor.TOP_CENTER,
						TextAnchor.CENTER, 0.0D));

			}
			long currentTickTime;
			long nextTickTime;
			int minorTick;
			if (!(isHiddenValue(tickDate.getTime())))

			{
				DateFormat formatter = getDateFormatOverride();
				String tickLabel;
				if (formatter != null) {
					tickLabel = formatter.format(tickDate);
				} else {
					tickLabel = getTickUnit().dateToString(tickDate);
				}

				double angle = 0.0D;
				TextAnchor rotationAnchor;
				TextAnchor anchor;
				if (isVerticalTickLabels()) {
					anchor = TextAnchor.CENTER_RIGHT;
					rotationAnchor = TextAnchor.CENTER_RIGHT;
					if (edge == RectangleEdge.TOP) {
						angle = 1.570796326794897D;
					} else
						angle = -1.570796326794897D;
				} else {
					// TextAnchor rotationAnchor;
					if (edge == RectangleEdge.TOP) {
						anchor = TextAnchor.BOTTOM_CENTER;
						rotationAnchor = TextAnchor.BOTTOM_CENTER;
					} else {
						anchor = TextAnchor.TOP_CENTER;
						rotationAnchor = TextAnchor.TOP_CENTER;
					}
				}

				Tick tick = new DateTick(tickDate, tickLabel, anchor, rotationAnchor, angle);

				result.add(tick);
				hasRolled = false;

				currentTickTime = tickDate.getTime();
				tickDate = unit.addToDate(tickDate, getTimeZone());
				nextTickTime = tickDate.getTime();

				for (minorTick = 1; minorTick < minorTickSpaces; ++minorTick) {

					long minorTickTime = currentTickTime
							+ (nextTickTime - currentTickTime) * minorTick / minorTickSpaces;

					if ((getRange().contains(minorTickTime)) && (!(isHiddenValue(minorTickTime)))) {
						result.add(new DateTick(TickType.MINOR, new Date(minorTickTime), "", TextAnchor.TOP_CENTER,
								TextAnchor.CENTER, 0.0D));
					}
				}

			} else {
				tickDate = unit.rollDate(tickDate, getTimeZone());
				hasRolled = true;
				continue;
			}
		}
		return result;
	}

	private Date correctTickDateForPosition(Date time, DateTickUnit unit, DateTickMarkPosition position) {
		Date result = time;
		switch (unit.getUnit()) {
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			break;
		case 1:
			result = calculateDateForPosition(new Month(time, getTimeZone(), getLocale()), position);

			break;
		case 0:
			result = calculateDateForPosition(new Year(time, getTimeZone(), getLocale()), position);

		}

		return result;
	}

	private Date calculateDateForPosition(RegularTimePeriod period, DateTickMarkPosition position) {
		ParamChecks.nullNotPermitted(period, "period");
		Date result = null;
		if (position == DateTickMarkPosition.START) {
			result = new Date(period.getFirstMillisecond());
		} else if (position == DateTickMarkPosition.MIDDLE) {
			result = new Date(period.getMiddleMillisecond());
		} else if (position == DateTickMarkPosition.END) {
			result = new Date(period.getLastMillisecond());
		}
		return result;
	}
}
