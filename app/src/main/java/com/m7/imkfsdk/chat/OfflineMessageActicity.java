package com.m7.imkfsdk.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.m7.imkfsdk.R;
import com.m7.imkfsdk.chat.holder.ImageViewHolder;
import com.m7.imkfsdk.utils.RegexUtils;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.OnSubmitOfflineMessageListener;

/**
 * Created by longwei on 16/8/15.
 */
public class OfflineMessageActicity extends Activity{
    EditText id_et_content, id_et_phone, id_et_email;
    Button btn_submit;
    ImageView back;
    private String peerId;
    LoadingFragmentDialog loadingFragmentDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kf_dialog_offline);

        loadingFragmentDialog = new LoadingFragmentDialog();

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        id_et_content = (EditText) findViewById(R.id.id_et_content);
        id_et_phone = (EditText) findViewById(R.id.id_et_phone);
        id_et_email = (EditText) findViewById(R.id.id_et_email);

        btn_submit = (Button) findViewById(R.id.id_btn_submit);

        Intent intent = getIntent();
        peerId = intent.getStringExtra("PeerId");

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = id_et_content.getText().toString().trim();
                String phone = id_et_phone.getText().toString().trim();
                String email = id_et_email.getText().toString().trim();

                if("".equals(phone)) {
                    Toast.makeText(OfflineMessageActicity.this, "请输入电话号", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    if(RegexUtils.checkMobile(phone) || RegexUtils.checkPhone(phone)) {

                    }else {
                        Toast.makeText(OfflineMessageActicity.this, "请输入正确的电话号", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if("".equals(email)) {
                    Toast.makeText(OfflineMessageActicity.this, "请输入邮箱", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    if(RegexUtils.checkEmail(email)) {

                    }else {
                        Toast.makeText(OfflineMessageActicity.this, "请输入正确的邮箱", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if(!"".equals(content)) {
                    if(!"".equals(phone) || !"".equals(email)) {
                        loadingFragmentDialog.show(getFragmentManager(), "");
                        IMChatManager.getInstance().submitOfflineMessage(peerId, content, phone, email, new OnSubmitOfflineMessageListener() {
                            @Override
                            public void onSuccess() {
                                loadingFragmentDialog.dismiss();
                                Toast.makeText(OfflineMessageActicity.this, "提交留言成功", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onFailed() {
                                loadingFragmentDialog.dismiss();
                                Toast.makeText(OfflineMessageActicity.this, "提交留言失败", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                }else {
                    Toast.makeText(OfflineMessageActicity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getStringExtra("PeerId") != null) {
            peerId = intent.getStringExtra("PeerId");
        }
    }
}
