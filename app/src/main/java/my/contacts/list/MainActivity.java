package my.contacts.list;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    EditText name;
    EditText phone;
    SQLiteDatabase db;
    DBHelper helper;
    MyAdapter adapter;
    ArrayList<Contact> al;

    boolean nameEmpty = true;
    boolean phoneEmpty = true;
    int selected = -1;
    InputMethodManager imm;
    int ii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = findViewById(R.id.list);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        helper = new DBHelper(this);
        al = new ArrayList<>();

        setAddEnable(false);
        Log.d("enenen", "" + false);
        setUpdateEnable(false);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        getAllContacts();

        adapter = new MyAdapter(this, al, R.layout.row);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                findViewById(R.id.ll).setVisibility(View.VISIBLE);
                findViewById(R.id.addbutton).setVisibility(View.GONE);
                selected = position;
                setAddEnable(false);
                name.setText(al.get(position).name);
                phone.setText(al.get(position).phone);
            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (name.getText().toString().length() != 0)
                    nameEmpty = false;
                else
                    nameEmpty = true;
                setAddEnable(name.getText().toString().length() != 0 && !phoneEmpty && selected == -1);
                setUpdateEnable(/*count != 0 && */name.getText().toString().length() != 0 && !phoneEmpty && selected != -1);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (phone.getText().toString().length() != 0)
                    phoneEmpty = false;
                else
                    phoneEmpty = true;
                setAddEnable(phone.getText().toString().length() != 0 && !nameEmpty && selected == -1);
                Log.d("enenen", "109줄 " + (name.getText().toString().length() != 0 && !phoneEmpty && selected == -1));
                setUpdateEnable(phone.getText().toString().length() != 0 && !nameEmpty && selected != -1);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    void getAllContacts() {
        db = helper.getReadableDatabase();
        Cursor c = db.query("contacts", new String[]{"_id", "name", "phone"}, null, null, null, null, null);
        while (c.moveToNext()) {
            al.add(new Contact(c.getLong(0), R.drawable.face, c.getString(1)/*이름*/, c.getString(2)/*폰번호*/));
        }
        c.close();
        helper.close();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                db = helper.getWritableDatabase();
                ContentValues values;
                String n = name.getText().toString(); //getText는 String 타입이 아니라 CharSequence 타입이기 때문에
                String p = phone.getText().toString();
                values = new ContentValues();
                values.put("name", n);
                values.put("phone", p);
                long id = db.insert("contacts", null, values);
                Contact contact = new Contact(id, R.drawable.face, n, p);
                al.add(contact);
                name.setText("");
                phone.setText("");
                adapter.notifyDataSetChanged();
                findViewById(R.id.ll).setVisibility(View.GONE);
                findViewById(R.id.addbutton).setVisibility(View.VISIBLE);
                break;
            case R.id.update:
                db = helper.getWritableDatabase();
                values = new ContentValues();
                values.put("name", name.getText().toString());
                values.put("phone", phone.getText().toString());
                db.update("contacts", values, "_id=" + al.get(selected).id, null);
                al.get(selected).name = name.getText().toString();
                al.get(selected).phone = phone.getText().toString();
                name.setText("");
                phone.setText("");
                adapter.notifyDataSetChanged();
                selected = -1;
                findViewById(R.id.ll).setVisibility(View.GONE);
                findViewById(R.id.addbutton).setVisibility(View.VISIBLE);
                break;
            case R.id.cancel:
                findViewById(R.id.ll).setVisibility(View.GONE);
                findViewById(R.id.addbutton).setVisibility(View.VISIBLE);
                name.setText("");
                phone.setText("");
        }
        imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(phone.getWindowToken(), 0);
    }

    public void add(View v) {
        findViewById(R.id.ll).setVisibility(View.VISIBLE);
        findViewById(R.id.addbutton).setVisibility(View.GONE);
        setUpdateEnable(false);
        selected = -1;
    }

    void setAddEnable(boolean enabled) {
        findViewById(R.id.add).setEnabled(enabled);
    }

    void setUpdateEnable(boolean enabled) {
        findViewById(R.id.update).setEnabled(enabled);
    }


        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + al.get(ii).phone));
                startActivity(intent);
            }
        }
    }
    class MyAdapter extends BaseAdapter {

        Activity context;
        ArrayList<Contact> al;
        int layout;


        public MyAdapter(Activity context, ArrayList<Contact> al, int layout) {
            this.context = context;
            this.al = al;
            this.layout = layout;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final int i = position;
            if (view == null)
                view = View.inflate(context, layout, null);
            ImageView image = view.findViewById(R.id.profile);
            TextView name = view.findViewById(R.id.namefield);
            TextView phone = view.findViewById(R.id.phonefield);
            Button call = view.findViewById(R.id.call);
            Button delBtn = view.findViewById(R.id.delbtn);
            image.setImageResource(al.get(position).profile);
            name.setText(al.get(position).name);
            phone.setText(al.get(position).phone);
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    ii = i;
                    int permissionResult = context.checkSelfPermission(Manifest.permission.CALL_PHONE);
                    if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                        intent = new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + al.get(i).phone));
                    } else {
                        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        return;
                    }
                    context.startActivity(intent);
                }
            });
            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBHelper dbHelper = new DBHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Contact contact = al.get(i);
                    db.delete("contacts", "_id=" + contact.id, null);
                    al.remove(i);
                    notifyDataSetChanged();
                }
            });
            return view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return al.size();
        }

        @Override
        public Object getItem(int position) {
            return al.get(position);
        }

    }
}
