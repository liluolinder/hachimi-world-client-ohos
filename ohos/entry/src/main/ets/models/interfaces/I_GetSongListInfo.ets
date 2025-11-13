import { I_GetSongRes_data_song } from "./I_GetSong"
import { I_GetSongListRes_data_item } from "./I_GetSongList"

export interface I_GetSongListInfoRes {
  ok: boolean
  data: I_GetSongListInfo_data
}

export interface I_GetSongListInfo_data {
  playlist_info: I_GetSongListRes_data_item
  songs: I_GetSongRes_data_song[]
}