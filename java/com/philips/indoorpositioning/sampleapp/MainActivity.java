/*
 * (C) Philips Lighting, 2016.
 *   All rights reserved.
 */

package com.philips.indoorpositioning.sampleapp;

import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.philips.indoorpositioning.library.IndoorPositioning;
import com.philips.indoorpositioning.library.IndoorPositioning.IndoorPositioningHeadingOrientation;
import com.philips.indoorpositioning.library.IndoorPositioning.IndoorPositioningMode;
import com.philips.indoorpositioning.library.IndoorPositioning.Listener;

public class MainActivity extends Activity {

    private Handler handler = new Handler();

    private IndoorPositioning ip;

    private ToggleButton tbStub;
    private TextView tvLatitude, tvLongitude, tvHorAccuracy, tvAltitude, tvAltitudeAccuracy, tvHeading,
            tvHeadingAccuracy, tvLastError, tvFloor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ip = new IndoorPositioning(getApplicationContext());

        tvLatitude = (TextView) findViewById(R.id.latitude);
        tvLongitude = (TextView) findViewById(R.id.longitude);
        tvHorAccuracy = (TextView) findViewById(R.id.horizontal_accuracy);
        tvAltitude = (TextView) findViewById(R.id.altitude);
        tvAltitudeAccuracy = (TextView) findViewById(R.id.altitude_accuracy);
        tvHeading = (TextView) findViewById(R.id.heading);
        tvHeadingAccuracy = (TextView) findViewById(R.id.heading_accuracy);
        tvLastError = (TextView) findViewById(R.id.last_error);
        tvFloor = (TextView) findViewById(R.id.floor);

        ((TextView) findViewById(R.id.version)).setText(ip.getVersion());
        ip.setConfiguration(getString(R.string.app_configuration));
        ip.setHeadingOrientation(IndoorPositioningHeadingOrientation.PORTRAIT);

        tbStub = (ToggleButton) findViewById(R.id.button_stub);
    }

    private boolean userStarted;

    public void onClickStart(View view) {
        tbStub.setEnabled(false);

        if (!ip.isRunning()) {
            tvLatitude.setText(null);
            tvLongitude.setText(null);
            tvHorAccuracy.setText(null);
            tvAltitude.setText(null);
            tvAltitudeAccuracy.setText(null);

            tvHeading.setText(null);
            tvHeadingAccuracy.setText(null);

            tvLastError.setText(null);
            tvFloor.setText(null);
        }
        ip.start();

        userStarted = true;
    }

    public void onClickStop(View view) {
        ip.stop();
        tbStub.setEnabled(true);
        userStarted = false;
    }

    public void onClickStub(View view) {
        if (tbStub.isChecked()) {
            ip.setMode(IndoorPositioningMode.SIMULATION);
        } else {
            ip.setMode(IndoorPositioningMode.DEFAULT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ip.register(indoorPositioningListener, handler);

        if (userStarted) {
            onClickStart(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ip.isRunning()) {
            ip.stop();
        }
        ip.unregister();
    }

    /**
     * {@link IndoorPositioningListener} instance.
     */
    private Listener indoorPositioningListener = new Listener() {

        @Override
        public void didUpdateHeading(Map<String, Object> heading) {

            Locale l = Locale.getDefault();

            Float headingDegrees = (Float) heading.get(Listener.HEADING_DEGREES);
            tvHeading.setText(headingDegrees != null ? String.format(l, "%.2f°", headingDegrees) : null);

            Float headingAccuracy = (Float) heading.get(Listener.HEADING_ACCURACY);
            tvHeadingAccuracy.setText(headingAccuracy != null ? String.format(l, "%.0f°", headingAccuracy) : null);
        }

        @Override
        public void didUpdateLocation(Map<String, Object> location) {

            Locale l = Locale.getDefault();

            Double lat = (Double) location.get(Listener.LOCATION_LATITUDE);
            tvLatitude.setText(lat != null ? String.format(l, "%f°", lat) : null);

            Double lon = (Double) location.get(Listener.LOCATION_LONGITUDE);
            tvLongitude.setText(lon != null ? String.format(l, "%f°", lon) : null);

            Float horAccuracy = (Float) location.get(Listener.LOCATION_HORIZONTAL_ACCURACY);
            tvHorAccuracy.setText(horAccuracy != null ? String.format(l, "%.2f m", horAccuracy) : null);

            Double alt = (Double) location.get(Listener.LOCATION_ALTITUDE);
            tvAltitude.setText(alt != null ? String.format(l, "%.2f m", alt) : "");

            Float altAccuracy = (Float) location.get(Listener.LOCATION_VERTICAL_ACCURACY);
            tvAltitudeAccuracy.setText(altAccuracy != null ? String.format(l, "%.2f m", altAccuracy) : null);

            Integer floor = (Integer) location.get(Listener.LOCATION_FLOOR_LEVEL);
            tvFloor.setText(floor != null ? String.format(l, "%d", floor) : "Unknown");
        }

        @Override
        public void didFailWithError(Error error) {
            tvLastError.setText(error.toString());
        }
    };
}
