import { StringFormat, XAnimation } from "commonpackage"
import { __System__, __UI__, __User__ } from "../../../main/ets/commons/shareDatas"
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect"
import { __Client__ } from "../../../main/ets/models/classes/hachimi"
import { I_GetRecentSongRes_data_song } from "../../../main/ets/models/interfaces/I_GetRecentSong"
import { I_SearchSongRes_data_hit } from "../../../main/ets/models/interfaces/I_SearchSong"
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer"
import { Player } from "../player/Player"
import { curves } from "@kit.ArkUI"


@ObservedV2
export class HomeData {
  @Trace inSearchMode: boolean = false
}

export const __Home__ = new HomeData()

@ComponentV2
export struct Home {
  @Local ColumnNumber: number = -1
  @Local canGetMore: boolean = true
  @Local showType: number = 0 //0为最新歌曲 , 1为搜索

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
              __Client__.searchSong(value, true)
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

      if ((this.showType == 0 && __Client__.recentSongArray.dataArray.length == 0) ||
        (this.showType == 1 && __Client__.searchSongArray.dataArray.length == 0)) {
        Column({ space: 10 }) {
          LoadingProgress()
            .size({ width: 80, height: 80 })
          Text('正在加载请稍后，加载速度取决于网速')
            .fontWeight(FontWeight.Bold)
            .fontSize(26)
        }.size({ width: '100%', height: '100%' })
        .justifyContent(FlexAlign.Center)
      } else {
        List({ space: 10 }) {
          if (this.showType == 0) {
            LazyForEach(__Client__.recentSongArray, (item: I_GetRecentSongRes_data_song, index: number) => {
              HomeCover({ data: item, type: 0 })
                .onClick(async () => {
                  __musicPlayer__.playNow = item
                  __musicPlayer__.playList = []
                  __Client__.recentSongArray.dataArray.forEach((itemA) => {
                    __musicPlayer__.playList.push(itemA.display_id)
                  })
                  __musicPlayer__.playIndex = index
                })

            }, (item: I_GetRecentSongRes_data_song) => JSON.stringify(item))
          } else {
            LazyForEach(__Client__.searchSongArray, (item: I_SearchSongRes_data_hit, index: number) => {
              HomeCover({ data: item, type: 1 })
                .onClick(async () => {
                  __musicPlayer__.playNow = item
                  __musicPlayer__.playList = []
                  __Client__.searchSongArray.dataArray.forEach((itemA) => {
                    __musicPlayer__.playList.push(itemA.display_id)
                  })
                  __musicPlayer__.playIndex = index
                })
            }, (item: I_SearchSongRes_data_hit) => JSON.stringify(item))
          }
        }
        .padding({ left: 16, right: 16 })
        .scrollBar(BarState.Off)
        .contentStartOffset(__System__.topRectHeight + 70)
        .contentEndOffset(Math.max(__System__.bottomRectHeight, 16) + 100)
        .onAreaChange((_, newValue) => {
          XAnimation.runWithAnimation(() => {
            this.ColumnNumber = Math.max(Math.ceil(new Number(newValue.width).valueOf() / 300), 2)
          }, this.ColumnNumber > 0)
        })
        .lanes(this.ColumnNumber, 10)
        .layoutWeight(1)
        .cachedCount(this.ColumnNumber * 10)
        .edgeEffect(EdgeEffect.Spring)
        .blur(__Home__.inSearchMode ? 50 : 0)
      }


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
  @Param @Require data: I_GetRecentSongRes_data_song | I_SearchSongRes_data_hit
  @Param defaultImg: ResourceStr = $r('app.media.layered_image')
  @Param imageFit: ImageFit = ImageFit.Cover
  @Local private isFinish: boolean = false

  build() {
    ListItem() {
      Column() {
        RelativeContainer() {
          Stack() {
            Image((this.type == 0 ? (this.data as I_GetRecentSongRes_data_song).cover_url :
              (this.data as I_SearchSongRes_data_hit).cover_art_url) ?? this.defaultImg)
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
          }.width('100%').enabled(false).aspectRatio(3 / 2)


          Text(StringFormat.formatSeconds(this.data.duration_seconds))
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
        }.aspectRatio(3 / 2)

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
