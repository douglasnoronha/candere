// @flow

import React, { Component } from "react";

import { Redirect } from "react-router";
import { Link } from "react-router-dom";

import {observer, inject} from "mobx-react";

import {AppBar, Toolbar, Typography, IconButton, TextField, Button} from "@material-ui/core";
import ArrowBack from "@material-ui/icons/ArrowBack";

import Expander from "./Expander";

import type {Props} from "./DeviceGrid";

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
  form: {
    flex: 1,
    display: "flex",
    flexDirection: "column",
    position: "relative",
    minHeight: 0
  },
  textField: {
    boxSizing: "border-box",
    marginLeft: 36,
    marginRight: 36
  },
  button: {
    height: 48
  }
};

type State = {
  nickname?: string,
  address?: string,
  username?: string,
  password?: string,
  failed: boolean,
  redirect: boolean
};

class AddDevice extends Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {failed: false, redirect: false};
  }

  handleChange = (name, cb) => e => this.setState({ [name]: !!cb ? cb(e) : e.target.value });

  submit = () => {
    const { nickname, address, username, password } = this.state;

    if (!nickname || !address || !username || !password) {
      this.setState({ failed: true });
      return;
    }

    const {models: {deviceGridModel}} = this.props;

    deviceGridModel.add({id: 0, nickname, address, username, password}).then(result => {
      if (result && result.success) {
        this.setState({ redirect: true });
      } else {
        this.setState({ failed: true });
      }
    });
  };

  render() {
    if (this.state.redirect) {
      return <Redirect push to="/" />;
    }

    return (
      <div style={styles.root}>
        {/* app bar */}
        <AppBar elevation={0} position="static" color="default">
          <Toolbar style={styles.toolbar}>
            <Link to="/">
              <IconButton>
                <ArrowBack />
              </IconButton>
            </Link>
            <Typography variant="title">Add Device</Typography>
          </Toolbar>
        </AppBar>

        {/* main content */}
        <form style={styles.form} noValidate autoComplete="off">
          <TextField
            id="nickname"
            label="Nickname"
            style={styles.textField}
            onChange={this.handleChange("nickname")}
            margin="normal"
          />
          <TextField
            id="address"
            label="Address"
            style={styles.textField}
            onChange={this.handleChange("address")}
            margin="normal"
          />
          <TextField
            id="username"
            label="Username"
            style={styles.textField}
            onChange={this.handleChange("username")}
            margin="normal"
          />
          <TextField
            id="password"
            label="Password"
            type="password"
            style={styles.textField}
            onChange={this.handleChange("password")}
            margin="normal"
          />
          <Expander />
          <Button
            variant="raised"
            size="large"
            onClick={this.submit}
            color={!this.state.failed ? "primary" : "secondary"}
            style={styles.button}>
            LOG IN
          </Button>
        </form>
      </div>
    );
  }
}

export default inject("models")(observer(AddDevice));
