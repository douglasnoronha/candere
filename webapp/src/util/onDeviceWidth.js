// @flow

import invariant from "./invariant";

// stolen from Bootstrap
// https://getbootstrap.com/docs/4.0/layout/overview/
export const BREAKPOINTS = {
    // phones
    small: 576,
    // tablets
    medium: 768,
    // laptops
    large: 992,
    // desktops
    extraLarge: 1200
};

function getDeviceWidth(): number {
    invariant(document.body);
    invariant(document.documentElement);

    return Math.max.apply(Math, [
        document.body.clientWidth,
        document.body.offsetWidth,
        document.documentElement.clientWidth,
        document.documentElement.offsetWidth
    ]);
}

export default function onDeviceWidth(cb: (number) => any): void {
    window.addEventListener("load", () => {
        cb(getDeviceWidth());
    });

    window.addEventListener("resize", () => {
        cb(getDeviceWidth());
    });

    try {
        cb(getDeviceWidth());
    } catch(e) {}
}