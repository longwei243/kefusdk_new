package com.m7.imkfsdk.chat.chatrow;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;

import com.m7.imkfsdk.R;
import com.m7.imkfsdk.chat.holder.BaseHolder;
import com.m7.imkfsdk.chat.holder.VoiceViewHolder;
import com.moor.imkf.model.entity.FromToMessage;

/**
 * Created by longwei on 2016/3/9.
 */
public class VoiceRxChatRow extends BaseChatRow {

    public VoiceRxChatRow(int type) {
        super(type);
    }

    @Override
    public boolean onCreateRowContextMenu(ContextMenu contextMenu, View targetView, FromToMessage detail) {
        return false;
    }

    @Override
    protected void buildChattingData(Context context, BaseHolder baseHolder, FromToMessage detail, int position) {
        VoiceViewHolder holder = (VoiceViewHolder) baseHolder;
        FromToMessage message = detail;
        if(message != null) {
        }
    }

    @Override
    public View buildChatView(LayoutInflater inflater, View convertView) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.kf_chat_row_text_rx, null);
            VoiceViewHolder holder = new VoiceViewHolder(mRowType);
            convertView.setTag(holder.initBaseHolder(convertView, true));
        }
        return convertView;
    }

    @Override
    public int getChatViewType() {
        return ChatRowType.TEXT_ROW_RECEIVED.ordinal();
    }
}
