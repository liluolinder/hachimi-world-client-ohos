import { deviceInfo } from "@kit.BasicServicesKit"
import { ConfigurationConstant } from "@kit.AbilityKit"
import { __CustomBackgroundData__ } from "../../../../productDefault/components/CustomBackground"
import { __UI__, __System__ } from "../../commons/shareDatas"

@ObservedV2
class _backgroundBlurModifier implements AttributeModifier<CommonAttribute> {
    @Trace backgroundColor: ResourceColor = Color.White

    applyNormalAttribute(instance: CommonAttribute): void {
        if(__CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial){
            if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK) {
                this.backgroundColor = "#b2292929" //模糊背景黑
            } else if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_LIGHT) {
                this.backgroundColor = '#b2e7e9ec' //模糊背景白
            }
        }else{
            if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK) {
                this.backgroundColor = "#292929" //模糊背景黑
            } else if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_LIGHT) {
                this.backgroundColor = '#e7e9ec' //模糊背景白
            }
        }

        if (__System__.pcMode === true) {
            if (deviceInfo.deviceType == "2in1" || deviceInfo.deviceType == "PC") {
                instance
                    .backgroundEffect({
                        radius: __CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial ? 100 : 0,
                        saturation: 1.2,
                        brightness: __System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK ?
                            0.9 : 1.1,
                        color: this.backgroundColor
                    })
                    .useEffect(true, EffectType.WINDOW_EFFECT)
            } else {
                instance
                    .backgroundEffect({
                        radius: __CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial ? 100 : 0,
                        saturation: 1.2,
                        brightness: __System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK ?
                            0.9 : 1.1,
                        color: this.backgroundColor
                    })
                    .useEffect(true)
            }
        } else {
            instance
                .backgroundEffect({
                    radius: __CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial ? 100 : 0,
                    saturation: 1.2,
                    brightness: __System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK ? 0.9 :
                        1.1,
                    color: this.backgroundColor
                })
                .useEffect(true)
        }
    }
}

export let backgroundBlurModifier: _backgroundBlurModifier = new _backgroundBlurModifier()

@ObservedV2
class _backgroundBlurModifierAuto implements AttributeModifier<CommonAttribute> {
    @Trace backgroundColor: ResourceColor = Color.White

    applyNormalAttribute(instance: CommonAttribute): void {
        if(__CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial){
            if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK) {
                this.backgroundColor = "#b2292929" //模糊背景黑
            } else if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_LIGHT) {
                this.backgroundColor = '#b2e7e9ec' //模糊背景白
            }
        }else{
            if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK) {
                this.backgroundColor = "#292929" //模糊背景黑
            } else if (__System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_LIGHT) {
                this.backgroundColor = '#e7e9ec' //模糊背景白
            }
        }

        if (__System__.pcMode === true) {
            if (deviceInfo.deviceType == "2in1" || deviceInfo.deviceType == "PC") {
                instance
                    .backgroundEffect({
                        radius: __CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial &&__UI__.isSplitLayout ? 100 : 0,
                        saturation: 1.2,
                        brightness: __System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK ?
                            0.9 : 1.1,
                        color: this.backgroundColor
                    })
                    .useEffect(true, EffectType.WINDOW_EFFECT)
            } else {
                instance
                    .backgroundEffect({
                        radius: __CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial &&__UI__.isSplitLayout ? 100 : 0,
                        saturation: 1.2,
                        brightness: __System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK ?
                            0.9 : 1.1,
                        color: this.backgroundColor
                    })
                    .useEffect(true)
            }
        } else {
            instance
                .backgroundEffect({
                    radius: __CustomBackgroundData__.enableCustomBackground && __UI__.advanceMaterial ? 100 : 0,
                    saturation: 1.2,
                    brightness: __System__.currentColorMode == ConfigurationConstant.ColorMode.COLOR_MODE_DARK ? 0.9 :
                        1.1,
                    color: this.backgroundColor
                })
                .useEffect(true)
        }
    }
}

export let backgroundBlurModifierAuto: _backgroundBlurModifierAuto = new _backgroundBlurModifierAuto()

