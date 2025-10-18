import { __User__ } from "../../../main/ets/commons/shareDatas"
import { client } from "../../../main/ets/models/classes/pilipala"
import { I_checkQRCodeStatusRes_Data } from "../../../main/ets/models/interfaces/I_checkQRCodeStatusRes"
import { I_LoginQRCode } from "../../../main/ets/models/interfaces/I_LoginQRCodeRes"

@ComponentV2
export struct LoginOrUserInfo {
    @Local loginQRCode?: I_LoginQRCode
    @Local loginIndex: number = 0
    @Local QRCodeStatus?: I_checkQRCodeStatusRes_Data
    @Local checkStatusIntervalIndex: number = 0

    @Monitor('loginIndex')
    generateOrCheckQRCode() {
        if (this.loginIndex == 1) {
            client.generateLoginQRCode().then(async (QRCode: I_LoginQRCode | undefined) => {
                if (QRCode) {
                    this.loginQRCode = QRCode
                    this.QRCodeStatus = await client.checkQRCodeStatus(this.loginQRCode?.key!)
                    this.checkStatusIntervalIndex = setInterval(async () => {
                        this.QRCodeStatus = await client.checkQRCodeStatus(this.loginQRCode?.key!)
                        if (this.QRCodeStatus?.code == 0 || this.QRCodeStatus?.code == 86038) {
                            clearInterval(this.checkStatusIntervalIndex)
                        }
                    }, 1000)
                }
            })
        }
    }

    aboutToDisappear(): void {
        clearInterval(this.checkStatusIntervalIndex)
    }

    build() {
        Column() {
            if (!__User__.isLogin) {

                if (this.loginIndex == 0) {
                    Image($r('app.media.bilibili'))
                        .size({ width: 150, height: 150 })
                    Blank()
                    Column({ space: 20 }) {
                        Button('二维码登录')
                            .size({ width: '100%', height: 42 })
                            .backgroundColor($r('app.color.bilibili_pink'))
                            .onClick(() => {
                                this.loginIndex = 1
                            })
                        Button('账号密码登录')
                            .size({ width: '100%', height: 42 })
                            .onClick(() => {
                                this.loginIndex = 2
                            })

                    }.padding({ bottom: 40 })
                } else if (this.loginIndex == 1) {
                    Column({ space: 25 }) {
                        Image(this.loginQRCode?.QRCode)
                            .size({ width: 200, height: 200 })

                        Text(this.QRCodeStatus?.message)
                            .fontSize(20)
                            .fontWeight(FontWeight.Medium)
                    }.alignItems(HorizontalAlign.Center)

                }


            }
        }.size({ width: '100%', height: '100%' })
        .padding({ left: 16, right: 16 })
    }
}

@Builder
export function LoginOrUserInfoBuilder() {
    LoginOrUserInfo()
}