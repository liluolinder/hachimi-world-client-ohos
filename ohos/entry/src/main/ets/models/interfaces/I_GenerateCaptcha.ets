export interface I_GenerateCaptchaRes{
  ok: boolean
  data:I_GenerateCaptchaRes_data
}

export interface I_GenerateCaptchaRes_data{
  captcha_key: string
  url: string
}