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
import com.moor.imkf.utils.NetUtils;

import java.io.Serializable;
import java.util.List;


public class MainActivity extends Activity {

    private LoadingFragmentDialog loadingDialog;

    private SharedPreferences sp;

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

        if(!NetUtils.hasDataConnection(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "当前没有网络连接", Toast.LENGTH_SHORT).show();
            return;
        }

            loadingDialog.show(getFragmentManager(), "");
            if (MobileApplication.isKFSDK) {
                loadingDialog.dismiss();
                getPeers();
            } else {
                startKFService();
            }
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

//        final String accessId = sp.getString("accessId", "1cf5bdb0-c66d-11e5-9875-63635d52845f");
//        final String name = sp.getString("name", "正式环境测试号0801");
//        final String userId = sp.getString("userId", "8888");
//        String tcp_ip = sp.getString("tcpIp", "115.29.190.253");
//        String http_ip = sp.getString("httpIp", "http://115.29.190.253:4999/sdkChat");
//
//        System.out.println("accessId is:"+accessId+",name is:"+name+",tcpip is:"+tcp_ip);
//
//        IMChatManager.getInstance().setTcpIpAndPort(tcp_ip, 8006);
//        IMChatManager.getInstance().setHttpIp(http_ip);


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
                        Log.d("MobileApplication", "sdk初始化失败, 请填写正确的accessid");
                    }
                });

                //初始化IMSdk,填入相关参数
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", accessId, name, userId);
                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "2ff6ebc0-e40c-11e5-82a5-51d279813f91", "ATest", "8888");
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "1cf5bdb0-c66d-11e5-9875-63635d52845f", "TestTest", "777");
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "4db84e50-2b93-11e6-8e31-2589dcc314d2", "陈辰测试", "777");
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "40600870-b82e-11e5-bb61-858869d3e632", "帅测试", "777");
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "ba995ec0-a57c-11e6-80ad-ed126d59818a", "海航测试", "777");
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.m7.imkf.KEFU_NEW_MSG", "ad974b30-9b3e-11e6-a597-7dd254955b0d", "测试", "888");
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
