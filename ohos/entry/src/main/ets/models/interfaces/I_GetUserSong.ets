import { I_GetSongRes_data_song } from "./I_GetSong"

export interface I_GetUserSongRes{
  ok:boolean,
  data:I_GetUserSongRes_Data
}
export interface I_GetUserSongRes_Data{
  songs:I_GetSongRes_data_song[],
  total:number,
  page:number,
  size:number
}
