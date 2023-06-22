package com.example.odometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private OdometerService odometer;//Используется для OdometerService.
    private boolean bound = false;//Хранит информацию о том, связана активность со службой или нет.
    private final int PERMISSION_REQUEST_CODE = 698;// значение используемое для запроса разрешения
    private final int NOTIFICATION_ID = 423;//Константа для идентификатора уведомления.

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, OdometerService.PERMISSION_STRING)
                //если разрешение небыло предоставлено
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,//запросить его во время выполнения
                    new String[]{OdometerService.PERMISSION_STRING},
                    PERMISSION_REQUEST_CODE);
        } else {
//если разрешение уже было выдано, выполнить связывание со службой
            Intent intent = new Intent(this, OdometerService.class); //Интент, отправленный OdometerService.
            // связывание службы производится при запуске активности
            bindService(intent, connection,//Объект ServiceConnection.
                    Context.BIND_AUTO_CREATE); //Метод bindService() использует интент и соединение со службой для связывания активности со службой.
            //флаг Context.BIND_AUTO_CREATE приказывает Android создать службу, если она еще не существует.
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //Метод onRequestPermissionsResult() возвращает результаты ваших запросов разрешений.
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: { //Проверить, совпадает ли код с тем, который был использован в методе requestPermissions().
                //Если запрос был отменен, результаты не возвращаются.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, OdometerService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                } else {

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            //Эти настройки необходимы для всех уведомлений.
                            .setSmallIcon(android.R.drawable.ic_menu_compass)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(getResources().getString(R.string.permission_denied))
                            //А эти — только для всплывающих уведомлений.
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVibrate(new long[]{1000, 1000})
                            //уведомление исчезает плсле щелчка
                            .setAutoCancel(true);
                    //создание действия
                    Intent actionIntent = new Intent(this, MainActivity.class);
                    //Добавление PendingIntent к уведомлению означает, что уведомление будет запускать MainActivity по щелчку.
                    PendingIntent actionPendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(actionPendingIntent);
//Выдача уведомления
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        }
    }
}