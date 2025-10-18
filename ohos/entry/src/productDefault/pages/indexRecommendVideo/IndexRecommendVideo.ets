import { scanBarcode, scanCore } from "@kit.ScanKit"
import { BusinessError } from "@kit.BasicServicesKit"
import { StringFormat, XAnimation, XContext } from "commonpackage"
import { min_split_width, __System__, __UI__ } from "../../../main/ets/commons/shareDatas"
import { client } from "../../../main/ets/models/classes/pilipala"
import { I_GetIndexRecommendVideo_item } from "../../../main/ets/models/interfaces/I_GetIndexRecommendVideo"
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect"
import { S_PlayerParam } from "../../../main/ets/models/interfaces/S_NavParams"

@ObservedV2
export class IndexRecommendVideoData{
    @Trace inSearchMode: boolean = false
}

export const __IndexRecommendVideoData__ = new IndexRecommendVideoData()

@ComponentV2
export struct IndexRecommendVideo {
    @Local ColumnNumber: number = -1
    @Local canGetMore: boolean = true

    build() {
        Stack({ alignContent: Alignment.Top }) {
            Row({ space: 10 }) {
                Button({ type: ButtonType.Circle }) {
                    SymbolGlyph($r('sys.symbol.circle_viewfinder'))
                        .fontSize(24)
                        .fontColor([$r('sys.color.font_secondary')])
                }
                .width(40)
                .height(40)
                .attributeModifier(backgroundBlurModifier)
                .backgroundColor(Color.Transparent)
                .useEffect(true)
                .onClick(() => {
                    let options: scanBarcode.ScanOptions = {
                        scanTypes: [scanCore.ScanType.ONE_D_CODE],
                        enableMultiMode: true,
                        enableAlbum: true
                    };
                    try {
                        scanBarcode.startScanForResult(XContext.getAbilityContext(), options)
                            .then((result: scanBarcode.ScanResult) => {

                            })
                            .catch((error: BusinessError) => {
                                console.log(`Liquid:${error}`)
                            });
                    } catch (error) {
                        console.log(`Liquid:${error}`)

                    }
                })
                .visibility(__IndexRecommendVideoData__.inSearchMode ? Visibility.None : Visibility.Visible)

                Search()
                    .attributeModifier(backgroundBlurModifier)
                    .width(__IndexRecommendVideoData__.inSearchMode ? '100%' : "45%")
                    .focusable(__IndexRecommendVideoData__.inSearchMode)
                    .onSubmit((value: string) => {
                        XAnimation.runWithAnimation(() => {
                            __IndexRecommendVideoData__.inSearchMode = false
                        })
                    })
                    .onChange((value: string) => {
                        if (value == '' || value == undefined) {
                            XAnimation.runWithAnimation(() => {

                            })
                        }
                    })
                    .onTouch(() => {
                        XAnimation.runWithAnimation(() => {
                            __IndexRecommendVideoData__.inSearchMode = true
                        })
                    })


            }
            .zIndex(1)
            .justifyContent(FlexAlign.End)
            .width('100%')
            .padding({ top: __System__.topRectHeight, left: 16, right: 16 })
            .transition(XAnimation.getAnimation('up'))
            .zIndex(114514)

            List({ space: 10 }) {
                LazyForEach(client.indexRecommendVideo, (item: I_GetIndexRecommendVideo_item, index: number) => {

                    IndexRecommendVideoCover({ data: item })

                }, (item: I_GetIndexRecommendVideo_item) => JSON.stringify(item))
            }
            .padding({ left: 16, right: 16 })
            .scrollBar(BarState.Off)
            .contentStartOffset(__System__.topRectHeight + 70)
            .contentEndOffset(Math.max(__System__.bottomRectHeight, 16))
            .onAreaChange((_, newValue) => {
                XAnimation.runWithAnimation(() => {
                    this.ColumnNumber = Math.max(Math.ceil(new Number(newValue.width).valueOf() / 300), 2)
                }, this.ColumnNumber > 0)
            })
            .lanes(this.ColumnNumber, 10)
            .layoutWeight(1)
            .cachedCount(this.ColumnNumber * 10)
            .edgeEffect(EdgeEffect.Spring)
            .onReachEnd(async () => {
                if (this.canGetMore) {
                    this.canGetMore = false
                    client.getIndexRecommendVideo().then((res) => {
                        this.canGetMore = true
                    })
                }

            })
            .blur(__IndexRecommendVideoData__.inSearchMode? 50: 0)

            if (__IndexRecommendVideoData__.inSearchMode) {
                Column()
                    .size({ width: '100%', height: '100%' })
                    .onClick(() => {
                        XAnimation.runWithAnimation(() => {
                            __IndexRecommendVideoData__.inSearchMode = false
                        })
                    })
            }

        }
        .size({ width: '100%', height: '100%' })

    }
}

@ComponentV2
export struct IndexRecommendVideoCover {
    @Param @Require data: I_GetIndexRecommendVideo_item
    @Param defaultImg: ResourceStr = $r('app.media.default_videoCover')
    @Param imageFit: ImageFit = ImageFit.Cover
    @Local private isFinish: boolean = false

    build() {
        ListItem() {
            Column() {
                RelativeContainer() {
                    Stack() {
                        Image(this.data.pic ?? this.defaultImg)
                            .transition(TransitionEffect.OPACITY)
                            .objectFit(this.imageFit)
                            .onComplete((res) => {
                                this.isFinish = true
                            })
                        if(!this.isFinish){
                            Image(this.defaultImg)
                                .transition(TransitionEffect.OPACITY.animation({duration: 150, curve: Curve.Ease}))
                                .objectFit(ImageFit.Cover)
                        }
                    }.width('100%').enabled(false).aspectRatio(3 / 2)


                    Text(StringFormat.formatSeconds(this.data.duration)).fontSize(12).fontColor(Color.White)
                        .backgroundColor('#40000000')
                        .alignRules({
                            bottom: { anchor: "__container__", align: VerticalAlign.Bottom },
                            right: { anchor: "__container__", align: HorizontalAlign.End }
                        })
                        .padding({ left: 5, right: 5, top: 3, bottom: 3 })
                        .borderRadius({ topLeft: 5 })

                    Text(){
                        SymbolSpan($r('sys.symbol.eye')).fontColor([Color.White])
                        Span(' ')
                        Span(StringFormat.formatNumber(this.data.stat?.view ?? 0))
                    }.fontSize(12).fontColor(Color.White)
                        .backgroundColor('#40000000')
                        .alignRules({
                            bottom: { anchor: "__container__", align: VerticalAlign.Bottom },
                            left: { anchor: "__container__", align: HorizontalAlign.Start }
                        })
                        .padding({ left: 5, right: 5, top: 3, bottom: 3 })
                        .borderRadius({ topRight: 5 })
                }.aspectRatio(3 / 2)

                Column({ space: 10 }) {
                    Column(){
                        Text(this.data.title)
                            .fontSize(15)
                            .maxLines(2)
                            .textOverflow({ overflow: TextOverflow.Ellipsis })
                            .fontWeight(FontWeight.Medium)
                            .textAlign(TextAlign.Start)
                    }.height(32)


                    Flex({justifyContent: FlexAlign.SpaceBetween, space: {main: {value: 5, unit: undefined}}}) {

                        Row({space: 5}){
                            Image(this.data.owner.face)
                                .size({ width: 15, height: 15 })
                                .borderRadius(1000)

                            Text(this.data.owner.name)
                                .fontSize(12)
                                .maxLines(1)
                                .layoutWeight(1)
                                .opacity(0.8)
                                .textOverflow({overflow: TextOverflow.Ellipsis})
                        }


                        if (this.data.is_followed == 1) {
                            Row({space: 2}) {
                                SymbolGlyph($r('sys.symbol.checkmark')).fontColor([Color.White]).fontSize(9)
                                Text('已关注').fontColor(Color.White).fontSize(9).maxLines(1)
                            }.backgroundColor($r('app.color.bilibili_pink'))
                            .padding(4)
                            .borderRadius(5)
                            .opacity(0.8)
                        }
                    }.width('100%')



                }.width('100%')
                .padding({top: 8, bottom: 8, left: 10, right: 10})
                .alignItems(HorizontalAlign.Start)

            }.alignItems(HorizontalAlign.Start)
        }
        .attributeModifier(backgroundBlurModifier)
        .onClick(async () => {
            __UI__.mainPageStack.pushPathByName('player',
                { bvid: this.data.bvid, cid: this.data.cid } as S_PlayerParam)
        })
        .transition(TransitionEffect.OPACITY.animation({duration: 150, curve: Curve.Ease}))
        .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
        .width('100%')
        .clip(true)
        .borderRadius(10)

    }
}


