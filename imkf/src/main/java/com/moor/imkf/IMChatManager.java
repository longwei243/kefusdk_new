package com.moor.imkf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.moor.imkf.db.dao.InfoDao;
import com.moor.imkf.db.dao.InvestigateDao;
import com.moor.imkf.db.dao.MessageDao;
import com.moor.imkf.event.KFLoginEvent;
import com.moor.imkf.eventbus.EventBus;
import com.moor.imkf.http.HttpManager;
import com.moor.imkf.http.HttpResponseListener;
import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.model.entity.Info;
import com.moor.imkf.model.entity.Investigate;
import com.moor.imkf.model.entity.Peer;
import com.moor.imkf.model.parser.HttpParser;
import com.moor.imkf.requesturl.RequestUrl;
import com.moor.imkf.tcpservice.service.IMService;
import com.moor.imkf.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * SDK管理类
 */
public class IMChatManager {

    /**
     * 接收新消息的action
     */
    public static String NEW_MSG_ACTION = "";
    /**
     * 接收是否是机器人的action
     */
    public static final String ROBOT_ACTION = "action_robot";
    /**
     * 接收客服在线的action
     */
    public static final String ONLINE_ACTION = "action_online";
    /**
     * 接收客服领取会话的action
     */
    public static final String CLIAM_ACTION = "action_cliam";
    /**
     * 接收客服不在线的action
     */
    public static final String OFFLINE_ACTION = "action_offline";
    /**
     * 客服发起的评价
     */
    public static final String INVESTIGATE_ACTION = "action_investigate";
    /**
     * 技能组排队数
     */
    public static final String QUEUENUM_ACTION = "action_queuenum";
    /**
     * 会话结束
     */
    public static final String FINISH_ACTION = "action_finish";

    private Context appContext;

    private static IMChatManager instance = new IMChatManager();

    private InitListener initListener;

    private IMChatManager() {
        EventBus.getDefault().register(this);
    }

    public static IMChatManager getInstance() {
        return instance;
    }

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Handler mDelivery;

    /**
     * 获取应用全局context
     * @return
     */
    public Context getAppContext() {
        return appContext;
    }


    /**
     * 初始化sdk方法，必须先调用该方法进行初始化后才能使用IM相关功能
     * @param appContext 上下文
     * @param receiverAction 接收新消息广播的action,必须和AndroidManifest中注册的广播中的action一致
     * @param accessId 接入id
     * @param userName 用户名
     * @param userId 用户id
     */
    public void init(Context appContext, String receiverAction, String accessId, String userName,
                     String userId) {
        this.appContext = appContext.getApplicationContext();
        mDelivery = new Handler(Looper.getMainLooper());
        sp = appContext.getSharedPreferences("isnewvisitor", 0);
        editor = sp.edit();

        if(receiverAction != null && !"".equals(receiverAction)) {
            IMChatManager.NEW_MSG_ACTION = receiverAction;
        }

        Info info = new Info();
        if(userName != null) {
            info.loginName = userName;
        }
        if(userId != null) {
            info.userId = userId;
        }

        if(accessId != null) {
            info.accessId = accessId;
        }

        InfoDao.getInstance().insertInfoToDao(info);

        Intent imserviceIntent = new Intent(appContext, IMService.class);
        appContext.startService(imserviceIntent);
        editor.putBoolean("firstInit", true);
        editor.commit();
    }

    /**
     * 注销,停止IMService
     */
    public void quit() {
        if(appContext != null) {
            EventBus.getDefault().post(KFLoginEvent.LOGIN_OFF);
        }
    }

    public void onEventMainThread(KFLoginEvent KFLoginEvent){
        switch (KFLoginEvent){
            case LOGIN_SUCCESS:
                if(sp.getBoolean("firstInit", true)) {
                    editor.putBoolean("firstInit", false);
                    editor.commit();
                    HttpManager.getInvestigateList(InfoDao.getInstance().getConnectionId(), new GetInvestigateResponseHandler());
                    if(initListener != null) {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                initListener.oninitSuccess();
                            }
                        });

                    }
                }
                break;
            case LOGIN_FAILED:
                if(sp.getBoolean("firstInit", true)) {
                    editor.putBoolean("firstInit", false);
                    editor.commit();
                    if(initListener != null) {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                initListener.onInitFailed();
                            }
                        });

                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置初始化回调接口
     * @param initListener
     */
    public void setOnInitListener(InitListener initListener) {
        this.initListener = initListener;
    }


    /**
     * 获取评价列表
     * @return
     */
    public List<Investigate> getInvestigate() {
        List<Investigate> list = new ArrayList<Investigate>();
        list = InvestigateDao.getInstance().getInvestigatesFromDao();
        if(list.size() == 0) {
            //去网络上加载
            HttpManager.getInvestigateList(InfoDao.getInstance().getConnectionId(), new GetInvestigateResponseHandler());
        }

        return list;
    }

    private class GetInvestigateResponseHandler implements HttpResponseListener {

        @Override
        public void onFailed() {

        }

        @Override
        public void onSuccess(String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            if ("true".equals(succeed)) {
                List<Investigate> list = HttpParser.getInvestigates(responseString);
                InvestigateDao.getInstance().deleteAll();
                InvestigateDao.getInstance().insertInvestigateToDao(list);
            } else {

            }
        }
    }


    /**
     * 提交评价
     * @param investigate
     */
    public void submitInvestigate(Investigate investigate, SubmitInvestigateListener listener) {

        HttpManager.submitInvestigate(InfoDao.getInstance().getConnectionId(), investigate.name, investigate.value, new SubmitResponse(listener));
    }

    private class SubmitResponse implements HttpResponseListener {

        private SubmitInvestigateListener listener;

        public SubmitResponse(SubmitInvestigateListener listener) {
            this.listener = listener;
        }

        @Override
        public void onFailed() {
            if(listener != null) {
                listener.onFailed();
            }

        }

        @Override
        public void onSuccess(String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            if ("true".equals(succeed)) {
                if(listener != null) {
                    listener.onSuccess();
                }

            } else {
                if(listener != null) {
                    listener.onFailed();
                }

            }
        }
    }

    private boolean getIsNewVisitor() {
        boolean isnew = sp.getBoolean("isnew", true);
        if(isnew) {
            editor.putBoolean("isnew", false);
            editor.commit();
        }
        return isnew;
    }

    /**
     * 开始会话
     */
    public void beginSession(String peerId, OnSessionBeginListener listener) {
        HttpManager.beginNewChatSession(InfoDao.getInstance().getConnectionId(), getIsNewVisitor(), peerId, new BeginSessionResponse(listener));
    }

    private class BeginSessionResponse implements HttpResponseListener {

        OnSessionBeginListener listener;
        public BeginSessionResponse(OnSessionBeginListener listener) {
            this.listener = listener;
        }

        @Override
        public void onFailed() {
            if(listener != null) {
                listener.onFailed();
            }
        }

        @Override
        public void onSuccess(String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            LogUtil.d("IMChatManger", "biginSession:"+responseString);
            if ("true".equals(succeed)) {

                if(listener != null) {
                    listener.onSuccess();
                }

            } else {
                if (listener != null) {
                    listener.onFailed();
                }
            }
        }
    }

    /**
     * 提交离线留言
     * @param content 留言内容
     * @param phone 电话
     * @param email 邮箱
     * @param listener
     */
    public void submitOfflineMessage(String peerId, String content, String phone, String email, OnSubmitOfflineMessageListener listener) {
       if(content == null) {
           content = "";
       }
        if(phone == null) {
            phone = "";
        }
        if(email == null) {
            email = "";
        }
        HttpManager.submitOfflineMessage(InfoDao.getInstance().getConnectionId(), peerId, content, phone, email, new SubmitOfflineMsgResponse(listener));
    }

    private class SubmitOfflineMsgResponse implements HttpResponseListener {

        OnSubmitOfflineMessageListener listener;
        public SubmitOfflineMsgResponse(OnSubmitOfflineMessageListener listener) {
            this.listener = listener;
        }

        @Override
        public void onFailed() {
            if(listener != null) {
                listener.onFailed();
            }
        }

        @Override
        public void onSuccess(String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            if ("true".equals(succeed)) {
                if(listener != null) {
                    listener.onSuccess();
                }
            } else {
                if (listener != null) {
                    listener.onFailed();
                }
            }
        }
    }


    /**
     * 从本地数据库中获取消息数据
     * @param i 第几页的数据，默认一页15条，从第一页开始取（i=1）
     * @return 消息数据列表
     */
    public List<FromToMessage> getMessages(int i) {
        List<FromToMessage> messagesList = new ArrayList<FromToMessage>();
        messagesList = MessageDao.getInstance().getMessages(i);
        return messagesList;
    }

    /**
     * 更新一条消息数据到本地数据库中
     * @param message
     */
    public void updateMessageToDB(FromToMessage message) {
        MessageDao.getInstance().updateMsgToDao(message);
    }


    /**
     * 通过传递进来的消息数量判断数据库中消息是否全被取出
     * @param size 消息数量
     * @return true说明消息全部被取出,false说明还有消息未取出
     */
    public boolean isReachEndMessage(int size) {
        return MessageDao.getInstance().isReachEndMessage(size);
    }

    /**
     * 转人工服务
     */
    public void convertManual(OnConvertManualListener listener) {
        HttpManager.convertManual(InfoDao.getInstance().getConnectionId(), new ConvertManualResponse(listener));
    }

    private class ConvertManualResponse implements HttpResponseListener {

        OnConvertManualListener listener;
        public ConvertManualResponse(OnConvertManualListener listener) {
            this.listener = listener;
        }

        @Override
        public void onFailed() {
            if(listener != null) {
                listener.offLine();
            }
        }

        @Override
        public void onSuccess(String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            LogUtil.d("IMChatManger", "ConvertManualResponse:"+responseString);
            if ("true".equals(succeed)) {
                if(listener != null) {
                    listener.onLine();
                }
            } else {
                if(listener != null) {
                    listener.offLine();
                }
            }
        }
    }


    /**
     * 获取技能组
     */
    public void getPeers(GetPeersListener listener) {
        System.out.println("发出了技能组请求");
        HttpManager.getPeers(InfoDao.getInstance().getConnectionId(), new GetPeersResponse(listener));
    }

    private class GetPeersResponse implements HttpResponseListener {

        private GetPeersListener listener;

        public GetPeersResponse(GetPeersListener listener) {
            this.listener = listener;
        }

        @Override
        public void onFailed() {
            if(listener != null) {
                listener.onFailed();
            }

        }

        @Override
        public void onSuccess(String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            System.out.println("获取技能组返回数据:"+responseString);
            if ("true".equals(succeed)) {
                if(listener != null) {
                    List<Peer> peers = HttpParser.getPeers(responseString);
                    listener.onSuccess(peers);
                }

            } else {
                if(listener != null) {
                    listener.onFailed();
                }

            }
        }
    }

    /**
     * 删除评价消息
     * @param msg
     */
    public void deleteInvestigateMsg (FromToMessage msg) {
        MessageDao.getInstance().deleteMsg(msg);
    }

    /**
     * 设置tcp的IP和端口
     * @param tcpIp
     * @param port
     */
    public void setTcpIpAndPort(String tcpIp, int port) {
        if(tcpIp != null && !"".equals(tcpIp) && port > 0) {
            RequestUrl.baseTcpHost = tcpIp;
            RequestUrl.baseTcpPort = port;
        }
    }

    /**
     * 设置http的IP
     */
    public void setHttpIp(String httpIp) {
        if(httpIp != null && !"".equals(httpIp)) {
            RequestUrl.baseHttp1 = httpIp;
        }
    }

    /**
     * 设置七牛的IP
     */
    public void setQiNiuIp(String qiNiuIp) {
        if(qiNiuIp != null && !"".equals(qiNiuIp)) {
            RequestUrl.QiniuHttp = qiNiuIp;
        }
    }

}
