// @flow

import React, { Component } from "react";

import { Redirect } from "react-router";
import { Link } from "react-router-dom";

import {observer, inject} from "mobx-react";

import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from "@material-ui/core";

import ArrowBack from "@material-ui/icons/ArrowBack";
import Refresh from "@material-ui/icons/Refresh";
import DeleteForever from "@material-ui/icons/DeleteForever";

import Expander from "./Expander";

import type {DeviceData} from "../models/DeviceGridModel";
import type {Props} from "./DeviceGrid";

import bridge from "../bridge";

const styles = {
  root: {
    display: "flex",
    flexDirection: "column",
    width: "100%",
    height: "100%"
  },
  toolbar: {
    paddingLeft: 8
  },
  content: {
    flex: 1,
    display: "flex",
    flexDirection: "column",
    position: "relative",
    minHeight: 0
  },
  img: {
    objectFit: "cover"
  },
  details: {
    display: "flex",
    padding: "16px 24px",
    borderBottom: "1px solid rgba(0,0,0,0.1)"
  },
  button: {
    height: 48
  }
};

type DeviceDetailsProps = Props & {
  id: string
};

type State = {
  fetched: boolean,
  redirect: boolean,
  deleteDialog: boolean,
  lastImageRefresh: number,
  device?: DeviceData
};

class DeviceDetails extends Component<DeviceDetailsProps, State> {
  constructor(props) {
    super(props);
    this.state = { fetched: false, redirect: false, deleteDialog: false, lastImageRefresh: Date.now() };
  }

  componentDidMount() {
    this._update();

    const {models: {deviceGridModel}} = this.props;

    if (!this.state.fetched) {
      deviceGridModel.fetch().then(() => {
        this._update();
      });
      this.setState({ fetched: true });
    }
  }

  _update() {
    const {id, models: {deviceGridModel}} = this.props;
    const idInt = parseInt(id);

    if (deviceGridModel.items.has(idInt)) {
      this.setState({ device: deviceGridModel.items.get(idInt)});
    }
  }

  refreshImage = () => {
    const {models: {deviceGridModel}} = this.props;
  
    if (this.state.device) {
      deviceGridModel.refresh(this.state.device);
    }

    this.setState({ lastImageRefresh: Date.now() });
  }

  launchStreamActivity = () => {
    if (!this.state.device || !this.state.device.rtspUrl) {
      return;
    }

    if (bridge) {
      bridge.launchStreamActivity(this.state.device.rtspUrl);
    }
  };

  delete = () => {
    if (!this.state.device) {
      return;
    }

    const {models: {deviceGridModel}} = this.props;

    deviceGridModel.remove(this.state.device).then(() => {
      this.setState({ redirect: true });
    });
  }

  openDialog = () => {
    this.setState({ deleteDialog: true });
  }

  closeDialog = () => {
    this.setState({ deleteDialog: false });
  }

  render() {
    if (this.state.redirect) {
      return <Redirect push to="/" />;
    }

    if (!this.state.device) {
      return <div />;
    }

    const { device, lastImageRefresh } = this.state;
    const {models: {windowModel}} = this.props;

    const width = windowModel.deviceWidth;
    const height = Math.round(width * 9 / 16);

    return (
      <div style={styles.root}>
        {/* dialog for deleting a device */}
        <Dialog
          open={this.state.deleteDialog}
          onClose={this.closeDialog}>

          <DialogTitle>Are you sure you want to delete this device?</DialogTitle>
          <DialogContent>
            <DialogContentText>
              This operation is current un-reversable, if you continue and wish to reuse the device later you
              will have to readd it.
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={this.closeDialog} color="primary">
              Cancel
            </Button>
            <Button onClick={this.delete} color="secondary">
              Continue
            </Button>
          </DialogActions>
        </Dialog>

        {/* app bar */}
        <AppBar elevation={0} position="static" color="default">
          <Toolbar style={styles.toolbar}>
            <Link to="/">
              <IconButton>
                <ArrowBack />
              </IconButton>
            </Link>
            <Typography variant="title">{device.nickname}</Typography>
          </Toolbar>
        </AppBar>

        {/* main content */}
        <div style={styles.content}>
          { device.snapshotUrl
              ? <img
                  style={styles.img}
                  width={width}
                  height={height}
                  src={device.snapshotUrl + "#" + lastImageRefresh}
                  alt="Camera feed"
                />
              : undefined
          }
          <div style={styles.details}>
            <div>
              <Typography variant="headline" component="h3">
                {device.address}
              </Typography>
              <Typography component="p">
                {device.username}
              </Typography>
            </div>
            <Expander />
            <IconButton color="primary" onClick={this.refreshImage}>
              <Refresh />
            </IconButton>
            <IconButton color="secondary" onClick={this.openDialog}>
              <DeleteForever />
            </IconButton>
          </div>
        </div>

        {/* RTSP link for app */}
        {bridge ? (
          <Button
            variant="raised"
            size="large"
            onClick={this.launchStreamActivity}
            color="primary"
            style={styles.button}>
            Live stream
          </Button>
        ) : undefined}
      </div>
    );
  }
}

export default inject("models")(observer(DeviceDetails));
