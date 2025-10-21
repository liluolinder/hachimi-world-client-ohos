import { StringFormat } from "commonpackage";
import { minPlayerWidth, min_width } from "../../../main/ets/commons/commonItem";
import { __System__ } from "../../../main/ets/commons/shareDatas";
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer";
import WindowsUtil from "../../../main/ets/models/classes/WindowsUtil";

@ComponentV2
export struct Player {
  @Local ImageMarginTop: number = 0

  @Builder
  songInfo() {
    Column() {
      Image(__musicPlayer__.playNow?.cover_url ?? $r('app.media.linder'))
        .size({ width: '60%', height: '60%' })
        .aspectRatio(1)
        .constraintSize({
          maxWidth: 350,
          maxHeight: 350
        })
        .borderRadius(20)
        .margin({ top: this.ImageMarginTop })

      Column({space:10}) {
        Text(__musicPlayer__.playNow?.title ?? '暂未播放')
          .fontWeight(FontWeight.Bolder)
          .fontSize(24)
          .textAlign(TextAlign.Center)
          .size({ width: '100%' })
          .maxLines(2)
        Text(__musicPlayer__.playNow?.uploader_name ?? '')
          .fontWeight(FontWeight.Normal)
          .fontSize(16)
          .textAlign(TextAlign.Center)
          .size({ width: '100%' })
          .maxLines(1)

      }.size({ width: '60%' })
      .margin({ top: 20 })

      Row({ space: 10 }) {
        Text(StringFormat.formatMilliseconds(__musicPlayer__.currentTime))
        Slider({
          value: __musicPlayer__.currentTime,
          min: 0,
          max: __musicPlayer__.durationTime ?? 0,
          style: SliderStyle.NONE
        })
          .size({ width: '60%' })
          .trackThickness(10)
          .selectedColor($r('sys.color.font'))
          .backgroundColor($r('sys.color.comp_background_gray'))
          .onChange((value: number, mode: SliderChangeMode) => {
            __musicPlayer__.Seek(value)
          })
        Text(StringFormat.formatMilliseconds(__musicPlayer__.durationTime))
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .margin({ top: 30 })

      Row({ space: 10 }) {
        Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
          SymbolGlyph($r('sys.symbol.backward_end_fill'))
            .fontSize(60)
            .fontColor([$r('sys.color.font')])
        }
        .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
        .onClick(() => {
          __musicPlayer__.Last()
        })

        Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
          SymbolGlyph(__musicPlayer__.isPlaying ? $r('sys.symbol.pause') : $r('sys.symbol.play_fill'))
            .fontSize(60)
            .fontColor([$r('sys.color.font')])
        }
        .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
        .onClick(() => {
          __musicPlayer__.isPlaying ? __musicPlayer__.Pause() : __musicPlayer__.Play()
        })

        Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
          SymbolGlyph($r('sys.symbol.forward_end_fill'))
            .fontSize(60)
            .fontColor([$r('sys.color.font')])
        }
        .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
        .onClick(() => {
          __musicPlayer__.Next()
        })

      }.size({ width: '80%' })
      .justifyContent(FlexAlign.SpaceBetween)
      .margin({ top: 40 })

    }.size({ width: '100%', height: '90%' })
    .margin({bottom:40})
    .justifyContent(FlexAlign.SpaceBetween)
    .onAreaChange((_, newArea) => {
      this.ImageMarginTop = (Number(newArea.width) - Number(newArea.width) * 0.6) / 2
    })
  }

  @Builder
  lrc() {

  }

  build() {
    Column() {
      if (__System__.windowWidth > minPlayerWidth) {
        Row() {
          Column() {
            this.songInfo()
          }.size({ width: '50%', height: '100%' })

          Column() {
            this.lrc()
          }.size({ width: '50%', height: '100%' })

        }
        .size({ width: '100%', height: '100%' })
      } else {
        Swiper() {
          this.songInfo()
        }
        .size({ width: '100%', height: '100%' })
      }
    }.size({ width: '100%', height: '100%' })
    .padding({ top: __System__.pcMode ? 0 : __System__.topRectHeight })
  }
}