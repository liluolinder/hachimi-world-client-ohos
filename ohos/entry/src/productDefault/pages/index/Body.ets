import { XAnimation, XContext } from "commonpackage";
import { min_width } from "../../../main/ets/commons/commonItem";
import {  __SettingsUIData__, __System__, __UI__ } from "../../../main/ets/commons/shareDatas";
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
      Tabs({ controller: this.tabController, index: $$this.currentTabIndex }) {
        TabContent() {
          Home()
        }
        TabContent() {
          Home()
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