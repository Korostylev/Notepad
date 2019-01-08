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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;

import ru.korostylev.notepad.notepad.DBHelper;
import ru.korostylev.notepad.notepad.MainActivity;
import ru.korostylev.notepad.notepad.R;
import ru.korostylev.notepad.notepad.adapters.CasesAdapter;
import ru.korostylev.notepad.notepad.items.CaseItem;

public class RecordsFragment extends Fragment {
    private DBHelper dbHelper;
    private ArrayList<Integer> idRecords;
    private ArrayList<String> arrayList;
    private ArrayList<CaseItem> arrayCases;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, null);

        final int tabID = getArguments().getInt("numPage", MainActivity.numPage);

        dbHelper = new DBHelper(getContext());
        ListView records = (ListView) view.findViewById(R.id.list_records);

        readFromDBToArray(tabID);

        if (tabID == MainActivity.TAG_TABS[0]) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_note, arrayList);
            records.setAdapter(adapter);
        }
        else
            records.setAdapter(new CasesAdapter(getContext(), arrayCases));

        records.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RecordFragment fragment = new RecordFragment();
                Bundle args = new Bundle();
                args.putInt("tabID", tabID);
                args.putInt("recordID", idRecords.get(i));
                fragment.setArguments(args);

                FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.page_container, fragment);
                transaction.commit();

                MainActivity.pages[tabID].logo.setVisibility(View.GONE);
                MainActivity.pages[tabID].back.setVisibility(View.VISIBLE);
                MainActivity.myBackstack = 2; // "Информация"
            }
        });


        Button toRecordCreate = (Button) view.findViewById(R.id.button_by_records);

        if (tabID == MainActivity.TAG_TABS[2]) {
            toRecordCreate.setText("Удалить старое");
            toRecordCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteOldRecords();

                    RecordsFragment records = new RecordsFragment();
                    Bundle args = new Bundle();
                    args.putInt("numPage", tabID);
                    records.setArguments(args);
                    FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.page_container, records);
                    transaction.commit();

                    MainActivity.myBackstack = 1; // MAIN
                }
            });
        }
        else {
            toRecordCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordCreateFragment fragment = new RecordCreateFragment();
                    Bundle args = new Bundle();
                    args.putInt("tabID", tabID);
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.page_container, fragment);
                    transaction.commit();

                    MainActivity.pages[tabID].logo.setVisibility(View.GONE);
                    MainActivity.pages[tabID].back.setVisibility(View.VISIBLE);
                    MainActivity.myBackstack = 3; // "Создание"
                }
            });
            if (tabID == MainActivity.TAG_TABS[0])
                toRecordCreate.setText("Добавить заметку");
            if (tabID == MainActivity.TAG_TABS[1])
                toRecordCreate.setText("Довавить запись");
        }

        return view;
    }

    private void readFromDBToArray(int tabID) {
        arrayList = new ArrayList<>();
        arrayCases = new ArrayList<CaseItem>();
        idRecords = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String tableName;
        if (tabID == MainActivity.TAG_TABS[0])
            tableName = "notes";
        else
            tableName = "cases";

        Cursor c = db.query(tableName, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("title");

            if (tabID == MainActivity.TAG_TABS[0]) {
                do {
                    arrayList.add(0, c.getString(nameColIndex));
                    idRecords.add(0, c.getInt(idColIndex));
                } while (c.moveToNext());
            } else {
                int timeColIndex = c.getColumnIndex("time");
                ArrayList<Long> times = new ArrayList<>();
                Calendar calendar = Calendar.getInstance();
                times.add((long) 0);
                long time;
                do {
                    time = c.getLong(timeColIndex);

                    if ((tabID == MainActivity.TAG_TABS[1] && Calendar.getInstance().getTimeInMillis() < time) ||
                            (tabID == MainActivity.TAG_TABS[2] && Calendar.getInstance().getTimeInMillis() > time)) {
                        for (int i = 0; i < times.size(); i++) {
                            if (time < times.get(i) || times.get(i) == 0) {
                                calendar.setTimeInMillis(time);

                                arrayCases.add(i, new CaseItem(c.getString(nameColIndex),
                                        checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "."
                                                + checkValue(calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR),
                                        checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE))));
                                idRecords.add(i, c.getInt(idColIndex));
                                times.add(i, time);
                                break;
                            }
                        }
                    }
                } while (c.moveToNext());
            }
        }

        c.close();
        dbHelper.close();
    }

    private void deleteOldRecords() {
        dbHelper = new DBHelper(getActivity());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (int i = 0; i < idRecords.size(); i++)
            db.delete("cases", "id = ?", new String[] {Integer.toString(idRecords.get(i))});

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
