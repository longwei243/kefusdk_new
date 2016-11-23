package com.m7.imkfsdk.chat.chatrow;

import com.moor.imkf.model.entity.FromToMessage;

/**
 * Created by longwei on 2016/3/9.
 */
public class ChatRowUtils {

    /**
     * 获取聊天消息类型
     */
    public static int getChattingMessageType(FromToMessage msg) {
        if(FromToMessage.MSG_TYPE_TEXT.equals(msg.msgType)) {
            return 200;
        }else if(FromToMessage.MSG_TYPE_IMAGE.equals(msg.msgType)) {
            return 300;
        }else if(FromToMessage.MSG_TYPE_AUDIO.equals(msg.msgType)) {
            return 400;
        }else if(FromToMessage.MSG_TYPE_INVESTIGATE.equals(msg.msgType)) {
            return 500;
        }else if(FromToMessage.MSG_TYPE_FILE.equals(msg.msgType)) {
            return 600;
        }else if(FromToMessage.MSG_TYPE_IFRAME.equals(msg.msgType)) {
            return 700;
        }
        return 0;
    }

    /**
     *获取消息条目类型
     */
    public static Integer getMessageRowType(FromToMessage iMessage) {
        if(FromToMessage.MSG_TYPE_TEXT.equals(iMessage.msgType)) {
            if("1".equals(iMessage.userType)) {
                return ChatRowType.TEXT_ROW_RECEIVED.getId();
            }
            return ChatRowType.TEXT_ROW_TRANSMIT.getId();
        }else if(FromToMessage.MSG_TYPE_IMAGE.equals(iMessage.msgType)) {
            if("1".equals(iMessage.userType)) {
                return ChatRowType.IMAGE_ROW_RECEIVED.getId();
            }
            return ChatRowType.IMAGE_ROW_TRANSMIT.getId();
        }else if(FromToMessage.MSG_TYPE_AUDIO.equals(iMessage.msgType)) {
            if("1".equals(iMessage.userType)) {
                return ChatRowType.VOICE_ROW_RECEIVED.getId();
            }
            return ChatRowType.VOICE_ROW_TRANSMIT.getId();
        }else if(FromToMessage.MSG_TYPE_INVESTIGATE.equals(iMessage.msgType)) {
            if("1".equals(iMessage.userType)) {

            }
            return ChatRowType.INVESTIGATE_ROW_TRANSMIT.getId();
        }else if(FromToMessage.MSG_TYPE_FILE.equals(iMessage.msgType)) {
            if("1".equals(iMessage.userType)) {
                return ChatRowType.FILE_ROW_RECEIVED.getId();
            }
            return ChatRowType.FILE_ROW_TRANSMIT.getId();
        }else if(FromToMessage.MSG_TYPE_IFRAME.equals(iMessage.msgType)) {
            if("1".equals(iMessage.userType)) {
                return ChatRowType.IFRAME_ROW_RECEIVED.getId();
            }
        }
        return -1;
    }
}
