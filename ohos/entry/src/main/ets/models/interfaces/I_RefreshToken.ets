export interface I_RefreshToken{
  ok: boolean
  data: I_RefreshToken_data_OK | I_RefreshToken_data_Error

}

export interface I_RefreshToken_data_OK{
  access_token:string
  refresh_token:string
  expires_in:string
}

export interface I_RefreshToken_data_Error{
  code:string
  msg:string
}