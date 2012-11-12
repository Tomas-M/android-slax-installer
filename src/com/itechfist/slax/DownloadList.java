package com.itechfist.slax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;


public class DownloadList {

	private ArrayList<String> downloadUrl;
	private String url;
	//private int baseVersion = 000;
	private final String BASE_VERSION = "base_version";
	private final String BASE_VALUE = "base_value";
	private Context context;
	private long totalSize = 0;
	public DownloadList(String url,Context context){
		this.url = url;
		this.context = context;
	}

	public ArrayList<String> getDownloadUrl() {
		
		// Get the base version, based on this we will be deciding list of files to be downloded
		//populateBaseVersion();
		populateDownloadingUrls();
		//populateFileSize();
		return downloadUrl;
	}
	
	public long getTotalSize(){
		//populateDownloadingUrls();
		return totalSize;
	}
	
	
	private boolean populateDownloadingUrls(){
		// Code for Reading through File,and setting up Download URLS;
        downloadUrl = new ArrayList<String>();
		File file = new File(url);
		//Read text from file
		StringBuilder text = new StringBuilder();
		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;

		    while ((line = br.readLine()) != null) {
    			String str1[] =  line.split("/");
    			
    			String tempVersion = (str1[0]).split(",")[0];
    			
    			// Checking if File already exist or not
    			String fileName = line.split("/slax")[1];
    			String chkFile = Environment.getExternalStorageDirectory() + "/slax" + fileName;
    			File cFile = new File(chkFile);
				if (!cFile.exists()) {
					// setting version in sharedpreferences
					SlaxInstallerInstallActivity.setVersion(fileName, line.split(",")[0]);
					String dlUrl = line.split(",")[2];
					totalSize = totalSize + Integer.valueOf(line.split(",")[1]);
					downloadUrl.add(dlUrl);

				} 
				else if (cFile.isFile() && !tempVersion.equals(SlaxInstallerInstallActivity.getVersion(fileName))) {
					if(cFile.exists())
						cFile.delete(); // Deleting outdate file;
					
					String dlUrl = line.split(",")[2];
					totalSize = totalSize + Integer.valueOf(line.split(",")[1]);
					downloadUrl.add(dlUrl);
				}else{
					
				}
				text.append(line);	
    		   
    			
		    }
		    
		}
		
		
		catch (IOException e) {
		    //You'll need to add proper error handling here
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	
/*	public static byte[] createChecksum(String filename) throws Exception {
	       InputStream fis =  new FileInputStream(filename);

	       byte[] buffer = new byte[1024];
	       MessageDigest complete = MessageDigest.getInstance("MD5");
	       int numRead;

	       do {
	           numRead = fis.read(buffer);
	           if (numRead > 0) {
	               complete.update(buffer, 0, numRead);
	           }
	       } while (numRead != -1);

	       fis.close();
	       return complete.digest();
	   }

	   // see this How-to for a faster way to convert
	   // a byte array to a HEX string
	
	  
	   public static String getMD5Checksum(String filename) throws Exception {
	       byte[] b = createChecksum(filename);
	       String result = "";

	       for (int i=0; i < b.length; i++) {
	           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	       }
	       return result;
	   }
*/
	
	void populateDownloadUrls(){
		downloadUrl = new ArrayList<String>();
	}
	
	
}
