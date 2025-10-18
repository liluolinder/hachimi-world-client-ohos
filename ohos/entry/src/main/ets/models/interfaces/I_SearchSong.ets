export interface I_SearchSongRes {
  ok: boolean
  data: I_SearchSongRes_data
}

export interface I_SearchSongRes_data {
  hits: I_SearchSongRes_data_hit[]
  query: string        //搜索词
  processing_time_ms: number  //搜索使用时间 ms
  total_hits: null|number       //所有命中数
  limit: number              //限制
  offset: number
}

export interface I_SearchSongRes_data_hit {
  id: number    //歌曲编号
  display_id: string  //展示ID
  title: string           //歌曲标题
  subtitle: string | null  //歌曲副标题
  description: string | null        //歌曲描述
  duration_seconds: number                  //歌曲时长   单位:s
  artist:string  //艺术家
  audio_url:string  //音频url
  cover_art_url: string //封面
  uploader_uid: number | null  //上传者UID
  uploader_name: string | null  //上传者昵称
  play_count: number     //播放次数
  like_count: number    //喜欢数量
}