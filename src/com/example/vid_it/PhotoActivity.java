package com.example.vid_it;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;





public class PhotoActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	// Activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
	private static final int ACCOUNT_PICKER_REQUEST_CODE = 300;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private GoogleApiClient mGoogleApiClient;
	private ContentsResult result;
	private static final String TAG = "avideo activity";
	private static final int REQUEST_CODE_RESOLUTION = 3;
	private static final int REQUEST_CODE_CREATOR = 2;
	private boolean mIsInResolution = false;
	private final String KEY_IN_RESOLUTION = "is_in_resolution";
	private String accountName;

	// directory name to store captured images and videos
	private static final String STORAGE_DIRECTORY = "Tester";

	private Uri file_store; // file url to store image/video

	public Bitmap mBitmapToSave;

	private ImageView imgPreview;
	private VideoView videoPreview;
	private Button googleDrive, facebookPublish;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);
		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		videoPreview = (VideoView) findViewById(R.id.videoPreview);

		//btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		googleDrive = (Button) findViewById(R.id.btnCapturePicture);
		facebookPublish = (Button) findViewById(R.id.continueButton);



		/*
		 * Capture image button click event
		 */
		googleDrive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent temp =AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
				startActivityForResult(temp,ACCOUNT_PICKER_REQUEST_CODE);
			}
		});



		facebookPublish.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), PublishActivity.class);
				//	i.putExtra("BitmapImage", bitmap_to_send);
				startActivity(i);
			}
		});

		//  camera availability??
		if (!isDeviceSupportCamera()) {
			Toast.makeText(getApplicationContext(),
					"Hmm... Seems like your phone does not get a camera get a better phone...",
					Toast.LENGTH_LONG).show();
			// close that app!
			finish();
		}
		if (savedInstanceState != null) {
			mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
		}

		try {
			InputStream inputStream = getAssets().open("some_image.jpg");
			mBitmapToSave = BitmapFactory.decodeStream(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.capture_image:
			captureImage();
			return true;
		}
		return false;
	} 

	/**
	 * Checking device has camera hardware or not
	 * */
	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// CAMERA GO!
			return true;
		} else {
			// Awh shitty phone..
			return false;
		}
	}

	/*
	 * ok got image now try to store it
	 */
	private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		file_store = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, file_store);

		// INTENT!!!
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}

	// Time to finally store
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle ( some odd reason
		// changes
		outState.putParcelable("file_uri", file_store);
		//TODO: save file here
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url
		file_store = savedInstanceState.getParcelable("file_uri");
	}

	/*
	 * Recording video
	 */
	private void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		file_store = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

		// set video quality
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, file_store); 


		//INTENT@!!
		startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
	}

	/**
	 * Receiving activity result method will be called after closing the camera
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the result is capturing Image
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// successfully captured the image
				// display it in image view
				previewCapturedImage();
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled Image capture
				Toast.makeText(getApplicationContext(),
						"User cancelled image capture", Toast.LENGTH_SHORT)
						.show();
			} else {
				// failed to capture image
				Toast.makeText(getApplicationContext(),
						"Sorry! Failed to capture image", Toast.LENGTH_SHORT)
						.show();
			}
		} else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// video successfully recorded
				// preview the recorded video
				previewVideo();
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled recording
				Toast.makeText(getApplicationContext(),
						"User cancelled video recording", Toast.LENGTH_SHORT)
						.show();
			} else {
				// failed to record video
				Toast.makeText(getApplicationContext(),
						"Sorry! Failed to record video", Toast.LENGTH_SHORT)
						.show();
			}
		} else if(requestCode == ACCOUNT_PICKER_REQUEST_CODE) {
			if(resultCode == RESULT_OK) {
				//get acccounts
				accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if(accountName == null){
					mGoogleApiClient = new GoogleApiClient.Builder(PhotoActivity.this)
					.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.useDefaultAccount()
					.addConnectionCallbacks(PhotoActivity.this)
					.addOnConnectionFailedListener( PhotoActivity.this).build();
					mGoogleApiClient.connect();

				}else{
					mGoogleApiClient = new GoogleApiClient.Builder(PhotoActivity.this)
					.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.setAccountName(accountName)
					.addConnectionCallbacks(PhotoActivity.this)
					.addOnConnectionFailedListener( PhotoActivity.this).build();
					mGoogleApiClient.connect();
					Log.i(TAG,accountName);
					onConnected(null);}
				Log.i(TAG, accountName);
			}else
				Log.i(TAG,"There is no accountName");

		}

		switch (requestCode) {
		case REQUEST_CODE_RESOLUTION:
			retryConnecting();
			break;
		}
	}

	private void retryConnecting() {
		mIsInResolution = false;
		if (!mGoogleApiClient.isConnecting()) {
			Log.d("test", "connect()");
			mGoogleApiClient.connect();
		}
	}


	//Preview that IMAGE!
	private void previewCapturedImage() {
		try {
			// hide video preview
			videoPreview.setVisibility(View.GONE);

			imgPreview.setVisibility(View.VISIBLE);

			Matrix mat = new Matrix();
			mat.postRotate(90);
			mat.postScale((float) 1.7, (float) 1.55);

			// bimatp factory to store for compression purposes TODO:!!!!
			BitmapFactory.Options options = new BitmapFactory.Options();

			// downsizing image as it throws OutOfMemory Exception for larger TODO:!! IF TOO LARGE COMPRESS BEFORE BIT
			// images
			options.inSampleSize = 8;

			Bitmap _bitmap = BitmapFactory.decodeFile(file_store.getPath(), options);
			Bitmap bitmap = Bitmap.createBitmap(_bitmap, 0, 0, _bitmap.getWidth(), _bitmap.getHeight(), mat, true);

			imgPreview.setImageBitmap(bitmap);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Previewing recorded video
	 */
	private void previewVideo() {
		try {
			// hide image preview
			imgPreview.setVisibility(View.GONE);// Did not know that...

			videoPreview.setVisibility(View.VISIBLE);
			videoPreview.setVideoPath(file_store.getPath());

			videoPreview.setOnPreparedListener (new OnPreparedListener() {                    
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
				}
			});

			// start playing
			videoPreview.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ------------ Helper Methods ---------------------- 
	 * */

	/*
	 * Creating file url to store image/video
	 */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/*
	 * returning image / video
	 */
	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(
				Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				STORAGE_DIRECTORY);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(STORAGE_DIRECTORY, "Oops! Failed create "
						+ STORAGE_DIRECTORY + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	protected void onPause() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPause();
	}


	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "GoogleApiClient connection suspended");
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "API client connected.");

		//showToast("Inside Connected");
		
		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(
				new ResultCallback<DriveApi.ContentsResult>() {
					@Override
					public void onResult(DriveApi.ContentsResult trash) {
						Log.i(TAG, "After first await().");
						//showToast(""+result.getContents().toString());
						try{
						File temp2 = new File(file_store.getPath());
						RandomAccessFile f = new RandomAccessFile(temp2, "r");
						byte[] b = new byte[(int)f.length()];
						f.read(b);
						OutputStream outputStream = trash.getContents()
								.getOutputStream();
						try {
							outputStream.write(b);
						} catch (IOException e) {
							showMessage("Unable to write file contents.");
							e.printStackTrace();
						}

						MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
						.setTitle("New file")
						.setMimeType("image/jpeg")
						.setStarred(true).build();
						
						Intent i = new Intent(Intent.ACTION_SEND);
						i.setType("image/jpeg");
						i.putExtra(Intent.EXTRA_STREAM,file_store);
						//showToast("meta data created");
						IntentSender intentSender = Drive.DriveApi.newCreateFileActivityBuilder()
								.setInitialMetadata(changeSet)
								.setInitialContents(trash.getContents())
								.build(mGoogleApiClient);
						try {
							startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0 ,0, 0);
						}
						catch(SendIntentException e) {
							Log.i(TAG, "Failed to Lauch this app");
						}

						//showToast("await() complete");
						if (!trash.getStatus().isSuccess()) {
							//   showToast("Error while trying to create the file");
							return;
						}
						// showToast("Created a file: " + dfres.getDriveFile().getDriveId());
						}catch(FileNotFoundException e){
							e.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
						
				});

		//		Log.i(TAG, "After first await().");
		//		//showToast(""+result.getContents().toString());
		//		OutputStream outputStream = result.getContents().getOutputStream();
		//		ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
		//		//java.io.File fileContent = new java.io.File(fileUri.getPath());
		//
		//
		//		MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
		//		.setTitle("New file")
		//		.setMimeType("audio/MP3")
		//		.setStarred(true).build();
		//		//showToast("meta data created");
		//		DriveFileResult dfres= Drive.DriveApi.getRootFolder(mGoogleApiClient)
		//				.createFile(mGoogleApiClient, changeSet, result.getContents())
		//				.await();
		//		//showToast("await() complete");
		//		if (!result.getStatus().isSuccess()) {
		//			//   showToast("Error while trying to create the file");
		//			return;
		//		}
		//		// showToast("Created a file: " + dfres.getDriveFile().getDriveId());
	}



	private Object getGoogleApiClient() {
		// TODO Auto-generated method stub
		return mGoogleApiClient;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// Show a localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(
					result.getErrorCode(), this, 0
					).show();
			return;
		}
		// If there is an existing resolution error being displayed or a resolution
		// activity has started before, do nothing and wait for resolution
		// progress to be completed.
		if (mIsInResolution) {
			return;
		}
		mIsInResolution = true;
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
			retryConnecting();
		}
	}

	/**
	 * Receives the new file's contents and executes the editor AsyncTask
	 */
	private ResultCallback<DriveApi.ContentsResult> mSaveFileCallback = new ResultCallback<DriveApi.ContentsResult>() {
		@Override
		public void onResult(DriveApi.ContentsResult contentsResult) {
			EditFileAsyncTask editFileAsyncTask = new EditFileAsyncTask();
			editFileAsyncTask.execute(contentsResult);
		}
	};

	private void showMessage( String message){
		Log.i(TAG,message);
		Toast.makeText(this,  message, Toast.LENGTH_LONG).show();
	}

	private class EditFileAsyncTask extends AsyncTask<DriveApi.ContentsResult, Void, Boolean> {

		@Override
		protected Boolean doInBackground(DriveApi.ContentsResult... params) {
			DriveApi.ContentsResult contentsResult = params[0];
			if (!contentsResult.getStatus().isSuccess()) {
				showMessage("Failed to create new contents.");
				return false;
			}
			showMessage("New contents created.");
			OutputStream outputStream = contentsResult.getContents().getOutputStream();
			ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
			mBitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
			try {
				outputStream.write(bitmapStream.toByteArray());
			} catch (IOException e) {
				showMessage("Unable to write file contents.");
				e.printStackTrace();
			}

			MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
			.setMimeType("image/jpeg")
			.setTitle("some_image.jpg")
			.build();

			IntentSender intentSender = Drive.DriveApi
					.newCreateFileActivityBuilder()
					.setInitialMetadata(metadataChangeSet)
					.setInitialContents(contentsResult.getContents())
					.build(mGoogleApiClient);

			try {
				startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
			} catch (SendIntentException e) {
				showMessage("Failed to launch file chooser.");
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				showMessage("Error while editing contents");
				return;
			}
			showMessage("Successfully edited contents");
		}
	}
}


