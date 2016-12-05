package com.m7.imkfsdk.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.m7.imkfsdk.MobileApplication;
import com.m7.imkfsdk.R;
import com.m7.imkfsdk.chat.adapter.ChatAdapter;
import com.m7.imkfsdk.recordbutton.AudioRecorderButton;
import com.m7.imkfsdk.utils.FaceConversionUtil;
import com.m7.imkfsdk.utils.FileUtils;
import com.m7.imkfsdk.view.ChatListView;
import com.moor.imkf.ChatListener;
import com.moor.imkf.GetPeersListener;
import com.moor.imkf.IMChat;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.IMMessage;
import com.moor.imkf.OnConvertManualListener;
import com.moor.imkf.OnSessionBeginListener;
import com.moor.imkf.db.dao.InfoDao;
import com.moor.imkf.model.entity.ChatEmoji;
import com.moor.imkf.model.entity.ChatMore;
import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.model.entity.Investigate;
import com.moor.imkf.model.entity.Peer;
import com.m7.imkfsdk.utils.JsonParser;
import com.moor.imkf.utils.LogUtil;
import com.moor.imkf.utils.NullUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 聊天界面
 * @author LongWei
 */
public class ChatActivity extends MyBaseActivity implements OnClickListener,
		OnItemClickListener, ChatListView.OnRefreshListener, AudioRecorderButton.RecorderFinishListener{
	private ChatListView mChatList;
	private Button mChatSend, mChatMore, mChatSetModeVoice,
			mChatSetModeKeyboard, chat_btn_convert;
	ImageView chat_btn_back;
	private EditText mChatInput;
	private ChatAdapter chatAdapter;
	private RelativeLayout mChatEdittextLayout,
			mChatMoreContainer;
	private LinearLayout mMore;
	private AudioRecorderButton mRecorderButton;
//	private SpeechRecorderButton mRecorderButton;
	private RelativeLayout mChatFaceContainer;
	private ImageView mChatEmojiNormal, mChatEmojiChecked;
	private InputMethodManager manager;
	private TextView mOtherName;
	private OnCorpusSelectedListener mListener;
	private ViewPager mChatEmojiVPager, mChatMoreVPager;
	private ArrayList<View> facePageViews;
	private ArrayList<View> morePageViews;
	private LinearLayout mChatIvImageMore, mChatIvImageFace;
	private ArrayList<ImageView> pointViewsFace, pointViewsMore;
	private List<List<ChatEmoji>> emojis;
	private List<FaceAdapter> faceAdapters;
	private List<MoreAdapter> moreAdapters;
	private int current = 0;
	private ArrayList<ChatMore> moreList;
	// 表情分页的结果集合
	public List<List<ChatMore>> moreLists = new ArrayList<List<ChatMore>>();
	private List<FromToMessage> fromToMessage;
	private Boolean JZflag = true;
	private View header;
	private int i = 2;
	private int height;
	private List<FromToMessage> descFromToMessage = new ArrayList<FromToMessage>();

	private static final String tag = "ChatActivity";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 200;
	private static final int PICK_FILE_ACTIVITY_REQUEST_CODE = 300;
	private String picFileFullName;

	MsgReceiver msgReceiver;
	KeFuStatusReceiver keFuStatusReceiver;

	private String peerId;

	LinearLayout chat_queue_ll;
	TextView chat_queue_tv;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
			} else if (msg.what == 1) {
				updateMessage();
			} else if (msg.what == 2) {
				// 加载更多的时候
				JZMoreMessage();
			}

			if(msg.what == 0x111) {
				//当前是机器人
				Toast.makeText(ChatActivity.this, "当前是机器人为你服务", Toast.LENGTH_SHORT).show();
				chat_btn_convert.setVisibility(View.VISIBLE);
			}

			if(msg.what == 0x222) {
				//当前是客服
				Toast.makeText(ChatActivity.this, "当前客服在线", Toast.LENGTH_SHORT).show();
				chat_btn_convert.setVisibility(View.GONE);
			}

			if(msg.what == 0x333) {
				Toast.makeText(ChatActivity.this, "当前客服不在线", Toast.LENGTH_SHORT).show();
				chat_btn_convert.setVisibility(View.GONE);
				showOffineDialog();
			}

			if(msg.what == 0x444) {
				sendInvestigate();
			}

			if(msg.what == 0x555) {
				String queueNem = (String) msg.obj;
				showQueueNumLabel(queueNem);
			}

			if(msg.what == 0x666) {
				chat_queue_ll.setVisibility(View.GONE);
				chat_btn_convert.setVisibility(View.GONE);
				Toast.makeText(ChatActivity.this, "当前是客服为你服务", Toast.LENGTH_SHORT).show();
			}

			if(msg.what == 0x777) {
				showSessionFinishDialog();
			}
			
			if(msg.what == 0x88) {
				updateMessage();
			}

			if ("拍照".equals(msg.obj)) {
				if(Build.VERSION.SDK_INT < 23) {
					takePicture();
				}else {
					//6.0
					if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
						//该权限已经有了
						takePicture();
					}else {
						//申请该权限
						ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA}, 0x3333);
					}
				}
			} else if ("图库".equals(msg.obj)) {

				if(Build.VERSION.SDK_INT < 23) {
					openAlbum();
				}else {
					//6.0
					if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
						//该权限已经有了
						System.out.println("权限已经有了");
						openAlbum();
					}else {
						//申请该权限
						ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x2222);
					}
				}
			}else if("评价".equals(msg.obj)) {
				//评价
				openInvestigateDialog();
			}else if("文件".equals(msg.obj)) {
				openFile();
			}

		}
	};

	/**
	 * 打开文件选择
	 */
	private void openFile() {
		Intent intent = null;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");//设置类型
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, PICK_FILE_ACTIVITY_REQUEST_CODE);
	}

	private void showSessionFinishDialog() {
		new AlertDialog.Builder(this).setTitle("温馨提示")
				.setMessage("客服结束了会话，你想要")
				.setPositiveButton("继续咨询", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						IMChatManager.getInstance().getPeers(new GetPeersListener() {
							@Override
							public void onSuccess(List<Peer> peers) {
								if (peers.size() > 1) {
									PeerDialog dialog = new PeerDialog();
									Bundle b = new Bundle();
									b.putSerializable("Peers", (Serializable) peers);
									b.putString("type", "chat");
									dialog.setArguments(b);
									dialog.show(getFragmentManager(), "");

								} else if (peers.size() == 1) {
									beginSession(peers.get(0).getId());
								} else {
									beginSession("");
								}
							}

							@Override
							public void onFailed() {

							}
						});
					}
				})
				.setNegativeButton("退出", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						MobileApplication.isKFSDK = false;
						IMChatManager.getInstance().quit();
						finish();
					}
				})
				.setCancelable(false)
				.create()
				.show();

	}

	/**
	 * 显示排队数
	 */
	private void showQueueNumLabel(String queueNum) {

		if(Integer.parseInt(queueNum) > 0) {
			chat_queue_ll.setVisibility(View.VISIBLE);
			chat_queue_tv.setText(queueNum);
		}else {
			chat_queue_ll.setVisibility(View.GONE);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.kf_activity_chat);

		//获取技能组id
		Intent intent = getIntent();
		if(intent.getStringExtra("PeerId") != null) {
			peerId = intent.getStringExtra("PeerId");
		}

		IntentFilter intentFilter = new IntentFilter("com.m7.imkfsdk.msgreceiver");
		msgReceiver = new MsgReceiver();
		registerReceiver(msgReceiver, intentFilter);

		IntentFilter kefuIntentFilter = new IntentFilter();
		kefuIntentFilter.addAction(IMChatManager.ROBOT_ACTION);
		kefuIntentFilter.addAction(IMChatManager.ONLINE_ACTION);
		kefuIntentFilter.addAction(IMChatManager.OFFLINE_ACTION);
		kefuIntentFilter.addAction(IMChatManager.CLIAM_ACTION);
		kefuIntentFilter.addAction(IMChatManager.INVESTIGATE_ACTION);
		kefuIntentFilter.addAction(IMChatManager.QUEUENUM_ACTION);
		kefuIntentFilter.addAction(IMChatManager.FINISH_ACTION);
		kefuIntentFilter.addAction(IMChatManager.USERINFO_ACTION);
		keFuStatusReceiver = new KeFuStatusReceiver();
		registerReceiver(keFuStatusReceiver, kefuIntentFilter);


		init();
		registerListener();
		initEmojiViewPager();
		initEmojiPoint();
		initEmojiData();
		initMoreViewPager();
		initMorePoint();
		initMoreData();
		updateMessage();

		if(Build.VERSION.SDK_INT > 22) {
			//6.0
			if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				//写存储权限已经有了
				beginSession(peerId);
			}else {
				//申请该权限
				ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x1111);
			}
		}else {
			beginSession(peerId);
		}

	}

	/**
	 * 查询数据库更新页面
	 */
	public void updateMessage() {
		fromToMessage = IMChatManager.getInstance().getMessages(1);
		descFromToMessage.clear();
		for (int i = fromToMessage.size() - 1; i >= 0; i--) {
			descFromToMessage.add(fromToMessage.get(i));
		}
		// 是否有数据
		if (IMChatManager.getInstance().isReachEndMessage(
				descFromToMessage.size())) {
			mChatList.dismiss();
		}
		chatAdapter = new ChatAdapter(ChatActivity.this, descFromToMessage);
		mChatList.setAdapter(chatAdapter);
		chatAdapter.notifyDataSetChanged();
		mChatList.setSelection(fromToMessage.size() + 1);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		updateMessage();
	}

	// 分页加载更多
	public void JZMoreMessage() {
		fromToMessage = IMChatManager.getInstance().getMessages(i);
		descFromToMessage.clear();
		for (int i = fromToMessage.size() - 1; i >= 0; i--) {
			descFromToMessage.add(fromToMessage.get(i));
		}

		chatAdapter.notifyDataSetChanged();

		if (mChatList.getHeaderViewsCount() > 0) {
			mChatList.removeHeaderView(header);
		}

		// 是否有数据
		if (IMChatManager.getInstance().isReachEndMessage(
				descFromToMessage.size())) {
			mChatList.setSelectionFromTop(fromToMessage.size() - (i - 1) * 15,
					height);
			mChatList.dismiss();
		} else {
			mChatList.setSelectionFromTop(fromToMessage.size() - (i - 1) * 15
					+ 1, height);
		}

		mChatList.onRefreshFinished();
		JZflag = true;
		i++;

	}

	// 初始化方法
	public void init() {
		// 设置进来时间软键盘不弹出的默认状态
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mChatSend = (Button) this.findViewById(R.id.chat_send);
		chat_btn_back = (ImageView) this.findViewById(R.id.chat_btn_back);
		mRecorderButton = (AudioRecorderButton) findViewById(R.id.chat_press_to_speak);
		mRecorderButton.setRecordFinishListener(this);
		mChatInput = (EditText) this.findViewById(R.id.chat_input);
		mChatEdittextLayout = (RelativeLayout) this
				.findViewById(R.id.chat_edittext_layout);
		mMore = (LinearLayout) this.findViewById(R.id.more);
		mChatEmojiNormal = (ImageView) this
				.findViewById(R.id.chat_emoji_normal);
		mChatEmojiChecked = (ImageView) this
				.findViewById(R.id.chat_emoji_checked);
		mChatFaceContainer = (RelativeLayout) this
				.findViewById(R.id.chat_face_container);
		mChatMoreContainer = (RelativeLayout) this
				.findViewById(R.id.chat_more_container);
		mChatMore = (Button) this.findViewById(R.id.chat_more);

		mChatSetModeVoice = (Button) this
				.findViewById(R.id.chat_set_mode_voice);
		mChatSetModeKeyboard = (Button) this
				.findViewById(R.id.chat_set_mode_keyboard);

		//转人工服务按钮，判断是否需要显示
		chat_btn_convert = (Button) this.findViewById(R.id.chat_btn_convert);

		mOtherName = (TextView) this.findViewById(R.id.other_name);
		mChatInput.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mChatEdittextLayout
							.setBackgroundResource(R.drawable.kf_input_bar_bg_active);
				} else {
					mChatEdittextLayout
							.setBackgroundResource(R.drawable.kf_input_bar_bg_normal);
				}

			}
		});

		mChatInput.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatEdittextLayout
						.setBackgroundResource(R.drawable.kf_input_bar_bg_active);
				mChatEmojiNormal.setVisibility(View.VISIBLE);
				mChatEmojiChecked.setVisibility(View.GONE);

				mMore.setVisibility(View.GONE);
				mChatFaceContainer.setVisibility(View.GONE);
				mChatMoreContainer.setVisibility(View.GONE);
			}
		});

		// 监听文字框
		mChatInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				if (!TextUtils.isEmpty(s)) {
					mChatMore.setVisibility(View.GONE);
					mChatSend.setVisibility(View.VISIBLE);
				} else {
					mChatMore.setVisibility(View.VISIBLE);
					mChatSend.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mChatList = (ChatListView) this.findViewById(R.id.chat_list);
		header = View.inflate(this, R.layout.kf_chatlist_header, null);
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		header.measure(w, h);
		height = header.getMeasuredHeight();

		mChatList.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				mMore.setVisibility(View.GONE);
				mChatEmojiNormal.setVisibility(View.VISIBLE);
				mChatEmojiChecked.setVisibility(View.GONE);
				mChatFaceContainer.setVisibility(View.GONE);
				mChatMoreContainer.setVisibility(View.GONE);
				return false;
			}
		});

		emojis = FaceConversionUtil.getInstace().emojiLists;

		moreList = new ArrayList<ChatMore>();
		ChatMore chatMore1 = new ChatMore(1, R.drawable.kf_icon_chat_photo + "",
				"拍照");
		ChatMore chatMore2 = new ChatMore(2, R.drawable.kf_icon_chat_pic + "",
				"图库");
		ChatMore chatMore3 = new ChatMore(3, R.drawable.kf_icon_chat_file + "",
				"文件");
		ChatMore chatMore4 = new ChatMore(4, R.drawable.kf_icon_chat_investigate + "",
				"评价");
		moreList.add(chatMore1);
		moreList.add(chatMore2);
		moreList.add(chatMore3);
		moreList.add(chatMore4);

		int pageCount = (int) Math.ceil(moreList.size() / 8 + 0.1);
		for (int i = 0; i < pageCount; i++) {
			moreLists.add(getData(i));
		}

		mChatEmojiVPager = (ViewPager) findViewById(R.id.chat_emoji_vPager);
		mChatMoreVPager = (ViewPager) findViewById(R.id.chat_more_vPager);
		mChatInput = (EditText) findViewById(R.id.chat_input);
		mChatIvImageFace = (LinearLayout) findViewById(R.id.chat_iv_image_face);
		mChatIvImageMore = (LinearLayout) findViewById(R.id.chat_iv_image_more);


		chat_queue_ll = (LinearLayout) findViewById(R.id.chat_queue_ll);
		chat_queue_tv = (TextView) findViewById(R.id.chat_queue_tv);


//		mRecorderButton = (SpeechRecorderButton) findViewById(R.id.chat_press_to_speak);
//		mRecorderButton.setSpeechRecorderListener(this);
	}

	// 注册监听方法
	public void registerListener() {
		mChatSend.setOnClickListener(this);
		chat_btn_back.setOnClickListener(this);
		mChatSetModeVoice.setOnClickListener(this);
		mChatSetModeKeyboard.setOnClickListener(this);
		mChatEmojiNormal.setOnClickListener(this);
		mChatEmojiChecked.setOnClickListener(this);
		mChatMore.setOnClickListener(this);
		mChatList.setOnRefreshListener(this);
		chat_btn_convert.setOnClickListener(this);
	}

	// 获取分页数据
	private List<ChatMore> getData(int page) {
		int startIndex = page * 8;
		int endIndex = startIndex + 8;

		if (endIndex > moreList.size()) {
			endIndex = moreList.size();
		}
		List<ChatMore> list = new ArrayList<ChatMore>();
		list.addAll(moreList.subList(startIndex, endIndex));
		if (list.size() < 8) {
			for (int i = list.size(); i < 8; i++) {
				ChatMore object = new ChatMore();
				list.add(object);
			}
		}
		return list;
	}

	// 隐藏软键盘
	private void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.chat_btn_back:
			finish();
			break;
		case R.id.chat_btn_convert:
			//转人工服务
			IMChatManager.getInstance().convertManual(new OnConvertManualListener() {
				@Override
				public void onLine() {
					//有客服在线,隐藏转人工按钮
					chat_btn_convert.setVisibility(View.GONE);
					Toast.makeText(ChatActivity.this, "转人工服务成功", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void offLine() {
					//当前没有客服在线
					showOffineDialog();
				}
			});
			break;
		case R.id.chat_send:
			String txt = mChatInput.getText().toString();

			FromToMessage fromToMessage = IMMessage.createTxtMessage(txt);

			//界面显示
			descFromToMessage.add(fromToMessage);
			chatAdapter.notifyDataSetChanged();
			mChatList.setSelection(descFromToMessage.size());
			mChatInput.setText("");

			//发送消息
			IMChat.getInstance().sendMessage(fromToMessage, new ChatListener() {
				@Override
				public void onSuccess() {
					//消息发送成功
					Log.d("ChatActivity", "文本消息发送成功");
					updateMessage();
				}

				@Override
				public void onFailed() {
					//消息发送失败
					Log.d("ChatActivity", "文本消息发送失败");
					updateMessage();
				}

				@Override
				public void onProgress() {

				}
			});

			break;

		case R.id.chat_set_mode_voice:
			if(Build.VERSION.SDK_INT < 23) {
				showVoice();
			}else {
				//6.0
				if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
					//该权限已经有了
					showVoice();
				}else {
					//申请该权限
					ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0x4444);
				}
			}

			break;

		case R.id.chat_set_mode_keyboard:
			mChatEdittextLayout.setVisibility(View.VISIBLE);
			mChatSetModeKeyboard.setVisibility(View.GONE);
			mChatSetModeVoice.setVisibility(View.VISIBLE);
			mChatInput.requestFocus();
			mRecorderButton.setVisibility(View.GONE);
			mChatFaceContainer.setVisibility(View.GONE);

			if (TextUtils.isEmpty(mChatInput.getText())) {
				mChatMore.setVisibility(View.VISIBLE);
				mChatSend.setVisibility(View.GONE);
			} else {
				mChatMore.setVisibility(View.GONE);
				mChatSend.setVisibility(View.VISIBLE);
			}

			break;
		case R.id.chat_emoji_normal:
			hideKeyboard();
			mMore.setVisibility(View.VISIBLE);
			mChatEmojiNormal.setVisibility(View.GONE);
			mChatEmojiChecked.setVisibility(View.VISIBLE);
			mChatMoreContainer.setVisibility(View.GONE);
			mChatFaceContainer.setVisibility(View.VISIBLE);
			mChatMoreVPager.setVisibility(View.GONE);
			mChatEmojiVPager.setVisibility(View.VISIBLE);
			break;
		case R.id.chat_emoji_checked:
			mChatEmojiNormal.setVisibility(View.VISIBLE);
			mChatEmojiChecked.setVisibility(View.GONE);
			mChatMoreContainer.setVisibility(View.GONE);
			mChatFaceContainer.setVisibility(View.GONE);
			mMore.setVisibility(View.GONE);
			break;

		case R.id.chat_more:
			if (mChatMoreVPager.getVisibility() == View.VISIBLE) {
				mChatMoreVPager.setVisibility(View.GONE);
				mMore.setVisibility(View.GONE);
			} else {
				mChatMoreVPager.setVisibility(View.VISIBLE);
				mMore.setVisibility(View.VISIBLE);
				mChatEmojiNormal.setVisibility(View.VISIBLE);
				mChatEmojiChecked.setVisibility(View.GONE);
				mChatFaceContainer.setVisibility(View.GONE);
				mChatMoreContainer.setVisibility(View.VISIBLE);
				mChatEmojiVPager.setVisibility(View.GONE);

				hideKeyboard();
			}

			break;
		default:
			break;
		}
	}

	private void showVoice() {
		hideKeyboard();
		mChatEdittextLayout.setVisibility(View.GONE);
		mMore.setVisibility(View.GONE);
		mChatSetModeVoice.setVisibility(View.GONE);
		mChatSetModeKeyboard.setVisibility(View.VISIBLE);
		mChatSend.setVisibility(View.GONE);
		mChatMore.setVisibility(View.VISIBLE);
		mRecorderButton.setVisibility(View.VISIBLE);
		mChatEmojiNormal.setVisibility(View.VISIBLE);
		mChatEmojiChecked.setVisibility(View.GONE);
		mChatMoreContainer.setVisibility(View.VISIBLE);
		mChatFaceContainer.setVisibility(View.GONE);
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}


	// 表情选择监听器
	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	// 初始化文件的viewpager
	private void initMoreViewPager() {

		morePageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(this);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		morePageViews.add(nullView1);

		// 中间添加表情页
		moreAdapters = new ArrayList<MoreAdapter>();
		for (int i = 0; i < moreLists.size(); i++) {
			GridView view = new GridView(this);
			MoreAdapter adapter = new MoreAdapter(this, moreLists.get(i),
					handler);
			view.setAdapter(adapter);
			moreAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(4);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			morePageViews.add(view);

		}

		// 右侧添加空页面
		View nullView2 = new View(this);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		morePageViews.add(nullView2);

	}

	// 初始化游标
	private void initMorePoint() {

		pointViewsMore = new ArrayList<ImageView>();
		ImageView imageView;
		mChatIvImageMore.removeAllViews();
		for (int i = 0; i < morePageViews.size(); i++) {
			imageView = new ImageView(this);
			imageView.setBackgroundResource(R.drawable.kf_d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			mChatIvImageMore.addView(imageView, layoutParams);
			if (i == 0 || i == morePageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.kf_d2);
			}
			pointViewsMore.add(imageView);

		}
	}

	// 填充数据
	private void initMoreData() {
		mChatMoreVPager.setAdapter(new ViewPagerAdapter(morePageViews));

		mChatMoreVPager.setCurrentItem(1);
		current = 0;
		mChatMoreVPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// 描绘分页点
				drawMorePoint(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == pointViewsMore.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						mChatMoreVPager.setCurrentItem(arg0 + 1);// 第二屏
																	// 会再次实现该回调方法实现跳转.
						pointViewsMore.get(1).setBackgroundResource(
								R.drawable.kf_d2);
					} else {
						mChatMoreVPager.setCurrentItem(arg0 - 1);// 倒数第二屏
						pointViewsMore.get(arg0 - 1).setBackgroundResource(
								R.drawable.kf_d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	// 绘制游标背景
	public void drawMorePoint(int index) {
		for (int i = 1; i < pointViewsMore.size(); i++) {
			if (index == i) {
				pointViewsMore.get(i).setBackgroundResource(R.drawable.kf_d2);
			} else {
				pointViewsMore.get(i).setBackgroundResource(R.drawable.kf_d1);
			}
		}
	}

	// 初始化显示表情的viewpager
	private void initEmojiViewPager() {
		facePageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(this);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		facePageViews.add(nullView1);

		// 中间添加表情页
		faceAdapters = new ArrayList<FaceAdapter>();

		for (int i = 0; i < emojis.size(); i++) {
			GridView view = new GridView(this);
			FaceAdapter adapter = new FaceAdapter(this, emojis.get(i));
			view.setAdapter(adapter);
			faceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(7);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			facePageViews.add(view);
		}

		// 右侧添加空页面
		View nullView2 = new View(this);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		facePageViews.add(nullView2);

	}

	// 初始化游标
	private void initEmojiPoint() {

		pointViewsFace = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < facePageViews.size(); i++) {
			imageView = new ImageView(this);
			imageView.setBackgroundResource(R.drawable.kf_d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			mChatIvImageFace.addView(imageView, layoutParams);
			if (i == 0 || i == facePageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.kf_d2);
			}
			pointViewsFace.add(imageView);

		}

	}

	// 填充数据
	private void initEmojiData() {
		mChatEmojiVPager.setAdapter(new ViewPagerAdapter(facePageViews));

		mChatEmojiVPager.setCurrentItem(1);
		current = 0;
		mChatEmojiVPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// 描绘分页点
				drawFacePoint(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == pointViewsFace.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						mChatEmojiVPager.setCurrentItem(arg0 + 1);// 第二屏
																	// 会再次实现该回调方法实现跳转.
						pointViewsFace.get(1).setBackgroundResource(
								R.drawable.kf_d2);
					} else {
						mChatEmojiVPager.setCurrentItem(arg0 - 1);// 倒数第二屏
						pointViewsFace.get(arg0 - 1).setBackgroundResource(
								R.drawable.kf_d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	// 绘制游标背景
	public void drawFacePoint(int index) {
		for (int i = 1; i < pointViewsFace.size(); i++) {
			if (index == i) {
				pointViewsFace.get(i).setBackgroundResource(R.drawable.kf_d2);
			} else {
				pointViewsFace.get(i).setBackgroundResource(R.drawable.kf_d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mChatFaceContainer.getVisibility() == View.VISIBLE
				&& mChatMoreContainer.getVisibility() == View.GONE) {

			ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(
					arg2);
			if (emoji.getId() == R.drawable.kf_face_del_icon) {
				int selection = mChatInput.getSelectionStart();
				String text = mChatInput.getText().toString();
				if (selection > 0) {
					String text2 = text.substring(selection - 1);
					if (":".equals(text2)) {
						String str = text.substring(0, selection - 1);
						int start = str.lastIndexOf(":");
						int end = selection;
						mChatInput.getText().delete(start, end);
						return;
					}
					mChatInput.getText().delete(selection - 1, selection);
				}

			}
			if (!TextUtils.isEmpty(emoji.getCharacter())) {
				if (mListener != null)
					mListener.onCorpusSelected(emoji);
				SpannableString spannableString = FaceConversionUtil
						.getInstace().addFace(this, emoji.getId(),
								emoji.getCharacter(), mChatInput);
				mChatInput.append(spannableString);
			}
		}
	}

	// 拍照
	public void takePicture() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File outDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
			File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
			picFileFullName = outFile.getAbsolutePath();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		} else {
			Log.e(tag, "请确认已经插入SD卡");
		}
	}

	// 打开本地相册
	public void openAlbum() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
		} else {
			intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		this.startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	/**
	 * 打开评价对话框
	 */
	private void openInvestigateDialog() {
		InvestigateDialog dialog = new InvestigateDialog();
		dialog.show(getFragmentManager(), "InvestigateDialog");
	}

	// 拍照回调
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Log.d("拍照发送图片", "获取图片成功，本地路径是：" + picFileFullName);
				//发送图片
				FromToMessage fromToMessage = IMMessage.createImageMessage(picFileFullName);
				ArrayList fromTomsgs = new ArrayList<FromToMessage>();
				fromTomsgs.add(fromToMessage);
				descFromToMessage.addAll(fromTomsgs);
				chatAdapter.notifyDataSetChanged();
				mChatList.setSelection(descFromToMessage.size());
				IMChat.getInstance().sendMessage(fromToMessage, new ChatListener() {
					@Override
					public void onSuccess() {
						updateMessage();
					}

					@Override
					public void onFailed() {
						updateMessage();
					}

					@Override
					public void onProgress() {

					}
				});

			} else if (resultCode == RESULT_CANCELED) {
				// 用户取消了图像捕获
			} else {
				// 图像捕获失败，提示用户
				Log.e(tag, "拍照失败");
			}
		} else if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				if (uri != null) {
					String realPath = getRealPathFromURI(uri);
					picFileFullName = realPath;
					Log.d("发送图片消息了", "图片的本地路径是：" + picFileFullName);
					//准备发送图片消息
					FromToMessage fromToMessage = IMMessage.createImageMessage(picFileFullName);
					ArrayList fromTomsgs = new ArrayList<FromToMessage>();
					fromTomsgs.add(fromToMessage);
					descFromToMessage.addAll(fromTomsgs);
					chatAdapter.notifyDataSetChanged();
					mChatList.setSelection(descFromToMessage.size());
					IMChat.getInstance().sendMessage(fromToMessage, new ChatListener() {
						@Override
						public void onSuccess() {
							updateMessage();
						}

						@Override
						public void onFailed() {
							updateMessage();
						}

						@Override
						public void onProgress() {

						}
					});
				} else {
					Log.e(tag, "从相册获取图片失败");
				}
			}
		}else if(requestCode == PICK_FILE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
			String path = "";
			if ("content".equalsIgnoreCase(uri.getScheme())) {
				String[] projection = { "_data" };
				Cursor cursor = null;
				try {
					cursor = getContentResolver().query(uri, projection,null, null, null);
					int column_index = cursor.getColumnIndexOrThrow("_data");
					if (cursor.moveToFirst()) {
						path =  cursor.getString(column_index);
					}
				} catch (Exception e) {

				}
			}else if ("file".equalsIgnoreCase(uri.getScheme())) {
				path =  uri.getPath();
			}
			File file = new File(path);
			String fileSizeStr = "";
			if(file.exists()) {
				long fileSize = file.length();

				if((fileSize / 1024 / 1024) > 20.0) {
					//大于20M不能上传
					Toast.makeText(ChatActivity.this, "上传文件不能大于20MB", Toast.LENGTH_SHORT).show();
				}else {
					fileSizeStr = FileUtils.formatFileLength(fileSize);
					String fileName = path.substring(path.lastIndexOf("/") + 1);
					//发送文件
					FromToMessage fromToMessage = IMMessage.createFileMessage(path, fileName, fileSizeStr);
					ArrayList fromTomsgs = new ArrayList<FromToMessage>();
					fromTomsgs.add(fromToMessage);
					descFromToMessage.addAll(fromTomsgs);
					chatAdapter.notifyDataSetChanged();
					mChatList.setSelection(descFromToMessage.size());
					IMChat.getInstance().sendMessage(fromToMessage, new ChatListener() {
						@Override
						public void onSuccess() {
							System.out.println("文件消息发送成功");
							updateMessage();
						}

						@Override
						public void onFailed() {
							System.out.println("文件消息发送失败");
							updateMessage();
						}

						@Override
						public void onProgress() {
							System.out.println("文件消息正在发送");
							updateMessage();
						}
					});
				}
			}
		}

	}

	// 获取字符
	public String getRealPathFromURI(Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			// Do not call Cursor.close() on a cursor obtained using this
			// method,
			// because the activity will do that for you at the appropriate time
			Cursor cursor = this.managedQuery(contentUri, proj, null, null,
					null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (Exception e) {
			return contentUri.getPath();
		}
	}

	// 覆盖手机返回键
	@Override
	public void onBackPressed() {
		if (mMore.getVisibility() == View.VISIBLE) {
			mMore.setVisibility(View.GONE);
			mChatEmojiNormal.setVisibility(View.VISIBLE);
			mChatEmojiChecked.setVisibility(View.INVISIBLE);
		} else {
			finish();
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		unregisterReceiver(msgReceiver);
		unregisterReceiver(keFuStatusReceiver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		chatAdapter.onPause();
	}

	@Override
	public void toRefresh() {
		// TODO Auto-generated method stub
		if (JZflag == true) {
			JZflag = false;
			new Thread() {
				public void run() {
					try {
						sleep(800);
						Message msg = new Message();
						msg.what = 2;
						handler.sendMessage(msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				};
			}.start();
		}
	}



	private void voiceToText(String newFilePath, final FromToMessage fromToMessage) {
		SpeechRecognizer mIat= SpeechRecognizer.createRecognizer(ChatActivity.this, null);
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
		mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, newFilePath);
		mIat.startListening(new RecognizerListener() {
			@Override
			public void onVolumeChanged(int i, byte[] bytes) {
				LogUtil.e("科大讯飞语音转化回调","onVolumeChanged");
			}

			@Override
			public void onBeginOfSpeech() {
				LogUtil.e("科大讯飞语音转化回调","onBeginOfSpeech");
			}

			@Override
			public void onEndOfSpeech() {
				LogUtil.e("科大讯飞语音转化回调","onEndOfSpeech");
			}

			@Override
			public void onResult(RecognizerResult recognizerResult, boolean b) {
				LogUtil.e("科大讯飞语音转化回调","onResult");
				if(b) {
					printResult(recognizerResult, fromToMessage);
				}

			}

			@Override
			public void onError(SpeechError speechError) {
				LogUtil.e("科大讯飞语音转化回调onError",speechError.getPlainDescription(true));
			}

			@Override
			public void onEvent(int i, int i1, int i2, Bundle bundle) {
				LogUtil.e("科大讯飞语音转化回调","onEvent");
			}
		});
	}


	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

	private void printResult(RecognizerResult results, FromToMessage fromToMessage) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		String voiceText = resultBuffer.toString();

		sendVoiceMsg(voiceText, fromToMessage);

	}

	/**
	 * 新消息接收器,用来通知界面进行更新
	 */
	class MsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			handler.sendEmptyMessage(1);
		}
	}

	/**
	 * 客服状态接收器
	 */
	class KeFuStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(IMChatManager.ROBOT_ACTION.equals(action)) {
				//当前是机器人
				handler.sendEmptyMessage(0x111);
			}else if(IMChatManager.ONLINE_ACTION.equals(action)) {
				//当前是客服
				handler.sendEmptyMessage(0x222);
			}else if(IMChatManager.OFFLINE_ACTION.equals(action)) {
				//当前是客服
				handler.sendEmptyMessage(0x333);
			}else if(IMChatManager.INVESTIGATE_ACTION.equals(action)) {
				//客服发起了评价
				System.out.println("接收到了评价");
				handler.sendEmptyMessage(0x444);
			}else if(IMChatManager.QUEUENUM_ACTION.equals(action)) {
				//技能组排队数
				System.out.println("接收到排队人数的广播");
				if(intent.getStringExtra(IMChatManager.QUEUENUM_ACTION) != null) {
					String queueNum = intent.getStringExtra(IMChatManager.QUEUENUM_ACTION);
					Message queueMsg = Message.obtain();
					queueMsg.what = 0x555;
					queueMsg.obj = queueNum;
					handler.sendMessage(queueMsg);
				}
			}else if(IMChatManager.CLIAM_ACTION.equals(action)) {
				//客服领取了会话
				handler.sendEmptyMessage(0x666);
			}else if(IMChatManager.FINISH_ACTION.equals(action)) {
				//客服关闭了会话
				handler.sendEmptyMessage(0x777);
			}else if(IMChatManager.USERINFO_ACTION.equals(action)) {
				//客服信息
				String type = intent.getStringExtra(IMChatManager.CONSTANT_TYPE);
				String exten = intent.getStringExtra(IMChatManager.CONSTANT_EXTEN);
				String userName = intent.getStringExtra(IMChatManager.CONSTANT_USERNAME);
				String userIcon = intent.getStringExtra(IMChatManager.CONSTANT_USERICON);

				LogUtil.e("聊天界面,客服姓名", NullUtil.checkNull(userName));

				mOtherName.setText("客服"+userName+"为您服务");
			}
		}
	}

	private void beginSession(String peerId) {
		IMChatManager.getInstance().beginSession(peerId, new OnSessionBeginListener() {

			@Override
			public void onSuccess() {

				if(IMChatManager.getInstance().isInvestigateOn()) {
					//显示评价按钮
					moreList.clear();
					ChatMore chatMore1 = new ChatMore(1, R.drawable.kf_icon_chat_photo + "",
							"拍照");
					ChatMore chatMore2 = new ChatMore(2, R.drawable.kf_icon_chat_pic + "",
							"图库");
					ChatMore chatMore3 = new ChatMore(3, R.drawable.kf_icon_chat_file + "",
							"文件");
					ChatMore chatMore4 = new ChatMore(4, R.drawable.kf_icon_chat_investigate + "",
							"评价");
					moreList.add(chatMore1);
					moreList.add(chatMore2);
					moreList.add(chatMore3);
					moreList.add(chatMore4);

					moreLists.clear();
					int pageCount = (int) Math.ceil(moreList.size() / 8 + 0.1);
					for (int i = 0; i < pageCount; i++) {
						moreLists.add(getData(i));
					}
					initMoreViewPager();
					initMorePoint();
					initMoreData();
				}else {
					//隐藏评价按钮
					moreList.clear();
					ChatMore chatMore1 = new ChatMore(1, R.drawable.kf_icon_chat_photo + "",
							"拍照");
					ChatMore chatMore2 = new ChatMore(2, R.drawable.kf_icon_chat_pic + "",
							"图库");
					ChatMore chatMore3 = new ChatMore(3, R.drawable.kf_icon_chat_file + "",
							"文件");
					moreList.add(chatMore1);
					moreList.add(chatMore2);
					moreList.add(chatMore3);

					moreLists.clear();
					int pageCount = (int) Math.ceil(moreList.size() / 8 + 0.1);
					for (int i = 0; i < pageCount; i++) {
						moreLists.add(getData(i));
					}
					initMoreViewPager();
					initMorePoint();
					initMoreData();
				}
			}

			@Override
			public void onFailed() {
				chat_btn_convert.setVisibility(View.GONE);
				showOffineDialog();
			}
		});
	}

	private void showOffineDialog() {
		Intent intent = new Intent(ChatActivity.this, OfflineMessageActicity.class);
		intent.putExtra("PeerId", peerId);
		startActivity(intent);
		finish();
	}


	public ChatAdapter getChatAdapter() {
		return chatAdapter;
	}

	public void resendMsg(FromToMessage msg, int position) {
		IMChat.getInstance().reSendMessage(msg, new ChatListener() {
			@Override
			public void onSuccess() {
				updateMessage();
			}

			@Override
			public void onFailed() {
				updateMessage();
			}

			@Override
			public void onProgress() {
				updateMessage();
			}
		});
	}

	/**
	 * 客服主动发起评价
	 */
	private void sendInvestigate() {
		ArrayList<Investigate> investigates = (ArrayList<Investigate>) IMChatManager.getInstance().getInvestigate();
		IMMessage.createInvestigateMessage(investigates);
		updateMessage();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case 0x1111:
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
					beginSession(peerId);
				} else {
					//不给权限就关闭界面
					finish();
				}
				break;
			case 0x2222:
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
					openAlbum();
				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				break;
			case 0x3333:
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
					takePicture();
				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				break;
			case 0x4444:
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
					showVoice();
				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				break;
		}
	}

	public ChatListView getChatListView() {
		return mChatList;
	}


	@Override
	public void onRecordFinished(float mTime, String filePath, String pcmFilePath) {

		//先在界面上显示出来
		FromToMessage fromToMessage = IMMessage.createAudioMessage(mTime, filePath, "");
		descFromToMessage.add(fromToMessage);
		chatAdapter.notifyDataSetChanged();
		mChatList.setSelection(descFromToMessage.size());

		voiceToText(pcmFilePath, fromToMessage);
	}

	/**
	 * 发送录音消息
	 * @param voiceText
     */
	private void sendVoiceMsg(String voiceText, FromToMessage fromToMessage){

		fromToMessage.voiceText = voiceText;

		IMChat.getInstance().sendMessage(fromToMessage, new ChatListener() {
			@Override
			public void onSuccess() {
				updateMessage();
			}

			@Override
			public void onFailed() {
				updateMessage();
			}

			@Override
			public void onProgress() {

			}
		});
	}

}
