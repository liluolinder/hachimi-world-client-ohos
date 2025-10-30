export interface I_GetSongListRes{
  ok: boolean
  data:I_GetSongListRes_data
}

export interface I_GetSongListRes_data{
  playlists: I_GetSongListRes_data_item[]
}

export interface I_GetSongListRes_data_item {
  id: number
  name: string
  cover_url: null|string
  description: null|string
  create_time: string //2025-10-30T12:21:34.753576Z
  is_public: boolean
  songs_count: number
}