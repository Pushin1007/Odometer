package com.example.odometer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Random;


public class OdometerService extends Service {
    private final IBinder binder = new OdometerBinder();
    private final Random random = new Random();// временно возвращаем случайное число

    public class OdometerBinder extends Binder {
        OdometerService getOdometer() { //Метод используется активностью для получения ссылки на OdometerService.
            return OdometerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public double getDistance() {//получаем случайное вещественное число
        return random.nextDouble();
    }
}