import { XAnimation, XPreferencesManager } from "commonpackage";
import { min_width, __SettingsUIData__, __System__, __UI__ } from "../../../main/ets/commons/shareDatas";
import { Home } from "../Home/Home";

import { settingsPageBuilder } from "../settings/SettingsMainPage";

@ComponentV2
export struct Body {
    @Consumer('tabController') tabController: TabsController = new TabsController();
    @Consumer('mainPageStack') pageStack: NavPathStack = new NavPathStack()
    @Consumer('currentTabIndex') currentTabIndex: number = 0


    build() {
        Stack() {
            Tabs({ controller: this.tabController, index: $$this.currentTabIndex }) {
                TabContent(){
                    Home()
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

        }.size({ width: '100%', height: '100%' })
        .bindSheet(__SettingsUIData__.settingsShow, settingsPageBuilder(), {
            preferType: SheetType.CENTER,
            height: SheetSize.LARGE,
            onDisappear: () => __SettingsUIData__.settingsShow = false
        })

    }
}