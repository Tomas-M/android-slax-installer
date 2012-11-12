package com.itechfist.slax;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SlaxInstallerInstallActivity extends Activity {

	TextView messageTv;
	Button downloadBtn;
	boolean backPress = false;
	private Context context;
	private long totalSize = 0;
	private ArrayList<String> listUrl;	
	ProgressDialog progDialog;
	private AsyncTask<String,Integer,Integer> prpTask;
	static SharedPreferences prefs;
	private final String PREFS = "data"; 
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		// TODO Auto-generated method stub
		Dialog d = null;
		switch(id){
		
			case 0:
				progDialog = new ProgressDialog(this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setMessage("Preparing, please wait");
                progDialog.setCancelable(false);
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
	
	
	

    public static String getVersion(String filename){
		return prefs.getString(filename, "new");
	}
    public static void setVersion(String filename, String version){
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putString(filename, version);
    	editor.commit();
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		setContentView(R.layout.slaxinstaller_install_page);
		context = this;
		messageTv = (TextView) findViewById(R.id.messageTv);
		
		final boolean sdCardFlag = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		
		downloadBtn = (Button) findViewById(R.id.downloadBtn);
		downloadBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				//Download code goes here
				
				if(!backPress){
					if(sdCardFlag && checkNetworkStatus(context)){
						// Preparing for Download
						prpTask = new FetchHttpResponse().execute();
					}
					else{
						Toast.makeText(context,"Please check your network availability and SD card status", Toast.LENGTH_SHORT).show();
						downloadBtn.setText("Back");
						messageTv.setText(R.string.missingsdcard);
						backPress = true;
					}
				}
				else{
					downloadBtn.setText(R.string.download);
					messageTv.setText(R.string.downloadtext);
					backPress = false;
					
				}
				
				
			}
		});
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
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if(prpTask != null) 
		  prpTask.cancel(true);
	}
	
    
    class FetchHttpResponse extends AsyncTask<String,Integer,Integer>{
         

    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(0);
			
		} 
        @Override
        protected void onCancelled() {
        	// TODO Auto-generated method stub
        	super.onCancelled();
            
        }
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if(result == 0){ // Folder Structure was created succssefully, proceed with doanloading
			
			  
			  // Once Folder Structure is Calculated, will proceed with remaining calculation
			  downloadBtn.setText(R.string.download);
			  messageTv.setText(R.string.downloadtext);
			  //Download code
			  totalSize = getTotalDLSize();
              if(totalSize > 0){  
			    Intent intent = new Intent(SlaxInstallerInstallActivity.this,SlaxInstallerDownloadActivity.class);
                intent.putExtra("TotalSize", totalSize);
			    // Setting up total Done
			
			    startActivity(intent);
			    setResult(2);
			    finish();
              }
              
			}
			else{ // Some Issue with creating folder structure, ask user again to tap doanload button
				
				Toast.makeText(context,"Network error, please try again", Toast.LENGTH_SHORT).show();
				downloadBtn.setText(R.string.download);
				messageTv.setText(R.string.downloadtext);
				backPress = false;				
				
			}
			try{dismissDialog(0);}catch(Exception e){};
		}

		@Override
		protected Integer doInBackground(String... params) {
			try{Looper.prepare();}catch(Exception e){e.printStackTrace();}
			try {
				getHttpResponse();
				DownloadFromUrl("http://ftp.slax.org/Slax-7.x/latest-version-unpacked/filelist.txt","filelist.txt",this);
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			    return -1;
			}catch(Exception e){
				e.printStackTrace();
				return -1;
			}
			return 0;
		}
    	
    }	
	public void DownloadFromUrl(String DownloadUrl, String fileName,AsyncTask<String,Integer,Integer>  tsk) throws IOException{

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
				if(tsk.isCancelled()){
					return;
				}
				baf.append((byte) current);
			}
			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
             fos.flush();
			fos.close();

		} catch (IOException e) {
			Log.d("DownloadManager", "Error: " + e);
		    throw e;
		}

	}

		
     void getHttpResponse() throws ClientProtocolException, IOException, Exception{
	    	
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
	void createFolderStructure(String responseString) throws Exception{
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

	// Method for Getting total Size of Download
	private long getTotalDLSize(){
		String url = Environment.getExternalStorageDirectory() + "/slax" + "/filelist.txt";
		DownloadList dList = new DownloadList(url,this);
		/*
		 * The following two method should be called one after another
		 */
		listUrl = dList.getDownloadUrl();
		if(listUrl.size() <= 0)
		{
			//No files to download, Go to the download Complete Activity
			Toast.makeText(context,"All files are up to date", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(context,SlaxDownloadComplete.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
		return dList.getTotalSize();
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
