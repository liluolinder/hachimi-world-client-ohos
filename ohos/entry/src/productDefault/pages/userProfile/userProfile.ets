import { __System__, __User__ } from "../../../main/ets/commons/shareDatas"
import { CircleShape } from "@kit.ArkUI"
import { I_GetProfileRes_data } from "../../../main/ets/models/interfaces/I_GetProfile"
import { I_GetUserSongRes_Data } from "../../../main/ets/models/interfaces/I_GetUserSong"
import { I_GetSongRes_data_song } from "../../../main/ets/models/interfaces/I_GetSong"
import { HomeCover } from "../home/Home"
import { __musicPlayer__ } from "../../../main/ets/models/classes/musicPlayer"
import { __Client__ } from "../../../main/ets/models/classes/hachimi"
import { XAnimation, XPreferencesManager } from "commonpackage"

@ComponentV2
export struct userProfile{

  @Local ColumnNumber:number = 0
  @Consumer('tabController') tabController: TabsController = new TabsController();

  build() {
    Column({space:10}){
      Row(){
        Text('神人空间')
          .fontSize(22)
          .fontWeight(FontWeight.Bold)
          .size({ height:50})

        Blank()

        Button('退出登录')
          .backgroundColor($r('sys.color.gray_01'))
          .fontColor($r('sys.color.font'))
          .size({ height:50})
          .onClick(()=>{
            __User__.isLogin = false
            __User__.access_token = ''
            __User__.refresh_token = ''
            __User__.expires_in = ''
            __User__.uid = 0
            __User__.username = ''
            XPreferencesManager.putPreferencesValue("hachimi", "isLogin", false)
            XPreferencesManager.putPreferencesValue("hachimi", "refresh_token", __User__.refresh_token)
            XPreferencesManager.putPreferencesValue("hachimi", "access_token", __User__.access_token)
            XPreferencesManager.putPreferencesValue("hachimi", "expires_in", __User__.expires_in)
            XPreferencesManager.putPreferencesValue("hachimi", "uid", __User__.uid)
            XPreferencesManager.putPreferencesValue("hachimi", "username", __User__.username)
            this.tabController.changeIndex(1)
          })

      }.size({width:'100%', height:50})
      .padding(16)
      .backgroundColor($r('sys.color.gray_01'))
      .borderRadius(20)

      Row({space:10}){
        if (__User__.userInfo)
        {
          Column({space:10}){
            Image(__User__.userInfo?.avatar_url??'')
              .size({width:90, height:90})
              .clipShape(new CircleShape({width:90,height:90}))
              .backgroundColor($r('sys.color.comp_background_list_card'))

            Text(__User__.userInfo?.username)
              .textOverflow({overflow:TextOverflow.MARQUEE})
          }
          Column({space:10}){
            Text(`uid:${__User__.userInfo?.uid}`)
              .width('100%')

            Column({space:5}){
              Text(`简介`)
                .width('100%')
                .opacity(0.4)
              Scroll() {
                Text(__User__.userInfo?.bio??'本人很懒暂无简介')
                  .fontSize(18)
                  .fontWeight(FontWeight.Medium)
                  .width('100%')
              }
              .fadingEdge(true)
              .scrollBar(BarState.Off)
              .edgeEffect(EdgeEffect.Spring)
              .align(Alignment.Top)
              .width('100%')
              .layoutWeight(1)
              .clip(true)
            }.layoutWeight(1)
          }.layoutWeight(1)
        }else {
         Column(){
           LoadingProgress()
             .size({ width: 100 })
         }.size({width:'100%'})
        }
      }.size({width:'100%', height:150})
      .padding(16)
      .backgroundColor($r('sys.color.gray_01'))
      .borderRadius(20)

      Column({space:10}){

        Text('全部作品')
          .fontSize(22)
          .fontWeight(FontWeight.Medium)
          .width('100%')

       if (__User__.userSong)
       {
         if (__User__.userSong.songs.length == 0) {
           Column(){
             Text(`什么也没有`)
               .fontSize(26)
               .fontWeight(FontWeight.Medium)
           }.size({width:'100%'})
           .layoutWeight(1)
           .justifyContent(FlexAlign.Center)
         }else {
           List({ space: 20 }) {
             ForEach(__User__.userSong.songs, (item: I_GetSongRes_data_song, indexA: number) => {
               HomeCover({ data: item, type: 0 })
                 .onClick(async () => {
                   __musicPlayer__.playNow = item
                   __musicPlayer__.playList = []
                   __User__.userSong?.songs.forEach((itemA) => {
                     __musicPlayer__.playList.push(itemA.display_id)
                   })
                   __musicPlayer__.playIndex = indexA
                 })

             }, (item: I_GetSongRes_data_song) => JSON.stringify(item))
           }
           .scrollBar(BarState.Off)
           .width('100%')
           .layoutWeight(1)
           .lanes(this.ColumnNumber, 20)
           .edgeEffect(EdgeEffect.Spring)
           .contentEndOffset(Math.max(__System__.bottomRectHeight, 16) + 100)
           .borderRadius(20)
           .clip(true)
           .onAreaChange((_, newValue) => {
             XAnimation.runWithAnimation(() => {
               this.ColumnNumber = Math.max(Math.ceil(new Number(newValue.width).valueOf() / 300), 2)
             }, this.ColumnNumber > 0)
           })

         }
       }else {
         Column(){
           LoadingProgress()
             .size({ width: 100 })
         }.size({width:'100%'})
         .layoutWeight(1)
         .justifyContent(FlexAlign.Center)
       }
      }.layoutWeight(1)
      .backgroundColor($r('sys.color.gray_01'))
      .borderRadius(20)
      .padding(16)

    }.size({width:'100%', height:'100%'})
    .padding({top:__System__.topRectHeight, left:16, right:16})
  }
}