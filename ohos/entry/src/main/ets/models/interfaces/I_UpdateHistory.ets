export interface I_UpdateHistoryRes{
  error:string
  ok:boolean
  data:null | I_UpdateHistoryRes_data
}

export interface I_UpdateHistoryRes_data{
  code: string
  msg: string
}