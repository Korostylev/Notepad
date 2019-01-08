package ru.korostylev.notepad.notepad.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.korostylev.notepad.notepad.NotificationHelper;

public class AlarmBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.createEverydayNotification(context, 8, 0);
    }
}
