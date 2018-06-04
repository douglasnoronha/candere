// @flow

import EventEmitter from "events";

import type {JSONResponse} from "./types";

const ENDPOINT = "http://127.0.0.1:27960"

export default class FetchHandler extends EventEmitter {
  _fetch: fetch = window.fetch.bind(window);

  _post(url: string, data: any): Promise<Response> {
    return this._fetch(url, {
      method: 'POST',
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin',
      mode: 'cors'
    });
  }

  fetch(endpoint: string, data?: any): Promise<any> {
    const url = ENDPOINT + endpoint;
    return (data ? this._post(url, data) : this._fetch(url))
      .then(res => res.json())
      .then((json: JSONResponse<any>) => {
        Object.keys(json.data).forEach(key => {
          this.emit(key, json.data[key]);
        });

        return json.response;
      });
  }
}

type MockRoute = (options: any) => JSONResponse<any> | () => JSONResponse<any>;

export class MockFetchHandler extends FetchHandler {
  routes: {[key: string]: MockRoute} = {};

  constructor() {
    super();
    this._fetch = (url: string, options: any) => {
      if (this.routes[url]) {
        return Promise.resolve({
          json: () => {
            return Promise.resolve(this.routes[url](options)); 
          }
        });
      }
      throw new Error("ok");
    };
  }

  route(key: string, cb: MockRoute): void {
    this.routes[key] = cb;
  }
}