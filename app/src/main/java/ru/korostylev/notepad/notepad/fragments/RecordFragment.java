package ru.korostylev.notepad.notepad.fragments;

import android.content.Context;
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
import android.widget.TextView;

import java.util.Calendar;

import ru.korostylev.notepad.notepad.DBHelper;
import ru.korostylev.notepad.notepad.MainActivity;
import ru.korostylev.notepad.notepad.NotificationHelper;
import ru.korostylev.notepad.notepad.R;

public class RecordFragment extends Fragment {

    private DBHelper dbHelper;
    private TextView textTitle, textContent, textDateTime;

    private Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, null);

        final int tabID = getArguments().getInt("tabID", MainActivity.numPage);
        final int recordID = getArguments().getInt("recordID", 0);

        dbHelper = new DBHelper(getActivity());
        textTitle = (TextView) view.findViewById(R.id.textView_title);
        textContent = (TextView) view.findViewById(R.id.textView_content);
        textDateTime = (TextView) view.findViewById(R.id.textView_dateTime);

        readRecordByDB(tabID, recordID);

        Button editBtn = (Button) view.findViewById(R.id.button_edit_record);
        editBtn.setText("Изменить");
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordCreateFragment fragment = new RecordCreateFragment();
                Bundle args = new Bundle();
                args.putInt("tabID", tabID);
                args.putInt("recordID", recordID);
                fragment.setArguments(args);

                FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.page_container, fragment);
                transaction.commit();

                MainActivity.myBackstack = 4; // "Редактирование"
                MainActivity.numRec = recordID;
            }
        });

        Button deleteBtn = (Button) view.findViewById(R.id.button_delete_record);
        deleteBtn.setText("Удалить");
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRecordByID(tabID, recordID);

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

    private void readRecordByDB(int tabID, int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String tableName;
        if (tabID == MainActivity.TAG_TABS[0]) {
            tableName = "notes";
            textDateTime.setVisibility(View.GONE);
        }
        else
            tableName = "cases";

        Cursor c = db.query(tableName, null,
                "id = ?", new String[] {Integer.toString(id)},
                null, null, null);

        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex("title");
            int contentColIndex = c.getColumnIndex("content");

            textTitle.setText(c.getString(nameColIndex));
            textContent.setText(c.getString(contentColIndex));

            if (tabID != MainActivity.TAG_TABS[0]) {
                int timeColIndex = c.getColumnIndex("time");
                calendar.setTimeInMillis(c.getLong(timeColIndex));

                String outDateTime = checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "." + checkValue(calendar.get(Calendar.MONTH) + 1) + "."
                        + calendar.get(Calendar.YEAR) + " в "
                        + checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE));
                textDateTime.setText(outDateTime);
            }
        } else {
            textTitle.setText("Отсутствует");
            textContent.setText("Отсутствует");
        }

        c.close();
        dbHelper.close();
    }

    private void deleteRecordByID(int tabID, int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String tableName;
        if (tabID == MainActivity.TAG_TABS[0])
            tableName = "notes";
        else {
            tableName = "cases";
            NotificationHelper.cancelNotification(getContext(), id);
        }

        db.delete(tableName, "id = ?", new String[] {Integer.toString(id)});

        db.close();
        dbHelper.close();
    }

    private String checkValue(int value) {
        if (value < 10)
            return "0" + value;
        else
            return String.valueOf(value);
    }
}
