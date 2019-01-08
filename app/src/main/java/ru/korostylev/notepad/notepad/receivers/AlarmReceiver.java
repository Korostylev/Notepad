package ru.korostylev.notepad.notepad.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import ru.korostylev.notepad.notepad.DBHelper;
import ru.korostylev.notepad.notepad.MainActivity;
import ru.korostylev.notepad.notepad.NotificationHelper;
import ru.korostylev.notepad.notepad.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean check = false;

        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("cases", null, null, null, null, null, null);

        Calendar calendar = Calendar.getInstance();
        long min = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("hour", 6));
        calendar.set(Calendar.MINUTE, intent.getIntExtra("min", 0));
        calendar.set(Calendar.SECOND, 0);
        long max = calendar.getTimeInMillis();

        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("title");
            int timeColIndex = c.getColumnIndex("time");
            int contentColIndex = c.getColumnIndex("content");

            do {
                if ((c.getLong(timeColIndex) >= min) && (c.getLong(timeColIndex) <= max)) {
                    NotificationHelper.createEasyNotification(context, c.getInt(idColIndex),
                            c.getString(nameColIndex), c.getString(contentColIndex), c.getLong(timeColIndex));

                    check = true;
                }
            } while (c.moveToNext());
        }
        c.close();
        dbHelper.close();

        if (check) {
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentIntent(contentIntent)
                    // обязательные настройки
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Сегодня есть дела")// Текст уведомления
                    .setAutoCancel(true); // автоматически закрыть уведомление после нажатия

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, builder.build());
        }
    }
}
