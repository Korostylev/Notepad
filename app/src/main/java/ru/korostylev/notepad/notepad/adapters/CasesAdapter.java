package ru.korostylev.notepad.notepad.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.korostylev.notepad.notepad.R;
import ru.korostylev.notepad.notepad.items.CaseItem;

public class CasesAdapter extends BaseAdapter {
    private ArrayList<CaseItem> data = new ArrayList<CaseItem>();
    private Context context;

    public CasesAdapter(Context context, ArrayList<CaseItem> array) {
        this.context = context;
        if (array != null)
            data = array;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //Получение объекта inflater из контекста
        LayoutInflater inflater = LayoutInflater.from(context);
        //Если someView (View из ListView) вдруг оказался равен
        //null тогда мы загружаем его с помошью inflater
        if (view == null) {
            view = inflater.inflate(R.layout.item_case, viewGroup, false);
        }
        //Обявляем наши текствьюшки и связываем их с разметкой
        TextView title = (TextView) view.findViewById(R.id.item_case_title);
        TextView time = (TextView) view.findViewById(R.id.item_case_time);
        TextView date = (TextView) view.findViewById(R.id.item_case_date);

        //Устанавливаем в каждую текствьюшку соответствующий текст
        title.setText(data.get(i).title);
        time.setText(data.get(i).time);
        date.setText(data.get(i).date);

        return view;
    }
}
