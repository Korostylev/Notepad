package ru.korostylev.notepad.notepad;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import ru.korostylev.notepad.notepad.fragments.PageFragment;
import ru.korostylev.notepad.notepad.fragments.RecordCreateFragment;
import ru.korostylev.notepad.notepad.fragments.RecordFragment;
import ru.korostylev.notepad.notepad.fragments.RecordsFragment;

public class MainActivity extends AppCompatActivity {
    public static boolean exit = false;
    public static byte myBackstack = 1;
    public static int numPage = 1;
    public static int numRec = 0;
    public static PageFragment[] pages = new PageFragment[3];

//    public static final int[] TAG_TABS = {R.id.notes_container, R.id.cases_container, R.id.old_cases_container};
    public static final int[] TAG_TABS = {0, 1, 2};
    public static final String[] NAME_TABS = {"Заметки", "Дела", "Старое"};

    // настройки
    private static final String SETTING_NOTIFICATION_CHECK = "checknotification";

    ViewPager pager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (pages[0] == null) {  // При повороте не пересоздает
            for (int i = 0; i < pages.length; i++) {
                pages[i] = new PageFragment();
                Bundle args = new Bundle();
                args.putInt("numPage", i);
                pages[i].setArguments(args);
            }
        }

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                while (myBackstack > 1)
                    pages[numPage].back.performClick();

                numPage = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setCurrentItem(numPage);
        // OLD

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.getBoolean(SETTING_NOTIFICATION_CHECK, true)) {
            //everyDayNotification();
            NotificationHelper.createEverydayNotification(this, 8, 0);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(SETTING_NOTIFICATION_CHECK, false);
            editor.apply();
        }
    }

    @Override
    public void onBackPressed() {
        pages[numPage].back.performClick();

        if (exit)
            super.onBackPressed();

        exit = false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Скрыть клавиатуру
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

        return super.dispatchTouchEvent(ev);
    }
}
