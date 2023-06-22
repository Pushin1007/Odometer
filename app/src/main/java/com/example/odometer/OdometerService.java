package com.example.odometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Random;


public class OdometerService extends Service {
    private LocationListener listener;//слушатель позиционирования, приватный чтобы был доступен извне
    private final IBinder binder = new OdometerBinder();
    private final Random random = new Random();// временно возвращаем случайное число

    private static double distanceInMeters;// расстояние
    private static double speed;// скорость

    private static Location lastLocation = null; // последнее местоположение

    private LocationManager locManager;// Для объекта LocationManager создается приватная переменная, чтобы к нему можно было обращаться из других методов.
    public static final String PERMISSION_STRING
            = android.Manifest.permission.ACCESS_FINE_LOCATION; //Строка разрешения добавляется в виде константы.

    @Override
    public void onCreate() {
        super.onCreate();
        listener = new LocationListener() {// создаем слушателя
            @Override
            public void onLocationChanged(@NonNull Location location) { //Location описывает текущее местоположение
                if (lastLocation == null) {
                    lastLocation = location; //Задает исходное местонахождение пользователя.
                }
                distanceInMeters += location.distanceTo(lastLocation); //Обновляет пройденное расстояние и последнее местонахождение пользователя.
                lastLocation = location;
                speed = location.getSpeed();// можно показывать скорость
            }

            @Override
            public void onFlushComplete(int requestCode) {
                LocationListener.super.onFlushComplete(requestCode);
            }

            @Override
            public void onProviderDisabled(String arg0) {
            }

            @Override
            public void onProviderEnabled(String arg0) {
            }

            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle bundle) {
            }
        };
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //Получить объект LocationManager.
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING) // проверить наличие разрешения
                == PackageManager.PERMISSION_GRANTED) {
            String provider = locManager.getBestProvider(new Criteria(), true); //Получить самого точного  провайдера местоположения
            if (provider != null) {
                locManager.requestLocationUpdates(provider, 1000, 1, listener);
            }
        }

    }

    public class OdometerBinder extends Binder {
        OdometerService getOdometer() { //Метод используется активностью для получения ссылки на OdometerService.
            return OdometerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public double getDistance() {

        // Расстояние в метрах преобразуется в мили. При желании точность вычислений можно было бы повысить, но для наших целей хватит и этой точности.
        return distanceInMeters;
//return this.distanceInMeters / 1609.344;
    }

    public double getSpeed() {
        return speed;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locManager != null && listener != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                    == PackageManager.PERMISSION_GRANTED) {
                locManager.removeUpdates(listener); //Прекратить получение обновлений (если имеется разрешение  на их удаление).
            }
            locManager = null;
            listener = null;
        }
    }
}