package com.eventshare.eventshare;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;


public class LvData<ObjectType, Adapter extends ArrayAdapter<ObjectType>> {

    private ListView listView;
    private List<ObjectType> objectList;
    private Adapter adapter;

    public LvData(ListView listView, /*Adapter adapter,*/ List<ObjectType> list, Context context) {
        this.listView = listView;
        this.objectList = list;
               // this.adapter = adapter;
               // = new Adapter(context.getApplicationContext(), 0, objectList);
    }

    public List<ObjectType> getList() {
        return objectList;
    }

    public void setAdapter(Adapter ad) {
        this.adapter = ad;
        listView.setAdapter(adapter);
    }

    public void add(ObjectType o) {
        objectList.add(o);
    }

    public void addFirst(ObjectType o) {
        objectList.add(0, o);
    }

    public void refresh() {
        adapter.notifyDataSetChanged(); // update adapter
        listView.invalidate(); // redraw listview
        listView.setSelection(0);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public Object getItemAtPosition(int pos) {
        return listView.getItemAtPosition(pos);
    }
}
