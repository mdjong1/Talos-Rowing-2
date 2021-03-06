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

package com.example.talos_2.android.remote;

import android.content.Intent;

import com.example.talos_2.android.app.RoboStrokeActivity;
import com.example.talos_2.data.remote.DataSender;
import com.example.talos_2.data.remote.RemoteDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TalosBroadcastServiceConnector implements DataSender {

	private static final Logger logger = LoggerFactory.getLogger(TalosBroadcastServiceConnector.class);

	private final RoboStrokeActivity owner;
	private Intent service;
	private boolean started;
	private TalosRemoteServiceHelper helper;
			
	public TalosBroadcastServiceConnector(RoboStrokeActivity owner) {
		this.owner = owner;				
	}

	@Override
	public synchronized void start() {

		TalosRemoteServiceHelper helper;
		
		this.helper = helper = new TalosRemoteServiceHelper(owner, TalosRemoteServiceHelper.BROADCAST_SERVICE_ID);

		service = helper.service;

		int port = RemoteDataHelper.getPort(owner.getRoboStroke());
		String host = RemoteDataHelper.getAddr(owner.getRoboStroke());
		
		logger.info("starting boardcast service to endpoint {}:{}", host, port);
		
		service.putExtra("port", port);
		service.putExtra("host", host);
		
		owner.startService(service);
		
		started = true;
	}

	@Override
	public synchronized void stop() {
		
		if (started) {
			owner.stopService(service);

			started = false;
		}
	}


	@Override
	public void write(String data) {
		
		if (started) {
			Intent intent = helper.getServiceIntent();
			intent.putExtra("data", data);
			owner.sendBroadcast(intent);
		}
	}

	@Override
	public void setAddress(String address) {
		restart();
	}
	
	@Override
	public synchronized void setPort(int port) {		
		restart();
	}

	private synchronized void restart() {
		if (started) {
			stop();
			try {
				start();
			} catch (Exception e) {
			}
		}
	}
}
