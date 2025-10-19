export interface I_GetProfileRes{
  ok: boolean
  data: I_GetProfileRes_data
}

export interface I_GetProfileRes_data{
  uid: number
  username: string
  avatar_url: null | string
  bio: string  //介绍
  gender: null | string
  is_banned: boolean
}