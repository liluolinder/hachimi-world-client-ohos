import { backgroundTaskManager } from '@kit.BackgroundTasksKit';
import { BusinessError } from '@kit.BasicServicesKit';
import { wantAgent, WantAgent } from '@kit.AbilityKit';
import { XContext } from 'commonpackage';

export  function  OnContinuousTaskCancel() {
  try {
    backgroundTaskManager.on("continuousTaskCancel", callback);
    console.info(`Succeeded in operationing OnContinuousTaskCancel.`);
  } catch (error) {
    console.error(`Operation OnContinuousTaskCancel failed. code is ${(error as BusinessError).code} message is ${(error as BusinessError).message}`);
  }
}

export function OffContinuousTaskCancel() {
  try {
    // callback参数不传，则取消所有已注册的回调
    backgroundTaskManager.off("continuousTaskCancel", callback);
    console.info(`Succeeded in operationing OffContinuousTaskCancel.`);
  } catch (error) {
    console.error(`Operation OffContinuousTaskCancel failed. code is ${(error as BusinessError).code} message is ${(error as BusinessError).message}`);
  }
}

// 申请长时任务.then()写法
export function startContinuousTask() {
  let wantAgentInfo: wantAgent.WantAgentInfo = {
    // 点击通知后，将要执行的动作列表
    // 添加需要被拉起应用的bundleName和abilityName
    wants: [
      {
        bundleName: "world.hachimi.app",
        abilityName: "EntryAbility"
      }
    ],
    // 指定点击通知栏消息后的动作是拉起ability
    actionType: wantAgent.OperationType.START_ABILITY,
    // 使用者自定义的一个私有值
    requestCode: 0,
    // 点击通知后，动作执行属性
    actionFlags: [wantAgent.WantAgentFlags.UPDATE_PRESENT_FLAG],
  };

  try {
    // 通过wantAgent模块下getWantAgent方法获取WantAgent对象
    wantAgent.getWantAgent(wantAgentInfo).then((wantAgentObj: WantAgent) => {
      try {
        let list: Array<string> = ["audioPlayback"];
        backgroundTaskManager.startBackgroundRunning(XContext.getAbilityContext(), list, wantAgentObj).then((res: backgroundTaskManager.ContinuousTaskNotification) => {
          console.info("Operation startBackgroundRunning succeeded");
          // 此处执行具体的长时任务逻辑，如录音，录制等。
        }).catch((error: BusinessError) => {
          console.error(`Failed to Operation startBackgroundRunning. code is ${error.code} message is ${error.message}`);
        });
      } catch (error) {
        console.error(`Failed to Operation startBackgroundRunning. code is ${(error as BusinessError).code} message is ${(error as BusinessError).message}`);
      }
    });
  } catch (error) {
    console.error(`Failed to Operation getWantAgent. code is ${(error as BusinessError).code} message is ${(error as BusinessError).message}`);
  }
}

// 取消长时任务.then()写法
export function stopContinuousTask() {
  backgroundTaskManager.stopBackgroundRunning(XContext.getAbilityContext()).then(() => {
    console.info(`Succeeded in operationing stopBackgroundRunning.`);
  }).catch((err: BusinessError) => {
    console.error(`Failed to operation stopBackgroundRunning. Code is ${err.code}, message is ${err.message}`);
  });
}

function callback(info: backgroundTaskManager.ContinuousTaskCancelInfo) {
  // 长时任务id
  console.info('OnContinuousTaskCancel callback id ' + info.id);
  // 长时任务取消原因
  console.info('OnContinuousTaskCancel callback reason ' + info.reason);
}