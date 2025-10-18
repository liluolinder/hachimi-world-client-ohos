import { ListItemWithArrow } from "../../components/ListItemWithArrow"
import { LoginOrUserInfoBuilder } from "./LoginOrUserInfo"
import { XAnimation } from "commonpackage";
import { ImageModifier } from "@ohos.arkui.modifier";
import { __CustomBackgroundData__ } from "../../components/CustomBackground";
import { I_PageInfo } from "../../../main/ets/models/interfaces/I_PageInfo";
import { min_width, __SettingsUIData__, __System__, __UI__, __User__ } from "../../../main/ets/commons/shareDatas";
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect";
import { LengthUnit } from "@kit.ArkUI";

@ComponentV2
export struct SideBar {
    @Consumer('tabController') tabController: TabsController = new TabsController();
    @Consumer('currentTabIndex') currentTabIndex: number = 0
    @Local showLoginOrUserInfo: boolean = false
    pageArray: I_PageInfo[] = [
        { name: '推荐视频', symbol: $r('sys.symbol.wand_and_stars') },
        { name: '历史观看', symbol: $r('sys.symbol.clock') },
        { name: '收藏集', symbol: $r('sys.symbol.rectangle') },

    ]
    avatarModifier_Large: ImageModifier = new ImageModifier().height(75).width(75).borderRadius(1000).draggable(false)
    avatarModifier_Small: ImageModifier =
        new ImageModifier().size({ width: '100%' }).aspectRatio(1).borderRadius(1000).clip(true)

    build() {

        Column({ space: 10 }) {
            Scroll() {
                Column({ space: 10 }) {
                    Row({ space: 12 }) {
                        Stack(){
                            Image(__User__.userDetailedInfo?.face ?? $r('app.media.bilibili_icon'))
                                .attributeModifier(__UI__.isLargeSidebar ? this.avatarModifier_Large :
                                    this.avatarModifier_Small)
                        }

                        if (__UI__.isLargeSidebar) {
                            if(__User__.isLogin){
                                Column({space: 2}){
                                    Text(__User__.userDetailedInfo?.uname)
                                        .fontWeight(FontWeight.Bold)
                                        .fontSize(20)
                                        .maxLines(1)
                                        .textOverflow({ overflow: TextOverflow.MARQUEE })
                                    Text(`硬币数: ${__User__.userDetailedInfo?.money ?? 0}`).maxLines(1).borderRadius(5).fontSize(10).opacity(0.8)
                                    Row({space: 5}){
                                        Text(`LV${__User__.userDetailedInfo?.level_info.current_level ?? 0}`).maxLines(1).padding({left: 5, right: 5, top: 2, bottom: 2}).borderRadius(5).fontSize(10).backgroundColor($r('app.color.bilibili_pink')).fontColor(Color.White).fontWeight(FontWeight.Medium)
                                        if(__User__.userDetailedInfo?.vipStatus){
                                            Text((__User__.userDetailedInfo?.vipType ?? 0) == 1? '大会员': '年度大会员').maxLines(1).padding({left: 5, right: 5, top: 2, bottom: 2}).borderRadius(5).fontSize(10).backgroundColor($r('app.color.bilibili_pink')).fontColor(Color.White).fontWeight(FontWeight.Medium)
                                        }
                                    }.margin({top: 2})
                                }.alignItems(HorizontalAlign.Start)
                                .transition(TransitionEffect.OPACITY.combine(TransitionEffect.translate({ x: -100 })))
                            }else{
                                Text('请先登录')
                                    .fontWeight(FontWeight.Bold)
                                    .fontSize(20)
                                    .layoutWeight(1)
                                    .textAlign(TextAlign.Center)
                                    .maxLines(1)
                                    .textOverflow({ overflow: TextOverflow.MARQUEE })
                            }

                        }
                    }
                    .backgroundColor($r('sys.color.comp_background_list_card'))
                    .borderRadius(20)
                    .padding(__UI__.isLargeSidebar ? 12 : 0)
                    .width('100%')
                    .onClick(() => {
                        this.showLoginOrUserInfo = true
                    })
                    .bindSheet($$this.showLoginOrUserInfo, LoginOrUserInfoBuilder, {
                        height: SheetSize.MEDIUM,
                        preferType: SheetType.CENTER,
                        title: { title: '登录' }
                    })


                    Column({space: __UI__.isLargeSidebar ? 0 : 10}){
                        ForEach(this.pageArray, (item: I_PageInfo, index: number) => {
                            ListItemWithArrow({
                                symbol: item.symbol,
                                image: item.image,
                                message: item.name,
                                isLarge: __UI__.isLargeSidebar,
                                isHighlight: this.currentTabIndex == index
                            })
                                .onClick(() => {
                                    this.tabController.changeIndex(index)
                                })
                        })
                    }
                    .borderRadius(20)
                    .width('100%')
                    .backgroundColor(__UI__.isLargeSidebar? $r('sys.color.comp_background_list_card'): Color.Transparent)

                }
            }
            .fadingEdge(true)
            .scrollBar(BarState.Off)
            .edgeEffect(EdgeEffect.Spring)
            .align(Alignment.Top)
            .width('100%')
            .layoutWeight(1)
            .clip(true)

            Flex({ direction: __UI__.isLargeSidebar? FlexDirection.Row: FlexDirection.ColumnReverse, space: {main: {value: 10, unit: LengthUnit.VP}} }) {
                if (__System__.windowWidth > min_width) {
                    ListItemWithArrow({
                        symbol: __UI__.isLargeSidebar ? $r('sys.symbol.open_sidebar') : $r('sys.symbol.close_sidebar'),
                        isLarge: false,
                        hasBackground: true,
                        showArrow: false
                    })
                        .onClick(() => {
                            XAnimation.runWithAnimation(() => {
                                __UI__.isLargeSidebar = !__UI__.isLargeSidebar
                            })
                        }).height(__UI__.isLargeSidebar ? 55 : 'auto')
                        .width(__UI__.isLargeSidebar ? 55 : '100%')

                }

                ListItemWithArrow({
                    symbol: $r('sys.symbol.gearshape'),
                    message: '设置',
                    isLarge: __UI__.isLargeSidebar,
                    hasBackground: true,
                    showArrow: true
                })
                    .layoutWeight(__UI__.isLargeSidebar? 1: 0)
                    .onClick(() => {
                        __SettingsUIData__.settingsShow = true
                    })

            }.height('auto')

        }.size({ height: '100%', width: '100%' })
        .padding({
            top: Math.max(__System__.topRectHeight, 10),
            left: __UI__.isLargeSidebar ? 16 : 12,
            right: __UI__.isLargeSidebar ? 16 : 12,
            bottom: __UI__.isLargeSidebar? Math.max(__System__.bottomRectHeight, 10): 10
        })
        .attributeModifier(backgroundBlurModifier)

    }
}