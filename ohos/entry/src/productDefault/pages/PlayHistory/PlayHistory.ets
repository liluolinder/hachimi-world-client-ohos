import { StringFormat, XAnimation, XContext } from "commonpackage"
import { __System__, __UI__, __User__ } from "../../../main/ets/commons/shareDatas"
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect"
import { __Client__ } from "../../../main/ets/models/classes/hachimi"
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer"
import { I_GetHistoryRes_Data_Item } from "../../../main/ets/models/interfaces/I_GetHistory"


@ObservedV2
export class HistoryData {

}

export const __HistoryData__ = new HistoryData()

@ComponentV2
export struct PlayHistory {
  @Local ColumnNumber: number = -1
  @Local canGetMore: boolean = true


  build() {
    if(!__User__.isLogin){
      Stack(){
        Text('还没有登录').fontSize(32).fontWeight(FontWeight.Medium)
      }.size({ width: '100%', height: '100%' })
    }else {
      Stack({ alignContent: Alignment.Top }) {
        List({ space: 10 }) {
          LazyForEach(__Client__.playHistoryArray, (item: I_GetHistoryRes_Data_Item, index: number) => {
            ListItem(){
              historyCover({ data: item })
            }
            .borderRadius(10)
            .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
            .clip(true)
            .onClick(async () => {
              __musicPlayer__.playNow = item.song_info
              __musicPlayer__.playList = []
              __Client__.playHistoryArray.dataArray.forEach((itemA)=>{
                __musicPlayer__.playList.push(itemA.song_info.display_id)
              })
              __musicPlayer__.playIndex = index
            })


          }, (item: I_GetHistoryRes_Data_Item) => JSON.stringify(item))
        }
        .padding({ left: 16, right: 16 })
        .scrollBar(BarState.Off)
        .contentStartOffset(Math.max(10, __System__.topRectHeight))
        .contentEndOffset(Math.max(__System__.bottomRectHeight, 16) + 118)
        .onAreaChange((_, newValue) => {
          XAnimation.runWithAnimation(() => {
            this.ColumnNumber = Math.max(Math.ceil(new Number(newValue.width).valueOf() / 700), 1)
          }, this.ColumnNumber > 0)
        })
        .lanes(this.ColumnNumber, 10)
        .layoutWeight(1)
        .cachedCount(this.ColumnNumber * 2)
        .edgeEffect(EdgeEffect.Spring)
        .onReachEnd(async () => {
          if (__Client__.playHistoryArray.dataArray.length!=0 && this.canGetMore) {
            this.canGetMore  = false
            __Client__.getHistory(__Client__.playHistoryArray.dataArray[__Client__.playHistoryArray.dataArray.length-1].play_time)
            this.canGetMore  = true
          }
        })

      }
      .size({ width: '100%', height: '100%' })
    }
  }

}

@ComponentV2
export struct historyCover {
  @Param @Require data: I_GetHistoryRes_Data_Item
  @Param defaultImg: ResourceStr = $r('app.media.layered_image')
  @Param imageFit: ImageFit = ImageFit.Cover
  @Local private isFinish: boolean = false

  build() {
    Column() {
      Row() {
        RelativeContainer() {
          Row() {
            Image(this.data.song_info.cover_url ?? this.defaultImg)
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
          Column({space: 5}){
            Text(this.data.song_info.title)
              .fontSize(15)
              .maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
              .fontWeight(FontWeight.Medium)
              .textAlign(TextAlign.Start)


            Row({space: 5}) {
              Text(this.data.song_info.uploader_name)
                .fontSize(12)
                .maxLines(1)
                .opacity(0.8)
                .layoutWeight(1)
                .textOverflow({overflow: TextOverflow.Ellipsis})
            }
          }.alignItems(HorizontalAlign.Start)


         // Text(StringFormat.formatTimestamp(this.data)).fontSize(14).opacity(0.8)
        }.layoutWeight(1).alignItems(HorizontalAlign.Start).justifyContent(FlexAlign.SpaceBetween).padding(10).height('100%')
      }

    }.alignItems(HorizontalAlign.Start)
    .width('100%')
    .height(120)
    .attributeModifier(backgroundBlurModifier)
    .transition(TransitionEffect.OPACITY.animation({ duration: 150, curve: Curve.Ease }))
    .width('100%')
    .clip(true)
    .borderRadius(10)
  }


}
