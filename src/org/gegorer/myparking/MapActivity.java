package org.gegorer.myparking;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MapActivity extends Activity implements OnClickListener, LocationListener {

    final static String TAG = MapActivity.class.getSimpleName();
    final static private long LOC_UPDATE_MIN_TIME = 10000;//ms
    final static private float LOC_UPDATE_MIN_DIST = 10;//meter
	private String mLat;
	private String mLon;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		try {
    		SharedPreferences settings = getSharedPreferences("Location", 0);
    		mLat = settings.getString("lat", "Invalid");
    		mLon = settings.getString("lon", "Invalid");
		} catch(RuntimeException e) {
		    mLat = "Invalid";
		    mLon = "Invalid";
		}
		Button saveBtn = (Button) findViewById(R.id.save);
		saveBtn.setOnClickListener(this);
		
		Button navBtn = (Button) findViewById(R.id.nav);
		navBtn.setOnClickListener(this);
		if (mLat.equals("Invalid") || mLon.equals("Invalid")) {
			navBtn.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

    @Override
    protected void onResume() {
        super.onResume();
        startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.save:
			save();
			break;
		case R.id.nav:
			nav();
			break;
		default:
			break;
		}
	}

	private void UpdateCoordTextViews() {
	    TextView latV = (TextView)getWindow().findViewById(R.id.textLat);
	    TextView lonV = (TextView)getWindow().findViewById(R.id.textLon);
	    latV.setText(mLat);
	    lonV.setText(mLon);
	}
    /**
     * Callbacks from the LocationListener
     */
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged");
        mLat = String.valueOf(location.getLatitude());
        mLon = String.valueOf(location.getLongitude());
        UpdateCoordTextViews();
    }

    public void onProviderDisabled(String provider) {
        stopListening();
        startListening();
    }

    public void onProviderEnabled(String provider) {
        stopListening();
        startListening();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private void startListening() {
        try {
            Criteria criteria = new Criteria();
            //criteria.setAccuracy(Criteria.ACCURACY_FINE);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Log.d(TAG, locationManager.getProviders(false).toString());
            if( locationManager != null && provider != null ) {
                Log.d(TAG, "provider: " + provider);
                locationManager.requestLocationUpdates(
                        provider,
                        LOC_UPDATE_MIN_TIME,
                        LOC_UPDATE_MIN_DIST, this);

                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    mLat = String.valueOf(location.getLatitude());
                    mLon = String.valueOf(location.getLongitude());
                }
            }
            else {
                Log.d(TAG, "null provider ");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        UpdateCoordTextViews();
    }
    
    private void stopListening() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(this);
    }

	private void save() {
		// TODO: confirm dialog for overwrite
		SharedPreferences settings = getSharedPreferences("Location", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lat", mLat);
		editor.putString("lon", mLon);
		editor.commit();

		Button navBtn = (Button) findViewById(R.id.nav);
		navBtn.setEnabled(true);
	}
	
	private void nav() {
	    String mapLink = "geo:" + mLat + "," + mLon;

	    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapLink));
	    startActivity(intent);
	}

}
