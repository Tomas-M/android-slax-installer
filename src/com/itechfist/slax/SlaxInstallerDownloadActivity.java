package com.itechfist.slax;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SlaxInstallerDownloadActivity extends Activity implements ServiceCallback.Receiver{
	

	private Context context;
	private long totalSize = 0;
	private ProgressBar dlBar;
	private ArrayList<String> listUrl;
	private Bundle formData;
	public ServiceCallback mReceiver;
	private long previousSize = 0;
	private long lastUpdateTime = 0;
	private TextView dlSpeed;
	private Button cancelButton;
	long doneSize = 0;
	// Used for registering listener, for receiving download updated
	private boolean registerListener(){
		return FileDownloader.registerListener(mReceiver);
	}
	// de-registering listener, for receiving download updated	
	private boolean deRegisterListener(){
		return FileDownloader.deRegisterListener();
	}
	
	@Override
	protected void onCreate(Bundle arg) {
		super.onCreate(arg);
		setContentView(R.layout.slaxinstaller_download_page);
        // Fetching form Data
		context = this;
	    formData = new Bundle();
        mReceiver = new ServiceCallback(new Handler());
        mReceiver.setReceiver(this); 	    
		dlBar = (ProgressBar)findViewById(R.id.download_progress);
		dlSpeed = (TextView)findViewById(R.id.dl_speed);
		cancelButton = (Button) findViewById(R.id.cancelDownloadBtn);
        // Setting up listener
		cancelButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				
				//Log.e("HELL", " FileDownloader.isServiceRunning : "+FileDownloader.isServiceRunning);
				FileDownloader.isServiceRunning = false;
				Intent mainPageIntent = new Intent(context,SlaxInstallerActivity.class);
				//mainPageIntent.putExtra("Return", true);
				mainPageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//mainPageIntent.putExtra("cancelled", true);
				Toast.makeText(getApplicationContext(), "Download cancelled", Toast.LENGTH_LONG).show();
				startActivity(mainPageIntent);
				finish();
				
				//Log.e("HELL", " FileDownloader.isServiceRunning : "+FileDownloader.isServiceRunning);
				/*if(isMyServiceRunning() || FileDownloader.isServiceRunning){
					stopService(new Intent(context,FileDownloader.class));
				    //FileDownloader.stopDownload();
				}*/
			}
			
		});
		

		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		deRegisterListener();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(FileDownloader.isServiceRunning)
		 registerListener();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		deRegisterListener();
		super.onBackPressed();
	
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		
		System.out.println("#### On Start Called Again" + (getIntent() != null));

		// Checking if Activity is going to start a fresh download, or its going to sync with ongoing download
		if(getIntent() != null){
			Intent intent = getIntent();
			Bundle b = intent.getExtras();
			System.out.println("#### Does it contains Key: " + b.containsKey("TotalSize"));
			if(b.containsKey("TotalSize")){
			   totalSize = b.getLong("TotalSize", -1);			
			   
		       if(totalSize == -1)
		       {}
		       else{
	    	
		    	 // Setting up download bar maximum limit and done limits
		    	 int totalDownload = (int) totalSize;
		    	 dlBar.setMax(totalDownload);
		    	 long size = getDoneSize();
		    	 int totalDone = (int) size;
		    	 dlBar.setProgress(totalDone);
                 // Starting Download Service
		    	 Context ctx = getApplicationContext();
                 Intent srvIntent = new Intent(context,FileDownloader.class);
                 formData.putParcelable("Receiver", mReceiver);
                 formData.putLong("TotalSize", totalSize);
                 srvIntent.putExtras(formData);
                 startService(srvIntent);
                 // Registering Activity for getting download updated
                 registerListener();
		        }
		    } 
			// Total Size
			else if(b.containsKey(SlaxInstallerActivity.DOWNLOADING_DATA)){
				 // Registering Activity for getting download updated
				 registerListener();
				 long total = FileDownloader.getTotalSize();
				 long done = FileDownloader.getDoneSize();
		    	 // Setting up download bar maximum limit and done limits		 
				 int totalDownload = (int) total;
				 int totalDone = (int) done;
				 dlBar.setMax(totalDownload);
				 dlBar.setProgress(totalDone);
				
			}
			
		  } // Intent
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
			
		//outState.putLong("TotalData", totalSize);
		//outState.putLong("DoneData", doneSize);
		super.onSaveInstanceState(outState);
	    
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	    // Restoring Value for dlBar
		if(FileDownloader.isServiceRunning || isMyServiceRunning()){
		   System.out.println("#### Refreshed Donload Bar");
		   long total = FileDownloader.getTotalSize();
		   long done =  FileDownloader.getDoneSize();
   	       // Setting up download bar maximum limit and done limits		 
		   int totalDownload = (int) total;
		   int totalDone = (int) done;
		   dlBar.setMax(totalDownload);
		   dlBar.setProgress(totalDone);	
		}   
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		deRegisterListener();
	}
	// Used for get already download size
	private int getDoneSize(){
		String url = Environment.getExternalStorageDirectory() + "/slax" + "/filelist.txt";
		DownloadList dList = new DownloadList(url,this);
		ArrayList<String> listUrl = dList.getDownloadUrl();
		if(listUrl.size() > 0){
			int size = 0;
			for(String str : listUrl){
    			// Checking if File already exist or not
    			String chkFile = str.split("/slax")[1];
    			chkFile = Environment.getExternalStorageDirectory() + "/slax" + chkFile;
    			File file = new File(chkFile);
    			if(file.exists()){
    				size = (int) file.getTotalSpace();
    			}
			}
			return size;
			
		}else{
		  return -1;
		}	
	}


    // returns download speed
    public float getDownloadSpeed(long size1,long size2){
     
    	long nowTime = System.currentTimeMillis();
        long timeDiff = nowTime - lastUpdateTime;
        float diff = timeDiff/1000;
        long sizeDiff = size1 - size2;
        float speed = 0;
        try{ 
          if(sizeDiff == 0){
        	speed = 0;
        	return speed;
          }
          else{
        	speed = (float)(sizeDiff/1024);
          }
          speed = (float) (speed/diff);
          
          lastUpdateTime = nowTime;

        }catch(Exception e){e.printStackTrace();} 

       return speed;
    }
	public void onReceiveResult(int resultCode, Bundle resultData) {

		if(resultCode == 0){ // Download Progress status

			doneSize = resultData.getLong("Size");
			String speed = resultData.getString("Speed");
			int ds = (int) doneSize;
			dlBar.setProgress(ds);
			/*
			  float speed = 0;
			  if(lastUpdateTime != 0){
			    speed = getDownloadSpeed(size,previousSize);
			  } */
			lastUpdateTime = 1;
			dlSpeed.setText("Speed : "+ speed +"kbps"+"\n\n\n\n");
			previousSize = doneSize;
		}else if(resultCode == 1){ // Download Status 
			boolean flag = resultData.getBoolean("DOWNLOAD_STATUS");
			if(flag){ // File was downloaded Succesfully
				//No files to download, Go to the download Complete Activity
				 // Removing Listeners
				 cancelButton.setOnClickListener(null);
				 cancelButton.setVisibility(View.INVISIBLE);
				 cancelButton.setEnabled(false);
				
				Toast.makeText(context,"Download Complete", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(context,SlaxDownloadComplete.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();				
			}else{ // Download was aborted
				//No files to download, Go to the download Complete Activity
				Toast.makeText(context,"Download Failed", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(context,SlaxInstallerInstallActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();				
			}
			
		}
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.itechfist.slax.FileDownloader".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}	
}
