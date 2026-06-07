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

public class SensorGraphFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_UNIT = "unit";
    private static final String ARG_USE_MAGNITUDE = "use_magnitude";

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView txtSensorTitle;
    private TextView txtSensorValue;
    private LineChartView lineChartView;

    private int sensorType;
    private String title;
    private String unit;
    private boolean useMagnitude;

    public static SensorGraphFragment newInstance(int sensorType, String title, String unit, boolean useMagnitude) {
        SensorGraphFragment fragment = new SensorGraphFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_UNIT, unit);
        args.putBoolean(ARG_USE_MAGNITUDE, useMagnitude);

        fragment.setArguments(args);
        return fragment;
    }

    public SensorGraphFragment() {
        // Constructeur vide obligatoire pour les fragments
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            sensorType = getArguments().getInt(ARG_SENSOR_TYPE);
            title = getArguments().getString(ARG_TITLE);
            unit = getArguments().getString(ARG_UNIT);
            useMagnitude = getArguments().getBoolean(ARG_USE_MAGNITUDE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtSensorTitle = view.findViewById(R.id.txtSensorTitle);
        txtSensorValue = view.findViewById(R.id.txtSensorValue);
        lineChartView = view.findViewById(R.id.lineChartSensor);

        txtSensorTitle.setText(title);
        lineChartView.setTitle(title);
        lineChartView.setUnit(unit);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        if (sensor == null) {
            txtSensorValue.setText("Ce capteur n'est pas disponible sur cet appareil.");
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
        float value;

        if (useMagnitude && event.values.length >= 3) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            value = (float) Math.sqrt(x * x + y * y + z * z);
        } else {
            value = event.values[0];
        }

        txtSensorValue.setText("Valeur : " + value + " " + unit);
        lineChartView.addValue(value);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé dans ce TP
    }
}
