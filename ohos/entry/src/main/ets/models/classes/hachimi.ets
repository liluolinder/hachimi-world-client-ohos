import { http } from "@kit.NetworkKit";
import { BusinessError } from "@kit.BasicServicesKit";
import { JSON } from "@kit.ArkTS";
import { I_GetSongRes, I_GetSongRes_data_song, I_GetSongDetailRes } from "../interfaces/I_GetSong";
import { BasicDataSource, XPreferencesManager } from "commonpackage";
import { USER_AGENT } from "../../commons/commonItem";
import { I_SearchSongParm, I_SearchSongRes } from "../interfaces/I_SearchSong";
import { __System__, __User__ } from "../../commons/shareDatas";
import { I_UpdateHistoryRes } from "../interfaces/I_UpdateHistory";
import { I_GenerateCaptchaRes, I_GenerateCaptchaRes_data } from "../interfaces/I_GenerateCaptcha";
import { I_LoginRes, I_LoginRes_data, I_LoginRes_dataError } from "../interfaces/I_Login";
import { I_GetProfileRes, I_GetProfileRes_data } from "../interfaces/I_GetProfile";
import { I_GetHistoryRes, I_GetHistoryRes_Data_Item } from "../interfaces/I_GetHistory";
import { I_RefreshToken, I_RefreshToken_data_OK } from "../interfaces/I_RefreshToken";
import { I_CreateSongListRes } from "../interfaces/I_CreateSongList";
import { I_GetSongListRes, I_GetSongListRes_data_item } from "../interfaces/I_GetSongList";
import { I_AddSong2SongListInfoParm, I_AddSong2SongListInfoRes } from "../interfaces/I_AddSong2SongListInfo";
import { I_GetSongListInfoRes, I_GetSongListInfo_data } from "../interfaces/I_GetSongListInfo";
import { I_GetUserSongRes, I_GetUserSongRes_Data } from "../interfaces/I_GetUserSong";


@ObservedV2
export class hachimi {
  @Trace recentSongArray: BasicDataSource<I_GetSongRes_data_song> =
    new BasicDataSource<I_GetSongRes_data_song>()
  @Trace recommendSongArray: BasicDataSource<I_GetSongRes_data_song> =
    new BasicDataSource<I_GetSongRes_data_song>()
  @Trace weeklySongArray: BasicDataSource<I_GetSongRes_data_song> =
    new BasicDataSource<I_GetSongRes_data_song>()
  @Trace pureSongArray: BasicDataSource<I_GetSongRes_data_song> =
    new BasicDataSource<I_GetSongRes_data_song>()
  @Trace classicalSongArray: BasicDataSource<I_GetSongRes_data_song> =
    new BasicDataSource<I_GetSongRes_data_song>()
  @Trace useNoOriginalSongArray: BasicDataSource<I_GetSongRes_data_song> =
    new BasicDataSource<I_GetSongRes_data_song>()
  @Trace searchSongArray: BasicDataSource<I_GetSongRes_data_song> = new BasicDataSource<I_GetSongRes_data_song>()
  @Trace playHistoryArray: BasicDataSource<I_GetHistoryRes_Data_Item> = new BasicDataSource<I_GetHistoryRes_Data_Item>()
  @Trace songListArray: BasicDataSource<I_GetSongListRes_data_item> = new BasicDataSource<I_GetSongListRes_data_item>()

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

  async getProfile(uid: number):Promise<undefined | I_GetProfileRes_data > {
    await this.refreshToken()
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

  async getUserSong(uid: number):Promise<undefined | I_GetUserSongRes_Data> {
    await this.refreshToken()
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/page_by_user?user_id=${uid}`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetUserSongRes = result as I_GetUserSongRes
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

  async refreshToken(): Promise<undefined | 'ok'> {
    let nowTime = new Date()
    if (__User__.isLogin && nowTime.getTime() >= (new Date(__User__.expires_in)).getTime()) {
      console.log(`触发刷新token`)
      console.log(__User__.refresh_token)
      let req = http.createHttp()
      return await req.request(`https://api.hachimi.world/auth/refresh_token`, {
        method: http.RequestMethod.POST,
        header: {
          'sec-ch-ua-platform': '"Windows"',
          'user-agent': USER_AGENT,
        },
        extraData: {
          'refresh_token': __User__.refresh_token,
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
    } else {
      return undefined
    }
  }

  async getRecentSong(needClear: boolean = false): Promise<undefined | number> {
    await this.refreshToken()
    if (needClear) {
      this.recentSongArray.clearData()
    }
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
      let res: I_GetSongRes = result as I_GetSongRes
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

  async getRecommendSong(needClear: boolean = false): Promise<undefined | number> {
    if (needClear) {
      this.recentSongArray.clearData()
    }
    if (__User__.isLogin) {
      return await this.getRecommendSongLogin()
    } else {
      return await this.getRecommendSongNoLogin()
    }
  }

  async getRecommendSongLogin(): Promise<undefined | number> {
    await this.refreshToken()
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/recommend`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
        'authorization': `Bearer ${__User__.access_token}`,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetSongRes = result as I_GetSongRes
      if (res.ok) {
        res.data.songs.forEach((item) => {
          this.recommendSongArray.pushData(item)
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

  async getRecommendSongNoLogin(): Promise<undefined | number> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/recommend_anonymous`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetSongRes = result as I_GetSongRes
      if (res.ok) {
        res.data.songs.forEach((item) => {
          this.recommendSongArray.pushData(item)
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

  async getWeeklySong(needClear: boolean = false): Promise<undefined | number> {
    if (needClear) {
      this.recentSongArray.clearData()
    }
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/hot/weekly`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_GetSongRes = result as I_GetSongRes
      if (res.ok) {
        res.data.songs.forEach((item) => {
          this.weeklySongArray.pushData(item)
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

  async getSongDetail(id: string): Promise<undefined | I_GetSongRes_data_song> {
    let req = http.createHttp()
    console.log(`https://api.hachimi.world/song/detail?id=${id}`)
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

  async searchSong(param:I_SearchSongParm, needClear: boolean = false): Promise<undefined | number> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/song/search?q=${encodeURI(param.songName??'')}&${param.limit??-1 == -1 ? '' :
      `limit=${param.limit}&`}&filter=${encodeURI(param.filter??'')}`, {
      method: http.RequestMethod.GET,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
      },
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_SearchSongRes = result as I_SearchSongRes
      console.log(JSON.stringify(res))
      if (res.ok) {
        switch (param.type??'') {
          case '纯净':
            if (needClear) {
              this.pureSongArray.clearData()
            }
            res.data.hits.forEach((item) => {
              this.pureSongArray.pushData(item)
            })
            break;
          case '古典':
            if (needClear) {
              this.classicalSongArray.clearData()
            }
            res.data.hits.forEach((item) => {
              this.classicalSongArray.pushData(item)
            })
            break;
          case '原曲不使用':
            if (needClear) {
              this.useNoOriginalSongArray.clearData()
            }
            res.data.hits.forEach((item) => {
              this.useNoOriginalSongArray.pushData(item)
            })
            break;
          default :
            if (needClear) {
              this.searchSongArray.clearData()
            }
            res.data.hits.forEach((item) => {
              this.searchSongArray.pushData(item)
            })
            break;
        }


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
    await this.refreshToken()
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

  async getHistory(cursor: string = '', needClear: boolean = false): Promise<number | undefined> {
    await this.refreshToken()
    if (needClear) {
      this.playHistoryArray.clearData()
    }
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/play_history/cursor?${cursor == '' ? '' :
      `cursor=${cursor}&`}size=20`, {
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
      if (res.ok) {
        if (res.data.list.length != 0) {
          res.data.list.forEach((item) => {
            this.playHistoryArray.pushData(item)
          })
        }
        return res.data.list.length
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

  async getSongList(needClear: boolean = false): Promise<number | undefined> {
    if (needClear) {
      this.songListArray.clearData()
    }
    await this.refreshToken()
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/playlist/list`, {
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
      let res: I_GetSongListRes = result as I_GetSongListRes
      console.log(JSON.stringify(res))
      if (res.ok) {
        res.data.playlists.forEach((item) => {
          this.songListArray.pushData(item)
        })
        return res.data.playlists.length
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

  async getSongListInfo(id: number): Promise<I_GetSongListInfo_data | undefined> {
    await this.refreshToken()
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/playlist/detail_private?id=${id}`, {
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
      let res: I_GetSongListInfoRes = result as I_GetSongListInfoRes
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

  async createSongList(name: string, is_public: boolean, des: string | null = null): Promise<undefined | number> {
    await this.refreshToken()
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/playlist/create`, {
      method: http.RequestMethod.POST,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
        'authorization': `Bearer ${__User__.access_token}`,
        'origin': 'https://hachimi.world',
        'referer': 'https://hachimi.world'
      },
      extraData: {
        'description': des,
        'name': name,
        'is_public': is_public
      }
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_CreateSongListRes = result as I_CreateSongListRes
      if (res.ok) {
        return res.data.id
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

  async addSong2SongListInfo(param: I_AddSong2SongListInfoParm): Promise<'ok' | undefined> {
    await this.refreshToken()
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/playlist/add_song`, {
      method: http.RequestMethod.POST,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
        'authorization': `Bearer ${__User__.access_token}`,
        'origin': 'https://hachimi.world',
        'referer': 'https://hachimi.world'
      },
      extraData: {
        'playlist_id': param.playlist_id,
        'song_id': param.song_id
      }
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_AddSong2SongListInfoRes = result as I_AddSong2SongListInfoRes
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
}

export let __Client__: hachimi = new hachimi()
