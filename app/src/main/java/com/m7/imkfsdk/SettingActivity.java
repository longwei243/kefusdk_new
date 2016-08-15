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

    private EditText et_accessId, et_name, et_userId, et_tcp_ip, et_http_ip;
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
        et_tcp_ip = (EditText) findViewById(R.id.et_tcp_ip);
        et_http_ip = (EditText) findViewById(R.id.et_http_ip);
        btn_save = (Button) findViewById(R.id.btn_save);

        if(!"".equals(sp.getString("accessId", ""))) {
            String accessId = sp.getString("accessId", "1cf5bdb0-c66d-11e5-9875-63635d52845f");
            String name = sp.getString("name", "正式环境测试号0801");
            String userId = sp.getString("userId", "8888");
            String tcp_ip = sp.getString("tcpIp", "115.29.190.253");
            String http_ip = sp.getString("httpIp", "http://115.29.190.253:4999/sdkChat");
            et_accessId.setText(accessId);
            et_name.setText(name);
            et_userId.setText(userId);
            et_tcp_ip.setText(tcp_ip);
            et_http_ip.setText(http_ip);
        }

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accessIdStr = et_accessId.getText().toString().trim();
                String nameStr = et_name.getText().toString().trim();
                String userIdStr = et_userId.getText().toString().trim();
                String tcpIpStr = et_tcp_ip.getText().toString().trim();
                String httpIpStr = et_http_ip.getText().toString().trim();

                if(accessIdStr != null && !"".equals(accessIdStr)
                        && nameStr != null && !"".equals(nameStr)
                        && userIdStr != null && !"".equals(userIdStr)
                        && tcpIpStr != null && !"".equals(tcpIpStr)
                        && httpIpStr != null && !"".equals(httpIpStr)) {

                    editor.putString("accessId", accessIdStr);
                    editor.putString("name", nameStr);
                    editor.putString("userId", userIdStr);
                    editor.putString("tcpIp", tcpIpStr);
                    editor.putString("httpIp", httpIpStr);
                    editor.commit();

                    finish();
                }else {
                    Toast.makeText(SettingActivity.this, "请填写所有参数", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
