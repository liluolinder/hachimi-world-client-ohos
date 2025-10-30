import { http } from "@kit.NetworkKit";
import { BusinessError } from "@kit.BasicServicesKit";
import { JSON } from "@kit.ArkTS";
import { I_GetRecentSongRes, I_GetRecentSongRes_data_song, I_GetSongDetailRes } from "../interfaces/I_GetRecentSong";
import { BasicDataSource, XPreferencesManager } from "commonpackage";
import { USER_AGENT } from "../../commons/commonItem";
import { I_SearchSongRes, I_SearchSongRes_data_hit } from "../interfaces/I_SearchSong";
import { __System__, __User__ } from "../../commons/shareDatas";
import { I_UpdateHistoryRes } from "../interfaces/I_UpdateHistory";
import { I_GenerateCaptchaRes, I_GenerateCaptchaRes_data } from "../interfaces/I_GenerateCaptcha";
import { I_LoginRes, I_LoginRes_data, I_LoginRes_dataError } from "../interfaces/I_Login";
import { I_GetProfileRes } from "../interfaces/I_GetProfile";
import { I_GetHistoryRes, I_GetHistoryRes_Data_Item } from "../interfaces/I_GetHistory";
import { I_RefreshToken, I_RefreshToken_data_OK } from "../interfaces/I_RefreshToken";


@ObservedV2
export class hachimi {
  @Trace recentSongArray: BasicDataSource<I_GetRecentSongRes_data_song> =
    new BasicDataSource<I_GetRecentSongRes_data_song>()

  @Trace searchSongArray: BasicDataSource<I_SearchSongRes_data_hit> = new BasicDataSource<I_SearchSongRes_data_hit>()

  @Trace playHistoryArray: BasicDataSource<I_GetHistoryRes_Data_Item> = new BasicDataSource<I_GetHistoryRes_Data_Item>()

  async generateCaptcha(): Promise<I_GenerateCaptchaRes_data | undefined> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/auth/captcha/generate`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GenerateCaptchaRes = result as I_GenerateCaptchaRes
      if (res.ok) {
        return res.data
      } else {
        return undefined
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async login(email: string, password: string, captcha_key: string): Promise<string | undefined> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/auth/login/email`, {
      method: http.RequestMethod.POST,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
      extraData: {
        'email': email,
        'password': password,
        'captcha_key': captcha_key,
        'device_info': 'OHOS Client',
        'code': null
      }
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_LoginRes = result as I_LoginRes
      console.log(JSON.stringify(res))
      if (res.ok) {
        __User__.isLogin = true
        __User__.access_token = (res.data as I_LoginRes_data).token.access_token
        __User__.refresh_token = (res.data as I_LoginRes_data).token.refresh_token
        __User__.expires_in = (res.data as I_LoginRes_data).token.expires_in
        __User__.uid = (res.data as I_LoginRes_data).uid
        __User__.username = (res.data as I_LoginRes_data).username
        XPreferencesManager.putPreferencesValue("hachimi", "isLogin", true)
        XPreferencesManager.putPreferencesValue("hachimi", "refresh_token", __User__.refresh_token)
        XPreferencesManager.putPreferencesValue("hachimi", "access_token", __User__.access_token)
        XPreferencesManager.putPreferencesValue("hachimi", "expires_in", __User__.expires_in)
        XPreferencesManager.putPreferencesValue("hachimi", "uid", __User__.uid)
        XPreferencesManager.putPreferencesValue("hachimi", "username", __User__.username)
        return 'ok'
      } else {
        return (res.data as I_LoginRes_dataError).msg
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async getProfile(uid: number) {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/user/profile?uid=${uid}`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetProfileRes = result as I_GetProfileRes
      if (res.ok) {
        return 'ok'
      } else {
        return undefined
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async refreshToken(){
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/auth/refresh_token`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
      extraData:{
        'refresh_token':__User__.refresh_token,
        'device_info': 'OHOS Client'
      }
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_RefreshToken = result as I_RefreshToken
      console.log(JSON.stringify(res))
      if (res.ok) {
        let resOK = res.data as I_RefreshToken_data_OK
        __User__.access_token = resOK.access_token
        __User__.refresh_token = resOK.refresh_token
        __User__.expires_in = resOK.expires_in
        XPreferencesManager.putPreferencesValue("hachimi", "refresh_token", __User__.refresh_token)
        XPreferencesManager.putPreferencesValue("hachimi", "access_token", __User__.access_token)
        XPreferencesManager.putPreferencesValue("hachimi", "expires_in", __User__.expires_in)
        return 'ok'
      } else {
        return undefined
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async getRecentSong(): Promise<undefined | number> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/recent_v2`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,

      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetRecentSongRes = result as I_GetRecentSongRes
      if (res.ok) {

        res.data.songs.forEach((item) => {
          this.recentSongArray.pushData(item)
        })

        return res.data.songs.length
      } else {
        return undefined
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async getSongDetail(id: string): Promise<undefined | I_GetRecentSongRes_data_song> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/detail?id=${id}`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,

      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetSongDetailRes = result as I_GetSongDetailRes
      if (res.ok) {
        return res.data
      } else {
        return undefined
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async searchSong(songName: string, needClear: boolean = false): Promise<undefined | number> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/search?q=${encodeURI(songName)}`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_SearchSongRes = result as I_SearchSongRes
      if (res.ok) {
        if (needClear) {
          this.searchSongArray.clearData()
        }
        res.data.hits.forEach((item) => {
          this.searchSongArray.pushData(item)
        })

        return res.data.hits.length
      } else {
        return undefined
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async updateHistory(songID: number): Promise<undefined | boolean> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/play_history/touch`, {
      method: http.RequestMethod.POST,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
        'authorization': `Bearer ${__User__.access_token}`,
        'origin': 'https://hachimi.world',
        'referer': 'https://hachimi.world'
      },
      extraData: {
        'song_id': songID
      }
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_UpdateHistoryRes = result as I_UpdateHistoryRes
      console.log(JSON.stringify(res))
      return res.ok
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async updateHistoryNoLogin(songID: number): Promise<undefined | boolean> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/play_history/touch_anonymous`, {
      method: http.RequestMethod.POST,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
        'origin': 'https://hachimi.world',
        'referer': 'https://hachimi.world'
      },
      extraData: {
        'song_id': songID
      }
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_UpdateHistoryRes = result as I_UpdateHistoryRes
      console.log(JSON.stringify(res))
      return res.ok
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }

  async getHistory(cursor:string = ''):Promise<number | undefined>{
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/play_history/cursor?${cursor == ''?'':`cursor=${cursor}&`}size=20`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
        'authorization': `Bearer ${__User__.access_token}`,
        'origin': 'https://hachimi.world',
        'referer': 'https://hachimi.world'
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetHistoryRes = result as I_GetHistoryRes
      console.log(JSON.stringify(res))
      if (res.ok)
      {
        if (res.data.list.length!=0) {
          res.data.list.forEach((item)=>{
            this.playHistoryArray.pushData(item)
          })
        }
        return  res.data.list.length
      }else {
        return undefined
      }
    })
      .catch((err: BusinessError) => {
        console.log(`Liquid:${JSON.stringify(err)}`)
        return undefined
      }).finally(() => {
        req.destroy();
      });
  }
}

export let __Client__: hachimi = new hachimi()
