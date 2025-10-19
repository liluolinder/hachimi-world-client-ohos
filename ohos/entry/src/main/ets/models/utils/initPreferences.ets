import { XPreferencesManager } from "commonpackage";
import { __User__ } from "../../commons/shareDatas";

export async function initPreferences() {
  __User__.isLogin = XPreferencesManager.getPreferencesValue("hachimi", "isLogin", false) as boolean

  if (__User__.isLogin) {
    __User__.refresh_token = XPreferencesManager.getPreferencesValue("hachimi", "refresh_token", "") as string
    __User__.access_token = XPreferencesManager.getPreferencesValue("hachimi", "access_token", "") as string
    __User__.uid  = XPreferencesManager.getPreferencesValue("hachimi", "uid", 0) as number
    __User__.username  = XPreferencesManager.getPreferencesValue("hachimi", "username", "") as string
  }
}