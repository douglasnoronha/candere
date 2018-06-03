// @flow

import DataModel from './DataModel';
import FetchHandler from "./FetchHandler";
import type {DataBase} from "./types";

const KEY = "devices_stats";
const ENDPOINT = "/devices/stats";

export type DeviceStats = DataBase & {
  brightness: {
    start: number,
    data: number[],
    // fractions of an hour
    units: number
  }
};

export default class DeviceGridModel extends DataModel<DeviceStats> {
  constructor(handler: FetchHandler) {
    super(handler, KEY, ENDPOINT);
  }
}