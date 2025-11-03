import { File, XAnimation, XContext, XPreferencesManager } from 'commonpackage';
import { __CustomBackgroundData__ } from '../../components/CustomBackground';
import { SettingsClickItem, SettingsToggleItem } from '../../components/SettingsItem';
import { fileIo, fileUri } from '@kit.CoreFileKit';


@Builder
export function settingsCustomBackgroundBuilder() {
  SettingsCustomBackground()
}

@ComponentV2
struct SettingsCustomBackground {
  build() {
    NavDestination() {
      List({ space: 10 }) {
        SettingsToggleItem({
          symbol: $r('sys.symbol.ohos_photo'),
          message: '使用自定义背景',
          checked: __CustomBackgroundData__.enableCustomBackground,
          onChange: (checked) => {
            XAnimation.runWithAnimation(() => __CustomBackgroundData__.enableCustomBackground = checked)
            XPreferencesManager.putPreferencesValue('pilipala_settings', 'enableCustomBackground', checked)
            __CustomBackgroundData__.flash = !__CustomBackgroundData__.flash

          }
        })

        if (__CustomBackgroundData__.enableCustomBackground) {
          SettingsToggleItem({
            symbol: $r('sys.symbol.brush'),
            message: '自定义背景跟随颜色模式',
            checked: __CustomBackgroundData__.customBackgroundFollowColorMode,
            onChange: (checked) => {
              XAnimation.runWithAnimation(() => __CustomBackgroundData__.customBackgroundFollowColorMode = checked)
              XPreferencesManager.putPreferencesValue('pilipala_settings', 'customBackgroundFollowColorMode', checked)
              __CustomBackgroundData__.flash = !__CustomBackgroundData__.flash
            }
          })
            .transition(XAnimation.getAnimation('bottom'))

          SettingsClickItem({
            symbol: $r('sys.symbol.picture_fill'),
            message: __CustomBackgroundData__.customBackgroundFollowColorMode ? '选取浅色模式背景' : '选取背景',
            onPress: async () => {
              File.pickMedia().then(async (result) => {
                console.log('testTAG', result)
                if (!result) {
                  return
                }
                await File.copy(result, XContext.getFilesDir() + '/background')
                __CustomBackgroundData__.flash = !__CustomBackgroundData__.flash
              })
            }
          })
            .transition(XAnimation.getAnimation('bottom'))

          if (__CustomBackgroundData__.customBackgroundFollowColorMode) {
            SettingsClickItem({
              symbol: $r('sys.symbol.picture_fill'),
              message: '选取深色模式背景',
              onPress: async () => {
                File.pickMedia().then(async (result) => {
                  if (!result) {
                    return
                  }
                  await File.copy(result, XContext.getFilesDir() + '/background_dark')
                  __CustomBackgroundData__.flash = !__CustomBackgroundData__.flash
                })
              }
            })
              .transition(XAnimation.getAnimation('bottom'))
          }
        }
      }.margin({ left: 16, right: 16, top: 5 })
      .contentEndOffset(16)
      .fadingEdge(true)
      .scrollBar(BarState.Off)

    }.padding({ top: 8 })
    .backgroundColor($r('sys.color.comp_background_gray'))
    .title('自定义背景')
  }
}