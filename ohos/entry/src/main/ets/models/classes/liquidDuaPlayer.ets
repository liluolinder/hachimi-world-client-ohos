import { media } from '@kit.MediaKit';
import { XContext } from 'commonpackage';
import { USER_AGENT } from '../../commons/commonItem';


@ObservedV2
export class liquidDuaPlayer {
  videoPlayer?: media.AVPlayer
  audioPlayer?: media.AVPlayer

  @Trace duration: number = 0
  @Trace playing: boolean = false
  @Trace totalTime: number = 1

  @Trace videoWidth: number = 0
  @Trace videoHeight: number = 0
  @Trace isBuffing: boolean = true
  @Trace isFinish: boolean = false
  @Trace speed: media.PlaybackSpeed = media.PlaybackSpeed.SPEED_FORWARD_1_00_X
  private surfaceID: string = ''

  private videoPrepared: boolean = false
  private audioPrepared: boolean = false

  private currentCommand ?: string

  private syncInterval: number = 0

  private offset ?: number

  async init() {
    this.videoPlayer = await media.createAVPlayer()
    this.audioPlayer = await media.createAVPlayer()
    this.videoPlayer.loop = true
    this.audioPlayer.loop = true
    this.setStateChangeListener()
    this.setTimeUpdateListener()
    this.setSeekDoneListener()
    this.setBufferingListener()
    this.setErrorListener()
    this.setSpeedDoneListener()
  }

  destroy(){
    this.videoPlayer?.release()
    this.audioPlayer?.release()
  }

  async setUrlAndHead(videoUrl:string, audioUrl:string, startOffset: number = 0){
    if (this.videoPlayer && this.audioPlayer && videoUrl.replaceAll(' ', '').length > 0 && audioUrl.replaceAll(' ', '').length > 0) {
      await this.videoPlayer.reset()
      await this.audioPlayer.reset()
      this.videoPrepared = false
      this.audioPrepared = false
      this.isBuffing = true
      this.offset = startOffset > 0? startOffset: undefined
      this.isFinish = false
      this.videoPlayer.setMediaSource(media.createMediaSourceWithUrl(videoUrl, {  'user-agent': USER_AGENT, "referer" : "https://www.bilibili.com" }), {preferredWidth: 1, preferredHeight: 2, preferredBufferDuration: 3, preferredHdr: false})
/*
      this.audioPlayer.setMediaSource(media.createMediaSourceWithUrl(audioUrl, {  'user-agent': USER_AGENT, "referer" : "https://www.bilibili.com" }), {preferredWidth: 1, preferredHeight: 2, preferredBufferDuration: 3, preferredHdr: false})
*/
      this.audioPlayer.url = audioUrl
    }
  }

  setSurfaceId (surfaceId:string){
    if (this.videoPlayer && surfaceId!='' && surfaceId != ' ') {
      this.surfaceID = surfaceId
      this.videoPlayer.surfaceId = surfaceId
    }

  }

  async play(){
    if(this.currentCommand) return
    if(this.videoPrepared && this.audioPrepared){
        // this.currentCommand = 'play'
        this.videoPlayer?.play()
        await this.audioPlayer?.play()
        this.audioPlayer?.seek(this.videoPlayer?.getPlaybackPosition())
    }
  }

  async pause(){
    clearInterval(this.syncInterval)
    await this.videoPlayer?.pause()
    await this.audioPlayer?.pause()
  }

  async seek(duration: number){
    if(this.currentCommand) return
    this.currentCommand = 'seek'
    await this.pause()
    this.videoPrepared = false
    this.audioPrepared = false
    this.videoPlayer?.seek(duration)
    this.audioPlayer?.seek(duration)
  }


  async setSpeed(speed: media.PlaybackSpeed|undefined){
    this.videoPlayer?.setSpeed(speed ?? media.PlaybackSpeed.SPEED_FORWARD_1_00_X)
    this.audioPlayer?.setSpeed(speed ?? media.PlaybackSpeed.SPEED_FORWARD_1_00_X)
  }

  private setSpeedDoneListener(){
    this.videoPlayer?.on('speedDone', (speed)=>{
      this.speed = speed
    })
  }

  private setStateChangeListener(){
    this.videoPlayer?.on('stateChange', async (state)=> {
      switch (state){
        case 'initialized':
          this.videoPlayer!.surfaceId = this.surfaceID
          this.videoPlayer?.prepare()
          break
        case 'prepared':
          this.videoPrepared = true
          this.videoWidth = this.videoPlayer!.width
          this.videoHeight = this.videoPlayer!.height
          this.totalTime = this.videoPlayer!.duration
          this.play()
        break
        case 'playing':
          setTimeout(()=>{
            if(this.offset) {
              this.seek(this.offset!)
              this.offset = undefined
            }
          }, 100)
          this.playing = true
        break
        case 'paused':
          this.playing = false
        break
      }
    })

    this.audioPlayer?.on('stateChange', async (state)=> {
      switch (state){
        case 'initialized':
          this.audioPlayer?.prepare()
          break
        case 'prepared':
          this.audioPrepared = true
          this.play()
        break
      }
    })
  }

  private setTimeUpdateListener(){
    this.videoPlayer?.on('timeUpdate', (duration)=>{
      this.duration = duration
      if(this.totalTime > 1000 && this.totalTime - duration < 1000){
        this.isFinish = true
        this.playing = false
        this.videoPlayer?.reset()
        this.audioPlayer?.reset()
      }
    })

  }

  private setSeekDoneListener(){
    this.audioPlayer?.on('seekDone', (duration)=>{
      this.audioPrepared = true
      const command = this.currentCommand
      if(this.videoPrepared && this.audioPrepared) this.currentCommand = undefined
      switch (command){
        case 'play':
          this.videoPlayer?.play()
          this.audioPlayer?.play()
          break
        case 'seek':
          this.play()
          break
      }

      this.setSync()
    })

    this.videoPlayer?.on('seekDone', (duration)=>{
      this.duration = duration
      this.videoPrepared = true
      const command = this.currentCommand
      if(this.videoPrepared && this.audioPrepared) this.currentCommand = undefined
      switch (command){
        case 'play':
          this.videoPlayer?.play()
          this.audioPlayer?.play()
          break
        case 'seek':
          this.play()
          break
      }
      this.setSync()
    })
  }

  private setSync(){
    clearInterval(this.syncInterval)
    this.syncInterval = setInterval(async ()=>{
      if(this.isBuffing) return
      if(!this.currentCommand && Math.abs(this.videoPlayer!.getPlaybackPosition() - this.audioPlayer!.getPlaybackPosition()) > 1000){
        await this.pause()
        this.play()
        clearInterval(this.syncInterval)
      }
    }, 500)
  }

  private setBufferingListener(){
    this.videoPlayer?.on('bufferingUpdate', (state)=>{
      switch (state){
        case media.BufferingInfoType.BUFFERING_START:
          this.isBuffing = true
          this.audioPlayer?.pause()
        break
        case media.BufferingInfoType.BUFFERING_END:
          this.isBuffing = false
          this.audioPlayer?.play()
          break
      }
    })
  }

  private setErrorListener(){
    this.videoPlayer?.on('error', (error)=>{
      if([5400102].includes(error.code)) return
      this.videoPlayer?.reset()
      this.audioPlayer?.reset()
      XContext.toast(`播放器发生错误！${error.code} ${error.name}${'\n'}${error.message}`)
      console.error('testTAG', error.code, error.name, error.message)
    })
  }
}