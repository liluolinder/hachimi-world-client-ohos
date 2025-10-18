import { min_split_width, __System__, __UI__ } from "../../../main/ets/commons/shareDatas"
import { client } from "../../../main/ets/models/classes/pilipala"
import { I_AllCollectionsRes_data_list } from "../../../main/ets/models/interfaces/I_AllCollectionsRes"
import { curves } from "@kit.ArkUI"
import { XAnimation, XContext } from "commonpackage"
import { Cover } from "../../components/Cover"
import { scanBarcode, scanCore } from "@kit.ScanKit"
import { BusinessError } from "@kit.BasicServicesKit"
import { I_CollectionsInfoRes_data_item } from "../../../main/ets/models/interfaces/I_CollectionsInfoRes"
import { I_CollectionPushParm } from "../../../main/ets/models/interfaces/I_CollectionPushParm"
import { backgroundBlurModifier} from "../../../main/ets/models/classes/backGroundEffect"

@ObservedV2
export class CollectionMainPage{
    @Trace inSearchMode: boolean = false
}

export let __CollectionMainPage__ = new CollectionMainPage()

@ComponentV2
export struct collectionMainPage {
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
                .useEffect(true)
                .attributeModifier(backgroundBlurModifier)
                .backgroundColor(Color.Transparent)
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
                .visibility(__CollectionMainPage__.inSearchMode ? Visibility.None : Visibility.Visible)

                Search()
                    .useEffect(true)
                    .attributeModifier(backgroundBlurModifier)
                    .width(__CollectionMainPage__.inSearchMode ? '100%' : "45%")
                    .focusable(__CollectionMainPage__.inSearchMode)
                    .onSubmit((value: string) => {
                        XAnimation.runWithAnimation(() => {
                            __CollectionMainPage__.inSearchMode = false
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
                            __CollectionMainPage__.inSearchMode = true
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
                LazyForEach(client.allCollectionArray, (item: I_AllCollectionsRes_data_list, index: number) => {
                    ListItem() {
                        Column({ space: 15 }) {
                            Cover({ data: item.act_pic, aspectRatioValue: undefined })
                            Column({ space: 2 }) {
                                Text(item.act_name)
                                    .fontSize(16)
                                    .maxLines(1)
                                    .textOverflow({ overflow: TextOverflow.MARQUEE }).fontWeight(FontWeight.Medium)
                            }.alignItems(HorizontalAlign.Start)

                        }.alignItems(HorizontalAlign.Start)
                    }
                    .onClick(async () => {
                        __UI__.mainPageStack.pushPathByName('collectionInfoPage', item)
                    })
                    .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
                    .padding(15)
                    .width('100%')
                    .borderRadius(15)
                    .transition(TransitionEffect.OPACITY
                        .combine(TransitionEffect.scale({ x: 0.5, y: 0.5 }))
                        .animation({ curve: curves.springMotion(0.5, 0.7) }))
                    .attributeModifier(backgroundBlurModifier)
                    .useEffect(true)
                }, (item: string) => item)
            }
            .onAreaChange((_, newValue) => {
                XAnimation.runWithAnimation(() => {
                    this.ColumnNumber = Math.max(Math.ceil(new Number(newValue.width).valueOf() / 240), 2)
                }, this.ColumnNumber > 0)
            })
            .padding({ left: 16, right: 16 })
            .scrollBar(BarState.Off)
            .contentStartOffset(__System__.topRectHeight + 70)
            .contentEndOffset(Math.max(__System__.bottomRectHeight, 16))
            .blur(__CollectionMainPage__.inSearchMode? 50: 0)
            .lanes(this.ColumnNumber, 10)
            .alignSelf(ItemAlign.Center)
            .align(Alignment.Center)
            .layoutWeight(1)
            .edgeEffect(EdgeEffect.Spring)
            .onReachEnd(async () => {
                if (this.canGetMore) {
                    this.canGetMore = false
                    client.getAllCollections().then((res) => {
                        this.canGetMore = true
                    })
                }

            })

            if (__CollectionMainPage__.inSearchMode) {
                Column()
                    .size({ width: '100%', height: '100%' })
                    .onClick(() => {
                        XAnimation.runWithAnimation(() => {
                            __CollectionMainPage__.inSearchMode = false
                        })
                    })
            }

        }
        .backgroundColor(Color.Transparent)

        .size({ width: '100%', height: '100%' })
    }
}