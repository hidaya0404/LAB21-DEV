package ensa.ma.sensors.fragments;



import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ensa.ma.sensors.R;

public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private TextView tvHeading;
    private ImageView imageViewCompass;

    private float[] gravityValues;
    private float[] magneticValues;

    private float currentDegree = 0f;

    public CompassFragment() {
        // Constructeur vide obligatoire
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHeading = view.findViewById(R.id.tvHeading);
        imageViewCompass = view.findViewById(R.id.imageViewCompass);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (accelerometer == null || magnetometer == null) {
            tvHeading.setText("Accéléromètre ou magnétomètre non disponible");
        } else {
            tvHeading.setText("Heading: 0.0°");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_UI
                );
            }

            if (magnetometer != null) {
                sensorManager.registerListener(
                        this,
                        magnetometer,
                        SensorManager.SENSOR_DELAY_UI
                );
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = event.values.clone();
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values.clone();
        }

        if (gravityValues != null && magneticValues != null) {
            calculateDirection();
        }
    }

    private void calculateDirection() {
        float[] rotationMatrix = new float[9];
        float[] orientationValues = new float[3];

        boolean success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                gravityValues,
                magneticValues
        );

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            float azimuthRadians = orientationValues[0];
            float azimuthDegrees = (float) Math.toDegrees(azimuthRadians);

            if (azimuthDegrees < 0) {
                azimuthDegrees += 360;
            }

            String direction = getDirectionName(azimuthDegrees);

            tvHeading.setText("Heading: " + Math.round(azimuthDegrees) + "° - " + direction);

            imageViewCompass.setRotation(-azimuthDegrees);

            currentDegree = -azimuthDegrees;
        }
    }

    private String getDirectionName(float degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "Nord";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "Nord-Est";
        } else if (degree >= 67.5 && degree < 112.5) {
            return "Est";
        } else if (degree >= 112.5 && degree < 157.5) {
            return "Sud-Est";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "Sud";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "Sud-Ouest";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "Ouest";
        } else {
            return "Nord-Ouest";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }
}
