package com.itechfist.slax;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SlaxInstallerActivity extends Activity{

	/*private ListView menuListView;
	private ArrayAdapter<String> listAdapter ;*/
	ProgressDialog progDialog;
    
	public final static String DOWNLOADING_DATA = "dl_data";
    private Context context;
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		// TODO Auto-generated method stub
		Dialog d = null;
		switch(id){
		
			case 0:
				progDialog = new ProgressDialog(this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setMessage("Preparing, please wait");
                /*progThread = new ProgressThread(handler);
                progThread.start();*/
                d = progDialog;
                break;
			case 1:
				   AlertDialog.Builder builder = new AlertDialog.Builder(this);
				   builder.setTitle("Network Error");	   
				   android.content.DialogInterface.OnClickListener okay= new android.content.DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				   };
				   
				   builder.setPositiveButton("Okay", okay);
				   d = builder.create();
				   break;
		
		
		}
		return d;//return super.onCreateDialog(id, args);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 2){
			
			//finish();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstances) {
		super.onCreate(savedInstances);
		setContentView(R.layout.slaxinstaller_mainactivity);
		context = this;
		final Button install = (Button) findViewById(R.id.installBtn);
		install.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				startActivityForResult(new Intent(getApplicationContext(), SlaxInstallerInstallActivity.class),2);
				
			}
		});
		
		final Button debug = (Button) findViewById(R.id.debugBtn);
		debug.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				startActivity(new Intent(SlaxInstallerActivity.this,SlaxDebugMessageActivity.class));
				/*try{
					startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
				}catch (ActivityNotFoundException e) {
					
					try{
						startActivity(new Intent(Settings.ACTION_SETTINGS));
						Toast.makeText(getApplicationContext(), "Could not open Debug Settings. Please open it manually and enable debugging.", Toast.LENGTH_LONG).show();
					}catch(ActivityNotFoundException exe){
						Toast.makeText(getApplicationContext(), "Could not open Settings. Please open it manually and enable debugging.", Toast.LENGTH_LONG).show();
					}
					 
				}finally{
					
				}*/
				
			}
		});
		
		// Checking if Service was running, if yes then deviating user to Download page directly
		/*boolean cancelled = false;
		Intent intT = getIntent();
		if(intT !=null && intT.getExtras()!=null){
			cancelled = intT.getExtras().getBoolean("cancelled");
		}*/
		
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// Checking if Associated service already running, if yes then directly call SlaxInstallerDownloader
		if(!checkNetworkStatus(this)){
			//showDialog(1);
		}
		
		if (FileDownloader.isServiceRunning) {
			System.out.println("#### Ok am doing ");
			Intent intent = new Intent(this,
					SlaxInstallerDownloadActivity.class);
			intent.putExtra(SlaxInstallerActivity.DOWNLOADING_DATA, true);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else {
			// Don nothing just wait for user response.
		}

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
    
    class FetchHttpResponse extends AsyncTask<String,String,String>{
         

    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(0);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			dismissDialog(0);
		}

		@Override
		protected String doInBackground(String... params) {
			try{Looper.prepare();}catch(Exception e){e.printStackTrace();}
			try {
				getHttpResponse();
				DownloadFromUrl("http://ftp.slax.org/Slax-7.x/latest-version-unpacked/filelist.txt","filelist.txt");
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
    	
    }	
	public void DownloadFromUrl(String DownloadUrl, String fileName) {

		try {
			File dir = new File (Environment.getExternalStorageDirectory() + "/slax");
			if(dir.exists()==false) {
				dir.mkdirs();
			}

			URL url = new URL(DownloadUrl); //you can write here any link
			File file = new File(dir, fileName);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(5000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
             fos.flush();
			fos.close();

		} catch (IOException e) {
			Log.d("DownloadManager", "Error: " + e);
		}

	}

		
     void getHttpResponse() throws ClientProtocolException, IOException{
	    	
    	HttpClient httpclient = new DefaultHttpClient();
    	HttpResponse response = httpclient.execute(new HttpGet("http://ftp.slax.org/Slax-7.x/latest-version-unpacked/filelist.txt"));
    	StatusLine statusLine = response.getStatusLine();
    	//File slaxDir = new File(Environment.getExternalStorageDirectory() + "/slax");
    	if(statusLine.getStatusCode() == HttpStatus.SC_OK){
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    		response.getEntity().writeTo(out);
    		out.close();
    		String responseString = out.toString();
    		createFolderStructure(responseString);
    	} else{
    		response.getEntity().getContent().close();
    		throw new IOException(statusLine.getReasonPhrase());
    	}

    }
    
   /*
    * This Method takes a File Parsed string as a input, and in return it creates all the Directorie
    */
	void createFolderStructure(String responseString){
		 // Creating Directory's
		 File file = new File(Environment.getExternalStorageDirectory() + "/slax");
		 if(!file.isDirectory())
		   file.mkdirs();
		 // Making Sub directories
		 for(String str: responseString.split("\n")){
			 // Read Each Individual Item, and Create Folder Structure as Required
			 String[] subStr = str.split("/slax");
			 if(subStr.length > 1){
				 String[] subDirStr = subStr[1].split("/"); 
                String dirStruct = "";
                 for(String sstr : subDirStr){
                   if(sstr.indexOf(".") < 0){
                   	 
                	  dirStruct = dirStruct + sstr + "/";
                   }
                 }
                 if(dirStruct.length() > 1 && dirStruct.indexOf("vmlinuz") < 0){ 
    			    File dirFile = new File(Environment.getExternalStorageDirectory() + "/slax" + dirStruct);
   			    if(!dirFile.isDirectory())
   			    	dirFile.mkdirs();                     
                 }
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
