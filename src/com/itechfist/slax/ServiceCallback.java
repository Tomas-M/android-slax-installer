package com.itechfist.slax;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;


public class ServiceCallback extends ResultReceiver implements Parcelable{
	private Receiver mReceiver;
	public ServiceCallback(Handler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
	}
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }	
   
    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }	
}
