/*
 * Copyright (c) 2011 Tal Shalif
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

import com.example.talos_2.RoboStroke;
import com.example.talos_2.data.SensorDataSink;
import com.example.talos_2.ui.UILiaison;
import com.example.talos_2.ui.graph.XYSeries.XMode;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public class StrokeGraph extends SensorGraphBase {
	private static final float Y_RANGE = 4f;

	public StrokeGraph(com.example.talos_2.ui.UILiaison factory, float xRange, RoboStroke roboStroke)	{
		super(factory, XMode.ROLLING, xRange, Y_RANGE, roboStroke);
	}
	
	@Override
	protected synchronized void attachSensors(SensorDataSink lineDataSink) {
		roboStroke.getStrokeRateScanner().addSensorDataSink(lineDataSink);
	}
	
	@Override
	protected void detachSensors(SensorDataSink lineDataSink) {
		roboStroke.getStrokeRateScanner().removeSensorDataSink(lineDataSink);
	}
}