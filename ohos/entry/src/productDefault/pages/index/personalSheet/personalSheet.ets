import { XAnimation, XContext } from "commonpackage"
import { webview } from "@kit.ArkWeb";
import { __Client__ } from "../../../../main/ets/models/classes/hachimi";
import { I_GenerateCaptchaRes_data } from "../../../../main/ets/models/interfaces/I_GenerateCaptcha";
import { BusinessError } from "@kit.BasicServicesKit";
import { Want } from "@kit.AbilityKit";
import { window } from "@kit.ArkUI";
import { WindowMode } from "@kit.TestKit";
import { __User__ } from "../../../../main/ets/commons/shareDatas";

@Builder
export function personalSheetBuilder() {
  personalSheet()
}

enum loginType {
  Login, Register
}

enum loginState {
  Idle, Loading, Error, Complete
}


@ComponentV2
export struct personalSheet {
  controller: webview.WebviewController = new webview.WebviewController();
  @Local localLoginType: loginType = loginType.Login
  @Local localLoginState: loginState = loginState.Idle
  @Local loginErrorInfo?: string = ''
  @Local email: string = ''
  @Local password: string = ''
  @Local confirmPassword: string = ''
  @Local captchaInfo?: I_GenerateCaptchaRes_data
  @Consumer('showLoginOrUserInfo') showLoginOrUserInfo: boolean = false

  @Monitor('localLoginType')
  clearEmailWithPassword() {
    this.email = ''
    this.password = ''
    this.confirmPassword = ''
  }

  @Builder
  loginBuilder() {
    Row({ space: 10 }) {
      Image($r('app.media.icon'))
        .size({ width: 60, height: 60 })
      Text('基米天堂')
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
    }
    .alignRules({
      top: { anchor: "__container__", align: VerticalAlign.Top },
      end: { anchor: "__container__", align: HorizontalAlign.End }
    })
    .id('logoWithName')

    Row({ space: 10 }) {
      Text('欢迎回家')
        .fontSize(28)
        .fontWeight(FontWeight.Normal)
    }
    .alignRules({
      top: { anchor: "logoWithName", align: VerticalAlign.Bottom },
      start: { anchor: "__container__", align: HorizontalAlign.Start },
    })
    .id('welcome')

    Column({ space: 10 }) {
      TextInput({ placeholder: '邮箱', text: $$this.email })
        .height(48)
        .contentType(ContentType.EMAIL_ADDRESS)

      TextInput({ placeholder: '密码', text: $$this.password })
        .height(48)
        .contentType(ContentType.PASSWORD)
    }
    .margin({ top: 10 })
    .alignRules({
      top: { anchor: "welcome", align: VerticalAlign.Bottom },
      start: { anchor: "__container__", align: HorizontalAlign.Start },
    })
    .id('input')

    Row() {
      Button('创建账号')
        .backgroundColor($r('sys.color.comp_background_list_card'))
        .fontColor($r('sys.color.font'))
        .onClick(() => {
          XAnimation.runWithAnimation(() => {
            this.localLoginType = loginType.Register
          })
        })

      Button('忘记账号')
        .backgroundColor($r('sys.color.comp_background_list_card'))
        .fontColor($r('sys.color.font'))
    }
    .justifyContent(FlexAlign.SpaceBetween)
    .alignRules({
      top: { anchor: "input", align: VerticalAlign.Bottom },
      start: { anchor: "__container__", align: HorizontalAlign.Start },
    })
    .id('accountButton')
    .width('100%')
    .margin({ top: 10 })

    Row() {
      Text(this.loginErrorInfo)
        .fontColor($r('sys.color.warning'))
    }
    .alignRules({
      top: { anchor: "accountButton", align: VerticalAlign.Bottom },
      start: { anchor: "__container__", align: HorizontalAlign.Start },
    })
    .width('100%')
    .margin({ top: 10 })

    Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
      if (this.localLoginState == loginState.Loading) {
        LoadingProgress()
          .size({ width: 60, height: 60 })
      } else {
        SymbolGlyph($r('sys.symbol.arrow_right'))
      }
    }.size({ width: 80, height: 80 })
    .onClick(() => {
      if (this.localLoginState != loginState.Loading) {
        this.loginErrorInfo = ''
        this.localLoginState = loginState.Loading
        XAnimation.runWithAnimation(async () => {
          this.captchaInfo = await __Client__.generateCaptcha()

          if (this.captchaInfo && this.captchaInfo.url && this.captchaInfo.url!='') {
            let want: Want = {
              action: 'ohos.want.action.viewData',
              entities: ['entity.system.browsable'],
              uri: this.captchaInfo?.url,
            };

            XContext.startAbility(want)
            XContext.getContext().showAlertDialog(
              {
                title: '提醒',
                message: '是否在弹出浏览器进行完成人机验证',
                alignment: DialogAlignment.Center,
                primaryButton: {
                  value: '否',
                  action: () => {
                  }
                },
                secondaryButton: {
                  enabled: true,
                  defaultFocus: true,
                  style: DialogButtonStyle.HIGHLIGHT,
                  value: '是',
                  action: async () => {
                    this.loginErrorInfo = await __Client__.login(this.email, this.password, this.captchaInfo?.captcha_key)
                    if (this.loginErrorInfo != 'ok' || this.loginErrorInfo == undefined) {
                      this.localLoginState = loginState.Error
                    } else {
                      this.localLoginState = loginState.Complete
                      this.showLoginOrUserInfo = false
                      XContext.toast(`欢迎${__User__.username}`)
                    }
                  }
                }
              }
            )
          }else {
            this.loginErrorInfo = '出现意料之外的问题'
          }

        })
      }
    })
    .backgroundColor($r('sys.color.button_background_color_transparent'))
    .alignRules({
      bottom: { anchor: "__container__", align: VerticalAlign.Bottom },
      end: { anchor: "__container__", align: HorizontalAlign.End },
    })
  }

  @Builder
  registerBuilder() {
    Row({ space: 10 }) {
      Image($r('app.media.icon'))
        .size({ width: 60, height: 60 })
      Text('基米天堂')
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
    }
    .alignRules({
      top: { anchor: "__container__", align: VerticalAlign.Top },
      end: { anchor: "__container__", align: HorizontalAlign.End }
    })
    .id('logoWithName')

    Row({ space: 10 }) {
      Text('成为神人')
        .fontSize(28)
        .fontWeight(FontWeight.Normal)
    }
    .alignRules({
      top: { anchor: "logoWithName", align: VerticalAlign.Bottom },
      start: { anchor: "__container__", align: HorizontalAlign.Start },
    })
    .id('welcome')

    Column({ space: 10 }) {
      TextInput({ placeholder: '邮箱', text: $$this.email })
        .height(48)
        .contentType(ContentType.EMAIL_ADDRESS)

      TextInput({ placeholder: '密码', text: $$this.password })
        .height(48)
        .contentType(ContentType.PASSWORD)

      TextInput({ placeholder: '确认密码', text: $$this.confirmPassword })
        .height(48)
        .contentType(ContentType.PASSWORD)

    }
    .offset({ top: 10 })
    .alignRules({
      top: { anchor: "welcome", align: VerticalAlign.Bottom },
      start: { anchor: "__container__", align: HorizontalAlign.Start },
    })
    .id('input')

    Row() {
      Button('已有账号')
        .backgroundColor($r('sys.color.comp_background_list_card'))
        .fontColor($r('sys.color.font'))
        .onClick(() => {
          XAnimation.runWithAnimation(() => {
            this.localLoginType = loginType.Login
          })
        })
    }.justifyContent(FlexAlign.SpaceBetween)
    .alignRules({
      top: { anchor: "input", align: VerticalAlign.Bottom },
      start: { anchor: "__container__", align: HorizontalAlign.Start },
    })
    .width('100%')
    .offset({ top: 20 })


    Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
      SymbolGlyph($r('sys.symbol.arrow_right'))
    }.size({ width: 80, height: 80 })
    .backgroundColor($r('sys.color.button_background_color_transparent'))
    .alignRules({
      bottom: { anchor: "__container__", align: VerticalAlign.Bottom },
      end: { anchor: "__container__", align: HorizontalAlign.End },
    })
  }

  build() {
    Row() {
      RelativeContainer() {
        if (this.localLoginType == loginType.Login) {
          this.loginBuilder()
        } else {
          this.registerBuilder()
        }
      }.backgroundColor($r('sys.color.comp_background_list_card'))
      .padding(16)
      .layoutWeight(1)
      .borderRadius(20)

    }
    .size({ width: '100%', height: '100%' })
    .padding(16)
  }
}