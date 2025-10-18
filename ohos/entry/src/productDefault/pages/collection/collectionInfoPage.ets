import { curves } from "@kit.ArkUI"
import { BasicDataSource, XAnimation, XContext } from "commonpackage"
import { Cover } from "../../components/Cover"
import { BigImage } from "../../components/BigImage"
import { CollectionVideo } from "../../components/CollectionVideo"
import { request } from "@kit.BasicServicesKit"
import { photoAccessHelper } from "@kit.MediaLibraryKit"
import { fileIo, fileUri } from "@kit.CoreFileKit"
import { I_AllCollectionsRes_data_list } from "../../../main/ets/models/interfaces/I_AllCollectionsRes"
import { I_CollectionsInfoRes_data_item } from "../../../main/ets/models/interfaces/I_CollectionsInfoRes"
import { __System__, __UI__ } from "../../../main/ets/commons/shareDatas"
import { client } from "../../../main/ets/models/classes/pilipala"
import { I_CollectionPushParm } from "../../../main/ets/models/interfaces/I_CollectionPushParm"
import { NavButton } from "../../components/NavButton"
import { backgroundBlurModifierAuto } from "../../../main/ets/models/classes/backGroundEffect"
import { continueManager } from "../../../main/ets/models/classes/ContinueManager"

@Builder
export function collectionInfoPageBuilder() {
  collectionInfoPage()
}

@ComponentV2
export struct collectionInfoPage {
  @Local ColumnNumber: number = -1
  @Local collectionInfo?: I_AllCollectionsRes_data_list
  @Local viewTypeIndex: number = 0
  @Local viewItem?: I_CollectionsInfoRes_data_item
  @Local singerCollectionArray: BasicDataSource<I_CollectionsInfoRes_data_item> =
    new BasicDataSource<I_CollectionsInfoRes_data_item>()
  @Local pageOpacity: number =  __UI__.isSplitLayout? 0 : 1
  @Local isLoading: boolean = true

  currentIndex: number = 0

  back(){
    if (this.viewTypeIndex != 0) {
      XAnimation.runWithAnimation(() => this.viewTypeIndex = 0)
      return
    }
    if (__UI__.mainPageStack.getAllPathName().length ==
      1) {
      XAnimation.runWithAnimation(() => __UI__.indexNavigationMode = NavigationMode.Stack)
    }
    __UI__.mainPageStack.pop()
  }

  aboutToAppear(): void {
    if (__UI__.isSplitLayout) XAnimation.runWithAnimation(() => __UI__.indexNavigationMode =
      NavigationMode.Split)
  }

  saveImageOrVideo2Photo(webUri: string, type: '.png' | '.mp4') {
    const path = XContext.getCacheDir() + '/SelineRead_' + new Date().getTime().toString() + type
    console.log(`Liquid:${webUri}`)
    request.downloadFile(XContext.getAbilityContext(), {
      //替换下载地址
      url: webUri,
      //文件下载后保存的沙箱路径
      filePath: path,
      background: true,
      header: {
        'Accept-Encoding': 'gzip, deflate, br',
        'Accept-Language': 'zh-CN,zh;q=0.9',
        'Cookie': 'sessionid=XXX',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
      },
    }).then((downloadTask: request.DownloadTask) => {
      downloadTask.on('complete', async () => {

        try {
          let srcFileUri = fileUri.getUriFromPath(path)
          let srcFileUris: Array<string> = [srcFileUri];
          let photoCreationConfigs: Array<photoAccessHelper.PhotoCreationConfig> = [
            {
              title: this.viewItem?.card_info.card_name,
              fileNameExtension: type.replace('.', ''),
              photoType: type == '.png'? photoAccessHelper.PhotoType.IMAGE: photoAccessHelper.PhotoType.VIDEO,
              subtype: photoAccessHelper.PhotoSubtype.DEFAULT
            }
          ];
          let desFileUris: Array<string> = await photoAccessHelper.getPhotoAccessHelper(XContext.getAbilityContext()).showAssetsCreationDialog(srcFileUris, photoCreationConfigs);
          let desFile: fileIo.File = await fileIo.open(desFileUris[0], fileIo.OpenMode.WRITE_ONLY);
          let srcFile: fileIo.File = await fileIo.open(srcFileUri, fileIo.OpenMode.READ_ONLY);
          await fileIo.copyFile(srcFile.fd, desFile.fd);
          fileIo.closeSync(srcFile);
          fileIo.closeSync(desFile);
          XContext.toast('已保存')
        } catch {
          XContext.toast('保存失败')
        }
      })
      downloadTask.on('fail', (err) => {
        XContext.toast('下载失败')
        console.log(`Liquid2:${JSON.stringify(err)}`)
      })
    })
  }

  @Builder
  NavigationMenus() {
    Row({ space: 8 }) {
      if (this.viewTypeIndex > 0) {
        NavButton({
          symbol: $r('sys.symbol.download')
        })

          .onClick(() => {
            XContext.toast('正在保存')
            if (this.viewTypeIndex == 1) {
              this.saveImageOrVideo2Photo(this.viewItem?.card_info.card_img!, '.png')
            } else if (this.viewTypeIndex == 2) {
              this.saveImageOrVideo2Photo(this.viewItem?.card_info.card_img!, '.png')
              this.saveImageOrVideo2Photo(this.viewItem?.card_info.video_list[0]!, '.mp4')
            }
          })
      }

    }.padding({ left: 16, right: 16 })
  }

  @Builder
  NavigationTitle() {
    Row({ space: 8 }) {
      NavButton({
        symbol: $r('sys.symbol.chevron_left'),
        symbolSize: 28
      })
        .onClick(() => {
          this.back()
        })
      Text(this.collectionInfo?.act_name ?? '')
        .maxLines(1)
        .maxFontSize(23)
        .minFontSize(12)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
        .fontWeight(FontWeight.Medium)
        .fontColor($r('sys.color.font'))
        .opacity(0.8)
    }.padding({ left: 16, right: 16 })
  }

  build() {
    NavDestination() {
      if(this.isLoading){
        Stack(){
          LoadingProgress().size({width: 100, height: 100})
        }
          .width('100%').height('100%')
        .transition(TransitionEffect.OPACITY)
      }
      if (this.viewTypeIndex == 1) {
        BigImage({
          image: this.viewItem?.card_info.card_img
        })
          .zIndex(114514)
          .geometryTransition(this.viewItem?.card_info.card_name, { follow: true })
          .transition(TransitionEffect.OPACITY)
      } else if (this.viewTypeIndex == 2) {
        CollectionVideo({
          videoSrc: this.viewItem?.card_info.video_list[0]
        })
          .geometryTransition(this.viewItem?.card_info.card_name, { follow: true })
          .transition(TransitionEffect.OPACITY)
      } else {
        List({ space: 10, initialIndex: this.currentIndex }) {
          LazyForEach(this.singerCollectionArray,
            (item: I_CollectionsInfoRes_data_item, index: number) => {
              ListItem() {
                Column({ space: 10 }) {
                  Cover({
                    data: item.card_info.card_img,
                    isVideo: item.card_info.card_type == 2,
                    aspectRatioValue: undefined
                  })
                    .geometryTransition(item.card_info.card_name, { follow: true })
                    .transition(TransitionEffect.OPACITY)


                  Text(item.card_info.card_name)
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.MARQUEE })
                    .fontWeight(FontWeight.Medium)

                }.alignItems(HorizontalAlign.Start)
              }
              .attributeModifier(backgroundBlurModifierAuto)
              .useEffect(true)
              .borderRadius(15)
              .onClick(() => {
                XAnimation.runWithAnimation(() => {
                  this.viewItem = item
                  if (item.card_info.card_type == 1) {
                    this.viewTypeIndex = 1
                  } else {
                    this.viewTypeIndex = 2
                  }
                })
              })
              .clickEffect({ level: ClickEffectLevel.LIGHT, scale: 0.95 })
              .padding(15)
              .width('100%')
              .transition(TransitionEffect.OPACITY
                .combine(TransitionEffect.scale({ x: 0.5, y: 0.5 }))
                .animation({ curve: curves.springMotion(0.5, 0.7) }))

            }, (item: string) => item)
        }
        .onScrollIndex((start) => this.currentIndex = start)
        .scrollBar(BarState.Off)
        .onAreaChange((_, newValue) => {
          XAnimation.runWithAnimation(() => {
            this.ColumnNumber = Math.max(Math.ceil(new Number(newValue.width).valueOf() / 240), 2)
          }, this.ColumnNumber > 0)
        })
        .lanes(this.ColumnNumber, 10)
        .alignSelf(ItemAlign.Center)
        .align(Alignment.Center)
        .layoutWeight(1)
        .fadingEdge(true)
        .contentEndOffset(Math.max(__System__.bottomRectHeight, 16))
        .padding({ left: 16, right: 16 })
        .transition(TransitionEffect.OPACITY)
      }
    }
    .opacity(this.pageOpacity)
    .onShown(() => {
      if (__UI__.isSplitLayout) {
        animateToImmediately({ duration: 500, curve: Curve.Ease },
          () => this.pageOpacity = 1)
      }
      if(this.collectionInfo){
        continueManager.on({
          type: 'collection',
          collectionData: this.collectionInfo
        })
      }
    })
    .onWillHide(() => {
      continueManager.off()
      if (__UI__.isSplitLayout) {
        animateToImmediately({ duration: 500, curve: Curve.Ease },
          () => this.pageOpacity = 0)
      }
    })
    .title(this.NavigationTitle())
    .padding({ top: Math.max(__System__.topRectHeight, 16) })
    .width('100%')
    .height('100%')
    .backgroundColor(__UI__.indexNavigationMode == NavigationMode.Stack ? $r('app.color.start_window_background') :
      $r('app.color.start_window_background'))
    .menus(this.NavigationMenus)
    .onReady((context: NavDestinationContext) => {
      const param = context.pathInfo.param as I_AllCollectionsRes_data_list
      client.getCollectionsInfo(param.act_id, param.lottery_id)
        .then((res?: I_CollectionsInfoRes_data_item[]) => {
          let data: I_CollectionPushParm = {
            collectionInfo: param,
            singerCollectionArray: res ?? []
          }
          this.collectionInfo = data.collectionInfo
          data.singerCollectionArray.forEach((item)=>{
            this.singerCollectionArray.pushData(item)
          })
          XAnimation.runWithAnimation(()=> this.isLoading = false)
          continueManager.on({
            type: 'collection',
            collectionData: data.collectionInfo
          })
        })
    })
    .backgroundColor(__UI__.isSplitLayout ? Color.Transparent :
      $r('app.color.start_window_background'))
    .onBackPressed(() => {
      this.back()
      return true
    })
    .hideBackButton(true)
  }
}