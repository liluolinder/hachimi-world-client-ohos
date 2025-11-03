import { XAnimation } from "commonpackage"
import { __System__, __User__ } from "../../../main/ets/commons/shareDatas"
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect"
import { __Client__ } from "../../../main/ets/models/classes/hachimi"
import { I_GetSongListRes_data_item } from "../../../main/ets/models/interfaces/I_GetSongList"
import { curves } from "@kit.ArkUI"

@ComponentV2
export struct SongList {
  @Local ColumnNumber: number = -1
  @Local canGetMore: boolean = true
  songListStack = new NavPathStack()

  build() {
    Navigation(this.songListStack) {
      if (!__User__.isLogin) {
        Stack() {
          Text('还没有登录').fontSize(32).fontWeight(FontWeight.Medium)
        }.size({ width: '100%', height: '100%' })
      } else {
        Stack({ alignContent: Alignment.Top }) {

          if (__Client__.songListArray.dataArray.length == 0) {
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
              LazyForEach(__Client__.songListArray, (item: I_GetSongListRes_data_item, index: number) => {
                ListItem() {
                  songListCover({ data: item })
                }
                .borderRadius(10)
                .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
                .clip(true)
                .onClick(async () => {
                  let res = await __Client__.getSongListInfo(item.id)
                  this.songListStack.pushPathByName('songListInfoPage', res)
                })

              }, (item: I_GetSongListRes_data_item) => JSON.stringify(item))
            }
            .padding({ left: 16, right: 16 })
            .scrollBar(BarState.Off)
            .contentStartOffset(Math.max(__System__.topRectHeight, 10))
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
          }
        }
        .size({ width: '100%', height: '100%' })
      }
    }.mode(NavigationMode.Stack)
    .hideTitleBar(true)
    .hideToolBar(true)

  }
}

@ComponentV2
export struct songListCover {
  @Param @Require data: I_GetSongListRes_data_item
  @Param defaultImg: ResourceStr = $r('app.media.layered_image')
  @Param imageFit: ImageFit = ImageFit.Cover
  @Local private isFinish: boolean = false

  build() {
    ListItem() {
      Column() {
        RelativeContainer() {
          Stack() {
            Image(this.data.cover_url ?? this.defaultImg)
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


          Text(`${this.data.songs_count}首 `)
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
            Text(this.data.name)
              .fontSize(15)
              .maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
              .fontWeight(FontWeight.Medium)
              .textAlign(TextAlign.Start)
          }.height(32)

        }.width('100%')
        .padding({
          top: 8,
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
