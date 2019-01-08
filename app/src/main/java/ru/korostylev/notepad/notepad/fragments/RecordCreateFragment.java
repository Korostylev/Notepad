package ru.korostylev.notepad.notepad.fragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

import ru.korostylev.notepad.notepad.DBHelper;
import ru.korostylev.notepad.notepad.MainActivity;
import ru.korostylev.notepad.notepad.R;
import ru.korostylev.notepad.notepad.receivers.NotificationReceiver;

public class RecordCreateFragment extends Fragment {

    private DBHelper dbHelper;
    private Calendar calendar = Calendar.getInstance();
    private EditText editTitle, editContent;
    private Button createRecord, editDate, editTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_creater, null);

        final int tabID = getArguments().getInt("tabID", MainActivity.numPage);
        final int recordID = getArguments().getInt("recordID", 0);

        dbHelper = new DBHelper(getContext());
        editTitle = (EditText) view.findViewById(R.id.edit_title);
        editContent = (EditText) view.findViewById(R.id.edit_content);

        editDate = (Button) view.findViewById(R.id.button_edit_date);
        editTime = (Button) view.findViewById(R.id.button_edit_time);
        createRecord = (Button) view.findViewById(R.id.button_create_record);

        if (tabID == MainActivity.TAG_TABS[0]) {
            editDate.setVisibility(View.GONE);
            editTime.setVisibility(View.GONE);
            createRecord.setText("Довавить заметку");
        } else {
            editDate.setText("Выбрать день");
            editDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DatePickerDialog(getActivity(), d, calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            editTime.setText("Выбрать время");
            editTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new TimePickerDialog(getActivity(), t, calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE), true).show();
                }
            });
            createRecord.setText("Довавить запись");
        }

        if (recordID != 0) {
            createRecord.setText("Сохранить изменения");
            readRecordByDB(tabID, recordID);
        }

        createRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createRecordDB(tabID, recordID, String.valueOf(editTitle.getText()), String.valueOf(editContent.getText()));

                RecordsFragment records = new RecordsFragment();
                Bundle args = new Bundle();
                args.putInt("numPage", tabID);
                records.setArguments(args);
                FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.page_container, records);
                transaction.commit();

                MainActivity.pages[tabID].logo.setVisibility(View.VISIBLE);
                MainActivity.pages[tabID].back.setVisibility(View.GONE);
                MainActivity.myBackstack = 1; // MAIN
            }
        });

        return view;
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            calendar.set(Calendar.YEAR, i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);

            editDate.setText(checkValue(i2) + "." + checkValue(i1 + 1) + "." + i);
        }
    };

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            calendar.set(Calendar.HOUR_OF_DAY, i);
            calendar.set(Calendar.MINUTE, i1);

            editTime.setText(checkValue(i) + ":" + checkValue(i1));
        }
    };

    private void createRecordDB(int tabID, int recordID, String title, String content) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        cv.put("title", title);
        cv.put("content", content);

        String tableName;
        if (tabID == MainActivity.TAG_TABS[0]) {
            tableName = "notes";
        } else {
            cv.put("time", calendar.getTimeInMillis());
            tableName = "cases";
        }

        int notifID = 0;

        if (recordID == 0)
            notifID = (int) db.insert(tableName, null, cv);
        else {
            db.update(tableName, cv, "id = ?", new String[]{String.valueOf(recordID)});
            notifID = recordID;
        }

        if (tabID == MainActivity.TAG_TABS[1])
            createNotification(notifID, title, content, calendar.getTimeInMillis());

        db.close();
        dbHelper.close();
    }

    private void readRecordByDB(int tabID, int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String tableName;
        if (tabID == MainActivity.TAG_TABS[0])
            tableName = "notes";
        else
            tableName = "cases";

        Cursor c = db.query(tableName, null,
                "id = ?", new String[] {Integer.toString(id)},
                null, null, null);

        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex("title");
            int contentColIndex = c.getColumnIndex("content");

            editTitle.setText(c.getString(nameColIndex));
            editContent.setText(c.getString(contentColIndex));

            if (tabID != MainActivity.TAG_TABS[0]) {
                int timeColIndex = c.getColumnIndex("time");
                calendar.setTimeInMillis(c.getLong(timeColIndex));

                editDate.setText(checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "."
                        + checkValue(calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR));
                editTime.setText(checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE)));
            }
        } else {
            editTitle.setText("Отсутствует");
            editContent.setText("Отсутствует");
        }

        c.close();
        dbHelper.close();
    }

    private void createNotification(int id, String title, String text, long time) {
        Intent intent = new Intent(getActivity(), NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("id", id);
        PendingIntent pending = PendingIntent.getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, time, 0, pending);
    }

    private String checkValue(int value) {
        if (value < 10)
            return "0" + value;
        else
            return String.valueOf(value);
    }
}
