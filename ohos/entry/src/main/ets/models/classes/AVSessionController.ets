import { avSession } from '@kit.AVSessionKit';
import { common, wantAgent } from '@kit.AbilityKit';
import { XContext } from 'commonpackage';
import { BusinessError } from '@kit.BasicServicesKit';
import { __Play__ } from '../../commons/shareDatas';
import { __musicPlayer__ } from './musicPlayer';

export class AVSessionController {
  private context: common.UIAbilityContext = XContext.getAbilityContext()
  private AVSession: avSession.AVSession | undefined = undefined;

  constructor() {
    this.initAVSession();
  }

  private async initAVSession() {

    if (!this.context) {
      console.info(`session create failed : conext is undefined`);
      return;
    }
    this.AVSession = await avSession.createAVSession(this.context, "PLAY_AUDIO", 'audio');
    await this.AVSession.activate();
    // [StartExclude avsession_controller]
    console.info(`session create successed : sessionId : ${this.AVSession.sessionId}`);
    this.setLaunchAbility();
    this.setListenerForMesFromController();
    await this.setAVMetadata()

  }

  async setListenerForMesFromController() {
    if (!this.AVSession) {
      return;
    }
    this.AVSession.on('play', this.onPlay);
    this.AVSession.on('pause', this.onPause);
    this.AVSession.on('playNext', this.onPlayNext);
    this.AVSession.on('playPrevious', this.onPlayPrevious);
    this.AVSession.on('seek', this.onSeek);
    //this.AVSession.on('setLoopMode', this.onSetLoopMode);
    //this.AVSession.on('toggleFavorite', this.onToggleFavorite);
  }

  async unregisterSessionListener() {
    if (!this.AVSession) {
      return;
    }
    this.AVSession.off('play');
    this.AVSession.off('pause');
    this.AVSession.off('playNext');
    this.AVSession.off('playPrevious');
    //this.AVSession.off('setLoopMode');
    this.AVSession.off('seek');
    //this.AVSession.off('toggleFavorite');
  }

  private setLaunchAbility() {
    if (!this.context) {
      return;
    }
    let wantAgentInfo: wantAgent.WantAgentInfo = {
      wants: [
        {
          bundleName: this.context.abilityInfo.bundleName,
          abilityName: this.context.abilityInfo.name
        }
      ],
      operationType: wantAgent.OperationType.START_ABILITIES,
      requestCode: 0,
      wantAgentFlags: [wantAgent.WantAgentFlags.UPDATE_PRESENT_FLAG]
    };
    wantAgent.getWantAgent(wantAgentInfo).then((agent) => {
      if (this.AVSession) {
        this.AVSession.setLaunchAbility(agent);
      }
    });
  }

  public async setAVMetadata() {
    try {
      if (this.context) {
        let metadata: avSession.AVMetadata = {
          assetId: __Play__.playList[__Play__.playIndex].id.toString(),
          title:  __Play__.playList[__Play__.playIndex]?.title,
          artist:  __Play__.playList[__Play__.playIndex]?.uploader_name??'',
          mediaImage:  __Play__.playList[__Play__.playIndex]?.cover_url,
          duration: __musicPlayer__.durationTime,
        };
       // let lrc = await MediaTools.getLrcFromRawFile(this.context, this.songList[this.musicIndex].lyric);
      
        if (this.AVSession) {
          this.AVSession.setAVMetadata(metadata).then(() => {
            console.info('SetAVMetadata successfully');
          }).catch((err: BusinessError) => {
            console.error(`SetAVMetadata BusinessError: code: ${err.code}, message: ${err.message}`);
          });
        }
      }
    } catch (error) {
      console.error(`SetAVMetadata try: code: ${(error as BusinessError).code}`);
    }
  }

  public setProgressState(ms: number) {
    if (this.AVSession) {
      this.AVSession.setAVPlaybackState({
        position: {
          elapsedTime: ms,
          updateTime: new Date().getTime()
        }
      }, (err: BusinessError) => {
        if (err) {
          console.error(`SetAVPlaybackState1 BusinessError: code: ${err.code}, message: ${err.message}`);
        } else {
          console.info('SetAVPlaybackState1 successfully');
        }
      });
    }
  }

  public setPlayState(isPlay: boolean) {
    if (this.AVSession) {
      this.AVSession.setAVPlaybackState({
        state: isPlay ? avSession.PlaybackState.PLAYBACK_STATE_PLAY : avSession.PlaybackState.PLAYBACK_STATE_PAUSE,
      }, (err: BusinessError) => {
        if (err) {
          console.error(`SetAVPlaybackState BusinessError: code: ${err.code}, message: ${err.message}`);
        } else {
          console.info('SetAVPlaybackState successfully');
        }
      });
    }
  }

  private  onPlay(){
    __musicPlayer__.Play()
  }

  private onPause(){
    __musicPlayer__.Pause()
  }

  private onPlayNext(){

  }

  private onPlayPrevious(){

  }
  private onSeek: (curMs: number) => void = (curMs: number) => {
    __musicPlayer__.Seek(curMs);
  }
  private onSetLoopMode(){

  }
  private onToggleFavorite(){

  }

}