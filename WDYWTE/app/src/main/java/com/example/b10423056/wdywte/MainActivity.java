package com.example.b10423056.wdywte;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {

    static final String db_name="wdywteDB";
    static final String tb_name="store";
    SQLiteDatabase db;

    Cursor c ;
    ArrayList<String> storeList = new ArrayList<String>();    //儲存 Note 裡邊的資料
    ArrayList<String> choosenList = new ArrayList<String>();
    //Button
    Button btn_start;
    Button btn_restart;
    Button btn_comfirm;
    ImageButton btn_editNote;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button)findViewById(R.id.btn_start);
        btn_editNote = (ImageButton)findViewById(R.id.btn_editNote);
        btn_restart = (Button)findViewById(R.id.btn_restart);
        btn_comfirm = (Button)findViewById(R.id.btn_comfirm);
        result = (TextView)findViewById(R.id.result);

        choosenListReset();

        btn_start.setOnClickListener(this);

        //database
        if(checkDataBaseExist())
        {
            query();
        }
        else
        {
            //if note nothing
            result.setText("請點擊右下角增加店家");
        }

        String out = "";
        for(String str:storeList)
        {
            out = out + str + "\n";
        }
        //result.setText(out);

    }

    public void onNote(View v)
    {
        Intent it = new Intent(this, NoteStore.class);
        startActivityForResult(it,123);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, getIntent());
        if(resultCode==RESULT_OK && requestCode==123){
            query();
            choosenListReset();
        }
    }

    private void choosenListReset()
    {
        choosenList.clear();
        choosenList.add("你已經抽完全部選項了\n我也幫不了你...");
        choosenList.add("你TMD到底想吃什麼");
    }

    private void query()
    {
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        c = db.rawQuery("SELECT * FROM " + tb_name, null);
        storeList.clear();
        if(c.moveToFirst())
        {
            do {
                String sName = c.getString(0);
                double w = c.getDouble(1);
                while(w>0)
                {
                    storeList.add(sName);
                    w-=1;
                }
            }while(c.moveToNext());
        }
        Collections.shuffle(storeList);
        db.close();
    }

    private boolean checkDataBaseExist() {
        String path = "/data/data/com.example.b10423056.wywte/databases/wdywteDB";
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return checkDB != null;
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    private boolean started = false;
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            final Random random = new Random();
            int i = random.nextInt(storeList.size());
            result.setText(storeList.get(i));
            if(started) {
                handler.postDelayed(this,10);
            }

        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        /*
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            started = true;
            handler.post(runnable);
        }
        if(motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            started = false;
        }
        return true;
        */
        return false;
    }

    @Override
    public void onClick(View view) {
        if(storeList.size() > 0)
        {
            if(started)
            {
                started = false;
                btn_start.setText("START!");
                btn_start.setVisibility(View.INVISIBLE);
                btn_comfirm.setVisibility(View.VISIBLE);
                btn_restart.setVisibility(View.VISIBLE);
            }
            else
            {
                started = true;
                btn_start.setText("STOP");
                btn_visibility_origin();
            }
            handler.post(runnable);
        }
        else if(choosenList.size() > 2)
        {
            Random rnd = new Random();
            result.setText(choosenList.get(rnd.nextInt(2)));
        }
        else
        {
            result.setText("請點擊右下角增加店家");
        }
    }

    private void btn_visibility_origin()
    {
        btn_start.setVisibility(View.VISIBLE);
        btn_comfirm.setVisibility(View.INVISIBLE);
        btn_restart.setVisibility(View.INVISIBLE);
    }

    public void onRestart(View v)
    {
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        String sName = result.getText().toString();
        c = db.rawQuery("SELECT weight FROM " + tb_name +" WHERE storeName='"+sName+"'", null);
        c.moveToFirst();
        double new_weight = c.getDouble(0);
        if(new_weight > 3)
        {
            new_weight-=0.3;
        }
        updateDB("weight",new_weight, sName);
        db.close();
        query();
        choosenList.add(sName);
        storeList.removeAll(choosenList);
        onClick(v);
    }

    public void onComfirm(View v)
    {
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        String sName = result.getText().toString();

        c = db.rawQuery("SELECT weight FROM " + tb_name +" WHERE storeName='"+sName+"'", null);
        c.moveToFirst();
        double new_weight = c.getDouble(0);
        if(new_weight < ((double)storeList.size()/4.0))
        {
            new_weight+=0.7;
        }
        updateDB("weight",new_weight, sName);

        c = db.rawQuery("SELECT freq FROM " + tb_name +" WHERE storeName='"+sName+"'", null);
        c.moveToFirst();
        int new_freq = c.getInt(0)+1;
        updateDB("freq",new_freq, sName);
        query();
        db.close();

        btn_visibility_origin();
        result.setText("你選擇了：\n"+sName+"\n祝你用餐愉快 :)");
    }

    private void updateDB(String weightORfreq, double newI, String sName)
    {
        if(weightORfreq == "freq")
        {
            newI = (int)newI;
        }
        String update ="UPDATE "+ tb_name +" SET "+weightORfreq+" = "+newI+" WHERE storeName ='"+sName+"'";
        c = db.rawQuery(update, null);
        c.moveToFirst();
    }
}
