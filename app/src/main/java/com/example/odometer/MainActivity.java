package com.example.odometer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private OdometerService odometer;//Используется для OdometerService.
    private boolean bound = false;//Хранит информацию о том, связана активность со службой или нет.

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, OdometerService.class); //Интент, отправленный OdometerService.
        // связывание службы производится при запуске активности
        bindService(intent, connection,//Объект ServiceConnection.
                Context.BIND_AUTO_CREATE); //Метод bindService() использует интент и соединение со службой для связывания активности со службой.
        //флаг Context.BIND_AUTO_CREATE приказывает Android создать службу, если она еще не существует.
    }

    private ServiceConnection connection = new ServiceConnection() {//Необходимо определить объект ServiceConnection.
        @Override
        //Код, выполняемый при связывании со службой
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            //ComponentName идентифицирует службу, он включает имена пакета и класса службы
            OdometerService.OdometerBinder odometerBinder =
                    (OdometerService.OdometerBinder) binder;
            odometer = odometerBinder.getOdometer();//Получить ссылку на OdometerService при установлении связи со службой.
            bound = true; // активность связывается со службой и присваивается  true
        }

        @Override
        //Код, выполняемый при разрыве связи со службой
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayDistance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);//связывание отменчется при остановке активности
            bound = false;
        }
    }

    private void displayDistance() {//Вывод значения, возвращенного методом getDistance() службы.
        final TextView distanceView = (TextView) findViewById(R.id.distance);
        final Handler handler = new Handler();// создаем объект  Handler
        handler.post(new Runnable() { //Вызвать метод post() класса Handler, передав ему объект Runnable.
            @Override
            public void run() {
                double distance = 0.0;
                if (bound && odometer != null) {
                    distance = odometer.getDistance();// Вызывает метод getDistance() службы OdometerService.
                }
                String distanceStr = String.format(Locale.getDefault(),
                        "%1$,.2f miles", distance);
                distanceView.setText(distanceStr);
                handler.postDelayed(this, 1000); // значение обновляется каждую секунду
            }
        });
    }
}