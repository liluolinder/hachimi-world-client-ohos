import { XAnimation, XContext } from "commonpackage";
import { min_width } from "../../../main/ets/commons/commonItem";
import {  __SettingsUIData__, __System__, __UI__ } from "../../../main/ets/commons/shareDatas";
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer";
import { Home } from "../Home/Home";
import { playerBuilder } from "../player/Player";
import { PlayHistory } from "../PlayHistory/PlayHistory";

import { settingsPageBuilder } from "../settings/SettingsMainPage";


@ComponentV2
export struct Body{
  @Consumer('tabController') tabController: TabsController = new TabsController();
  @Consumer('mainPageStack') pageStack: NavPathStack = new NavPathStack()
  @Consumer('currentTabIndex') currentTabIndex: number = 0



  build() {
    NavDestination(){
      Stack(){
        Tabs({ controller: this.tabController, index: $$this.currentTabIndex }) {
          TabContent() {
            Home()
          }
          TabContent() {

          }
          TabContent() {
            PlayHistory()
          }
        }
        .height('100%')
        .scrollable(false)
        .vertical(__System__.windowWidth > min_width)
        .barWidth(0)
        .barHeight(0)
        .barMode(BarMode.Fixed)

        Stack() {
          Image(__musicPlayer__.playNow?.cover_url ?? $r('app.media.linder'))
            .brightness(0.75)
            .width('100%')
            .height('100%')
            .blur(150)
            .geometryTransition('playerBackground', {follow: true})
            .transition(TransitionEffect.OPACITY)
            .borderRadius(20)
          Column({ space: 10 }) {
            if (__musicPlayer__.playNow == undefined) {
              Text('暂未播放')
                .fontColor(Color.White)
                .layoutWeight(1)
                .fontSize(26)
                .fontWeight(FontWeight.Bold)
                .textAlign(TextAlign.Center)

            } else {
              Row() {
                Image(__musicPlayer__.playNow.cover_url)
                  .size({ width: 65, height: 65 })
                  .borderRadius(16)
                  .geometryTransition('playerCoverImage', {follow: true})
                  .transition(TransitionEffect.OPACITY)


                Column() {
                  Text(__musicPlayer__.playNow.title)
                    .textAlign(TextAlign.Center)
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.MARQUEE })
                    .fontWeight(FontWeight.Medium)
                    .geometryTransition('playerTitleText', {follow: true})
                    .transition(TransitionEffect.OPACITY)
                    .fontColor(Color.White)
                  Text(__musicPlayer__.playNow.uploader_name)
                    .textAlign(TextAlign.Center)
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.MARQUEE })
                    .geometryTransition('playerArtistsText', {follow: true})
                    .transition(TransitionEffect.OPACITY)
                    .fontColor(Color.White)

                }.layoutWeight(1)

                Row({ space: 10 }) {
                  Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
                    SymbolGlyph($r('sys.symbol.backward_end_fill'))
                      .fontSize(36)
                      .fontColor([Color.White])
                  }
                  .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
                  .onClick(()=>{
                    __musicPlayer__.Last()
                  })
                  .geometryTransition('playerPreviousButton', {follow: true})
                  .transition(TransitionEffect.OPACITY)

                  Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
                    SymbolGlyph(__musicPlayer__.isPlaying ? $r('sys.symbol.pause') : $r('sys.symbol.play_fill'))
                      .fontSize(36)
                      .fontColor([Color.White])
                  }
                  .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
                  .onClick(()=>{
                    __musicPlayer__.isPlaying ? __musicPlayer__.Pause() : __musicPlayer__.Play()
                  })
                  .geometryTransition('playerPlayPauseButton', {follow: true})
                  .transition(TransitionEffect.OPACITY)

                  Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
                    SymbolGlyph($r('sys.symbol.forward_end_fill'))
                      .fontSize(36)
                      .fontColor([Color.White])
                  }
                  .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
                  .onClick(()=>{
                    __musicPlayer__.Next()
                  })
                  .geometryTransition('playerNextButton', {follow: true})
                  .transition(TransitionEffect.OPACITY)

                }

              }.size({ width: '100%', height: '100%' })
              .padding(8)
              .justifyContent(FlexAlign.SpaceBetween)
            }
          }
          .size({ width: '100%', height: 78 })
          .borderRadius(20)
          .clip(true)
        }.position({ bottom: 40 })
        .size({ width: '100%', height: 78 })
        .padding({ left: 8, right: 8 })
        .onClick(() => {
          console.log('testTAG jump')
          XAnimation.runWithAnimation(()=> {__UI__.mainPageStack.pushPathByName('player', undefined, false)})
        })
      }

      if (__System__.windowWidth < min_width && __UI__.isShowSideBar) {
        Stack().height('100%').width('100%').onClick(() => {
          XAnimation.runWithAnimation(() => __UI__.isShowSideBar = false)
        })
      }
    }
    .transition(TransitionEffect.OPACITY)
    .backgroundColor(Color.Transparent)
    .bindSheet(__SettingsUIData__.settingsShow, settingsPageBuilder(), {
      preferType: SheetType.CENTER,
      height: SheetSize.LARGE,
      onDisappear: () => __SettingsUIData__.settingsShow = false
    })
    .hideTitleBar(true)
    .onBackPressed(()=>{
      XContext.getAbilityContext().moveAbilityToBackground()
      return true
    })
  }
}