import { StringFormat, XAnimation } from "commonpackage";
import { minPlayerWidth, min_width } from "../../../main/ets/commons/commonItem";
import { __PlayerUIData__, __System__, __UI__ } from "../../../main/ets/commons/shareDatas";
import { __Client__ } from "../../../main/ets/models/classes/hachimi";
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer";
import { ImageModifier } from "@kit.ArkUI";
import { I_GetSongRes_data_song } from "../../../main/ets/models/interfaces/I_GetSong";

@Builder
export function playerBuilder() {
  Player()
}

@ComponentV2
export struct Player {
  @Local ImageMarginTop: number = 0
  @Local infoWidth: number = 0
  @Local infoHeight: number = 0
  @Provider('lrcController') lrcController: ListScroller = new ListScroller()

  @Builder
  musicController() {
    Row({ space: 10 }) {
      Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
        SymbolGlyph($r('sys.symbol.backward_end_fill'))
          .fontSize(60)
          .fontColor([Color.White])
      }
      .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
      .onClick(() => {
        __musicPlayer__.Last()
      })
      .geometryTransition('playerPreviousButton')
      .transition(TransitionEffect.OPACITY)

      Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
        SymbolGlyph(__musicPlayer__.isPlaying ? $r('sys.symbol.pause') : $r('sys.symbol.play_fill'))
          .fontSize(60)
          .fontColor([Color.White])
      }
      .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
      .onClick(() => {
        __musicPlayer__.isPlaying ? __musicPlayer__.Pause() : __musicPlayer__.Play()
      })
      .geometryTransition('playerPlayPauseButton')
      .transition(TransitionEffect.OPACITY)

      Button({ type: ButtonType.ROUNDED_RECTANGLE }) {
        SymbolGlyph($r('sys.symbol.forward_end_fill'))
          .fontSize(60)
          .fontColor([Color.White])
      }
      .backgroundColor($r('sys.color.ohos_id_color_background_transparent'))
      .onClick(() => {
        __musicPlayer__.Next()
      })
      .geometryTransition('playerNextButton')
      .transition(TransitionEffect.OPACITY)

    }
    .transition(TransitionEffect.OPACITY)
    .size({ width: '100%' })
    .padding({ left: 32, right: 32 })
    .justifyContent(FlexAlign.SpaceBetween)

  }

  @Builder
  songTitleWithUPer(){
    Column() {
      Text(__musicPlayer__.playNow?.title ?? '暂未播放')
        .fontWeight(FontWeight.Bolder)
        .fontSize(28)
        .textAlign(TextAlign.Center)
        .size({ width: '100%' })
        .maxLines(2)
        .geometryTransition('playerTitleText', { follow: true })
        .transition(TransitionEffect.OPACITY)
        .fontColor(Color.White)

      Text(__musicPlayer__.playNow?.subtitle)
        .fontWeight(FontWeight.Bolder)
        .fontSize(28)
        .textAlign(TextAlign.Center)
        .size({ width: '100%' })
        .maxLines(1)
        .transition(TransitionEffect.OPACITY)
        .fontColor(Color.White)

      Text(__musicPlayer__.playNow?.uploader_name ?? '')
        .fontWeight(FontWeight.Normal)
        .fontSize(18)
        .textAlign(TextAlign.Center)
        .size({ width: '100%' })
        .maxLines(1)
        .geometryTransition('playerArtistsText', { follow: true })
        .transition(TransitionEffect.OPACITY)
        .fontColor(Color.White)


      //TODO:点击打开半模态展示完整信息
      if (__musicPlayer__.playNow?.origin_infos && __musicPlayer__.playNow?.origin_infos != null && __musicPlayer__.playNow?.origin_infos[0] != undefined) {
        Text("原作:" + __musicPlayer__.playNow?.origin_infos[0].title??'')
      }


      //TODO:只展示存在MV，点击后可以跳转B站
      if (__musicPlayer__.playNow?.external_links && __musicPlayer__.playNow?.external_links!=null )
      {
        Text("MV")
      }


    }.size({ width: '100%' })
    .transition(TransitionEffect.OPACITY)
  }

  @Builder
  songInfo() {
    Column() {
      Column() {
        if (this.infoHeight <= 600) {
          Column() {
            Image(__musicPlayer__.playNow?.cover_url ?? $r('app.media.linder'))
              .layoutWeight(__System__.windowWidth <= minPlayerWidth ? 1 : undefined)
              .size({
                width: (__System__.windowWidth <= minPlayerWidth) ? undefined :
                  (this.infoWidth <= this.infoHeight) ? '70%' : undefined,
                height: (__System__.windowWidth <= minPlayerWidth) ? undefined :
                  (this.infoWidth > this.infoHeight) ? '70%' : undefined
              })
              .borderRadius(20)
              .aspectRatio(1)
              .geometryTransition('playerCoverImage', { follow: true })
              .transition(TransitionEffect.OPACITY)

          }.layoutWeight(1)
          .justifyContent(FlexAlign.Center)
          .alignItems(HorizontalAlign.Center)
          .size({ width: '100%', height: '100%' })
        } else if (this.infoHeight <= 700) {
          Column() {

            this.songTitleWithUPer()

            Image(__musicPlayer__.playNow?.cover_url ?? $r('app.media.linder'))
              .borderRadius(20)
              .layoutWeight(0.5)
              .geometryTransition('playerCoverImage', { follow: true })
              .transition(TransitionEffect.OPACITY)
              .aspectRatio(1)
              .constraintSize({maxWidth:500, maxHeight:500})
              .size({
                width: this.infoWidth < this.infoHeight ? '70%' : undefined,
                height: this.infoWidth > this.infoHeight ? '70%' : undefined
              })


            if (this.infoWidth <= 500) {
              this.musicController()
            }


          }.size({ width: '100%', height: '100%' })
          .padding({ top: __System__.topRectHeight })
          .justifyContent(FlexAlign.SpaceEvenly)

        } else {
          Column() {
            Column({space:10}){
              Image(__musicPlayer__.playNow?.cover_url ?? $r('app.media.linder'))
                .constraintSize({maxWidth:400, maxHeight:400})
                .borderRadius(20)
                .geometryTransition('playerCoverImage', { follow: true })
                .transition(TransitionEffect.OPACITY)
                .aspectRatio(1)

              this.songTitleWithUPer()
            }.offset({top:Math.min(this.ImageMarginTop,40)})
            .constraintSize({ maxHeight:430})
            .size({ width: '100%' })
            .padding({left:16, right:16})


            Column({space:20}) {
              Row({ space: 10 }) {
                Text(StringFormat.formatMilliseconds(__musicPlayer__.currentTime))
                  .fontColor(Color.White)
                Slider({
                  value: __musicPlayer__.currentTime,
                  min: 0,
                  max: __musicPlayer__.durationTime ?? 0,
                  style: SliderStyle.NONE
                })
                  .layoutWeight(1)
                  .trackThickness(10)
                  .selectedColor(Color.White)
                  .onChange((value: number, mode: SliderChangeMode) => {
                    __musicPlayer__.Seek(value)
                  })
                Text(StringFormat.formatMilliseconds(__musicPlayer__.durationTime))
                  .fontColor(Color.White)
              }
              .size({ width: '100%' })
              .padding({ left: 32, right: 32 })
              .justifyContent(FlexAlign.Center)
              this.musicController()
            }.offset({bottom:50})
          }
          .size({ width: '100%', height: '100%' })
          .padding({ top: __System__.topRectHeight })
          .justifyContent(FlexAlign.SpaceBetween)
        }


      }
    }.size({ width: '100%', height: '100%' })
    .onAreaChange((_, newArea) => {
      this.infoWidth = Number(newArea.width)
      this.infoHeight = Number(newArea.height)
      this.ImageMarginTop = (Number(newArea.width) - Number(newArea.width) * 0.7) / 2
      console.log(this.ImageMarginTop.toString())
      console.log(`信息组件${JSON.stringify(newArea)}`);
    })
  }

  @Builder
  lrc() {
    Column() {
      if (__musicPlayer__.playNow?.lyrics != null && __musicPlayer__.playNow.lyrics != '') {
        List({ space: 50, scroller: this.lrcController }) {
          ForEach(__musicPlayer__.playNow.lyrics.replaceAll('\r', '').split('\n'), (item: string, index: number) => {
            lrcItem({ lrcString: item, index: index })
          }, (item: string) => item)
        }.layoutWeight(1)
        .contentEndOffset(__System__.bottomRectHeight)
      } else {
        Text('暂无歌词')
          .fontSize(26)
          .fontColor(Color.Blue)
          .fontWeight(FontWeight.Bold)
      }
    }.padding({ top: __System__.topRectHeight })
    .justifyContent(FlexAlign.Center)
    .size({ width: '100%', height: '100%' })
  }

  build() {
    NavDestination() {
      Stack() {
        Image(__musicPlayer__.playNow?.cover_url ?? $r('app.media.linder'))
          .width('100%')
          .height('100%')
          .blur(150)
          .geometryTransition('playerBackground', { follow: true })
          .transition(TransitionEffect.OPACITY)
          .brightness(0.9)
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
            this.lrc()
          }
          .size({ width: '100%', height: '100%' })
        }
      }.size({ width: '100%', height: '100%' })
      .onAreaChange((_, newArea) => {
        console.log(`最大组件${JSON.stringify(newArea)}`);
      })
    }
    .hideTitleBar(true)
    .transition(TransitionEffect.OPACITY.combine(TransitionEffect.translate({ y: __System__.windowHeight })))
    .onBackPressed(() => {
      XAnimation.runWithAnimation(() => {
        __UI__.mainPageStack.pop(false)
      })
      return true
    })
  }
}

@ComponentV2
export struct lrcItem {
  @Param @Require lrcString: string
  @Param @Require index: number
  @Local time: number = -1
  @Local lrc: string = ''
  @Consumer('lrcController') lrcController: ListScroller = new ListScroller()

  aboutToAppear(): void {
    const LYRIC_REGEX = /(?:\[(\d{2}):(\d{2})\.(\d{3})\])?(.+)/
    const match = this.lrcString.match(LYRIC_REGEX);
    if (match) {
      const _ = match[0];
      const min = match[1];
      const sec = match[2];
      const ms = match[3];
      const lyricText = match[4];
      const trimmedLyric = lyricText.trim();
      // 无时间戳的歌词行
      if (!min && !sec && !ms) {
        this.time = -1;
        this.lrc = trimmedLyric;
      } else {
        this.time = StringFormat.convertToMilliseconds(min, sec, ms);
        this.lrc = trimmedLyric;
      }
    } else {
      this.time = -1;
      this.lrc = this.lrcString;
    }
  }

  changeLrcColor(): ResourceStr | Color {

    if (__PlayerUIData__.lrcIndex <= this.index) {
      __PlayerUIData__.lrcIndex = this.index
      this.lrcController.scrollToIndex(this.index, true, ScrollAlign.CENTER)
      return Color.Blue
    } else {
      return Color.White
    }

  }

  build() {
    ListItem() {
      Text(this.time + this.lrc)
        .fontSize(28)
        .fontColor(__musicPlayer__.currentTime >= this.time && this.time >= 0 ? this.changeLrcColor() : Color.White)
        .fontWeight(FontWeight.Bolder)
        .width('100%')
        .textAlign(TextAlign.Center)
    }.width('100%')
    .onClick(() => {
      if (this.time != -1) {
        __musicPlayer__.currentTime = this.time
        __PlayerUIData__.lrcIndex = this.index
        __musicPlayer__.Seek(this.time)
        this.lrcController.scrollToIndex(this.index, true, ScrollAlign.CENTER)
      }
    })
  }
}