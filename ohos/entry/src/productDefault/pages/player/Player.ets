import { BasicDataSource, XAnimation, XPreferencesManager } from "commonpackage"
import { __UI__, __PlayerUIData__, __System__, min_player_landscape_width } from "../../../main/ets/commons/shareDatas"
import { liquidDuaPlayer } from "../../../main/ets/models/classes/liquidDuaPlayer"
import { client } from "../../../main/ets/models/classes/pilipala"
import { I_GetCommentRes_data_reply, I_GetCommentRes_data } from "../../../main/ets/models/interfaces/I_GetComment"
import { I_GetVideoInfo_data } from "../../../main/ets/models/interfaces/I_GetVideoInfo"
import { I_GetVideoPlayerUrl_data } from "../../../main/ets/models/interfaces/I_GetVideoPlayerUrl"
import { S_PlayerParam } from "../../../main/ets/models/interfaces/S_NavParams"
import { S_VideoQualityInfoList, S_VideoQualityItem } from "../../../main/ets/models/interfaces/S_QualityItem"
import { VideoInfo } from "./VideoInfo"
import { VideoPlayer } from "./VideoPlayer"
import json from "@ohos.util.json"
import { gestureShare } from "../../../main/ets/models/classes/ShareUtil"
import { continueManager } from "../../../main/ets/models/classes/ContinueManager"

@Builder
export function playerBuilder() {
  player()
}

@ComponentV2
export struct player {
  @Provider('videoInfo') videoInfo ?: I_GetVideoInfo_data
  @Provider('playerInfo') playerInfo ?: I_GetVideoPlayerUrl_data
  @Provider('commentArray') commentArray: BasicDataSource<I_GetCommentRes_data_reply> =
    new BasicDataSource<I_GetCommentRes_data_reply>()
  @Provider('isLandscapeLayout') isLandscapeLayout: boolean = false
  @Provider('hideVideoInfo') hideVideoInfo: boolean = false
  @Local isLoading: boolean = true
  videoHeight: number = 0
  @Local scrollOffset: number = 0
  @Provider('player') player: liquidDuaPlayer = new liquidDuaPlayer()

  @Provider('componentWidth') componentWidth: number = 1
  @Provider('componentHeight') componentHeight: number = 300

  @Provider('areaWidth') areaWidth: number = 0
  @Provider('areaHeight') areaHeight: number = 0

  @Provider('currentVideoPage') currentVideoPage ?: number = undefined
  @Provider('currentVideoQuality') currentVideoQuality: number = XPreferencesManager.getPreferencesValue("pilipala", "preferVideoQuality", -1) as number
  @Provider('videoQualityList') videoQualityList: S_VideoQualityItem[] = []

  @Local pageOpacity: number =  __UI__.isSplitLayout? 0 : 1

  scroller: Scroller = new Scroller()

  aboutToDisappear(): void {
    client.heartbeatUpdate({
      aid: this.videoInfo?.aid??0,
      cid: this.videoInfo?.pages.find(i => i.page == this.currentVideoPage)?.cid ?? this.videoInfo?.cid,
      play_type: 4,    //开始播放为1，结束播放为4, 播放中为0
      type: 3,         //普通视频设置为3， 动漫和电视剧设置为4
      played_time: Math.round(this.player.duration / 1000)   //播放完成设置为-1,其余为秒
    })
    this.player?.destroy()
  }


  back(force: boolean) {

    if (__PlayerUIData__.isFullScreen) {
      XAnimation.runWithAnimation(()=> __PlayerUIData__.isFullScreen = false)
      if(!force){
        return
      }
    }
    if (__UI__.mainPageStack.getAllPathName().length ==
      1) {
      XAnimation.runWithAnimation(() => __UI__.indexNavigationMode = NavigationMode.Stack)
    }
    __UI__.mainPageStack.pop()
  }

  aboutToAppear(): void {
    if (__UI__.isSplitLayout) XAnimation.runWithAnimation(() => __UI__.indexNavigationMode =
      NavigationMode.Split)
  }

  build() {
    NavDestination() {
      if (this.isLoading) {
        Stack() {
          LoadingProgress().size({ width: 100, height: 100 })
        }
        .width('100%').height('100%')
        .transition(TransitionEffect.OPACITY)
      } else {
        Scroll(this.scroller) {
          Flex({ direction: this.isLandscapeLayout ? FlexDirection.Row : FlexDirection.Column }) {
            VideoPlayer({
              back: () => {
                this.back(true)
              }
            })
              .onAreaChange((_, newValue) => this.videoHeight = new Number(newValue.height).valueOf())
              .width(this.isLandscapeLayout ?  this.hideVideoInfo || (__PlayerUIData__.isFullScreen && this.hideVideoInfo)? '100%' :'calc(100% - 300vp)' : '100%')
              .height(this.isLandscapeLayout ? '100%' : 'auto')
              .padding({ top: this.isLandscapeLayout || __PlayerUIData__.isFullScreen ? 0 : __System__.topRectHeight })
            if (!this.hideVideoInfo) {
              Column() {
                if (!this.isLandscapeLayout) {
                  Stack(){
                    Button({type: ButtonType.Normal, stateEffect: true}){
                      Row({space: 10}){
                        SymbolGlyph($r('sys.symbol.play_fill')).fontSize(23).fontColor([$r('sys.color.font')])
                        Text('继续观看').fontSize(23).fontColor($r('sys.color.font')).fontWeight(FontWeight.Medium)
                      }
                    }.backgroundColor(Color.Transparent).width('100%').height('100%')
                    .onClick(()=>{
                      this.scroller.scrollEdge(Edge.Top)
                    })
                  }.height((Math.max(10, __System__.topRectHeight) + 50) * this.scrollOffset / (this.componentHeight + (this.isLandscapeLayout ? 0 : __System__.topRectHeight)))
                    .padding({top: Math.max(10, __System__.topRectHeight) * this.scrollOffset / (this.componentHeight + (this.isLandscapeLayout ? 0 : __System__.topRectHeight))})
                  .opacity(this.scrollOffset / (this.componentHeight + (this.isLandscapeLayout ? 0 : Math.max(10, __System__.topRectHeight))) * 0.75)
                  .width('100%')
                }
                VideoInfo()
                  .layoutWeight(1)
                  .backgroundColor(__UI__.isSplitLayout? Color.Transparent: $r('app.color.start_window_background'))
                  .padding({ top: this.isLandscapeLayout ? Math.max(10, __System__.topRectHeight) : 10 })
                  .width(this.isLandscapeLayout ? 300 : '100%')
              }.height('100%')
              .transition(XAnimation.getAnimation(this.isLandscapeLayout? 'right': 'bottom', 0, false))

            }
          }.width('100%')
        }.scrollBar(BarState.Off)
        .onDidScroll((type, offset) => {
          this.scrollOffset += offset
        })
        .transition(TransitionEffect.OPACITY)
      }
    }
    .onShown(() => {
      if (__UI__.isSplitLayout) {
        animateToImmediately({ duration: 500, curve: Curve.Ease },
          () => this.pageOpacity = 1)
      }
      if(this.videoInfo){
        gestureShare.on({
          url: 'https://www.bilibili.com/video/' + this.videoInfo?.bvid,
          imageUrl: this.videoInfo?.pic,
          title: this.videoInfo?.title,
          description: this.videoInfo?.owner.name
        })
        continueManager.on({
          type: 'video',
          bv: this.videoInfo.bvid,
          cid: this.videoInfo.cid
        })
      }
    })
    .onWillHide(() => {
      this.player.pause()
      gestureShare.off()
      continueManager.off()
      if (__UI__.isSplitLayout) {
        animateToImmediately({ duration: 500, curve: Curve.Ease },
          () => this.pageOpacity = 0)
      }
    })
    .opacity(this.pageOpacity)
    .onAreaChange((_, newValue) => {
      XAnimation.runWithAnimation(() => this.isLandscapeLayout = new Number(newValue.width).valueOf() > min_player_landscape_width,
        this.areaWidth > 0)
      if (!this.isLandscapeLayout && !__PlayerUIData__.isFullScreen) {
        XAnimation.runWithAnimation(() => this.hideVideoInfo = false)
      }
      this.areaWidth = new Number(newValue.width).valueOf()
      this.areaHeight = new Number(newValue.height).valueOf()
    })
    .onBackPressed(() => {
      this.back(false)
      return true
    })
    .hideTitleBar(true)
    .width('100%')
    .height('100%')
    .backgroundColor(__UI__.isSplitLayout? Color.Transparent: $r('app.color.start_window_background'))
    .onReady(async (context: NavDestinationContext) => {
      let param: S_PlayerParam = context.pathInfo.param as S_PlayerParam
      this.currentVideoPage = param.page
      this.videoInfo = await client.getVideoInfo(param.bvid) ?? undefined
      this.playerInfo = await client.getVideoPlayerUrl(param.bvid, param.cid.toString()) ?? undefined
      client.heartbeatUpdate({
        aid: this.videoInfo?.aid??0,
        cid: this.videoInfo?.pages.find(i => i.page == this.currentVideoPage)?.cid ?? this.videoInfo?.cid,
        play_type: 1,    //开始播放为1，结束播放为4, 播放中为0
        type: 3,         //普通视频设置为3， 动漫和电视剧设置为4
        played_time: Math.round((param.startDuration ?? 0) / 1000)  //播放完成设置为-1,其余为秒
      })
      this.playerInfo!.dash.video.forEach((item)=>{
        if(!item.codecs.includes('av01') && !this.videoQualityList.find(i => i.id == item.id)){
          this.videoQualityList.push({ name: S_VideoQualityInfoList.find(i => i.id == item.id)?.name ?? '未知', id: item.id, codec: item.codecs, url: item.baseUrl})
        }
      })
      XAnimation.runWithAnimation(() => this.isLoading = false)
      await this.player.init()
      this.currentVideoQuality = this.videoQualityList.find(i => i.id == this.currentVideoQuality)?.id ?? this.playerInfo!.dash.video[0].id
      this.player.setUrlAndHead(this.videoQualityList.find(i => i.id == this.currentVideoQuality)?.url ?? '', this.playerInfo!.dash.audio[0].baseUrl, param.startDuration)
      console.log('testTAG', JSON.stringify(this.playerInfo!.dash.video))
      console.log('testTAG', JSON.stringify(this.playerInfo!.dash.audio))

      client.getComment({
        type: 1,
        sort: 1,
        oid: this.videoInfo!.aid,
        pn: 0
      }).then((res?: I_GetCommentRes_data | null) => {
        if (res && res != null) {
          if (res.replies != null) {

            res.replies.forEach((item)=>{
              this.commentArray?.pushData(item)
            })
            console.log(`${json.stringify(res.replies)}`)
          }
        }
      })
      gestureShare.on({
        url: 'https://www.bilibili.com/video/' + this.videoInfo?.bvid,
        imageUrl: this.videoInfo?.pic,
        title: this.videoInfo?.title,
        description: this.videoInfo?.owner.name
      })
      continueManager.on({
        type: 'video',
        bv: this.videoInfo?.bvid,
        cid: this.videoInfo?.cid
      })
    })
  }
}



