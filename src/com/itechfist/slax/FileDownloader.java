package com.itechfist.slax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.StatFs;
import android.widget.RemoteViews;
import android.widget.Toast;

public class FileDownloader extends Service {
	private static final int HELLO_ID = 1;
	private static final String CURRENT_STATUS = "status";
	private static final String STATUS_IN_PROGRESS = "in progress";
	private static final String STATUS_STOPPED = "stoppped";
	private static final String STATUS_COMPLETE = "complete";
	private static final String STATUS_REFRESH = "refresh";
	private ArrayList<String> listUrl;
	private static ResultReceiver receiver; 
	public static long totalSize = 0;
	public static long doneSize = 0;
	Bundle formData;
	DownloadList dList;
	public static boolean isServiceRunning = false;
	// Binder which receives Notifications from Activity
	private final IBinder mBinder = new LocalBinder();
	private Context context;
	public static AsyncTask<String,Integer,Integer> dlTask;

	// Returns Mapping for Activity
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
    public class LocalBinder extends Binder {
    	public FileDownloader getService() {
            return FileDownloader.this;
        }
    }
    
    
    public static boolean  registerListener(ResultReceiver receiver){
    	FileDownloader.receiver = receiver;
    	return true;
    }
    
    public static boolean deRegisterListener(){
    	FileDownloader.receiver = null;
    	return true;
    }
    
    public static long getTotalSize(){
    	return FileDownloader.totalSize;
    }
    public static long getDoneSize(){
    	return FileDownloader.doneSize;
    }

    public static boolean stopDownload(){
    	if(FileDownloader.dlTask != null || FileDownloader.isServiceRunning){
    		System.out.println("#### Initiating Stop Download" + FileDownloader.dlTask.isCancelled());
    		FileDownloader.dlTask.cancel(true);
    		
    	}
    	
    	return true;
    }
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		formData = new Bundle();
		
		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if(intent != null){
		  formData = intent.getExtras();
		  if(formData.containsKey("Receiver")){
		      receiver = (ResultReceiver)formData.getParcelable("Receiver");
		      FileDownloader.totalSize = formData.getLong("TotalSize");
		      System.out.println("#### Received Total Size :" + FileDownloader.totalSize);
  		      String url = Environment.getExternalStorageDirectory() + "/slax" + "/filelist.txt";
  		      dList = new DownloadList(url,this);
  		      // Starting AsyncTask for Downloading data
  		      isServiceRunning = true;
  		      System.out.println("#### Service Started");
  		      FileDownloader.dlTask = new Downloader().execute("Start Downloading");	
		  }
		  else{
			  System.out.println("#### Trying to Cancel Service");
			  if(FileDownloader.dlTask != null && !FileDownloader.dlTask.isCancelled())
				  FileDownloader.dlTask.cancel(true);
			  
			   stopSelf();
		  }
		}
	}	
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isServiceRunning = false;
		if(FileDownloader.dlTask != null){
			dlTask.cancel(true); // Stopping Downloader Thread
		}
	    stopSelf();
	}

   /*
    * This task downloaded file listed under List, one by one
    */
	class Downloader extends AsyncTask<String,Integer,Integer>{
    	

		@Override
		protected Integer doInBackground(String... arg0) {
			
			// Fetching List of files to be downloaded
			
			listUrl = dList.getDownloadUrl();
			if(listUrl != null && listUrl.size() > 0 && checkNetworkStatus(context))
			{
			  //listUrl.add("http://ftp.slax.org/Slax-7.x/latest-version-unpacked/slax/boot/vmlinuz.abc");
			  int index = 1;
			  long done = 0;
			  for(String str : listUrl){
	
				index = index + 1;
				InputStream input;
				OutputStream output;
				String chkFile = "";
				try{
			    	//Thread.sleep(2000);
		            URL url = new URL(str);
		            URLConnection connection = url.openConnection();			    	
		            connection.connect();
		            // this will be useful so that you can show a typical 0-100% progress bar
		            int fileLength = connection.getContentLength();
		            //Log.i("####", "Downloading file "+ str);
		            //Log.i("####","File Size " + fileLength);
	    			chkFile = str.split("/slax")[1];
	    			chkFile = Environment.getExternalStorageDirectory() + "/slax" + chkFile;
	    			
		            input = new BufferedInputStream(url.openStream());
		            output = new FileOutputStream(chkFile);

		            byte data[] = new byte[1024];
		            int count;
		            long currentTime = System.currentTimeMillis();
		            long previousTime = System.currentTimeMillis();
		            long diff = 0;
		            long lastDownloadStat = 0;
		            
		            while (((count = input.read(data)) != -1) && isServiceRunning)  {
                        
                        FileDownloader.doneSize = done;
                        Bundle b = new Bundle();
                        b.putLong("Size", done);
                        done = done + count;
                        diff = currentTime - previousTime;
                        long speed = done - lastDownloadStat;
                        if(FileDownloader.receiver != null && diff >= 1000) // Send update in gap of one second 
                          { 
                        	  double kbps = ((double)speed)/1024d;
                        	  System.out.println("#### Speed :" + kbps);
                        	  int pt2 = (int)( kbps*100);
                        	  System.out.println("#### Till two decimal precision" + pt2);
                        	  kbps = (double)pt2/100d;
                        	  System.out.println("#### Converted :" + kbps);
                        	  // double speedD = (((int)(speed*100)/1024))/100;
                        	  b.putString("Speed", String.valueOf(kbps));
                        	  FileDownloader.receiver.send(0, b);
                        	  previousTime = currentTime;
                        	  lastDownloadStat = done;
                          }
                        
		                output.write(data, 0, count);
		                if(this.isCancelled()){
		                	File file = new File(chkFile);
		                    if(file.exists())
		                    	file.delete();		                	
		                }
		                currentTime = System.currentTimeMillis();
		                
		            }
		            if(!isServiceRunning){
		            	File file = new File(chkFile);
	                    if(file.exists())
	                    	file.delete();	
		            }
		            output.flush();
		            output.close();
		            input.close();		            
		            
			    }catch (IOException ex){
			    	StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
                    long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getBlockCount();
                    long megAvailable = bytesAvailable / 1048576;
                    if(megAvailable < 200)
                    	Toast.makeText(getApplicationContext(), "Device is out of space. Please clean some space and retry.", Toast.LENGTH_LONG).show();
			    }
				catch(Exception e){
			    	File file = new File(chkFile);
                    if(file.exists())
                    	file.delete(); // Incomplete Download, deleting file
                    //no space left messgae
                    return -1;
			    }  
			  }
			}  
			
			
			
			return 0;
		}
		@Override
		protected void onCancelled() {
		// TODO Auto-generated method stub
		  super.onCancelled();
		  System.out.println("#### Cancelled, Running Task");
		  stopSelf();
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if(isServiceRunning){
				if(result == 0){ // Succesfull Download
					Bundle b  = new Bundle();
					b.putBoolean("DOWNLOAD_STATUS", true);
					FileDownloader.receiver.send(1, b);
					isServiceRunning = false;
									
				}else{
					Bundle b  = new Bundle();
					b.putBoolean("DOWNLOAD_STATUS", false);
					FileDownloader.receiver.send(1, b);
					isServiceRunning = false;
					//stopSelf();				
				}

			}
			stopSelf();
		}
	}
	
	
   /*
    * Set Type 1 : Download in Progress
    * Set Type 2 : Download Complete
    * set Type 0 : Refresh Download
    * Set Type -1: Download Stopped 	
    */
   private boolean  publishNotification(String tickerText,String text,boolean vibrate,boolean sound,boolean lcd,int maxCount,int doneCount,boolean pendingIntent,int type){
	   // Getting reference of notification Manager
	   NotificationManager notifMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	   // Getting a reference of Notification
	   int notif_icon = -1;
	   if(type == 1)  // Download In Progress
	     notif_icon= R.drawable.notification_download_progress;
       if(type == -1) // Download Stopped
    	 notif_icon = R.drawable.notification_download_stopped;
       if(type == 2) // Download Complete
    	 notif_icon = R.drawable.notification_download_complete;
       if(type == 0) // Download Refresh
    	 notif_icon = R.drawable.notification_refresh;
    	 
	   // Recording Starting time
	   long when = System.currentTimeMillis();

	   Notification notif = new Notification(notif_icon,tickerText,when);
	   // Setting up Task type.

	   // Setting Notification Properties
	   if(sound)   // Adding sound to notification
		   notif.defaults |= Notification.DEFAULT_SOUND;
	   if(vibrate)  // Adding vibration to Notification
		   notif.defaults |= Notification.DEFAULT_VIBRATE;
	   if(lcd)		// Adding Lights to Notification  
		   notif.defaults |= Notification.DEFAULT_LIGHTS;

	   // Setting up Images
	   int notifImage = -1;
	   if(type == 1)  // Download In Progress
		   notifImage= R.drawable.notification_download_progress;
	   if(type == -1) // Download Stoppped
		   notifImage = R.drawable.notification_download_stopped;
	   if(type == 2) // Download Complete
		   notifImage = R.drawable.notification_download_complete;
	   if(type == 0) // Download Refresh
		   notifImage = R.drawable.notification_refresh;

	   // Setting Custom layout
	   RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
	   contentView.setImageViewResource(R.id.notification_image,notifImage);
	   contentView.setTextViewText(R.id.notification_title, "Slax Booster");
	   contentView.setTextViewText(R.id.notification_text, text);
	   contentView.setProgressBar(R.id.notification_progressbar, maxCount, doneCount, false);
	   notif.contentView = contentView;
	   if(pendingIntent){
		   // Seting up Intent for Cancelling Notification Message/Call
		   Intent notificationIntent;
		   notificationIntent = new Intent(getApplicationContext(),SlaxActivity.class);
		   notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		   Bundle b = new Bundle();
		   if(type == 1)  
		     b.putString(FileDownloader.CURRENT_STATUS,FileDownloader.STATUS_IN_PROGRESS);
		   if(type == -1)
			 b.putString(FileDownloader.CURRENT_STATUS,FileDownloader.STATUS_STOPPED);
		   if(type == 2)  
			 b.putString(FileDownloader.CURRENT_STATUS,FileDownloader.STATUS_COMPLETE);
		   if(type == 0)
			 b.putString(FileDownloader.CURRENT_STATUS,FileDownloader.STATUS_REFRESH);
			   
		   notificationIntent.putExtras(b);

		   PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
		   notif.contentIntent = contentIntent;      
	   }
	   notifMgr.notify(HELLO_ID, notif);	  
	   return true;
   }	
	  
   // Checking Network Availibility
   public boolean checkNetworkStatus(Context context){
      
   	ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(context.CONNECTIVITY_SERVICE);
	   NetworkInfo info = connectivityManager.getActiveNetworkInfo();
	   if(info == null)
          return false;
	   else 
		   return true;

   }	
}
