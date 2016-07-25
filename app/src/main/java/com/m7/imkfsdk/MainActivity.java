package com.m7.imkfsdk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.m7.imkfsdk.chat.ChatActivity;
import com.m7.imkfsdk.chat.LoadingFragmentDialog;
import com.m7.imkfsdk.chat.PeerDialog;
import com.moor.imkf.GetPeersListener;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.InitListener;
import com.moor.imkf.model.entity.Peer;

import java.io.Serializable;
import java.util.List;


public class MainActivity extends Activity {

    private SharedPreferences sp;
    private LoadingFragmentDialog loadingDialog;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x444:
                    loadingDialog.dismiss();
                    getPeers();

                    break;
                case 0x555:
                    loadingDialog.dismiss();
                    Toast.makeText(MainActivity.this, "客服初始化失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.kf_activity_main);
        sp = getSharedPreferences("setting", 0);
        loadingDialog = new LoadingFragmentDialog();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断版本若为6.0申请权限
                if(Build.VERSION.SDK_INT < 23) {
                    init();
                }else {
                    //6.0
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        //该权限已经有了
                        init();
                    }else {
                        //申请该权限
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0x1111);
                    }
                }
            }
        });
        findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MobileApplication.isKFSDK) {
                    IMChatManager.getInstance().quit();
                    MobileApplication.isKFSDK = false;
                }
            }
        });

        findViewById(R.id.setting_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(settingIntent);
            }
        });
    }

    private void init() {
        //填写过参数才能登录
//                if(!"".equals(sp.getString("accessId", ""))) {
            loadingDialog.show(getFragmentManager(), "");
            if (MobileApplication.isKFSDK) {
                loadingDialog.dismiss();
                getPeers();
            } else {
                startKFService();
            }
//                }else {
//                    Toast.makeText(MainActivity.this, "请先设置参数", Toast.LENGTH_SHORT).show();
//                }
    }

    private void getPeers() {
        IMChatManager.getInstance().getPeers(new GetPeersListener() {
            @Override
            public void onSuccess(List<Peer> peers) {
                if (peers.size() > 1) {
                    PeerDialog dialog = new PeerDialog();
                    Bundle b = new Bundle();
                    b.putSerializable("Peers", (Serializable) peers);
                    b.putString("type", "init");
                    dialog.setArguments(b);
                    dialog.show(getFragmentManager(), "");

                } else if (peers.size() == 1) {
                    startChatActivity(peers.get(0).getId());
                } else {
                    startChatActivity("");
                }
            }

            @Override
            public void onFailed() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    private void startKFService() {

        new Thread() {
            @Override
            public void run() {
                IMChatManager.getInstance().setOnInitListener(new InitListener() {
                    @Override
                    public void oninitSuccess() {
                        MobileApplication.isKFSDK = true;
                        loadingDialog.dismiss();
                        getPeers();
                        Log.d("MobileApplication", "sdk初始化成功");

                    }

                    @Override
                    public void onInitFailed() {
                        MobileApplication.isKFSDK = false;
                        loadingDialog.dismiss();
                        Toast.makeText(MainActivity.this, "客服初始化失败", Toast.LENGTH_SHORT).show();
                        Log.d("MobileApplication", "sdk初始化失败");
                    }
                });

                //初始化IMSdk,填入相关参数
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "0e7fb510-3931-11e6-8df8-8f2e14b5dba3", "7moor测试", "7777");
                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "2ff6ebc0-e40c-11e5-82a5-51d279813f91", "AT", "8888");
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "206ef9e0-41c7-11e6-abe7-c9cb83533f27", "雅希测试", "8888");
            }
        }.start();

    }

    private void startChatActivity(String peerId) {
        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
        chatIntent.putExtra("PeerId", peerId);
        startActivity(chatIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0x1111:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {

                }
                break;
        }
    }
}
