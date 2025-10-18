import { XAnimation, XContext, XPreferencesManager } from 'commonpackage';
import { min_split_width, min_width, __System__, __UI__, __User__ } from '../commons/shareDatas';

import { CustomBackground } from '../../../productDefault/components/CustomBackground';
import { DecorBar } from '../../../productDefault/components/DecorBar';
import { Body } from '../../../productDefault/pages/index/Body';
import { SideBar } from '../../../productDefault/pages/index/SideBar';

@Entry
@ComponentV2
struct Index {
    @Provider('tabController') tabController: TabsController = new TabsController()
    @Provider('currentTabIndex') currentTabIndex: number = XPreferencesManager.getPreferencesValue("pilipala", "currentTabIndex", 0) as number

    async aboutToAppear(): Promise<void> {

    }

    onBackPress(): boolean | void {
    }

    build() {
        Stack({ alignContent: Alignment.TopStart }) {
            if (__System__.pcMode) {
                DecorBar()
                    .zIndex(114514)
            }
            CustomBackground()

            Navigation(__UI__.mainPageStack) {
                SideBarContainer(__UI__.sideBarMode) {
                    SideBar()
                    Body()
                }
                .showSideBar(__UI__.isShowSideBar)
                .autoHide(false)
                .showControlButton(false)
                .sideBarWidth(__UI__.isLargeSidebar? 260: 70)
                .maxSideBarWidth(__UI__.isLargeSidebar? 260: 70)
                .minSideBarWidth(__UI__.isLargeSidebar? 260: 70)
                .gesture(
                    PanGesture({ fingers: 1, direction: PanDirection.Right })
                        .onActionEnd(() => {
                            if (__System__.windowWidth < min_width) {
                                XAnimation.runWithAnimation(() => {
                                    __UI__.isShowSideBar = true
                                })
                            }
                        })
                )
                .gesture(
                    PanGesture({ fingers: 1, direction: PanDirection.Left })
                        .onActionEnd(() => {
                            if (__System__.windowWidth < min_width) {
                                XAnimation.runWithAnimation(() => {
                                    __UI__.isShowSideBar = false
                                })
                            }
                        })
                )

            }
            .backgroundColor(Color.Transparent)
            .layoutWeight(1)
            .hideToolBar(true)
            .navBarWidth(__System__.windowWidth / 2.5 + (__UI__.isLargeSidebar? 190: 0))
            .mode(__UI__.indexNavigationMode)
            .onNavigationModeChange((mode) => {
                __UI__.indexNavigationMode = mode
            })

        }.size({ width: '100%', height: '100%' })
        .backgroundColor($r('app.color.start_window_background'))

    }
}