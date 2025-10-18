import { StringFormat, XAnimation, XContext, XPreferencesManager } from 'commonpackage'
import { PlayerUIData, __PlayerUIData__, __UI__, __System__, min_width } from '../../../main/ets/commons/shareDatas'
import { liquidDuaPlayer } from '../../../main/ets/models/classes/liquidDuaPlayer'
import WindowsUtil from '../../../main/ets/models/classes/WindowsUtil'
import { I_GetVideoInfo_data } from '../../../main/ets/models/interfaces/I_GetVideoInfo'
import { I_GetVideoPlayerUrl_data } from '../../../main/ets/models/interfaces/I_GetVideoPlayerUrl'
import { S_VideoQualityItem } from '../../../main/ets/models/interfaces/S_QualityItem'
import { media } from '@kit.MediaKit'
import { continueManager } from '../../../main/ets/models/classes/ContinueManager'
import { client } from '../../../main/ets/models/classes/pilipala'
import { PlayingMenuItem } from '../../components/PlayingMenuItem'
import { S_PlaybackSpeed, S_PlayBackSpeedList } from '../../../main/ets/models/interfaces/S_PlaybackSpeed'

@ComponentV2
export struct VideoPlayer {
  @Local playerUIData: PlayerUIData = __PlayerUIData__
  @Consumer('videoInfo') videoInfo ?: I_GetVideoInfo_data
  @Consumer('playerInfo') playerInfo ?: I_GetVideoPlayerUrl_data
  @Consumer('isLandscapeLayout') isLandscapeLayout: boolean = false
  @Consumer('hideVideoInfo') hideVideoInfo: boolean = false
  @Local displayControlButtons: boolean = true
  xComponentController: XComponentController = new XComponentController()
  @Event back: () => void
  audioPrepared: boolean = false
  videoPrepared: boolean = false

  @Consumer('player') player : liquidDuaPlayer = new liquidDuaPlayer()
  @Consumer('componentWidth') componentWidth: number = 1
  @Consumer('componentHeight') componentHeight: number = 300

  @Consumer('areaWidth') areaWidth: number = 0
  @Consumer('areaHeight') areaHeight: number = 0

  @Consumer('currentVideoQuality') currentVideoQuality: number = -1
  @Consumer('videoQualityList') videoQualityList: S_VideoQualityItem[] = []
  @Consumer('currentVideoPage') currentVideoPage : number = 0

  clickTimeout: number = 0
  isClicked: boolean = false
  isPressed: boolean = false
  pressedTimeout: number = 0
  swipingDirection: 'horizon'|'vertical'|'none' = 'none'
  @Local isLongPressed: boolean = false
  touchStartX: number = 0
  touchStartY: number = 0
  @Local swipeHorizonDistance ?:number = undefined

  @Local sliderValue: number = 0
  sliderDragging: boolean = false

  heartbeatInterval: number = 0

  @Local displayPlayMenu: boolean = false

  aboutToDisappear(): void {
    clearInterval(this.heartbeatInterval)
  }

  aboutToAppear(): void {
    this.heartbeatInterval = setInterval(()=>{
      if(this.player.playing){
        client.heartbeatUpdate({
          aid: this.videoInfo?.aid??0,
          cid: this.videoInfo?.pages.find(i => i.page == this.currentVideoPage)?.cid ?? this.videoInfo?.cid,
          play_type: 0,    //开始播放为1，结束播放为4, 播放中为0
          type: 3,         //普通视频设置为3， 动漫和电视剧设置为4
          played_time: Math.round((this.player?.duration) / 1000)  //播放完成设置为-1,其余为秒
        })
      }
    }, 30000)
  }

  @Monitor('player.isFinish')
  onFinish(){
    if(this.player.isFinish){
      client.heartbeatUpdate({
        aid: this.videoInfo?.aid??0,
        cid: this.videoInfo?.pages.find(i => i.page == this.currentVideoPage)?.cid ?? this.videoInfo?.cid,
        play_type: 0,    //开始播放为1，结束播放为4, 播放中为0
        type: 3,         //普通视频设置为3， 动漫和电视剧设置为4
        played_time: -1 //播放完成设置为-1,其余为秒
      })
    }
  }


  @Monitor('isLandscapeLayout','playerUIData.isFullScreen','areaWidth','areaHeight', 'player.videoWidth', 'player.videoHeight', 'hideVideoInfo')
  calcSize() {
    const areaWidth = this.areaWidth - (this.isLandscapeLayout && !__PlayerUIData__.isFullScreen && !this.hideVideoInfo? 300 : 0)
    const areaHeight = this.isLandscapeLayout || __PlayerUIData__.isFullScreen ? this.areaHeight : this.areaHeight * 0.7
    const aspectRatioNow = this.player.videoWidth / this.player.videoHeight;
    const screenAspectRatio = areaWidth / areaHeight;
    XAnimation.runWithAnimation(()=>{
      if (aspectRatioNow > screenAspectRatio) {
        this.componentWidth = areaWidth
        this.componentHeight = areaWidth / aspectRatioNow
      } else {
        this.componentHeight = areaHeight;
        this.componentWidth = areaHeight * aspectRatioNow
      }
    })
  }

  @Monitor('player.duration')
  updateSliderValue(){
    continueManager.setVideoDuration(this.player.duration)
    if(this.sliderDragging) return
    this.sliderValue = this.player.duration
  }

  @Monitor('playerUIData.isFullScreen')
  changeFullScreen() {
    this.hideVideoInfo = false
    if (__PlayerUIData__.isFullScreen) {
      WindowsUtil.setBarState(false, false)
      XAnimation.runWithAnimation(() => {
        __UI__.indexNavigationMode = NavigationMode.Stack
        this.displayControlButtons = false
        this.hideVideoInfo = true
      })
      WindowsUtil.windowClass?.maximize()
    } else {
      WindowsUtil.setBarState(true, true)
      XAnimation.runWithAnimation(() => {
        if (__UI__.isSplitLayout) {
          __UI__.indexNavigationMode = NavigationMode.Split
        }
        this.hideVideoInfo = false
        this.displayControlButtons = true
      })
    }
    WindowsUtil.setKeepScreenOn(__PlayerUIData__.isFullScreen)
  }

  build() {
    RelativeContainer() {

      Stack() {
        XComponent({ type: XComponentType.SURFACE, controller: this.xComponentController })
          .onLoad(() => {
            this.player?.setSurfaceId(this.xComponentController.getXComponentSurfaceId())
          })
          .width(this.componentWidth)
          .height(this.componentHeight)

        if(this.player.isBuffing){
          Stack(){
            LoadingProgress().width(50).height(50).color(Color.White)
          }.borderRadius(20).backgroundColor('#90000000').size({width: 75, height: 75})
        }

        if(this.player.isFinish){
          Button({type: ButtonType.Normal, stateEffect: true}){
            SymbolGlyph($r('sys.symbol.arrow_clockwise')).fontSize(35).fontColor([Color.White])
          }.borderRadius(20).backgroundColor('#90000000').size({width: 75, height: 75})
          .onClick(()=>{
            this.player.setUrlAndHead(this.videoQualityList.find(i => i.id == this.currentVideoQuality)?.url ?? '', this.playerInfo!.dash.audio[0].baseUrl)
          })
        }

        if(this.isLongPressed){
          Text().borderRadius(10).padding(10).backgroundColor('#90000000').fontSize(22).fontColor(Color.White)
        }

        if(this.isLongPressed){
          Text('3x快进中').borderRadius(10).padding(10).backgroundColor('#90000000').fontSize(22).fontColor(Color.White)
        }

        if(this.swipeHorizonDistance){
          Text(`${this.swipeHorizonDistance > 0? '+': ''}${this.swipeHorizonDistance}s`).borderRadius(10).padding(10).backgroundColor('#90000000').fontSize(22).fontColor(Color.White)
        }

      }.width('100%')
      .height('100%')
      .onTouch((event)=>{
        if(event.type == TouchType.Down){
          this.isPressed = true
          this.touchStartX = event.touches[0].x
          this.touchStartY = event.touches[0].y
          setTimeout(()=>{
            if(this.isPressed || this.swipingDirection != 'none') return
            if(this.isClicked){
              const areaWidth = this.areaWidth - (this.isLandscapeLayout && !__PlayerUIData__.isFullScreen && !this.hideVideoInfo? 300 : 0)
              XAnimation.runWithAnimation(() => this.displayControlButtons = false, true, 0.4, 1)
              if(event.touches[0].x < areaWidth * 0.25){
                this.player.seek(Math.max(0, this.player.duration - 10000))
                XContext.toast('快退10秒')
              }else if(event.touches[0].x >= areaWidth * 0.25 && event.touches[0].x <= areaWidth * 0.75){
                if(this.player.playing){
                  XAnimation.runWithAnimation(() => this.displayControlButtons = true, true, 0.4, 1)
                  this.player.pause()
                }else{
                  this.player.play()
                }
              }else if(event.touches[0].x > areaWidth * 0.75){
                this.player.seek(Math.min(this.player.totalTime, this.player.duration + 10000))
                XContext.toast('快进10秒')
              }
              clearTimeout(this.clickTimeout)
              this.isClicked = false
            }else {
              XAnimation.runWithAnimation(() => this.displayControlButtons = !this.displayControlButtons, true, 0.4, 1)
            }
            this.isClicked = true
            clearTimeout(this.clickTimeout)
            this.clickTimeout = setTimeout(()=>{
              this.isClicked = false
            }, 300)
          }, 100)

          clearTimeout(this.pressedTimeout)
          this.pressedTimeout = setTimeout(()=>{
            if(this.isPressed && this.swipingDirection == 'none'){
              this.isLongPressed = true
              this.player.setSpeed(media.PlaybackSpeed.SPEED_FORWARD_3_00_X)
            }
          }, 500)
        }
        if(event.type == TouchType.Move){
          const touchCurrentX = event.touches[0].x
          const touchCurrentY = event.touches[0].y
          if(this.isLongPressed) return
          if(this.swipingDirection == 'none' && Math.abs(this.touchStartX - touchCurrentX) > 30){
            this.touchStartX = touchCurrentX
            this.swipingDirection = 'horizon'
          }
          if(this.swipingDirection == 'none' && Math.abs(this.touchStartY - touchCurrentY) > 30){
            this.touchStartY = touchCurrentY
            this.swipingDirection = 'vertical'
          }
          if(this.swipingDirection == 'horizon'){
            this.swipeHorizonDistance = Math.round((touchCurrentX - this.touchStartX) / 25)
          }
        }
        if(event.type == TouchType.Up || event.type == TouchType.Cancel){
          this.swipingDirection = 'none'
          if(this.swipeHorizonDistance){
            this.player.seek(Math.min(this.player.totalTime, Math.max(0, this.player.duration + this.swipeHorizonDistance * 1000)))
            this.swipeHorizonDistance = undefined
          }
          clearTimeout(this.pressedTimeout)
          this.isPressed = false
          if(this.isLongPressed){
            this.isLongPressed = false
            this.player.setSpeed(media.PlaybackSpeed.SPEED_FORWARD_1_00_X)
          }
        }
      })


      if (this.displayControlButtons) {

        Row({ space: 10 }) {
          Row({ space: 8 }) {
            Button({ type: ButtonType.Circle, stateEffect: true }) {
              SymbolGlyph($r('sys.symbol.chevron_left')).fontSize(26).fontColor([Color.White])
            }.size({ width: 38, height: 38 })
            .backgroundColor(Color.Transparent)
            .onClick(() => {
              this.back()
            })

            Text(this.videoInfo?.title)
              .fontSize(20)
              .fontColor(Color.White)
              .textOverflow({ overflow: TextOverflow.MARQUEE })
              .maxLines(1)
              .layoutWeight(1)
          }.layoutWeight(1)

          Row({ space: 8 }) {
            if (this.hideVideoInfo && this.isLandscapeLayout) {
              Button({ type: ButtonType.Circle, stateEffect: true }) {
                SymbolGlyph($r('sys.symbol.open_sidebar')).fontSize(26).fontColor([Color.White])
              }.size({ width: 38, height: 38 })
              .backgroundColor(Color.Transparent)
              .onClick(() => {
                XAnimation.runWithAnimation(() => this.hideVideoInfo = false)
              })
            }
            if(this.areaWidth < min_width){
              Button({ type: ButtonType.Circle, stateEffect: true }) {
                SymbolGlyph($r('sys.symbol.dot_grid_2x2')).fontSize(26).fontColor([Color.White])
              }.size({ width: 38, height: 38 })
              .backgroundColor(Color.Transparent)
              .onClick(() => {
                this.displayPlayMenu = true
              })
              .bindSheet(this.displayPlayMenu!!, this.portraitMenu(),
                {
                  title: {title: '菜单'},
                  preferType: SheetType.CENTER,
                  detents: [SheetSize.FIT_CONTENT, SheetSize.LARGE]
                })
            }
          }
        }
        .opacity(0.9)
        .justifyContent(FlexAlign.SpaceBetween)
        .padding({
          left: 16,
          right: 16,
          top: __PlayerUIData__.isFullScreen || !this.isLandscapeLayout ? 10 : Math.max(__System__.topRectHeight, 16)
        })
        .width('100%')
        .linearGradient({
          angle: 0,
          colors: [[Color.Transparent, 0], ['#90000000', 0.75]]
        })
        .alignRules({
          top: { anchor: "__container__", align: VerticalAlign.Top },
        })
        .transition(XAnimation.getAnimation('up', 0, false))

        Row() {
          Button({ type: ButtonType.Circle, stateEffect: true }) {
            SymbolGlyph(this.player.playing? $r('sys.symbol.pause_fill'): $r('sys.symbol.play_fill')).fontSize(26).fontColor([Color.White]).symbolEffect(new ReplaceSymbolEffect(EffectScope.WHOLE), true)
          }.size({ width: 38, height: 38 })
          .backgroundColor(Color.Transparent)
          .onClick(() => {
            if(this.player.playing){
              this.player.pause()
            }else{
              this.player.play()
            }
          })

          Slider({
            min: 0,
            value: this.sliderValue,
            max: this.player.totalTime
          })
            .onTouch((event)=>{
              if(event.type == TouchType.Down) this.sliderDragging = true
              if(event.type == TouchType.Up || event.type == TouchType.Cancel) setTimeout(()=> this.sliderDragging = false, 50)
            })
            .layoutWeight(1)
            .onChange((value, mode)=>{
              if(mode == SliderChangeMode.End || mode == SliderChangeMode.Click) this.player.seek(value)
            })

          Row({space: 10}){

            Text(`${StringFormat.formatSeconds(this.player.duration / 1000)}/${StringFormat.formatSeconds(this.player.totalTime / 1000)}`).fontColor(Color.White).fontSize(14).maxLines(1)

            if(this.areaWidth > min_width){
              Text(S_PlayBackSpeedList.find(i => i.level == this.player.speed)?.name).fontSize(14).maxLines(1).textOverflow({overflow: TextOverflow.Ellipsis}).fontColor(Color.White)
                .bindMenu(this.playBackSpeedMenu())

              Text(this.videoQualityList.find(i => i.id == this.currentVideoQuality)?.name).fontSize(14).maxLines(1).textOverflow({overflow: TextOverflow.Ellipsis}).fontColor(Color.White)
                .bindMenu(this.videoQualityMenu())
            }

            Button({ type: ButtonType.Circle, stateEffect: true }) {
              SymbolGlyph(this.playerUIData.isFullScreen? $r('sys.symbol.arrow_down_right_and_arrow_up_left'): $r('sys.symbol.full_screen')).fontSize(26).fontColor([Color.White])
            }.size({ width: 38, height: 38 })
            .backgroundColor(Color.Transparent)
            .onClick(() => {
              XAnimation.runWithAnimation(()=> __PlayerUIData__.isFullScreen = !__PlayerUIData__.isFullScreen)
            })
          }
        }
        .width('100%')
        .padding({
          left: 16,
          right: 16,
          bottom: __PlayerUIData__.isFullScreen || !this.isLandscapeLayout ? 10 : Math.max(__System__.bottomRectHeight, 10)
        })
        .justifyContent(FlexAlign.SpaceBetween)
        .linearGradient({
          angle: 0,
          colors: [['#90000000', 0], [Color.Transparent, 0.75]]
        })
        .alignRules({
          bottom: { anchor: "__container__", align: VerticalAlign.Bottom },
        })
        .transition(XAnimation.getAnimation('bottom', 0, false))

      }
    }.width('100%')
    .height(this.isLandscapeLayout || __PlayerUIData__.isFullScreen ? '100%' : this.componentHeight)
    .backgroundColor(Color.Black)
    .clip(true)
  }

  @Builder
  videoQualityMenu(){
    Menu(){
      ForEach(this.videoQualityList, (item: S_VideoQualityItem)=>{
        MenuItem({content: item.name})
          .onClick(()=>{
            this.currentVideoQuality = item.id
            this.player.setUrlAndHead(this.videoQualityList.find(i => i.id == this.currentVideoQuality)?.url ?? '', this.playerInfo!.dash.audio[0].baseUrl, this.player.duration)
          })
      })
    }
  }

  @Builder
  playBackSpeedMenu(){
    Menu(){
      ForEach(S_PlayBackSpeedList, (item: S_PlaybackSpeed)=>{
        MenuItem({content: item.name})
          .onClick(()=>{
            this.player.setSpeed(item.level)
          })
      })
    }
  }

  @Builder
  portraitMenu(){
    List({space: 10}){
      ListItem(){
        PlayingMenuItem({
          symbol: $r('sys.symbol.background_blurred'),
          message: '画质',
          currentSelected: this.videoQualityList.findIndex(i => i.id == this.currentVideoQuality),
          list: this.videoQualityList.map(i => i.name),
          onSelected: (index: number) =>{
            this.currentVideoQuality = this.videoQualityList[index].id
            this.player.setUrlAndHead(this.videoQualityList.find(i => i.id == this.currentVideoQuality)?.url ?? '', this.playerInfo!.dash.audio[0].baseUrl, this.player.duration)
            XPreferencesManager.putPreferencesValue("pilipala", "preferVideoQuality", this.currentVideoQuality)
          }
        })
      }

      ListItem(){
        PlayingMenuItem({
          symbol: $r('sys.symbol.timer'),
          message: '播放速度',
          currentSelected: S_PlayBackSpeedList.findIndex(i => i.level == this.player.speed),
          list: S_PlayBackSpeedList.map(i => i.name),
          onSelected: (index: number) =>{
            this.player.setSpeed(S_PlayBackSpeedList[index]?.level)
          }
        })
      }
    }.contentEndOffset(16)
    .chainAnimation(true)
    .fadingEdge(true)
    .scrollBar(BarState.Off)
    .margin({left: 16, right: 16})
    .layoutWeight(1)
  }
}