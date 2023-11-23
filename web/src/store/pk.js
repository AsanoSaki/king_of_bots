export default {
  state: {
    status: "matching",  // 当前状态，matching表示正在匹配，playing表示正在对战
    socket: null,  // 前端和后端建立的链接
    opponent_username: "",  // 对手的用户名
    opponent_photo: "",  // 对手的头像
    game_map: null,  // 游戏地图
  },
  getters: {},
  mutations: {
    updateSocket(state, socket) {
        state.socket = socket;
    },
    updateOpponent(state, opponent) {
        state.opponent_username = opponent.username;
        state.opponent_photo = opponent.photo;
    },
    updateStatus(state, status) {
        state.status = status;
    },
    updateGameMap(state, game_map) {
      state.game_map = game_map;
    },
  },
  actions: {},
  modules: {},
};
