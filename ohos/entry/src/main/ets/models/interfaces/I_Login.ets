export interface I_LoginRes{
  ok:boolean
  data:I_LoginRes_data | I_LoginRes_dataError
}

export interface I_LoginRes_dataError{
  code:string|number
  msg:string
}

export interface I_LoginRes_data{
  uid: number,
  username: string,
  token:I_LoginRes_data_token
}
export interface I_LoginRes_data_token {
  access_token: string
  refresh_token: string
  expires_in:string
}