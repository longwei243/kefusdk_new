package com.m7.imkfsdk.chat;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.m7.imkfsdk.R;
import com.m7.imkfsdk.utils.RegexUtils;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.OnSubmitOfflineMessageListener;

/**
 * 离线留言对话框
 * Created by longwei
 */
public class OfflineMessageDialog extends DialogFragment {

    EditText id_et_content, id_et_phone, id_et_email;
    Button btn_cancel, btn_submit;
    private String peerId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the layout inflater
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        View view = inflater.inflate(R.layout.kf_dialog_offline, null);
        id_et_content = (EditText) view.findViewById(R.id.id_et_content);
        id_et_phone = (EditText) view.findViewById(R.id.id_et_phone);
        id_et_email = (EditText) view.findViewById(R.id.id_et_email);

        btn_submit = (Button) view.findViewById(R.id.id_btn_submit);
        btn_cancel = (Button) view.findViewById(R.id.id_btn_cancel);

        Bundle bundle = getArguments();
        peerId = bundle.getString("PeerId");

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = id_et_content.getText().toString().trim();
                String phone = id_et_phone.getText().toString().trim();
                String email = id_et_email.getText().toString().trim();

                if("".equals(phone)) {
                    Toast.makeText(getActivity(), "请输入电话号", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    if(RegexUtils.checkMobile(phone) || RegexUtils.checkPhone(phone)) {

                    }else {
                        Toast.makeText(getActivity(), "请输入正确的电话号", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if("".equals(email)) {
                    Toast.makeText(getActivity(), "请输入邮箱", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    if(RegexUtils.checkEmail(email)) {

                    }else {
                        Toast.makeText(getActivity(), "请输入正确的邮箱", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if(!"".equals(content)) {
                    if(!"".equals(phone) || !"".equals(email)) {
                        IMChatManager.getInstance().submitOfflineMessage(peerId, content, phone, email, new OnSubmitOfflineMessageListener() {
                            @Override
                            public void onSuccess() {
                                dismiss();
                                Toast.makeText(getActivity(), "提交留言成功", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailed() {
                                dismiss();
                                Toast.makeText(getActivity(), "提交留言失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else {
                    Toast.makeText(getActivity(), "请输入内容", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void show(android.app.FragmentManager manager, String tag) {
        if(!this.isAdded()) {
            try {
                super.show(manager, tag);
            }catch (Exception e) {}
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        }catch (Exception e) {}

    }
}
