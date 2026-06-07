package ensa.ma.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ensa.ma.sensors.R;

public class StepCounterFragment extends Fragment implements SensorEventListener {

    private static final int REQUEST_ACTIVITY_RECOGNITION = 100;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private TextView txtStepStatus;
    private TextView txtTotalSteps;
    private TextView txtSessionSteps;

    private int initialSteps = -1;

    public StepCounterFragment() {
        // Constructeur vide obligatoire
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_counter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtStepStatus = view.findViewById(R.id.txtStepStatus);
        txtTotalSteps = view.findViewById(R.id.txtTotalSteps);
        txtSessionSteps = view.findViewById(R.id.txtSessionSteps);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor == null) {
            txtStepStatus.setText("Le capteur de pas n'est pas disponible sur cet appareil.");
        } else {
            txtStepStatus.setText("Capteur détecté : " + stepCounterSensor.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasActivityRecognitionPermission()) {
            startStepCounter();
        } else {
            requestActivityRecognitionPermission();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private boolean hasActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT < 29) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                requireContext(),
                "android.permission.ACTIVITY_RECOGNITION"
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= 29) {
            requestPermissions(
                    new String[]{"android.permission.ACTIVITY_RECOGNITION"},
                    REQUEST_ACTIVITY_RECOGNITION
            );
        }
    }

    private void startStepCounter() {
        if (sensorManager != null && stepCounterSensor != null) {
            sensorManager.registerListener(
                    this,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );

            txtStepStatus.setText("Compteur de pas actif.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int totalSteps = (int) event.values[0];

        if (initialSteps == -1) {
            initialSteps = totalSteps;
        }

        int sessionSteps = totalSteps - initialSteps;

        txtTotalSteps.setText("Pas depuis le dernier redémarrage : " + totalSteps);
        txtSessionSteps.setText("Pas depuis l'ouverture de cette page : " + sessionSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé dans ce TP
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounter();
            } else {
                txtStepStatus.setText("Permission refusée. Le compteur de pas ne peut pas fonctionner.");
            }
        }
    }
}
