// @flow

import DataModel from './DataModel';
import FetchHandler from "./FetchHandler";
import type {DataBase} from "./types";

const KEY = "devices";
const ENDPOINT = "/devices";

export type DeviceData = DataBase & {
  nickname: string,
  address: string,
  username: string,
  password: string,
  manufacturer?: string,
  snapshotUrl?: string,
  rtspUrl?: string
};

export default class DeviceGridModel extends DataModel<DeviceData> {
  constructor(handler: FetchHandler) {
    super(handler, KEY, ENDPOINT);
  }
}