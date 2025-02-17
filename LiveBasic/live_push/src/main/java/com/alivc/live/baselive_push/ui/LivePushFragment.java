package com.alivc.live.baselive_push.ui;

import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK;
import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alivc.component.custom.AlivcLivePushCustomFilter;
import com.alivc.live.annotations.AlivcLiveConnectionStatus;
import com.alivc.live.annotations.AlivcLiveConnectionStatusChangeReason;
import com.alivc.live.annotations.AlivcLiveMode;
import com.alivc.live.annotations.AlivcLiveNetworkQuality;
import com.alivc.live.annotations.AlivcLivePushKickedOutType;
import com.alivc.live.annotations.AlivcLiveRecordAudioQuality;
import com.alivc.live.annotations.AlivcLiveRecordMediaEvent;
import com.alivc.live.annotations.AlivcLiveRecordMediaFormat;
import com.alivc.live.annotations.AlivcLiveRecordStreamType;
import com.alivc.live.commonbiz.testapi.EGLContextTest;
import com.alivc.live.commonui.dialog.CommonDialog;
import com.alivc.live.commonui.messageview.AutoScrollMessagesView;
import com.alivc.live.commonui.seiview.LivePusherSEIView;
import com.alivc.live.commonui.widgets.LivePushTextSwitch;
import com.alivc.live.baselive_push.R;
import com.alivc.live.baselive_push.adapter.IPushController;
import com.alivc.live.baselive_push.adapter.OnSoundEffectChangedListener;
import com.alivc.live.baselive_push.ui.widget.PushMoreConfigBottomSheetLive;
import com.alivc.live.baselive_push.ui.widget.PushMusicBottomSheetLive;
import com.alivc.live.baselive_push.ui.widget.SoundEffectView;
import com.alivc.live.beauty.BeautyFactory;
import com.alivc.live.beauty.BeautyInterface;
import com.alivc.live.beauty.constant.BeautySDKType;
import com.alivc.live.commonbiz.SharedPreferenceUtils;
import com.alivc.live.commonutils.FastClickUtil;
import com.alivc.live.commonutils.FileUtil;
import com.alivc.live.commonutils.TextFormatUtil;
import com.alivc.live.commonutils.ToastUtils;
import com.alivc.live.player.annotations.AlivcLivePlayVideoStreamType;
import com.alivc.live.pusher.AlivcLiveLocalRecordConfig;
import com.alivc.live.pusher.AlivcLiveNetworkQualityProbeConfig;
import com.alivc.live.pusher.AlivcLiveNetworkQualityProbeResult;
import com.alivc.live.pusher.AlivcLivePublishState;
import com.alivc.live.pusher.AlivcLivePushAudioEffectReverbMode;
import com.alivc.live.pusher.AlivcLivePushAudioEffectVoiceChangeMode;
import com.alivc.live.pusher.AlivcLivePushBGMListener;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewDisplayMode;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.alivc.live.pusher.AlivcSnapshotListener;
import com.alivc.live.pusher.WaterMarkInfo;
import com.aliyunsdk.queen.menu.QueenBeautyMenu;
import com.aliyunsdk.queen.menu.QueenMenuPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class LivePushFragment extends Fragment {
    public static final String TAG = "LivePushFragment";

    private static final String PUSH_CONFIG = "push_config";
    private static final String URL_KEY = "url_key";
    private static final String ASYNC_KEY = "async_key";
    private static final String AUDIO_ONLY_KEY = "audio_only_key";
    private static final String VIDEO_ONLY_KEY = "video_only_key";
    private static final String QUALITY_MODE_KEY = "quality_mode_key";
    private static final String CAMERA_ID = "camera_id";
    private static final String FLASH_ON = "flash_on";
    private static final String AUTH_TIME = "auth_time";
    private static final String PRIVACY_KEY = "privacy_key";
    private static final String MIX_EXTERN = "mix_extern";
    private static final String MIX_MAIN = "mix_main";
    private static final String BEAUTY_CHECKED = "beauty_checked";
    private static final String FPS = "fps";
    private static final String PREVIEW_ORIENTATION = "preview_orientation";
    private static final String WATER_MARK_INFOS = "water_mark";
    private ImageView mExit;
    private View mMusic;
    private View mFlash;
    private View mCamera;
    private View mSnapshot;
    private View mBeautyButton;
    private View mSoundEffectButton;

    private TextView mPreviewButton;
    private TextView mPushButton;
    private TextView mOperaButton;
    private TextView mMore;
    private TextView mRestartButton;
    private TextView mDataButton;
    private TextView mNetworkDetectButton;

    private AlivcLivePushConfig mAlivcLivePushConfig = null;
    private String mPushUrl = null;
    private boolean mAsync = false;

    private boolean mAudio = false;
    private boolean mVideoOnly = false;

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isFlash = false;
    private boolean mMixExtern = false;
    private boolean mMixMain = false;
    private boolean flashState = true;

    private int snapshotCount = 0;

    private int mQualityMode = 0;

    ScheduledExecutorService mExecutorService = new ScheduledThreadPoolExecutor(5);
    private boolean mIsStartAsnycPushing = false;

    private PushMusicBottomSheetLive mMusicDialog = null;

    private String mAuthString = "?auth_key=%1$d-%2$d-%3$d-%4$s";
    private String mMd5String = "%1$s-%2$d-%3$d-%4$d-%5$s";
    private String mTempUrl = null;
    private String mAuthTime = "";
    private String mPrivacyKey = "";
    private TextView mStatusTV;
    private LinearLayout mActionBar;
    Vector<Integer> mDynamicals = new Vector<>();
    // 高级美颜管理类
    private BeautyInterface mBeautyManager;
    private boolean isBeautyEnable = true;
    private int mCurBr;
    private int mTargetBr;
    private boolean mBeautyOn = true;
    private int mFps;
    private int mPreviewOrientation;
    private CommonDialog mDialog;
    private boolean isConnectResult = false;//是否正在链接中
    private QueenBeautyMenu mBeautyBeautyContainerView;

    private QueenMenuPanel mQueenMenuPanel;
    private IPushController mPushController = null;
    private ArrayList<WaterMarkInfo> waterMarkInfos = null;
    //音效
    private SoundEffectView mSoundEffectView;

    private LivePushViewModel mLivePushViewModel;
    private LivePusherSEIView mSeiView;
    private LivePushTextSwitch mShowCustomMessageView;
    private LivePushTextSwitch mLocalRecordView;
    private AlivcResolutionEnum mCurrentResolution;

    private AutoScrollMessagesView mMessagesView;
    private boolean isShowStatistics = false;

    private TextView mManualCreateEGLContextTv;

    private boolean mIsBGMPlaying = false;

    public static LivePushFragment newInstance(AlivcLivePushConfig livePushConfig, String url, boolean async, boolean mAudio, boolean mVideoOnly, int cameraId,
                                               boolean isFlash, int mode, String authTime, String privacyKey, boolean mixExtern,
                                               boolean mixMain, boolean beautyOn, int fps, int previewOrientation,
                                               ArrayList<WaterMarkInfo> waterMarkInfos) {
        LivePushFragment livePushFragment = new LivePushFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PUSH_CONFIG, livePushConfig);
        bundle.putString(URL_KEY, url);
        bundle.putBoolean(ASYNC_KEY, async);
        bundle.putBoolean(AUDIO_ONLY_KEY, mAudio);
        bundle.putBoolean(VIDEO_ONLY_KEY, mVideoOnly);
        bundle.putInt(QUALITY_MODE_KEY, mode);
        bundle.putInt(CAMERA_ID, cameraId);
        bundle.putBoolean(FLASH_ON, isFlash);
        bundle.putString(AUTH_TIME, authTime);
        bundle.putString(PRIVACY_KEY, privacyKey);
        bundle.putBoolean(MIX_EXTERN, mixExtern);
        bundle.putBoolean(MIX_MAIN, mixMain);
        bundle.putBoolean(BEAUTY_CHECKED, beautyOn);
        bundle.putInt(FPS, fps);
        bundle.putInt(PREVIEW_ORIENTATION, previewOrientation);
        bundle.putSerializable(WATER_MARK_INFOS, waterMarkInfos);
        livePushFragment.setArguments(bundle);
        return livePushFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof IPushController) {
            mPushController = (IPushController) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Fragment may be recreated, so move all init logic to onActivityCreated
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLivePushViewModel = new ViewModelProvider(getActivity()).get(LivePushViewModel.class);

        AlivcLivePusher pusher = mPushController.getLivePusher();
        if (getArguments() != null) {
            mAlivcLivePushConfig = (AlivcLivePushConfig) getArguments().getSerializable(PUSH_CONFIG);
            mPushUrl = getArguments().getString(URL_KEY);
            mAsync = getArguments().getBoolean(ASYNC_KEY, false);
            mAudio = getArguments().getBoolean(AUDIO_ONLY_KEY, false);
            mVideoOnly = getArguments().getBoolean(VIDEO_ONLY_KEY, false);
            mCameraId = getArguments().getInt(CAMERA_ID);
            isFlash = getArguments().getBoolean(FLASH_ON, false);
            mMixExtern = getArguments().getBoolean(MIX_EXTERN, false);
            mMixMain = getArguments().getBoolean(MIX_MAIN, false);
            mQualityMode = getArguments().getInt(QUALITY_MODE_KEY);
            mBeautyOn = getArguments().getBoolean(BEAUTY_CHECKED);
            mFps = getArguments().getInt(FPS);
            mPreviewOrientation = getArguments().getInt(PREVIEW_ORIENTATION);
            waterMarkInfos = (ArrayList<WaterMarkInfo>) getArguments().getSerializable(WATER_MARK_INFOS);
            flashState = isFlash;
        }
        if (pusher != null) {
            pusher.setLivePushInfoListener(mPushInfoListener);
            pusher.setLivePushErrorListener(mPushErrorListener);
            pusher.setLivePushNetworkListener(mPushNetworkListener);
            pusher.setLivePushBGMListener(mPushBGMListener);
        }

        if (mAlivcLivePushConfig.isExternMainStream()) {
            mLivePushViewModel.startPCM(pusher);
            mLivePushViewModel.startYUV(pusher);
        }

        if (pusher != null && (mBeautyOn)) {
            pusher.setCustomFilter(new AlivcLivePushCustomFilter() {
                @Override
                public void customFilterCreate() {
                    sendLogToMessageView("[CBK][CustomFilter] customFilterCreate: [" + Thread.currentThread().getId() + "]");
                    initBeautyManager();
                }

                @Override
                public int customFilterProcess(int inputTexture, int textureWidth, int textureHeight, long extra) {
                    if (mBeautyManager == null) {
                        return inputTexture;
                    }

                    return mBeautyManager.onTextureInput(inputTexture, textureWidth, textureHeight);
                }

                @Override
                public void customFilterDestroy() {
                    destroyBeautyManager();
                    sendLogToMessageView("[CBK][CustomFilter] customFilterDestroy: [" + Thread.currentThread().getId() + "]");
                }
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.push_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQueenMenuPanel = QueenBeautyMenu.getPanel(this.getContext());
        mQueenMenuPanel.onHideMenu();
        mQueenMenuPanel.onHideValidFeatures();
        mQueenMenuPanel.onHideCopyright();

        mMessagesView = view.findViewById(R.id.v_messages);
        sendLogToMessageView("----------statistics info----------");

        mBeautyBeautyContainerView = view.findViewById(R.id.beauty_beauty_menuPanel);
        mBeautyBeautyContainerView.addView(mQueenMenuPanel);

        mSoundEffectView = view.findViewById(R.id.sound_effect_view);

        // 发送SEI
        mSeiView = view.findViewById(R.id.sei_view);
        mSeiView.setSendSeiViewListener((payload, text) -> mPushController.getLivePusher().sendMessage(text, 0, 0, true));

        mShowCustomMessageView = view.findViewById(R.id.btn_show_custom_message);
        mShowCustomMessageView.setTextViewText(getString(R.string.sei_send_custom_message_tv));
        mShowCustomMessageView.setOnSwitchToggleListener(isChecked -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            mSeiView.setVisibility(visibility);
        });

        // 本地录制
        mLocalRecordView = view.findViewById(R.id.btn_local_record);
        mLocalRecordView.setTextViewText(getString(R.string.local_record_tv));
        mLocalRecordView.setOnSwitchToggleListener(isChecked -> {
            AlivcLivePusher livePusher = mPushController.getLivePusher();
            if (livePusher == null) {
                return;
            }
            if (isChecked) {
                AlivcLiveLocalRecordConfig recordConfig = new AlivcLiveLocalRecordConfig();
                String dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                recordConfig.storagePath = FileUtil.getExternalCacheFolder(getContext()) + "/" + dateFormat + ".mp4";
                recordConfig.streamType = AlivcLiveRecordStreamType.AUDIO_VIDEO;
                recordConfig.audioQuality = AlivcLiveRecordAudioQuality.MEDIUM;
                recordConfig.mediaFormat = AlivcLiveRecordMediaFormat.MP4;
                livePusher.startLocalRecord(recordConfig);
            } else {
                livePusher.stopLocalRecord();
            }
        });

        mManualCreateEGLContextTv = view.findViewById(R.id.tv_manual_create_egl_context);
        mManualCreateEGLContextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(() -> EGLContextTest.testGLContext());
                sendLogToMessageView("Manual Create EGLContext: " + thread.getName());
                thread.start();
            }
        });

        mSoundEffectView.setOnSoundEffectChangedListener(new OnSoundEffectChangedListener() {
            @Override
            public void onSoundEffectChangeVoiceModeSelected(int position) {
                if (position >= 0 && position < AlivcLivePushAudioEffectVoiceChangeMode.values().length) {
                    AlivcLivePusher livePusher = mPushController.getLivePusher();
                    Log.d(TAG, "onSoundEffectChangeVoiceModeSelected: " + position + " --- " + AlivcLivePushAudioEffectVoiceChangeMode.values()[position]);
                    livePusher.setAudioEffectVoiceChangeMode(AlivcLivePushAudioEffectVoiceChangeMode.values()[position]);
                }
            }

            @Override
            public void onSoundEffectRevertBSelected(int position) {
                if (position >= 0 && position < AlivcLivePushAudioEffectReverbMode.values().length) {
                    AlivcLivePusher livePusher = mPushController.getLivePusher();
                    Log.d(TAG, "onSoundEffectRevertBSelected: " + position + " --- " + AlivcLivePushAudioEffectReverbMode.values()[position]);
                    livePusher.setAudioEffectReverbMode(AlivcLivePushAudioEffectReverbMode.values()[position]);
                }
            }
        });
        mDataButton = (TextView) view.findViewById(R.id.data);
        mNetworkDetectButton = (TextView) view.findViewById(R.id.network_detect);
        mStatusTV = (TextView) view.findViewById(R.id.tv_status);
        mExit = (ImageView) view.findViewById(R.id.exit);
        mMusic = view.findViewById(R.id.music);
        mFlash = view.findViewById(R.id.flash);
        mFlash.setSelected(isFlash);
        mCamera = view.findViewById(R.id.camera);
        mSnapshot = view.findViewById(R.id.snapshot);
        mActionBar = (LinearLayout) view.findViewById(R.id.action_bar);
        mCamera.setSelected(true);
        mSnapshot.setSelected(true);
        mPreviewButton = view.findViewById(R.id.preview_button);
        mPreviewButton.setSelected(true);
        mPushButton = view.findViewById(R.id.push_button);
        mPushButton.setSelected(false);
        mOperaButton = view.findViewById(R.id.opera_button);
        mOperaButton.setSelected(false);
        mMore = (TextView) view.findViewById(R.id.more);
        mBeautyButton = view.findViewById(R.id.beauty_button);
        mBeautyButton.setSelected(SharedPreferenceUtils.isBeautyOn(getActivity().getApplicationContext()));
        mSoundEffectButton = view.findViewById(R.id.sound_effect_button);
        mRestartButton = (TextView) view.findViewById(R.id.restart_button);
        mExit.setOnClickListener(onClickListener);
        mMusic.setOnClickListener(onClickListener);
        mFlash.setOnClickListener(onClickListener);

        mCamera.setOnClickListener(onClickListener);
        mSnapshot.setOnClickListener(onClickListener);
        mPreviewButton.setOnClickListener(onClickListener);
        mSoundEffectButton.setOnClickListener(onClickListener);
        mPushButton.setOnClickListener(onClickListener);
        mOperaButton.setOnClickListener(onClickListener);
        mBeautyButton.setOnClickListener(onClickListener);
        mRestartButton.setOnClickListener(onClickListener);
        mMore.setOnClickListener(onClickListener);
        mDataButton.setOnClickListener(onClickListener);
        mNetworkDetectButton.setOnClickListener(onClickListener);

        if (mVideoOnly) {
            mMusic.setVisibility(View.GONE);
        }
        if (mAudio) {
            mPreviewButton.setVisibility(View.GONE);
        }
        if (mMixMain) {
            mBeautyButton.setVisibility(View.GONE);
            mMusic.setVisibility(View.GONE);
            mFlash.setVisibility(View.GONE);
            mCamera.setVisibility(View.GONE);
        }
        mMore.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mBeautyButton.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mFlash.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mCamera.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mFlash.setClickable(mCameraId != CAMERA_TYPE_FRONT.getCameraId());
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mPushButton.isSelected() && !isConnectResult) {
                        showDialog(getSafeString(R.string.connecting_dialog_tips));
                    } else {
                        getActivity().finish();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final int id = view.getId();
            AlivcLivePusher pusher = mPushController.getLivePusher();
            if (pusher == null) {
                return;
            }

            if (id == R.id.music) {
                if (!mAlivcLivePushConfig.isVideoOnly()) {
                    if (mMusicDialog == null) {
                        mMusicDialog = new PushMusicBottomSheetLive(getContext());
                        mMusicDialog.setOnMusicSelectListener(new PushMusicBottomSheetLive.OnMusicSelectListener() {
                            @Override
                            public void onBGMEarsBack(boolean state) {
                                pusher.setBGMEarsBack(state);
                            }

                            @Override
                            public void onAudioNoise(boolean state) {
                                pusher.setAudioDenoise(state);
                            }

                            @Override
                            public void onEarphone(boolean state) {
                                pusher.enableSpeakerphone(!state);
                            }

                            /**
                             * 开启音频智能降噪（使用智能降噪须知）
                             * <p>
                             * 1、使用智能降噪，请关闭普通降噪；两者功能互斥使用
                             * 2、智能降噪功能以插件形式提供，调用该接口前，请确保已集成了libpluginAliDenoise.so；插件获取方式请参考官网文档；
                             * 3、此接口可以通话过程中控制打开智能降噪功能，通话过程中可以支持开启和关闭智能降噪
                             * 4、默认关闭，开启后可能导致功耗增加，智能降噪适合于会议，教育等语音通讯为主的场景，不适合有背景音乐的场景
                             * <p>
                             * 注意事项！！！
                             * 如遇libMNN相关的so冲突，请检查当前工程中是否使用到了视频云的其它产品，如美颜SDK/Animoji SDK
                             * 美颜SDK/Animoji SDK中，包含libMNN相关的so，因此外部无需再导入一份，只保留libpluginAliDenoise.so，
                             * 全局MNN相关的库，统一留一份即可；
                             * 具体请查看官网文档或API文档，或者您可以咨询技术同学协助解决问题；
                             *
                             * @see <a href="https://help.aliyun.com/zh/live/developer-reference/push-sdk/">推流SDK（新版）官网文档</a>
                             */
                            @Override
                            public void onAudioIntelligentNoise(boolean state) {
                                int result = 0;
                                if (state) {
                                    result = pusher.startIntelligentDenoise();
                                } else {
                                    result = pusher.stopIntelligentDenoise();
                                }
                                ToastUtils.show(result == 0 ? getSafeString(R.string.success) : getSafeString(R.string.failed));
                            }

                            @Override
                            public void onBGPlay(boolean state) {
                                if (state) {
                                    pusher.resumeBGM();
                                } else {
                                    pusher.pauseBGM();
                                }
                            }

                            @Override
                            public void onBgResource(String path) {
                                if (!TextUtils.isEmpty(path)) {
                                    pusher.startBGMAsync(path);
                                    if (mAlivcLivePushConfig.getLivePushMode() == AlivcLiveMode.AlivcLiveInteractiveMode) {
                                        //互动模式，开启 bgm 后，需要设置音量，否则没有声音
                                        pusher.setBGMVolume(50);
                                    }
                                } else {
                                    try {
                                        pusher.stopBGMAsync();
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onBGLoop(boolean state) {
                                pusher.setBGMLoop(state);
                            }

                            @Override
                            public void onMute(boolean state) {
                                pusher.setMute(state);
                            }

                            @Override
                            public void onCaptureVolume(int progress) {
                                pusher.setCaptureVolume(progress);
                            }

                            @Override
                            public void onBGMVolume(int progress) {
                                pusher.setBGMVolume(progress);
                            }
                        });
                    }
                    mMusicDialog.show();
                }
                return;
            }


            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    LivePushActivity.PauseState stateListener = mPushController.getPauseStateListener();
                    try {
                        if (id == R.id.exit) {
                            if (mPushButton.isSelected() && !isConnectResult) {
                                showDialog(getSafeString(R.string.connecting_dialog_tips));
                                return;
                            }
                            getActivity().finish();
                        } else if (id == R.id.flash) {
                            pusher.setFlash(!mFlash.isSelected());
                            flashState = !mFlash.isSelected();
                            mFlash.post(new Runnable() {
                                @Override
                                public void run() {
                                    mFlash.setSelected(!mFlash.isSelected());
                                }
                            });
                        } else if (id == R.id.camera) {
                            if (FastClickUtil.isProcessing()) {
                                return;
                            }
                            if (mCameraId == CAMERA_TYPE_FRONT.getCameraId()) {
                                mCameraId = CAMERA_TYPE_BACK.getCameraId();
                            } else {
                                mCameraId = CAMERA_TYPE_FRONT.getCameraId();
                            }
                            pusher.switchCamera();
                            if (mBeautyManager != null) {
                                mBeautyManager.switchCameraId(mCameraId);
                            }
                            mFlash.post(new Runnable() {
                                @Override
                                public void run() {
                                    mFlash.setClickable(mCameraId != CAMERA_TYPE_FRONT.getCameraId());
                                    if (mCameraId == CAMERA_TYPE_FRONT.getCameraId()) {
                                        mFlash.setSelected(false);
                                    } else {
                                        mFlash.setSelected(flashState);
                                    }
                                }
                            });
                        } else if (id == R.id.preview_button) {
                            if (FastClickUtil.isProcessing()) {
                                return;
                            }
                            final boolean isPreview = mPreviewButton.isSelected();
                            if (isPreview) {
                                pusher.stopPreview();
                            } else {
                                SurfaceView previewView = mPushController.getPreviewView();
                                if (mAsync) {
                                    pusher.startPreviewAsync(previewView);
                                } else {
                                    pusher.startPreview(previewView);
                                }
                            }

                            mPreviewButton.post(new Runnable() {
                                @Override
                                public void run() {
                                    mPreviewButton.setText(isPreview ? getSafeString(R.string.start_preview_button) : getSafeString(R.string.stop_preview_button));
                                    mPreviewButton.setSelected(!isPreview);
                                }
                            });
                        } else if (id == R.id.push_button) {
                            final boolean isPush = mPushButton.isSelected();
                            if (!isPush) {
                                if (mAsync) {
                                    pusher.startPushAsync(mPushUrl);
                                } else {
                                    pusher.startPush(mPushUrl);
                                }
                            } else {
                                pusher.stopPush();
                                mOperaButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOperaButton.setText(getSafeString(R.string.pause_button));
                                        mOperaButton.setSelected(false);
                                    }
                                });
                                if (stateListener != null) {
                                    stateListener.updatePause(false);
                                }
                            }

                            mPushButton.post(new Runnable() {
                                @Override
                                public void run() {
                                    mStatusTV.setText(isPush ? getSafeString(R.string.wating_push) : getSafeString(R.string.pushing));
                                    mPushButton.setText(isPush ? getSafeString(R.string.start_push) : getSafeString(R.string.stop_button));
                                    mPushButton.setSelected(!isPush);
                                }
                            });
                        } else if (id == R.id.opera_button) {
                            final boolean isPause = mOperaButton.isSelected();
                            if (!isPause) {
                                pusher.pause();
                            } else {
                                if (mAsync) {
                                    pusher.resumeAsync();
                                } else {
                                    pusher.resume();
                                }
                            }

                            if (stateListener != null) {
                                stateListener.updatePause(!isPause);
                            }
                            updateOperaButtonState(isPause);
                        } else if (id == R.id.sound_effect_button) {
                            if (!mAlivcLivePushConfig.isVideoOnly()) {
                                mSoundEffectView.post(() -> {
                                    if (mBeautyBeautyContainerView.getVisibility() == View.VISIBLE) {
                                        changeBeautyContainerVisibility();
                                    }
                                    changeSoundEffectVisibility();
                                });
                            }
                        } else if (id == R.id.beauty_button) {
                            if (!mBeautyOn) {
                                ToastUtils.show(getSafeString(R.string.beauty_off_tips));
                                return;
                            }
                            if (!mAlivcLivePushConfig.isAudioOnly()) {
                                mBeautyButton.post(() -> {
                                    if (mSoundEffectView.getVisibility() == View.VISIBLE) {
                                        changeSoundEffectVisibility();
                                    }
                                    changeBeautyContainerVisibility();
                                });
                            }
                        } else if (id == R.id.restart_button) {
                            if (mAsync) {
                                if (!mIsStartAsnycPushing) {
                                    mIsStartAsnycPushing = true;
                                    pusher.restartPushAsync();
                                }
                            } else {
                                pusher.restartPush();
                            }
                        } else if (id == R.id.more) {
                            mMore.post(new Runnable() {
                                @Override
                                public void run() {
                                    PushMoreConfigBottomSheetLive pushMoreDialog = new PushMoreConfigBottomSheetLive(getActivity(), R.style.Live_BottomSheetDialog);
                                    pushMoreDialog.setOnMoreConfigListener(new PushMoreConfigBottomSheetLive.OnMoreConfigListener() {

                                        @Override
                                        public void onDisplayModeChanged(AlivcPreviewDisplayMode mode) {
                                            pusher.setPreviewMode(mode);
                                        }

                                        @Override
                                        public void onPushMirror(boolean state) {
                                            pusher.setPushMirror(state);
                                        }

                                        @Override
                                        public void onPreviewMirror(boolean state) {
                                            pusher.setPreviewMirror(state);

                                        }

                                        @Override
                                        public void onAutoFocus(boolean state) {
                                            pusher.setAutoFocus(state);
                                        }

                                        @Override
                                        public void onAddDynamic() {
                                            if (mDynamicals.size() < 5) {
                                                float startX = 0.1f + mDynamicals.size() * 0.2f;
                                                float startY = 0.1f + mDynamicals.size() * 0.2f;
                                                int id = pusher.addDynamicsAddons(getActivity().getFilesDir().getPath() + File.separator + "alivc_resource/qizi/", startX, startY, 0.2f, 0.2f);
                                                if (id > 0) {
                                                    mDynamicals.add(id);
                                                } else {
                                                    ToastUtils.show(getSafeString(R.string.add_dynamic_failed) + id);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onRemoveDynamic() {
                                            if (mDynamicals.size() > 0) {
                                                int index = mDynamicals.size() - 1;
                                                int id = mDynamicals.get(index);
                                                pusher.removeDynamicsAddons(id);
                                                mDynamicals.remove(index);
                                            }
                                        }

                                        @Override
                                        public void onResolutionChanged(AlivcResolutionEnum resolutionEnum) {
                                            mCurrentResolution = resolutionEnum;
                                            if (resolutionEnum != AlivcResolutionEnum.RESOLUTION_SELF_DEFINE) {
                                                pusher.changeResolution(resolutionEnum);
                                            }
                                        }
                                    });
                                    pushMoreDialog.setOnDismissListener(dialogInterface -> {
                                        if (mCurrentResolution == AlivcResolutionEnum.RESOLUTION_SELF_DEFINE) {
                                            AlivcResolutionEnum.RESOLUTION_SELF_DEFINE.setSelfDefineResolution(pushMoreDialog.getResolutionWidth(), pushMoreDialog.getResolutionHeight());
                                            pusher.changeResolution(AlivcResolutionEnum.RESOLUTION_SELF_DEFINE);
                                        }
                                    });
                                    pushMoreDialog.setQualityMode(mQualityMode);
                                    pushMoreDialog.show();
                                }
                            });


//                            PushMoreDialog pushMoreDialog = new PushMoreDialog();
//                            pushMoreDialog.setAlivcLivePusher(pusher, new DynamicListener() {
//                                @Override
//                                public void onAddDynamic() {
//                                    if (mDynamicals.size() < 5) {
//                                        float startX = 0.1f + mDynamicals.size() * 0.2f;
//                                        float startY = 0.1f + mDynamicals.size() * 0.2f;
//                                        int id = pusher.addDynamicsAddons(getActivity().getFilesDir().getPath() + File.separator + "alivc_resource/qizi/", startX, startY, 0.2f, 0.2f);
//                                        if (id > 0) {
//                                            mDynamicals.add(id);
//                                        } else {
//                                            ToastUtils.show(getSafeString(R.string.add_dynamic_failed) + id);
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onRemoveDynamic() {
//                                    if (mDynamicals.size() > 0) {
//                                        int index = mDynamicals.size() - 1;
//                                        int id = mDynamicals.get(index);
//                                        pusher.removeDynamicsAddons(id);
//                                        mDynamicals.remove(index);
//                                    }
//                                }
//                            });
//                            pushMoreDialog.setQualityMode(mQualityMode);
//                            pushMoreDialog.setPushUrl(mPushUrl);
//                            pushMoreDialog.show(getFragmentManager(), "moreDialog");
                        } else if (id == R.id.snapshot) {
                            pusher.snapshot(1, 0, new AlivcSnapshotListener() {
                                @Override
                                public void onSnapshot(Bitmap bmp) {
                                    String dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SS").format(new Date());
                                    File f = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "snapshot-" + dateFormat + ".png");
                                    if (f.exists()) {
                                        f.delete();
                                    }
                                    try {
                                        FileOutputStream out = new FileOutputStream(f);
                                        bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                                        out.flush();
                                        out.close();
                                    } catch (FileNotFoundException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    showDialog("截图已保存：" + getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/snapshot-" + dateFormat + ".png");
                                }
                            });
                        } else if (id == R.id.data) {
                            isShowStatistics = !isShowStatistics;
                            ToastUtils.show((isShowStatistics ? "open" : "close") + " statistics info");
                        } else if (id == R.id.network_detect) {
                            if (pusher != null) {
                                boolean isSelect = mNetworkDetectButton.isSelected();
                                if (isSelect) {
                                    int result = pusher.stopLastMileDetect();
                                    sendLogToMessageView("[API] stopLastMileDetect: [end][" + result + "]");
                                } else {
                                    AlivcLiveNetworkQualityProbeConfig probeConfig = new AlivcLiveNetworkQualityProbeConfig();
                                    probeConfig.probeUpLink = true;
                                    probeConfig.probeDownLink = true;
                                    int result = pusher.startLastMileDetect(probeConfig);
                                    sendLogToMessageView("[API] startLastMileDetect: [end][" + result + "][" + probeConfig + "]");
                                }
                                mNetworkDetectButton.setSelected(!isSelect);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        showDialog(e.getMessage());
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        showDialog(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    AlivcLivePushInfoListener mPushInfoListener = new AlivcLivePushInfoListener() {
        @Override
        public void onPreviewStarted(AlivcLivePusher pusher) {
            //AlivcLivePusher pusher = mPushController.getLivePusher();
            // 添加水印
            for (int i = 0; i < waterMarkInfos.size(); i++) {
                pusher.addWaterMark(waterMarkInfos.get(i).mWaterMarkPath, waterMarkInfos.get(i).mWaterMarkCoordX, waterMarkInfos.get(i).mWaterMarkCoordY, waterMarkInfos.get(i).mWaterMarkWidth);
            }
            ToastUtils.show(getSafeString(R.string.start_preview));
            sendLogToMessageView("[CBK][INFO] onPreviewStarted");
        }

        @Override
        public void onPreviewStopped(AlivcLivePusher pusher) {
            if (isAdded()) {
                ToastUtils.show(getSafeString(R.string.stop_preview));
            }
            sendLogToMessageView("[CBK][INFO] onPreviewStopped");
        }

        @Override
        public void onPushStarted(AlivcLivePusher pusher) {
            isConnectResult = true;
            mIsStartAsnycPushing = false;
            if (isAdded()) {
                ToastUtils.show(getSafeString(R.string.start_push));
            }
            sendLogToMessageView("[CBK][INFO] onPushStarted");
        }

        @Override
        public void onFirstFramePushed(AlivcLivePusher pusher) {
            sendLogToMessageView("[CBK][INFO] onFirstFramePushed");
        }

        @Override
        public void onPushPaused(AlivcLivePusher pusher) {
            ToastUtils.show(getSafeString(R.string.pause_push));
            sendLogToMessageView("[CBK][INFO] onPushPaused");
        }

        @Override
        public void onPushResumed(AlivcLivePusher pusher) {
            ToastUtils.show(getSafeString(R.string.resume_push));
            sendLogToMessageView("[CBK][INFO] onPushResumed");
        }

        @Override
        public void onPushStopped(AlivcLivePusher pusher) {
            ToastUtils.show(getSafeString(R.string.stop_push));
            sendLogToMessageView("[CBK][INFO] onPushStopped");
        }

        /**
         * 推流重启通知
         *
         * @param pusher AlivcLivePusher实例
         */
        @Override
        public void onPushRestarted(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            ToastUtils.show(getSafeString(R.string.restart_success));
            sendLogToMessageView("[CBK][INFO] onPushRestarted");
        }

        @Override
        public void onFirstFramePreviewed(AlivcLivePusher pusher) {
            sendLogToMessageView("[CBK][INFO] onFirstFramePreviewed");
        }

        @Override
        public void onDropFrame(AlivcLivePusher pusher, int countBef, int countAft) {
            sendLogToMessageView("[CBK][INFO] onDropFrame: [" + countBef + "->" + countAft + "]");
        }

        @Override
        public void onAdjustBitrate(AlivcLivePusher pusher, int currentBitrate, int targetBitrate) {
            sendLogToMessageView("[CBK][INFO] onAdjustBitrate: [" + currentBitrate + "->" + targetBitrate + "]");
        }

        @Override
        public void onAdjustFps(AlivcLivePusher pusher, int currentFps, int targetFps) {
            sendLogToMessageView("[CBK][INFO] onAdjustFps: [" + currentFps + "->" + targetFps + "]");
        }

        @Override
        public void onPushStatistics(AlivcLivePusher pusher, AlivcLivePushStatsInfo statistics) {
            if (isShowStatistics) {
                sendLogToMessageView("[CBK][INFO] onPushStatistics: " + statistics);
            }
        }

        @Override
        public void onLocalRecordEvent(AlivcLiveRecordMediaEvent mediaEvent, String storagePath) {
            sendLogToMessageView("[CBK][INFO] onLocalRecordEvent: [" + mediaEvent + "][" + storagePath + "]");
        }

        @Override
        public void onRemoteUserEnterRoom(AlivcLivePusher pusher, String userId, boolean isOnline) {
            sendLogToMessageView("[CBK][INFO] onRemoteUserEnterRoom: [" + userId + "][" + isOnline + "]");
        }

        @Override
        public void onRemoteUserAudioStream(AlivcLivePusher pusher, String userId, boolean isPushing) {
            sendLogToMessageView("[CBK][INFO] onRemoteUserAudioStream: [" + userId + "][" + isPushing + "]");
        }

        @Override
        public void onRemoteUserVideoStream(AlivcLivePusher pusher, String userId, AlivcLivePlayVideoStreamType videoStreamType, boolean isPushing) {
            sendLogToMessageView("[CBK][INFO] onRemoteUserVideoStream: [" + userId + "][" + videoStreamType + "][" + isPushing + "]");
        }

        @Override
        public void onSetLiveMixTranscodingConfig(AlivcLivePusher pusher, boolean isSuccess, String msg) {
            sendLogToMessageView("[CBK][INFO] onSetLiveMixTranscodingConfig: [" + isSuccess + "][" + msg + "]");
        }

        @Override
        public void onKickedOutByServer(AlivcLivePusher pusher, AlivcLivePushKickedOutType kickedOutType) {
            sendLogToMessageView("[CBK][INFO] onKickedOutByServer: [" + kickedOutType + "]");
        }

        @Override
        public void onMicrophoneVolumeUpdate(AlivcLivePusher pusher, int volume) {
            super.onMicrophoneVolumeUpdate(pusher, volume);
        }

        @Override
        public void onAudioPublishStateChanged(AlivcLivePublishState oldState, AlivcLivePublishState newState) {
            sendLogToMessageView("[CBK][INFO] onAudioPublishStateChanged: [" + oldState + "->" + newState + "]");
        }

        @Override
        public void onVideoPublishStateChanged(AlivcLivePublishState oldState, AlivcLivePublishState newState) {
            sendLogToMessageView("[CBK][INFO] onVideoPublishStateChanged: [" + oldState + "->" + newState + "]");
        }

        @Override
        public void onScreenSharePublishStateChanged(AlivcLivePublishState oldState, AlivcLivePublishState newState) {
            sendLogToMessageView("[CBK][INFO] onScreenSharePublishStateChanged: [" + oldState + "->" + newState + "]");
        }

        @Override
        public void onLocalDualAudioStreamPushState(AlivcLivePusher pusher, boolean isPushing) {
            sendLogToMessageView("[CBK][INFO] onLocalDualAudioStreamPushState: [" + isPushing + "]");
        }
    };

    AlivcLivePushErrorListener mPushErrorListener = new AlivcLivePushErrorListener() {
        @Override
        public void onSystemError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            mIsStartAsnycPushing = false;
            showDialog(getSafeString(R.string.system_error) + error.toString());
            sendLogToMessageView("[CBK][ERROR] onSystemError: [" + error + "]");
        }

        @Override
        public void onSDKError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            mIsStartAsnycPushing = false;
            showDialog(getSafeString(R.string.sdk_error) + error.toString());
            sendLogToMessageView("[CBK][ERROR] onSDKError: [" + error + "]");
        }
    };

    AlivcLivePushNetworkListener mPushNetworkListener = new AlivcLivePushNetworkListener() {
        @Override
        public void onNetworkPoor(AlivcLivePusher pusher) {
            showNetWorkDialog(getSafeString(R.string.network_poor));
            sendLogToMessageView("[CBK][NETWORK] onNetworkPoor");
        }

        @Override
        public void onNetworkRecovery(AlivcLivePusher pusher) {
            ToastUtils.show(getSafeString(R.string.network_recovery));
            sendLogToMessageView("[CBK][NETWORK] onNetworkRecovery");
        }

        @Override
        public void onReconnectStart(AlivcLivePusher pusher) {
            ToastUtils.show(getSafeString(R.string.reconnect_start));
            sendLogToMessageView("[CBK][NETWORK] onReconnectStart");
        }

        @Override
        public void onReconnectFail(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showDialog(getSafeString(R.string.reconnect_fail));
            sendLogToMessageView("[CBK][NETWORK] onReconnectFail");
        }

        @Override
        public void onReconnectSucceed(AlivcLivePusher pusher) {
            ToastUtils.show(getSafeString(R.string.reconnect_success));
            sendLogToMessageView("[CBK][NETWORK] onReconnectSucceed");
        }

        @Override
        public void onSendDataTimeout(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showDialog(getSafeString(R.string.senddata_timeout));
            sendLogToMessageView("[CBK][NETWORK] onSendDataTimeout");
        }

        @Override
        public void onConnectFail(AlivcLivePusher pusher) {
            isConnectResult = true;
            mIsStartAsnycPushing = false;
            showDialog(getSafeString(R.string.connect_fail));
            sendLogToMessageView("[CBK][NETWORK] onConnectFail");
        }

        @Override
        public void onNetworkQualityChanged(AlivcLiveNetworkQuality upQuality, AlivcLiveNetworkQuality downQuality) {
            sendLogToMessageView("[CBK][NETWORK] onNetworkQualityChanged: [" + upQuality + "][" + downQuality + "]");
        }

        @Override
        public void onConnectionLost(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;

            // 断网后停止本地录制
            AlivcLivePusher livePusher = mPushController.getLivePusher();
            if (livePusher != null) {
                livePusher.stopLocalRecord();
            }

            ToastUtils.show(getSafeString(R.string.connection_lost));
            sendLogToMessageView("[CBK][NETWORK] onConnectionLost");
        }

        @Override
        public String onPushURLAuthenticationOverdue(AlivcLivePusher pusher) {
            sendLogToMessageView("[CBK][NETWORK] onPushURLAuthenticationOverdue");
            return "";
        }

        @Override
        public void onSendMessage(AlivcLivePusher pusher) {
            sendLogToMessageView("[CBK][NETWORK] onSendMessage: " + getSafeString(R.string.send_message));
        }

        @Override
        public void onPacketsLost(AlivcLivePusher pusher) {
            ToastUtils.show(getSafeString(R.string.packet_lost));
            sendLogToMessageView("[CBK][NETWORK] onPacketsLost");
        }

        @Override
        public void onLastMileDetectResultWithQuality(AlivcLivePusher pusher, AlivcLiveNetworkQuality networkQuality) {
            super.onLastMileDetectResultWithQuality(pusher, networkQuality);
            sendLogToMessageView("[CBK][NETWORK] onLastMileDetectResultWithQuality: [" + networkQuality + "]");
        }

        @Override
        public void onLastMileDetectResultWithBandWidth(AlivcLivePusher pusher, int code, AlivcLiveNetworkQualityProbeResult networkQualityProbeResult) {
            super.onLastMileDetectResultWithBandWidth(pusher, code, networkQualityProbeResult);
            sendLogToMessageView("[CBK][NETWORK] onLastMileDetectResultWithBandWidth: [" + code + "][" + networkQualityProbeResult + "]");
        }

        @Override
        public void onPushURLTokenWillExpire(AlivcLivePusher pusher) {
            super.onPushURLTokenWillExpire(pusher);
            sendLogToMessageView("[CBK][NETWORK] onPushURLTokenWillExpire");
        }

        @Override
        public void onPushURLTokenExpired(AlivcLivePusher pusher) {
            super.onPushURLTokenExpired(pusher);
            sendLogToMessageView("[CBK][NETWORK] onPushURLTokenExpired");
        }

        @Override
        public void onConnectionStatusChange(AlivcLivePusher pusher, AlivcLiveConnectionStatus connectionStatus, AlivcLiveConnectionStatusChangeReason connectionStatusChangeReason) {
            super.onConnectionStatusChange(pusher, connectionStatus, connectionStatusChangeReason);
            sendLogToMessageView("[CBK][NETWORK] onConnectionStatusChange: [" + connectionStatus + "][" + connectionStatusChangeReason + "]");
        }
    };

    private AlivcLivePushBGMListener mPushBGMListener = new AlivcLivePushBGMListener() {
        @Override
        public void onStarted() {
            sendLogToMessageView("[CBK][BGM] onStarted");
            mIsBGMPlaying = true;
        }

        @Override
        public void onStopped() {
            sendLogToMessageView("[CBK][BGM] onStopped");
            mIsBGMPlaying = false;
        }

        @Override
        public void onPaused() {
            sendLogToMessageView("[CBK][BGM] onPaused");
            mIsBGMPlaying = false;
        }

        @Override
        public void onResumed() {
            sendLogToMessageView("[CBK][BGM] onResumed");
            mIsBGMPlaying = true;
        }

        @Override
        public void onProgress(final long progress, final long duration) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMusicDialog != null) {
                        mMusicDialog.updateProgress(progress, duration);
                    }
                }
            });
        }

        @Override
        public void onCompleted() {
            sendLogToMessageView("[CBK][BGM] onCompleted");
            mIsBGMPlaying = false;
        }

        @Override
        public void onDownloadTimeout() {
            sendLogToMessageView("[CBK][BGM] onDownloadTimeout");
            mIsBGMPlaying = false;
        }

        @Override
        public void onOpenFailed() {
            sendLogToMessageView("[CBK][BGM] onOpenFailed");
            showDialog(getSafeString(R.string.bgm_open_failed));
            mIsBGMPlaying = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMusicDialog != null && mMusicDialog.isShowing()) {
            mMusicDialog.dismiss();
            mMusicDialog = null;
        }
        if (mExecutorService != null && !mExecutorService.isShutdown()) {
            mExecutorService.shutdown();
        }
        destroyBeautyManager();
    }

    private void showDialog(final String message) {
        if (getActivity() == null || message == null) {
            return;
        }
        if (mDialog == null || !mDialog.isShowing()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        mDialog = new CommonDialog(getActivity());
                        mDialog.setDialogTitle(getSafeString(R.string.dialog_title));
                        mDialog.setDialogContent(message);
                        mDialog.setConfirmButton(TextFormatUtil.getTextFormat(getActivity(), R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        mDialog.show();
                    }
                }
            });
        }
    }

    private void showNetWorkDialog(final String message) {
        if (getActivity() == null || message == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(getSafeString(R.string.dialog_title));
                    dialog.setMessage(message);
                    dialog.setNegativeButton(getSafeString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.setNeutralButton(getSafeString(R.string.reconnect), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AlivcLivePusher pusher = mPushController.getLivePusher();
                            try {
                                pusher.reconnectPushAsync(null);
                            } catch (IllegalStateException e) {

                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
    }


    private String getMD5(String string) {

        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    private String getUri(String url) {
        String result = "";
        String temp = url.substring(7);
        if (temp != null && !temp.isEmpty()) {
            result = temp.substring(temp.indexOf("/"));
        }
        return result;
    }


    public final String getSafeString(@StringRes int resId) {
        Context context = getContext();
        if (context != null) {
            return getResources().getString(resId);
        } else {
            return "";
        }
    }

    private String getAuthString(String time) {
        if (!time.isEmpty() && !mPrivacyKey.isEmpty()) {
            long tempTime = (System.currentTimeMillis() + Integer.valueOf(time)) / 1000;
            String tempprivacyKey = String.format(mMd5String, getUri(mPushUrl), tempTime, 0, 0, mPrivacyKey);
            String auth = String.format(mAuthString, tempTime, 0, 0, getMD5(tempprivacyKey));
            mTempUrl = mPushUrl + auth;
        } else {
            mTempUrl = mPushUrl;
        }
        return mTempUrl;
    }

    public boolean isBGMPlaying() {
        return mIsBGMPlaying;
    }

    // 初始化美颜
    private void initBeautyManager() {
        if (mBeautyManager == null) {
            Log.d(TAG, "initBeautyManager start");
            // v4.4.4版本-v6.1.0版本，互动模式下的美颜，处理逻辑参考BeautySDKType.INTERACT_QUEEN，即：InteractQueenBeautyImpl；
            // v6.1.0以后的版本（从v6.2.0开始），基础模式下的美颜，和互动模式下的美颜，处理逻辑保持一致，即：QueenBeautyImpl；
            mBeautyManager = BeautyFactory.createBeauty(BeautySDKType.QUEEN, LivePushFragment.this.getActivity());
            // initialize in texture thread.
            mBeautyManager.init();
            mBeautyManager.setBeautyEnable(isBeautyEnable);
            mBeautyManager.switchCameraId(mCameraId);
            Log.d(TAG, "initBeautyManager end");
        }
    }

    // 销毁美颜
    private void destroyBeautyManager() {
        if (mBeautyManager != null) {
            mBeautyManager.release();
            mBeautyManager = null;
        }
    }

    private void changeBeautyContainerVisibility() {
        if (mBeautyBeautyContainerView.getVisibility() == View.VISIBLE) {
            mActionBar.setVisibility(View.VISIBLE);
            mQueenMenuPanel.onHideMenu();
            mBeautyBeautyContainerView.setVisibility(View.GONE);
        } else {
            mActionBar.setVisibility(View.GONE);
            mQueenMenuPanel.onShowMenu();
            mBeautyBeautyContainerView.setVisibility(View.VISIBLE);
        }
    }

    private void changeSoundEffectVisibility() {
        if (mSoundEffectView.getVisibility() == View.VISIBLE) {
            mActionBar.setVisibility(View.VISIBLE);
            mSoundEffectView.setVisibility(View.GONE);
        } else {
            mActionBar.setVisibility(View.GONE);
            mSoundEffectView.setVisibility(View.VISIBLE);
        }
    }

    public void updateOperaButtonState(boolean value) {
        if (mOperaButton != null) {
            mOperaButton.post(new Runnable() {
                @Override
                public void run() {
                    if (mOperaButton != null) {
                        mOperaButton.setText(value ? getSafeString(R.string.pause_button) : getSafeString(R.string.resume_button));
                        mOperaButton.setSelected(!value);
                    }
                }
            });
        }
        if (mPreviewButton != null) {
            mPreviewButton.post(new Runnable() {
                @Override
                public void run() {
                    if (mPreviewButton != null) {
                        mPreviewButton.setText(value ? getSafeString(R.string.stop_preview_button) : getSafeString(R.string.start_preview_button));
                        mPreviewButton.setSelected(value);
                    }
                }
            });
        }
    }

    // 发送日志到消息视图
    private void sendLogToMessageView(String message) {
        if (mMessagesView != null) {
            mMessagesView.post(new Runnable() {
                @Override
                public void run() {
                    if (mMessagesView != null) {
                        mMessagesView.appendMessage(message);
                    }
                }
            });
        }
    }
}
