package com.example.talos_2.android.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

class TalosRemoteServiceHelper {

	final Intent service;
	final Context owner;
	private final String serviceId;
	final static String BROADCAST_SERVICE_ID = "com.example.talos_2.android.app.TalosBroadcastService";
	final static String RECEIVER_SERVICE_ID = "com.example.talos_2.android.app.TalosReceiverService";

	private final static Map<String, Pair<String, String>> serviceClassMap = new HashMap<String, Pair<String, String>>();

	static {
		serviceClassMap.put(BROADCAST_SERVICE_ID, Pair.create("com.example.talos_2.android.remote", "com.example.talos_2.android.app.TalosBroadcastService"));
		serviceClassMap.put(RECEIVER_SERVICE_ID, Pair.create("com.example.talos_2.android.remote", "com.example.talos_2.android.app.TalosReceiverService"));
	}

	protected TalosRemoteServiceHelper(Context owner, String serviceId) {

		this.owner = owner;
		this.serviceId = serviceId;
		this.service = getServiceIntent(true);
	}

	Intent getServiceIntent() {
		return getServiceIntent(false);
	}

    /**
     * return service intent - if it is the service launching intent, restrict to talos remote package, class name (to prevent misc. warning in log)
     * @param serviceInitIntent if true then this is the service launching intent and we set the intent class name to talos remote package, class name
     */
	Intent getServiceIntent(boolean serviceInitIntent) {

		Intent intent = new Intent(serviceId);

		if (serviceInitIntent) {
			Pair<String, String> classInfo = serviceClassMap.get(serviceId);

			if (classInfo == null) {
				throw new IllegalArgumentException("no class mapping for service ID '" + serviceId + "'");
			}

			intent.setClassName(classInfo.first, classInfo.second);
		}

		return intent;

	}

//	private void installTalosRemote() {
//		new AlertDialog.Builder(owner)
//		.setMessage(owner.getString(R.string.talos_remote_missing_text))
//		.setTitle(R.string.talos_remote_missing)
//		.setIcon(R.drawable.icon)
//		.setCancelable(true)
//		.setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//
//				String appName = "com.example.talos_2.android.remote";
//
//				try {
//					owner.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
//				} catch (android.content.ActivityNotFoundException anfe) {
//					owner.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
//				}
//			}
//		}).show();
//	}

}
