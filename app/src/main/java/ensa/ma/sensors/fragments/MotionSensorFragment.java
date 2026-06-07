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
import android.widget.TextView;

import ensa.ma.sensors.R;
import ensa.ma.sensors.views.LineChartView;

public class MotionSensorFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_UNIT = "unit";

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView txtMotionTitle;
    private TextView txtMotionStatus;
    private TextView txtX;
    private TextView txtY;
    private TextView txtZ;
    private TextView txtMagnitude;

    private LineChartView lineChartMotion;

    private int sensorType;
    private String title;
    private String unit;

    public MotionSensorFragment() {
        // Constructeur vide obligatoire pour Fragment
    }

    public static MotionSensorFragment newInstance(int sensorType, String title, String unit) {
        MotionSensorFragment fragment = new MotionSensorFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_UNIT, unit);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            sensorType = getArguments().getInt(ARG_SENSOR_TYPE);
            title = getArguments().getString(ARG_TITLE);
            unit = getArguments().getString(ARG_UNIT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_motion_sensor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtMotionTitle = view.findViewById(R.id.txtMotionTitle);
        txtMotionStatus = view.findViewById(R.id.txtMotionStatus);
        txtX = view.findViewById(R.id.txtX);
        txtY = view.findViewById(R.id.txtY);
        txtZ = view.findViewById(R.id.txtZ);
        txtMagnitude = view.findViewById(R.id.txtMagnitude);
        lineChartMotion = view.findViewById(R.id.lineChartMotion);

        txtMotionTitle.setText(title);
        lineChartMotion.setTitle(title);
        lineChartMotion.setUnit(unit);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        if (sensor == null) {
            txtMotionStatus.setText("Ce capteur n'est pas disponible sur cet appareil.");
        } else {
            txtMotionStatus.setText("Capteur détecté : " + sensor.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sensorManager != null && sensor != null) {
            sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
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
        if (event.values.length < 3) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        txtX.setText("X : " + x + " " + unit);
        txtY.setText("Y : " + y + " " + unit);
        txtZ.setText("Z : " + z + " " + unit);
        txtMagnitude.setText("Magnitude : " + magnitude + " " + unit);

        lineChartMotion.addValue(magnitude);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé dans ce TP
    }
}
