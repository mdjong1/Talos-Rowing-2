package com.example.talos_2.data.remote;

import com.example.talos_2.ParamKeys;
import com.example.talos_2.RoboStroke;
import com.example.talos_2.data.SessionRecorderConstants;

public class RemoteDataHelper {

	public static String getAddr(RoboStroke rs) {
		
		String res = rs.getParameters().getValue(ParamKeys.PARAM_SESSION_BROADCAST_HOST.getId());
		
		return res == null ? SessionRecorderConstants.BROADCAST_HOST : res;
	}

	public static int getPort(RoboStroke rs) {
		Integer res = rs.getParameters().getValue(ParamKeys.PARAM_SESSION_BROADCAST_PORT.getId());
		return res == null ? SessionRecorderConstants.BROADCAST_PORT : res;
	}



}
