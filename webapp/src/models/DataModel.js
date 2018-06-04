// @flow

import path from "path";

import {decorate, observable, action} from "mobx";
import type {ObservableMap} from "mobx";

import FetchHandler from "./FetchHandler";
import type {DataBase, DataUpdate} from "./types";

class DataModel<T: DataBase> {
    handler: FetchHandler;
    key: string;
    endpoint: string;

    _invalidate: boolean = true;

    items: ObservableMap<number, T> = observable.map();
    
    constructor(handler: FetchHandler, key: string, endpoint: string) {
        this.handler = handler;
        this.key = key;
        this.endpoint = endpoint;

        handler.on(key, (res: DataUpdate<T>) => {
            if (res.operation === "+" && res.item) {
                const {id} = res.item;
                this.items.set(id, res.item);
            } else if (res.operation === "-" && res.item) {
                const {id} = res.item;
                this.items.delete(id);
                console.log("delete");
            } else if (res.operation === "=" && res.items) {
                this.items.clear();

                if (Array.isArray(res.items)) {
                    res.items.forEach(item => this.items.set(item.id, item));
                }
            }
        });
    }

    fetch = (): Promise<void> => {
        if (this._invalidate) {
            this._invalidate = false;
            console.log("fetch");
            return this.handler.fetch(path.join(this.endpoint, "fetch"));
        }
        
        return Promise.resolve();
    }

    invalidate = () => {
        this._invalidate = true;
        return this.fetch();
    }

    add = (item: T): Promise<{ success: boolean }> => this.handler.fetch(
        path.join(this.endpoint, "add"),
        item
    );

    remove = (item: T): Promise<{ success: boolean }> => this.handler.fetch(
        path.join(this.endpoint, "remove"),
        item
    );

    refresh = (item: T): Promise<{ success: boolean }> => this.handler.fetch(
        path.join(this.endpoint, "refresh"),
        item
    )
}

export default decorate(DataModel, {
    fetch: action,
    invalidate: action,
    add: action,
    update: action
});