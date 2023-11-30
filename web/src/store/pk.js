export default {
  state: {
    status: "matching",  // 当前状态，matching表示正在匹配，playing表示正在对战
    socket: null,  // 前端和后端建立的链接
    opponent_username: "",  // 对手的用户名
    opponent_photo: "",  // 对手的头像
    game_map: null,  // 游戏地图
    a_id: 0,
    a_sx: 0,
    a_sy: 0,
    b_id: 0,
    b_sx: 0,
    b_sy: 0,
    gameObject: null,  // 整个GameMap对象
    loser: "none",  // none表示没人输，all表示平局，A/B表示A/B赢
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
    updateGame(state, game) {
      state.game_map = game.map;
      state.a_id = game.a_id;
      state.a_sx = game.a_sx;
      state.a_sy = game.a_sy;
      state.b_id = game.b_id;
      state.b_sx = game.b_sx;
      state.b_sy = game.b_sy;
    },
    updateGameObject(state, gameObject) {
      state.gameObject = gameObject;
    },
    updateLoser(state, loser) {
      state.loser = loser;
    },
  },
  actions: {},
  modules: {},
};
