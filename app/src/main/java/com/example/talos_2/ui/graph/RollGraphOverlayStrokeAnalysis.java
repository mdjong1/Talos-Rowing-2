/*
 * Copyright (c) 2012 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.talos_2.ui.graph;

import com.example.talos_2.common.filter.LowpassFilter;
import com.example.talos_2.ui.PaintStyle;
import com.example.talos_2.ui.RSCanvas;
import com.example.talos_2.ui.RSPaint;
import com.example.talos_2.ui.RSRect;
import com.example.talos_2.ui.UILiaison;

public class RollGraphOverlayStrokeAnalysis  {
	
	private static final float Y_SCALE = 8f;

	private static final double ROLL_PANNEL_DIM_FACTOR = 0.60;
	
	private final int rollAccumSize = 1;
	private int rollAccumCount;
	private float rollAccum;
	
	private final LowpassFilter filter = new LowpassFilter(.5f);
	
	private long rollAccumTimestamp;

	private final com.example.talos_2.ui.UILiaison uiFactory;
	
	private final RollOverlayType rollOverlayType = RollOverlayType.TOP;

	private final CyclicArrayXYSeries rollPanelSeries;

	private final MultiXYSeries multySeries;

	@SuppressWarnings("serial")
	public RollGraphOverlayStrokeAnalysis(com.example.talos_2.ui.UILiaison uiFactory, MultiXYSeries multySeries) {
		this.uiFactory = uiFactory;
		this.multySeries = multySeries;
		
		rollPanelSeries = new CyclicArrayXYSeries(multySeries.xMode, new XYSeries.Renderer(uiFactory.createPaint())) {
				{
					setIndependantYAxis(true);				
				}
			};
		multySeries.addSeries(rollPanelSeries, false);

	}
	
	void drawRollPanels(com.example.talos_2.ui.RSCanvas canvas, com.example.talos_2.ui.RSRect rect, double xAxisSize) {
		XYSeries ser = rollPanelSeries;
		
		final int len = ser.getItemCount();
		
		if (len > 0) {
			final int red = uiFactory.getRedColor();
			final int green = uiFactory.getGreenColor();
			
			com.example.talos_2.ui.RSPaint paint = uiFactory.createPaint();
			paint.setStyle(com.example.talos_2.ui.PaintStyle.FILL);
			paint.setAntiAlias(false);
			paint.setStrokeWidth(0);
			
			final double maxYValue = Y_SCALE / 2;
			final double scaleX = rect.width() / xAxisSize;
			
			final double minX = multySeries.getMinX();
			
			double startX = ser.getX(0);
			double stopX;
			
			for (int i = 1; i < len; ++i, startX = stopX) {
				stopX = ser.getX(i);
				
				double avgY = Math.min(ser.getY(i), maxYValue);
				
				int color = avgY > 0 ? green : red;
				int alpha = (int) ((avgY / maxYValue) * 255 * (rollOverlayType == RollOverlayType.BACKGROUND ? ROLL_PANNEL_DIM_FACTOR : 1));
				
				paint.setColor(color);
				paint.setAlpha(Math.abs(alpha));
				
				float left = (float) ((startX - minX) * scaleX);
				float right = (float) (((stopX - minX) * scaleX));
				
				canvas.drawRect((int)left, rect.top, (int)right, rect.bottom, paint);
			}
		}
	}
	
	void reset() {
		synchronized (multySeries) {
			resetRollAccum();
		}
	}
	
	private void resetRollAccum() {
		rollAccum = 0;
		rollAccumCount = 0;
	}
	
	void updateRoll(long timestamp, float roll) {
		synchronized (multySeries) {

			float y = filter
			.filter(new float[] {roll})[0];

			rollAccum += y;

			if (rollAccumCount++ == 0) {
				rollAccumTimestamp = timestamp;
			}

			if (rollAccumCount == rollAccumSize) {
				rollPanelSeries.add(rollAccumTimestamp, rollAccum
						/ rollAccumSize);
				resetRollAccum();
			}
		}
	}
}
