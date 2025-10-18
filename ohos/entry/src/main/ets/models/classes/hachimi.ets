import { http } from "@kit.NetworkKit";
import { BusinessError } from "@kit.BasicServicesKit";
import { JSON } from "@kit.ArkTS";
import { I_GetRecentSongRes, I_GetRecentSongRes_data_song } from "../interfaces/I_GetRecentSong";
import { BasicDataSource } from "commonpackage";
import { USER_AGENT } from "../../commons/commonItem";
import { I_SearchSongRes, I_SearchSongRes_data_hit } from "../interfaces/I_SearchSong";
import { __User__ } from "../../commons/shareDatas";


@ObservedV2
export class hachimi {
  @Trace recentSongArray: BasicDataSource<I_GetRecentSongRes_data_song> =
    new BasicDataSource<I_GetRecentSongRes_data_song>()
  @Trace searchSongArray: BasicDataSource<I_SearchSongRes_data_hit> = new BasicDataSource<I_SearchSongRes_data_hit>()

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

  async searchSong(songName: string, needClear:boolean = false): Promise<undefined | number> {
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


  async updateHistory(songID:number): Promise<undefined | number> {
    let req = http.createHttp()
    return await req.request(`https://api.hachimi.world/play_history/touch`, {
      method: http.RequestMethod.POST,
      header: {
        'sec-ch-ua-platform': '"Windows"',
        'user-agent': USER_AGENT,
        'authorization':__User__.authorization,
        'origin':'https://hachimi.world',
        'referer':'https://hachimi.world'
      },
      extraData:{
        'song_id':songID
      }
    }).then(async (data: http.HttpResponse) => {
      let obj = JSON.parse(JSON.stringify(data))
      let result = JSON.parse((obj as object)?.['result'])
      let res: I_SearchSongRes = result as I_SearchSongRes
      return undefined
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
