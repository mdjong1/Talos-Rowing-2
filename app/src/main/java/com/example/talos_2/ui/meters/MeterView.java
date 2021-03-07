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

package com.example.talos_2.ui.meters;

import com.example.talos_2.ui.LayoutMode;
import com.example.talos_2.ui.RSTextView;
import com.example.talos_2.ui.RSView;

public interface MeterView {
	
	public abstract void updateLayout(com.example.talos_2.ui.LayoutMode meterLayout);

	public com.example.talos_2.ui.RSTextView getSplitTimeTxt();
	public com.example.talos_2.ui.RSTextView getSpmTxt();
	public com.example.talos_2.ui.RSTextView getSpeedTxt();
	public com.example.talos_2.ui.RSTextView getAvgSpeedTxt();
	public com.example.talos_2.ui.RSView getAccuracyHighlighter();
	public com.example.talos_2.ui.RSView getStrokeModeHighlighter();
	public com.example.talos_2.ui.RSTextView getStrokeCountTxt();
	public com.example.talos_2.ui.RSTextView getSplitDistanceTxt();
	public com.example.talos_2.ui.RSTextView getTotalDistanceTxt();
	public com.example.talos_2.ui.RSTextView getTotalTimeTxt();
	public com.example.talos_2.ui.RSTextView getSplitStrokesTxt();
	public com.example.talos_2.ui.RSTextView getAvgSpmTxt();
}
