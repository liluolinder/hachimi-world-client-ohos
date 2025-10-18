import { window } from "@kit.ArkUI"
import { ConfigurationConstant } from "@kit.AbilityKit"
import { XAnimation } from "commonpackage"
import { __CustomBackgroundData__ } from "../../../productDefault/components/CustomBackground"

export const min_width: number = 700

export const min_split_width: number = 1100

export const min_player_landscape_width: number = 900

@ObservedV2
export class SysShareData {
    @Trace bottomRectHeight: number = 0
    @Trace topRectHeight: number = 0
    @Trace leftRectHeight: number = 0
    @Trace rightRectHeight: number = 0
    @Trace windowWidth: number = 0
    @Trace windowHeight: number = 0
    @Trace windowState: window.WindowStatusType = window.WindowStatusType.UNDEFINED
    @Trace pcMode: boolean = false
    @Trace DecorHeight: number = 50
    @Trace currentColorMode: ConfigurationConstant.ColorMode = ConfigurationConstant.ColorMode.COLOR_MODE_NOT_SET

    @Monitor('windowWidth')
    changeSideBarWidthByBreakPoint() {
        XAnimation.runWithAnimation(()=>{
            __UI__.isSplitLayout = __System__.windowWidth > min_split_width
            __UI__.indexNavigationMode = __System__.windowWidth < min_split_width? NavigationMode.Stack: __UI__.mainPageStack.getAllPathName().length > 0 && !__PlayerUIData__.isFullScreen? NavigationMode.Split: __UI__.indexNavigationMode
            __UI__.isLargeSidebar = __System__.windowWidth < min_width
            __UI__.isShowSideBar = __System__.windowWidth > min_width
            __UI__.sideBarMode = __System__.windowWidth > min_width? SideBarContainerType.Embed: SideBarContainerType.Overlay

        })
    }

    @Monitor('currentColorMode')
    changeColorMode() {
        __CustomBackgroundData__.flash = !__CustomBackgroundData__.flash
    }
}

export let __System__: SysShareData = new SysShareData()

@ObservedV2
export class UserData {
    @Trace isLogin: boolean = false
    @Trace cookie: string = ''
    @Trace csrf:string=''
}

export let __User__: UserData = new UserData()

@ObservedV2
export class UIData {
    @Trace mainPageStack: NavPathStack = new NavPathStack()
    @Trace isLargeSidebar: boolean = true
    @Trace indexNavigationMode: NavigationMode = NavigationMode.Stack
    @Trace sideBarMode: SideBarContainerType = SideBarContainerType.AUTO
    @Trace advanceMaterial: boolean = true
    @Trace isShowSideBar: boolean = __System__.windowWidth > min_width
    @Trace isSplitLayout: boolean = __System__.windowWidth > min_split_width
}

export let __UI__: UIData = new UIData()

@ObservedV2
export class SettingsUIData {
    @Trace settingsShow: boolean = false
    settingsNavPathStack= new NavPathStack()
}

export let __SettingsUIData__: SettingsUIData = new SettingsUIData()

@ObservedV2
export class PlayerUIData {
    @Trace isFullScreen: boolean = false
}

export let __PlayerUIData__: PlayerUIData = new PlayerUIData()