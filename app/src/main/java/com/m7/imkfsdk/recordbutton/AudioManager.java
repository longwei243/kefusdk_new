package com.m7.imkfsdk.recordbutton;

import com.moor.imkf.mp3recorder.MP3Recorder;

import java.io.File;
import java.util.UUID;

/**
 * 录音工具类
 */
public class AudioManager {

	private String mDir;
	private String mCurrentFilePath;
	
	private boolean isPrepared;
	
	private static AudioManager instance;
	
	private AudioStateListener listener;

	MP3Recorder mp3Recorder;
	
	public interface AudioStateListener{
		void wellPrepared();
	}
	
	public void setAudioStateListener(AudioStateListener listener) {
		this.listener = listener;
	}
	
	private AudioManager(){}
	
	private AudioManager(String dir){
		this.mDir = dir;
	}
	
	public static AudioManager getInstance(String dir) {
		if(instance == null) {
			synchronized (AudioManager.class) {
				instance = new AudioManager(dir);
			}
		}
		return instance;
	}
	
	public void prepareAudio() {
		try {
			isPrepared = false;
			File dir = new File(mDir);
			if(!dir.exists()) {
				dir.mkdirs();
				
			}

			String fileName = generateFileName();
			File file = new File(dir, fileName);
			mCurrentFilePath = file.getAbsolutePath();

			mp3Recorder = new MP3Recorder(file);
			mp3Recorder.start();

			if(listener != null) {
				listener.wellPrepared();
			}
			isPrepared = true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String generateFileName() {
		
		return UUID.randomUUID().toString() + ".mp3";
	}

	public int getVoiceLevel(int maxLevel) {
		if(isPrepared && mp3Recorder != null) {
			long a =  maxLevel * mp3Recorder.getVolume();
			long b = a / 1000;
			int lev = (int)b + 1;
			if(lev > maxLevel) {
				lev = maxLevel;
			}
			return lev;
		}
		return 1;
	}

	public void release() {
		if(mp3Recorder != null) {
			mp3Recorder.stop();
			mp3Recorder = null;
		}
	}

	public void cancel() {
		release();
		if(mCurrentFilePath != null) {
			File file = new File(mCurrentFilePath);
			file.delete();
			mCurrentFilePath = null;
		}
	}

	public String getCurrentFilePath() {
		return mCurrentFilePath;
	}
}
