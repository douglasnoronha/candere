// @flow

import "./index.css";

import React from "react";
import ReactDOM from "react-dom";

import { CssBaseline, MuiThemeProvider, createMuiTheme } from "@material-ui/core";

import { Provider } from "mobx-react";

import { Switch, Route } from "react-router";
import { BrowserRouter } from "react-router-dom";

import registerServiceWorker from "./registerServiceWorker";

import App from "./components/App";
import AddDevice from "./components/AddDevice";
import DeviceDetails from "./components/DeviceDetails";

import FetchHandler from "./models/FetchHandler";
import DeviceGridModel from "./models/DeviceGridModel";
import DeviceStatsModel from "./models/DeviceGridModel";
import WindowModel from "./models/WindowModel";

import type {DeviceData} from "./models/DeviceGridModel";
import type {JSONResponse} from "./models/types";

import invariant from "./util/invariant";

const theme = createMuiTheme({
    typography: {
        fontFamily: [
            "Lato",
            "sans-serif"
        ]
    }
})

window.addEventListener("load", () => {
    const root = document.querySelector("#root");
    invariant(root, "root element missing");

    const handler = new FetchHandler();

    const models = {
        windowModel: new WindowModel(),
        deviceGridModel: new DeviceGridModel(handler),
        deviceStatsModel: new DeviceStatsModel(handler)
    };
    
    ReactDOM.render((
        <MuiThemeProvider theme={theme}>
            <CssBaseline />
            <Provider models={models}>
                <BrowserRouter basename="/static">
                    <Switch >
                        <Route exact path="/" component={App} />
                        <Route path="/add" component={AddDevice} />
                        <Route
                            path="/devices/:id"
                            component={matchProps => <DeviceDetails {...matchProps.match.params} />}
                        />
                    </Switch>
                </BrowserRouter>
            </Provider>
        </MuiThemeProvider >
    ), root);
    
    registerServiceWorker();
});