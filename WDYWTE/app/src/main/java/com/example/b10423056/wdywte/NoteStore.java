package com.example.b10423056.wdywte;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NoteStore extends AppCompatActivity implements AdapterView.OnItemClickListener {
    //database
    static final String db_name="wdywteDB";
    static final String tb_name="store";
    //static final String[] FROM = new String[] {"storeName", "weight", "freq"};
    SQLiteDatabase db;

    EditText input;
    Button newBtn;
    ListView lvStore;
    Cursor c ;
    ArrayList<String> storeList = new ArrayList<String>();    //儲存 Note 裡邊的資料
    ArrayList<String> weightList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    //SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_store);

        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新建");
        builder.setIcon(android.R.drawable.ic_input_add);
        builder.setMessage("請輸入名稱：");

        input = new EditText(this);
        builder.setView(input);

        //SET positive button
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String str = input.getText().toString().replaceAll("[\\t\\n\\r]+"," ");
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                input.setText("");

                addData(str);
                query();
            }
        });

        //Set negative button
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                input.setText("");
            }
        });

        //Create the dialog
        final AlertDialog ad = builder.create();

        //=========================================================

        //button
        newBtn = (Button) findViewById(R.id.btn_new);
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.show();
            }
        });

        //database
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        //create table
        String createTable = "CREATE TABLE IF NOT EXISTS " + tb_name +
                "(storeName VARCHAR(255) PRIMARY KEY NOT NULL, " +
                "weight DOUBLE default 3, " +
                "freq INTEGER default 0) ";
        db.execSQL(createTable);

        //DROP TABLE
        //String delTable = "DROP TABLE " + tb_name;
        //db.execSQL(delTable);

        //listView
        lvStore = (ListView) findViewById(R.id.lv_store);

        //Adapter
        adapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, storeList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.GREEN);

                // Generate ListView Item using TextView
                return view;
            }
        };
        lvStore.setAdapter(adapter);
        lvStore.setOnItemClickListener(this);
        query();

    }
    private void addData(String name)
    {
        ContentValues  cv = new ContentValues(3);
        cv.put("storeName", name);

        db.insert(tb_name, null, cv);
    }

    private void delData(String name)
    {
        db.delete(tb_name, "storeName = "+name,null);
        query();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
        final String str = storeList.get(index).split("已")[0].replaceAll("\\s","");
        AlertDialog.Builder bbb = new AlertDialog.Builder(this).setMessage("你確定要刪除\"" + str + "\"嗎？")
                .setCancelable(true)
                .setIcon(android.R.drawable.ic_delete)
                .setTitle("確定？");
        bbb.setPositiveButton("刪除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                delData("'"+str+"'");
            }
        });
        bbb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alert = bbb.create();
        alert.show();
    }

    private void query()
    {
        c = db.rawQuery("SELECT * FROM " + tb_name, null);
        storeList.clear();
        if(c.moveToFirst())
        {
            String sName;
            int freq;
            do {
                sName = c.getString(0);
                freq = c.getInt(2);
                sName = sName + "           已去過次數：" + freq;
                storeList.add(sName);
            }while(c.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }

    public void onBack(View v)
    {
        db.close();
        setResult(RESULT_OK);
        finish();
    }

}
