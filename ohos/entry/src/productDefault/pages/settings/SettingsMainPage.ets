import { __SettingsUIData__ } from "../../../main/ets/commons/shareDatas"
import { SettingsClickItem } from "../../components/SettingsItem"


@Builder
export function settingsPageBuilder() {
  SettingsMainPageNav()
}

@ComponentV2
struct SettingsMainPageNav {
  aboutToAppear(): void {
    __SettingsUIData__.settingsNavPathStack.pushPathByName('settingsMainPage', undefined, false)
  }

  build() {
    Navigation(__SettingsUIData__.settingsNavPathStack)
      .hideTitleBar(true)
  }
}

@Builder
export function settingsMainPageBuilder() {
  SettingsMainPage()
}

@ComponentV2
struct SettingsMainPage {
  build() {
    NavDestination() {
      List({ space: 10 }) {
        SettingsClickItem({
          symbol: $r('sys.symbol.ohos_photo'),
          message: '自定义背景',
          onPress: () => {
            __SettingsUIData__.settingsNavPathStack.pushPathByName('settingsCustomBackground', undefined, true)
          }
        })

      }
      .margin({ left: 16, right: 16, top: 5 })
      .contentEndOffset(16)
      .fadingEdge(true)
      .chainAnimation(true)
      .scrollBar(BarState.Off)
    }
    .backgroundColor($r('sys.color.comp_background_gray'))
    .title('设置')
    .hideBackButton(true)
    .onBackPressed(() => {
      __SettingsUIData__.settingsShow = false
      return true
    })
    .padding({ top: 8 })
  }
}