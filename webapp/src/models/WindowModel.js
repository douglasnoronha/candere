// @flow

import {decorate, observable} from "mobx";

import onDeviceWidth from "../util/onDeviceWidth";

// used for responsive layouts
class WindowModel {
    deviceWidth: number = 0;
    
    constructor() {
        onDeviceWidth(deviceWidth => (this.deviceWidth = deviceWidth));
    }
}

export default decorate(WindowModel, {
  deviceWidth: observable
});