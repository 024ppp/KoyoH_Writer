package com.example.administrator.koyoh_writer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.os.Handler;
import android.widget.EditText;
import android.os.Message;
import android.widget.Spinner;
import java.util.Date;
import java.text.SimpleDateFormat;

//public class MainActivity extends AppCompatActivity {
public class MainActivity extends Activity implements View.OnClickListener {
    TextView show;
    Button btnClear;
    RadioGroup radioGroup;
    Handler handler;
    Spinner spinner;
    EditText editText;

    private boolean flag = false;
    private NfcWriter nfcWriter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // view取得
        show = (TextView) findViewById(R.id.show);
        radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        btnClear = (Button) findViewById(R.id.btnClear);
        spinner = (Spinner) findViewById(R.id.spinner);
        editText = (EditText) findViewById(R.id.editText);
        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                // サブスレッドからのメッセージ
                if (msg.what == 0x123) {
                    // 表示する
                    show.append("\n PCから受信-" + msg.obj.toString());
                }
            }
        };

        //Scroll可能にする
        show.setMovementMethod(ScrollingMovementMethod.getInstance());

        findViewById(R.id.btnClear).setOnClickListener(this);
        this.nfcWriter = new NfcWriter(this);
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.btnClear :
                    show.setText("");
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent = this.createPendingIntent();
        // Enable NFC adapter
        this.nfcWriter.enable(this, pendingIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable NFC adapter
        this.nfcWriter.disable(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String txt = "";
        String rBtnText = "";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId != -1) {
            // 選択されているラジオボタンの取得
            RadioButton radioButton = (RadioButton) findViewById(checkedId);// (Fragmentの場合は「getActivity().findViewById」にする)
            // ラジオボタンのテキストを取得
            rBtnText = radioButton.getText().toString();
        } else {
            // 何も選択されていない場合の処理
        }

        //ラヂオボタン選択によって分岐
        if (rBtnText.equals("Read")){
            //a = this.nfcWriter.getIdm(intent);
            txt = this.nfcWriter.getTagText(intent);
            show.append("\n●" + sdf.format(date));
            show.append("\nRead : " + txt);
        }
        else if (rBtnText.equals("Write")){
            //書き込む文字を生成
            txt = spinner.getSelectedItem().toString()
                    + editText.getText();

            if (this.nfcWriter.write(this, intent, txt)) {
                //NFC書き込み成功
                Toast.makeText(this, "ICタグへの書き込みが成功しました。", Toast.LENGTH_SHORT).show();
                show.append("\n●" + sdf.format(date));
                show.append("\nWrite : " + txt);
            } else {
                Toast.makeText(this, this.nfcWriter.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            show.append("\n" + "読み書き選択無し");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.nfcWriter = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
