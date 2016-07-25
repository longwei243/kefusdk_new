package com.m7.imkfsdk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by longwei on 2016/3/14.
 */
public class SettingActivity extends Activity {

    private EditText et_accessId, et_name, et_userId;
    private Button btn_save;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kf_activity_setting);

        sp = getSharedPreferences("setting", 0);
        editor = sp.edit();

        et_accessId = (EditText) findViewById(R.id.et_accessId);
        et_name = (EditText) findViewById(R.id.et_name);
        et_userId = (EditText) findViewById(R.id.et_userId);
        btn_save = (Button) findViewById(R.id.btn_save);

        if(!"".equals(sp.getString("accessId", ""))) {
            String accessId = sp.getString("accessId", "");
            String name = sp.getString("name", "");
            String userId = sp.getString("userId", "");
            et_accessId.setText(accessId);
            et_name.setText(name);
            et_userId.setText(userId);
        }

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accessIdStr = et_accessId.getText().toString().trim();
                String nameStr = et_name.getText().toString().trim();
                String userIdStr = et_userId.getText().toString().trim();

                if(accessIdStr != null && !"".equals(accessIdStr)
                        && nameStr != null && !"".equals(nameStr)
                        && userIdStr != null && !"".equals(userIdStr)) {

                    editor.putString("accessId", accessIdStr);
                    editor.putString("name", nameStr);
                    editor.putString("userId", userIdStr);
                    editor.commit();

                    finish();
                }else {
                    Toast.makeText(SettingActivity.this, "请填写所有参数", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
