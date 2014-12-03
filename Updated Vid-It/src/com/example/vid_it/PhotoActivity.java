package com.example.vid_it;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
//import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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

//import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
//import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
//import com.google.android.gms.drive.DriveApi;
//import com.google.android.gms.drive.DriveFolder.DriveFileResult;
//import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.ContentsResult;

public class PhotoActivity extends Activity implements com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks, com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {
	private static final String TAG = "PhotoActivity";
	// Activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private GoogleApiClient mGoogleApiClient;
	private static final int REQUEST_CODE_CREATOR = 2;
	private static final int REQUEST_CODE_RESOLUTION = 3;

	//private boolean first = true;

	// directory name to store captured images and videos
	private static final String STORAGE_DIRECTORY = "Vidit";

	private Uri file_store; // file url to store image/video

	private ImageView imgPreview;
	private VideoView videoPreview;
	private Button googleDrive, facebookPublish;

	public Bitmap bitmap_to_send;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);

		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		videoPreview = (VideoView) findViewById(R.id.videoPreview);
		googleDrive = (Button) findViewById(R.id.btnCapturePicture);
		//btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);
		facebookPublish = (Button) findViewById(R.id.continueButton);

		if(bitmap_to_send == null) {
			Log.i(TAG,"it is null!!!!!!!!!!!!!!!!!!!!");

			startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_CAPTURE_IMAGE_REQUEST_CODE);


		}
		else{

			Log.i(TAG,"it is NOT null??????????????????");
		}
		/*
		 * Capture image button click event
		 */
		googleDrive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(bitmap_to_send == null)
					Log.i(TAG,"it is null!!!!!!!!!!!!!!!!!!!!");
				else
					Log.i(TAG,"it is NOT null??????????????????");
				saveFileToDrive();
			}	
		});

		facebookPublish.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), PublishActivity.class);
				i.putExtra("BitmapImage", bitmap_to_send);
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
	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.i(TAG,"We are in On resume-----------------------------------");
		if (mGoogleApiClient == null) {
			// Create the API client and bind it to an instance variable.
			// We use this instance as the callback for connection and
			// connection
			// failures.
			// Since no account name is passed, the user is prompted to choose.
			Log.i(TAG,"We are in onResume if Statement");
			mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener( this).build();
			//mGoogleApiClient.connect();

		}
		if (mGoogleApiClient != null){
			Log.i(TAG,"We are in onResume not null");
			setContentView(R.layout.camera_preview);
		}
		// Connect the client. Once connected, the camera is launched.
		mGoogleApiClient.connect();
	
	}

	private void saveFileToDrive() {
		// Start by creating a new contents, and setting a callback.
		Log.i(TAG, "Creating new contents.");
		final Bitmap image = bitmap_to_send;
		Log.i(TAG, "After bitmap");
		if(mGoogleApiClient == null){
			Log.i(TAG,"I hate my life.... Oh how I hate my life.");
		}
		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(
				new ResultCallback<ContentsResult>() {
					@Override
					public void onResult(ContentsResult result) {
						Log.i(TAG, "We are in on result. Callback passed");
						// If the operation was not successful, we cannot do
						// anything
						// and must
						// fail.
						if (!result.getStatus().isSuccess()) {
							Log.i(TAG, "Failed to create new contents.");
							return;
						}
						// Otherwise, we can write our data to the new contents.
						Log.i(TAG, "New contents created.");
						// Get an output stream for the contents.
						OutputStream outputStream = result.getContents()
								.getOutputStream();
						// Write the bitmap data from it.
						ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
						image.compress(Bitmap.CompressFormat.PNG, 100,
								bitmapStream);
						try {
							outputStream.write(bitmapStream.toByteArray());
						} catch (IOException e1) {
							Log.i(TAG, "Unable to write file contents.");
						}
						// Create the initial metadata - MIME type and title.
						// Note that the user will be able to change the title
						// later.
						Log.i(TAG, "Before meta data");
						MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
						.setMimeType("image/jpeg")
						.setTitle("Android Photo.png").build();
						// Create an intent for the file chooser, and start it.
						IntentSender intentSender = Drive.DriveApi
								.newCreateFileActivityBuilder()
								.setInitialMetadata(metadataChangeSet)
								.setInitialContents(result.getContents())
								.build(mGoogleApiClient);
						try {
							startIntentSenderForResult(intentSender,
									REQUEST_CODE_CREATOR, null, 0, 0, 0);
						} catch (SendIntentException e) {
							Log.i(TAG, "Failed to launch file chooser.");
						}
					}
				});
		
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
		//startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}

	// Time to finally store
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle ( some odd reason
		// changes
		outState.putParcelable("file_uri", file_store);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url
		file_store = savedInstanceState.getParcelable("file_uri");
	}



	/*
	 * Recording video
	 
	private void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		file_store = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

		// set video quality
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, file_store); 


		//INTENT@!!
		startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
	}
*/
	/**
	 * Receiving activity result method will be called after closing the camera
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the result is capturing Image
		Log.i(TAG,"We are insidethe ActivityResult..........................................");
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// successfully captured the image
				// display it in image view
				//bitmap_to_send = (Bitmap) data.getExtras().get("data");
				previewCapturedImage(data);
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
		}
	}

	//Preview that IMAGE!
	private void previewCapturedImage(Intent data) {
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

			Bitmap _bitmap = ((Bitmap)data.getExtras().get("data"));
			Bitmap bitmap = Bitmap.createBitmap(_bitmap, 0, 0, _bitmap.getWidth(), _bitmap.getHeight(), mat, true);

			bitmap_to_send = bitmap;

			imgPreview.setImageBitmap(bitmap_to_send);
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
	public void onConnectionFailed(ConnectionResult result) {
		// Called whenever the API client fails to connect.
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}
		// The failure has a resolution. Resolve it.
		// Called typically when the app is not yet authorized, and an
		// authorization
		// dialog is displayed to the user.
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		super.onCreate(connectionHint);

	}

	public void onDisconnected() {
		Log.i(TAG, "GoogleApiClient connection suspended");

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
}