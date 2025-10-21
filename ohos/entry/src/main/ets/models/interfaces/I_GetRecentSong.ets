export interface I_GetRecentSongRes{
  data:I_GetRecentSongRes_data
  ok:boolean
}
export interface I_GetRecentSongRes_data{
  songs:I_GetRecentSongRes_data_song[]
}

export interface I_GetSongDetailRes{
  data:I_GetRecentSongRes_data_song
  ok:boolean
}

export interface I_GetRecentSongRes_data_song{
  id: number    //歌曲编号
  display_id: string  //展示ID
  title: string           //歌曲标题
  subtitle: string | null  //歌曲副标题
  description: string | null        //歌曲描述
  tags:I_GetRecentSongRes_data_song_tag[] | null  //歌曲tag
  duration_seconds: number                  //歌曲时长   单位:s
  lyrics: string | null                           //歌词
  audio_url:string  //音频url
  cover_url: string //封面
  production_crew: I_GetRecentSongRes_data_song_productionCrew[] | null  //制作团队
  creation_type: number | null   //创作类型
  origin_infos: I_GetRecentSongRes_data_song_originInfos[] | null
  uploader_uid: number | null  //上传者UID
  uploader_name: string | null  //上传者昵称
  play_count: number     //播放次数
  like_count: number    //喜欢数量
  external_links: I_GetRecentSongRes_data_song_externalLinks[] | null
}

export interface I_GetRecentSongRes_data_song_tag{
  id: number      //tagID
  name: string | null // tag
  description: string | null     //描述
}

export interface I_GetRecentSongRes_data_song_productionCrew{

  id: number   //编号ID
  song_id: number    //歌曲ID
  role:string   //角色
  uid: null|number    //UID
  person_name:string //姓名

}
export interface I_GetRecentSongRes_data_song_originInfos{

  song_display_id: null | string //原作展示Id
  title: string   //原作title
  artist: string| null  //原作作者
  url: string | null    //原作url
  origin_type: number| null    //种类

}

export interface I_GetRecentSongRes_data_song_externalLinks{
  platform: string | null    //平台
  url: string | null  //转跳url
}

