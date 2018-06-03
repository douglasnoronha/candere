// @flow

type Bridge = {
  getHref: () => string,
  launchStreamActivity: (rtsp: string) => any
};

const bridge: ?Bridge = window._candere_bridge;

export default bridge;