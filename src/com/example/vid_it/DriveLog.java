package com.example.vid_it;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class DriveLog extends Activity implements ConnectionCallbacks,OnConnectionFailedListener{
	private static final String TAG = "android-drive-quickstart";
	private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
	private static final int REQUEST_CODE_CREATOR = 2;
	private static final int REQUEST_CODE_RESOLUTION = 3;
	private GoogleApiClient mGoogleApiClient;
	private Bitmap mBitmapToSave;

	/**
	 * Create a new file and save it to Drive.
	 */
	private void saveFileToDrive() {
		// Start by creating a new contents, and setting a callback.
		Log.i(TAG, "Creating new contents.");
		final Bitmap image = mBitmapToSave;
		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(
				new ResultCallback<ContentsResult>() {
					@Override
					public void onResult(ContentsResult result) {
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
	protected void onResume() {
		super.onResume();
		if (mGoogleApiClient == null) {
			// Create the API client and bind it to an instance variable.
			// We use this instance as the callback for connection and
			// connection
			// failures.
			// Since no account name is passed, the user is prompted to choose.
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.addConnectionCallbacks((com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) this)
					.addOnConnectionFailedListener((com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) this).build();
		}
		// Connect the client. Once connected, the camera is launched.
		mGoogleApiClient.connect();
	}

	@Override
	protected void onPause() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPause();
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_CAPTURE_IMAGE:
			// Called after a photo has been taken.
			if (resultCode == Activity.RESULT_OK) {
				// Store the image data as a bitmap for writing later.
				mBitmapToSave = (Bitmap) data.getExtras().get("data");
			}
			break;
		case REQUEST_CODE_CREATOR:
			// Called after a file is saved to Drive.
			if (resultCode == RESULT_OK) {
				Log.i(TAG, "Image successfully saved.");
				mBitmapToSave = null;
				// Just start the camera again for another photo.
				startActivityForResult(new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE),
						REQUEST_CODE_CAPTURE_IMAGE);
			}
			break;
		}
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
	public void onCreate(Bundle connectionHint) {
		 super.onCreate(connectionHint);
		Log.i(TAG, "API client connected.");
		if (mBitmapToSave == null) {
			// This activity has no UI of its own. Just start the camera.
			startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
					REQUEST_CODE_CAPTURE_IMAGE);
			return;
		}
		saveFileToDrive();
	}


	@Override
	public void onConnected(Bundle arg0) {
		 super.onCreate(arg0);
		Log.i(TAG, "API client connected.");
		if (mBitmapToSave == null) {
			// This activity has no UI of its own. Just start the camera.
			startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
					REQUEST_CODE_CAPTURE_IMAGE);
			return;
		}
		saveFileToDrive();
		
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "GoogleApiClient connection suspended");
	}
}