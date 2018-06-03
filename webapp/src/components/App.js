// @flow

import React from "react";

import { Link } from "react-router-dom";

import {observer, inject} from "mobx-react";
import copy from "clipboard-copy";

import {AppBar, Toolbar, Typography, IconButton, Paper, Button} from "@material-ui/core";

import Add from "@material-ui/icons/Add";
import Refresh from "@material-ui/icons/Refresh";

import Expander from "./Expander";
import DeviceGrid from "./DeviceGrid";

import type {Props} from "./DeviceGrid";

import bridge from "../bridge";

const styles = {
  root: {
    display: "flex",
    flexDirection: "column",
    width: "100%",
    height: "100%"
  },
  title: {
    fontWeight: "bold",
    textTransform: "uppercase",
    letterSpacing: "0.3rem"
  },
  content: {
    flex: 1,
    position: "relative",
    minHeight: 0
  },
  fab: {
    position: "absolute",
    bottom: 16,
    right: 16
  },
  urlBar: {
    height: 48,
  },
  url: {
    lineHeight: "48px",
    textAlign: "center"
  }
};


const App = ({models: {deviceGridModel}, style}: Props) => {
  const url = bridge ? bridge.getHref() : window.location.href;
  const copyUrl = () => {
    copy(url);
  };

  return (
    <div style={styles.root}>
      {/* app bar */}
      <AppBar elevation={0} position="static" color="default">
        <Toolbar>
          <Typography variant="title" style={styles.title}>Candere</Typography>
          <Expander />
          <IconButton>
            <Refresh onClick={deviceGridModel.invalidate} />
          </IconButton>
        </Toolbar>
      </AppBar>

      {/* main content */}
      <div style={styles.content}>
        <DeviceGrid />
        <Link to="/add">
          <Button variant="fab" color="primary" aria-label="add" style={styles.fab}>
            <Add />
          </Button>
        </Link>
      </div>

      {/* URL bar for server */}
      {bridge ? (
        <Paper style={styles.urlBar} elevation={4}>
          <Typography onClick={copyUrl} style={styles.url} component="p">{url}</Typography>
        </Paper>
      ) : undefined }
    </div>
  );
};

export default inject("models")(observer(App));
