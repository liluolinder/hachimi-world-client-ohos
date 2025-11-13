import { BasicDataSource, StringFormat, XAnimation } from "commonpackage"
import { __System__, __UI__, __User__ } from "../../../main/ets/commons/shareDatas"
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect"
import { __Client__ } from "../../../main/ets/models/classes/hachimi"
import { I_GetSongRes_data_song } from "../../../main/ets/models/interfaces/I_GetSong"
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer"
import { curves } from "@kit.ArkUI"
import { min_width } from "../../../main/ets/commons/commonItem"


@ObservedV2
export class HomeData {
  @Trace inSearchMode: boolean = false
}

export const __Home__ = new HomeData()

@ComponentV2
export struct Home {
  @Local canGetMore: boolean = true
  @Local showType: number = 0 //0为最新歌曲 , 1为搜索
  @Local homoBlockArray: ResourceStr[] =
    ['最近发布', '每日推荐', '本周热门', '纯净哈基米专区', '古典专区', '原曲不使用专区']

  @Local tempListItemArray:number[] =[]

  getDaraProvider(index:number):BasicDataSource<I_GetSongRes_data_song>{

    switch (index){
      case 0:
        return __Client__.recentSongArray
      case 1:
        return __Client__.recommendSongArray
      case 2:
        return __Client__.weeklySongArray
      case 3:
        return __Client__.pureSongArray
      case 4:
        return __Client__.classicalSongArray
      case 5:
        return __Client__.useNoOriginalSongArray
    }
    return __Client__.recentSongArray
  }

  build() {
    Stack({ alignContent: Alignment.Top }) {
      Row({ space: 10 }) {
        Search()
          .attributeModifier(backgroundBlurModifier)
          .width(__Home__.inSearchMode ? '100%' : "45%")
          .focusable(__Home__.inSearchMode)
          .onSubmit((value: string) => {
            XAnimation.runWithAnimation(() => {
              __Home__.inSearchMode = false
            })
            if (value != '') {
              this.showType = 1
             // __Client__.searchSong(value, true)
            }
          })
          .onChange((value: string) => {
            if (value == '' || value == undefined) {
              XAnimation.runWithAnimation(() => {
                this.showType = 0
              })
            }
          })
          .onTouch(() => {
            XAnimation.runWithAnimation(() => {
              __Home__.inSearchMode = true
            })
          })
      }
      .zIndex(1)
      .justifyContent(FlexAlign.End)
      .width('100%')
      .padding({ top: __System__.topRectHeight, left: 16, right: 16 })
      .transition(XAnimation.getAnimation('up'))
      .zIndex(114514)


      List({ space: 30 }) {
        ForEach(this.tempListItemArray,()=>{
          ListItem()
            .height(__System__.topRectHeight + 70)
            .width('100%')
        })
        ForEach(this.homoBlockArray, (item: ResourceStr, index: number) => {
          ListItem() {
            Column() {
              Row() {
                Text(item)
                  .width('100%')
                  .fontSize(24)
                  .fontWeight(FontWeight.Bold)
              }

              Column() {
                if ((this.getDaraProvider(index).totalCount() != 0 ) ){
                  List({ space: 10 }) {
                    LazyForEach(this.getDaraProvider(index), (item: I_GetSongRes_data_song, indexA: number) => {
                      HomeCover({ data: item, type: index >=3?1:0 })
                        .width(150)
                        .onClick(async () => {
                          __musicPlayer__.playNow = item
                          __musicPlayer__.playList = []
                          this.getDaraProvider(index).dataArray.forEach((itemA) => {
                            __musicPlayer__.playList.push(itemA.display_id)
                          })
                          __musicPlayer__.playIndex = indexA
                        })

                    }, (item: I_GetSongRes_data_song) => JSON.stringify(item))
                  }
                  .scrollBar(BarState.Off)
                  .width('100%')
                  .listDirection(Axis.Horizontal)
                  .lanes(2, 10)
                  .edgeEffect(EdgeEffect.Spring)
                  .blur(__Home__.inSearchMode ? 50 : 0)
                } else {
                  Column() {
                    LoadingProgress()
                      .size({ width: 100 })
                  }.height('100%')
                  .justifyContent(FlexAlign.Center)
                }
              }
            }.height(482)
          }
        },(item:ResourceStr)=>item.toString() + (new Date).toDateString())

      }
      .onAreaChange(()=>{
        this.tempListItemArray = []
        for (let index = 0; index < (__System__.windowWidth > min_width ? 2 : 1); index++) {
          this.tempListItemArray.push(0)
        }
      })
      .height('100%')
      .width('100%')
      .contentEndOffset(Math.max(__System__.bottomRectHeight, 16) + 100)
      .padding({ left: 16, right: 16 })
      .lanes(__System__.windowWidth > min_width ? 2 : 1, 20)


      if (__Home__.inSearchMode) {
        Column()
          .size({ width: '100%', height: '100%' })
          .onClick(() => {
            XAnimation.runWithAnimation(() => {
              __Home__.inSearchMode = false
            })
          })
      }


    }.size({ width: '100%', height: '100%' })

  }
}

@ComponentV2
export struct HomeCover {
  @Param @Require type: number
  @Param @Require data: I_GetSongRes_data_song
  @Param defaultImg: ResourceStr = $r('app.media.layered_image')
  @Param imageFit: ImageFit = ImageFit.Cover
  @Local private isFinish: boolean = false

  build() {
    ListItem() {
      Column() {
        RelativeContainer() {
          Stack() {
            Image((this.type == 0 ? this.data.cover_url :
              this.data.cover_art_url) ?? this.defaultImg)
              .transition(TransitionEffect.OPACITY)
              .objectFit(this.imageFit)
              .onComplete((res) => {
                this.isFinish = true
              })
            if (!this.isFinish) {
              Image(this.defaultImg)
                .transition(TransitionEffect.OPACITY.animation({ duration: 150, curve: Curve.Ease }))
                .objectFit(ImageFit.Cover)
            }
          }.width('100%').enabled(false).aspectRatio(1)


          Text(StringFormat.formatSeconds(this.data.duration_seconds ?? 0))
            .fontSize(12)
            .fontColor(Color.White)
            .backgroundColor('#40000000')
            .alignRules({
              bottom: { anchor: "__container__", align: VerticalAlign.Bottom },
              right: { anchor: "__container__", align: HorizontalAlign.End }
            })
            .padding({
              left: 5,
              right: 5,
              top: 3,
              bottom: 3
            })
            .borderRadius({ topLeft: 5 })

          Text() {
            SymbolSpan($r('sys.symbol.eye')).fontColor([Color.White])
            Span(' ')
            Span(StringFormat.formatNumber(this.data.play_count ?? 0))
          }
          .fontSize(12)
          .fontColor(Color.White)
          .backgroundColor('#40000000')
          .alignRules({
            bottom: { anchor: "__container__", align: VerticalAlign.Bottom },
            left: { anchor: "__container__", align: HorizontalAlign.Start }
          })
          .padding({
            left: 5,
            right: 5,
            top: 3,
            bottom: 3
          })
          .borderRadius({ topRight: 5 })
        }.aspectRatio(1)

        Column({ space: 10 }) {
          Column() {
            Text(this.data.title)
              .fontSize(15)
              .maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
              .fontWeight(FontWeight.Medium)
              .textAlign(TextAlign.Start)
          }.height(32)


          Flex({ justifyContent: FlexAlign.SpaceBetween, space: { main: { value: 5, unit: undefined } } }) {

            Row({ space: 5 }) {

              Text(this.data.uploader_name)
                .fontSize(12)
                .maxLines(1)
                .layoutWeight(1)
                .opacity(0.8)
                .textOverflow({ overflow: TextOverflow.Ellipsis })
            }

          }.width('100%')

        }.width('100%')
        .padding({
          top: 8,
          bottom: 8,
          left: 10,
          right: 10
        })
        .alignItems(HorizontalAlign.Start)

      }.alignItems(HorizontalAlign.Start)
    }
    .attributeModifier(backgroundBlurModifier)
    .transition(TransitionEffect.OPACITY
      .combine(TransitionEffect.scale({ x: 0.5, y: 0.5 }))
      .animation({ curve: curves.springMotion(0.5, 0.7) }))
    .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
    .width('100%')
    .clip(true)
    .borderRadius(10)

  }
}
