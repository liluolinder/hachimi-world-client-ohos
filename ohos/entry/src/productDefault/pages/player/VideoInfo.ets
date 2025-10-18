import { BasicDataSource, XAnimation, StringFormat, XContext } from 'commonpackage'
import { SalmonSegmentButton } from 'commonpackage/src/main/ets/components/SalmonSegmentButton'
import { __System__ } from '../../../main/ets/commons/shareDatas'
import { backgroundBlurModifierAuto } from '../../../main/ets/models/classes/backGroundEffect'
import { client } from '../../../main/ets/models/classes/pilipala'
import { I_GetCommentRes_data_reply, I_GetCommentRes_data } from '../../../main/ets/models/interfaces/I_GetComment'
import {
  I_GetVideoInfo_data,
  I_GetVideoInfo_data_descV2,
  I_GetVideoInfo_data_pages
} from '../../../main/ets/models/interfaces/I_GetVideoInfo'
import { I_GetVideoPlayerUrl_data } from '../../../main/ets/models/interfaces/I_GetVideoPlayerUrl'
import json from '@ohos.util.json'
import { S_VideoQualityInfoList, S_VideoQualityItem } from '../../../main/ets/models/interfaces/S_QualityItem'
import { liquidDuaPlayer } from '../../../main/ets/models/classes/liquidDuaPlayer'
import { systemShare } from '@kit.ShareKit'
import { uniformTypeDescriptor } from '@kit.ArkData'

@ComponentV2
export struct VideoInfo {
  @Consumer('videoInfo') videoInfo ?: I_GetVideoInfo_data
  @Consumer('playerInfo') playerInfo ?: I_GetVideoPlayerUrl_data
  @Consumer('commentArray') commentArray: BasicDataSource<I_GetCommentRes_data_reply> =
    new BasicDataSource<I_GetCommentRes_data_reply>()
  @Local videoInfoIndex: number = 0
  swiperController: SwiperController = new SwiperController()
  @Consumer('isLandscapeLayout') isLandscapeLayout: boolean = false
  @Consumer('hideVideoInfo') hideVideoInfo: boolean = false
  currentPage: number = 1
  @Local isLoading: boolean = false
  @Local isBottom: boolean = false
  pageScroller: ListScroller = new ListScroller()
  @Local isLoadingPage: boolean = false
  @Consumer('currentVideoPage') currentVideoPage : number = 0
  @Consumer('player') player: liquidDuaPlayer = new liquidDuaPlayer()
  @Consumer('videoQualityList') videoQualityList: S_VideoQualityItem[] = []
  @Consumer('currentVideoQuality') currentVideoQuality: number = -1

  build() {
    Column({ space: 10 }) {

      Row({ space: 8 }) {
        if (this.isLandscapeLayout) {
          Button({ type: ButtonType.Circle, stateEffect: true }) {
            SymbolGlyph($r('sys.symbol.close_sidebar')).fontSize(20).fontColor([$r('sys.color.font')])
          }
          .size({ width: 34, height: 34 })
          .backgroundColor($r('sys.color.comp_background_primary'))
          .onClick(() => {
            XAnimation.runWithAnimation(() => this.hideVideoInfo = true)
          })
        }

        SalmonSegmentButton({
          buttonList: ['详情', '评论'],
          index: this.videoInfoIndex,
          onChange: (index) => {
            this.swiperController.changeIndex(index, true)
          },
          modifier: backgroundBlurModifierAuto
        })
          .layoutWeight(1)
      }
      .height(35)
      .padding({ left: 10, right: 10 })
      .width('100%')

      Swiper(this.swiperController) {
        List({ space: 10 }) {
          ListItem() {
            Row({ space: 10 }) {
              Image(this.videoInfo?.owner.face)
                .size({ width: 50, height: 50 })
                .borderRadius(1000)

              Column() {
                Text(this.videoInfo?.owner.name)
                  .fontSize(20)
                  .fontWeight(FontWeight.Medium)
                  .maxLines(1)
                  .textOverflow({ overflow: TextOverflow.MARQUEE })
                Column() {
                  Text(StringFormat.formatTimestamp(this.videoInfo?.pubdate ?? 0)).fontSize(12).opacity(0.8)
                  Text(this.videoInfo?.bvid).fontSize(10).opacity(0.6)
                }.alignItems(HorizontalAlign.Start)
              }.height('100%').alignItems(HorizontalAlign.Start).justifyContent(FlexAlign.SpaceBetween)
            }
            .width('100%')
            .padding(10)
            .height(80)
            .attributeModifier(backgroundBlurModifierAuto)
            .borderRadius(20)
          }

          ListItem() {
            List({ space: 10 }) {
              ListItem() {
                Button({ type: ButtonType.Normal, stateEffect: true }) {
                  Column({ space: 5 }) {
                    SymbolGlyph($r('sys.symbol.hand_thumbsup')).fontSize(22).fontColor([$r('sys.color.font')])
                    Text(this.videoInfo?.stat.like.toString())
                      .fontSize(11)
                      .fontColor($r('sys.color.font'))
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.MARQUEE })
                  }.opacity(0.8)
                }
                .padding(10)
                .backgroundColor(Color.Transparent)
              }
              .borderRadius(20)
              .clip(true)
              .constraintSize({ minWidth: 75 })
              .attributeModifier(backgroundBlurModifierAuto)
              .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.9 })
              .onClick(() => {
                //点赞
              })

              ListItem() {
                Button({ type: ButtonType.Normal, stateEffect: true }) {
                  Column({ space: 5 }) {
                    SymbolGlyph($r('sys.symbol.hand_thumbsdown')).fontSize(22).fontColor([$r('sys.color.font')])
                    Text('不喜欢')
                      .fontSize(11)
                      .fontColor($r('sys.color.font'))
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.MARQUEE })
                  }.opacity(0.8)
                }
                .padding(10)
                .backgroundColor(Color.Transparent)
                .width(75)
              }
              .borderRadius(20)
              .clip(true)
              .attributeModifier(backgroundBlurModifierAuto)
              .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.9 })
              .onClick(() => {
                //不喜欢
              })

              ListItem() {
                Button({ type: ButtonType.Normal, stateEffect: true }) {
                  Column({ space: 5 }) {
                    SymbolGlyph($r('sys.symbol.moneybag')).fontSize(22).fontColor([$r('sys.color.font')])
                    Text(this.videoInfo?.stat.coin.toString())
                      .fontSize(11)
                      .fontColor($r('sys.color.font'))
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.MARQUEE })
                  }.opacity(0.8)
                }
                .padding(10)
                .backgroundColor(Color.Transparent)
                .width(75)
              }
              .borderRadius(20)
              .clip(true)
              .attributeModifier(backgroundBlurModifierAuto)
              .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.9 })
              .onClick(() => {
                //投币
              })

              ListItem() {
                Button({ type: ButtonType.Normal, stateEffect: true }) {
                  Column({ space: 5 }) {
                    SymbolGlyph($r('sys.symbol.star')).fontSize(22).fontColor([$r('sys.color.font')])
                    Text(this.videoInfo?.stat.favorite.toString())
                      .fontSize(11)
                      .fontColor($r('sys.color.font'))
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.MARQUEE })
                  }.opacity(0.8)
                }
                .padding(10)
                .backgroundColor(Color.Transparent)
                .width(75)
              }
              .borderRadius(20)
              .clip(true)
              .attributeModifier(backgroundBlurModifierAuto)
              .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.9 })
              .onClick(() => {
                //收藏
              })

              ListItem() {
                Button({ type: ButtonType.Normal, stateEffect: true }) {
                  Column({ space: 5 }) {
                    SymbolGlyph($r('sys.symbol.share')).fontSize(22).fontColor([$r('sys.color.font')])
                    Text(this.videoInfo?.stat.share.toString())
                      .fontSize(11)
                      .fontColor($r('sys.color.font'))
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.MARQUEE })
                  }.opacity(0.8)
                }
                .padding(10)
                .backgroundColor(Color.Transparent)
                .width(75)
              }
              .borderRadius(20)
              .clip(true)
              .attributeModifier(backgroundBlurModifierAuto)
              .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.9 })
              .onClick(() => {
                let data: systemShare.SharedData = new systemShare.SharedData({
                  utd: uniformTypeDescriptor.UniformDataType.TEXT,
                  content: `https://www.bilibili.com/video/${this.videoInfo?.bvid}`,
                  title: this.videoInfo?.title,
                  description: this.videoInfo?.owner.name,
                  thumbnailUri: this.videoInfo?.pic,
                });
                let controller: systemShare.ShareController = new systemShare.ShareController(data)
                controller.show(XContext.getAbilityContext(), {
                  previewMode: systemShare.SharePreviewMode.DEFAULT,
                  selectionMode: systemShare.SelectionMode.SINGLE
                });
              })

            }
            .width('100%')
            .listDirection(Axis.Horizontal)
            .scrollBar(BarState.Off)
            .fadingEdge(true)
            .chainAnimation(true)

          }.width('100%')

          if ((this.videoInfo?.pages.length ?? 0) > 1) {
            ListItem() {
              List({ space: 10, initialIndex: this.currentVideoPage - 1, scroller: this.pageScroller }) {
                ForEach(this.videoInfo?.pages, (item: I_GetVideoInfo_data_pages, index: number) => {
                  ListItem() {
                    Button({ type: ButtonType.Normal, stateEffect: true }) {
                      Flex({
                        justifyContent: FlexAlign.SpaceBetween,
                        alignItems: ItemAlign.Center,
                        space: { main: { value: 20, unit: undefined } }
                      }) {
                        Text(item.part)
                          .maxLines(2)
                          .textOverflow({ overflow: TextOverflow.Ellipsis })
                          .fontSize(16)
                          .fontWeight(FontWeight.Medium)
                        SymbolGlyph($r('sys.symbol.chevron_right'))
                          .fontColor([$r('sys.color.font')])
                          .fontSize(20)
                          .opacity(0.8)
                      }
                      .backgroundColor(index == this.currentVideoPage - 1 ? $r('app.color.list_item_background_highlight') :
                        Color.Transparent)
                      .animation({ duration: 150 })
                      .borderRadius(18)
                      .padding({ left: 15, right: 15 })
                      .height(55)
                    }
                    .enabled(!this.isLoadingPage)
                    .padding(2)
                    .backgroundColor(Color.Transparent)
                    .borderRadius(20)
                    .attributeModifier(backgroundBlurModifierAuto)
                    .clip(true)
                    .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
                    .constraintSize({ maxWidth: '25%', minWidth: 200 })
                    .onClick(async () => {
                      this.isLoadingPage = true
                      await this.player.pause()
                      this.playerInfo =
                        await client.getVideoPlayerUrl(this.videoInfo!.bvid, item.cid.toString()) ?? undefined
                      this.videoQualityList = []
                      this.playerInfo!.dash.video.forEach((item)=>{
                        if(!item.codecs.includes('av01') && !this.videoQualityList.find(i => i.id == item.id)){
                          this.videoQualityList.push({ name: S_VideoQualityInfoList.find(i => i.id == item.id)?.name ?? '未知', id: item.id, codec: item.codecs, url: item.baseUrl})
                        }
                      })
                      
                      this.player.setUrlAndHead(this.playerInfo!.dash.video[0].baseUrl,
                        this.playerInfo!.dash.audio[0].baseUrl)
                      setTimeout(()=>{
                        this.isLoadingPage = false
                        this.currentVideoPage = index + 1
                        this.pageScroller.scrollToIndex(index, true, ScrollAlign.CENTER)
                      }, 500)
                    })
                  }
                })
              }.scrollBar(BarState.Off)
              .listDirection(Axis.Horizontal)
              .fadingEdge(true)
              .chainAnimation(true)
            }
          }

          if (this.videoInfo?.desc_v2) {
            ListItem() {
              Column() {

                Text() {
                  ForEach(this.videoInfo?.desc_v2, (item: I_GetVideoInfo_data_descV2) => {
                    if (item.type == 1) {
                      Span(item.raw_text)
                        .fontColor($r('sys.color.font'))
                    } else {
                      Span(`@${item.raw_text}`)
                        .fontColor(Color.Red)
                    }

                  })
                }.width('100%')
                .copyOption(CopyOptions.LocalDevice)
                .enableDataDetector(true)

              }
              .width('100%')
              .padding(10)
              .attributeModifier(backgroundBlurModifierAuto)
              .borderRadius(20)
            }
          }

        }
        .nestedScroll({
          scrollForward: NestedScrollMode.PARENT_FIRST,
          scrollBackward: NestedScrollMode.SELF_FIRST
        })
        .padding({ left: 10, right: 10 })
        .chainAnimation(true)
        .scrollBar(BarState.Off)
        .contentEndOffset(Math.max(__System__.bottomRectHeight, 10))
        .fadingEdge(true)


        List({ space: 10 }) {
          LazyForEach(this.commentArray, (item: I_GetCommentRes_data_reply) => {
            ListItem() {
              Column({ space: 10 }) {
                Row({ space: 6 }) {
                  Image(item.member.avatar)
                    .size({ width: 25, height: 25 })
                    .borderRadius(1000)
                  Text(item.member.uname)
                    .fontSize(16)
                    .fontWeight(FontWeight.Medium)
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })

                  //等级
                  Text(`LV ${item.member.level_info.current_level}`)
                    .fontSize(10)
                    .padding({
                      left: 3,
                      right: 3,
                      top: 1,
                      bottom: 1
                    })
                    .backgroundColor($r('app.color.bilibili_pink'))
                    .opacity(0.7)
                    .borderRadius(3)
                    .fontColor(Color.White)
                    .maxLines(1)

                  if (item.member.following == 1) {
                    Text('你关注的人')
                      .fontSize(10)
                      .padding({
                        left: 3,
                        right: 3,
                        top: 1,
                        bottom: 1
                      })
                      .backgroundColor($r('app.color.bilibili_pink'))
                      .opacity(0.7)
                      .borderRadius(3)
                      .fontColor(Color.White)
                      .maxLines(1)
                  }

                  //原始粉丝等标签
                  if (item.member.is_contractor) {
                    Text(item.member.contract_desc)
                      .fontSize(10)
                      .padding({
                        left: 3,
                        right: 3,
                        top: 1,
                        bottom: 1
                      })
                      .backgroundColor($r('app.color.bilibili_pink'))
                      .opacity(0.7)
                      .borderRadius(3)
                      .fontColor(Color.White)
                      .maxLines(1)
                  }

                }


                Text(item.content.message)
                  .width('100%')
                  .enableDataDetector(true)

                Column({ space: 5 }) {
                  Row() {
                    Text(StringFormat.formatTimestamp(item.ctime))
                      .fontSize(12)
                      .opacity(0.6)
                    Text(item.reply_control.location)
                      .fontSize(12)
                      .opacity(0.6)
                  }.width('100%').justifyContent(FlexAlign.SpaceBetween)


                  if (item.up_action.like || item.up_action.reply) {
                    Row({ space: 10 }) {
                      if (item.up_action.like) {
                        Text('UP主觉得很赞')
                          .fontSize(10)
                          .padding({
                            left: 5,
                            right: 5,
                            top: 3,
                            bottom: 3
                          })
                          .backgroundColor($r('sys.color.comp_background_gray'))
                          .opacity(0.7)
                          .borderRadius(5)
                      }
                      if (item.up_action.reply) {
                        Text('UP主回复过')
                          .fontSize(10)
                          .padding({
                            left: 5,
                            right: 5,
                            top: 3,
                            bottom: 3
                          })
                          .backgroundColor($r('sys.color.comp_background_gray'))
                          .opacity(0.7)
                          .borderRadius(5)
                      }
                    }
                  }
                }.alignItems(HorizontalAlign.Start)

              }.alignItems(HorizontalAlign.Start)

            }.attributeModifier(backgroundBlurModifierAuto)
            .padding({
              left: 10,
              right: 10,
              top: 8,
              bottom: 8
            })
            .borderRadius(15)
          })


          if (this.isLoading) {
            ListItem() {
              Row({ space: 5 }) {
                LoadingProgress().width(15).height(15)
                Text('正在加载').fontSize(14)
              }
            }.width('100%').opacity(0.8)
          }


          if (this.isBottom) {
            ListItem() {
              Text('已经到底啦').fontSize(14).opacity(0.8)
            }.width('100%')
          }
        }
        .layoutWeight(1)
        .padding({ left: 10, right: 10 })
        .scrollBar(BarState.Off)
        .contentEndOffset(Math.max(10, __System__.bottomRectHeight))
        .fadingEdge(true)
        .onReachEnd(async () => {
          if (this.isLoading || this.isBottom) {
            return
          }
          XAnimation.runWithAnimation(() => this.isLoading = true)
          const res = await client.getComment({
            type: 1,
            sort: 1,
            oid: this.videoInfo!.aid,
            pn: this.currentPage
          })
          if (res && res != null && res.replies != null) {
            this.currentPage += 1
            res.replies.forEach((item) => {
              this.commentArray?.pushData(item)
            })
            console.log(`${json.stringify(res.replies)}`)
          }else {
            // XAnimation.runWithAnimation(() => this.isBottom = true)
          }
          XAnimation.runWithAnimation(() => this.isLoading = false)
        })
        .nestedScroll({
          scrollForward: NestedScrollMode.PARENT_FIRST,
          scrollBackward: NestedScrollMode.SELF_FIRST
        })

      }.layoutWeight(1)
      .indicator(false)
      .onChange((index) => {
        this.videoInfoIndex = index
      })
      .loop(false)

    }.layoutWeight(1)
    .width('100%')
  }
}