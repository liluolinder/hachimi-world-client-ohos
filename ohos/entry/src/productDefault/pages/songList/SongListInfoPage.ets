import { XAnimation } from "commonpackage"
import { min_width } from "../../../main/ets/commons/commonItem"
import { __System__ } from "../../../main/ets/commons/shareDatas"
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect"
import { I_GetRecentSongRes_data_song } from "../../../main/ets/models/interfaces/I_GetRecentSong"
import { I_GetSongListInfo_data } from "../../../main/ets/models/interfaces/I_GetSongListInfo"
import { curves } from "@kit.ArkUI"
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer"

@Builder
export function SongListInfoPageBuilder() {
  SongListInfoPage()
}

@ComponentV2
export struct SongListInfoPage {
  @Local songListInfo?: I_GetSongListInfo_data
  @Local ColumnNumber: number = 2
  @Local canGetMore: boolean = true
  @Local contentEndOffsetNum: number =
    __System__.windowWidth > min_width ? 10 : Math.max(__System__.bottomRectHeight, 16) + 100

  @Builder
  songListBuilder() {
    Column() {
      List({ space: 10 }) {
        ForEach(this.songListInfo?.songs, (item: I_GetRecentSongRes_data_song, index: number) => {
          ListItem() {
            songListItemCover({ data: item })
          }
          .borderRadius(10)
          .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
          .clip(true)
          .onClick(async () => {
            __musicPlayer__.playNow = item
            __musicPlayer__.playList = []
            this.songListInfo?.songs.forEach((itemA) => {
              __musicPlayer__.playList.push(itemA.song_display_id)
            })
            __musicPlayer__.playIndex = index
          })

        }, (item: I_GetRecentSongRes_data_song) => JSON.stringify(item))
      }
      .onAreaChange((_, newValue) => {
        XAnimation.runWithAnimation(() => {
          this.ColumnNumber = Math.max(Math.ceil(new Number(newValue.width).valueOf() / 600), 1)
        }, this.ColumnNumber > 0)
      })
      .padding({ left: 16, right: 16 })
      .scrollBar(BarState.Off)
      .contentStartOffset(10)
      .contentEndOffset(this.contentEndOffsetNum)
      .height('100%')
      .width('100%')
      .lanes(this.ColumnNumber, 10)
      .edgeEffect(EdgeEffect.Spring)
    }
    .layoutWeight(1)
    .size({ width: '100%', height: '100%' })
    .backgroundColor($r('sys.color.gray_01'))
    .borderRadius(20)
    .clip(true)
  }

  build() {
    NavDestination() {

      Column() {

        if (__System__.windowWidth > min_width) {
          Row({ space: 10 }) {
            Column() {
              Scroll() {

                Column() {
                  Image(this.songListInfo?.playlist_info.cover_url)
                    .width('100%')
                    .aspectRatio(1)
                    .borderRadius(20)
                    .padding(20)

                  Column({ space: 10 }) {
                    Text(this.songListInfo?.playlist_info.name)
                      .width('100%')
                      .fontSize(22)
                      .fontWeight(FontWeight.Bold)
                      .textAlign(TextAlign.Center)

                    Text(this.songListInfo?.playlist_info.description)
                      .width('100%')
                      .fontSize(18)
                      .fontWeight(FontWeight.Medium)
                  }.padding({ left: 16, right: 16 })

                }

              }
              .fadingEdge(true)
              .scrollBar(BarState.Off)
              .edgeEffect(EdgeEffect.Spring)
              .align(Alignment.Top)
              .width('100%')
              .layoutWeight(1)
              .clip(true)
            }.size({ width: 250, height: '100%' })
            .backgroundColor($r('sys.color.gray_01'))
            .borderRadius(20)

            this.songListBuilder()

          }.size({ width: '100%', height: '100%' })
          .padding({ bottom: Math.max(__System__.bottomRectHeight, 5) + 75 })

        } else {

          Column({ space: 10 }) {
            Column() {

              Row({ space: 10 }) {

                Image(this.songListInfo?.playlist_info.cover_url)
                  .size({ width: 80 })
                  .aspectRatio(1)
                  .borderRadius(20)


                Text(this.songListInfo?.playlist_info.name)
                  .fontSize(22)
                  .layoutWeight(1)
                  .fontWeight(FontWeight.Bold)
                  .textOverflow({ overflow: TextOverflow.MARQUEE })

              }.size({ width: '100%' })
              .padding({
                top: 20,
                left: 20,
                right: 20,
                bottom: 10
              })

              Scroll() {
                Text(this.songListInfo?.playlist_info.description)
                  .width('100%')
                  .fontSize(18)
                  .fontWeight(FontWeight.Medium)
              }
              .padding({ left: 16, right: 16 })
              .scrollBar(BarState.Off)
              .edgeEffect(EdgeEffect.Spring)
              .align(Alignment.Top)
              .width('100%')
              .layoutWeight(1)
              .clip(true)
            }
            .padding({ bottom: 8 })
            .size({ width: '100%', height: 200 })
            .clip(true)
            .borderRadius(20)
            .backgroundColor($r('sys.color.gray_01'))

            this.songListBuilder()
          }
          .layoutWeight(1)
          .size({ width: '100%', height: '100%' })
        }
      }.padding({ left: 16, right: 16 })

    }.padding({ top: Math.max(__System__.topRectHeight, 10) })
    .title(this.songListInfo?.playlist_info.name)
    .onReady((paramContext) => {
      this.songListInfo = paramContext.pathInfo.param as I_GetSongListInfo_data | undefined
    })
  }
}


@ComponentV2
export struct songListItemCover {
  @Param @Require data: I_GetRecentSongRes_data_song
  @Param defaultImg: ResourceStr = $r('app.media.layered_image')
  @Param imageFit: ImageFit = ImageFit.Cover
  @Local private isFinish: boolean = false

  build() {
    Column() {
      Row() {
        RelativeContainer() {
          Row() {
            Image(this.data.cover_url ?? this.defaultImg)
              .autoResize(true)
              .objectFit(this.imageFit)
              .onComplete((res) => {
                this.isFinish = true
              })
            if (!this.isFinish) {
              Image(this.defaultImg)
                .autoResize(true)
                .transition(TransitionEffect.OPACITY.animation({ duration: 150, curve: Curve.Ease }))
                .objectFit(ImageFit.Cover)
            }
          }
          .height(120)
          .enabled(false)
          .aspectRatio(3 / 2)

        }.aspectRatio(3 / 2)

        Column() {
          Column({ space: 5 }) {
            Text(this.data.title)
              .fontSize(15)
              .maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
              .fontWeight(FontWeight.Medium)
              .textAlign(TextAlign.Start)


            Row({ space: 5 }) {
              Text(this.data.uploader_name)
                .fontSize(12)
                .maxLines(1)
                .opacity(0.8)
                .layoutWeight(1)
                .textOverflow({ overflow: TextOverflow.Ellipsis })
            }
          }.alignItems(HorizontalAlign.Start)

        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Start)
        .justifyContent(FlexAlign.SpaceBetween)
        .padding(10)
        .height('100%')
      }

    }
    .alignItems(HorizontalAlign.Start)
    .width('100%')
    .height(120)
    .attributeModifier(backgroundBlurModifier)
    .transition(TransitionEffect.OPACITY
      .combine(TransitionEffect.scale({ x: 0.5, y: 0.5 }))
      .animation({ curve: curves.springMotion(0.5, 0.7) }))
    .width('100%')
    .clip(true)
    .borderRadius(10)
  }
}
