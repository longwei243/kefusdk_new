package com.moor.imkf;

import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.moor.imkf.db.dao.InfoDao;
import com.moor.imkf.db.dao.MessageDao;
import com.moor.imkf.http.FileDownLoadListener;
import com.moor.imkf.http.HttpManager;
import com.moor.imkf.http.HttpResponseListener;
import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.model.parser.HttpParser;
import com.moor.imkf.qiniu.http.ResponseInfo;
import com.moor.imkf.qiniu.storage.UpCompletionHandler;
import com.moor.imkf.qiniu.storage.UpProgressHandler;
import com.moor.imkf.qiniu.storage.UploadManager;
import com.moor.imkf.qiniu.storage.UploadOptions;
import com.moor.imkf.requesturl.RequestUrl;
import com.moor.imkf.utils.LogUtil;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * 提供发送消息的方法
 *
 */
public class IMChat {

    String connectionId = "";

    private static IMChat instance = new IMChat();

    private String sessionId = "";

    private IMChat() {

    }

    public static IMChat getInstance() {
        return instance;
    }

    /**
     * 发送消息
     */
    public void sendMessage(FromToMessage fromToMessage, ChatListener chatListener) {

        connectionId = InfoDao.getInstance().getConnectionId();
        LogUtil.d("IMChat", "connectionId的值是:"+connectionId);
        if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_TEXT)) {
            //文本消息
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().insertSendMsgsToDao(fromToMessage);
            //发送到网络中
            HttpManager.newMsgToServer(connectionId,
                    fromToMessage, new NewMessageResponseHandler(fromToMessage, chatListener));
        }else if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_AUDIO)) {
            //录音
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().insertSendMsgsToDao(fromToMessage);

            //获取7牛token
            HttpManager.getQiNiuToken(connectionId,
                    fromToMessage.filePath, new UploadFileResponseHandler("ly", fromToMessage, chatListener));
        }else if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_IMAGE)) {
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().insertSendMsgsToDao(fromToMessage);

            //获取7牛token
            HttpManager.getQiNiuToken(connectionId,
                    fromToMessage.filePath, new UploadFileResponseHandler("img", fromToMessage, chatListener));

        }else if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_FILE)) {
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().insertSendMsgsToDao(fromToMessage);

            //获取7牛token
            HttpManager.getQiNiuToken(connectionId,
                    fromToMessage.filePath, new UploadFileResponseHandler("file", fromToMessage, chatListener));

        }


    }


    /**
     * 上传文件回调
     * @author LongWei
     *
     */
    class UploadFileResponseHandler implements HttpResponseListener {
        String fileType = "";
        FromToMessage fromToMessage;
        ChatListener chatListener;

        public UploadFileResponseHandler(String fileType,
                                         FromToMessage fromToMessage,
                                         ChatListener chatListener) {
            this.fileType =  fileType;
            this.fromToMessage = fromToMessage;
            this.chatListener = chatListener;
        }

        @Override
        public void onFailed() {
//			Toast.makeText(ChatActivity.this, "上传7牛失败了", Toast.LENGTH_SHORT).show();;
            MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
            if (chatListener != null) {
                chatListener.onFailed();
            }
        }

        @Override
        public void onSuccess(String responseString) {
            // TODO Auto-generated method stub
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            if ("true".equals(succeed)) {
                String upToken = HttpParser.getUpToken(responseString);
                // qiniu SDK自带方法上传
                UploadManager uploadManager = new UploadManager();
                if ("img".equals(fileType)) {// 图片
//					final String imgFileKey = UUID.randomUUID().toString();
                    String fileName = UUID.randomUUID().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                    String date = sdf.format(new Date());
                    final String imgFileKey = "kefu/image/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;

                    uploadManager.put(fromToMessage.filePath, imgFileKey, upToken,
                            new UpCompletionHandler() {



                                @Override
                                public void complete(String key,
                                                     ResponseInfo info, JSONObject response) {
                                    // TODO Auto-generated method stub
                                    System.out.println(key + "     " + info
                                            + "      " + response);

                                    fromToMessage.message = RequestUrl.QiniuHttp + imgFileKey;
//									System.out.println("图片在服务器上的位置是："+fromToMessage.message);
                                    MessageDao.getInstance().updateMsgToDao(fromToMessage);
//									//发送新消息给服务器
									HttpManager.newMsgToServer(InfoDao.getInstance().getConnectionId(),
											fromToMessage, new NewMessageResponseHandler(fromToMessage, chatListener));
                                }
                            },new UploadOptions(null, null, false,
                                    new UpProgressHandler(){
                                        public void progress(String key, final double percent){
                                            Log.i("qiniu", key + ": " + (int) (percent * 100));
                                            int progress = (int) (percent * 100);
                                            chatListener.onProgress();
                                        }
                                    }, null));

                } else if ("ly".equals(fileType)) {// 音频文件
                    String fileName = UUID.randomUUID().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                    String date = sdf.format(new Date());
                    final String fileKey = "kefu/sound/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;


                    uploadManager.put(fromToMessage.filePath, fileKey, upToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key,
                                                     ResponseInfo info, JSONObject response) {
                                    // TODO Auto-generated method stub
//									System.out.println("上传录音成功了");
                                    System.out.println(key + "     " + info
                                            + "      " + response);
                                    //设置获得的url
                                    fromToMessage.message = RequestUrl.QiniuHttp + fileKey;
//									System.out.println("录音在服务器上的位置是："+fromToMessage.message);
                                    MessageDao.getInstance().updateMsgToDao(fromToMessage);
                                    //发送新消息给服务器
									HttpManager.newMsgToServer(InfoDao.getInstance().getConnectionId(),
											fromToMessage, new NewMessageResponseHandler(fromToMessage, chatListener));
                                }
                            }, new UploadOptions(null, null, false,
                                    new UpProgressHandler(){
                                        public void progress(String key, final double percent){
                                            Log.i("qiniu", key + ": " + (int) (percent * 100));
                                            int progress = (int) (percent * 100);
                                            chatListener.onProgress();
                                        }
                                    }, null));
                } else if ("file".equals(fileType)) {// 音频文件
                    String fileName = fromToMessage.fileName;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                    String date = sdf.format(new Date());
                    final String fileKey = "kefu/file/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;


                    uploadManager.put(fromToMessage.filePath, fileKey, upToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key,
                                                     ResponseInfo info, JSONObject response) {
                                    //设置获得的url
                                    fromToMessage.message = RequestUrl.QiniuHttp + fileKey+"?fileName="+fromToMessage.fileName+"?fileSize="+fromToMessage.fileSize;
                                    MessageDao.getInstance().updateMsgToDao(fromToMessage);
                                    //发送新消息给服务器
                                    HttpManager.newMsgToServer(InfoDao.getInstance().getConnectionId(),
                                            fromToMessage, new NewMessageResponseHandler(fromToMessage, chatListener));
                                }
                            }, new UploadOptions(null, null, false,
                                    new UpProgressHandler(){
                                        public void progress(String key, final double percent){
                                            Log.i("qiniu", key + ": " + (int) (percent * 100));
                                            int progress = (int) (percent * 100);
                                            if(progress % 2 == 0) {
                                                fromToMessage.fileProgress = progress;
                                                MessageDao.getInstance().updateMsgToDao(fromToMessage);
                                                chatListener.onProgress();
                                            }

                                        }
                                    }, null));
                }
            } else {
//				Toast.makeText(ChatActivity.this, message, 3000).show();
            }

        }
    }

    class NewMessageResponseHandler implements HttpResponseListener {
        FromToMessage fromToMessage;
        ChatListener chatListener;
        public NewMessageResponseHandler(FromToMessage fromToMessage, ChatListener chatListener) {
            this.fromToMessage = fromToMessage;
            this.chatListener = chatListener;
        }

        @Override
        public void onFailed() {
            MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
            if(chatListener != null) {
                chatListener.onFailed();
            }

            LogUtil.d("NewMessageResponseHandler", "消息发送失败onFailure");
        }

        @Override
        public void onSuccess(String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            LogUtil.d("NewMessageResponseHandler", "服务器返回的数据是:"+responseString);
            if ("true".equals(succeed)) {
                long when = HttpParser.getWhen(responseString);
                MessageDao.getInstance().updateSucceedMsgToDao(fromToMessage, when);
                if(chatListener != null) {
                    chatListener.onSuccess();
                }

            } else {
                MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
                LogUtil.d("NewMessageResponseHandler", "消息发送失败onSuccess");
                if(chatListener != null) {
                    chatListener.onFailed();
                }

                if("404".equals(message)) {
                    //connection断了，启动重连

                }
            }
        }
    }

    /**
     * 获取sessionId
     * @return
     */
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取客服的Id,供消息拼接使用
     * @return
     */
    public String get_id() {
        String imServiceNo = InfoDao.getInstance().getImServiceNo();
        return imServiceNo;
    }

    private String getDeviceId() {
        TelephonyManager TelephonyMgr = (TelephonyManager) IMChatManager.getInstance().getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        String szImei = TelephonyMgr.getDeviceId();
        return szImei;
    }


    /**
     * 重新发送消息
     */
    public void reSendMessage(FromToMessage fromToMessage, ChatListener chatListener) {

        connectionId = InfoDao.getInstance().getConnectionId();
        if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_TEXT)) {
            //文本消息
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().updateMsgToDao(fromToMessage);
            //发送到网络中
            HttpManager.newMsgToServer(connectionId,
                    fromToMessage, new NewMessageResponseHandler(fromToMessage, chatListener));
        }else if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_AUDIO)) {
            //录音
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().updateMsgToDao(fromToMessage);

            //获取7牛token
            HttpManager.getQiNiuToken(InfoDao.getInstance().getConnectionId(),
                    fromToMessage.filePath, new UploadFileResponseHandler("ly", fromToMessage, chatListener));
        }else if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_IMAGE)) {
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().updateMsgToDao(fromToMessage);

            //获取7牛token
            HttpManager.getQiNiuToken(InfoDao.getInstance().getConnectionId(),
                    fromToMessage.filePath, new UploadFileResponseHandler("img", fromToMessage, chatListener));

        }else if(fromToMessage.msgType.equals(FromToMessage.MSG_TYPE_FILE)) {
            fromToMessage.sendState = "sending";
            MessageDao.getInstance().updateMsgToDao(fromToMessage);

            //获取7牛token
            HttpManager.getQiNiuToken(InfoDao.getInstance().getConnectionId(),
                    fromToMessage.filePath, new UploadFileResponseHandler("file", fromToMessage, chatListener));

        }
    }


    public void downLoadFile(final FromToMessage message, final FileMessageDownLoadListener listener) {
        if(message != null) {
            if (message.filePath == null || "".equals(message.filePath)) {
                final String dirStr = Environment.getExternalStorageDirectory() +  File.separator + "m7" + File.separator + "fileDownload";
                File dir = new File(dirStr);
                if (!dir.exists()) {
                    dir.mkdirs();

                }
                File file = new File(dir, message.fileName);
                if (file.exists()) {
                    file.delete();
                }
                HttpManager.downloadFile(message.message, file, new FileDownLoadListener() {
                    @Override
                    public void onSuccess(File file) {
                        message.filePath = file.getAbsolutePath();
                        message.fileDownLoadStatus = "success";
                        message.fileProgress = 100;
                        MessageDao.getInstance().updateMsgToDao(message);

                        if(listener != null) {
                            listener.onSuccess(file);
                        }
                    }

                    @Override
                    public void onFailed() {

                        message.fileProgress = 0;
                        message.fileDownLoadStatus = "failed";
                        MessageDao.getInstance().updateMsgToDao(message);

                        if(listener != null) {
                            listener.onFailed();
                        }
                    }

                    @Override
                    public void onProgress(int progress) {
                        message.fileProgress = progress;
                        message.fileDownLoadStatus = "downloading";
                        MessageDao.getInstance().updateMsgToDao(message);

                        if(listener != null) {
                            listener.onProgress();
                        }
                    }
                });
            }
        }
    }

}
