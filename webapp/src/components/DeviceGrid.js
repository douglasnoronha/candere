// @flow

import React, {Component} from "react";

import { Link } from "react-router-dom";

import {observer, inject} from "mobx-react";

import {GridList, GridListTile, GridListTileBar, ListSubheader} from "@material-ui/core";

import {BREAKPOINTS} from "../util/onDeviceWidth";

import type WindowModel from "../models/WindowModel";
import type DeviceGridModel from "../models/DeviceGridModel";

const styles = {
    root: {
        height: "100%",
        overflowX: "hidden",
        overflowY: "auto"
    },
    grid: {
        height: "auto",
        width: "100%"
    }
};

export type Props = {
    style: any,
    models: {
        windowModel: WindowModel,
        deviceGridModel: DeviceGridModel
    }
};

class DeviceGrid extends Component<Props, {fetched: boolean}> {
    constructor() {
        super();
        this.state = { fetched: false };
    }

    componentDidMount() {
        const {models: {deviceGridModel}} = this.props;

        if (!this.state.fetched) {
            deviceGridModel.fetch();
            this.setState({ fetched: true });
        }
    }

    render() {
        console.log("render device grid");
        const {models: {windowModel, deviceGridModel}, style} = this.props;
    
        const cols = windowModel.deviceWidth < BREAKPOINTS.medium ? 1 : Math.floor(windowModel.deviceWidth / 400);

        const width = cols === 1 ? windowModel.deviceWidth : 400;
        const height = Math.round(width * 9 / 16);

        const gridTileStyle = {width, height};

        return (
            <div style={Object.assign({}, styles.root, style)}>
                <GridList
                    style={styles.grid}
                    cellHeight="auto"
                    cols={cols}>
    
                    <GridListTile key="header" cols={cols} style={{height: "auto"}}>
                        <ListSubheader component="div">Devices</ListSubheader>
                    </GridListTile>
    
                    {
                        [...deviceGridModel.items.entries()].map(([key, item]) => {
                            return (
                                <Link key={item.id} to={"/devices/" + key}>
                                    <GridListTile style={gridTileStyle} cols={1}>
                                        { item.snapshotUrl ? <img src={item.snapshotUrl} alt="Camera feed" /> : undefined }
                                        <GridListTileBar
                                            title={item.nickname}
                                            subtitle={item.address}
                                        />
                                    </GridListTile>
                                </Link>
                            );
                        })
                    }
                </GridList>
            </div>
        );
    }
}

export default inject("models")(observer(DeviceGrid));