package ru.korostylev.notepad.notepad.fragments;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import ru.korostylev.notepad.notepad.MainActivity;
import ru.korostylev.notepad.notepad.R;

public class PageFragment extends Fragment {
    public ImageButton back;
    public ImageView logo;
    private int numPage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Отмена пересоздания при повороте
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);

        if (getArguments() != null)
            numPage = getArguments().getInt("numPage");
        else
            numPage = MainActivity.numPage;

        logo = (ImageView) view.findViewById(R.id.page_logo);

        back = (ImageButton) view.findViewById(R.id.page_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.myBackstack == 1)
                    MainActivity.exit = true;

                else if (MainActivity.myBackstack == 2 || MainActivity.myBackstack == 3) {
                    RecordsFragment records = new RecordsFragment();
                    Bundle args = new Bundle();
                    args.putInt("numPage", numPage);
                    records.setArguments(args);

                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.page_container, records);
                    transaction.commit();

                    logo.setVisibility(View.VISIBLE);
                    back.setVisibility(View.GONE);
                    MainActivity.myBackstack = 1; // MAIN
                }
                else if (MainActivity.myBackstack == 4) {
                    RecordFragment fragment = new RecordFragment();
                    Bundle args = new Bundle();
                    args.putInt("tabID", numPage);
                    args.putInt("recordID", MainActivity.numRec);
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.page_container, fragment);
                    transaction.commit();

                    MainActivity.myBackstack = 2; // "Информация"
                }
            }
        });

        RecordsFragment records = new RecordsFragment();
        Bundle args = new Bundle();
        args.putInt("numPage", numPage);
        records.setArguments(args);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (getChildFragmentManager().findFragmentById(R.id.page_container) == null)
            transaction.add(R.id.page_container, records);
        else
            transaction.replace(R.id.page_container, records);
        transaction.commit();

        logo.setVisibility(View.VISIBLE);
        back.setVisibility(View.GONE);
        MainActivity.myBackstack = 1; // MAIN

        return view;
    }
}
