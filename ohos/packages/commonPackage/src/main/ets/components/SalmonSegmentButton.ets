import { XAnimation } from '../models/classes/AnimationManager'

@ComponentV2
export struct SalmonSegmentButton {
  @Local private areaHeight: number = 0
  @Local private areaWidth: number = 0
  @Param @Require buttonList: ResourceStr[]
  @Param index: number = 0
  @Local private currentIndex: number = 0
  @Local private blockOffset: number = 0
  @Event onChange: (index: number) => void
  @Param modifier ?: AttributeModifier<CommonAttribute> = undefined

  @Monitor('index')
  indexChange() {
    this.currentIndex = this.index
    this.calcBlockOffset(true)
  }

  calcBlockOffset(animate: boolean) {
    XAnimation.runWithAnimation(() => this.blockOffset = this.currentIndex * this.areaWidth / this.buttonList.length,
      animate)
  }

  build() {
    Stack({ alignContent: Alignment.Start }) {
      Stack() {
        Button({ type: ButtonType.Capsule }).size({ width: '100%', height: '100%' })
          .backgroundColor($r('sys.color.comp_background_primary'))
      }.width(this.areaWidth / this.buttonList.length).height('100%').padding(2).offset({ x: this.blockOffset })

      Row() {
        ForEach(this.buttonList, (text: string, index: number) => {
          Button() {
            Text(text)
              .fontColor($r('sys.color.font'))
              .opacity(this.currentIndex == index ? 1 : 0.6)
              .maxLines(1)
              .padding({ left: 5, right: 5 })
              .textAlign(TextAlign.Center)
              .fontWeight(this.currentIndex == index ? FontWeight.Medium : FontWeight.Normal)
              .animation({ duration: 300 })
              .maxFontSize(Math.max(Math.min(this.areaHeight, this.areaWidth / this.buttonList.length) / 2, 8))
              .minFontSize(8)
              .textOverflow({ overflow: TextOverflow.MARQUEE })
          }
          .backgroundColor(Color.Transparent)
          .height(this.areaHeight - 4)
          .width(this.areaWidth / this.buttonList.length - 4)
          .onClick(() => {
            this.currentIndex = index
            this.calcBlockOffset(true)
          })

        })
      }.justifyContent(FlexAlign.SpaceAround).width('100%').hitTestBehavior(HitTestMode.Transparent)
    }
    .size({ width: '100%', height: '100%' })
    .borderRadius(9999)
    .backgroundColor(this.modifier? Color.Transparent: $r('sys.color.comp_background_secondary'))
    .attributeModifier(this.modifier)
    .onTouch((event) => {
      if (event.type == TouchType.Down) {
        XAnimation.runWithAnimation(() => this.blockOffset =
          Math.max(0, Math.min(this.areaWidth - this.areaWidth / this.buttonList.length,
            event.touches[0].x - this.areaWidth / this.buttonList.length / 2)))
      }
      if (event.type == TouchType.Move) {
        this.blockOffset = Math.max(0, Math.min(this.areaWidth - this.areaWidth / this.buttonList.length,
          event.touches[0].x - this.areaWidth / this.buttonList.length / 2))
      }
      if (event.type == TouchType.Up || event.type == TouchType.Cancel) {
        const step = this.areaWidth / this.buttonList.length
        const x = Math.max(0, Math.min(this.areaWidth, event.touches[0].x))
        for (let index = 0; index < this.buttonList.length; index++) {
          if (x >= step * index && x <= step * (index + 1)) {
            this.currentIndex = index
            break
          }
        }
        this.calcBlockOffset(true)
        this.onChange(this.currentIndex)
      }
    })
    .onAreaChange((_, newValue) => {
      this.areaWidth = new Number(newValue.width).valueOf()
      this.areaHeight = new Number(newValue.height).valueOf()
      this.calcBlockOffset(false)
    })
  }
}