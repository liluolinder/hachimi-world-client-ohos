import { media } from "@kit.MediaKit";
import { BusinessError } from "@kit.BasicServicesKit";
import { AVSessionController } from "./AVSessionController";
import { BackgroundUtil } from "./BackgroundRunningManager";
import {  __User__ } from "../../commons/shareDatas";
import { I_GetRecentSongRes_data_song } from "../interfaces/I_GetRecentSong";
import { __Client__ } from "./hachimi";

@ObservedV2
export class musicPlayer {
  avPlayer?: media.AVPlayer
  @Trace durationTime: number = 0
  @Trace currentTime: number = 0
  @Trace isPlaying: boolean = false

  @Trace playNow?:I_GetRecentSongRes_data_song
  @Trace playIndex:number = -1
  @Trace playList:I_GetRecentSongRes_data_song[]=[]

  private avSessionController: AVSessionController = new AVSessionController()

  @Monitor('playIndex')
  changePlayNow(){
    this.playNow = this.playList[this.playIndex]
    if (__musicPlayer__) {
      __musicPlayer__.init( this.playList[this.playIndex].audio_url)
      __musicPlayer__.Play()
    }
    if (__User__.isLogin) {
      __Client__.updateHistory(this.playList[this.playIndex].id)
    }else {
      __Client__.updateHistoryNoLogin(this.playList[this.playIndex].id)
    }
  }

  @Monitor('isPlaying')
  startOrStopContinuesTasks(){
    this.avSessionController?.setPlayState(this.isPlaying)
  }

  setAVPlayerCallback() {
    if (!this.avPlayer) {
      return
    }
    // error回调监听函数,当avPlayer在操作过程中出现错误时调用 reset接口触发重置流程
    this.avPlayer.on('error', (err) => {
      console.error(`Invoke avPlayer failed, code is ${err.code}, message is ${err.message}`);
      this.avPlayer?.reset(); // 调用reset重置资源，触发idle状态
    })
    this.avPlayer.on('stateChange', async (state: string) => {
      switch (state) {
        case 'idle': // 成功调用reset接口后触发该状态机上报
          this.Release()
          this.isPlaying = false
          break;
        case 'initialized': // avplayer 设置播放源后触发该状态上报
          this.Prepare()
          this.isPlaying = false
          break;
        case 'prepared': // prepare调用成功后上报该状态机
          this.currentTime = this.avPlayer?.currentTime! // 播放的时间进度
          this.durationTime = this.avPlayer?.duration! //  歌曲总共时间
          this.avSessionController?.setAVMetadata();
          this.isPlaying = false
          this.Play()
          break;
        case 'playing':
          this.isPlaying = true //  开始播放
          break;
        case 'paused':
          this.isPlaying = false //未开始播放
          this.Pause()
          break;
        case 'stopped':
          this.Reset()
          this.isPlaying = false
          break;
        case 'released':
          this.isPlaying = false
          break;
        case 'completed':
          this.Next()
          break;
      }
    })
    this.avPlayer.on('durationUpdate', (duration: number) => {
      this.durationTime = duration
    });
    this.avPlayer.on('timeUpdate', (time:number) => {
     this.currentTime = time
      this.avSessionController?.setProgressState(time)
    });
  }

  async init(url: string) {
    this.Release()
    this.avPlayer = await media.createAVPlayer()
    this.setAVPlayerCallback()
    this.avPlayer.url = url
    this.avSessionController?.setAVMetadata();
  }

  SetSpeed(speed: number): void {
    if (this.avPlayer) {
      try {
        this.avPlayer.setSpeed(speed);
        console.info(`avSetSpeed enum ${speed}`);
      } catch (e) {
        console.error(`avSetSpeed == ${JSON.stringify(e)}`);
      }
    }
  }

  Prepare() {
    if (this.avPlayer) {
      this.avPlayer.prepare((err: BusinessError) => {
        if (err) {
          console.error('Failed to prepare,error message is :' + err.message);
        } else {
          console.info('Succeeded in preparing');
        }
      });
    }
  }

  Stop() {
    if (this.avPlayer) {
      this.avPlayer.stop((err: BusinessError) => {
        if (err) {
          console.error('Failed to stop,error message is :' + err.message);
        } else {
          console.info('Succeeded in stopping');
        }
      });
    }
  }

  Play() {
    if (this.avPlayer) {
      BackgroundUtil.startContinuousTask()
      this.avPlayer.play().then(() => {
        console.info('Succeeded in playing');
      }, (err: BusinessError) => {
        console.error('Failed to play,error message is :' + err.message);
      });
    }
  }

  Pause() {
    if (this.avPlayer) {
      this.avPlayer.pause((err: BusinessError) => {
        if (err) {
          console.error('Failed to pause,error message is :' + err.message);
        } else {
          console.info('Succeeded in pausing');
        }
      });
    }
  }

  Seek(seekTime: number, mode: media.SeekMode = media.SeekMode.SEEK_PREV_SYNC) {
    if (this.avPlayer) {
      this.avPlayer.seek(seekTime, mode);
    }
  }

  Release() {
    if (this.avPlayer) {
      this.avPlayer.release((err: BusinessError) => {
        if (err) {
          console.error('Failed to release,error message is :' + err.message);
        } else {
          console.info('Succeeded in releasing');
        }
      });
      BackgroundUtil.stopContinuousTask()
    }
  }

  Reset() {
    if (this.avPlayer) {
      this.avPlayer.reset(async (err: BusinessError) => {
        if (err) {
          console.error('Failed to reset,error message is :' + err.message);
        } else {
          console.info('Succeeded in resetting');
        }
      });
    }
  }

  Next(){
    console.log(this.playIndex.toString())
    this.playIndex = this.playIndex == this.playList.length -1 ? 0:this.playIndex+1
    console.log(this.playIndex.toString())
  }

  Last(){
    this.playIndex = this.playIndex == 0 ? this.playList.length -1:this.playIndex-1
  }


}

export let __musicPlayer__: musicPlayer = new musicPlayer()