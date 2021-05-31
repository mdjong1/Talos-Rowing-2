/*
 * Copyright (c) 2013 Tal Shalif
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

package com.example.talos_2.android.app;

import android.content.Intent;

import com.example.talos_2.data.remote.DataReceiver;
import com.example.talos_2.data.remote.DataRemote;
import com.example.talos_2.data.remote.DataRemote.DataRemoteError;
import com.example.talos_2.data.remote.DatagramDataReceiver;

public class TalosReceiverService extends TalosService {
 
	private final static String BROADCAST_ID = TalosReceiverService.class.getName();

	private DataReceiver impl;

	public TalosReceiverService() {
	}

	@Override
	protected DataRemote makeImpl(String host, int port) throws DataRemoteError {
		impl = new DatagramDataReceiver(host, port, new DataReceiver.Listener() {

			@Override
			public void onDataReceived(String s) {

				Intent intent = new Intent(BROADCAST_ID);
				intent.putExtra("data", s);
				sendBroadcast(intent);
			}
		});

		return impl;
	}


	@Override
	protected void afterStart() {		
	}


	@Override
	protected void beforeStop() {
	}
}