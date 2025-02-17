package com.alivc.live.interactive_live;

import android.content.Context;
import android.widget.FrameLayout;

import com.alivc.live.commonbiz.LocalStreamReader;
import com.alivc.live.commonbiz.ResourcesConst;
import com.alivc.live.interactive_common.InteractiveMode;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.interactive_common.listener.InteractLivePushPullListener;
import com.alivc.live.interactive_common.utils.LivePushGlobalConfig;
import com.alivc.live.pusher.AlivcResolutionEnum;

import java.io.File;

/**
 * 以主播身份进入多人连麦互动界面的 Controller
 */
public class MultiAnchorController {

    private final InteractLiveManager mInteractLiveManager;
    private final Context mContext;
    private final LocalStreamReader mLocalStreamReader;
    //主播预览 View
    private FrameLayout mAnchorRenderView;

    //主播推流地址
    private final InteractiveUserData mPushUserData;

    public MultiAnchorController(Context context, InteractiveUserData userData) {
        this.mContext = context;
        AlivcResolutionEnum resolution = LivePushGlobalConfig.mAlivcLivePushConfig.getResolution();
        int width = AlivcResolutionEnum.getResolutionWidth(resolution, LivePushGlobalConfig.mAlivcLivePushConfig.getLivePushMode());
        int height = AlivcResolutionEnum.getResolutionHeight(resolution, LivePushGlobalConfig.mAlivcLivePushConfig.getLivePushMode());
        mLocalStreamReader = new LocalStreamReader.Builder()
                .setVideoWith(width)
                .setVideoHeight(height)
                .setVideoStride(width)
                .setVideoSize(height * width * 3 / 2)
                .setVideoRotation(0)
                .setAudioSampleRate(44100)
                .setAudioChannel(1)
                .setAudioBufferSize(2048)
                .build();
        mPushUserData = userData;
        mInteractLiveManager = new InteractLiveManager();
        mInteractLiveManager.init(context, InteractiveMode.MULTI_INTERACTIVE);
    }

    /**
     * 设置主播预览 View
     *
     * @param frameLayout 主播预览 View
     */
    public void setAnchorRenderView(FrameLayout frameLayout) {
        this.mAnchorRenderView = frameLayout;
    }

    /**
     * 开始直播
     */
    public void startPush() {
        externAV();
        mInteractLiveManager.startPreviewAndPush(mPushUserData, mAnchorRenderView, true);
        mInteractLiveManager.addAnchorMixTranscodingConfig(mPushUserData);
    }

    public void startConnect(InteractiveUserData userData, FrameLayout frameLayout) {
        if (userData == null) {
            return;
        }
        mInteractLiveManager.setPullView(userData, frameLayout, false);
        mInteractLiveManager.startPullRTCStream(userData);
        mInteractLiveManager.addAudienceMixTranscodingConfig(userData, frameLayout);
    }

    public boolean isOnConnected(String key) {
        return mInteractLiveManager.isPulling(mInteractLiveManager.getUserDataByKey(key));
    }

    private void externAV() {
        if (LivePushGlobalConfig.mAlivcLivePushConfig.isExternMainStream()) {
            File yuvFile = ResourcesConst.localCaptureYUVFilePath(mContext);
            mLocalStreamReader.readYUVData(yuvFile, (buffer, pts, videoWidth, videoHeight, videoStride, videoSize, videoRotation) -> {
                mInteractLiveManager.inputStreamVideoData(buffer, videoWidth, videoHeight, videoStride, videoSize, pts, videoRotation);
            });
            File pcmFile = ResourcesConst.localCapturePCMFilePath(mContext);
            mLocalStreamReader.readPCMData(pcmFile, (buffer, length, pts, audioSampleRate, audioChannel) -> {
                mInteractLiveManager.inputStreamAudioData(buffer, length, audioSampleRate, audioChannel, pts);
            });
        }
    }

    /**
     * 结束连麦
     */
    public void stopConnect(String key) {
        InteractiveUserData userData = mInteractLiveManager.getUserDataByKey(key);
        mInteractLiveManager.stopPullRTCStream(userData);
        mInteractLiveManager.removeAudienceLiveMixTranscodingConfig(userData, mPushUserData != null ? mPushUserData.userId : "");
    }

    public void resume() {
        mInteractLiveManager.resumePush();
        mInteractLiveManager.resumePlayRTCStream();
    }

    public void pause() {
        mInteractLiveManager.pausePush();
        mInteractLiveManager.pausePlayRTCStream();
    }

    public void release() {
        mInteractLiveManager.release();
        mLocalStreamReader.stopYUV();
        mLocalStreamReader.stopPcm();
    }

    public void setMultiInteractLivePushPullListener(InteractLivePushPullListener listener) {
        mInteractLiveManager.setInteractLivePushPullListener(listener);
    }

    public void switchCamera() {
        mInteractLiveManager.switchCamera();
    }

    public void enableSpeakerPhone(boolean enable) {
        mInteractLiveManager.enableSpeakerPhone(enable);
    }

    public void setMute(boolean b) {
        mInteractLiveManager.setMute(b);
    }

    public void enableAudioCapture(boolean enable) {
        mInteractLiveManager.enableAudioCapture(enable);
    }

    public void muteLocalCamera(boolean muteLocalCamera) {
        mInteractLiveManager.muteLocalCamera(muteLocalCamera);
    }

    public void enableLocalCamera(boolean enable) {
        mInteractLiveManager.enableLocalCamera(enable);
    }
}
