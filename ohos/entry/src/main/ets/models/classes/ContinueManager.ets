import { XContext } from "commonpackage"
import { AbilityConstant } from "@kit.AbilityKit"
import { __UI__ } from "../../commons/shareDatas"
import { S_PlayerParam } from "../interfaces/S_NavParams"
import { I_AllCollectionsRes_data_list } from "../interfaces/I_AllCollectionsRes"

export interface S_ContinueData{
  type: 'video'|'collection'|'live'|'film'

  //视频数据
  bv ?: string
  cid ?: number
  duration ?: number

 //收藏集数据
  collectionData ?: I_AllCollectionsRes_data_list
}

class ContinueManager {
  data ?: S_ContinueData
  private duration: number = 0

  getData(){
    if(this.data){
      this.data.duration = this.duration
      return JSON.stringify(this.data)
    }
    return undefined
  }

  on(data: S_ContinueData) {
    try {
      this.data = data
      XContext.getAbilityContext().setMissionContinueState(AbilityConstant.ContinueState.ACTIVE)
    } catch {
    }
  }

  setVideoDuration(duration: number){
    this.duration = duration
  }

  off() {
    try {
      XContext.getAbilityContext().setMissionContinueState(AbilityConstant.ContinueState.INACTIVE)
    } catch {
    }
  }

  continue(json ?: string) {
    if(!json) return
    const data = JSON.parse(json) as S_ContinueData
    switch (data.type) {
      case 'video':
        __UI__.mainPageStack.pushPathByName('player',
          {
            bvid: data.bv ?? '',
            cid: data.cid ?? 0,
            startDuration: data.duration
          } as S_PlayerParam)
        break
      case 'collection':
        __UI__.mainPageStack.pushPathByName('collectionInfoPage', data.collectionData)
        break
    }
  }
}

export const continueManager: ContinueManager = new ContinueManager()