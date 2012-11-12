package com.itechfist.slax;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.storage.StorageManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class SlaxActivity extends Activity {
	
	/*private static final String CURRENT_STATUS = "status";
	private static final String STATUS_IN_PROGRESS = "in progress";
	private static final String STATUS_STOPPED = "stoppped";
	private static final String STATUS_COMPLETE = "complete";
	private static final String STATUS_REFRESH = "refresh";
	private final int baseVersion = 000;
	private final String BASE_VERSION = "base_version";
	private final String BASE_VALUE = "base_value";*/
	
	ArrayList<SlaxServerFiles> slax = new ArrayList<SlaxServerFiles>();
	ListView slaxListView;
	ProgressDialog progDialog;
	Button downloadBtn;
	
    @Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		// TODO Auto-generated method stub
		
		switch(id){
		
			case 0:
				progDialog = new ProgressDialog(this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setMessage("Loading...");
                /*progThread = new ProgressThread(handler);
                progThread.start();*/
                break;
		
		
		}
		return progDialog;//return super.onCreateDialog(id, args);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.activity_slax);
        downloadBtn = (Button)findViewById(R.id.download_data);
        final Context context = this;
        //createDirectoyStruct();
        downloadBtn.setVisibility(View.INVISIBLE);
        
        // Setting up Downloader Listener
        downloadBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				if(FileDownloader.isServiceRunning){
					Toast.makeText(getApplicationContext(), "Download service already running.", Toast.LENGTH_LONG).show();
				}else{
					Intent intent = new Intent(context,FileDownloader.class);
					startService(intent);	
				}
				
			}
        	
        });
        
        // Creating Async Task for Downloading Header Data
        new FetchHttpResponse().execute();
       /* try {
			getHttpResponse();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_slax, menu);
        return true;
    }
    

    /*
     * Custom Adpater for List display, and Indicating downloaded and yet to be downloaded files
     * It also displays the file which are updated.
     */
    class SlaxCustomAdapter extends ArrayAdapter<SlaxServerFiles>{

    	int rand = 0;
    	
    	@Override
		public int getCount() {
			return slaxObj.size();
		}

		@Override
		public SlaxServerFiles getItem(int position) {
			return super.getItem(position);
		}

		//Context context;
    	ArrayList<SlaxServerFiles> slaxObj;
    	
		public SlaxCustomAdapter(Context context, int textViewResourceId , ArrayList<SlaxServerFiles> obj) {
			super(context, textViewResourceId);
			slaxObj = obj;
			//this.context = context;
		}
    	
		 @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    View rowView = convertView;
		    SlaxHolder holder = null;
		        
		    if (rowView == null) {
		    
		      LayoutInflater inflater = getLayoutInflater();
		      rowView = inflater.inflate(R.layout.list_row_item, null);
		      holder = new SlaxHolder();
		      holder.fileImg = (ImageView) rowView.findViewById(R.id.versionTv);
		      holder.fileName= (TextView) rowView.findViewById(R.id.filenameTv);
		      rowView.setTag(holder);
		    }
		    else{
		    	holder = (SlaxHolder) rowView.getTag();
		    }
		    	
		    SlaxServerFiles slaxData = slaxObj.get(position);
		    if(slaxData.getFlag())
		    	holder.fileImg.setBackgroundDrawable(getResources().getDrawable(R.drawable.correct));
		    else
		    	holder.fileImg.setBackgroundDrawable(getResources().getDrawable(R.drawable.wrong));
		    
		    holder.fileName.setText(slaxData.getFile());
		   /* int i = (rand++)%3;
		    switch(i){
		    	
		    case 0:
		    	rowView.setBackgroundColor(Color.GREEN);
		    	break;
		    case 1:
		    	rowView.setBackgroundColor(Color.YELLOW);
		    	break;
		    case 2:
		    	rowView.setBackgroundColor(Color.RED);
		    	break;
		    default:
		    	break;
		    
		    }*/
		    return rowView;
		  }
    	
		
    }
    static class SlaxHolder
    {
        ImageView fileImg;
        TextView fileName;
    }
    
    
    /*
     * Async Task Downloads the header file for Directories creations and Associated file download
     */
    
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
			setupListView();
			downloadBtn.setVisibility(View.VISIBLE);
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
            // Temp Holder for base version
    		
    		for(String str : responseString.split("\n")){

    			String absPath[] = str.split("unpacked/slax");
    			String str1[] =  str.split("/");
    			String fileName = str1[str1.length-1];
    			File file = new File(Environment.getExternalStorageDirectory() + "/slax"+absPath[1]);
    			boolean flag = false;
    			if(file.exists())
    				flag = true;
    			slax.add(new SlaxServerFiles(flag, fileName));
    			
    		}
    		// Writing Shared preferences for holding current version value
    		/*Editor editor = getApplicationContext().getSharedPreferences(BASE_VERSION, Context.MODE_PRIVATE).edit();
    		Log.i("####","Base Version :" + baseVersion);
    		editor.putInt(BASE_VALUE, baseVersion);
    		editor.commit();*/
    		
    		

    	} else{
    		response.getEntity().getContent().close();
    		throw new IOException(statusLine.getReasonPhrase());
    	}

    }
		
		void setupListView(){
			
			slaxListView = (ListView) findViewById(R.id.listView);
			View header = getLayoutInflater().inflate(R.layout.header_row_item, null);
			SlaxCustomAdapter customAdptr = new SlaxCustomAdapter(getApplicationContext(), R.layout.list_row_item, slax);
			slaxListView.addHeaderView(header);
			
			slaxListView.setAdapter(customAdptr);
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

} // End of Activity
