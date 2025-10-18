import { XAnimation } from "commonpackage"
import { webview } from "@kit.ArkWeb";
import { __Client__ } from "../../../../main/ets/models/classes/hachimi";
import { I_GenerateCaptchaRes_data } from "../../../../main/ets/models/interfaces/I_GenerateCaptcha";
import { BusinessError } from "@kit.BasicServicesKit";
import { USER_AGENT } from "../../../../main/ets/commons/commonItem";

@Builder
export function personalSheetBuilder() {
  personalSheet()
}

enum loginType {
  Login, Register
}


@ComponentV2
export struct personalSheet {
  controller: webview.WebviewController = new webview.WebviewController();

  @Local localLoginType: loginType = loginType.Login
  @Local email: string = ''
  @Local password: string = ''
  @Local confirmPassword: string = ''

  @Local captchaInfo?:I_GenerateCaptchaRes_data

  @Monitor('localLoginType')
  clearEmailWithPassword(){
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
      TextInput({ placeholder: '邮箱',text: $$this.email })
        .height(48)
        .contentType(ContentType.EMAIL_ADDRESS)

      TextInput({ placeholder: '密码',text: $$this.password })
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
    .onClick(() => {
      XAnimation.runWithAnimation(async () => {
        //this.captchaInfo = await __Client__.generateCaptcha()
      })
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
      TextInput({ placeholder: '邮箱',text: $$this.email })
        .height(48)
        .contentType(ContentType.EMAIL_ADDRESS)

      TextInput({ placeholder: '密码',text: $$this.password })
        .height(48)
        .contentType(ContentType.PASSWORD)

      TextInput({ placeholder: '确认密码',text: $$this.confirmPassword })
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