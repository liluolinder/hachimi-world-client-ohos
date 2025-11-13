import { I_GetSongRes_data, I_GetSongRes_data_song } from "./I_GetSong"


export interface I_SearchSongParm{
  songName?: string
  filter?: string
  limit?: number
  type?: string
}

export interface I_SearchSongRes {
  ok: boolean
  data: I_SearchSongRes_data
}

export interface I_SearchSongRes_data {
  hits: I_GetSongRes_data_song[]
  query: string        //搜索词
  processing_time_ms: number  //搜索使用时间 ms
  total_hits: null|number       //所有命中数
  limit: number              //限制
  offset: number
}

