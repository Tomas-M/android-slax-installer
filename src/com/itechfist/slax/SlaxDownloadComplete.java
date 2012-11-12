package com.itechfist.slax;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SlaxDownloadComplete extends Activity {

	private Context context;
	private Button enableDebug;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setting up content view
		setContentView(R.layout.slaxinstaller_download_complete);
		context = this;
		// Fetching form Objects
		enableDebug = (Button) findViewById(R.id.enable_debugging);
		// Setting up onClick Listener
		enableDebug.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				
				Intent intent = new Intent(SlaxDownloadComplete.this,SlaxInstallerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				SlaxDownloadComplete.this.finish();
				
			}

		});

	}

}
