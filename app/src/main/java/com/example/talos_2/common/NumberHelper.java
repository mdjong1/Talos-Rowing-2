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
package com.example.talos_2.common;

public class NumberHelper {
	public static double validRange(double x, double min, double max) {
		return Math.min(max, Math.max(min, x));
	}
	
	public static float validRange(float x, float min, float max) {
		return Math.min(max, Math.max(min, x));
	}
	
	public static long validRange(long x, long min, long max) {
		return Math.min(max, Math.max(min, x));
	}
}
