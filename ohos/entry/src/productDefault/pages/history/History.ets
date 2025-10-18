import { StringFormat, XAnimation, XContext } from "commonpackage"
import { SalmonSegmentButton } from "commonpackage/src/main/ets/components/SalmonSegmentButton"
import { __System__, __UI__, __User__ } from "../../../main/ets/commons/shareDatas"
import { backgroundBlurModifier } from "../../../main/ets/models/classes/backGroundEffect"
import { client } from "../../../main/ets/models/classes/pilipala"
import {
  I_GetHistoryRes_data_cursor,
  I_GetHistoryRes_data_listObj
} from "../../../main/ets/models/interfaces/I_GetHistory"
import { http } from "@kit.NetworkKit"
import { S_PlayerParam } from "../../../main/ets/models/interfaces/S_NavParams"

@ObservedV2
export class HistoryData {
  requestType: 'all' | 'archive' | 'live' | 'article' = 'all'
  lastRequestCursor?: I_GetHistoryRes_data_cursor
  req = http.createHttp()
}

export const __HistoryData__ = new HistoryData()

@ComponentV2
export struct History {
  @Local ColumnNumber: number = -1
  @Local canGetMore: boolean = true

  build() {
    if(!__User__.isLogin){
      Stack(){
        Text('还没有登录').fontSize(32).fontWeight(FontWeight.Medium)
      }.size({ width: '100%', height: '100%' })
    }else {
      Stack({ alignContent: Alignment.Top }) {
        Row({ space: 10 }) {
          SalmonSegmentButton({
            buttonList: ['全部', '视频', '直播', '专栏'],
            index: 0,
            onChange: async (index) => {
              client.history.clearData()
              if (index == 0) {
                __HistoryData__.requestType = 'all'
              } else if (index == 1) {
                __HistoryData__.requestType = 'archive'
              } else if (index == 2) {
                __HistoryData__.requestType = 'live'
              } else if (index == 3) {
                __HistoryData__.requestType = 'article'
              }
              __HistoryData__.req.destroy()
              __HistoryData__.req = http.createHttp()
              __HistoryData__.lastRequestCursor = await client.getHistory(__HistoryData__.req,{ type: __HistoryData__.requestType })
            },
            modifier: backgroundBlurModifier
          })
            .height(40)


        }
        .zIndex(1)
        .justifyContent(FlexAlign.End)
        .width('100%')
        .padding({ top: Math.max(10, __System__.topRectHeight), left: 16, right: 16 })
        .transition(XAnimation.getAnimation('up'))
        .zIndex(114514)

        List({ space: 10 }) {
          LazyForEach(client.history, (item: I_GetHistoryRes_data_listObj, index: number) => {

            ListItem(){
              historyCover({ data: item })
            }
            .borderRadius(10)
            .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
            .clip(true)
            .swipeAction({
              end: {
                builder: () => { this.itemEnd(item, index) }
              }
            })


          }, (item: I_GetHistoryRes_data_listObj) => JSON.stringify(item))
        }

        .padding({ left: 16, right: 16 })
        .scrollBar(BarState.Off)
        .contentStartOffset(Math.max(10, __System__.topRectHeight) + 60)
        .contentEndOffset(Math.max(__System__.bottomRectHeight, 16))
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
          if (__HistoryData__.lastRequestCursor?.max != 0 && __HistoryData__.lastRequestCursor?.view_at != 0 && client.history.dataArray.length!=0) {
            __HistoryData__.req.destroy()
            __HistoryData__.req = http.createHttp()
            __HistoryData__.lastRequestCursor = await client.getHistory(__HistoryData__.req,{
              max: __HistoryData__.lastRequestCursor?.max,
              business: __HistoryData__.lastRequestCursor?.business,
              view_at: __HistoryData__.lastRequestCursor?.view_at,
              type: __HistoryData__.requestType
            })
          }

        })

      }
      .size({ width: '100%', height: '100%' })
    }
  }

  @Builder
  itemEnd(data: I_GetHistoryRes_data_listObj, index: number) {
      Button({type: ButtonType.Normal, stateEffect: true}) {
        SymbolGlyph($r('sys.symbol.trash')).fontSize(30).fontColor([Color.White])
      }
      .backgroundColor($r('sys.color.warning'))
      .borderRadius(10)
        .height('100%')
      .constraintSize({minWidth: 60, maxWidth: '100%'})
        .onClick(async ()=>{
          const code = await client.deleteHistory(data.history.oid, data.history.business)
          if(code == 0){
            XContext.toast('已删除')
            client.history.notifyDataDelete(index)
          }
        })
        .margin({left: 10})
  }
}

@ComponentV2
export struct historyCover {
  @Param @Require data: I_GetHistoryRes_data_listObj
  @Param defaultImg: ResourceStr = $r('app.media.default_videoCover')
  @Param imageFit: ImageFit = ImageFit.Cover
  @Local private isFinish: boolean = false

  build() {
      Column() {
        Row() {
          RelativeContainer() {
            Row() {
              Image(this.data.cover ?? this.defaultImg)
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
            }
            .height(120)
            .enabled(false)
            .aspectRatio(3 / 2)


            Text(this.data.progress == -1 ? '已看完' :
              `${StringFormat.formatSeconds(this.data.progress)}/${StringFormat.formatSeconds(this.data.duration)}`)
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

          }.aspectRatio(3 / 2)

          Column() {
              Column({space: 5}){
                Text(this.data.title)
                  .fontSize(15)
                  .maxLines(2)
                  .textOverflow({ overflow: TextOverflow.Ellipsis })
                  .fontWeight(FontWeight.Medium)
                  .textAlign(TextAlign.Start)
                //番剧展示 xxx话
                if(this.data.show_title){
                  Text(this.data.show_title)
                    .fontSize(14)
                    .maxLines(1)
                    .fontWeight(FontWeight.Medium)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })
                }
                Row({space: 5}) {
                  Image(this.data.author_face)
                    .size({ width: 15, height: 15 })
                    .borderRadius(1000)
                  Text(this.data.author_name)
                    .fontSize(12)
                    .maxLines(1)
                    .opacity(0.8)
                    .layoutWeight(1)
                    .textOverflow({overflow: TextOverflow.Ellipsis})
                }
              }.alignItems(HorizontalAlign.Start)


            Text(StringFormat.formatTimestamp(this.data.view_at)).fontSize(14).opacity(0.8)
          }.layoutWeight(1).alignItems(HorizontalAlign.Start).justifyContent(FlexAlign.SpaceBetween).padding(10).height('100%')
        }

      }.alignItems(HorizontalAlign.Start)
      .width('100%')
      .height(120)
    .attributeModifier(backgroundBlurModifier)
    .onClick(async () => {
      __UI__.mainPageStack.pushPathByName('player',
        { bvid: this.data.history.bvid, cid: this.data.history.cid, page: this.data.history.page, startDuration: this.data.progress * 1000 } as S_PlayerParam)
    })
    .transition(TransitionEffect.OPACITY.animation({ duration: 150, curve: Curve.Ease }))
    .width('100%')
    .clip(true)
    .borderRadius(10)
  }


}
