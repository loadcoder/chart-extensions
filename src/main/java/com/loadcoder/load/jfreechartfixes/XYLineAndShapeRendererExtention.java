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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

public abstract class XYLineAndShapeRendererExtention extends XYLineAndShapeRenderer {

	protected XYSeriesCollection seriesCollection;

	public XYLineAndShapeRendererExtention(boolean lines, boolean shapes, XYSeriesCollection seriesCollection) {
		super(lines, shapes);
		this.seriesCollection = seriesCollection;
	}

	/* 
	 * The purpose of this override is to change the behaviour for the visibility of the legend
	 * and how the color of the legend is set.
	 */
	@Override
	public LegendItem getLegendItem(int datasetIndex, int series) {
		XYPlot plot = getPlot();
		if (plot == null) {
			return null;
		}

		XYDataset dataset = plot.getDataset(datasetIndex);
		if (dataset == null) {
			return null;
		}

		//jfreechart diff: set the line paint with the implementation of abstract getLinePaint
		Paint linePaint = getLinePaint(series);

		String label = getLegendItemLabelGenerator().generateLabel(dataset, 
				series);
		String description = label;
		String toolTipText = null;
		if (getLegendItemToolTipGenerator() != null) {
			toolTipText = getLegendItemToolTipGenerator().generateLabel(
					dataset, series);
		}
		String urlText = null;
		if (getLegendItemURLGenerator() != null) {
			urlText = getLegendItemURLGenerator().generateLabel(dataset, 
					series);
		}
		boolean shapeIsVisible = getItemShapeVisible(series, 0);
		Shape shape = lookupLegendShape(series);
		boolean shapeIsFilled = getItemShapeFilled(series, 0);
		Paint fillPaint = (this.getUseFillPaint() ? lookupSeriesFillPaint(series) 
				: linePaint);
		boolean shapeOutlineVisible = this.getDrawOutlines();
		Paint outlinePaint = (this.getUseOutlinePaint() ? lookupSeriesOutlinePaint(
				series) : linePaint);
		Stroke outlineStroke = lookupSeriesOutlineStroke(series);
		boolean lineVisible = getItemLineVisible(series, 0);
		Stroke lineStroke = lookupSeriesStroke(series);

		LegendItem result = new LegendItem(label, description, toolTipText, urlText, shapeIsVisible, shape,
				shapeIsFilled, fillPaint, shapeOutlineVisible, outlinePaint, outlineStroke, lineVisible,
				this.getLegendLine(), lineStroke, linePaint);
		result.setLabelFont(lookupLegendTextFont(series));
		Paint labelPaint = lookupLegendTextPaint(series);
		if (labelPaint != null) {
			result.setLabelPaint(labelPaint);
		}
		result.setSeriesKey(dataset.getSeriesKey(series));
		result.setSeriesIndex(series);
		result.setDataset(dataset);
		result.setDatasetIndex(datasetIndex);
		return result;
	}

	public abstract Paint getLinePaint(int series);

	/*
	 * Overriding this class since the color of the series needs to be set with getLinePaint
	 * which makes it possible to set the color for the series in the series instance
	 */
	@Override
	protected void drawFirstPassShape(Graphics2D g2, int pass, int series, int item, Shape shape) {
		g2.setStroke(getItemStroke(series, item));
		g2.setPaint(getLinePaint(series)); // this line is different from the original
		g2.draw(shape);
	}
}
