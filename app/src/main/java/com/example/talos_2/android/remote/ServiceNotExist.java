package com.example.talos_2.android.remote;

public class ServiceNotExist extends Exception {
	private static final long serialVersionUID = 1L;

	public ServiceNotExist(String detailMessage) {
		super(detailMessage);
	}
	
}