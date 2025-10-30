import { I_GetRecentSongRes_data_song } from "./I_GetRecentSong"


export interface I_GetHistoryRes{
  ok: boolean
  data: I_GetHistoryRes_Data
}

export interface I_GetHistoryRes_Data{
  list:I_GetHistoryRes_Data_Item[]
}

export interface I_GetHistoryRes_Data_Item {
  id:number
  song_info:I_GetRecentSongRes_data_song
  play_time:string
}