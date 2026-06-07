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

public class ActivityRecognitionFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView txtActivityStatus;
    private TextView txtDetectedActivity;
    private TextView txtAccelerationValues;
    private TextView txtMovementValue;

    private LineChartView lineChartActivity;

    private final float[] gravity = new float[3];
    private final float[] linearAcceleration = new float[3];

    private static final float ALPHA = 0.8f;

    public ActivityRecognitionFragment() {
        // Constructeur vide obligatoire
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_recognition, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtActivityStatus = view.findViewById(R.id.txtActivityStatus);
        txtDetectedActivity = view.findViewById(R.id.txtDetectedActivity);
        txtAccelerationValues = view.findViewById(R.id.txtAccelerationValues);
        txtMovementValue = view.findViewById(R.id.txtMovementValue);
        lineChartActivity = view.findViewById(R.id.lineChartActivity);

        lineChartActivity.setTitle("Intensité du mouvement");
        lineChartActivity.setUnit("m/s²");

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null) {
            txtActivityStatus.setText("Accéléromètre non disponible sur cet appareil.");
        } else {
            txtActivityStatus.setText("Capteur détecté : " + accelerometer.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
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
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        linearAcceleration[0] = x - gravity[0];
        linearAcceleration[1] = y - gravity[1];
        linearAcceleration[2] = z - gravity[2];

        float movementMagnitude = (float) Math.sqrt(
                linearAcceleration[0] * linearAcceleration[0]
                        + linearAcceleration[1] * linearAcceleration[1]
                        + linearAcceleration[2] * linearAcceleration[2]
        );

        String activity = detectActivity(movementMagnitude, gravity);

        txtAccelerationValues.setText(
                "X : " + format(x) + " | Y : " + format(y) + " | Z : " + format(z)
        );

        txtMovementValue.setText(
                "Mouvement réel : " + format(movementMagnitude) + " m/s²"
        );

        txtDetectedActivity.setText("Activité : " + activity);

        lineChartActivity.addValue(movementMagnitude);
    }

    private String detectActivity(float movementMagnitude, float[] gravityValues) {

        if (movementMagnitude > 8.0f) {
            return "Sauter";
        }

        if (movementMagnitude > 1.5f) {
            return "Marcher";
        }

        float verticalValue = Math.abs(gravityValues[1]);

        if (verticalValue > 7.0f) {
            return "Debout / immobile";
        } else {
            return "Assis / faible mouvement";
        }
    }

    private String format(float value) {
        return String.format("%.2f", value);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé dans ce TP
    }
}
